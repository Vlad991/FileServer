package com.filesynch.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;

@Getter
@Setter
@NoArgsConstructor
public class ServerSettingsDTO implements Serializable {
    static final long serialVersionUID = 60L;
    private String port;
    private int wsReconnectionIterations;
    private int wsReconnectionInterval;
}
