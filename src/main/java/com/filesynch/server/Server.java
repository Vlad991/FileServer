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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.stereotype.Service;
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
    public final String FILE_INPUT_DIRECTORY = "input_files/";
    public final String FILE_OUTPUT_DIRECTORY = "output_files/";
    @Getter
    private HashMap<String, WebSocketSession> loginSessionHashMap = new HashMap<>();
    @Getter
    private HashMap<String, WebSocketSession> clientTextMessageSessionHashMap = new HashMap<>();
    @Getter
    private HashMap<String, WebSocketSession> clientFileInfoSessionHashMap = new HashMap<>();
    @Getter
    private HashMap<String, WebSocketSession> clientFilePartSessionHashMap = new HashMap<>();
    @Getter
    private HashMap<String, WebSocketSession> clientFirstFilePartSessionHashMap = new HashMap<>();
    @Setter
    private JProgressBar fileProgressBar;
    private ObjectMapper mapper = new ObjectMapper();

    public Server(ClientInfoRepository clientInfoRepository, FileInfoReceivedRepository fileInfoReceivedRepository, FileInfoSentRepository fileInfoSentRepository, FilePartReceivedRepository filePartReceivedRepository, FilePartSentRepository filePartSentRepository, TextMessageRepository textMessageRepository) {
        clientInfoConverter = new ClientInfoConverter();
        fileInfoReceivedConverter = new FileInfoReceivedConverter(clientInfoConverter);
        fileInfoSentConverter = new FileInfoSentConverter(clientInfoConverter);
        filePartReceivedConverter = new FilePartReceivedConverter(clientInfoConverter, fileInfoReceivedConverter);
        filePartSentConverter = new FilePartSentConverter(clientInfoConverter, fileInfoSentConverter);
        textMessageConverter = new TextMessageConverter(clientInfoConverter);
        serverStatus = ServerStatus.SERVER_STANDBY_FULL;
        this.clientInfoRepository = clientInfoRepository;
        this.fileInfoReceivedRepository = fileInfoReceivedRepository;
        this.fileInfoSentRepository = fileInfoSentRepository;
        this.filePartReceivedRepository = filePartReceivedRepository;
        this.filePartSentRepository = filePartSentRepository;
        this.textMessageRepository = textMessageRepository;
    }

    public String loginToServer(ClientInfoDTO clientInfoDTO, WebSocketSession clientLoginSession) {
        Logger.log(clientInfoDTO.toString());
        String login = null;
        ClientInfo clientInfoTest = clientInfoRepository.findByLogin(clientInfoDTO.getLogin());
        if (clientInfoDTO.getLogin() != null && clientInfoRepository.findByLogin(clientInfoDTO.getLogin()) != null) {
            login = clientInfoDTO.getLogin();
        } else {
            NewClient newClient = new NewClient();
            JFrame newClientFrame = Main.showNewClientIcon(clientLoginSession, newClient);
            newClient.getJLabelIPValue().setText(clientInfoDTO.getIpAddress());
            newClient.getJLabelPCModelValue().setText(clientInfoDTO.getPcModel());
            newClient.getJLabelPCNameValue().setText(clientInfoDTO.getPcName());
            synchronized (clientLoginSession) {
                try {
                    clientLoginSession.wait();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            login = newClient.getJTextFieldLogin().getText();
            clientInfoDTO.setLogin(login);
            clientInfoRepository.save(clientInfoConverter.convertToEntity(clientInfoDTO));
            Main.hideNewClientIcon(newClientFrame);
        }
        loginSessionHashMap.put(login, clientLoginSession);
        Main.updateClientList();
        File directory = new File(FILE_INPUT_DIRECTORY + login);
        if (!directory.exists()) {
            directory.mkdir();
        }
        return login;
    }

    public String sendTextMessageToServer(String login, String message) {
        if (clientIsLoggedIn(login)) {
            TextMessage textMessage = new TextMessage();
            textMessage.setMessage(message);
            textMessage.setClient(clientInfoRepository.findByLogin(login));
            textMessageRepository.save(textMessage);
            Logger.log(textMessage.getMessage());
            return "Message Received!";
        } else {
            System.out.println(message);
            return "You are not logged in!";
        }
    }

    public boolean sendFileInfoToServer(String login, FileInfoDTO fileInfoDTO) {
        if (clientIsLoggedIn(login)) {
            FileInfoReceived fileInfo = fileInfoReceivedConverter.convertToEntity(fileInfoDTO);
            fileInfo.setClient(clientInfoRepository.findByLogin(login));
            fileInfoReceivedRepository.save(fileInfo);
            Logger.log(fileInfoDTO.toString());
            Main.updateFileQueue();
            return true;
        } else {
            return false;
        }
    }

    public boolean sendFilePartToServer(String login, FilePartDTO filePartDTO) {
        if (clientIsLoggedIn(login)) {
            try {
                File file = new File(FILE_INPUT_DIRECTORY + login + "/" + filePartDTO.getFileInfoDTO().getName());
                if (filePartDTO.getOrder() == 1) {
                    file.createNewFile();
                }
                FileOutputStream out = new FileOutputStream(file, true);
                out.write(filePartDTO.getData(), 0, filePartDTO.getLength());
                out.flush();
                out.close();
                filePartDTO.setStatus(FilePartStatus.SENT);
                FilePartReceived filePart = filePartReceivedConverter.convertToEntity(filePartDTO);
                filePart.setClient(clientInfoRepository.findByLogin(login));
                FileInfoReceived fileInfo = fileInfoReceivedRepository
                        .findByHash(filePart.getFileInfo().getHash());
                if (fileInfo != null) {
                    filePart.setFileInfo(fileInfo);
                }
                filePartReceivedRepository.save(filePart);
                Main.updateFileQueue();
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }
            return true;
        } else {
            return false;
        }
    }

    public FilePartDTO getFirstNotSentFilePartFromServer(String login, FileInfoDTO fileInfoDTO) {
        if (!clientIsLoggedIn(login)) {
            Logger.log("Client is not logged in");
            return null;
        }
        FileInfoReceived fileInfoReceived = fileInfoReceivedRepository
                .findByHash(fileInfoDTO.getHash());
        if (fileInfoReceived == null) {
            FilePartDTO filePartDTO = new FilePartDTO();
            filePartDTO.setOrder(1);
            filePartDTO.setStatus(FilePartStatus.NOT_SENT);
            return filePartDTO;
        }
        List<FilePartReceived> filePartReceivedList = filePartReceivedRepository.findAllByFileInfo(fileInfoReceived);
        if (filePartReceivedList.size() == 0) {
            FilePartDTO filePartDTO = new FilePartDTO();
            filePartDTO.setOrder(1);
            filePartDTO.setStatus(FilePartStatus.NOT_SENT);
            return filePartDTO;
        }
        Collections.sort(filePartReceivedList, new Comparator<FilePartReceived>() {
            public int compare(FilePartReceived o1, FilePartReceived o2) {
                return Integer.compare(o1.getOrder(), o2.getOrder());
            }
        });
        FilePartReceived firstNotSentFilePartReceived = filePartReceivedList.stream()
                .filter(fp -> (fp.getStatus() == FilePartStatus.NOT_SENT))
                .findFirst()
                .get();
        return filePartReceivedConverter.convertToDto(firstNotSentFilePartReceived);
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
        Logger.log("Message sent");
        return true;
    }

    // this is cycle for sending file parts from client to server
    public boolean sendFileToClientFast(String login, String filename) {
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
        try {
            String filePathname = FILE_OUTPUT_DIRECTORY + filename;
            File file = new File(filePathname);
            FileInputStream in = new FileInputStream(file);

            FileInfoDTO fileInfoDTO = new FileInfoDTO();
            fileInfoDTO.setHash(getFileHash(filePathname));
            fileInfoDTO.setName(filename);
            fileInfoDTO.setSize(file.length());
            ClientInfo clientInfo = clientInfoRepository.findByLogin(login);
            ClientInfoDTO clientInfoDTO = clientInfoConverter.convertToDto(clientInfo);
            fileInfoDTO.setClient(clientInfoDTO);
            clientFileInfoSession.sendMessage(new org.springframework.web.socket.TextMessage(mapper.writeValueAsString(fileInfoDTO)));
            FileInfoSent fileInfo = fileInfoSentConverter.convertToEntity(fileInfoDTO);
            fileInfo.setClient(clientInfo);
            fileInfo.setFileStatus(FileStatus.TRANSFER_PROCESS);
            fileInfo = fileInfoSentRepository.save(fileInfo);
            Main.updateFileQueue();

            byte[] fileData = new byte[FILE_PART_SIZE];
            int bytesCount = in.read(fileData);
            int step = 1;
            fileProgressBar.setMinimum(0);
            fileProgressBar.setMaximum((int) fileInfoDTO.getSize());
            int progressValue = 0;
            while (bytesCount > 0) {
                Logger.log(String.valueOf(bytesCount));
                FilePartDTO filePartDTO = new FilePartDTO();
                filePartDTO.setOrder(step);
                step++;
                filePartDTO.setHashKey((long) filePartDTO.hashCode());
                filePartDTO.setFileInfoDTO(fileInfoDTO);
                filePartDTO.setData(fileData);
                filePartDTO.setLength(bytesCount);
                filePartDTO.setStatus(FilePartStatus.NOT_SENT);
                filePartDTO.setClient(clientInfoDTO);
                clientFilePartSession
                        .sendMessage(
                                new org.springframework.web.socket.TextMessage(mapper
                                        .writeValueAsString(filePartDTO)));
                FilePartSent filePartSent = filePartSentConverter.convertToEntity(filePartDTO);
                filePartSent.setClient(clientInfo);
                filePartSent.setFileInfo(fileInfo);
                filePartSent.setStatus(FilePartStatus.SENT);
                filePartSentRepository.save(filePartSent);

                bytesCount = in.read(fileData);
                progressValue += FILE_PART_SIZE;
                fileProgressBar.setValue(progressValue);
                //Thread.sleep(2000);
            }
            Main.updateFileQueue();
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    public boolean sendAllFilesToClient(String login) {
        setServerStatus(ServerStatus.SERVER_WORK);
        WebSocketSession loginSession = loginSessionHashMap.get(login);
        if (loginSession == null) {
            Logger.log("Login not correct");
            return false;
        }
        try (Stream<Path> walk = Files.walk(Paths.get(FILE_OUTPUT_DIRECTORY
                .substring(0, FILE_OUTPUT_DIRECTORY.length() - 1)))) {
            List<String> filePathNames = walk.filter(Files::isRegularFile)
                    .map(x -> x.toString()).collect(Collectors.toList());
            for (String filePath : filePathNames) {
                boolean result = sendFileToClient(login, filePath.replace(FILE_OUTPUT_DIRECTORY, ""));
                if (!result) {
                    return false;
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
        LinkedHashMap<Integer, FilePartDTO> filePartHashMap = new LinkedHashMap<>();
        FileInfoSent fileInfo = null;
        FileInfoDTO fileInfoDTO = null;
        ClientInfo clientInfo = null;
        try {
            FileInputStream in = new FileInputStream(file);
            fileInfo = fileInfoSentRepository.findByHash(fileHash);
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
                Main.updateFileQueue();
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
                filePartDTO.setStatus(FilePartStatus.NOT_SENT);
                filePartDTO.setClient(clientInfoDTO);
                filePartDTO.setHashKey((long) filePartDTO.hashCode());
                filePartHashMap.put(step, filePartDTO);
                step++;
                fileData = new byte[FILE_PART_SIZE];
                bytesCount = in.read(fileData);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        switch (fileInfo.getFileStatus()) {
            case NOT_TRANSFERRED:
                filePartSentRepository.deleteAllByClient_LoginAndFileInfo_Id(login, fileInfo.getId());
                for (Map.Entry<Integer, FilePartDTO> entry : filePartHashMap.entrySet()) {
                    FilePartSent filePart = filePartSentConverter.convertToEntity(entry.getValue());
                    filePart.setClient(clientInfo);
                    filePart.setFileInfo(fileInfo);
                    filePartSentRepository.save(filePart);
                }
                return sendAllFilePartsToClient(filePartHashMap, fileInfoDTO, clientFilePartSession);
            case TRANSFER_PROCESS:
                return sendAllFilePartsToClient(filePartHashMap, fileInfoDTO, clientFilePartSession);
            case TRANSFERRED:
                return true;
        }
        Main.updateFileQueue();
        Logger.log("File with hash: " + fileInfoDTO.getHash() + " sent");
        return true;
    }

    public boolean sendAllFilePartsToClient(LinkedHashMap<Integer, FilePartDTO> filePartHashMap,
                                            FileInfoDTO fileInfoDTO,
                                            WebSocketSession clientFilePartSession) {
        FileInfoSent fileInfo = fileInfoSentRepository.findByHash(fileInfoDTO.getHash());
        fileInfo.setFileStatus(FileStatus.TRANSFER_PROCESS);
        fileInfo = fileInfoSentRepository.save(fileInfo);
        List<FilePartSent> filePartList = filePartSentRepository.findAllByFileInfo(fileInfo);
        Collections.sort(filePartList, new Comparator<FilePartSent>() {
            public int compare(FilePartSent o1, FilePartSent o2) {
                return Integer.compare(o1.getOrder(), o2.getOrder());
            }
        });
        FilePartSent firstNotSentFilePart = filePartList.stream()
                .filter(fp -> (fp.getStatus() == FilePartStatus.NOT_SENT))
                .findFirst()
                .get();
        FilePartDTO firstNotSentFilePartDTOFromClient = null;
        try {
            WebSocketSession clientFirstFilePartSession =
                    clientFirstFilePartSessionHashMap.get(fileInfo.getClient().getLogin());
            synchronized (clientFirstFilePartSession) {
                clientFirstFilePartSession
                        .sendMessage(
                                new org.springframework.web.socket.TextMessage(
                                        mapper.writeValueAsString(fileInfoDTO)));
                clientFilePartSession.wait();
                firstNotSentFilePartDTOFromClient =
                        (FilePartDTO) clientFilePartSession.getAttributes().get("first_file_part");
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        FilePartSent firstNotSentFilePartFromClient = null;
        if (firstNotSentFilePartDTOFromClient.getOrder() == 1) {
            firstNotSentFilePartFromClient = firstNotSentFilePart;
        } else {
            firstNotSentFilePartFromClient =
                    filePartSentConverter.convertToEntity(firstNotSentFilePartDTOFromClient);
        }
        if (firstNotSentFilePart.getOrder() != firstNotSentFilePartFromClient.getOrder()) {
            if (firstNotSentFilePart.getOrder() > firstNotSentFilePartFromClient.getOrder()) {
                for (FilePartSent fp : filePartList) {
                    if (fp.getOrder() == (firstNotSentFilePart.getOrder() - 1)) {
                        fp.setStatus(FilePartStatus.NOT_SENT);
                        break;
                    }
                }
            } else {
                firstNotSentFilePart.setStatus(FilePartStatus.SENT);
            }
            sendAllFilePartsToClient(filePartHashMap, fileInfoDTO, clientFilePartSession);
        } else {
            fileProgressBar.setMinimum(0);
            fileProgressBar.setMaximum((int) fileInfoDTO.getSize());
            int progressValue = 0;
            for (int i = firstNotSentFilePart.getOrder(); i <= filePartHashMap.size(); i++) {
                try {
                    FilePartDTO filePartDTOToSend = filePartHashMap.get(i);
                    FilePartSent filePartToSend = filePartList.get(i - 1);

                    sendFilePartToClient(filePartDTOToSend, clientFilePartSession);

                    filePartToSend.setStatus(FilePartStatus.SENT);
                    filePartSentRepository.save(filePartToSend);
                    progressValue += FILE_PART_SIZE;
                    fileProgressBar.setValue(progressValue);
                } catch (Exception e) {
                    e.printStackTrace();
                    return false; //todo? (break)
                }
            }
        }
        fileInfo.setFileStatus(FileStatus.TRANSFERRED);
        fileInfoSentRepository.save(fileInfo);
        return true;
    }

    public boolean sendFilePartToClient(FilePartDTO filePartDTO, WebSocketSession clientFilePartSession) {
        boolean result = true;
        try {
            clientFilePartSession.
                    sendMessage(
                            new org.springframework.web.socket.TextMessage(
                                    mapper.writeValueAsString(filePartDTO)));
            //Thread.sleep(2000);
        } catch (IOException e) {
            result = false;
            e.printStackTrace();
            return false;
        }
        Logger.log(String.valueOf(result));
        if (result) {
            FilePartSent filePartSent = filePartSentRepository.findByHashKey(filePartDTO.getHashKey());
            filePartSent.setStatus(FilePartStatus.SENT);
            filePartSentRepository.save(filePartSent);
            return true;
        } else {
            return false;
        }
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
            ;
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

    public boolean clientIsLoggedIn(String login) { // todo check in session list
        ClientInfo client = clientInfoRepository.findByLogin(login);
        if (client == null) {
            return false;
        }
        return true;
    }
}
