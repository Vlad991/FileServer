package com.filesynch.async;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.filesynch.server.Logger;
import lombok.Getter;
import lombok.Setter;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;

@Getter
@Setter
public class Handler {
    private final int TIMEOUT = 20 * 1000;
    private WebSocketSession socketSession;
    private Object objectToSend;
    private boolean objectIsSent;
    private boolean isBusy;
    private ExecutorService threadPool;
    private ObjectMapper mapper;

    public Handler() {
    }

    public Handler(ExecutorService threadPool) {
        this.threadPool = threadPool;
        this.mapper = new ObjectMapper();
        this.isBusy = false;
    }

    public synchronized boolean sendMessage(Object objectToSend, String filePartName) throws InterruptedException {
        this.objectToSend = objectToSend;
        objectIsSent = false;
        CompletableFuture<Void> future = CompletableFuture
                .runAsync(() -> {
                    try {
                        Logger.log("thread-" + Thread.currentThread().getName().substring(Thread.currentThread().getName().length() - 2) + " "
                                + filePartName + " -> " + "sending");
                        socketSession.sendMessage(new TextMessage(mapper.writeValueAsString(objectToSend)));
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }, threadPool);
        future.join();
        synchronized (objectToSend) {
            Logger.log(Thread.currentThread().getName() + " begin wait...");
            long startTime = System.currentTimeMillis();
            objectToSend.wait(TIMEOUT);
            Logger.log(Thread.currentThread().getName() + " stop waiting since " + ((System.currentTimeMillis() - startTime) / 1000.0));
        }
        if (!objectIsSent) {
            return false;
        }
        return true;
    }
}
