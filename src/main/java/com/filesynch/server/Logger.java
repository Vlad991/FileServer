package com.filesynch.server;

import com.filesynch.rmi.ServerGuiInt;
import lombok.extern.slf4j.Slf4j;

import java.rmi.RemoteException;

@Slf4j
public class Logger {
    public static ServerGuiInt serverGuiInt;

    public synchronized static void log(String stringToLog) {
        String COLOR = "\033[0;31m";
        String RESET = "\033[0m";
        System.out.println(COLOR + stringToLog + RESET);
        try {
            serverGuiInt.log(stringToLog);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }
}
