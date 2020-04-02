package com.filesynch.async;

import com.filesynch.dto.FilePartDTO;

import java.util.Stack;

public class HandlerService {
    private Stack<Handler> textMessageHandlerStack;
    private Stack<Handler> fileInfoHandlerStack;
    private Stack<Handler> filePartHandlerStack;
    private Stack<Handler> commandHandlerStack;

    public Handler getFilePartHandler() {
        return filePartHandlerStack.pop();
    }

    public Handler getHandlerByFilePart(FilePartDTO filePartDTO) {
        return filePartHandlerStack.remove();
    }
}
