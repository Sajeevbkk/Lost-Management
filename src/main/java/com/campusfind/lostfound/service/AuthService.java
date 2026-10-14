package com.campusfind.lostfound.service;

import com.campusfind.lostfound.config.JwtService;
import com.campusfind.lostfound.dto.AuthRequest;
import com.campusfind.lostfound.dto.AuthResponse;
import com.campusfind.lostfound.dto.RegisterRequest;
import com.campusfind.lostfound.model.Role;
import com.campusfind.lostfound.model.User;
import com.campusfind.lostfound.repository.UserRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder, JwtService jwtService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByCollegeEmail(request.getCollegeEmail().trim())) {
            throw new IllegalArgumentException("College email is already registered");
        }
        if (userRepository.existsByCampusId(request.getCampusId().trim())) {
            throw new IllegalArgumentException("Student or Staff ID is already registered");
        }

        Role role = request.getRole() != null ? request.getRole() : Role.STUDENT;

        User user = new User(
                request.getFullName().trim(),
                request.getCollegeEmail().trim().toLowerCase(),
                request.getCampusId().trim().toUpperCase(),
                request.getDepartment().trim(),
                request.getPhoneNumber().trim(),
                passwordEncoder.encode(request.getPassword()),
                role
        );

        User savedUser = userRepository.save(user);
        String token = jwtService.generateToken(savedUser);

        return new AuthResponse(
                token,
                savedUser.getUserId(),
                savedUser.getFullName(),
                savedUser.getCollegeEmail(),
                savedUser.getCampusId(),
                savedUser.getDepartment(),
                savedUser.getPhoneNumber(),
                savedUser.getRole()
        );
    }

    public AuthResponse login(AuthRequest request) {
        String email = request.getCollegeEmail().trim().toLowerCase();
        User user = userRepository.findByCollegeEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("Invalid email or password"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw new IllegalArgumentException("Invalid email or password");
        }

        String token = jwtService.generateToken(user);

        return new AuthResponse(
                token,
                user.getUserId(),
                user.getFullName(),
                user.getCollegeEmail(),
                user.getCampusId(),
                user.getDepartment(),
                user.getPhoneNumber(),
                user.getRole()
        );
    }

    public User getCurrentAuthenticatedUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || auth.getPrincipal() == null) {
            throw new IllegalStateException("User is not authenticated");
        }

        if (auth.getPrincipal() instanceof User) {
            return (User) auth.getPrincipal();
        }

        String email = auth.getName();
        return userRepository.findByCollegeEmail(email)
                .orElseThrow(() -> new IllegalStateException("Authenticated user not found in database"));
    }
}
