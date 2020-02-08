package com.filesynch.server;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.filesynch.Main;
import com.filesynch.dto.FilePartDTO;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;

//@Component
public class FilePartWebSocket extends TextWebSocketHandler {
    private ObjectMapper mapper = new ObjectMapper();
    private Server server = Main.server;

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        String login = (String) session.getAttributes().get(Server.CLIENT_LOGIN);
        if (!server.clientIsLoggedIn(login)) {
            TextMessage textMessage = new TextMessage(mapper.writeValueAsString("You are not logged in"));
            session.sendMessage(textMessage);
            session.close();
        }
        server.getClientFilePartSessionHashMap().put(login, session);
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) {
        try {
            String login = (String) session.getAttributes().get(Server.CLIENT_LOGIN);
            String jsonString = message.getPayload();
            FilePartDTO filePartDTO = mapper.readValue(jsonString, FilePartDTO.class);
            boolean result = server.sendFilePartToServer(login, filePartDTO);
            TextMessage textMessage = new TextMessage(mapper.writeValueAsString(result));
            session.sendMessage(textMessage);
        } catch (IOException e) {
            Logger.log(e.getMessage());
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        String login = (String) session.getAttributes().get(Server.CLIENT_LOGIN);
        server.getClientFilePartSessionHashMap().remove(login);
        super.afterConnectionClosed(session, status);
    }
}
