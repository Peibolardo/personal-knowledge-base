package backend.service.interfaces;

public interface EmbeddingCommandService {

    void embedRequestResponseMessages(String requestId, String responseId);

}
