package com.smartstudy.assistant.service;

import com.smartstudy.assistant.dto.AuthRequest;
import com.smartstudy.assistant.dto.LoginRequest;
import com.smartstudy.assistant.model.User;
import com.smartstudy.assistant.repository.UserRepository;
import com.smartstudy.assistant.security.JwtService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthService(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            JwtService jwtService) {

        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    public Map<String, Object> signup(AuthRequest request) {

        String email = request.getEmail().trim().toLowerCase();

        if (userRepository.existsByEmail(email)) {
            throw new RuntimeException("Email is already registered.");
        }

        User user = new User();

        user.setName(request.getName().trim());
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(request.getPassword()));

        // NULL means no profile picture.
        user.setProfilePicture(null);

        userRepository.save(user);

        return response(user);
    }

    public Map<String, Object> login(LoginRequest request) {

        String email = request.getEmail().trim().toLowerCase();

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Invalid email or password."));

        if (!passwordEncoder.matches(
                request.getPassword(),
                user.getPassword())) {

            throw new RuntimeException("Invalid email or password.");
        }

        return response(user);
    }

    private Map<String, Object> response(User user) {

        return Map.of(
                "token", jwtService.generateToken(user.getEmail()),
                "user", Map.of(
                        "id", user.getId(),
                        "name", user.getName(),
                        "email", user.getEmail(),
                        "profilePicture",
                        user.getProfilePicture() == null
                                ? ""
                                : user.getProfilePicture()
                )
        );
    }
}
