package com.retomada.sistema_de_atendimento.security;

import com.retomada.sistema_de_atendimento.model.Atendente;
import com.retomada.sistema_de_atendimento.repository.AtendenteRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class DataInitializer implements CommandLineRunner {

    private final AtendenteRepository atendenteRepository;
    private final PasswordEncoder passwordEncoder;

    // Usando injeção de construtor (boa prática)
    public DataInitializer(AtendenteRepository atendenteRepository, PasswordEncoder passwordEncoder) {
        this.atendenteRepository = atendenteRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) throws Exception {
        // Verifica se o usuário 'admin' já existe para não tentar criá-lo novamente
        if (atendenteRepository.findByUsername("retomada").isEmpty()) {


            Atendente admin = new Atendente();
            admin.setUsername("retomada");
            // IMPORTANTE: A senha é codificada (hash) antes de ser salva!
            admin.setPassword(passwordEncoder.encode("goias2025"));

            admin.setRole(Atendente.Role.ROLE_ATENDENTE);

            atendenteRepository.save(admin);
        } else {
            System.out.println("Usuário 'retomada' já existe no banco de dados.");
        }
    }
}