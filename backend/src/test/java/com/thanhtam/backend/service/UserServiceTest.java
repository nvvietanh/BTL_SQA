package com.thanhtam.backend.service;

import com.thanhtam.backend.config.JwtUtils;
import com.thanhtam.backend.dto.UserExport;
import com.thanhtam.backend.entity.PasswordResetToken;
import com.thanhtam.backend.entity.Profile;
import com.thanhtam.backend.entity.Role;
import com.thanhtam.backend.entity.User;
import com.thanhtam.backend.repository.PasswordResetTokenRepository;
import com.thanhtam.backend.repository.UserRepository;
import com.thanhtam.backend.ultilities.ERole;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;

import javax.mail.MessagingException;
import java.util.*;

@RunWith(MockitoJUnitRunner.class)
public class UserServiceTest {

    @InjectMocks
    private UserServiceImpl userService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private RoleService roleService;

    @Mock
    private PasswordResetTokenRepository passwordResetTokenRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private EmailService emailService;

    @Mock
    private JwtUtils jwtUtils; // Nếu cần mock new JwtUtils() thì phải đổi cách inject

    private User user;
    private Role role;

    @Before
    public void setup() {
        user = new User();
        user.setId(1L);
        user.setUsername("testuser");
        user.setEmail("test@example.com");
        user.setPassword("encodedPassword");
        user.setProfile(new Profile());

        role = new Role();
        role.setId(4L);
        role.setName(ERole.ROLE_STUDENT);
    }

    @Test
    @Transactional
    @Rollback
    public void testGetUserByUsername_Found() {
        Mockito.when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));

        Optional<User> result = userService.getUserByUsername("testuser");

        assertTrue(result.isPresent());
        assertEquals("testuser", result.get().getUsername());
    }

    @Test
    @Transactional
    @Rollback
    public void testGetUserByUsername_NotFound() {
        Mockito.when(userRepository.findByUsername("unknown")).thenReturn(Optional.empty());

        Optional<User> result = userService.getUserByUsername("unknown");

        assertFalse(result.isPresent());
    }

    @Test
    @Transactional
    @Rollback
    public void testGetUserName() {
        Authentication authentication = Mockito.mock(Authentication.class);
        SecurityContext securityContext = Mockito.mock(SecurityContext.class);

        Mockito.when(securityContext.getAuthentication()).thenReturn(authentication);
        Mockito.when(authentication.getName()).thenReturn("testuser");
        SecurityContextHolder.setContext(securityContext);

        String username = userService.getUserName();

        assertEquals("testuser", username);
    }


    @Test
    @Transactional
    @Rollback
    public void testExistsByUsername() {
        Mockito.when(userRepository.existsByUsername("testuser")).thenReturn(true);

        boolean exists = userService.existsByUsername("testuser");

        assertTrue(exists);
    }

    @Test
    @Transactional
    @Rollback
    public void testExistsByEmail() {
        Mockito.when(userRepository.existsByEmail("test@example.com")).thenReturn(false);

        boolean exists = userService.existsByEmail("test@example.com");

        assertFalse(exists);
    }

    @Test
    @Transactional
    @Rollback
    public void testFindUsersByPage() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<User> page = new PageImpl<>(Collections.singletonList(user));
        Mockito.when(userRepository.findAll(pageable)).thenReturn(page);

        Page<User> result = userService.findUsersByPage(pageable);

        assertEquals(1, result.getTotalElements());
    }

    @Test
    @Transactional
    @Rollback
    public void testFindUsersDeletedByPage() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<User> page = new PageImpl<>(Collections.singletonList(user));
        Mockito.when(userRepository.findAllByDeleted(false, pageable)).thenReturn(page);

        Page<User> result = userService.findUsersDeletedByPage(pageable, false);

        assertEquals(1, result.getTotalElements());
    }

    @Test
    @Transactional
    @Rollback
    public void testFindAllByDeletedAndUsernameContains() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<User> page = new PageImpl<>(Collections.singletonList(user));
        Mockito.when(userRepository.findAllByDeletedAndUsernameContains(false, "test", pageable)).thenReturn(page);

        Page<User> result = userService.findAllByDeletedAndUsernameContains(false, "test", pageable);

        assertEquals(1, result.getTotalElements());
    }

    @Test
    @Transactional
    @Rollback
    public void testCreateUser_WithRoles() {
        user.setRoles(new HashSet<>(Collections.singletonList(role)));
        Mockito.when(roleService.findByName(Mockito.any())).thenReturn(Optional.of(role));
        Mockito.when(passwordEncoder.encode(Mockito.anyString())).thenReturn("encodedPassword");
        Mockito.when(userRepository.save(Mockito.any(User.class))).thenReturn(user);

        User createdUser = userService.createUser(user);

        assertNotNull(createdUser);
        assertEquals("encodedPassword", createdUser.getPassword());
    }

    @Test
    @Transactional
    @Rollback
    public void testCreateUser_NoRoles_DefaultStudent() {
        user.setRoles(null);
        Mockito.when(roleService.findByName(ERole.ROLE_STUDENT)).thenReturn(Optional.of(role));
        Mockito.when(passwordEncoder.encode(Mockito.anyString())).thenReturn("encodedPassword");
        Mockito.when(userRepository.save(Mockito.any(User.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        User createdUser = userService.createUser(user);

        assertNotNull(createdUser);
        assertTrue(createdUser.getRoles().contains(role));
    }

    @Test
    @Transactional
    @Rollback
    public void testFindUserById_Found() {
        Mockito.when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        Optional<User> result = userService.findUserById(1L);

        assertTrue(result.isPresent());
    }

    @Test
    @Transactional
    @Rollback
    public void testFindAllByDeletedToExport() {
        Mockito.when(userRepository.findAllByDeleted(false)).thenReturn(Collections.singletonList(user));

        List<UserExport> exports = userService.findAllByDeletedToExport(false);

        assertEquals(1, exports.size());
        assertEquals(user.getUsername(), exports.get(0).getUsername());
    }

    @Test
    @Transactional
    @Rollback
    public void testUpdateUser() {
        Mockito.when(userRepository.save(user)).thenReturn(user);

        userService.updateUser(user);

        Mockito.verify(userRepository, Mockito.times(1)).save(user);
    }

    @Test
    @Transactional
    @Rollback
    public void testFindAllByIntakeId() {
        Mockito.when(userRepository.findAllByIntakeId(1L)).thenReturn(Collections.singletonList(user));

        List<User> result = userService.findAllByIntakeId(1L);

        assertEquals(1, result.size());
    }

    @Test
    @Transactional
    @Rollback
    public void testRequestPasswordReset_Success() throws MessagingException {
        Mockito.when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(user));
        Mockito.when(jwtUtils.generatePasswordResetToken(1L)).thenReturn("eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJ0aGFuaHRhbTI4c3MiLCJpYXQiOjE3NDQyOTQ4MTIsImV4cCI6MTc0NDM4MTIxMiwicm9sZSI6W3siYXV0aG9yaXR5IjoiUk9MRV9BRE1JTiJ9XX0.dlO69r0W_COp4s0Y1zZ1e4cGUE-56KPuAW5WeebauMCzT0Q8Ct3tXw_flMBuSQuqIRQ4tcy_JNju-wjL5X5F0w");
        Mockito.doNothing().when(emailService).resetPassword(Mockito.anyString(), Mockito.anyString());

        boolean result = userService.requestPasswordReset("test@example.com");

        assertTrue(result);
    }

    @Test
    @Transactional
    @Rollback
    public void testRequestPasswordReset_Failure() throws MessagingException {
        Mockito.when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.empty());

        boolean result = userService.requestPasswordReset("test@example.com");

        assertFalse(result);
    }


    @Test
    @Transactional
    @Rollback
    public void testResetPassword_Success() {
        PasswordResetToken resetToken = new PasswordResetToken();
        resetToken.setUser(user);

        Mockito.when(jwtUtils.hasTokenExpired("eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJ0aGFuaHRhbTI4c3MiLCJpYXQiOjE3NDQyOTQ4MTIsImV4cCI6MTc0NDM4MTIxMiwicm9sZSI6W3siYXV0aG9yaXR5IjoiUk9MRV9BRE1JTiJ9XX0.dlO69r0W_COp4s0Y1zZ1e4cGUE-56KPuAW5WeebauMCzT0Q8Ct3tXw_flMBuSQuqIRQ4tcy_JNju-wjL5X5F0w")).thenReturn(false);
        Mockito.when(passwordResetTokenRepository.findByToken("eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJ0aGFuaHRhbTI4c3MiLCJpYXQiOjE3NDQyOTQ4MTIsImV4cCI6MTc0NDM4MTIxMiwicm9sZSI6W3siYXV0aG9yaXR5IjoiUk9MRV9BRE1JTiJ9XX0.dlO69r0W_COp4s0Y1zZ1e4cGUE-56KPuAW5WeebauMCzT0Q8Ct3tXw_flMBuSQuqIRQ4tcy_JNju-wjL5X5F0w")).thenReturn(resetToken);
        Mockito.when(passwordEncoder.encode("newPassword")).thenReturn("encodedPassword");
        Mockito.when(userRepository.save(Mockito.any(User.class))).thenReturn(user);

        boolean result = userService.resetPassword("eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJ0aGFuaHRhbTI4c3MiLCJpYXQiOjE3NDQyOTQ4MTIsImV4cCI6MTc0NDM4MTIxMiwicm9sZSI6W3siYXV0aG9yaXR5IjoiUk9MRV9BRE1JTiJ9XX0.dlO69r0W_COp4s0Y1zZ1e4cGUE-56KPuAW5WeebauMCzT0Q8Ct3tXw_flMBuSQuqIRQ4tcy_JNju-wjL5X5F0w", "newPassword");

        assertTrue(result);
    }

    @Test
    @Transactional
    @Rollback
    public void testResetPassword_TokenExpired() {
        Mockito.when(jwtUtils.hasTokenExpired("eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJ0aGFuaHRhbTI4c3MiLCJpYXQiOjE3NDQyOTQ4MTIsImV4cCI6MTc0NDM4MTIxMiwicm9sZSI6W3siYXV0aG9yaXR5IjoiUk9MRV9BRE1JTiJ9XX0.dlO69r0W_COp4s0Y1zZ1e4cGUE-56KPuAW5WeebauMCzT0Q8Ct3tXw_flMBuSQuqIRQ4tcy_JNju-wjL5X5F0w")).thenReturn(true);

        boolean result = userService.resetPassword("eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJ0aGFuaHRhbTI4c3MiLCJpYXQiOjE3NDQyOTQ4MTIsImV4cCI6MTc0NDM4MTIxMiwicm9sZSI6W3siYXV0aG9yaXR5IjoiUk9MRV9BRE1JTiJ9XX0.dlO69r0W_COp4s0Y1zZ1e4cGUE-56KPuAW5WeebauMCzT0Q8Ct3tXw_flMBuSQuqIRQ4tcy_JNju-wjL5X5F0w", "newPassword");

        assertFalse(result);
    }

    @Test
    @Transactional
    @Rollback
    public void testResetPassword_TokenNotFound() {
        Mockito.when(jwtUtils.hasTokenExpired("eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJ0aGFuaHRhbTI4c3MiLCJpYXQiOjE3NDQyOTQ4MTIsImV4cCI6MTc0NDM4MTIxMiwicm9sZSI6W3siYXV0aG9yaXR5IjoiUk9MRV9BRE1JTiJ9XX0.dlO69r0W_COp4s0Y1zZ1e4cGUE-56KPuAW5WeebauMCzT0Q8Ct3tXw_flMBuSQuqIRQ4tcy_JNju-wjL5X5F0w")).thenReturn(false);
        Mockito.when(passwordResetTokenRepository.findByToken("eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJ0aGFuaHRhbTI4c3MiLCJpYXQiOjE3NDQyOTQ4MTIsImV4cCI6MTc0NDM4MTIxMiwicm9sZSI6W3siYXV0aG9yaXR5IjoiUk9MRV9BRE1JTiJ9XX0.dlO69r0W_COp4s0Y1zZ1e4cGUE-56KPuAW5WeebauMCzT0Q8Ct3tXw_flMBuSQuqIRQ4tcy_JNju-wjL5X5F0w")).thenReturn(null);

        boolean result = userService.resetPassword("eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJ0aGFuaHRhbTI4c3MiLCJpYXQiOjE3NDQyOTQ4MTIsImV4cCI6MTc0NDM4MTIxMiwicm9sZSI6W3siYXV0aG9yaXR5IjoiUk9MRV9BRE1JTiJ9XX0.dlO69r0W_COp4s0Y1zZ1e4cGUE-56KPuAW5WeebauMCzT0Q8Ct3tXw_flMBuSQuqIRQ4tcy_JNju-wjL5X5F0w", "newPassword");

        assertFalse(result);
    }


    @Test
    @Transactional
    @Rollback
    public void testFindAllByUsernameContainsOrEmailContains() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<User> page = new PageImpl<>(Collections.singletonList(user));
        Mockito.when(userRepository.findAllByUsernameContainsOrEmailContains("test", "test@example.com", pageable)).thenReturn(page);

        Page<User> result = userService.findAllByUsernameContainsOrEmailContains("test", "test@example.com", pageable);

        assertEquals(1, result.getTotalElements());
    }

}