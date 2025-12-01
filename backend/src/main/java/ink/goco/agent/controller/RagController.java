package ink.goco.agent.controller;

import ink.goco.agent.service.DocumentService;
import ink.goco.agent.utils.GResult;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("rag")
public class RagController {

    @jakarta.annotation.Resource
    private DocumentService documentService;

    @PostMapping("upload")
    public GResult upload(@RequestParam("file") MultipartFile file) {

        if (file.isEmpty()) {
            return GResult.errorMsg("上传文件不能为空");
        }

        try {
            documentService.uploadRagDoc(file);
            return GResult.ok();
        } catch (IllegalArgumentException e) {
            return GResult.errorMsg(e.getMessage());
        }
    }
}
