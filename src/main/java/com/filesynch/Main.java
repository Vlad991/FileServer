package com.filesynch;

import com.filesynch.dto.ServerStatus;
import com.filesynch.entity.FileInfoReceived;
import com.filesynch.entity.FileInfoSent;
import com.filesynch.gui.FileSynchronizationServer;
import com.filesynch.gui.NewClient;
import com.filesynch.server.Logger;
import com.filesynch.server.Server;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.web.socket.WebSocketSession;

import javax.swing.*;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@SpringBootApplication
public class Main extends SpringBootServletInitializer {
    public static JFrame serverFrame;
    public static FileSynchronizationServer fileSynchronizationServer;
    public static Server server;
    private static String[] stringArgs;
    private static ConfigurableApplicationContext ctx;

    public static void main(String[] args) {
        serverFrame = new JFrame("File Synchronization Server");
        fileSynchronizationServer = new FileSynchronizationServer();
        serverFrame.setContentPane(fileSynchronizationServer.getJPanelServer());
        serverFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        serverFrame.pack();
        serverFrame.setLocationRelativeTo(null);
        serverFrame.setVisible(true);
        stringArgs = args;
    }

    public static void startServer(int port) {
        if (server == null || server.getServerStatus() == ServerStatus.SERVER_STANDBY_FULL) {
            try {
                server = new Server();
                ctx = SpringApplication.run(Main.class, stringArgs);

                Logger.logArea = fileSynchronizationServer.getJTextAreaLog();
                fileSynchronizationServer.getJLabelServerInfoValue().setText("127.0.0.1:" + port);
                server.setFileProgressBar(fileSynchronizationServer.getJProgressBarFile());
                Logger.log("File Server Started");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        updateClientList();
        updateFileQueue();
    }

    public static void stopServer() {
        if (server.getServerStatus() == ServerStatus.SERVER_WORK || server.getServerStatus() == ServerStatus.SERVER_STANDBY_TRANSFER) {
            SpringApplication.exit(ctx);
            Logger.log("File Server Stopped");
        }
    }

    public static JFrame showNewClientIcon(WebSocketSession client, NewClient newClient) {
        JFrame newClientFrame = new JFrame("New Client");
        newClient.setClient(client);
        newClientFrame.setContentPane(newClient.getJPanelMain());
        newClientFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        newClientFrame.pack();
        newClientFrame.setLocationRelativeTo(null);
        newClientFrame.setVisible(true);
        return newClientFrame;
    }

    public static void hideNewClientIcon(JFrame jFrame) {
        jFrame.setVisible(false);
    }

    public static void updateClientList() {
        HashMap<String, WebSocketSession> loginSessionHashMap = server.getLoginSessionHashMap();
        DefaultListModel demoList = new DefaultListModel();
        loginSessionHashMap.forEach((login, session) -> {
            demoList.addElement(login);
        });
        fileSynchronizationServer.getJListClientList().setModel(demoList);
    }

    public static void updateFileQueue() {
        List<FileInfoReceived> fileInfoReceivedList = server.getFileInfoReceivedRepository().findAll();
        List<FileInfoSent> fileInfoSentList = server.getFileInfoSentRepository().findAll();
        //List<FilePartReceived> filePartReceivedList = server.getFilePartReceivedRepository().findAll();
        //List<FilePartSent> filePartSentList = server.getFilePartSentRepository().findAll();


        DefaultListModel demoReceivedList = new DefaultListModel();
        DefaultListModel demoSentList = new DefaultListModel();
        for (FileInfoReceived f : fileInfoReceivedList) {
            demoReceivedList.addElement("" + f.getId() + ". " + f.getName() + " receiving from " + f.getClient().getLogin());
        }
        for (FileInfoSent f : fileInfoSentList) {

            demoSentList.addElement("" + f.getId() + ". " + f.getName() + " sending to " + f.getClient().getLogin());
        }

        fileSynchronizationServer.getJListQueueReceiving().setModel(demoReceivedList);
        fileSynchronizationServer.getJListQueueSending().setModel(demoSentList);
    }

    public static void sendMessage(String login, String message) {
        server.sendTextMessageToClient(login, message);
    }

    public static void sendFileFast(String login, String file) {
        server.sendFileToClientFast(login, file);
    }

    public static void sendFile(String login, String file) {
        if (!(file.charAt(0) == '.')) {
            server.sendFileToClient(login, file);
        }
    }

    public static void sendAllFilesFast(String login) {
        try (Stream<Path> walk = Files.walk(Paths.get(server.FILE_OUTPUT_DIRECTORY
                .substring(0, server.FILE_OUTPUT_DIRECTORY.length() - 1)))) {
            List<String> result = walk.filter(Files::isRegularFile)
                    .map(x -> x.toString()).collect(Collectors.toList());
            for (String filePath : result) {
                sendFile(login, filePath.replace(server.FILE_OUTPUT_DIRECTORY, ""));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void sendAllFiles(String login) {
        server.sendAllFilesToClient(login);
    }
}
