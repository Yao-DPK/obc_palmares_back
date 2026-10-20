package com.obc.palmares.web.controller;

import java.util.Map;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.obc.palmares.config.JwtService;
import com.obc.palmares.web.dto.LoginRequest;
import com.obc.palmares.web.service.CompteService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final CompteService comptes;
    private final JwtService jwt;

    // constructeur
    public AuthController(CompteService comptes, JwtService jwt){
        this.comptes = comptes;
        this.jwt = jwt;
    }

    @PostMapping("/login")
    public Map<String, Object> login(@Valid @RequestBody LoginRequest req) {
        var compte = comptes.authentifier(req.username(), req.password());
        String token = jwt.generer(compte.getId(), compte.getUsername(), compte.getPersonneId());
        return Map.of("token", token, "personneId", compte.getPersonneId());
    }
}