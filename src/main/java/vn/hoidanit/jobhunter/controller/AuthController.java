package vn.hoidanit.jobhunter.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import vn.hoidanit.jobhunter.domain.User;
import vn.hoidanit.jobhunter.domain.dto.LoginDTO;
import vn.hoidanit.jobhunter.domain.dto.ResLogin;
import vn.hoidanit.jobhunter.service.UserService;
import vn.hoidanit.jobhunter.util.SecurityUtil;
import vn.hoidanit.jobhunter.util.annotation.ApiMessage;
import vn.hoidanit.jobhunter.util.exception.IdInvalidException;

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

        @PostMapping("/auth/login")
        public ResponseEntity<ResLogin> login(@Valid @RequestBody LoginDTO loginDTO) {
                UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(
                                loginDTO.getUsername(), loginDTO.getPassword());

                Authentication authentication = authenticationManagerBuilder.getObject()
                                .authenticate(authenticationToken);
                SecurityContextHolder.getContext().setAuthentication(authentication);

                ResLogin resLogin = new ResLogin();
                User currentUserLogin = this.userService.handleGetUserByEmail(loginDTO.getUsername());
                if (currentUserLogin != null) {
                        ResLogin.UserLogin userLogin = new ResLogin.UserLogin(currentUserLogin.getId(),
                                        currentUserLogin.getEmail(),
                                        currentUserLogin.getName());
                        resLogin.setUserLogin(userLogin);
                }
                resLogin.setAccessToken(
                                this.securityUtil.createAccessToken(authentication.getName(), resLogin.getUserLogin()));

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

        @GetMapping("/auth/account")
        @ApiMessage("Get user information")
        public ResponseEntity<ResLogin.UserGetAccount> getAccount() {
                String email = SecurityUtil.getCurrentUserLogin().isPresent() ? SecurityUtil.getCurrentUserLogin().get()
                                : "";
                User currentUser = this.userService.handleGetUserByEmail(email);

                ResLogin.UserLogin userLogin = new ResLogin.UserLogin();
                ResLogin.UserGetAccount userGetAccount=new ResLogin.UserGetAccount();
                if (currentUser != null) {
                        userLogin.setId(currentUser.getId());
                        userLogin.setEmail(currentUser.getEmail());
                        userLogin.setName(currentUser.getName());

                        userGetAccount.setUser(userLogin);
                }
                return ResponseEntity.ok().body(userGetAccount);
        }

        @GetMapping("/auth/refresh")
        @ApiMessage("Get refresh token")
        public ResponseEntity<ResLogin> getRefreshToken(
                        @CookieValue(name = "refreshToken", defaultValue = "missingToken") String refreshToken)
                        throws IdInvalidException {
                if (refreshToken.equals("missingToken")) {
                        throw new IdInvalidException("You don't have refresh token at cookie");
                }

                Jwt decoded = this.securityUtil.checkValidRefreshToken(refreshToken);
                String email = decoded.getSubject();

                User currentUser = this.userService.getUserByRefreshTokenAndEmail(refreshToken, email);
                if (currentUser == null) {
                        throw new IdInvalidException("Refresh token is invalid");
                }

                ResLogin resLogin = new ResLogin();
                ResLogin.UserLogin userLogin = new ResLogin.UserLogin();

                userLogin.setId(currentUser.getId());
                userLogin.setEmail(currentUser.getEmail());
                userLogin.setName(currentUser.getName());

                resLogin.setUserLogin(userLogin);
                resLogin.setAccessToken(this.securityUtil.createAccessToken(email, userLogin));

                String new_refresh_token = this.securityUtil.createRefreshToken(email, resLogin);
                this.userService.updateRefreshToken(email, new_refresh_token);

                ResponseCookie resCookie = ResponseCookie
                                .from("refreshToken", new_refresh_token)
                                .httpOnly(true)
                                .secure(true)
                                .path("/")
                                .maxAge(refreshTokenExpiration)
                                .build();

                return ResponseEntity.ok().header(HttpHeaders.SET_COOKIE,
                                resCookie.toString()).body(resLogin);
        }

        @PostMapping("/auth/logout")
        @ApiMessage("Logout user")
        public ResponseEntity<Void> logout() throws IdInvalidException {
                String email = SecurityUtil.getCurrentUserLogin().isPresent()
                                ? SecurityUtil.getCurrentUserLogin().get()
                                : "";

                if (email.equals("")) {
                        throw new IdInvalidException("Access token is invalid");
                }

                this.userService.updateRefreshToken(email, null);
                ResponseCookie resCookie = ResponseCookie
                                .from("refreshToken", null)
                                .httpOnly(true)
                                .secure(true)
                                .path("/")
                                .maxAge(0)
                                .build();

                return ResponseEntity.ok().header(HttpHeaders.SET_COOKIE, resCookie.toString()).body(null);
        }
}
