package vn.hoidanit.jobhunter.controller;

import org.springframework.web.bind.annotation.RestController;

import com.turkraft.springfilter.boot.Filter;

import vn.hoidanit.jobhunter.domain.User;
import vn.hoidanit.jobhunter.domain.dto.ResultPaginationDTO;
import vn.hoidanit.jobhunter.service.UserService;
import vn.hoidanit.jobhunter.util.exception.IdInvalidException;

import java.util.Optional;
import java.util.regex.Pattern;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.PutMapping;

@RestController
public class UserController {

    private UserService userService;
    private PasswordEncoder passwordEncoder;

    public UserController(UserService userService, PasswordEncoder passwordEncoder) {
        this.userService = userService;
        this.passwordEncoder = passwordEncoder;
    }

    @PostMapping("/users")
    public ResponseEntity<User> createUser(@RequestBody User postManUser) {
        String hashCode = this.passwordEncoder.encode(postManUser.getPassword());
        postManUser.setPassword(hashCode);

        User newUser = this.userService.handleCreateUser(postManUser);
        return ResponseEntity.status(HttpStatus.CREATED).body(newUser);
    }

    @DeleteMapping("/users/{id}")
    public ResponseEntity<String> deleteUser(@PathVariable("id") long id) {
        this.userService.handleDeleteUser(id);
        return ResponseEntity.status(HttpStatus.OK).body("delete user by id:" + id);
    }

    @GetMapping("/users")
    public ResponseEntity<ResultPaginationDTO> getAllUser(
            @RequestParam("page") Optional<String> currentPage,
            @RequestParam("size") Optional<String> pageSize) {
        String sCurrentPage = currentPage.isPresent() ? currentPage.get() : "";
        String sPageSize = pageSize.isPresent() ? pageSize.get() : "";
        Pageable pageable = PageRequest.of(Integer.parseInt(sCurrentPage) - 1, Integer.parseInt(sPageSize));

        return ResponseEntity.status(HttpStatus.OK).body(this.userService.handleGetAllUser(pageable));
    }

    @GetMapping("/search/users")
    public ResponseEntity<ResultPaginationDTO> getAllUserFilter(@Filter Specification<User> spec, Pageable pageable) {
        return ResponseEntity.status(HttpStatus.OK).body(this.userService.handleGetAllUserFilter(spec, pageable));
    }

    @GetMapping("/users/{id}")
    public ResponseEntity<User> getUserById(@PathVariable("id") String id) throws IdInvalidException {
        if (Pattern.compile("^[0-9]+$").matcher(id).matches()) {
            if (this.userService.handleGetUserById(Long.parseLong(id)) != null) {
                return ResponseEntity.status(HttpStatus.OK)
                        .body(this.userService.handleGetUserById(Long.parseLong(id)));
            } else {
                throw new IdInvalidException("Id user don't exist");
            }
        } else {
            throw new IdInvalidException("Id is number");
        }
    }

    @PutMapping("/users")
    public ResponseEntity<User> updateUser(@RequestBody User user) {
        return ResponseEntity.status(HttpStatus.OK).body(this.userService.handleUpdateUser(user));
    }
}
