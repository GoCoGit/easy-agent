package ink.goco.agent.service;

import java.io.IOException;

public interface ChatService {
    void common(String msg);

    void rag(String msg);

    void web(String msg) throws IOException;
}
