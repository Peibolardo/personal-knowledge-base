package backend.sse.utils;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

/**
 * Configuration class for defining the TaskExecutor used to execute conversation summarization as a background task.
 * ThreadPoolTaskExecutor manages a pool of reusable threads, allowing summarization to run independently of the chat streaming flow.
 * This prevents the LLM summarization call from delaying the DONE event sent to the frontend after the messages have been persisted.
 */
@Configuration
public class AsyncConfig {

    /**
     * Creates and configures a Spring-managed TaskExecutor dedicate to conversation summarization.
     *
     * Core pool size: 2 threads normally available for executing tasks.
     * Maximum pool size: 4 threads when the pool needs to expand.
     * Queue capacity: 50 tasks waiting for an available worker.
     * Thread name prefix: "summary-" to identify worker threads in logs.
     *
     * @return the configured ThreadPoolTaskExecutor bean
     */
    @Bean(name = "summaryTaskExecutor")
    public ThreadPoolTaskExecutor summaryTaskExecutor(){

        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();

        executor.setCorePoolSize(2);
        executor.setMaxPoolSize(4);
        executor.setQueueCapacity(50);
        executor.setThreadNamePrefix("summary-");

        return executor;

    }

}
