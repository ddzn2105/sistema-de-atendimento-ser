package com.retomada.sistema_de_atendimento.service;

import com.retomada.sistema_de_atendimento.controller.dto.SenhaChamadaDTO;
import com.retomada.sistema_de_atendimento.controller.dto.SenhaGeradaDTO;
import com.retomada.sistema_de_atendimento.exceptions.SenhaNaoEncontradaException;
import com.retomada.sistema_de_atendimento.model.Senha;
import com.retomada.sistema_de_atendimento.model.enums.StatusSenha;
import com.retomada.sistema_de_atendimento.model.enums.TipoSenha;
import com.retomada.sistema_de_atendimento.repository.SenhaRepository;
import com.retomada.sistema_de_atendimento.service.interfaces.SenhaServiceInterface;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

@Service
public class SenhaService implements SenhaServiceInterface {
    @Autowired
    private SenhaRepository senhaRepository;

    private final AtomicInteger contadorDeChamadas = new AtomicInteger(0);

    @Override
    @Transactional
    public SenhaGeradaDTO gerarNovaSenha(TipoSenha tipo) {
        // 1. Descobre o inicio e fim do dia atual
        LocalDateTime inicioDoDia = LocalDateTime.now().with(LocalTime.MIN); // Ex: 2023-10-25T00:00:00
        LocalDateTime fimDoDia = LocalDateTime.now().with(LocalTime.MAX);    // Ex: 2023-10-25T23:59:59.999

        // 2. Busca a última senha gerada HOJE e incrementa, ou começa em 1
        int proximoNumero = senhaRepository.findTopByTipoAndDataHoraBetweenOrderByIdDesc(tipo, inicioDoDia, fimDoDia)
                .map(ultimaSenha -> ultimaSenha.getNumero() + 1)
                .orElse(1);

        Senha novaSenha = new Senha();
        novaSenha.setNumero(proximoNumero);
        novaSenha.setStatusSenha(StatusSenha.AGUARDANDO);
        novaSenha.setDataHora(LocalDateTime.now());
        novaSenha.setTipo(tipo);

        Senha senhaSalva = senhaRepository.save(novaSenha);

        return new SenhaGeradaDTO(senhaSalva.getNumero(), senhaSalva.getDataHora(), senhaSalva.getTipo());
    }

    @Override
    @Transactional
    public SenhaChamadaDTO chamarProximaSenha(String guiche) {
        LocalDateTime inicioDoDia = LocalDateTime.now().with(LocalTime.MIN);
        LocalDateTime fimDoDia = LocalDateTime.now().with(LocalTime.MAX);
        Optional<Senha> proximaSenha;

        Optional<Senha> proximaPreferencial = senhaRepository.findFirstByStatusSenhaAndTipoAndDataHoraBetweenOrderByDataHoraAsc(StatusSenha.AGUARDANDO, TipoSenha.PREFERENCIAL, inicioDoDia, fimDoDia);

        if (proximaPreferencial.isPresent() && contadorDeChamadas.get() >= 2) {
            proximaSenha = proximaPreferencial;
            contadorDeChamadas.set(0); 
        } else {
            proximaSenha = senhaRepository.findFirstByStatusSenhaAndTipoAndDataHoraBetweenOrderByDataHoraAsc(StatusSenha.AGUARDANDO, TipoSenha.NORMAL, inicioDoDia, fimDoDia);
            
            if (proximaSenha.isEmpty() && proximaPreferencial.isPresent()) {
                proximaSenha = proximaPreferencial;
                contadorDeChamadas.set(0); 
            } else if (proximaSenha.isPresent()) {
                contadorDeChamadas.incrementAndGet();
            }
        }

        Senha senhaASerChamada = proximaSenha.orElseThrow(() -> new RuntimeException("Nenhuma senha aguardando para ser chamada."));

        senhaASerChamada.setDataHoraChamada(LocalDateTime.now());
        senhaASerChamada.setStatusSenha(StatusSenha.CHAMADA);
        senhaASerChamada.setGuicheAtendimento(guiche);

        senhaRepository.save(senhaASerChamada);

        // Agora retorna também a dataHoraChamada
        return new SenhaChamadaDTO(senhaASerChamada.getNumero(), senhaASerChamada.getGuicheAtendimento(), senhaASerChamada.getTipo(), senhaASerChamada.getDataHoraChamada());
    }

    @Override
    @Transactional
    public void cancelarSenha(Long id) {
        Senha senha = senhaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Senha com ID" + id + " não encontrada!"));

        if( senha .getStatusSenha() == StatusSenha.AGUARDANDO) {
            senha.setStatusSenha(StatusSenha.CANCELADA);
            senhaRepository.save(senha);
        } else{
            throw new IllegalStateException("Apenas senhas com status AGUARDANDO podem ser canceladas!");
        }
    }

    public List<SenhaChamadaDTO> listarUltimasSenhasChamadas() {
    // Define o início (00:00:00) e o fim (23:59:59) do dia de hoje
    LocalDateTime inicioDoDia = LocalDateTime.now().with(LocalTime.MIN);
    LocalDateTime fimDoDia = LocalDateTime.now().with(LocalTime.MAX);

    // Busca apenas as senhas que foram chamadas DENTRO do dia de hoje
    List<Senha> ultimasSenhas = senhaRepository.findTop5ByStatusSenhaAndDataHoraChamadaBetweenOrderByDataHoraChamadaDesc(
        StatusSenha.CHAMADA, 
        inicioDoDia, 
        fimDoDia
    );

    return ultimasSenhas.stream()
            .map(senha -> new SenhaChamadaDTO(
                senha.getNumero(), 
                senha.getGuicheAtendimento(), 
                senha.getTipo(),
                senha.getDataHoraChamada() // Certifique-se de que o DTO suporta este campo
            ))
            .collect(Collectors.toList());
}

    @Override
    @Transactional
    public SenhaChamadaDTO rechamarSenha(int numero, String guiche, TipoSenha tipo) {
        LocalDateTime inicioDoDia = LocalDateTime.now().with(LocalTime.MIN);
        LocalDateTime fimDoDia = LocalDateTime.now().with(LocalTime.MAX);

        Senha senha = senhaRepository.findByNumeroAndTipoAndDataHoraBetween(numero, tipo, inicioDoDia, fimDoDia)
                .orElseThrow(() -> new SenhaNaoEncontradaException("Senha " + tipo + " #" + numero + " não encontrada hoje."));

        senha.setStatusSenha(StatusSenha.CHAMADA);
        senha.setDataHoraChamada(LocalDateTime.now()); // Hora atualizada = TV vai tocar de novo!
        senha.setGuicheAtendimento(guiche);
        senhaRepository.save(senha);

        return new SenhaChamadaDTO(senha.getNumero(), senha.getGuicheAtendimento(), senha.getTipo(), senha.getDataHoraChamada());
    }
}
