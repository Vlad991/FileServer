package com.filesynch.server.websocket;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.filesynch.Main;
import com.filesynch.dto.FileInfoDTO;
import com.filesynch.dto.FileStatus;
import com.filesynch.entity.FileInfoSent;
import com.filesynch.server.Logger;
import com.filesynch.server.Server;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;

public class FileStatusWebSocket extends TextWebSocketHandler {
    private ObjectMapper mapper = new ObjectMapper();
    private Server server = Main.server;

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        server = Main.server;
        String login = (String) session.getAttributes().get(Server.CLIENT_LOGIN);
        server.getClientFileStatusSessionHashMap().put(login, session);
        Logger.log("/file-status/" + login + ": connected");
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) {
        try {
            String login = (String) session.getAttributes().get(Server.CLIENT_LOGIN);
            if (server.getClientFileStatusSessionHashMap().get(login) == null) {
                server.getClientFileStatusSessionHashMap().put(login, session);
            }
            String jsonString = message.getPayload();
            FileInfoDTO fileInfoDTO = mapper.readValue(jsonString, FileInfoDTO.class);
            if (fileInfoDTO.getFileStatus() == FileStatus.TRANSFERRED) {
                Logger.log("File with hash: " + fileInfoDTO.getHash() + " SENT");
            } else {
                Logger.log("File with hash: " + fileInfoDTO.getHash() + " NOT SENT");
            }
            FileInfoSent fileInfo = server.getFileInfoSentRepository().findByHashAndName(fileInfoDTO.getHash(),
                    fileInfoDTO.getName());
            fileInfo.setFileStatus(fileInfoDTO.getFileStatus());
            server.getFileInfoSentRepository().save(fileInfo);
        } catch (IOException e) {
            Logger.log(e.getMessage());
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        String login = (String) session.getAttributes().get(Server.CLIENT_LOGIN);
        server.getClientFileStatusSessionHashMap().remove(login);
        Logger.log("/file-status/" + login + ": disconnected");
        super.afterConnectionClosed(session, status);
    }
}
