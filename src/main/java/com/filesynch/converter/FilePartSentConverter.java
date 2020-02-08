package com.filesynch.converter;

import com.filesynch.dto.FilePartDTO;
import com.filesynch.entity.FilePartSent;

public class FilePartSentConverter {
    private ClientInfoConverter clientInfoConverter;
    private FileInfoSentConverter fileInfoConverter;

    public FilePartSentConverter(ClientInfoConverter clientInfoConverter, FileInfoSentConverter fileInfoConverter) {
        this.clientInfoConverter = clientInfoConverter;
        this.fileInfoConverter = fileInfoConverter;
    }

    public FilePartDTO convertToDto(FilePartSent filePart) {
        FilePartDTO filePartDTO = new FilePartDTO();
        filePartDTO.setHashKey(filePart.getHashKey());
        filePartDTO.setClient(clientInfoConverter.convertToDto(filePart.getClient()));
        filePartDTO.setFileInfoDTO(fileInfoConverter.convertToDto(filePart.getFileInfo()));
        filePartDTO.setOrder(filePart.getOrder());
        filePartDTO.setStatus(filePart.getStatus());
        return filePartDTO;
    }

    public FilePartSent convertToEntity(FilePartDTO filePartDTO) {
        FilePartSent filePart = new FilePartSent();
        filePart.setHashKey(filePartDTO.getHashKey());
        filePart.setClient(clientInfoConverter.convertToEntity(filePartDTO.getClient()));
        filePart.setFileInfo(fileInfoConverter.convertToEntity(filePartDTO.getFileInfoDTO()));
        filePart.setOrder(filePartDTO.getOrder());
        filePart.setStatus(filePartDTO.getStatus());
        return filePart;
    }
}
