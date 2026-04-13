package com.retomada.sistema_de_atendimento.repository;

import com.retomada.sistema_de_atendimento.model.Atendente;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AtendenteRepository extends JpaRepository<Atendente, Long> {
    Optional<Atendente> findByUsername(String username);
}
