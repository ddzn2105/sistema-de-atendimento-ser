package com.retomada.sistema_de_atendimento.model;

import com.retomada.sistema_de_atendimento.model.enums.StatusSenha;
import com.retomada.sistema_de_atendimento.model.enums.TipoSenha;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Table(name = "senha")
@Entity
public class Senha {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TipoSenha tipo;

    @Column(nullable = false)
    private int numero;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StatusSenha statusSenha;

    @Column(nullable = false)
    private LocalDateTime dataHora;

    private LocalDateTime dataHoraChamada;

    private String guicheAtendimento;



}
