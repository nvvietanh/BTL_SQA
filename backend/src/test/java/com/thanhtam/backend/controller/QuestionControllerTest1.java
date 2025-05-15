package com.thanhtam.backend.controller;

import com.thanhtam.backend.dto.PageResult;
import com.thanhtam.backend.entity.Part;
import com.thanhtam.backend.entity.Question;
import com.thanhtam.backend.entity.Role;
import com.thanhtam.backend.entity.User;
import com.thanhtam.backend.service.PartService;
import com.thanhtam.backend.service.QuestionService;
import com.thanhtam.backend.service.QuestionTypeService;
import com.thanhtam.backend.service.RoleService;
import com.thanhtam.backend.service.UserService;
import com.thanhtam.backend.ultilities.ERole;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.*;

import javax.transaction.Transactional;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Transactional
@Rollback
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
public class QuestionControllerTest1 {

    @Autowired
    private QuestionService questionService;
    @Autowired
    private PartService partService;
    @Autowired
    private QuestionTypeService questionTypeService;
//    @Autowired
    private UserService userService;
    @Autowired
    private RoleService roleService;
    private QuestionController questionController;

    private User adminUser;
    private User lecturerUser;
    private Role adminRole;
    private Role lecturerRole;
    private Part part;

    @Before
    public void setUp() {
//        questionService = Mockito.mock(QuestionService.class);
//        partService = Mockito.mock(PartService.class);
//        questionTypeService = Mockito.mock(QuestionTypeService.class);
        userService = Mockito.mock(UserService.class);
//        roleService = Mockito.mock(RoleService.class);

        questionController = new QuestionController(
                questionService, partService, questionTypeService, userService, roleService
        );

        // Setup roles
        adminRole = new Role();
        adminRole.setId(1L);
        adminRole.setName(ERole.ROLE_ADMIN);
        lecturerRole = new Role(3L, ERole.ROLE_LECTURER);
//        lecturerRole.setName(ERole.ROLE_LECTURER);

        // Setup users
        adminUser = new User();
        adminUser.setUsername("thanhtam28ss");
        adminUser.setRoles(new HashSet<>(Collections.singletonList(adminRole)));

        lecturerUser = new User();
        lecturerUser.setUsername("tamht298");
        lecturerUser.setRoles(new HashSet<>(Collections.singletonList(lecturerRole)));

        // Setup part
        part = new Part();
        part.setId(0L);
    }

    @Test
    public void testGetQuestionsByPart_Admin_AllParts() {
        // partId = 0, isAdmin = true
        Pageable pageable = PageRequest.of(0, 10);
        Page<Question> page = new PageImpl<>(Arrays.asList(new Question(), new Question()));

        Mockito.when(userService.getUserName()).thenReturn("thanhtam28ss");
        Mockito.when(userService.getUserByUsername("thanhtam28ss")).thenReturn(Optional.of(adminUser));
        Mockito.when(roleService.findByName(ERole.ROLE_ADMIN)).thenReturn(Optional.of(adminRole));
        Mockito.when(questionService.findAllQuestions(pageable)).thenReturn(page);

        PageResult result = questionController.getQuestionsByPart(pageable, 0L);
        Assert.assertNotNull(result);
        System.out.println(result.getData().getClass());
        Assert.assertTrue(result.getData() instanceof List);
        Assert.assertEquals(2, result.getData().size());
        
    }

    @Test
    public void testGetQuestionsByPart_Lecturer_AllParts() {
        // partId = 0, isAdmin = false
        Pageable pageable = PageRequest.of(0, 10);
        Page<Question> page = new PageImpl<>(Collections.singletonList(new Question()));

        Mockito.when(userService.getUserName()).thenReturn("lecturer");
        Mockito.when(userService.getUserByUsername("lecturer")).thenReturn(Optional.of(lecturerUser));
        Mockito.when(roleService.findByName(ERole.ROLE_ADMIN)).thenReturn(Optional.of(adminRole));
        Mockito.when(questionService.findQuestionsByCreatedBy_Username(pageable, "lecturer")).thenReturn(page);

        PageResult result = questionController.getQuestionsByPart(pageable, 0L);
        Assert.assertNotNull(result);
        Assert.assertTrue(result.getData() instanceof Page);
        Assert.assertEquals(1, ((Page<?>) result.getData()).getTotalElements());
    }

    @Test
    public void testGetQuestionsByPart_Admin_SpecificPart() {
        // partId != 0, isAdmin = true
        Pageable pageable = PageRequest.of(0, 10);
        Page<Question> page = new PageImpl<>(Arrays.asList(new Question(), new Question(), new Question()));

        Mockito.when(userService.getUserName()).thenReturn("admin");
        Mockito.when(userService.getUserByUsername("admin")).thenReturn(Optional.of(adminUser));
        Mockito.when(roleService.findByName(ERole.ROLE_ADMIN)).thenReturn(Optional.of(adminRole));
        Mockito.when(partService.findPartById(1L)).thenReturn(Optional.of(part));
        Mockito.when(questionService.findQuestionsByPart_IdAndCreatedBy_Username(pageable, 1L, "admin")).thenReturn(page);

        PageResult result = questionController.getQuestionsByPart(pageable, 1L);
        Assert.assertNotNull(result);
        Assert.assertTrue(result.getData() instanceof Page);
        Assert.assertEquals(3, ((Page<?>) result.getData()).getTotalElements());
    }

    @Test
    public void testGetQuestionsByPart_Lecturer_SpecificPart() {
        // partId != 0, isAdmin = false
        Pageable pageable = PageRequest.of(0, 10);
        Page<Question> page = new PageImpl<>(Collections.singletonList(new Question()));

        Mockito.when(userService.getUserName()).thenReturn("lecturer");
        Mockito.when(userService.getUserByUsername("lecturer")).thenReturn(Optional.of(lecturerUser));
        Mockito.when(roleService.findByName(ERole.ROLE_ADMIN)).thenReturn(Optional.of(adminRole));
        Mockito.when(questionService.findQuestionsByPart_IdAndCreatedBy_Username(pageable, 1L, "lecturer")).thenReturn(page);

        PageResult result = questionController.getQuestionsByPart(pageable, 1L);
        Assert.assertNotNull(result);
        Assert.assertTrue(result.getData() instanceof Page);
        Assert.assertEquals(1, ((Page<?>) result.getData()).getTotalElements());
    }

    @Test(expected = java.util.NoSuchElementException.class)
    public void testGetQuestionsByPart_Admin_SpecificPart_NotFound() {
        // partId != 0, isAdmin = true, part không tồn tại
        Pageable pageable = PageRequest.of(0, 10);

        Mockito.when(userService.getUserName()).thenReturn("admin");
        Mockito.when(userService.getUserByUsername("admin")).thenReturn(Optional.of(adminUser));
        Mockito.when(roleService.findByName(ERole.ROLE_ADMIN)).thenReturn(Optional.of(adminRole));
        Mockito.when(partService.findPartById(999L)).thenReturn(Optional.empty());

        questionController.getQuestionsByPart(pageable, 999L);
    }

    @Test
    public void testGetQuestionsByPart_Loop_MultipleParts() {
        // Lặp qua nhiều partId với admin
        Pageable pageable = PageRequest.of(0, 10);
        Page<Question> page = new PageImpl<>(Arrays.asList(new Question(), new Question()));

        Mockito.when(userService.getUserName()).thenReturn("admin");
        Mockito.when(userService.getUserByUsername("admin")).thenReturn(Optional.of(adminUser));
        Mockito.when(roleService.findByName(ERole.ROLE_ADMIN)).thenReturn(Optional.of(adminRole));
        Mockito.when(partService.findPartById(Mockito.anyLong())).thenReturn(Optional.of(part));
        Mockito.when(questionService.findQuestionsByPart_IdAndCreatedBy_Username(pageable, Mockito.anyLong(), Mockito.eq("admin"))).thenReturn(page);

        for (long partId = 1; partId <= 5; partId++) {
            PageResult result = questionController.getQuestionsByPart(pageable, partId);
            Assert.assertNotNull(result);
            Assert.assertTrue(result.getData() instanceof Page);
        }
    }
}