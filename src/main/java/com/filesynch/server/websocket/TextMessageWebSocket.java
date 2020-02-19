package com.filesynch.server.websocket;

import com.filesynch.Main;
import com.filesynch.server.Server;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

public class TextMessageWebSocket extends TextWebSocketHandler {
    private Server server = Main.server;

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        server = Main.server;
        String login = (String) session.getAttributes().get(Server.CLIENT_LOGIN);
        server.getClientTextMessageSessionHashMap().put(login, session);
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) {
        String login = (String) session.getAttributes().get(Server.CLIENT_LOGIN);
        if (server.getClientTextMessageSessionHashMap().get(login) == null) {
            server.getClientTextMessageSessionHashMap().put(login, session);
        }
        String messageString = message.getPayload();
        server.sendTextMessageToServer(login, messageString);
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        String login = (String) session.getAttributes().get(Server.CLIENT_LOGIN);
        server.getClientTextMessageSessionHashMap().remove(login);
        super.afterConnectionClosed(session, status);
    }
}
