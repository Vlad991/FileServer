package com.filesynch.server.websocket;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.filesynch.Main;
import com.filesynch.async.AsyncService;
import com.filesynch.dto.FilePartDTO;
import com.filesynch.server.Logger;
import com.filesynch.server.Server;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.util.HashMap;

public class FilePartStatusWebSocket extends TextWebSocketHandler {
    private ObjectMapper mapper = new ObjectMapper();
    private Server server = Main.server;
    private HashMap<String, AsyncService> asyncServiceHashMap;

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        server = Main.server;
        String login = (String) session.getAttributes().get(Server.CLIENT_LOGIN);
        if (asyncServiceHashMap == null) {
            asyncServiceHashMap = new HashMap<>();
        }
        asyncServiceHashMap.put(login, server.getAsyncServiceHashMap().get(login));
        server.getClientFilePartStatusSessionHashMap().put(login, session);
        Logger.log("/file-part-status/" + login + ": connected");
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) {
        try {
            String login = (String) session.getAttributes().get(Server.CLIENT_LOGIN);
            if (server.getClientFilePartStatusSessionHashMap().get(login) == null) {
                server.getClientFilePartStatusSessionHashMap().put(login, session);
            }
            String jsonString = message.getPayload();
            FilePartDTO filePartDTO = mapper.readValue(jsonString, FilePartDTO.class);
            boolean result = server.saveFilePartStatus(login, filePartDTO);
            if (result) {
                asyncServiceHashMap.get(login).notifyHandler(filePartDTO, true);
            } else {
                asyncServiceHashMap.get(login).notifyHandler(filePartDTO, false);
            }
        } catch (IOException e) {
            Logger.log(e.getMessage());
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        String login = (String) session.getAttributes().get(Server.CLIENT_LOGIN);
        server.getClientFilePartStatusSessionHashMap().remove(login);
        Logger.log("/file-part-status/" + login + ": disconnected(" + status + ")");
        super.afterConnectionClosed(session, status);
    }
}
