package com.filesynch.async;

import com.filesynch.dto.FileInfoDTO;
import com.filesynch.dto.FilePartDTO;
import com.filesynch.dto.TextMessageDTO;
import com.filesynch.server.Logger;
import lombok.Getter;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.WebSocketSession;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Stack;
import java.util.concurrent.ExecutorService;

public class HandlerService {
    private LinkedHashMap<TextMessageDTO, Handler> textMessageHandlerStack;
    private LinkedHashMap<FileInfoDTO, Handler> fileInfoHandlerStack;
    @Getter
    private LinkedHashMap<FilePartDTO, Handler> filePartHandlerStack;
    public int FILE_PART_HANDLER_COUNT;
    private int HANDLER_TIMEOUT;
    private LinkedHashMap<TextMessageDTO, Handler> commandHandlerStack;

    public HandlerService(int filePartHandlerCount, int handlerTimeout) {
        this.FILE_PART_HANDLER_COUNT = filePartHandlerCount;
        this.HANDLER_TIMEOUT = handlerTimeout;
        this.textMessageHandlerStack = new LinkedHashMap<>();
        this.fileInfoHandlerStack = new LinkedHashMap<>();
        this.filePartHandlerStack = new LinkedHashMap<>();
        this.commandHandlerStack = new LinkedHashMap<>();
    }

    public synchronized Handler getFilePartHandler(ExecutorService threadPool,
                                                   WebSocketSession session,
                                                   FilePartDTO filePartDTO) throws InterruptedException {
        Handler handler = null;
        if (filePartHandlerStack.size() == FILE_PART_HANDLER_COUNT) {
            while (handler == null) {
                for (Map.Entry<FilePartDTO, Handler> entry : filePartHandlerStack.entrySet()) {
                    if (!entry.getValue().isBusy()) {
                        handler = entry.getValue();
                        filePartHandlerStack.remove(entry.getKey());
                        filePartHandlerStack.put(filePartDTO, handler);
                        break;
                    }
                }
                if (handler == null) {
                    wait();
                }
            }
        } else {
            handler = new Handler(threadPool, HANDLER_TIMEOUT);
            filePartHandlerStack.put(filePartDTO, handler);
        }
        handler.setBusy(true);
        handler.setSocketSession(session);
        return handler;
    }

    public synchronized void freeFilePartHandler(Handler handler) {
        handler.setBusy(false);
        notify();
    }

    public synchronized Handler getHandlerByFilePart(FilePartDTO filePartDTO) {
        for (Map.Entry<FilePartDTO, Handler> entry : filePartHandlerStack.entrySet()) {
            if ((filePartDTO.getHashKey().equals(entry.getKey().getHashKey())) && (filePartDTO.getFileInfoDTO().getName().equals(entry.getKey().getFileInfoDTO().getName()))) {
                return entry.getValue();
            }
        }
        return null;
    }
}
