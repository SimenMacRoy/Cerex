package com.cerex.security;

import com.cerex.domain.User;
import com.cerex.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * Loads user details from the database for Spring Security authentication.
 */
@Service
@RequiredArgsConstructor
public class CerexUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        User user = userRepository.findByEmail(email)
            .orElseThrow(() -> new UsernameNotFoundException(
                "User not found with email: " + email));
        return new CerexUserDetails(user);
    }

    @Transactional(readOnly = true)
    public UserDetails loadUserById(UUID userId) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new UsernameNotFoundException(
                "User not found with ID: " + userId));
        return new CerexUserDetails(user);
    }
}
