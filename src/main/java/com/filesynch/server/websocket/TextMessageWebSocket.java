package com.filesynch.server.websocket;

import com.filesynch.Main;
import com.filesynch.server.Logger;
import com.filesynch.server.Server;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.rmi.RemoteException;

public class TextMessageWebSocket extends TextWebSocketHandler {
    private Server server = Main.server;

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        server = Main.server;
        String login = (String) session.getAttributes().get(Server.CLIENT_LOGIN);
        server.getClientTextMessageSessionHashMap().put(login, session);
        Logger.log("/text/" + login + ": connected");
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) {
        String login = (String) session.getAttributes().get(Server.CLIENT_LOGIN);
        if (server.getClientTextMessageSessionHashMap().get(login) == null) {
            server.getClientTextMessageSessionHashMap().put(login, session);
        }
        String messageString = message.getPayload();
        try {
            server.sendTextMessageToServer(login, messageString);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        String login = (String) session.getAttributes().get(Server.CLIENT_LOGIN);
        server.getClientTextMessageSessionHashMap().remove(login);
        Logger.log("/text/" + login + ": disconnected");
        super.afterConnectionClosed(session, status);
    }
}
