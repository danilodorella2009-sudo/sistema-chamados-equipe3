package br.com.equipe3.chamados.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

    private static final String TECNICO = "TECNICO";
    private static final String SUPERVISOR = "ADMINISTRADOR_SUPERVISOR";
    private static final String SOLICITANTE = "SOLICITANTE";

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/login", "/css/**", "/error").permitAll()
                // Painel e filas: equipe de suporte
                .requestMatchers("/dashboard/**", "/chamados/fila", "/chamados/todos", "/chamados/alertas").hasAnyRole(TECNICO, SUPERVISOR)
                // Simulação do horário de atendimento (apresentação): só o supervisor
                .requestMatchers("/admin/**").hasRole(SUPERVISOR)
                // Abertura de chamado e "meus chamados": solicitante
                .requestMatchers("/chamados/novo", "/chamados/meus").hasRole(SOLICITANTE)
                .requestMatchers(HttpMethod.POST, "/chamados").hasRole(SOLICITANTE)
                .requestMatchers(HttpMethod.POST, "/chamados/*/validar", "/chamados/*/remoto/autorizar",
                        "/chamados/*/remoto/recusar").hasRole(SOLICITANTE)
                .requestMatchers(HttpMethod.POST, "/chamados/*/assumir", "/chamados/*/reclassificar",
                        "/chamados/*/transferir", "/chamados/*/retransferir", "/chamados/*/recusar-designacao", "/chamados/*/status", "/chamados/*/solucionar", "/chamados/*/remoto/solicitar",
                        "/chamados/*/remoto/diagnostico").hasAnyRole(TECNICO, SUPERVISOR)
                .requestMatchers(HttpMethod.POST, "/chamados/*/encerrar").hasRole(SUPERVISOR)
                // Base de conhecimento: todos leem; equipe de suporte escreve
                .requestMatchers("/kb/novo", "/kb/*/editar").hasAnyRole(TECNICO, SUPERVISOR)
                .requestMatchers(HttpMethod.POST, "/kb/**").hasAnyRole(TECNICO, SUPERVISOR)
                .anyRequest().authenticated())
            .formLogin(form -> form
                .loginPage("/login")
                .usernameParameter("email")
                .passwordParameter("senha")
                .defaultSuccessUrl("/", true)
                .permitAll())
            .logout(logout -> logout
                // motivo=inatividade é enviado pelo timer de inatividade (fragments/layout.html)
                .logoutSuccessHandler((request, response, authentication) -> response.sendRedirect(
                        request.getContextPath() + ("inatividade".equals(request.getParameter("motivo"))
                                ? "/login?expirado" : "/login?logout")))
                .permitAll());
        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
