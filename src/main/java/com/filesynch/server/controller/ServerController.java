package com.filesynch.server.controller;

import com.filesynch.dto.ClientInfoDTO;
import com.filesynch.server.Server;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@CrossOrigin
@RequestMapping
public class ServerController {
    private Server server;

    public ServerController(Server server) {
        this.server = server;
    }

    @PostMapping(value = "/register")
    public ClientInfoDTO register(@RequestBody ClientInfoDTO clientInfoDTO) {
        return server.registerToServer(clientInfoDTO);
    }

    @PostMapping(value = "/login")
    public ResponseEntity login(@RequestBody String login) {
        boolean result = server.loginToServer(login);
        if (!result) {
            return ResponseEntity.badRequest().build();
        }
        return ResponseEntity.accepted().build();
    }

    @PostMapping(value = "/logout")
    public ResponseEntity logout(@RequestBody String login) {
        server.logoutFromServer(login);
        return ResponseEntity.accepted().build();
    }
}
