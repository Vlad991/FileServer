package com.filesynch.server.websocket;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.filesynch.Main;
import com.filesynch.dto.ClientInfoDTO;
import com.filesynch.server.Logger;
import com.filesynch.server.Server;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;

public class RegistrationWebSocket extends TextWebSocketHandler {
    private ObjectMapper mapper = new ObjectMapper();
    private Server server = Main.server;

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        server = Main.server;
        String name = (String) session.getAttributes().get(Server.CLIENT_NAME);
        server.getRegistrationSessionHashMap().put(name, session);
        Logger.log("/register/" + name + ": connected");
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) {
        try {
            String name = (String) session.getAttributes().get(Server.CLIENT_NAME);
            if (server.getRegistrationSessionHashMap().get(name) == null) {
                server.getRegistrationSessionHashMap().put(name, session);
            }
            String jsonString = message.getPayload();
            ClientInfoDTO clientInfoDTO = mapper.readValue(jsonString, ClientInfoDTO.class);
            server.registerToServer(clientInfoDTO);
        } catch (IOException e) {
            Logger.log(e.getMessage());
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        String name = (String) session.getAttributes().get(Server.CLIENT_NAME);
        server.getRegistrationSessionHashMap().remove(name);
        Logger.log("/register/" + name + ": disconnected");
        super.afterConnectionClosed(session, status);
    }
}
