package com.filesynch.rmi;

import com.filesynch.dto.ClientInfoDTO;
import com.filesynch.dto.ServerSettingsDTO;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.HashMap;
import java.util.List;

public interface ServerRmiInt extends Remote {
    public String getServerStatus() throws RemoteException;

    public void connectGuiToServer(ServerGuiInt serverGuiInt) throws RemoteException;

    public int startServer() throws RemoteException;

    public void stopServer() throws RemoteException;

    public void addNewClient(Long id, String login) throws RemoteException;

    public void sendMessage(String login, String message) throws RemoteException;

    public void sendFile(String login, String file) throws RemoteException;

    public void sendAllFiles(String login) throws RemoteException;

    public HashMap<String, ClientInfoDTO> getLoginSessionHashMap() throws RemoteException;

    public List<ClientInfoDTO> getClientInfoDTOList() throws RemoteException;

    public void setSettings(ServerSettingsDTO settings) throws RemoteException;

    public ServerSettingsDTO getSettings() throws RemoteException;

    public void setClientSettings(ClientInfoDTO clientInfoDTO) throws RemoteException;

    public ClientInfoDTO getClientSettings(String login) throws RemoteException;

    public ClientInfoDTO getClientSettings(Long id) throws RemoteException;

    public boolean getQueueNewStatus() throws RemoteException;

    public boolean getQueueNewStatus(String login) throws RemoteException;

    public boolean getQueueTechnicalStatus() throws RemoteException;

    public boolean getQueueTechnicalStatus(String login) throws RemoteException;

    public boolean getQueueAliveStatus() throws RemoteException;

    public boolean getQueueAliveStatus(String login) throws RemoteException;

    public boolean getQueueFileInfoStatus() throws RemoteException;

    public boolean getQueueFileInfoStatus(String login) throws RemoteException;

    public boolean getQueueFilesStatus() throws RemoteException;

    public boolean getQueueFilesStatus(String login) throws RemoteException;

    public boolean getQueueFilesParts() throws RemoteException;

    public boolean getQueueFilesParts(String login) throws RemoteException;
}