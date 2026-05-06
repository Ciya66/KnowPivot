package com.knowpivot.server.interfaces.dto.response;

import com.knowpivot.server.infrastructure.annotation.JsonLongId;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DocumentResponse {

    @JsonLongId
    private Long docId;
    private String fileName;
    private Long fileSize;
    private Integer status;
    private Integer chunkCount;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
