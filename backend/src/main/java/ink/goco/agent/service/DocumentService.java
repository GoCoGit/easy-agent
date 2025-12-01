package ink.goco.agent.service;

import org.springframework.web.multipart.MultipartFile;

public interface DocumentService {

    void uploadRagDoc(MultipartFile file);

}
