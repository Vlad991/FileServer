package com.filesynch.async;

import com.filesynch.dto.FilePartDTO;

import java.util.concurrent.BlockingQueue;

public class AsyncService {
    private HandlerService handlerService;
    private BlockingQueue<FilePartDTO> filePartDTOQueue;

    public void addFilePartToHandling(FilePartDTO filePartDTO) {
        filePartDTOQueue.add(filePartDTO);
    }

    public void handleFilePart() {
        new Thread(() -> {
            try {
                FilePartDTO filePartDTO = filePartDTOQueue.poll();
                Handler handler = handlerService.getFilePartHandler();
                handler.sendMessage(filePartDTO);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }).start();
    }

    public void notifyHandler(FilePartDTO filePartDTO, boolean isSent) {
        Handler handler = handlerService.getHandlerByFilePart(filePartDTO);
        handler.setObjectIsSent(isSent);
        handler.notify();
    }
}
