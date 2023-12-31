package uk.jinhy.sumsumzip.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import uk.jinhy.sumsumzip.util.JwtProvider;
import uk.jinhy.sumsumzip.util.JwtType;

import java.io.IOException;

@Slf4j
@Component
@RequiredArgsConstructor
public class OAuthSuccessHandler implements AuthenticationSuccessHandler {
    private final JwtProvider jwtProvider;
    private final HttpSession httpSession;

    @Value("${spring.security.cors.client-origin}")
    private String clientOrigin;

    @Override
    public void onAuthenticationSuccess(
            HttpServletRequest request,
            HttpServletResponse response,
            Authentication authentication
    ) throws IOException {
        var oAuth2User = (OAuth2User)authentication.getPrincipal();
        var attributes = oAuth2User.getAttributes();
        var token = jwtProvider.createToken(
                JwtType.REFRESH_TOKEN,
                (String) attributes.get("email")
        );
        httpSession.setAttribute("refresh-token", token);
        response.sendRedirect(clientOrigin);
    }
}
