package vn.hoidanit.jobhunter.service;

import java.util.ArrayList;
import java.util.Optional;

import org.springframework.stereotype.Service;

import vn.hoidanit.jobhunter.domain.User;
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

    public ArrayList<User> handleGetAllUser() {
        return (ArrayList<User>) this.userRepository.findAll();
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
}
