package com.knowpivot.server.infrastructure.util;

import cn.hutool.core.lang.Snowflake;
import cn.hutool.core.util.IdUtil;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class IdGenerator {

    @Value("${knowpivot.snowflake.worker-id:1}")
    private long workerId;

    @Value("${knowpivot.snowflake.datacenter-id:1}")
    private long datacenterId;

    private Snowflake snowflake;

    @PostConstruct
    public void init() {
        snowflake = IdUtil.getSnowflake(workerId, datacenterId);
        log.info("Snowflake ID generator initialized: workerId={}, datacenterId={}", workerId, datacenterId);
    }

    public long nextId() {
        return snowflake.nextId();
    }

    public String nextIdStr() {
        return snowflake.nextIdStr();
    }
}
