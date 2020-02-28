package com.filesynch;

import com.filesynch.dto.ClientInfoDTO;
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
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.event.WindowAdapter;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@SpringBootApplication
@PropertySource("classpath:application.yml")
public class Main extends SpringBootServletInitializer {
    public static JFrame serverFrame;
    public static FileSynchronizationServer fileSynchronizationServer;
    public static Server server;
    private static String[] stringArgs;
    private static ConfigurableApplicationContext ctx;
    public static Environment environment;
    public static String port;

    public static void main(String[] args) {
        serverFrame = new JFrame("File Synchronization Server");
        fileSynchronizationServer = new FileSynchronizationServer();
        serverFrame.setContentPane(fileSynchronizationServer.getJPanelServer());
        serverFrame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        serverFrame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent windowEvent) {
                if (JOptionPane.showConfirmDialog(serverFrame,
                        "Are you sure you want to close this window?", "Close Window?",
                        JOptionPane.YES_NO_OPTION,
                        JOptionPane.QUESTION_MESSAGE) == JOptionPane.YES_OPTION) {
                    // todo send textmessages to all clients
                    stopServer();
                    System.exit(0);
                }
            }
        });
        serverFrame.pack();
        serverFrame.setLocationRelativeTo(null);
        serverFrame.setVisible(true);
        stringArgs = args;
    }

    public static void startServer() {
        if (server == null || server.getServerStatus() == ServerStatus.SERVER_STOP) {
            try {
                ctx = SpringApplication.run(Main.class, stringArgs);
                server = ctx.getBean(Server.class);
                //server.filePartHashMap = new HashMap<>();
                server.queueNew = new HashMap<>();
                server.queueTechnical = new ArrayList<>();
                server.queueAlive = new HashMap<>();
                server.queueFileInfo = new HashMap<>();
                server.queueFiles = new HashMap<>();
                server.queueFileParts = new HashMap<>();
                environment = ctx.getBean(Environment.class);
                port = environment.getProperty("server.port");

                Logger.logArea = fileSynchronizationServer.getJTextAreaLog();
                fileSynchronizationServer.getJLabelServerInfoValue().setText(InetAddress.getLocalHost().getHostAddress() + ":" + port);
                server.setFileProgressBar(fileSynchronizationServer.getJProgressBarFile());
                Logger.log("File Server Started");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        updateQueueTable();
    }

    public static void stopServer() {
        if (server != null && server.getServerStatus() == ServerStatus.SERVER_WORK) {
            try {
                String uri = "http://localhost:" + port + "/actuator/shutdown";
                HttpHeaders headers = new HttpHeaders();
                headers.add("Content-Type", "application/json");
                headers.add("Accept", "*/*");
                RestTemplate rest = new RestTemplate();
                HttpEntity<String> requestEntity = new HttpEntity<>(headers);
                ResponseEntity<String> responseEntity = rest.exchange(uri, HttpMethod.POST, requestEntity, String.class);
                Logger.log(responseEntity.getBody());
                SpringApplication.exit(ctx);
                Logger.log("File Server Stopped");
            } catch (Exception e) {
                Logger.log("File Server NOT Stopped");
                e.printStackTrace();
            }
        }
    }

    public static JFrame showNewClientIcon(ClientInfoDTO clientInfoDTO, NewClient newClient) {
        JFrame newClientFrame = new JFrame("New Client");
        newClient.setClientInfoDTO(clientInfoDTO);
        newClientFrame.setContentPane(newClient.getJPanelMain());
        newClientFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        newClientFrame.pack();
        newClientFrame.setLocationRelativeTo(null);
        newClientFrame.setVisible(true);
        newClient.getJLabelNameValue().setText(clientInfoDTO.getName());
        newClient.getJLabelExternalIPValue().setText(clientInfoDTO.getExternalIp());
        newClient.getJLabelLocalIPValue().setText(clientInfoDTO.getLocalIp());
        newClient.getJLabelPCNameValue().setText(clientInfoDTO.getPcName());
        newClient.getJLabelPCModelValue().setText(clientInfoDTO.getPcModel());
        newClient.getJLabelStatusValue().setText(clientInfoDTO.getStatus().getStatus());
        newClient.getJTextFieldFilesFolder().setText(clientInfoDTO.getFilesFolder());
        newClient.getJTextFieldSendFrequency().setText(String.valueOf(clientInfoDTO.getSendFrequency()));
        newClient.getJTextFieldAliveRequestFrequency().setText(String.valueOf(clientInfoDTO.getAliveRequestFrequency()));
        return newClientFrame;
    }

    public static void hideNewClientIcon(JFrame jFrame) {
        jFrame.setVisible(false);
    }

    public static void updateClientList() {
        HashMap<String, ClientInfoDTO> loginSessionHashMap = server.getLoginSessionHashMap();
        DefaultTableModel model = (DefaultTableModel) fileSynchronizationServer.getJTableClients().getModel();
        loginSessionHashMap.forEach((login, clientInfoDTO) -> {
            model.addRow(new Object[]{
                    clientInfoDTO.getLogin(),
                    clientInfoDTO.getName(),
                    clientInfoDTO.getExternalIp(),
                    clientInfoDTO.getLocalIp(),
                    clientInfoDTO.getPcName(),
                    clientInfoDTO.getPcModel(),
                    clientInfoDTO.getStatus(),
                    clientInfoDTO.getFilesFolder(),
                    clientInfoDTO.getSendFrequency(),
                    clientInfoDTO.getAliveRequestFrequency()});
        });
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

        fileSynchronizationServer.getJListQueueFileInfo().setModel(demoReceivedList);
        fileSynchronizationServer.getJListQueueFiles().setModel(demoSentList);
    }

    public static void updateNewQueue() {
        DefaultListModel demoNewList = new DefaultListModel();
        server.queueNew.forEach((name, clientInfoDTO) -> {
            demoNewList.addElement("new: " + name);
        });

        fileSynchronizationServer.getJListQueueNew().setModel(demoNewList);
    }

    public static void updateTechnicalQueue() {
        DefaultListModel demoNewList = new DefaultListModel();
        server.queueTechnical.forEach((message) -> {
            demoNewList.addElement(message);
        });

        fileSynchronizationServer.getJListQueueTechnical().setModel(demoNewList);
    }

    public static void updateAliveQueue() {
        DefaultListModel demoNewList = new DefaultListModel();
        server.queueAlive.forEach((login, clientInfoDTO) -> {
            demoNewList.addElement(login);
        });

        fileSynchronizationServer.getJListQueueAlive().setModel(demoNewList);
    }

    public static void updateFileInfoQueue() {
        DefaultListModel demoNewList = new DefaultListModel();
        server.queueFileInfo.forEach((name, fileInfoDTO) -> {
            demoNewList.addElement(fileInfoDTO);
        });

        fileSynchronizationServer.getJListQueueFileInfo().setModel(demoNewList);
    }

    public static void updateFilesQueue() {
        DefaultListModel demoNewList = new DefaultListModel();
        server.queueFiles.forEach((name, fileInfoDTO) -> {
            demoNewList.addElement(fileInfoDTO.getName() + ": sending/receiving");
        });

        fileSynchronizationServer.getJListQueueFiles().setModel(demoNewList);
    }

    public static void updateFilePartsQueue() {
        DefaultListModel demoNewList = new DefaultListModel();
        server.queueFileParts.forEach((name, filePartDTO) -> {
            demoNewList.addElement(filePartDTO);
        });

        fileSynchronizationServer.getJListQueueFileParts().setModel(demoNewList);
    }

    public static void updateQueueTable() {
        DefaultTableModel model = (DefaultTableModel) fileSynchronizationServer.getJTableQueues().getModel();
        model.setRowCount(0);
        model.addRow(new Object[]{"Queue NEW", server.queueNew.size(), server.queueNew.size(), "", "", "", "", ""});
        model.addRow(new Object[]{"Queue TECHNICAL", server.queueTechnical.size(), server.queueTechnical.size(), "", "", ""
                , "", ""});
        model.addRow(new Object[]{"Queue ALIVE", server.queueAlive.size(), server.queueAlive.size(), "", "", "", "", ""});
        model.addRow(new Object[]{"Queue FILE_INFO", server.queueFileInfo.size(), server.queueFileInfo.size(), "", "", "", "", ""});
        model.addRow(new Object[]{"Queue FILES", server.queueFiles.size(), server.queueFiles.size(), "", "", "", "", ""});
        model.addRow(new Object[]{"Queue FILES_PARTS", server.queueFileParts.size(), server.queueFileParts.size(), "", "", "", "", ""});


        // All updates
        updateNewQueue();
        updateTechnicalQueue();
        updateAliveQueue();
        updateFileInfoQueue();
        updateFilesQueue();
        updateFilePartsQueue();
    }

    public static void sendMessage(String login, String message) {
        server.sendTextMessageToClient(login, message);
    }

    public static void sendFile(String login, String file) {
        while(!server.sendFileToClient(login, file)) {
        }
    }

    public static void sendAllFiles(String login) {
        server.sendAllFilesToClient(login);
    }
}
