package com.foodordering.service.impl;

import com.foodordering.dto.request.LoginRequest;
import com.foodordering.dto.request.RegisterRequest;
import com.foodordering.dto.response.JwtAuthenticationResponse;
import com.foodordering.exception.AppException;
import com.foodordering.model.Role;
import com.foodordering.model.User;
import com.foodordering.repository.RoleRepository;
import com.foodordering.repository.UserRepository;
import com.foodordering.security.JwtTokenProvider;
import com.foodordering.service.AuthService;
import com.foodordering.util.AppConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Collections;

@Service
public class AuthServiceImpl implements AuthService {

    private static final Logger logger = LoggerFactory.getLogger(AuthServiceImpl.class);

    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider tokenProvider;

    public AuthServiceImpl(AuthenticationManager authenticationManager,
                           UserRepository userRepository,
                           RoleRepository roleRepository,
                           PasswordEncoder passwordEncoder,
                           JwtTokenProvider tokenProvider) {
        this.authenticationManager = authenticationManager;
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
        this.tokenProvider = tokenProvider;
    }

    @Override
    public JwtAuthenticationResponse authenticateUser(LoginRequest loginRequest) {
        logger.info("Authenticating user with username/email: {}", loginRequest.getUsernameOrEmail());
        
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        loginRequest.getUsernameOrEmail(),
                        loginRequest.getPassword()
                )
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);

        String jwt = tokenProvider.generateToken(authentication);
        logger.info("Authentication successful, token generated");
        return new JwtAuthenticationResponse(jwt);
    }

    @Override
    public Long registerUser(RegisterRequest registerRequest) {
        logger.info("Registering new user: {}", registerRequest.getUsername());

        if(userRepository.existsByUsername(registerRequest.getUsername())) {
            logger.warn("Username '{}' is already taken", registerRequest.getUsername());
            throw new AppException("Username is already taken!");
        }

        if(userRepository.existsByEmail(registerRequest.getEmail())) {
            logger.warn("Email '{}' is already in use", registerRequest.getEmail());
            throw new AppException("Email Address already in use!");
        }

        User user = new User();
        user.setName(registerRequest.getName());
        user.setUsername(registerRequest.getUsername());
        user.setEmail(registerRequest.getEmail());
        user.setPassword(passwordEncoder.encode(registerRequest.getPassword()));
        user.setPhone(registerRequest.getPhone());
        user.setAddress(registerRequest.getAddress());

        Role userRole = roleRepository.findByName(AppConstants.RoleName.ROLE_CUSTOMER)
                .orElseThrow(() -> {
                    logger.error("User role '{}' not found", AppConstants.RoleName.ROLE_CUSTOMER);
                    return new AppException("User Role not set.");
                });

        user.setRoles(Collections.singleton(userRole));

        User result = userRepository.save(user);
        logger.info("User '{}' registered successfully with ID {}", result.getUsername(), result.getId());
        return result.getId();
    }
}
