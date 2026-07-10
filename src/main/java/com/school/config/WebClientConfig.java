package com.school.config;

import io.netty.channel.ChannelOption;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;

import java.time.Duration;

@Configuration
public class WebClientConfig {

    @Bean
    public WebClient.Builder webClientBuilder() {
        return WebClient.builder();
    }

    @Bean
    public WebClient mpesaWebClient(WebClient.Builder builder) {
        HttpClient httpClient = HttpClient.create()
                .responseTimeout(Duration.ofSeconds(15))
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 5000);

        return builder
                .clientConnector(new ReactorClientHttpConnector(httpClient))
                .build();
    }
}