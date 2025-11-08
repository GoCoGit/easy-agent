package ink.goco.agent.controller;

import ink.goco.agent.entity.ChatEntity;
import ink.goco.agent.utils.CustomTextSplitter;
import ink.goco.agent.utils.LeeResult;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.formula.functions.T;
import org.springframework.ai.document.Document;
import org.springframework.ai.reader.TextReader;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.redis.RedisVectorStore;
import org.springframework.core.io.Resource;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("rag")
@RequiredArgsConstructor
public class RagController {

    private final RedisVectorStore redisVectorStore;

    @PostMapping("uploadRagDoc")
    public LeeResult uploadRagDoc(@RequestParam("file") MultipartFile file) {

        Resource resource = file.getResource();
        TextReader textReader = new TextReader(resource);
        textReader.getCustomMetadata().put("fileName", file.getOriginalFilename());
        List<Document> documents = textReader.get();

        CustomTextSplitter tokenTextSplitter = new CustomTextSplitter();
        List<Document> splitDocuments = tokenTextSplitter.split(documents);
        System.out.println(splitDocuments);

        redisVectorStore.add(splitDocuments);

        return LeeResult.ok();
    }

}
