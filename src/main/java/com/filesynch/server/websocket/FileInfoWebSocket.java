package com.filesynch.server.websocket;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.filesynch.Main;
import com.filesynch.dto.FileInfoDTO;
import com.filesynch.server.Logger;
import com.filesynch.server.Server;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;

//@Component
public class FileInfoWebSocket extends TextWebSocketHandler {
    private ObjectMapper mapper = new ObjectMapper();
    private Server server = Main.server;

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) {
        try {
            String login = (String) session.getAttributes().get(Server.CLIENT_LOGIN);
            if (server.getClientFileInfoSessionHashMap().get(login) == null) {
                server.getClientFileInfoSessionHashMap().put(login, session);
            }
            String jsonString = message.getPayload();
            FileInfoDTO fileInfoDTO = mapper.readValue(jsonString, FileInfoDTO.class);
            boolean result = server.sendFileInfoToServer(login, fileInfoDTO);
            TextMessage textMessage = new TextMessage(String.valueOf(result));
            server.getClientTextMessageSessionHashMap().get(login).sendMessage(textMessage);
        } catch (IOException e) {
            Logger.log(e.getMessage());
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        String login = (String) session.getAttributes().get(Server.CLIENT_LOGIN);
        server.getClientFileInfoSessionHashMap().remove(login);
        super.afterConnectionClosed(session, status);
    }
}