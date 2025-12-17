package com.jonasdurau.ceramicmanagement.config;

import jakarta.servlet.FilterChain;
    import jakarta.servlet.ServletException;
    import jakarta.servlet.http.HttpServletRequest;
    import jakarta.servlet.http.HttpServletResponse;
    import org.slf4j.Logger;
    import org.slf4j.LoggerFactory;
    import org.springframework.beans.factory.annotation.Value;
    import org.springframework.stereotype.Component;
    import org.springframework.util.AntPathMatcher;
    import org.springframework.web.filter.OncePerRequestFilter;

    import java.io.IOException;

    @Component
    public class InternalJobAuthFilter extends OncePerRequestFilter {

        private static final Logger logger = LoggerFactory.getLogger(InternalJobAuthFilter.class);
        private static final String TOKEN_HEADER_NAME = "X-Internal-Job-Token";
        private final AntPathMatcher pathMatcher = new AntPathMatcher();
        private final String expectedToken;

        public InternalJobAuthFilter(@Value("${internal.job.auth.token}") String expectedToken) {
            this.expectedToken = expectedToken;
        }

        @Override
        protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
            if (pathMatcher.match("/api/internal/tasks/**", request.getServletPath())) {
                String receivedToken = request.getHeader(TOKEN_HEADER_NAME);
                if (expectedToken != null && expectedToken.equals(receivedToken)) {
                    logger.debug("Token interno do job validado com sucesso para o caminho: {}", request.getServletPath());
                    filterChain.doFilter(request, response);
                } else {
                    logger.warn("Tentativa de acesso não autorizado ao endpoint interno do job {}. Token recebido: '{}'", request.getServletPath(), receivedToken);
                    response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                    response.getWriter().write("Acesso Proibido: Token interno inválido ou ausente.");
                    return;
                }
            } else {
                filterChain.doFilter(request, response);
            }
        }
    }
