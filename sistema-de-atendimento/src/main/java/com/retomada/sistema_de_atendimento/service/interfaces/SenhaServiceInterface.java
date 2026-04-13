package com.retomada.sistema_de_atendimento.service.interfaces;

import com.retomada.sistema_de_atendimento.controller.dto.SenhaChamadaDTO;
import com.retomada.sistema_de_atendimento.controller.dto.SenhaGeradaDTO;
import com.retomada.sistema_de_atendimento.model.enums.TipoSenha;

import java.util.List;

public interface SenhaServiceInterface {

    SenhaGeradaDTO gerarNovaSenha(TipoSenha tipo);

    SenhaChamadaDTO chamarProximaSenha(String guiche);

    void cancelarSenha(Long id);

    List<SenhaChamadaDTO> listarUltimasSenhasChamadas();

    SenhaChamadaDTO rechamarSenha(int numero, String guiche, TipoSenha tipo);
}
