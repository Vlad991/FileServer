package com.filesynch.server;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.filesynch.Main;
import com.filesynch.dto.ClientInfoDTO;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;

//@Component
public class LoginWebSocket extends TextWebSocketHandler {
    private ObjectMapper mapper = new ObjectMapper();
    private Server server = Main.server;

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        try {
            //String login = (String) session.getAttributes().get(Server.CLIENT_LOGIN);
            String jsonString = message.getPayload();
            ClientInfoDTO clientInfoDTO = mapper.readValue(jsonString, ClientInfoDTO.class);
            String login = server.loginToServer(clientInfoDTO, session);
            TextMessage textMessage = new TextMessage(mapper.writeValueAsString(login));
            session.sendMessage(textMessage); // todo first get textmesssage session
        } catch (IOException e) {
            Logger.log(e.getMessage());
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        super.afterConnectionClosed(session, status);
    }
}
