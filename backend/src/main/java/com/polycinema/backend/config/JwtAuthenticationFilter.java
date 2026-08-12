package com.polycinema.backend.config;

import com.polycinema.backend.entity.NguoiDung;
import com.polycinema.backend.repository.NguoiDungRepository;
import com.polycinema.backend.util.JwtUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final NguoiDungRepository nguoiDungRepository;

    /**
     * Chuẩn hóa role từ JWT sang Spring Security GrantedAuthority.
     * JWT lưu: 'ADMIN', 'STAFF', 'USER'
     * Spring Security cần: 'ROLE_ADMIN', 'ROLE_STAFF', 'ROLE_USER'
     */
    private String normalizeRole(String role) {
        if (role == null || role.isBlank()) return "ROLE_USER";
        String normalized = role.trim().toUpperCase();
        if (normalized.equals("CUSTOMER")) return "ROLE_USER";
        if (normalized.startsWith("ROLE_")) return normalized;
        return "ROLE_" + normalized;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        String authHeader = request.getHeader("Authorization");

        if (authHeader != null && authHeader.startsWith("Bearer ")) {

            String token = authHeader.substring(7);

            if (jwtUtil.isValid(token)) {

                String email = jwtUtil.extractEmail(token);
                String role  = jwtUtil.extractRole(token);

                // ── Email-change session invalidation ─────────────────────────
                // If the user's email was changed after this JWT was issued,
                // the token is stale — skip authentication so the request is
                // treated as unauthenticated. The user must log in again with
                // the new email to get a fresh token.
                if (email != null) {
                    NguoiDung user = nguoiDungRepository.findByEmail(email).orElse(null);
                    if (user != null && user.getEmailChangedAt() != null) {
                        Date issuedAt = jwtUtil.extractIssuedAt(token);
                        if (issuedAt != null) {
                            LocalDateTime tokenIat = issuedAt.toInstant()
                                    .atZone(ZoneId.systemDefault())
                                    .toLocalDateTime();
                            if (tokenIat.isBefore(user.getEmailChangedAt())) {
                                // Token predates the email change — reject
                                filterChain.doFilter(request, response);
                                return;
                            }
                        }
                    }
                }
                // ──────────────────────────────────────────────────────────────

                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(
                                email,
                                null,
                                List.of(new SimpleGrantedAuthority(normalizeRole(role)))
                        );

                SecurityContextHolder.getContext().setAuthentication(authentication);
            }
        }

        filterChain.doFilter(request, response);
    }
}
