package com.filesynch.configuration;

import com.filesynch.server.TextMessageWebSocket;
import com.mybank.controller.websocket.AdminWebSocketController;
import com.mybank.controller.websocket.ClientWebSocketController;
import com.mybank.controller.websocket.ManagerWebSocketController;
import com.mybank.interceptor.AdminSecurityInterceptor;
import com.mybank.interceptor.ClientSecurityInterceptor;
import com.mybank.interceptor.ManagerSecurityInterceptor;
import org.springframework.beans.factory.annotation.Value;
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
    }

    @Bean
    public TextMessageWebSocket textMessageWebSocket(){
        return new TextMessageWebSocket();
    }

    @Bean
    public ManagerWebSocketController managerWebSocketController(){
        return new ManagerWebSocketController();
    }

    @Bean
    public AdminWebSocketController adminWebSocketController(){
        return new AdminWebSocketController();
    }
}
