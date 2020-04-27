package com.filesynch.rmi;

import com.filesynch.Main;
import com.filesynch.dto.ClientInfoDTO;
import com.filesynch.dto.ClientStatus;
import com.filesynch.dto.ServerSettingsDTO;
import com.filesynch.dto.ServerStatus;
import com.filesynch.entity.ClientInfo;
import com.filesynch.entity.ServerSettings;
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
import java.util.Optional;

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
    public String getServerStatus() throws RemoteException {
        if (server != null && server.getServerStatus() == ServerStatus.SERVER_WORK) {
            return String.valueOf(port);
        } else {
            return null;
        }
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
                Main.server = server;
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
    public void addNewClient(Long id, String login) throws RemoteException {
        server.addNewClient(id, login);
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
    public HashMap<String, ClientInfoDTO> getLoginSessionHashMap() {
        return server.getLoginSessionHashMap();
    }

    @Override
    public List<ClientInfoDTO> getClientInfoDTOList() {
        return server.getClientInfoDTOList();
    }

    @Override
    public void setSettings(ServerSettingsDTO settingsDTO) {
        Optional<ServerSettings> settingsOpt = server.getServerSettingsRepository().findById(1L);
        ServerSettings settings;
        if (settingsOpt.isEmpty()) {
            settings = server.getServerSettingsConverter().convertToEntity(settingsDTO);
        } else {
            settings = server.getServerSettingsConverter().convertToEntity(settingsDTO);
            settings.setId(settingsOpt.get().getId());
        }
        server.setServerSettings(server.getServerSettingsRepository().save(settings));
    }

    @Override
    public ServerSettingsDTO getSettings() {
        Optional<ServerSettings> settingsOpt;
        if (server != null) {
            settingsOpt = server.getServerSettingsRepository().findById(1L);
            return settingsOpt.isPresent() ? server.getServerSettingsConverter().convertToDto(settingsOpt.get()) : new ServerSettingsDTO();
        } else {
            return new ServerSettingsDTO();
        }
    }

    @Override
    public void setClientSettings(ClientInfoDTO clientInfoDTO) {
        ClientInfo clientInfo = server.getClientInfoRepository().findByLogin(clientInfoDTO.getLogin());
        clientInfo.setFilePartSize(clientInfoDTO.getFilePartSize());
        clientInfo.setOutputFilesFolder(clientInfoDTO.getOutputFilesFolder());
        clientInfo.setInputFilesFolder(clientInfoDTO.getInputFilesFolder());
        clientInfo.setHandlersCount(clientInfoDTO.getHandlersCount());
        clientInfo.setHandlerTimeout(clientInfoDTO.getHandlerTimeout());
        clientInfo.setThreadsCount(clientInfoDTO.getThreadsCount());
        clientInfo.setAliveRequestFrequency(clientInfoDTO.getAliveRequestFrequency());
        clientInfo.setSendFrequency(clientInfoDTO.getSendFrequency());
        server.getClientInfoRepository().save(clientInfo);
        server.getLoginSessionHashMap().put(clientInfo.getLogin(),
                server.getClientInfoConverter().convertToDto(clientInfo));
    }

    @Override
    public ClientInfoDTO getClientSettings(String login) {
        return server.getClientInfoConverter().convertToDto(server.getClientInfoRepository().findByLogin(login));
    }

    @Override
    public boolean getQueueNewStatus() throws RemoteException {
        return true;
    }

    @Override
    public boolean getQueueNewStatus(String login) {
        return server.getRegistrationSessionHashMap().get(login) != null
                && server.getRegistrationSessionHashMap().get(login).isOpen();
    }

    @Override
    public boolean getQueueTechnicalStatus() throws RemoteException {
        return true;
    }

    @Override
    public boolean getQueueTechnicalStatus(String login) throws RemoteException {
        return server.getClientTextMessageSessionHashMap().get(login) != null
                && server.getClientTextMessageSessionHashMap().get(login).isOpen();
    }

    @Override
    public boolean getQueueAliveStatus() throws RemoteException {
        return true;
    }

    @Override
    public boolean getQueueAliveStatus(String login) throws RemoteException {
        return true;
    }

    @Override
    public boolean getQueueFileInfoStatus() throws RemoteException {
        return true;
    }

    @Override
    public boolean getQueueFileInfoStatus(String login) throws RemoteException {
        return server.getClientFileInfoSessionHashMap().get(login) != null
                && server.getClientFileInfoSessionHashMap().get(login).isOpen();
    }

    @Override
    public boolean getQueueFilesStatus() throws RemoteException {
        return true;
    }

    @Override
    public boolean getQueueFilesStatus(String login) throws RemoteException {
        return server.getClientFileStatusSessionHashMap().get(login) != null
                && server.getClientFileStatusSessionHashMap().get(login).isOpen();
    }

    @Override
    public boolean getQueueFilesParts() throws RemoteException {
        return false;
    }

    @Override
    public boolean getQueueFilesParts(String login) throws RemoteException {
        return server.getClientFilePartSessionHashMap().get(login) != null
                && server.getClientFilePartSessionHashMap().get(login).isOpen();
    }
}
