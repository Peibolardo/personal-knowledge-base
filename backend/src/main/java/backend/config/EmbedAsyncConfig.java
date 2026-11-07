package backend.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

/**
 * Configuration class for defining the TaskExecutor used to generate and persist message embeddings in the background.
 * ThreadPoolTaskExecutor manages reusable worker threads so embedding work can run independently of the chat request flow.
 */
@Configuration
public class EmbedAsyncConfig {

    /**
     * Creates and configures a Spring-managed TaskExecutor dedicated to message embedding.
     *
     * Core pool size: 2 threads normally available for executing tasks.
     * Maximum pool size: 4 threads when the pool needs to expand.
     * Queue capacity: 50 tasks waiting for an available worker.
     * Thread name prefix: "embedding-" to identify worker threads in logs.
     *
     * @return the configured ThreadPoolTaskExecutor bean
     */
    @Bean(name = "embeddingTaskExecutor")
    public ThreadPoolTaskExecutor embeddingTaskExecutor() {

        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();

        executor.setCorePoolSize(2);
        executor.setMaxPoolSize(4);
        executor.setQueueCapacity(50);
        executor.setThreadNamePrefix("embedding-");

        return executor;
    }
}
