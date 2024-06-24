package vn.hoidanit.jobhunter.service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import vn.hoidanit.jobhunter.domain.Company;
import vn.hoidanit.jobhunter.domain.User;
import vn.hoidanit.jobhunter.domain.response.ResultPaginationDTO;
import vn.hoidanit.jobhunter.domain.response.user.ResCreateUserDTO;
import vn.hoidanit.jobhunter.domain.response.user.ResUpdateUserDTO;
import vn.hoidanit.jobhunter.domain.response.user.ResUserDTO;
import vn.hoidanit.jobhunter.repository.UserRepository;

@Service
public class UserService {
    private UserRepository userRepository;
    private CompanyService companyService;

    public UserService(UserRepository userRepository, CompanyService companyService) {
        this.userRepository = userRepository;
        this.companyService = companyService;
    }

    public User handleCreateUser(User user) {
        if (user.getCompany() != null) {
            Company company = this.companyService.handleGetCompanyById(user.getCompany().getId());
            user.setCompany(company != null ? company : null);
        }

        return this.userRepository.save(user);
    }

    public void handleDeleteUser(long id) {
        this.userRepository.deleteById(id);
    }

    public ResultPaginationDTO handleGetAllUsers(Specification<User> spec, Pageable pageable) {
        Page<User> page = this.userRepository.findAll(spec, pageable);
        ResultPaginationDTO resultPaginationDTO = new ResultPaginationDTO();
        ResultPaginationDTO.Meta meta = new ResultPaginationDTO.Meta();

        meta.setPage(pageable.getPageNumber() + 1);
        meta.setPageSize(pageable.getPageSize());
        meta.setPages(page.getTotalPages());
        meta.setTotal(page.getTotalElements());

        resultPaginationDTO.setMeta(meta);

        List<ResUserDTO> list = page.getContent().stream().map(p -> new ResUserDTO(p.getId(), p.getEmail(), p.getName(),
                p.getGender(), p.getAddress(), p.getAge(), p.getUpdatedAt(), p.getCreatedAt(),
                p.getCompany() != null ? new ResUserDTO.CompanyUser(p.getCompany().getId(), p.getCompany().getName())
                        : null))
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

            if (updateUser.getCompany() != null) {
                Company company = this.companyService.handleGetCompanyById(updateUser.getCompany().getId());
                currentUser.setCompany(company != null ? company : null);
            }

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
        ResCreateUserDTO.CompanyUserCreate companyUser = new ResCreateUserDTO.CompanyUserCreate();

        resCreateUser.setId(user.getId());
        resCreateUser.setName(user.getName());
        resCreateUser.setEmail(user.getEmail());
        resCreateUser.setGender(user.getGender());
        resCreateUser.setAddress(user.getAddress());
        resCreateUser.setAge(user.getAge());
        resCreateUser.setCreatedAt(user.getCreatedAt());

        if (user.getCompany() != null) {
            companyUser.setId(user.getCompany().getId());
            companyUser.setName(user.getCompany().getName());

            resCreateUser.setCompany(companyUser);
        }

        return resCreateUser;
    }

    public ResUpdateUserDTO convertToResUpdateUser(User user) {
        ResUpdateUserDTO resUpdateUser = new ResUpdateUserDTO();
        ResUpdateUserDTO.CompanyUserUpdate companyUserUpdate = new ResUpdateUserDTO.CompanyUserUpdate();

        resUpdateUser.setId(user.getId());
        resUpdateUser.setName(user.getName());
        resUpdateUser.setGender(user.getGender());
        resUpdateUser.setAddress(user.getAddress());
        resUpdateUser.setAge(user.getAge());
        resUpdateUser.setUpdatedAt(user.getUpdatedAt());

        if (user.getCompany() != null) {
            companyUserUpdate.setId(user.getCompany().getId());
            companyUserUpdate.setName(user.getCompany().getName());

            resUpdateUser.setCompany(companyUserUpdate);
        }

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

        if (user.getCompany() != null) {
            ResUserDTO.CompanyUser companyUser = new ResUserDTO.CompanyUser();
            companyUser.setId(user.getCompany().getId());
            companyUser.setName(user.getCompany().getName());

            resUser.setCompany(companyUser);
        }

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
