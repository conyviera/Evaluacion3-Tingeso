package com.example.ProyectoTingeso.Services;

import com.example.ProyectoTingeso.Entities.UserEntity;
import com.example.ProyectoTingeso.Repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

@Service
public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    private static final List<String> ROLES_A_IGNORAR = Arrays.asList(
            "offline_access",
            "uma_authorization",
            "default-roles-sisgr-realm");

    /**
     * RF 7.1: Register system users with access credentials
     * 
     * @return
     */

    public UserEntity getUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Jwt jwt = (Jwt) authentication.getPrincipal();
        String keycloakId = jwt.getSubject();

        return userRepository.findByKeycloakId(keycloakId)
                .orElseGet(() -> {

                    UserEntity newUser = new UserEntity();
                    newUser.setKeycloakId(keycloakId);
                    newUser.setUsername(jwt.getClaimAsString("preferred_username"));
                    newUser.setEmail(jwt.getClaimAsString("email"));
                    newUser.setName(jwt.getClaimAsString("name"));

                    Map<String, Object> realmAccess = (Map<String, Object>) jwt.getClaims().get("realm_access");

                    List<String> rol = new ArrayList<>();
                    if (realmAccess != null && realmAccess.get("roles") instanceof List<?>) {

                        rol = (List<String>) realmAccess.get("roles");
                    }

                    List<String> appRol = rol.stream()
                            .filter(role -> !ROLES_A_IGNORAR.contains(role))
                            .toList();

                    newUser.setRol(String.join(",", appRol));

                    return userRepository.save(newUser);
                });
    }
}