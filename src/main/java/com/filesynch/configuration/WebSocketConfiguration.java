package com.filesynch.configuration;

import com.filesynch.server.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

@Configuration
@EnableWebSocket
public class WebSocketConfiguration implements WebSocketConfigurer {

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(textMessageWebSocket(), "/text")
                .setAllowedOrigins("*");
        registry.addHandler(loginWebSocket(), "/login")
                .setAllowedOrigins("*");
        registry.addHandler(firstFilePartWebSocket(), "/first-file-part")
                .setAllowedOrigins("*");
        registry.addHandler(filePartWebSocket(), "/file-part")
                .setAllowedOrigins("*");
        registry.addHandler(fileInfoWebSocket(), "/file-info")
                .setAllowedOrigins("*");
    }

    @Bean
    public TextMessageWebSocket textMessageWebSocket(){
        return new TextMessageWebSocket();
    }

    @Bean
    public LoginWebSocket loginWebSocket(){
        return new LoginWebSocket();
    }

    @Bean
    public FirstFilePartWebSocket firstFilePartWebSocket(){
        return new FirstFilePartWebSocket();
    }

    @Bean
    public FilePartWebSocket filePartWebSocket(){
        return new FilePartWebSocket();
    }

    @Bean
    public FileInfoWebSocket fileInfoWebSocket(){
        return new FileInfoWebSocket();
    }
}
