package com.sih.roadassistant.service;

import com.sih.roadassistant.dto.AuthRequest;
import com.sih.roadassistant.model.User;
import com.sih.roadassistant.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Random;
import java.util.Set;

@Service
public class AuthService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private EmailService emailService;

    private static final Set<String> DISPOSABLE_EMAIL_DOMAINS = Set.of(
            "yopmail.com", "tempmail.com", "10minutemail.com", "temp-mail.org",
            "sharklasers.com", "guerrillamail.com", "dispostable.com", "mailinator.com"
    );

    public User registerUser(AuthRequest request) {
        if (isDisposableEmail(request.getEmail())) {
            throw new RuntimeException("Temporary/Disposable email addresses are not allowed.");
        }
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new RuntimeException("Username is already taken");
        }
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email is already registered");
        }

        String verificationCode = String.format("%06d", new Random().nextInt(999999));
        User user = User.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .role("USER")
                .isVerified(false)
                .verificationCode(verificationCode)
                .build();

        User savedUser = userRepository.save(user);
        emailService.sendVerificationEmail(savedUser.getEmail(), verificationCode);
        return savedUser;
    }

    public boolean verifyEmail(String email, String code) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
        if (user.getVerificationCode() != null && user.getVerificationCode().equals(code)) {
            user.setIsVerified(true);
            userRepository.save(user);
            return true;
        }
        return false;
    }

    public User loginUser(AuthRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("Invalid email or password"));

        if (!user.getIsVerified()) {
            throw new RuntimeException("Please verify your email address first.");
        }

        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw new RuntimeException("Invalid email or password");
        }

        return user;

    }
    private boolean isDisposableEmail(String email) {
        if (email == null || !email.contains("@")) return false;
        String domain = email.substring(email.indexOf("@") + 1).toLowerCase().trim();
        return DISPOSABLE_EMAIL_DOMAINS.contains(domain);
    }
}