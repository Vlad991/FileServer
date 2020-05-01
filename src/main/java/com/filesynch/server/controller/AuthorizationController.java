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

    @PostMapping(value = "/login")
    public ResponseEntity login(@RequestBody String login) {
        boolean result = false;
        try {
            result = server.login(mapper.readValue(login, String.class));
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (!result) {
            return ResponseEntity.badRequest().build();
        }
        return ResponseEntity.accepted().build();
    }

    @GetMapping(value = "/client-info")
    public ClientInfoDTO getClientInfo(@RequestParam String login) {
        return server.getClientInfoDTO(login);
    }

    @PostMapping(value = "/logout")
    public ResponseEntity logout(@RequestBody String login) {
        try {
            server.logout(mapper.readValue(login, String.class));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return ResponseEntity.accepted().build();
    }
}
