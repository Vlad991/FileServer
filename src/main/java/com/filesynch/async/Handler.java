package com.filesynch.async;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Getter;
import lombok.Setter;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

@Getter
@Setter
public class Handler {
    private int timeout = 20 * 1000;
    private WebSocketSession socketSession;
    private ThreadService threadService;
    private boolean objectIsSent;
    private ObjectMapper mapper = new ObjectMapper();

    public boolean sendMessage(Object objectToSend) throws InterruptedException {
        Thread thread = threadService.getThread(new Thread(() -> {
            try {
                socketSession.
                        sendMessage(new TextMessage(mapper.writeValueAsString(objectToSend)));
                threadService.returnThread(Thread.currentThread());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }));
        thread.start();
        synchronized (this) {
            this.wait(timeout);
        }
        if (!objectIsSent) {
            return false;
            // todo handling sendMessage result (to iterate one more time)
        }
        return true;
    }
}
