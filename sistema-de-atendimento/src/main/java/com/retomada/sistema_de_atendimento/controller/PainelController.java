package com.retomada.sistema_de_atendimento.controller;

import com.retomada.sistema_de_atendimento.controller.dto.SenhaChamadaDTO;
import com.retomada.sistema_de_atendimento.service.SenhaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController

@RequestMapping("/api/painel")
public class PainelController {

    @Autowired
    private SenhaService senhaService;

    @GetMapping("/ultimas-chamadas")
    public ResponseEntity<List<SenhaChamadaDTO>> pegarUltimasChamadas() {
        return ResponseEntity.ok(senhaService.listarUltimasSenhasChamadas());
    }
}
