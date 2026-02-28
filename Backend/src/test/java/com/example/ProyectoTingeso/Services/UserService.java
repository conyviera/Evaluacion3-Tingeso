package com.example.ProyectoTingeso.Services;

import com.example.ProyectoTingeso.Entities.UserEntity;
import com.example.ProyectoTingeso.Repositories.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;

import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;


class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private SecurityContext securityContext;

    @Mock
    private Authentication authentication;

    @Mock
    private Jwt jwt;

    @InjectMocks
    private UserService userService;

    private UserEntity userEntity;


    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        // Configurar SecurityContextHolder con el mock
        SecurityContextHolder.setContext(securityContext);

        userEntity = new UserEntity();
        userEntity.setIdUser(1L);
        userEntity.setKeycloakId("uuid-keycloak-123");
        userEntity.setUsername("jdoe");
        userEntity.setEmail("jdoe@example.com");
        userEntity.setName("John Doe");
        userEntity.setRol("USER");
    }


    // ==================== Tests for getUser - User Exists ====================

    @Test
    void whenGetUser_andUserExists_thenReturnExistingUser() {
        //Given
        String keycloakId = "uuid-keycloak-123";

        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(jwt);
        when(jwt.getSubject()).thenReturn(keycloakId);
        when(userRepository.findByKeycloakId(keycloakId)).thenReturn(Optional.of(userEntity));

        //When
        UserEntity result = userService.getUser();

        //Then
        assertThat(result).isEqualTo(userEntity);
        assertThat(result.getKeycloakId()).isEqualTo(keycloakId);
        verify(userRepository, times(1)).findByKeycloakId(keycloakId);
        verify(userRepository, never()).save(any());
    }


    // ==================== Tests for getUser - User Does Not Exist ====================

    @Test
    void whenGetUser_andUserDoesNotExist_thenCreateNewUser() {
        //Given
        String keycloakId = "uuid-keycloak-456";
        Map<String, Object> claims = new HashMap<>();
        Map<String, Object> realmAccess = new HashMap<>();
        List<String> roles = Arrays.asList("USER", "ADMIN");
        realmAccess.put("roles", roles);
        claims.put("realm_access", realmAccess);

        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(jwt);
        when(jwt.getSubject()).thenReturn(keycloakId);
        when(jwt.getClaimAsString("preferred_username")).thenReturn("jsmith");
        when(jwt.getClaimAsString("email")).thenReturn("jsmith@example.com");
        when(jwt.getClaimAsString("name")).thenReturn("Jane Smith");
        when(jwt.getClaims()).thenReturn(claims);
        when(userRepository.findByKeycloakId(keycloakId)).thenReturn(Optional.empty());

        UserEntity newUser = new UserEntity();
        newUser.setIdUser(2L);
        newUser.setKeycloakId(keycloakId);
        newUser.setUsername("jsmith");
        newUser.setEmail("jsmith@example.com");
        newUser.setName("Jane Smith");
        newUser.setRol("USER,ADMIN");

        when(userRepository.save(any(UserEntity.class))).thenReturn(newUser);

        //When
        UserEntity result = userService.getUser();

        //Then
        assertThat(result.getKeycloakId()).isEqualTo(keycloakId);
        assertThat(result.getUsername()).isEqualTo("jsmith");
        assertThat(result.getEmail()).isEqualTo("jsmith@example.com");
        assertThat(result.getName()).isEqualTo("Jane Smith");
        assertThat(result.getRol()).isEqualTo("USER,ADMIN");
        verify(userRepository, times(1)).save(any(UserEntity.class));
    }


    // ==================== Tests for getUser - Role Filtering ====================

    @Test
    void whenGetUser_withValidRoles_thenFilterIgnoredRoles() {
        //Given
        String keycloakId = "uuid-keycloak-789";
        Map<String, Object> claims = new HashMap<>();
        Map<String, Object> realmAccess = new HashMap<>();
        List<String> roles = Arrays.asList(
                "USER",
                "ADMIN",
                "offline_access",
                "uma_authorization",
                "default-roles-sisgr-realm"
        );
        realmAccess.put("roles", roles);
        claims.put("realm_access", realmAccess);

        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(jwt);
        when(jwt.getSubject()).thenReturn(keycloakId);
        when(jwt.getClaimAsString("preferred_username")).thenReturn("admin_user");
        when(jwt.getClaimAsString("email")).thenReturn("admin@example.com");
        when(jwt.getClaimAsString("name")).thenReturn("Admin User");
        when(jwt.getClaims()).thenReturn(claims);
        when(userRepository.findByKeycloakId(keycloakId)).thenReturn(Optional.empty());

        UserEntity newUser = new UserEntity();
        newUser.setIdUser(3L);
        newUser.setKeycloakId(keycloakId);
        newUser.setUsername("admin_user");
        newUser.setEmail("admin@example.com");
        newUser.setName("Admin User");
        newUser.setRol("USER,ADMIN");

        when(userRepository.save(any(UserEntity.class))).thenReturn(newUser);

        //When
        UserEntity result = userService.getUser();

        //Then
        assertThat(result.getRol()).isEqualTo("USER,ADMIN");
        assertThat(result.getRol()).doesNotContain("offline_access");
        assertThat(result.getRol()).doesNotContain("uma_authorization");
        assertThat(result.getRol()).doesNotContain("default-roles-sisgr-realm");
        verify(userRepository, times(1)).save(any(UserEntity.class));
    }


    @Test
    void whenGetUser_withOnlyIgnoredRoles_thenReturnEmptyRolString() {
        //Given
        String keycloakId = "uuid-keycloak-000";
        Map<String, Object> claims = new HashMap<>();
        Map<String, Object> realmAccess = new HashMap<>();
        List<String> roles = Arrays.asList(
                "offline_access",
                "uma_authorization",
                "default-roles-sisgr-realm"
        );
        realmAccess.put("roles", roles);
        claims.put("realm_access", realmAccess);

        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(jwt);
        when(jwt.getSubject()).thenReturn(keycloakId);
        when(jwt.getClaimAsString("preferred_username")).thenReturn("limited_user");
        when(jwt.getClaimAsString("email")).thenReturn("limited@example.com");
        when(jwt.getClaimAsString("name")).thenReturn("Limited User");
        when(jwt.getClaims()).thenReturn(claims);
        when(userRepository.findByKeycloakId(keycloakId)).thenReturn(Optional.empty());

        UserEntity newUser = new UserEntity();
        newUser.setIdUser(4L);
        newUser.setKeycloakId(keycloakId);
        newUser.setUsername("limited_user");
        newUser.setEmail("limited@example.com");
        newUser.setName("Limited User");
        newUser.setRol("");

        when(userRepository.save(any(UserEntity.class))).thenReturn(newUser);

        //When
        UserEntity result = userService.getUser();

        //Then
        assertThat(result.getRol()).isEmpty();
        verify(userRepository, times(1)).save(any(UserEntity.class));
    }


    // ==================== Tests for getUser - Null/Empty Cases ====================

    @Test
    void whenGetUser_withNullRealmAccess_thenReturnUserWithEmptyRol() {
        //Given
        String keycloakId = "uuid-keycloak-111";
        Map<String, Object> claims = new HashMap<>();
        claims.put("realm_access", null);

        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(jwt);
        when(jwt.getSubject()).thenReturn(keycloakId);
        when(jwt.getClaimAsString("preferred_username")).thenReturn("norole_user");
        when(jwt.getClaimAsString("email")).thenReturn("norole@example.com");
        when(jwt.getClaimAsString("name")).thenReturn("No Role User");
        when(jwt.getClaims()).thenReturn(claims);
        when(userRepository.findByKeycloakId(keycloakId)).thenReturn(Optional.empty());

        UserEntity newUser = new UserEntity();
        newUser.setIdUser(5L);
        newUser.setKeycloakId(keycloakId);
        newUser.setUsername("norole_user");
        newUser.setEmail("norole@example.com");
        newUser.setName("No Role User");
        newUser.setRol("");

        when(userRepository.save(any(UserEntity.class))).thenReturn(newUser);

        //When
        UserEntity result = userService.getUser();

        //Then
        assertThat(result.getRol()).isEmpty();
        verify(userRepository, times(1)).save(any(UserEntity.class));
    }


    @Test
    void whenGetUser_withEmptyRolesList_thenReturnUserWithEmptyRol() {
        //Given
        String keycloakId = "uuid-keycloak-222";
        Map<String, Object> claims = new HashMap<>();
        Map<String, Object> realmAccess = new HashMap<>();
        realmAccess.put("roles", new ArrayList<>());
        claims.put("realm_access", realmAccess);

        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(jwt);
        when(jwt.getSubject()).thenReturn(keycloakId);
        when(jwt.getClaimAsString("preferred_username")).thenReturn("empty_roles_user");
        when(jwt.getClaimAsString("email")).thenReturn("empty@example.com");
        when(jwt.getClaimAsString("name")).thenReturn("Empty Roles User");
        when(jwt.getClaims()).thenReturn(claims);
        when(userRepository.findByKeycloakId(keycloakId)).thenReturn(Optional.empty());

        UserEntity newUser = new UserEntity();
        newUser.setIdUser(6L);
        newUser.setKeycloakId(keycloakId);
        newUser.setUsername("empty_roles_user");
        newUser.setEmail("empty@example.com");
        newUser.setName("Empty Roles User");
        newUser.setRol("");

        when(userRepository.save(any(UserEntity.class))).thenReturn(newUser);

        //When
        UserEntity result = userService.getUser();

        //Then
        assertThat(result.getRol()).isEmpty();
        verify(userRepository, times(1)).save(any(UserEntity.class));
    }


    @Test
    void whenGetUser_withMultipleValidRoles_thenReturnCommaSeparatedRoles() {
        //Given
        String keycloakId = "uuid-keycloak-333";
        Map<String, Object> claims = new HashMap<>();
        Map<String, Object> realmAccess = new HashMap<>();
        List<String> roles = Arrays.asList("USER", "ADMIN", "MANAGER", "SUPERVISOR");
        realmAccess.put("roles", roles);
        claims.put("realm_access", realmAccess);

        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(jwt);
        when(jwt.getSubject()).thenReturn(keycloakId);
        when(jwt.getClaimAsString("preferred_username")).thenReturn("super_admin");
        when(jwt.getClaimAsString("email")).thenReturn("superadmin@example.com");
        when(jwt.getClaimAsString("name")).thenReturn("Super Admin");
        when(jwt.getClaims()).thenReturn(claims);
        when(userRepository.findByKeycloakId(keycloakId)).thenReturn(Optional.empty());

        UserEntity newUser = new UserEntity();
        newUser.setIdUser(7L);
        newUser.setKeycloakId(keycloakId);
        newUser.setUsername("super_admin");
        newUser.setEmail("superadmin@example.com");
        newUser.setName("Super Admin");
        newUser.setRol("USER,ADMIN,MANAGER,SUPERVISOR");

        when(userRepository.save(any(UserEntity.class))).thenReturn(newUser);

        //When
        UserEntity result = userService.getUser();

        //Then
        assertThat(result.getRol()).isEqualTo("USER,ADMIN,MANAGER,SUPERVISOR");
        assertThat(result.getRol().split(",")).hasSize(4);
        verify(userRepository, times(1)).save(any(UserEntity.class));
    }

}