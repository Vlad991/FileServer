package com.filesynch.entity;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "server_settings")
public class ServerSettings {
    @Id
    @GeneratedValue(generator="increment")
    @GenericGenerator(name="increment", strategy = "increment")
    private Long id;
    @Column(name = "port")
    private String port;
    @Column(name = "ws_reconnection_iterations")
    private int wsReconnectionIterations;
    @Column(name = "ws_reconnection_interval")
    private int wsReconnectionInterval;
}
