package com.filesynch.rmi;

import com.filesynch.Main;
import com.filesynch.dto.ClientInfoDTO;
import com.filesynch.dto.ServerStatus;
import com.filesynch.server.Logger;
import com.filesynch.server.Server;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.HashMap;
import java.util.List;

@Getter
@Setter
public class ServerRmi extends UnicastRemoteObject implements ServerRmiInt {
    private ServerGuiInt serverGui;
    private Server server;
    private ConfigurableApplicationContext ctx;
    private String[] stringArgs;
    public Environment environment;
    public int port;

    public ServerRmi() throws RemoteException {
        super();
    }

    @Override
    public void connectGuiToServer(ServerGuiInt serverGuiInt) {
        this.serverGui = serverGuiInt;
        Logger.serverGuiInt = serverGuiInt;
        Main.serverGui = serverGui;
    }

    @Override
    public int startServer() {
        if (server == null || server.getServerStatus() == ServerStatus.SERVER_STOP) {
            try {
                ctx = SpringApplication.run(Main.class, stringArgs);
                server = ctx.getBean(com.filesynch.server.Server.class);
                //server.filePartHashMap = new HashMap<>();
//                server.queueNew = new HashMap<>();
//                server.queueTechnical = new ArrayList<>();
//                server.queueAlive = new HashMap<>();
//                server.queueFileInfo = new HashMap<>();
//                server.queueFiles = new HashMap<>();
//                server.queueFileParts = new HashMap<>();
                environment = ctx.getBean(Environment.class);
                port = Integer.parseInt(environment.getProperty("server.port"));
                Logger.log("File Server Started");
                server.setServerStatus(ServerStatus.SERVER_WORK);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
//        updateQueueTable();
        return port;
    }

    @Override
    public void stopServer() {
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
                server.setServerStatus(ServerStatus.SERVER_STOP);
            } catch (Exception e) {
                Logger.log("File Server NOT Stopped");
                e.printStackTrace();
            }
        }
    }

    @Override
    public void sendMessage(String login, String message) {
        try {
            server.sendTextMessageToClient(login, message);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void sendFile(String login, String file) {
        while (!server.sendFileToClient(login, file)) {
        }
    }

    @Override
    public void sendAllFiles(String login) {
        server.sendAllFilesToClient(login);
    }

    @Override
    public HashMap<String, ClientInfoDTO> getLoginSessionHashMap() throws RemoteException {
        return server.getLoginSessionHashMap();
    }

    @Override
    public List<ClientInfoDTO> getClientInfoDTOList() throws RemoteException {
        return server.getClientInfoDTOList();
    }
}
