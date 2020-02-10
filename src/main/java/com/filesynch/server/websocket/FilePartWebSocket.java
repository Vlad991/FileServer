package com.filesynch.server.websocket;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.filesynch.Main;
import com.filesynch.dto.FilePartDTO;
import com.filesynch.server.Logger;
import com.filesynch.server.Server;
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
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) {
        try {
            String login = (String) session.getAttributes().get(Server.CLIENT_LOGIN);
            if (server.getClientFilePartSessionHashMap().get(login) == null) {
                server.getClientFilePartSessionHashMap().put(login, session);
            }
            String jsonString = message.getPayload();
            FilePartDTO filePartDTO = mapper.readValue(jsonString, FilePartDTO.class);
            if (filePartDTO.getOrder() == 1) {
                WebSocketSession clientFirstFilePartSession = server.getClientFirstFilePartSessionHashMap().get(login);
                synchronized (clientFirstFilePartSession) {
                    clientFirstFilePartSession.getAttributes().put("first_file_part", filePartDTO);
                    clientFirstFilePartSession.notify();
                }
                return;
            }
            boolean result = server.sendFilePartToServer(login, filePartDTO);
            TextMessage textMessage = new TextMessage(mapper.writeValueAsString(result));
            server.getClientTextMessageSessionHashMap().get(login).sendMessage(textMessage);
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
