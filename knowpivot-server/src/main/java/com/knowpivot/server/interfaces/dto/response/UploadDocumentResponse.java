package com.knowpivot.server.interfaces.dto.response;

import com.knowpivot.server.infrastructure.annotation.JsonLongId;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UploadDocumentResponse {

    @JsonLongId
    private Long docId;
    private Integer status;
    private LocalDateTime createTime;
}
