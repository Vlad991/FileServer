package com.filesynch.entity;

import com.filesynch.dto.ClientStatus;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;

@Getter
@Setter
@NoArgsConstructor
@ToString
@Entity
@Table(name = "client_list")
public class ClientInfo {
    @Id
    @GeneratedValue(generator="increment")
    @GenericGenerator(name="increment", strategy = "increment")
    private Long id;
    @Column(name = "login", unique = true)
    private String login;
    @Column(name = "name")
    private String name;
    @Column(name = "external_ip")
    private String externalIp;
    @Column(name = "local_ip")
    private String localIp;
    @Column(name = "pc_name")
    private String pcName;
    @Column(name = "pc_model")
    private String pcModel;
    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private ClientStatus status;
    @Column(name = "files_folder")
    private String filesFolder;
    @Column(name = "send_frequency")
    private int sendFrequency; // per Hour
    @Column(name = "work_request_frequency")
    private int aliveRequestFrequency; // per Hour

}
