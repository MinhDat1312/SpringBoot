package vn.hoidanit.jobhunter.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import vn.hoidanit.jobhunter.domain.User;
import vn.hoidanit.jobhunter.domain.dto.LoginDTO;
import vn.hoidanit.jobhunter.domain.dto.ResLogin;
import vn.hoidanit.jobhunter.service.UserService;
import vn.hoidanit.jobhunter.util.SecurityUtil;
import org.springframework.web.bind.annotation.RequestMapping;

@RestController
@RequestMapping("/api/v1")
public class AuthController {
    private AuthenticationManagerBuilder authenticationManagerBuilder;
    private SecurityUtil securityUtil;
    private UserService userService;

    @Value("${hoidanit.jwt.refresh-token-validity-in-seconds}")
    private long refreshTokenExpiration;

    public AuthController(AuthenticationManagerBuilder authenticationManagerBuilder, SecurityUtil securityUtil,
            UserService userService) {
        this.authenticationManagerBuilder = authenticationManagerBuilder;
        this.securityUtil = securityUtil;
        this.userService = userService;
    }

    @PostMapping("/login")
    public ResponseEntity<ResLogin> login(@Valid @RequestBody LoginDTO loginDTO) {
        UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(
                loginDTO.getUsername(), loginDTO.getPassword());

        Authentication authentication = authenticationManagerBuilder.getObject().authenticate(authenticationToken);
        SecurityContextHolder.getContext().setAuthentication(authentication);

        ResLogin resLogin = new ResLogin();
        User currentUserLogin = this.userService.handleGetUserByEmail(loginDTO.getUsername());
        if (currentUserLogin != null) {
            ResLogin.UserLogin userLogin = new ResLogin.UserLogin(currentUserLogin.getId(), currentUserLogin.getEmail(),
                    currentUserLogin.getName());
            resLogin.setUserLogin(userLogin);
        }
        resLogin.setAccessToken(this.securityUtil.createAccessToken(authentication));

        String refreshToken = this.securityUtil.createRefreshToken(loginDTO.getUsername(), resLogin);
        this.userService.updateRefreshToken(loginDTO.getUsername(), refreshToken);

        ResponseCookie resCookie = ResponseCookie
                .from("refreshToken", refreshToken)
                .httpOnly(true)
                .secure(true)
                .path("/")
                .maxAge(refreshTokenExpiration)
                .build();

        return ResponseEntity
                .ok()
                .header(HttpHeaders.SET_COOKIE, resCookie.toString())
                .body(resLogin);
    }
}
