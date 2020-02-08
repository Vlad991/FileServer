package com.filesynch.entity;

import com.filesynch.dto.ClientStatus;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "client_list")
public class ClientInfo {
    @Id
    @GeneratedValue(generator="increment")
    @GenericGenerator(name="increment", strategy = "increment")
    private Long id;
    @Column(name = "name", unique = true)
    private String login; // name to be logged in to server (login)
    @Column(name = "ip_address")
    private String ipAddress;
    @Column(name = "pc_name")
    private String pcName;
    @Column(name = "pc_model")
    private String pcModel;
    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private ClientStatus status;
}
