package com.knowpivot.server.infrastructure.redis;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import redis.clients.jedis.JedisPooled;
import redis.clients.jedis.args.Rawable;
import redis.clients.jedis.commands.ProtocolCommand;
import redis.clients.jedis.exceptions.JedisDataException;
import redis.clients.jedis.params.ScanParams;
import redis.clients.jedis.resps.ScanResult;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.List;

/**
 * Redis Stack 向量存储 — 使用原始命令封装 FT.CREATE / FT.SEARCH / JSON.SET
 * 绕过 Jedis Search API 兼容性问题，直接发送原始 Redis 命令
 */
@Slf4j
@Component
@Data
@ConfigurationProperties(prefix = "knowpivot.vector")
public class RedisVectorStore {

    private final JedisPooled jedis;

    private int dimension = 1536;
    private String distanceMetric = "COSINE";

    public RedisVectorStore() {
        this.jedis = new JedisPooled("localhost", 6379);
    }

    // ==================== 原始命令封装 ====================

    private static class FtCreateCommand implements ProtocolCommand {
        @Override
        public byte[] getRaw() { return "FT.CREATE".getBytes(); }
    }

    private static class FtSearchCommand implements ProtocolCommand {
        @Override
        public byte[] getRaw() { return "FT.SEARCH".getBytes(); }
    }

    private static class FtDropIndexCommand implements ProtocolCommand {
        @Override
        public byte[] getRaw() { return "FT.DROPINDEX".getBytes(); }
    }

    private static class FtInfoCommand implements ProtocolCommand {
        @Override
        public byte[] getRaw() { return "FT.INFO".getBytes(); }
    }

    private static class JsonSetCommand implements ProtocolCommand {
        @Override
        public byte[] getRaw() { return "JSON.SET".getBytes(); }
    }

    // ==================== 索引管理 ====================

    /**
     * 创建向量索引（JSON 模式）
     */
    public void createIndex(String indexName) {
        if (indexExists(indexName)) {
            log.info("向量索引已存在: {}", indexName);
            return;
        }

        // FT.CREATE <idx> ON JSON PREFIX 1 <prefix> SCHEMA
        //   $.content AS content TEXT
        //   $.kbId AS kbId TAG
        //   $.docId AS docId TAG
        //   $.pageNum AS pageNum NUMERIC
        //   $.embedding AS embedding VECTOR HNSW 6 TYPE FLOAT32 DIM <dim> DISTANCE_METRIC <metric>
        Object result = jedis.sendCommand(new FtCreateCommand(),
                indexName,
                "ON", "JSON",
                "PREFIX", "1", indexName + ":",
                "SCHEMA",
                "$.content", "AS", "content", "TEXT",
                "$.kbId", "AS", "kbId", "TAG",
                "$.docId", "AS", "docId", "TAG",
                "$.pageNum", "AS", "pageNum", "NUMERIC",
                "$.embedding", "AS", "embedding", "VECTOR", "HNSW", "6",
                    "TYPE", "FLOAT32",
                    "DIM", String.valueOf(dimension),
                    "DISTANCE_METRIC", distanceMetric
        );

        log.info("向量索引创建成功: indexName={}, dim={}", indexName, dimension);
    }

    /**
     * 检查索引是否存在
     */
    public boolean indexExists(String indexName) {
        try {
            jedis.sendCommand(new FtInfoCommand(), indexName);
            return true;
        } catch (JedisDataException e) {
            return false;
        }
    }

    /**
     * 删除整个索引及其所有数据
     */
    public void dropIndex(String indexName) {
        try {
            jedis.sendCommand(new FtDropIndexCommand(), indexName, "DD");
            log.info("向量索引及数据已删除: {}", indexName);
        } catch (JedisDataException e) {
            log.warn("删除向量索引失败（可能不存在）: {}", indexName);
        }
    }

    // ==================== 向量存储 ====================

    /**
     * 存储向量（JSON 模式）
     */
    public void storeVector(String indexName, String vectorId, String content,
                            String kbId, String docId, Integer pageNum, float[] embedding) {
        String key = indexName + ":" + vectorId;

        String json = "{" +
                "\"content\":" + toJsonString(content) + "," +
                "\"kbId\":" + toJsonString(kbId) + "," +
                "\"docId\":" + toJsonString(docId) + "," +
                "\"pageNum\":" + (pageNum != null ? pageNum : 0) + "," +
                "\"embedding\":" + floatArrayToJsonArray(embedding) +
                "}";

        jedis.sendCommand(new JsonSetCommand(), key, "$", json);
        log.debug("向量已存储: key={}", key);
    }

    /**
     * 批量存储向量
     */
    public void storeVectors(String indexName, List<VectorData> vectors) {
        for (VectorData v : vectors) {
            storeVector(indexName, v.getVectorId(), v.getContent(),
                    v.getKbId(), v.getDocId(), v.getPageNum(), v.getEmbedding());
        }
        log.info("批量向量存储完成: indexName={}, count={}", indexName, vectors.size());
    }

    // ==================== 向量检索 ====================

    /**
     * 向量相似度检索 — 使用原始 FT.SEARCH 命令
     */
    @SuppressWarnings("unchecked")
    public List<VectorSearchResult> searchSimilar(String indexName, float[] queryVector,
                                                   int topK, double similarityThreshold) {
        byte[] vecParam = floatArrayToBytes(queryVector);

        // FT.SEARCH <idx> "*=>[KNN <topK> @embedding $vec AS score]"
        //   PARAMS 2 vec <bytes>
        //   SORTBY score ASC
        //   DIALECT 2
        Object raw = jedis.sendCommand(new FtSearchCommand(),
                indexName,
                "*=>[KNN " + topK + " @embedding $vec AS score]",
                "PARAMS", "2", "vec", new String(vecParam, java.nio.charset.StandardCharsets.ISO_8859_1),
                "SORTBY", "score", "ASC",
                "LIMIT", "0", String.valueOf(topK),
                "DIALECT", "2"
        );

        // 解析 RESP 响应: [total, key1, [field, val, ...], key2, [...], ...]
        List<Object> response = (List<Object>) raw;
        if (response == null || response.isEmpty()) {
            return new ArrayList<>();
        }

        long total = ((Number) response.get(0)).longValue();
        List<VectorSearchResult> results = new ArrayList<>();

        // COSINE distance ∈ [0,2]，score = distance，similarity = 1 - distance/2
        double maxDistance = 2.0 * (1.0 - similarityThreshold);

        for (int i = 1; i + 1 < response.size(); i += 2) {
            String fullKey = new String((byte[]) response.get(i));
            List<Object> fields = (List<Object>) response.get(i + 1);

            String content = null, kbId = null, docId = null, pageNumStr = null;
            Double score = null;

            // 解析 field-value 对
            for (int j = 0; j + 1 < fields.size(); j += 2) {
                String field = asString(fields.get(j));
                String value = asString(fields.get(j + 1));
                switch (field) {
                    case "content" -> content = value;
                    case "kbId" -> kbId = value;
                    case "docId" -> docId = value;
                    case "pageNum" -> pageNumStr = value;
                    case "score" -> score = Double.parseDouble(value);
                }
            }

            if (score == null) continue;

            // 相似度过滤: score 是余弦距离，越小越相似
            if (score > maxDistance) continue;

            double similarity = 1.0 - score / 2.0;
            results.add(VectorSearchResult.builder()
                    .vectorId(extractVectorId(fullKey, indexName))
                    .content(content)
                    .kbId(kbId)
                    .docId(docId)
                    .pageNum(pageNumStr != null ? Integer.parseInt(pageNumStr) : null)
                    .score(similarity)
                    .build());
        }

        log.debug("向量检索完成: index={}, total={}, filtered={}", indexName, total, results.size());
        return results;
    }

    // ==================== 向量删除 ====================

    /**
     * 删除单个向量
     */
    public void deleteVector(String vectorId) {
        ScanParams params = new ScanParams().match("*:" + vectorId).count(100);
        String cursor = "0";
        do {
            ScanResult<String> scanResult = jedis.scan(cursor, params);
            for (String key : scanResult.getResult()) {
                jedis.del(key);
                log.debug("向量已删除: key={}", key);
            }
            cursor = scanResult.getCursor();
        } while (!"0".equals(cursor));
    }

    /**
     * 删除指定文档下的所有向量
     */
    @SuppressWarnings("unchecked")
    public void deleteVectorsByDocId(String indexName, String docId) {
        try {
            Object raw = jedis.sendCommand(new FtSearchCommand(),
                    indexName, "@docId:{" + docId + "}",
                    "LIMIT", "0", "10000",
                    "DIALECT", "2"
            );
            List<Object> response = (List<Object>) raw;
            if (response == null) return;

            long total = ((Number) response.get(0)).longValue();
            for (int i = 1; i + 1 < response.size(); i += 2) {
                String key = new String((byte[]) response.get(i));
                jedis.del(key);
            }
            log.info("已删除文档向量: index={}, docId={}, count={}", indexName, docId, total);
        } catch (JedisDataException e) {
            log.warn("删除文档向量失败（索引可能不存在）: index={}, docId={}", indexName, docId);
        }
    }

    // ==================== 工具方法 ====================

    private byte[] floatArrayToBytes(float[] floats) {
        ByteBuffer buffer = ByteBuffer.allocate(floats.length * 4).order(ByteOrder.LITTLE_ENDIAN);
        for (float f : floats) {
            buffer.putFloat(f);
        }
        return buffer.array();
    }

    private String floatArrayToJsonArray(float[] floats) {
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < floats.length; i++) {
            if (i > 0) sb.append(",");
            sb.append(floats[i]);
        }
        sb.append("]");
        return sb.toString();
    }

    private String toJsonString(String s) {
        if (s == null) return "\"\"";
        return "\"" + s.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t") + "\"";
    }

    private String extractVectorId(String fullKey, String indexName) {
        String prefix = indexName + ":";
        return fullKey.startsWith(prefix) ? fullKey.substring(prefix.length()) : fullKey;
    }

    private String asString(Object obj) {
        if (obj == null) return null;
        if (obj instanceof byte[] bytes) return new String(bytes, java.nio.charset.StandardCharsets.UTF_8);
        return obj.toString();
    }

    // ==================== 内部数据类 ====================

    @Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class VectorData {
        private String vectorId;
        private String content;
        private String kbId;
        private String docId;
        private Integer pageNum;
        private float[] embedding;
    }

    @Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class VectorSearchResult {
        private String vectorId;
        private String content;
        private String kbId;
        private String docId;
        private Integer pageNum;
        private Double score;
    }
}
