package com.thanhtam.backend.service;

import com.thanhtam.backend.entity.Role;
import com.thanhtam.backend.repository.RoleRepository;
import com.thanhtam.backend.ultilities.ERole;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

import static org.junit.Assert.*;

@RunWith(MockitoJUnitRunner.class)
public class RoleServiceTest {

    @InjectMocks
    private RoleServiceImpl roleService;

    @Mock
    private RoleRepository roleRepository;

    private Role role;

    @Before
    public void setup() {
        role = new Role();
        role.setId(1L);
        role.setName(ERole.ROLE_ADMIN);
    }

    @Test
    @Transactional
    @Rollback
    public void testFindByName_Found() {
        // Arrange
        Mockito.when(roleRepository.findByName(ERole.ROLE_ADMIN)).thenReturn(Optional.of(role));

        // Act
        Optional<Role> result = roleService.findByName(ERole.ROLE_ADMIN);

        // Assert
        assertTrue(result.isPresent());
        assertEquals(ERole.ROLE_ADMIN, result.get().getName());
    }

    @Test
    @Transactional
    @Rollback
    public void testFindByName_NotFound() {
        // Arrange
        Mockito.when(roleRepository.findByName(ERole.ROLE_STUDENT)).thenReturn(Optional.empty());

        // Act
        Optional<Role> result = roleService.findByName(ERole.ROLE_STUDENT);

        // Assert
        assertFalse(result.isPresent());
    }
}
