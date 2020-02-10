package com.filesynch.server;

import lombok.extern.slf4j.Slf4j;

import javax.swing.*;

@Slf4j
public class Logger {
    public static JTextArea logArea;

    public static void log(String stringToLog) {
        String COLOR = "\033[0;31m";
        String RESET = "\033[0m";
        System.out.println(COLOR + stringToLog + RESET);
        logArea.append(stringToLog);
        logArea.append("\n");
        //log.warn(stringToLog);
    }
}
