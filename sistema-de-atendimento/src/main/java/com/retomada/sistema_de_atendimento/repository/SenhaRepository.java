package com.retomada.sistema_de_atendimento.repository;

import com.retomada.sistema_de_atendimento.model.Senha;
import com.retomada.sistema_de_atendimento.model.enums.StatusSenha;
import com.retomada.sistema_de_atendimento.model.enums.TipoSenha;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface SenhaRepository extends JpaRepository<Senha, Long> {
    Optional<Senha> findFirstByStatusSenhaAndTipoOrderByDataHoraAsc(StatusSenha status, TipoSenha tipo);
    List<Senha> findTop5ByStatusSenhaOrderByDataHoraChamadaDesc(StatusSenha status);
    Optional<Senha> findTopByTipoAndDataHoraBetweenOrderByIdDesc(TipoSenha tipo, LocalDateTime inicioDia, LocalDateTime fimDia);
    Optional<Senha> findTopByTipoOrderByIdDesc(TipoSenha tipo);
    Optional<Senha> findByNumeroAndTipo(int numero, TipoSenha tipo);


}
