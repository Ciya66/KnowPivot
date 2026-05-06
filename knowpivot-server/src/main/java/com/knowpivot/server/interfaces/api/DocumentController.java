package com.knowpivot.server.interfaces.api;

import com.knowpivot.server.application.service.DocumentApplicationService;
import com.knowpivot.server.infrastructure.common.PageResult;
import com.knowpivot.server.infrastructure.common.Result;
import com.knowpivot.server.interfaces.dto.response.DocumentResponse;
import com.knowpivot.server.interfaces.dto.response.UploadDocumentResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

/**
 * 文档管理接口
 */
@RestController
@RequestMapping("/api/v1/knowledge-bases/{kbId}/documents")
@RequiredArgsConstructor
public class DocumentController {

    private final DocumentApplicationService documentApplicationService;

    /**
     * 上传文档
     */
    @PostMapping("/upload")
    public Result<UploadDocumentResponse> upload(
            @PathVariable Long kbId,
            @RequestParam("file") MultipartFile file,
            @RequestParam(required = false) String fileName) {
        DocumentResponse response = documentApplicationService.upload(kbId, file, fileName);
        return Result.ok("上传成功，正在解析中",
                new UploadDocumentResponse(response.getDocId(), response.getStatus(), response.getCreateTime()));
    }

    /**
     * 获取文档列表
     */
    @GetMapping
    public Result<PageResult<DocumentResponse>> list(
            @PathVariable Long kbId,
            @RequestParam(required = false) Integer status,
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        PageResult<DocumentResponse> result = documentApplicationService.list(kbId, status, pageNum, pageSize);
        return Result.ok(result);
    }

    /**
     * 删除文档
     */
    @DeleteMapping("/{docId}")
    public Result<Void> delete(@PathVariable Long kbId, @PathVariable Long docId) {
        documentApplicationService.delete(docId);
        return Result.ok("删除成功", null);
    }
}
