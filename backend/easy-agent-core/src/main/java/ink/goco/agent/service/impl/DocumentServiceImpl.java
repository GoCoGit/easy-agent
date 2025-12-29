package ink.goco.agent.service.impl;

import ink.goco.agent.constant.RagFileType;
import ink.goco.agent.service.DocumentService;
import ink.goco.agent.utils.CustomTextSplitter;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.document.Document;
import org.springframework.ai.reader.TextReader;
import org.springframework.ai.vectorstore.redis.RedisVectorStore;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Service
@RequiredArgsConstructor
public class DocumentServiceImpl implements DocumentService {

    private final RedisVectorStore redisVectorStore;

    /**
     * 上传RAG文档
     * TODO 文档去重
     */
    @Override
    public void uploadRagDoc(MultipartFile file) {
        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || !originalFilename.contains(".")) {
            throw new IllegalArgumentException("文件必须包含拓展名");
        }

        String fileExtension = originalFilename.substring(originalFilename.lastIndexOf(".") + 1).toLowerCase();
        if (!isValidExtension(fileExtension)) {
            throw new IllegalArgumentException("不支持的文件类型: " + fileExtension);
        }

        Resource resource = file.getResource();
        TextReader textReader = new TextReader(resource);
        textReader.getCustomMetadata().put("fileName", file.getOriginalFilename());
        List<Document> documents = textReader.get();

        CustomTextSplitter tokenTextSplitter = new CustomTextSplitter();
        List<Document> splitDocuments = tokenTextSplitter.split(documents);

        redisVectorStore.add(splitDocuments);
    }


    private boolean isValidExtension(String extension) {
        return RagFileType.isValid(extension);
    }
}
