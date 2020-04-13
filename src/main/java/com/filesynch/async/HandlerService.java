package com.filesynch.async;

import com.filesynch.dto.FilePartDTO;
import lombok.Getter;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.WebSocketSession;

import java.util.Stack;
import java.util.concurrent.ExecutorService;

@Service
public class HandlerService {
    private Stack<Handler> textMessageHandlerStack;
    private Stack<Handler> fileInfoHandlerStack;
    @Getter
    private Stack<Handler> filePartHandlerStack;
    public final int FILE_PART_HANDLER_COUNT = 7;
    private Stack<Handler> commandHandlerStack;

    public HandlerService() {
        this.textMessageHandlerStack = new Stack<>();
        this.fileInfoHandlerStack = new Stack<>();
        this.filePartHandlerStack = new Stack<>();
        this.commandHandlerStack = new Stack<>();
    }

    public synchronized Handler getFilePartHandler(ExecutorService threadPool, WebSocketSession session) throws InterruptedException {
        Handler handler = null;
        if (filePartHandlerStack.size() == FILE_PART_HANDLER_COUNT) {
            while (handler == null) {
                for (Handler h : filePartHandlerStack) {
                    if (!h.isBusy()) {
                        handler = h;
                        break;
                    }
                }
                if (handler == null) {
                    wait();
                }
            }
        } else {
            handler = new Handler(threadPool);
            filePartHandlerStack.push(handler);
        }
        handler.setSocketSession(session);
        return handler;
    }

    public synchronized void freeFilePartHandler(Handler handler) {
        handler.setBusy(false);
        notify();
    }

    public Handler getHandlerByFilePart(FilePartDTO filePartDTO) {
        for (Handler handler : filePartHandlerStack) {
            FilePartDTO filePartDTO1 = (FilePartDTO) handler.getObjectToSend();
            if ((filePartDTO.getHashKey().equals(filePartDTO1.getHashKey())) && (filePartDTO.getFileInfoDTO().getName().equals(filePartDTO1.getFileInfoDTO().getName()))) {
                return handler;
            }
        }
        return null;
    }
}
