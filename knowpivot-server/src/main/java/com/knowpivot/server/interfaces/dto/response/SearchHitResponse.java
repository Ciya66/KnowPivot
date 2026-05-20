package com.knowpivot.server.interfaces.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SearchHitResponse {
    private String vectorId;
    private String content;
    private String docId;
    private Integer pageNum;
    private Double similarity;
}
