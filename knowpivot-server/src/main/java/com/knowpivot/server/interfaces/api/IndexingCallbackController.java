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

    /**
     * Python Embedding 消费 kafka 消息之后 的回调
     * 详见：knowpivot-task\task\infrastructure\server_client.py
     *
     * @param request Python 推送的请求
     * @return
     */
    @PostMapping("/indexing/callback")
    public Result<IndexingCallbackResponse> indexingCallback(
            @Valid @RequestBody IndexingCallbackRequest request) {
        IndexingCallbackResponse response = documentApplicationService.indexingCallback(request);
        return Result.ok("索引完成", response);
    }
}
