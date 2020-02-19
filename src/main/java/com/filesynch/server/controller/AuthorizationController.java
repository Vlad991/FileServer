package com.filesynch.server.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.filesynch.dto.ClientInfoDTO;
import com.filesynch.server.Server;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

@RestController
@CrossOrigin
@RequestMapping
public class AuthorizationController {
    private Server server;
    ObjectMapper mapper = new ObjectMapper();

    public AuthorizationController(Server server) {
        this.server = server;
    }

    @PostMapping(value = "/register")
    public ClientInfoDTO register(@RequestBody ClientInfoDTO clientInfoDTO) {
        return server.registerToServer(clientInfoDTO);
    }

    @PostMapping(value = "/login")
    public ResponseEntity login(@RequestBody String login) {
        boolean result = false;
        try {
            result = server.loginToServer(mapper.readValue(login, String.class));
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (!result) {
            return ResponseEntity.badRequest().build();
        }
        return ResponseEntity.accepted().build();
    }

    @PostMapping(value = "/logout")
    public ResponseEntity logout(@RequestBody String login) {
        try {
            server.logoutFromServer(mapper.readValue(login, String.class));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return ResponseEntity.accepted().build();
    }
}
