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
import java.io.IOException;
import java.net.InetAddress;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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
        serverFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
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

                Logger.logArea = fileSynchronizationServer.getJTextAreaLog();
                fileSynchronizationServer.getJLabelServerInfoValue().setText(InetAddress.getLocalHost().getHostAddress() + ":" + port);
                server.setFileProgressBar(fileSynchronizationServer.getJProgressBarFile());
                Logger.log("File Server Started");
                environment = ctx.getBean(Environment.class);
                port = environment.getProperty("server.port");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        updateClientList();
        updateFileQueue();
    }

    public static void stopServer() {
        if (server.getServerStatus() == ServerStatus.SERVER_WORK) {
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
