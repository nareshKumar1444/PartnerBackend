package com.partner.backend.admin.service;

import com.partner.backend.admin.dto.AdminLoginRequest;
import com.partner.backend.admin.dto.AuthTokenResponse;
import com.partner.backend.common.entity.User;
import com.partner.backend.common.entity.UserRole;
import com.partner.backend.common.exception.BadRequestException;
import com.partner.backend.common.exception.UnauthorizedException;
import com.partner.backend.common.repository.UserRepository;
import com.partner.backend.common.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AdminAuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    private final RedisTemplate<String, String> redisTemplate;
    public AuthTokenResponse login(AdminLoginRequest req) {
        User user = userRepository.findByEmail(req.getEmail())
                .orElseThrow(() -> new UnauthorizedException("Invalid email or password"));

        if (user.getRole() != UserRole.ADMIN) {
            throw new UnauthorizedException("Access denied: not an admin account");
        }

        if (!passwordEncoder.matches(req.getPassword(), user.getPasswordHash())) {
            throw new UnauthorizedException("Invalid email or password");
        }

        String token = jwtUtil.generateToken(user.getEmail(), user.getRole().name(), null);
        redisTemplate.opsForValue().set("LOGIN_USER:" + user.getEmail(), token);
        return new AuthTokenResponse(token, user.getRole().name(), user.getEmail());
    }

    public void logout(String authHeader) {
        try {
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                return;
            }
            String token = authHeader.substring(7);
            String email = jwtUtil.extractEmail(token);
            if (email != null) {
                redisTemplate.delete("LOGIN_USER:" + email);
            }
        } catch (Exception e) {
            // Never break logout — client session is cleared regardless.
        }
    }

    /**
     * Seeds the default admin account if none exists.
     * Called at startup from a CommandLineRunner bean.
     */
    public void seedAdmin(String email, String password) {
        if (!userRepository.existsByEmail(email)) {
            User admin = User.builder()
                    .email(email)
                    .passwordHash(passwordEncoder.encode(password))
                    .role(UserRole.ADMIN)
                    .build();
            userRepository.save(admin);
        }
    }
}
