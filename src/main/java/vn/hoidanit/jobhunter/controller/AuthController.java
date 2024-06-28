package vn.hoidanit.jobhunter.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import vn.hoidanit.jobhunter.domain.User;
import vn.hoidanit.jobhunter.domain.request.ReqLoginDTO;
import vn.hoidanit.jobhunter.domain.response.ResLoginDTO;
import vn.hoidanit.jobhunter.domain.response.user.ResCreateUserDTO;
import vn.hoidanit.jobhunter.service.UserService;
import vn.hoidanit.jobhunter.util.SecurityUtil;
import vn.hoidanit.jobhunter.util.annotation.ApiMessage;
import vn.hoidanit.jobhunter.util.exception.IdInvalidException;

import org.springframework.web.bind.annotation.RequestMapping;

@RestController
@RequestMapping("/api/v1")
public class AuthController {
        private AuthenticationManagerBuilder authenticationManagerBuilder;
        private PasswordEncoder passwordEncoder;
        private SecurityUtil securityUtil;
        private UserService userService;

        @Value("${hoidanit.jwt.refresh-token-validity-in-seconds}")
        private long refreshTokenExpiration;

        public AuthController(AuthenticationManagerBuilder authenticationManagerBuilder, SecurityUtil securityUtil,
                        UserService userService, PasswordEncoder passwordEncoder) {
                this.authenticationManagerBuilder = authenticationManagerBuilder;
                this.securityUtil = securityUtil;
                this.userService = userService;
                this.passwordEncoder = passwordEncoder;
        }

        @PostMapping("/auth/login")
        public ResponseEntity<ResLoginDTO> login(@Valid @RequestBody ReqLoginDTO loginDTO) {
                UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(
                                loginDTO.getUsername(), loginDTO.getPassword());

                Authentication authentication = authenticationManagerBuilder.getObject()
                                .authenticate(authenticationToken);
                SecurityContextHolder.getContext().setAuthentication(authentication);

                ResLoginDTO resLogin = new ResLoginDTO();
                User currentUserLogin = this.userService.handleGetUserByEmail(loginDTO.getUsername());
                if (currentUserLogin != null) {
                        ResLoginDTO.UserLogin userLogin = new ResLoginDTO.UserLogin(currentUserLogin.getId(),
                                        currentUserLogin.getEmail(),
                                        currentUserLogin.getName(),
                                        currentUserLogin.getRole() != null ? currentUserLogin.getRole() : null);
                        resLogin.setUser(userLogin);
                }
                resLogin.setAccessToken(
                                this.securityUtil.createAccessToken(authentication.getName(), resLogin));

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
        public ResponseEntity<ResLoginDTO.UserGetAccount> getAccount() {
                String email = SecurityUtil.getCurrentUserLogin().isPresent() ? SecurityUtil.getCurrentUserLogin().get()
                                : "";
                User currentUser = this.userService.handleGetUserByEmail(email);

                ResLoginDTO.UserLogin userLogin = new ResLoginDTO.UserLogin();
                ResLoginDTO.UserGetAccount userGetAccount = new ResLoginDTO.UserGetAccount();
                if (currentUser != null) {
                        userLogin.setId(currentUser.getId());
                        userLogin.setEmail(currentUser.getEmail());
                        userLogin.setName(currentUser.getName());
                        userLogin.setRole(currentUser.getRole() != null ? currentUser.getRole() : null);

                        userGetAccount.setUser(userLogin);
                }
                return ResponseEntity.ok().body(userGetAccount);
        }

        @GetMapping("/auth/refresh")
        @ApiMessage("Get refresh token")
        public ResponseEntity<ResLoginDTO> getRefreshToken(
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

                ResLoginDTO resLogin = new ResLoginDTO();
                ResLoginDTO.UserLogin userLogin = new ResLoginDTO.UserLogin();

                userLogin.setId(currentUser.getId());
                userLogin.setEmail(currentUser.getEmail());
                userLogin.setName(currentUser.getName());
                userLogin.setRole(currentUser.getRole() != null ? currentUser.getRole() : null);

                resLogin.setUser(userLogin);
                resLogin.setAccessToken(this.securityUtil.createAccessToken(email, resLogin));

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

        @PostMapping("/auth/register")
        public ResponseEntity<ResCreateUserDTO> register(@Valid @RequestBody User postManUser)
                        throws IdInvalidException {
                if (this.userService.isEmailExist(postManUser.getEmail())) {
                        throw new IdInvalidException("Email exists: " + postManUser.getEmail());
                }

                String hashCode = this.passwordEncoder.encode(postManUser.getPassword());
                postManUser.setPassword(hashCode);

                User newUser = this.userService.handleCreateUser(postManUser);
                return ResponseEntity.status(HttpStatus.CREATED).body(this.userService.convertToResCreateUser(newUser));
        }
}
