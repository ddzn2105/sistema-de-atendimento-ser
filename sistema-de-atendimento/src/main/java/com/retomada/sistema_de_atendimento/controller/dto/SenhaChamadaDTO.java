package com.retomada.sistema_de_atendimento.controller.dto;

import com.retomada.sistema_de_atendimento.model.enums.TipoSenha;
import java.time.LocalDateTime;

public record SenhaChamadaDTO(int numero, String guicheAtendimento, TipoSenha tipoSenha, LocalDateTime dataHoraChamada) {
}
