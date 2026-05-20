package com.knowpivot.server.interfaces.api;

import com.knowpivot.server.domain.service.KnowledgeSearchService;
import com.knowpivot.server.infrastructure.common.Result;
import com.knowpivot.server.interfaces.dto.response.SearchHitResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/knowledge")
@RequiredArgsConstructor
public class KnowledgeController {

    private final KnowledgeSearchService knowledgeSearchService;

    @PostMapping("/search")
    public Result<List<SearchHitResponse>> search(
            @RequestParam String indexName,
            @RequestParam String query,
            @RequestParam(defaultValue = "5") int topK,
            @RequestParam(defaultValue = "0.7") double minScore
    ) {
        List<KnowledgeSearchService.SearchHit> hits = knowledgeSearchService.search(indexName, query, topK, minScore);

        List<SearchHitResponse> list = hits.stream().map(searchHit -> SearchHitResponse.builder()
                .vectorId(searchHit.vectorId())
                .content(searchHit.content())
                .docId(searchHit.docId())
                .pageNum(searchHit.pageNum())
                .similarity(searchHit.similarity())
                .build()).toList();

        return Result.ok(list);
    }


}
