package com.filesynch.server;

import com.filesynch.rmi.ServerGuiInt;
import lombok.extern.slf4j.Slf4j;

import java.rmi.RemoteException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.logging.FileHandler;
import java.util.logging.SimpleFormatter;

@Slf4j
public class Logger {
    public static ServerGuiInt serverGuiInt;
    private static ExecutorService pool = Executors.newFixedThreadPool(1);
    private static java.util.logging.Logger logger = java.util.logging.Logger.getLogger(Logger.class.getName());
    static {
        FileHandler fh;
        try {
            // This block configure the logger with handler and formatter
            fh = new FileHandler("logs/server.log");
            logger.addHandler(fh);
            SimpleFormatter formatter = new SimpleFormatter();
            fh.setFormatter(formatter);
            // the following statement is used to log any messages
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void log(String stringToLog) {
        pool.execute(new Thread(() -> {
            String COLOR = "\033[0;31m";
            String RESET = "\033[0m";
            System.out.println(COLOR + stringToLog + RESET);
            try {
                logger.info(stringToLog);
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
                logger.info(stringToLog);
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
                logger.info(stringToLog);
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
                logger.info(stringToLog);
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
                logger.info(stringToLog);
                serverGuiInt.logRed(stringToLog);
            } catch (RemoteException e) {
                //e.printStackTrace();
            }
        }));
    }
}
