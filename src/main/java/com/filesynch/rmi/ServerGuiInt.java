package com.filesynch.rmi;

import com.filesynch.dto.ClientInfoDTO;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface ServerGuiInt extends Remote {
    public void log(String stringToLog) throws RemoteException;

    public ClientInfoDTO showNewClientIcon(ClientInfoDTO clientInfoDTO) throws RemoteException;

    public void hideNewClientIcon(String login) throws RemoteException;

    public void updateClientList() throws RemoteException;

    public void updateFileQueue() throws RemoteException;

    public void updateNewQueue() throws RemoteException;

    public void updateTechnicalQueue() throws RemoteException;

    public void updateAliveQueue() throws RemoteException;

    public void updateFileInfoQueue() throws RemoteException;

    public void updateFilesQueue() throws RemoteException;

    public void updateFilePartsQueue() throws RemoteException;

    public void updateQueueTable() throws RemoteException;
}