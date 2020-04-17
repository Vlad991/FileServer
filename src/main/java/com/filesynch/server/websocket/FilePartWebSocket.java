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
import java.util.HashMap;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class FilePartWebSocket extends TextWebSocketHandler {
    private ObjectMapper mapper = new ObjectMapper();
    private Server server = Main.server;
    private HashMap<String, ExecutorService> handlerThreadPoolHashMap;

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        server = Main.server;
        String login = (String) session.getAttributes().get(Server.CLIENT_LOGIN);
        server.getClientFilePartSessionHashMap().put(login, session);
        Logger.log("/file-part/" + login + ": connected");
        if (handlerThreadPoolHashMap == null) {
            handlerThreadPoolHashMap = new HashMap<>();
        }
        handlerThreadPoolHashMap
                .put(login,
                Executors.newFixedThreadPool(server.getAsyncServiceHashMap().get(login).getHandlerService().FILE_PART_HANDLER_COUNT));
        Logger.log(String.valueOf(session.getTextMessageSizeLimit()));
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) {
        String login = (String) session.getAttributes().get(Server.CLIENT_LOGIN);
        if (server.getClientFilePartSessionHashMap().get(login) == null) {
            server.getClientFilePartSessionHashMap().put(login, session);
        }
        CompletableFuture.runAsync(() -> {
            try {
                String jsonString = message.getPayload();
                FilePartDTO filePartDTO = mapper.readValue(jsonString, FilePartDTO.class);
                server.sendFilePartToServer(login, filePartDTO);
            } catch (Exception e) {
                e.printStackTrace();
                Logger.log(e.getMessage());
            }
        }, handlerThreadPoolHashMap.get(login));
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        String login = (String) session.getAttributes().get(Server.CLIENT_LOGIN);
        server.getClientFilePartSessionHashMap().remove(login);
        Logger.log("/file-part/" + login + ": disconnected");
        super.afterConnectionClosed(session, status);
    }
}
