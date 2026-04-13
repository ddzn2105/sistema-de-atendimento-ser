package com.retomada.sistema_de_atendimento.controller;

import com.retomada.sistema_de_atendimento.controller.dto.SenhaChamadaDTO;
import com.retomada.sistema_de_atendimento.controller.dto.SenhaGeradaDTO;
import com.retomada.sistema_de_atendimento.model.enums.TipoSenha;
import com.retomada.sistema_de_atendimento.service.SenhaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/atendentes")
public class AtendenteController {
    @Autowired
    private SenhaService senhaService;

    @PostMapping("/chamar")
    public ResponseEntity<SenhaChamadaDTO> chamarSenha(@RequestParam String guiche) {
        SenhaChamadaDTO senhaChamada = senhaService.chamarProximaSenha(guiche);
        return ResponseEntity.ok(senhaChamada);
    }

    @PutMapping("/senhas/{id}/cancelar")
    public ResponseEntity<Void> cancelarSenha(@PathVariable Long id){
        senhaService.cancelarSenha(id);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/senhas/rechamar")
    public ResponseEntity<SenhaChamadaDTO> rechamarSenha(@RequestParam int numero, @RequestParam TipoSenha tipo, @RequestParam String guiche) {
        SenhaChamadaDTO senhaRechamada = senhaService.rechamarSenha(numero, guiche, tipo);
        return ResponseEntity.ok(senhaRechamada);
    }

    @PostMapping("/senhas")
    public ResponseEntity<SenhaGeradaDTO> gerarSenha(@RequestParam TipoSenha tipo) {
        SenhaGeradaDTO novaSenha = senhaService.gerarNovaSenha(tipo);
        return ResponseEntity.ok(novaSenha);
    }
}
