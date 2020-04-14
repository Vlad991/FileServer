package com.filesynch.server;

import com.filesynch.rmi.ServerGuiInt;
import lombok.extern.slf4j.Slf4j;

import java.rmi.RemoteException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

@Slf4j
public class Logger {
    public static ServerGuiInt serverGuiInt;
    private static ExecutorService pool = Executors.newFixedThreadPool(1);

    public static void log(String stringToLog) {
        pool.execute(new Thread(() -> {
            String COLOR = "\033[0;31m";
            String RESET = "\033[0m";
            System.out.println(COLOR + stringToLog + RESET);
            try {
                serverGuiInt.log(stringToLog);
            } catch (RemoteException e) {
                //e.printStackTrace();
            }
        }));
    }

    public static void logYellow(String stringToLog) {
        pool.execute(new Thread(() -> {
            String COLOR = "\033[0;31m";
            String RESET = "\033[0m";
            System.out.println(COLOR + stringToLog + RESET);
            try {
                serverGuiInt.logYellow(stringToLog);
            } catch (RemoteException e) {
                //e.printStackTrace();
            }
        }));
    }

    public static void logBlue(String stringToLog) {
        pool.execute(new Thread(() -> {
            String COLOR = "\033[0;31m";
            String RESET = "\033[0m";
            System.out.println(COLOR + stringToLog + RESET);
            try {
                serverGuiInt.logBlue(stringToLog);
            } catch (RemoteException e) {
                //e.printStackTrace();
            }
        }));
    }

    public static void logGreen(String stringToLog) {
        pool.execute(new Thread(() -> {
            String COLOR = "\033[0;31m";
            String RESET = "\033[0m";
            System.out.println(COLOR + stringToLog + RESET);
            try {
                serverGuiInt.logGreen(stringToLog);
            } catch (RemoteException e) {
                //e.printStackTrace();
            }
        }));
    }

    public static void logRed(String stringToLog) {
        pool.execute(new Thread(() -> {
            String COLOR = "\033[0;31m";
            String RESET = "\033[0m";
            System.out.println(COLOR + stringToLog + RESET);
            try {
                serverGuiInt.logRed(stringToLog);
            } catch (RemoteException e) {
                //e.printStackTrace();
            }
        }));
    }
}
