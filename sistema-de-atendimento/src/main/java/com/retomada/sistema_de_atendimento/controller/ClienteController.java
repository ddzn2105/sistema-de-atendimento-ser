package com.retomada.sistema_de_atendimento.controller;

import com.retomada.sistema_de_atendimento.controller.dto.SenhaGeradaDTO;
import com.retomada.sistema_de_atendimento.model.enums.TipoSenha;
import com.retomada.sistema_de_atendimento.service.SenhaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/clientes")

public class ClienteController {

    @Autowired
    private SenhaService senhaService;

    @PostMapping("/senhas/normal")
    public ResponseEntity<SenhaGeradaDTO> pegarSenhaNormal(){
        SenhaGeradaDTO novaSenha = senhaService.gerarNovaSenha(TipoSenha.NORMAL);
        return ResponseEntity.ok(novaSenha);
    }

    @PostMapping("/senhas/preferencial")
    public ResponseEntity<SenhaGeradaDTO> pegarSenhaPreferencial(){
        SenhaGeradaDTO novaSenha = senhaService.gerarNovaSenha(TipoSenha.PREFERENCIAL);
        return ResponseEntity.ok(novaSenha);
    }
}
