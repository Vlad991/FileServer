package com.filesynch;

import com.filesynch.dto.ServerStatus;
import com.filesynch.entity.FileInfoReceived;
import com.filesynch.entity.FileInfoSent;
import com.filesynch.gui.FileSynchronizationServer;
import com.filesynch.gui.NewClient;
import com.filesynch.server.Server;
import org.springframework.web.socket.WebSocketSession;

import javax.swing.*;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.RMISocketFactory;
import java.rmi.server.UnicastRemoteObject;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Main {
    public static JFrame serverFrame;
    public static FileSynchronizationServer fileSynchronizationServer;
    public static Server server = new Server();
    public static Registry registry;

    public static void main(String[] args) {
        System.setProperty("java.security.policy","src/main/resources/.java.policy");
        System.setProperty("java.rmi.server.hostname", "192.168.0.4");
        //System.setProperty("java.rmi.server.hostname", "46.219.216.25");

        if (System.getSecurityManager() == null) {
            System.setSecurityManager(new SecurityManager());
        }
        serverFrame = new JFrame("File Synchronization Server");
        fileSynchronizationServer = new FileSynchronizationServer();
        serverFrame.setContentPane(fileSynchronizationServer.getJPanelServer());
        serverFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        serverFrame.pack();
        serverFrame.setLocationRelativeTo(null);
        serverFrame.setVisible(true);
    }

    public static void startServer(int port) {
        if (server == null || server.getServerStatus() == ServerStatus.SERVER_STANDBY_FULL) {
            try {
                server = new Server();
                RMISocketFactory.setSocketFactory(new FixedPortRMISocketFactory());
                ServerInt stub = (ServerInt) UnicastRemoteObject.exportObject(server, 20);

                LocateRegistry.createRegistry(port);
                Registry registry = LocateRegistry.getRegistry(port);
                registry.rebind("fs", stub);
                System.out.println("HelloImpl bound in registry");

                Logger logger = new Logger();
                logger.log = fileSynchronizationServer.getJTextAreaLog();
                fileSynchronizationServer.getJLabelServerInfoValue().setText("127.0.0.1:" + port);
                server.setLogger(logger);
                server.setFileProgressBar(fileSynchronizationServer.getJProgressBarFile());
                server.getLogger().log("File Server Started");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        updateClientList();
        updateFileQueue();
    }

    public static void stopServer(int port) {
        if (server.getServerStatus() == ServerStatus.SERVER_WORK || server.getServerStatus() == ServerStatus.SERVER_STANDBY_TRANSFER) {
            try {
                UnicastRemoteObject.unexportObject(registry, true);
                server.getLogger().log("File Server Stopped");
            } catch (Exception e) {
                e.printStackTrace();
            }
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
