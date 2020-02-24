package com.filesynch.server;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.filesynch.Main;
import com.filesynch.converter.*;
import com.filesynch.dto.*;
import com.filesynch.entity.*;
import com.filesynch.gui.NewClient;
import com.filesynch.repository.*;
import lombok.Getter;
import lombok.Setter;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.socket.WebSocketSession;

import javax.swing.*;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
public class Server {
    @Getter
    @Setter
    private ServerStatus serverStatus;
    private ClientInfoConverter clientInfoConverter;
    private FileInfoReceivedConverter fileInfoReceivedConverter;
    private FileInfoSentConverter fileInfoSentConverter;
    private FilePartReceivedConverter filePartReceivedConverter;
    private FilePartSentConverter filePartSentConverter;
    private TextMessageConverter textMessageConverter;
    @Getter
    private final ClientInfoRepository clientInfoRepository;
    @Getter
    private final FileInfoReceivedRepository fileInfoReceivedRepository;
    @Getter
    private final FileInfoSentRepository fileInfoSentRepository;
    @Getter
    private final FilePartReceivedRepository filePartReceivedRepository;
    @Getter
    private final FilePartSentRepository filePartSentRepository;
    private final TextMessageRepository textMessageRepository;
    public static final String CLIENT_LOGIN = "client_login";
    private final int FILE_PART_SIZE = 1024; // in bytes (100 kB)
    public static final String slash = File.separator;
    //public final String FILE_INPUT_DIRECTORY = "input_files" + slash;
    public final String FILE_OUTPUT_DIRECTORY = "output_files" + slash;
    @Getter
    private HashMap<String, ClientInfoDTO> loginSessionHashMap = new HashMap<>();
    @Getter
    private HashMap<String, WebSocketSession> clientTextMessageSessionHashMap = new HashMap<>();
    @Getter
    private HashMap<String, WebSocketSession> clientFileInfoSessionHashMap = new HashMap<>();
    @Getter
    private HashMap<String, WebSocketSession> clientFilePartSessionHashMap = new HashMap<>();
    @Getter
    private HashMap<String, WebSocketSession> clientFilePartStatusSessionHashMap = new HashMap<>();
    @Getter
    private HashMap<String, WebSocketSession> clientFileStatusSessionHashMap = new HashMap<>();
    @Getter
    private HashMap<String, WebSocketSession> clientLoadFileSessionHashMap = new HashMap<>();
    @Setter
    private JProgressBar fileProgressBar;
    private ObjectMapper mapper = new ObjectMapper();
    //public HashMap<String, HashMap<String, ArrayList<FilePartDTO>>> filePartHashMap;
    public HashMap<String, ClientInfoDTO> queueNew;
    public List<String> queueTechnical;
    public HashMap<String, ClientInfoDTO> queueAlive;
    public HashMap<String, FileInfoDTO> queueFileInfo;
    public HashMap<String, FileInfoDTO> queueFiles;
    public HashMap<String, FilePartDTO> queueFileParts;

    public Server(ClientInfoRepository clientInfoRepository, FileInfoReceivedRepository fileInfoReceivedRepository, FileInfoSentRepository fileInfoSentRepository, FilePartReceivedRepository filePartReceivedRepository, FilePartSentRepository filePartSentRepository, TextMessageRepository textMessageRepository) {
        clientInfoConverter = new ClientInfoConverter();
        fileInfoReceivedConverter = new FileInfoReceivedConverter(clientInfoConverter);
        fileInfoSentConverter = new FileInfoSentConverter(clientInfoConverter);
        filePartReceivedConverter = new FilePartReceivedConverter(clientInfoConverter, fileInfoReceivedConverter);
        filePartSentConverter = new FilePartSentConverter(clientInfoConverter, fileInfoSentConverter);
        textMessageConverter = new TextMessageConverter(clientInfoConverter);
        serverStatus = ServerStatus.SERVER_STOP;
        this.clientInfoRepository = clientInfoRepository;
        this.fileInfoReceivedRepository = fileInfoReceivedRepository;
        this.fileInfoSentRepository = fileInfoSentRepository;
        this.filePartReceivedRepository = filePartReceivedRepository;
        this.filePartSentRepository = filePartSentRepository;
        this.textMessageRepository = textMessageRepository;
        //filePartHashMap = new HashMap<>();
        queueNew = new HashMap<>();
        queueTechnical = new ArrayList<>();
        queueAlive = new HashMap<>();
        queueFileInfo = new HashMap<>();
        queueFiles = new HashMap<>();
        queueFileParts = new HashMap<>();
    }

    public ClientInfoDTO registerToServer(ClientInfoDTO clientInfoDTO) {
        if (clientIsRegistered(clientInfoDTO.getLogin())) {
            clientInfoDTO.setStatus(ClientStatus.CLIENT_FIRST);
            queueNew.remove(clientInfoDTO.getName());
            Main.updateQueueTable();
            return updateClientInfo(clientInfoDTO);
        }
        queueNew.put(clientInfoDTO.getName(), clientInfoDTO);
        Main.updateQueueTable();
        Logger.log(clientInfoDTO.toString());
        clientInfoDTO.setStatus(ClientStatus.NEW);
        ClientInfo clientInfo = clientInfoRepository.save(clientInfoConverter.convertToEntity(clientInfoDTO));
        String login = null;
        NewClient newClient = new NewClient();
        JFrame newClientFrame = Main.showNewClientIcon(clientInfoDTO, newClient);
        synchronized (clientInfoDTO) {
            try {
                clientInfoDTO.wait();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        login = newClient.getJTextFieldLogin().getText();
        clientInfo.setLogin(login);
        clientInfo.setFilesFolder(newClient.getJTextFieldFilesFolder().getText());
        clientInfo.setSendFrequency(Integer.parseInt(newClient.getJTextFieldSendFrequency().getText()));
        clientInfo.setAliveRequestFrequency(Integer.parseInt(newClient.getJTextFieldAliveRequestFrequency().getText()));
        clientInfo.setStatus(ClientStatus.CLIENT_FIRST);
        clientInfoRepository.save(clientInfo);
        clientInfoDTO = clientInfoConverter.convertToDto(clientInfo);
        Main.hideNewClientIcon(newClientFrame);
        loginSessionHashMap.put(login, clientInfoDTO);
        Main.updateClientList();
        File directory = new File(clientInfoDTO.getFilesFolder());
        if (!directory.exists()) {
            directory.mkdir();
        }
        queueNew.remove(clientInfo.getName());
        Main.updateQueueTable();
        return clientInfoDTO;
    }

    public boolean loginToServer(String login) {
        if (!clientIsRegistered(login)) {
            return false;
        }
        Logger.log("Connected: " + login);
        ClientInfo clientInfo = clientInfoRepository.findByLogin(login);
        clientInfo.setStatus(ClientStatus.CLIENT_WORK);
        clientInfoRepository.save(clientInfo);
        ClientInfoDTO clientInfoDTO = clientInfoConverter.convertToDto(clientInfo);
        loginSessionHashMap.put(login, clientInfoDTO);
        Main.updateClientList();
        File directory = new File(clientInfoDTO.getFilesFolder());
        if (!directory.exists()) {
            directory.mkdir();
        }
        return true;
    }

    public void logoutFromServer(String login) {
        setClientStatus(login, ClientStatus.CLIENT_PAUSE);
        Logger.log("Disconnected: " + login);
        loginSessionHashMap.remove(login);
    }

    public void sendTextMessageToServer(String login, String message) {
        if (clientIsLoggedIn(login)) {
            TextMessage textMessage = new TextMessage();
            textMessage.setMessage(message);
            textMessage.setClient(clientInfoRepository.findByLogin(login));
            textMessageRepository.save(textMessage);
            Logger.log(login + ": " + textMessage.getMessage());
            queueTechnical.add(message);
            Main.updateFileQueue();
        } else {
            System.out.println(message);
        }
    }

    public boolean sendFileInfoToServer(String login, FileInfoDTO fileInfoDTO) {
        if (clientIsLoggedIn(login)) {
            HashMap<String, ArrayList<FilePartDTO>> fileHashMap = new HashMap<>();
            fileHashMap.put(fileInfoDTO.getName(), new ArrayList<>());
            //filePartHashMap.put(login, fileHashMap);
            FileInfoReceived existingFileInfo = fileInfoReceivedRepository
                    .findByHashAndName(fileInfoDTO.getHash(), fileInfoDTO.getName());
            FileInfoReceived fileInfoReceived;
            if (existingFileInfo == null) {
                fileInfoReceived = fileInfoReceivedConverter.convertToEntity(fileInfoDTO);
                fileInfoReceived.setClient(clientInfoRepository.findByLogin(login));
                fileInfoReceivedRepository.save(fileInfoReceived);
            } else {
                FileInfoReceived convertedFileInfo = fileInfoReceivedConverter.convertToEntity(fileInfoDTO);
                convertedFileInfo.setId(existingFileInfo.getId());
                fileInfoReceived = fileInfoReceivedRepository.save(convertedFileInfo);
                filePartReceivedRepository.removeAllByFileInfo(fileInfoReceived);
                File file = new File(fileInfoDTO.getClient().getFilesFolder() + fileInfoReceived.getName());
                file.delete();
            }
            File file =
                    new File(loginSessionHashMap.get(login).getFilesFolder() + fileInfoDTO.getName());
            File fileDir = new File(loginSessionHashMap.get(login).getFilesFolder());
            fileDir.mkdir();
            try {
                file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
            Logger.log(fileInfoDTO.toString());
            queueFileInfo.put(fileInfoDTO.getHash(), fileInfoDTO);
            Main.updateFileQueue();
            return true;
        } else {
            return false;
        }
    }

    public boolean sendFilePartToServer(String login, FilePartDTO filePartDTO) {
        if (clientIsLoggedIn(login)) {
            try {
                String partHash = loadFilePart(login, filePartDTO);
                if (!partHash.equals(filePartDTO.getHashKey())) {
                    filePartDTO.setStatus(FilePartStatus.NOT_SENT);
                    sendFilePartStatusToClient(login, filePartDTO);
                    return false;
                }
                filePartDTO.setStatus(FilePartStatus.SENT);
                FilePartReceived filePart = filePartReceivedConverter.convertToEntity(filePartDTO);
                filePart.setClient(clientInfoRepository.findByLogin(login));
                FileInfoReceived fileInfo = fileInfoReceivedRepository
                        .findByHashAndName(filePart.getFileInfo().getHash(), filePart.getFileInfo().getName());
                if (fileInfo != null) {
                    filePart.setFileInfo(fileInfo);
                }
                filePartReceivedRepository.save(filePart);
                sendFilePartStatusToClient(login, filePartDTO);
                //queueFileParts.put(filePartDTO.getHashKey().toString(), filePartDTO);
                //Main.updateFileQueue();
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }
            return true;
        } else {
            return false;
        }
    }

    public boolean sendFilePartStatusToServer(String login, FilePartDTO filePartDTO) {
        if (clientIsLoggedIn(login)) {
            FilePartSent filePartSent = filePartSentRepository.findByHashKey(filePartDTO.getHashKey());
            filePartSent.setStatus(filePartDTO.getStatus());
            filePartSentRepository.save(filePartSent);
            return true;
        } else {
            return false;
        }
    }

    private String loadFilePart(String login, FilePartDTO filePartDTO) throws IOException {
        File file =
                new File(
                        loginSessionHashMap.get(login).getFilesFolder()
                                + filePartDTO.getFileInfoDTO().getName().split("\\.")[0]
                                + "__" + filePartDTO.getOrder() + "."
                                + filePartDTO.getFileInfoDTO().getName().split("\\.")[1]);
        if (!file.exists()) {
            file.createNewFile();
        }
        FileOutputStream out = new FileOutputStream(file, true);
        out.write(filePartDTO.getData(), 0, filePartDTO.getLength());
        out.flush();
        out.close();
        return getFileHash(loginSessionHashMap.get(login).getFilesFolder()
                + filePartDTO.getFileInfoDTO().getName().split("\\.")[0]
                + "__" + filePartDTO.getOrder() + "."
                + filePartDTO.getFileInfoDTO().getName().split("\\.")[1]);
    }

    public void loadFile(String login, FileInfoDTO fileInfoDTO) throws IOException {
        File file =
                new File(loginSessionHashMap.get(login).getFilesFolder() + fileInfoDTO.getName());
        if (!file.exists()) {
            file.createNewFile();
        }
        FileOutputStream out = new FileOutputStream(file, true);
        long filePartsQuantity = fileInfoDTO.getSize() / FILE_PART_SIZE + 1l;
        for (int i = 1; i <= filePartsQuantity; i++) {
            FileInputStream in = new FileInputStream(
                    loginSessionHashMap.get(login).getFilesFolder()
                    + fileInfoDTO.getName().split("\\.")[0]
                    + "__" + i + "."
                    + fileInfoDTO.getName().split("\\.")[1]);
            byte[] filePartData = new byte[FILE_PART_SIZE];
            int bytesCount = in.read(filePartData);
            out.write(filePartData, 0, bytesCount);
            out.flush();
        }
        out.close();
        String realFileHash = getFileHash(loginSessionHashMap.get(login).getFilesFolder() + fileInfoDTO.getName());
        if (realFileHash.equals(fileInfoDTO.getHash())) {
            fileInfoDTO.setFileStatus(FileStatus.TRANSFERRED);
        } else {
            fileInfoDTO.setFileStatus(FileStatus.NOT_TRANSFERRED);
        }
        sendFileStatusToClient(login, fileInfoDTO);
    }

    private void sendFileStatusToClient(String login, FileInfoDTO fileInfoDTO) {
        WebSocketSession session = clientFileStatusSessionHashMap.get(login);
        try {
            session.sendMessage(new org.springframework.web.socket.TextMessage(
                    mapper.writeValueAsString(fileInfoDTO)));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void sendFilePartStatusToClient(String login, FilePartDTO filePartDTO) {
        WebSocketSession session = clientFilePartStatusSessionHashMap.get(login);
        try {
            session.sendMessage(new org.springframework.web.socket.TextMessage(
                    mapper.writeValueAsString(filePartDTO)));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public boolean sendTextMessageToClient(String login, String message) {
        setServerStatus(ServerStatus.SERVER_WORK);
        WebSocketSession clientTextMessageSession = clientTextMessageSessionHashMap.get(login);
        if (clientTextMessageSession == null) {
            Logger.log("Login not correct");
            return false;
        }
        try {
            clientTextMessageSession.sendMessage(new org.springframework.web.socket.TextMessage(message));
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
        Logger.log("message sent to : " + login);
        queueTechnical.add(message);
        Main.updateFileQueue();
        return true;
    }

    public boolean sendAllFilesToClient(String login) {
        setServerStatus(ServerStatus.SERVER_WORK);
        if (!clientIsLoggedIn(login)) {
            return false;
        }
        try (Stream<Path> walk = Files.walk(Paths.get(FILE_OUTPUT_DIRECTORY
                .substring(0, FILE_OUTPUT_DIRECTORY.length() - 1)))) {
            List<String> filePathNames = walk.filter(Files::isRegularFile)
                    .map(x -> x.toString()).collect(Collectors.toList());
            for (String filePath : filePathNames) {
                while(!sendFileToClient(login, filePath.replace(FILE_OUTPUT_DIRECTORY, ""))) {
                    try {
                        Thread.sleep(5000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    public boolean sendFileToClient(String login, String filename) {
        if (filename.charAt(0) == '.') {
            Logger.log("Can't send hidden file");
            return true;
        }
        setServerStatus(ServerStatus.SERVER_WORK);
        WebSocketSession clientFileInfoSession = clientFileInfoSessionHashMap.get(login);
        WebSocketSession clientFilePartSession = clientFilePartSessionHashMap.get(login);
        if (clientFileInfoSession == null) {
            Logger.log("Login not correct");
            return false;
        }
        File file = new File(FILE_OUTPUT_DIRECTORY + filename);
        String fileHash = getFileHash(FILE_OUTPUT_DIRECTORY + filename);
        if (!file.exists()) {
            Logger.log("File " + filename + " not exists");
            return false;
        }
        FileInfoSent fileInfo = null;
        FileInfoDTO fileInfoDTO = null;
        ClientInfo clientInfo = null;
        try {
            FileInputStream in = new FileInputStream(file);
            fileInfo = fileInfoSentRepository.findByHashAndName(fileHash, filename);
            if (fileInfo == null) {
                fileInfoDTO = new FileInfoDTO();
                fileInfoDTO.setHash(fileHash);
                fileInfoDTO.setName(filename);
                fileInfoDTO.setSize(file.length());
                clientInfo = clientInfoRepository.findByLogin(login);
                ClientInfoDTO clientInfoDTO = clientInfoConverter.convertToDto(clientInfo);
                fileInfoDTO.setClient(clientInfoDTO);
                fileInfo = fileInfoSentConverter.convertToEntity(fileInfoDTO);
                fileInfo.setClient(clientInfo);
                fileInfo.setFileStatus(FileStatus.NOT_TRANSFERRED);
                clientFileInfoSession
                        .sendMessage(
                                new org.springframework.web.socket.TextMessage(mapper
                                        .writeValueAsString(fileInfoDTO)));
                fileInfoSentRepository.save(fileInfo);
                //queueFileInfo.put(fileInfoDTO.getHash(), fileInfoDTO);
                //Main.updateFileQueue();
            }
            fileInfoDTO = fileInfoSentConverter.convertToDto(fileInfo);
            clientInfo = clientInfoRepository.findByLogin(login);
            ClientInfoDTO clientInfoDTO = clientInfoConverter.convertToDto(clientInfo);

            byte[] fileData = new byte[FILE_PART_SIZE];
            int bytesCount = in.read(fileData);
            int step = 1;
            while (bytesCount > 0) {
                FilePartDTO filePartDTO = new FilePartDTO();
                filePartDTO.setOrder(step);
                filePartDTO.setFileInfoDTO(fileInfoDTO);
                filePartDTO.setData(fileData);
                filePartDTO.setLength(bytesCount);
                filePartDTO.setStatus(FilePartStatus.WAIT);
                filePartDTO.setClient(clientInfoDTO);
                filePartDTO.setHashKey(getFilePartHash(bytesCount, fileData));
                FilePartSent filePart = filePartSentConverter.convertToEntity(filePartDTO);
                filePart.setFileInfo(fileInfo);
                filePart.setClient(clientInfo);
                filePartSentRepository.save(filePart);
                sendFilePartToClient(filePartDTO, clientFilePartSession);
                step++;
                fileData = new byte[FILE_PART_SIZE];
                bytesCount = in.read(fileData);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        //queueFiles.put(fileInfoDTO.getHash(), fileInfoDTO);
        //Main.updateFileQueue();
        //Logger.log("File with hash: " + fileInfoDTO.getHash() + " sent");
        List<FilePartSent> filePartsWait = filePartSentRepository
                .findAllByFileInfoAndStatus(fileInfo, FilePartStatus.WAIT);
        List<FilePartSent> filePartsNotSent = filePartSentRepository
                .findAllByFileInfoAndStatus(fileInfo, FilePartStatus.NOT_SENT);
        if (filePartsWait.size() == 0 && filePartsNotSent.size() == 0) {
            return true;
        }
        return false;
    }

    public boolean sendFilePartToClient(FilePartDTO filePartDTO, WebSocketSession clientFilePartSession) {
        try {
            clientFilePartSession.
                    sendMessage(
                            new org.springframework.web.socket.TextMessage(
                                    mapper.writeValueAsString(filePartDTO)));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return true;
    }

    public String getFileHash(String filePathname) {
        String hash = "";
        try {
            MessageDigest digest = MessageDigest.getInstance("MD5");

            File file = new File(filePathname);
            FileInputStream fis = new FileInputStream(file);

            byte[] byteArray = new byte[1024];
            int bytesCount = 0;

            while ((bytesCount = fis.read(byteArray)) != -1) {
                digest.update(byteArray, 0, bytesCount);
            }
            fis.close();
            byte[] bytes = digest.digest();
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < bytes.length; i++) {
                sb.append(Integer.toString((bytes[i] & 0xff) + 0x100, 16).substring(1));
            }
            hash = sb.toString();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return hash;
    }

    private String getFilePartHash(int bytesCount, byte[] byteArray) {
        String hash = "";
        try {
            MessageDigest digest = MessageDigest.getInstance("MD5");
            digest.update(byteArray, 0, bytesCount);
            byte[] bytes = digest.digest();
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < bytes.length; i++) {
                sb.append(Integer.toString((bytes[i] & 0xff) + 0x100, 16).substring(1));
            }
            hash = sb.toString();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return hash;
    }

    public boolean clientIsLoggedIn(String login) {
        ClientInfoDTO client = loginSessionHashMap.get(login);
        if (client == null) {
            Logger.log("Not logged in: " + login);
            return false;
        }
        return true;
    }

    public boolean clientIsRegistered(String login) {
        ClientInfo client = clientInfoRepository.findByLogin(login);
        if (client == null) {
            Logger.log("Not registered: " + login);
            return false;
        }
        return true;
    }

    public ClientInfoDTO setClientStatus(String login, ClientStatus clientStatus) {
        ClientInfo clientInfo = clientInfoRepository.findByLogin(login);
        clientInfo.setStatus(clientStatus);
        ClientInfoDTO clientInfoDTO = loginSessionHashMap.get(login);
        if (clientInfoDTO != null) {
            clientInfoDTO.setStatus(clientStatus);
        }
        clientInfoRepository.save(clientInfo);
        return clientInfoDTO;
    }

    public List<ClientInfo> getClientInfoDTOList() {
        return clientInfoRepository.findAll();
    }

    public ClientInfoDTO updateClientInfo(ClientInfoDTO clientInfoDTO) {
        ClientInfo clientInfo = clientInfoConverter.convertToEntity(clientInfoDTO);
        Long id = clientInfoRepository.findByLogin(clientInfoDTO.getLogin()).getId();
        clientInfo.setId(id);
        clientInfoRepository.save(clientInfo);
        loginSessionHashMap.put(clientInfoDTO.getLogin(), clientInfoDTO);
        return clientInfoDTO;
    }
}
