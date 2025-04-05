package com.foodordering.config;

import com.foodordering.model.Role;
import com.foodordering.model.User;
import com.foodordering.repository.RoleRepository;
import com.foodordering.repository.UserRepository;
import com.foodordering.util.AppConstants;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.HashSet;
import java.util.Set;

@Configuration
public class DataInitializer {

    @Bean
    CommandLineRunner initDatabase(RoleRepository roleRepository,
                                 UserRepository userRepository,
                                 PasswordEncoder passwordEncoder) {
        return args -> {
            // Initialize roles if they don't exist
            Role adminRole = createRoleIfNotFound(roleRepository, AppConstants.RoleName.ROLE_ADMIN);
            Role restaurantRole = createRoleIfNotFound(roleRepository, AppConstants.RoleName.ROLE_RESTAURANT_STAFF);
            Role customerRole = createRoleIfNotFound(roleRepository, AppConstants.RoleName.ROLE_CUSTOMER);

            // Initialize admin user if not exists
            if (!userRepository.existsByUsername("admin")) {
                User admin = new User();
                admin.setName("Admin User");
                admin.setUsername("admin");
                admin.setEmail("admin@foodordering.com");
                admin.setPassword(passwordEncoder.encode("admin123"));
                admin.setPhone("1234567890");
                admin.setAddress("Admin Address");
                
                Set<Role> adminRoles = new HashSet<>();
                adminRoles.add(adminRole);
                adminRoles.add(restaurantRole); // Admin has both roles
                admin.setRoles(adminRoles);

                userRepository.save(admin);
            }

            // Initialize restaurant staff if not exists
            if (!userRepository.existsByUsername("restaurant")) {
                User restaurantStaff = new User();
                restaurantStaff.setName("Restaurant Staff");
                restaurantStaff.setUsername("restaurant");
                restaurantStaff.setEmail("restaurant@foodordering.com");
                restaurantStaff.setPassword(passwordEncoder.encode("restaurant123"));
                restaurantStaff.setPhone("0987654321");
                restaurantStaff.setAddress("Restaurant Address");
                
                Set<Role> staffRoles = new HashSet<>();
                staffRoles.add(restaurantRole);
                restaurantStaff.setRoles(staffRoles);

                userRepository.save(restaurantStaff);
            }

            // Initialize customer if not exists
            if (!userRepository.existsByUsername("customer")) {
                User customer = new User();
                customer.setName("Customer User");
                customer.setUsername("customer");
                customer.setEmail("customer@foodordering.com");
                customer.setPassword(passwordEncoder.encode("customer123"));
                customer.setPhone("5555555555");
                customer.setAddress("Customer Address");
                
                Set<Role> customerRoles = new HashSet<>();
                customerRoles.add(customerRole);
                customer.setRoles(customerRoles);

                userRepository.save(customer);
            }
        };
    }

    private Role createRoleIfNotFound(RoleRepository roleRepository, AppConstants.RoleName roleName) {
        return roleRepository.findByName(roleName)
                .orElseGet(() -> {
                    Role role = new Role();
                    role.setName(roleName);
                    return roleRepository.save(role);
                });
    }
}