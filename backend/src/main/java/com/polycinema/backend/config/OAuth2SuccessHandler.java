package com.polycinema.backend.config;

import com.polycinema.backend.entity.NguoiDung;
import com.polycinema.backend.repository.NguoiDungRepository;
import com.polycinema.backend.util.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@ConditionalOnProperty(name = "spring.security.oauth2.client.registration.google.client-id")
@RequiredArgsConstructor
public class OAuth2SuccessHandler implements AuthenticationSuccessHandler {

    private final NguoiDungRepository repo;
    private final JwtUtil jwtUtil;

    @Override
    public void onAuthenticationSuccess(
            HttpServletRequest request,
            HttpServletResponse response,
            Authentication authentication
    ) throws IOException {

        OAuth2User user =
                (OAuth2User) authentication.getPrincipal();

        OAuth2AuthenticationToken oauthToken =
                (OAuth2AuthenticationToken) authentication;

        String provider =
                oauthToken.getAuthorizedClientRegistrationId();

        String email;
        String name;
        String socialId;

        // GOOGLE
        if ("google".equals(provider)) {

            email = user.getAttribute("email");
            name = user.getAttribute("name");
            socialId = user.getAttribute("sub");

        }
        // DISCORD -> lưu vào FacebookId
        else if ("discord".equals(provider)) {

            email = user.getAttribute("email");
            name = user.getAttribute("username");
            socialId = user.getAttribute("id");

        } else {
            throw new RuntimeException(
                    "Provider không hỗ trợ: " + provider
            );
        }

        NguoiDung nd =
                repo.findByEmail(email).orElse(null);

        if (nd == null) {

            nd = new NguoiDung();
            nd.setEmail(email);
            nd.setHoTen(name);
            nd.setVaiTro("customer");
            nd.setTrangThai(true);
            nd.setIsEmailVerified(true);
        }

        // Google -> GoogleId
        if ("google".equals(provider)) {
            nd.setGoogleId(socialId);
        }

        // Discord -> FacebookId
        if ("discord".equals(provider)) {
            nd.setFacebookId(socialId);
        }

        repo.save(nd);

        String token =
                jwtUtil.generateToken(
                        nd.getEmail(),
                        nd.getVaiTro()
                );

        response.sendRedirect(
                "http://localhost:5173/?token=" + token
        );
    }
}