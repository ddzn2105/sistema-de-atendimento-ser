package com.retomada.sistema_de_atendimento.controller.dto;

import com.retomada.sistema_de_atendimento.model.enums.TipoSenha;

public record SenhaChamadaDTO(int numero, String guicheAtendimento, TipoSenha tipoSenha) {
}
