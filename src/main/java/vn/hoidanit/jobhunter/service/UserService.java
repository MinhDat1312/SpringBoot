package vn.hoidanit.jobhunter.service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import vn.hoidanit.jobhunter.domain.User;
import vn.hoidanit.jobhunter.domain.dto.Meta;
import vn.hoidanit.jobhunter.domain.dto.ResCreateUserDTO;
import vn.hoidanit.jobhunter.domain.dto.ResUpdateUserDTO;
import vn.hoidanit.jobhunter.domain.dto.ResUserDTO;
import vn.hoidanit.jobhunter.domain.dto.ResultPaginationDTO;
import vn.hoidanit.jobhunter.repository.UserRepository;

@Service
public class UserService {
    private UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public User handleCreateUser(User user) {
        return this.userRepository.save(user);
    }

    public void handleDeleteUser(long id) {
        this.userRepository.deleteById(id);
    }

    public ResultPaginationDTO handleGetAllUsers(Specification<User> spec, Pageable pageable) {
        Page<User> page = this.userRepository.findAll(spec, pageable);
        ResultPaginationDTO resultPaginationDTO = new ResultPaginationDTO();
        Meta meta = new Meta();

        meta.setCurrentPage(pageable.getPageNumber() + 1);
        meta.setPageSize(pageable.getPageSize());
        meta.setPages(page.getTotalPages());
        meta.setTotal(page.getTotalElements());

        resultPaginationDTO.setMeta(meta);

        List<ResUserDTO> list = page.getContent().stream().map(p -> new ResUserDTO(p.getId(), p.getEmail(), p.getName(),
                p.getGender(), p.getAddress(), p.getAge(), p.getUpdatedAt(), p.getCreatedAt()))
                .collect(Collectors.toList());
        resultPaginationDTO.setResult(list);

        return resultPaginationDTO;
    }

    public User handleGetUserById(long id) {
        Optional<User> optional = this.userRepository.findById(id);

        if (optional.isPresent()) {
            return optional.get();
        }
        return null;
    }

    public User handleUpdateUser(User updateUser) {
        User currentUser = this.handleGetUserById(updateUser.getId());

        if (currentUser != null) {
            currentUser.setName(updateUser.getName());
            currentUser.setGender(updateUser.getGender());
            currentUser.setAge(updateUser.getAge());
            currentUser.setAddress(updateUser.getAddress());
            currentUser.setEmail(updateUser.getEmail());

            return this.userRepository.save(currentUser);
        }

        return null;
    }

    public User handleGetUserByEmail(String email) {
        return this.userRepository.findByEmail(email);
    }

    public boolean isEmailExist(String email) {
        return this.userRepository.existsByEmail(email);
    }

    public boolean isIdExist(long id) {
        return this.userRepository.existsById(id);
    }

    public ResCreateUserDTO convertToResCreateUser(User user) {
        ResCreateUserDTO resCreateUser = new ResCreateUserDTO();

        resCreateUser.setId(user.getId());
        resCreateUser.setName(user.getName());
        resCreateUser.setEmail(user.getEmail());
        resCreateUser.setGender(user.getGender());
        resCreateUser.setAddress(user.getAddress());
        resCreateUser.setAge(user.getAge());
        resCreateUser.setCreatedAt(user.getCreatedAt());

        return resCreateUser;
    }

    public ResUpdateUserDTO convertToResUpdateUser(User user) {
        ResUpdateUserDTO resUpdateUser = new ResUpdateUserDTO();

        resUpdateUser.setId(user.getId());
        resUpdateUser.setName(user.getName());
        resUpdateUser.setGender(user.getGender());
        resUpdateUser.setAddress(user.getAddress());
        resUpdateUser.setAge(user.getAge());
        resUpdateUser.setUpdatedAt(user.getUpdatedAt());

        return resUpdateUser;
    }

    public ResUserDTO convertToResUser(User user) {
        ResUserDTO resUser = new ResUserDTO();

        resUser.setId(user.getId());
        resUser.setEmail(user.getEmail());
        resUser.setName(user.getName());
        resUser.setGender(user.getGender());
        resUser.setAddress(user.getAddress());
        resUser.setAge(user.getAge());
        resUser.setCreatedAt(user.getCreatedAt());
        resUser.setUpdatedAt(user.getUpdatedAt());

        return resUser;
    }

    public void updateRefreshToken(String email, String refreshToken) {
        User currentUser = this.handleGetUserByEmail(email);

        if (currentUser != null) {
            currentUser.setRefreshToken(refreshToken);
            this.userRepository.save(currentUser);
        }
    }

    public User getUserByRefreshTokenAndEmail(String refreshToken, String email) {
        return this.userRepository.findByRefreshTokenAndEmail(refreshToken, email);
    }
}
