package com.retomada.sistema_de_atendimento.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

@Entity
@Setter
@Getter
public class Atendente implements UserDetails {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(unique = true, nullable = false)
    private String username;
    @Column(nullable = false)
    private String password;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;

    public enum Role {
        ROLE_ATENDENTE,
        ROLE_CLIENTE // Pode ser útil no futuro
    }

    // --- MÉTODOS DA INTERFACE UserDetails ---

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        // Garante que o papel não seja nulo para evitar erros
        if (this.role == null) {
            return Collections.emptyList();
        }
        // Esta linha é crucial. Ela traduz o nosso "Role" para o formato que o Spring Security entende.
        return List.of(new SimpleGrantedAuthority(this.role.name()));
    }



    @Override
    public boolean isAccountNonExpired() {
        return true; // A conta nunca expira
    }

    @Override
    public boolean isAccountNonLocked() {
        return true; // A conta nunca é bloqueada
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true; // As credenciais nunca expiram
    }

    @Override
    public boolean isEnabled() {
        return true; // A conta está sempre habilitada
    }
}
