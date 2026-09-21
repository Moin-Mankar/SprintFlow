package com.sprintflow.backend.service;

import com.sprintflow.backend.entity.User;
import com.sprintflow.backend.repository.UserRepository;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    private final UserRepository userRepository;


    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public void updateFcmToken(String email , String fcmToken){
        User user = userRepository.findByEmail(email).orElseThrow(
                ()-> new RuntimeException("User with eamil " + email + " not found")
        );

        user.setFcmToken(fcmToken);
        userRepository.save(user);
    }

    public User getUserByEmail(String email){
        return userRepository.findByEmail(email)
                .orElseThrow(()-> new RuntimeException("User with email " + email + " not found"));
    }
}
