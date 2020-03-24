package com.filesynch.rmi;

import com.filesynch.dto.ClientInfoDTO;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.HashMap;
import java.util.List;

public interface ServerRmiInt extends Remote {
    public String getServerStatus() throws RemoteException;

    public void connectGuiToServer(ServerGuiInt serverGuiInt) throws RemoteException;

    public int startServer() throws RemoteException;

    public void stopServer() throws RemoteException;

    public void sendMessage(String login, String message) throws RemoteException;

    public void sendFile(String login, String file) throws RemoteException;

    public void sendAllFiles(String login) throws RemoteException;

    public HashMap<String, ClientInfoDTO> getLoginSessionHashMap() throws RemoteException;

    public List<ClientInfoDTO> getClientInfoDTOList() throws RemoteException;
}