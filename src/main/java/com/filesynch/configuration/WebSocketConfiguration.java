package com.filesynch.configuration;

import com.filesynch.server.interceptor.ClientSecurityInterceptor;
import com.filesynch.server.websocket.*;
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
                .addInterceptors(new ClientSecurityInterceptor())
                .setAllowedOrigins("*");
        registry.addHandler(filePartWebSocket(), "/file-part")
                .addInterceptors(new ClientSecurityInterceptor())
                .setAllowedOrigins("*");
        registry.addHandler(fileInfoWebSocket(), "/file-info")
                .addInterceptors(new ClientSecurityInterceptor())
                .setAllowedOrigins("*");
    }

    @Bean
    public TextMessageWebSocket textMessageWebSocket(){
        return new TextMessageWebSocket();
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
