package vn.hoidanit.jobhunter.service;

import java.util.ArrayList;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import vn.hoidanit.jobhunter.domain.User;
import vn.hoidanit.jobhunter.domain.dto.Meta;
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

    public ResultPaginationDTO handleGetAllUser(Pageable pageable) {
        Page<User> page = this.userRepository.findAll(pageable);
        ResultPaginationDTO resultPaginationDTO = new ResultPaginationDTO();
        Meta meta = new Meta();

        meta.setCurrentPage(page.getNumber());
        meta.setPageSize(page.getSize());
        meta.setPages(page.getTotalPages());
        meta.setTotal(page.getTotalElements());

        resultPaginationDTO.setMeta(meta);
        resultPaginationDTO.setResult(page.getContent());

        return resultPaginationDTO;
    }

    public ResultPaginationDTO handleGetAllUserFilter(Specification<User> spec, Pageable pageable) {
        Page<User> page = this.userRepository.findAll(spec, pageable);
        ResultPaginationDTO resultPaginationDTO = new ResultPaginationDTO();
        Meta meta = new Meta();

        meta.setCurrentPage(page.getNumber()+1);
        meta.setPageSize(page.getSize());
        meta.setPages(page.getTotalPages());
        meta.setTotal(page.getTotalElements());

        resultPaginationDTO.setMeta(meta);
        resultPaginationDTO.setResult(page.getContent());

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
            currentUser.setEmail(updateUser.getEmail());
            currentUser.setPassword(updateUser.getPassword());
            return this.userRepository.save(currentUser);
        }
        return null;
    }

    public User handleGetUserByEmail(String email) {
        return this.userRepository.findByEmail(email);
    }
}
