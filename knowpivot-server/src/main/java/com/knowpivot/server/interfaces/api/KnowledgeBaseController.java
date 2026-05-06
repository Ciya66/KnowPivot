package com.knowpivot.server.interfaces.api;

import com.knowpivot.server.application.service.KnowledgeApplicationService;
import com.knowpivot.server.infrastructure.common.PageResult;
import com.knowpivot.server.infrastructure.common.Result;
import com.knowpivot.server.interfaces.dto.request.CreateKnowledgeBaseRequest;
import com.knowpivot.server.interfaces.dto.request.UpdateKnowledgeBaseRequest;
import com.knowpivot.server.interfaces.dto.response.CreateKnowledgeBaseResponse;
import com.knowpivot.server.interfaces.dto.response.KnowledgeBaseResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 知识库管理接口
 */
@RestController
@RequestMapping("/api/v1/knowledge-bases")
@RequiredArgsConstructor
public class KnowledgeBaseController {

    private final KnowledgeApplicationService knowledgeApplicationService;

    /**
     * 创建知识库
     */
    @PostMapping
    public Result<CreateKnowledgeBaseResponse> create(@Valid @RequestBody CreateKnowledgeBaseRequest request) {
        KnowledgeBaseResponse response = knowledgeApplicationService.create(request);
        return Result.ok("创建成功", new CreateKnowledgeBaseResponse(response.getKbId(), response.getIndexName()));
    }

    /**
     * 获取知识库列表
     */
    @GetMapping
    public Result<PageResult<KnowledgeBaseResponse>> list(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        PageResult<KnowledgeBaseResponse> result = knowledgeApplicationService.list(pageNum, pageSize);
        return Result.ok(result);
    }

    /**
     * 获取知识库详情
     */
    @GetMapping("/{kbId}")
    public Result<KnowledgeBaseResponse> getDetail(@PathVariable Long kbId) {
        KnowledgeBaseResponse response = knowledgeApplicationService.getDetail(kbId);
        return Result.ok(response);
    }

    /**
     * 更新知识库
     */
    @PutMapping("/{kbId}")
    public Result<Void> update(@PathVariable Long kbId,
                               @Valid @RequestBody UpdateKnowledgeBaseRequest request) {
        knowledgeApplicationService.update(kbId, request);
        return Result.ok("更新成功", null);
    }

    /**
     * 删除知识库
     */
    @DeleteMapping("/{kbId}")
    public Result<Void> delete(@PathVariable Long kbId) {
        knowledgeApplicationService.delete(kbId);
        return Result.ok("删除成功", null);
    }
}
