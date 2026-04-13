package com.retomada.sistema_de_atendimento.controller.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.retomada.sistema_de_atendimento.model.enums.TipoSenha;

import java.time.LocalDateTime;

public record SenhaGeradaDTO(int numero,
                             @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
                             LocalDateTime dataHora, TipoSenha tipoSenha) {


}
