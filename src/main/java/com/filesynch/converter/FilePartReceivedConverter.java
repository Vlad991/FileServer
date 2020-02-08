package com.filesynch.converter;

import com.filesynch.dto.FilePartDTO;
import com.filesynch.entity.FilePartReceived;

public class FilePartReceivedConverter {
    private ClientInfoConverter clientInfoConverter;
    private FileInfoReceivedConverter fileInfoConverter;

    public FilePartReceivedConverter(ClientInfoConverter clientInfoConverter, FileInfoReceivedConverter fileInfoConverter) {
        this.clientInfoConverter = clientInfoConverter;
        this.fileInfoConverter = fileInfoConverter;
    }

    public FilePartDTO convertToDto(FilePartReceived filePart) {
        FilePartDTO filePartDTO = new FilePartDTO();
        filePartDTO.setHashKey(filePart.getHashKey());
        filePartDTO.setClient(clientInfoConverter.convertToDto(filePart.getClient()));
        filePartDTO.setFileInfoDTO(fileInfoConverter.convertToDto(filePart.getFileInfo()));
        filePartDTO.setOrder(filePart.getOrder());
        filePartDTO.setStatus(filePart.getStatus());
        return filePartDTO;
    }

    public FilePartReceived convertToEntity(FilePartDTO filePartDTO) {
        FilePartReceived filePart = new FilePartReceived();
        filePart.setHashKey(filePartDTO.getHashKey());
        filePart.setClient(clientInfoConverter.convertToEntity(filePartDTO.getClient()));
        filePart.setFileInfo(fileInfoConverter.convertToEntity(filePartDTO.getFileInfoDTO()));
        filePart.setOrder(filePartDTO.getOrder());
        filePart.setStatus(filePartDTO.getStatus());
        return filePart;
    }
}
