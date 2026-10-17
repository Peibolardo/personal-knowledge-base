package backend.utils.externalServiceServices;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;

import java.time.Duration;

/**
 * Configuration class to set up the URI for the Stream Request to the AI Service
 */
@Configuration
public class WebClientConfig {

    /**
     * Creates Bean to call WebClient with the configuration
     * Added a timeout of 60 secs in case no more events are received to stop the communication.
     * @param builder WebClient.Builder object to build and create the request
     * @param baseUrl Environment variable with the URL
     * @return WebClient object
     */
    @Bean
    public WebClient aiWebClient(WebClient.Builder builder, @Value("${python.service.url.api}") String baseUrl){

        HttpClient httpClient = HttpClient.create()
                .responseTimeout(Duration.ofSeconds(60));

        return builder
                .baseUrl(baseUrl)
                .clientConnector(new ReactorClientHttpConnector(httpClient))
                .build();

    }

}