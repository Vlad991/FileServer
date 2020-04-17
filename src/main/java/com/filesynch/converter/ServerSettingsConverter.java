package com.filesynch.converter;

import com.filesynch.dto.ServerSettingsDTO;
import com.filesynch.entity.ServerSettings;

public class ServerSettingsConverter {

    public ServerSettingsConverter() {
    }

    public ServerSettingsDTO convertToDto(ServerSettings settings) {
        ServerSettingsDTO settingsDTO = new ServerSettingsDTO();
        settingsDTO.setPort(settings.getPort());
        settingsDTO.setWsReconnectionInterval(settings.getWsReconnectionInterval());
        settingsDTO.setWsReconnectionIterations(settings.getWsReconnectionIterations());
        return settingsDTO;
    }

    public ServerSettings convertToEntity(ServerSettingsDTO settingsDTO) {
        ServerSettings settings = new ServerSettings();
        settings.setPort(settingsDTO.getPort());
        settings.setWsReconnectionInterval(settingsDTO.getWsReconnectionInterval());
        settings.setWsReconnectionIterations(settingsDTO.getWsReconnectionIterations());
        return settings;
    }
}
