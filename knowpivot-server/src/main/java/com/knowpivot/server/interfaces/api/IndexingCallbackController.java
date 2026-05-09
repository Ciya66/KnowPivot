package com.knowpivot.server.interfaces.api;

import com.knowpivot.server.application.service.DocumentApplicationService;
import com.knowpivot.server.infrastructure.common.Result;
import com.knowpivot.server.interfaces.dto.request.IndexingCallbackRequest;
import com.knowpivot.server.interfaces.dto.response.IndexingCallbackResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/documents")
@RequiredArgsConstructor
public class IndexingCallbackController {

    private final DocumentApplicationService documentApplicationService;

    @PostMapping("/indexing/callback")
    public Result<IndexingCallbackResponse> indexingCallback(
            @Valid @RequestBody IndexingCallbackRequest request) {
        IndexingCallbackResponse response = documentApplicationService.indexingCallback(request);
        return Result.ok("索引完成", response);
    }
}
