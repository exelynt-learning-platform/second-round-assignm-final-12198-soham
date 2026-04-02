package com.mjs.ecommerce.service;

import com.mjs.ecommerce.constants.Constants;
import com.mjs.ecommerce.model.User;
import com.mjs.ecommerce.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserServiceImpl implements UserService {
    @Autowired
    UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;
    @Override
    public User createUser(User user) {
        return userRepository.save(user);
    }

    @Override
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    @Override
    public User getUserById(Long id) {
        return userRepository.findById(id).orElseThrow(()->new RuntimeException("User Not Found"));
    }

    @Override
    public User updateUser(Long id, User user) {
        User existing = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException(Constants.USER_NOT_FOUND));

        existing.setName(user.getName());
        existing.setEmail(user.getEmail());
        existing.setPassword(passwordEncoder.encode(user.getPassword()));
        return userRepository.save(existing);
    }

    @Override
    public void deleteUser(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException(Constants.USER_NOT_FOUND));

        userRepository.delete(user);
    }
}
