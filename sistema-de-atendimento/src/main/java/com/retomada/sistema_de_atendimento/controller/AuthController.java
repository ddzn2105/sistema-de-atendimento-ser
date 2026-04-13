package com.retomada.sistema_de_atendimento.controller;

import com.retomada.sistema_de_atendimento.controller.dto.LoginDTO;
import com.retomada.sistema_de_atendimento.controller.dto.TokenDTO;
import com.retomada.sistema_de_atendimento.model.Atendente;
import com.retomada.sistema_de_atendimento.service.TokenService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")

public class AuthController {

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private TokenService tokenService; // Injeta nosso serviço de token

    @PostMapping("/login")
    public ResponseEntity<TokenDTO> login(@RequestBody LoginDTO loginDTO) {
        var authenticationToken = new UsernamePasswordAuthenticationToken(loginDTO.username(), loginDTO.password());
        var authentication = authenticationManager.authenticate(authenticationToken);

        var atendente = (Atendente) authentication.getPrincipal();
        String tokenJWT = tokenService.gerarToken(atendente);

        return ResponseEntity.ok(new TokenDTO(tokenJWT));
    }
}
