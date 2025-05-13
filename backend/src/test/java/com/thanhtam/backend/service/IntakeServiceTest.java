package com.thanhtam.backend.service;

import com.thanhtam.backend.entity.Intake;
import com.thanhtam.backend.repository.IntakeRepository;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

import static org.junit.Assert.*;

@RunWith(MockitoJUnitRunner.class)
public class IntakeServiceTest {

    @InjectMocks
    private IntakeServiceImpl intakeService;

    @Mock
    private IntakeRepository intakeRepository;

    private Intake intake;

    @Before
    public void setup() {
        intake = new Intake();
        intake.setId(1L);
        intake.setIntakeCode("INTK001");
    }

    @Test
    @Transactional
    @Rollback
    public void testFindByCode_Found() {
        // Trường hợp: findByCode trả về kết quả
        Mockito.when(intakeRepository.findByIntakeCode("INTK001")).thenReturn(Optional.of(intake));

        Intake result = intakeService.findByCode("INTK001");

        assertNotNull(result);
        assertEquals("INTK001", result.getIntakeCode());
    }

    @Test(expected = NoSuchElementException.class)
    @Transactional
    @Rollback
    public void testFindByCode_NotFound() {
        // Trường hợp: findByCode không tìm thấy -> gọi .get() sẽ throw NoSuchElementException
        Mockito.when(intakeRepository.findByIntakeCode("NOT_FOUND")).thenReturn(Optional.empty());

        intakeService.findByCode("NOT_FOUND"); // expect throw exception
    }

    @Test
    @Transactional
    @Rollback
    public void testFindById_Found() {
        // Trường hợp: findById trả về Optional có giá trị
        Mockito.when(intakeRepository.findById(1L)).thenReturn(Optional.of(intake));

        Optional<Intake> result = intakeService.findById(1L);

        assertTrue(result.isPresent());
        assertEquals(Long.valueOf(1L), result.get().getId());
    }

    @Test
    @Transactional
    @Rollback
    public void testFindById_NotFound() {
        // Trường hợp: findById trả về Optional.empty
        Mockito.when(intakeRepository.findById(999L)).thenReturn(Optional.empty());

        Optional<Intake> result = intakeService.findById(999L);

        assertFalse(result.isPresent());
    }

    @Test
    @Transactional
    @Rollback
    public void testFindAll() {
        // Trường hợp: findAll trả về list có phần tử
        List<Intake> intakeList = Arrays.asList(intake);
        Mockito.when(intakeRepository.findAll()).thenReturn(intakeList);

        List<Intake> result = intakeService.findAll();

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("INTK001", result.get(0).getIntakeCode());
    }

    @Test
    @Transactional
    @Rollback
    public void testFindAll_Empty() {
        // Trường hợp: findAll trả về list rỗng
        Mockito.when(intakeRepository.findAll()).thenReturn(Collections.emptyList());

        List<Intake> result = intakeService.findAll();

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }
}

