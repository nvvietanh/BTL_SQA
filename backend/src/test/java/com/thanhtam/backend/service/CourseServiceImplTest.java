package com.thanhtam.backend.service;

import com.thanhtam.backend.entity.Course;
import com.thanhtam.backend.repository.CourseRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class CourseServiceImplTest {

    @Mock
    private CourseRepository courseRepository;

    @InjectMocks
    private CourseServiceImpl courseService;

    @BeforeEach
    public void setUp() {
        // Khởi tạo các mock trước mỗi test
//        MockitoAnnotations.openMocks(this);
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void testGetCourseById_Success() {
        // Arrange
        Long courseId = 1L;
        Course course = new Course(courseId, "CS101", "Programming", "img_url", null);
        when(courseRepository.findById(courseId)).thenReturn(Optional.of(course));

        // Act
        Optional<Course> result = courseService.getCourseById(courseId);

        // Assert
        assertTrue(result.isPresent());
        assertEquals("CS101", result.get().getCourseCode());
        verify(courseRepository, times(1)).findById(courseId);
    }

    @Test
    public void testGetCourseById_NotFound() {
        // Arrange
        Long courseId = 1L;
        when(courseRepository.findById(courseId)).thenReturn(Optional.empty());

        // Act
        Optional<Course> result = courseService.getCourseById(courseId);

        // Assert
        assertFalse(result.isPresent());
        verify(courseRepository, times(1)).findById(courseId);
    }

    @Test
    public void testGetCourseList() {
        // Arrange
        List<Course> courses = Arrays.asList(
                new Course(1L, "CS101", "Programming", "img_url", null),
                new Course(2L, "CS102", "Database", "img_url", null)
        );
        when(courseRepository.findAll()).thenReturn(courses);

        // Act
        List<Course> result = courseService.getCourseList();

        // Assert
        assertEquals(2, result.size());
        assertEquals("CS101", result.get(0).getCourseCode());
        verify(courseRepository, times(1)).findAll();
    }

    @Test
    public void testGetCourseListByPage() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 10);
        List<Course> courses = Arrays.asList(new Course(1L, "CS101", "Programming", "img_url", null));
        Page<Course> page = new PageImpl<>(courses, pageable, courses.size());
        when(courseRepository.findAll(pageable)).thenReturn(page);

        // Act
        Page<Course> result = courseService.getCourseListByPage(pageable);

        // Assert
        assertEquals(1, result.getContent().size());
        assertEquals("CS101", result.getContent().get(0).getCourseCode());
        verify(courseRepository, times(1)).findAll(pageable);
    }

    @Test
    public void testSaveCourse() {
        // Arrange
        Course course = new Course(null, "CS101", "Programming", "img_url", null);
        Course savedCourse = new Course(1L, "CS101", "Programming", "img_url", null);
        when(courseRepository.save(course)).thenReturn(savedCourse);

        // Act
        courseService.saveCourse(course);

        // Assert
        verify(courseRepository, times(1)).save(course);
    }

    @Test
    public void testDelete() {
        // Arrange
        Long courseId = 1L;

        // Act
        courseService.delete(courseId);

        // Assert
        verify(courseRepository, times(1)).deleteById(courseId);
    }

    @Test
    public void testExistsByCode_True() {
        // Arrange
        String courseCode = "CS101";
        when(courseRepository.existsByCourseCode(courseCode)).thenReturn(true);

        // Act
        boolean result = courseService.existsByCode(courseCode);

        // Assert
        assertTrue(result);
        verify(courseRepository, times(1)).existsByCourseCode(courseCode);
    }

    @Test
    public void testExistsByCode_False() {
        // Arrange
        String courseCode = "CS101";
        when(courseRepository.existsByCourseCode(courseCode)).thenReturn(false);

        // Act
        boolean result = courseService.existsByCode(courseCode);

        // Assert
        assertFalse(result);
        verify(courseRepository, times(1)).existsByCourseCode(courseCode);
    }

    @Test
    public void testExistsById_True() {
        // Arrange
        Long courseId = 1L;
        when(courseRepository.existsById(courseId)).thenReturn(true);

        // Act
        boolean result = courseService.existsById(courseId);

        // Assert
        assertTrue(result);
        verify(courseRepository, times(1)).existsById(courseId);
    }

    @Test
    public void testFindAllByIntakeId() {
        // Arrange
        Long intakeId = 1L;
        List<Course> courses = Arrays.asList(new Course(1L, "CS101", "Programming", "img_url", null));
        when(courseRepository.findAllByIntakeId(intakeId)).thenReturn(courses);

        // Act
        List<Course> result = courseService.findAllByIntakeId(intakeId);

        // Assert
        assertEquals(1, result.size());
        assertEquals("CS101", result.get(0).getCourseCode());
        verify(courseRepository, times(1)).findAllByIntakeId(intakeId);
    }

    @Test
    public void testFindCourseByPartId() {
        // Arrange
        Long partId = 1L;
        Course course = new Course(1L, "CS101", "Programming", "img_url", null);
        when(courseRepository.findCourseByPartId(partId)).thenReturn(course);

        // Act
        Course result = courseService.findCourseByPartId(partId);

        // Assert
        assertNotNull(result);
        assertEquals("CS101", result.getCourseCode());
        verify(courseRepository, times(1)).findCourseByPartId(partId);
    }
}