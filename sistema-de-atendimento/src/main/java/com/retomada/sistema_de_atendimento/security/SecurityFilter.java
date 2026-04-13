package com.retomada.sistema_de_atendimento.security;

import com.retomada.sistema_de_atendimento.repository.AtendenteRepository;
import com.retomada.sistema_de_atendimento.service.TokenService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class SecurityFilter extends OncePerRequestFilter {

    @Autowired
    private TokenService tokenService;

    @Autowired
    private AtendenteRepository atendenteRepository;

    private static final Logger log = LoggerFactory.getLogger(SecurityFilter.class);

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        try {
            String tokenJWT = recuperarToken(request);

            if (tokenJWT != null) {
                String subject = tokenService.getSubject(tokenJWT);

                atendenteRepository.findByUsername(subject).ifPresent(atendente -> {
                    var authentication = new UsernamePasswordAuthenticationToken(
                            atendente,
                            null,
                            atendente.getAuthorities()
                    );
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                });
            }
        } catch (Exception e) {
            // 🔴 Aqui você pode logar a causa exata
            System.out.println("Erro ao autenticar token: " + e.getMessage());

        }

        filterChain.doFilter(request, response);
    }

    private String recuperarToken(HttpServletRequest request) {
        String authorizationHeader = request.getHeader("Authorization");
        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
            return authorizationHeader.substring(7); // remove "Bearer "
        }
        return null;
    }
}
