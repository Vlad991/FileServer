package com.filesynch.server.interceptor;

import com.filesynch.server.Server;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

public class ClientSecurityInterceptor implements HandshakeInterceptor {

    public ClientSecurityInterceptor() {
    }

    @Override
    public boolean beforeHandshake(ServerHttpRequest request,
                                   ServerHttpResponse response,
                                   WebSocketHandler wsHandler,
                                   Map<String, Object> attributes) throws Exception {
        final ServletServerHttpRequest servletRequest = (ServletServerHttpRequest) request;
        final HttpServletRequest httpServletRequest = servletRequest.getServletRequest();

        String login = httpServletRequest.getHeader(Server.CLIENT_LOGIN);
        if (login != null) {
            attributes.put(Server.CLIENT_LOGIN, login);
        }
        return true;
    }

    @Override
    public void afterHandshake(ServerHttpRequest request,
                               ServerHttpResponse response,
                               WebSocketHandler wsHandler,
                               Exception exception) {
    }
}
