package backend.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

/**
 * Configuration class for defining the TaskExecutor used to migrate existing messages into embedding chunks.
 * ThreadPoolTaskExecutor manages reusable worker threads so migration work can run asynchronously.
 */
@Configuration
public class EmbeddingMigrationAsyncConfig {

    /**
     * Creates and configures a Spring-managed TaskExecutor dedicated to embedding migrations.
     *
     * Core pool size: 2 threads normally available for executing migration tasks.
     * Maximum pool size: 4 threads when the pool needs to expand.
     * Queue capacity: 50 tasks waiting for an available worker.
     * Thread name prefix: "embedding-migration-" to identify migration workers in logs.
     * Shutdown behavior: allow queued and running migration tasks to complete when the application shuts down.
     * Termination wait: wait up to 30 seconds for the executor to finish before continuing shutdown.
     *
     * @return the configured ThreadPoolTaskExecutor bean
     */
    @Bean(name = "embeddingMigrationTaskExecutor")
    public ThreadPoolTaskExecutor embeddingMigrationTaskExecutor() {

        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();

        executor.setCorePoolSize(2);
        executor.setMaxPoolSize(4);
        executor.setQueueCapacity(50);
        executor.setThreadNamePrefix("embedding-migration-");

        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(30);

        return executor;
    }
}
