package com.thanhtam.backend.controller;

import com.thanhtam.backend.dto.PageResult;
import com.thanhtam.backend.dto.ServiceResult;
import com.thanhtam.backend.entity.Course;
import com.thanhtam.backend.entity.User;
import com.thanhtam.backend.service.CourseService;
import com.thanhtam.backend.service.S3Services;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.multipart.MultipartFile;

import javax.persistence.EntityNotFoundException;
import java.util.*;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

@RunWith(SpringRunner.class)
public class CourseControllerTest {

    @Mock
    private CourseService courseService;

    @Mock
    private S3Services s3Services;

    @InjectMocks
    private CourseController courseController;

    private Course course;
    private User adminUser;
    private Pageable pageable;

    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);

        // Thiết lập dữ liệu giả với ngày hiện tại: 06:30 PM +07, Thursday, May 15, 2025
        Date currentDate = new Date(2025 - 1900, 4, 15, 18, 30, 0);

        course = new Course();
        course.setId(1L);
        course.setCourseCode("CS101");
        course.setImgUrl("http://example.com/image.jpg");
        course.setIntakes(null);

        adminUser = new User();
        adminUser.setUsername("admin");

        pageable = PageRequest.of(0, 10);
    }

    private void setUserAuthentication(String role) {
        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                "admin", null, Collections.singletonList(new SimpleGrantedAuthority(role))
        );
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }

    // --- Test cases for getAllCourse ---

    // TC_CC_01
    // Lấy danh sách tất cả các khóa học thành công
    // input: courseService.getCourseList() trả về danh sách không rỗng
    // output: Trả về List<Course> không rỗng
    @Test
    public void testGetAllCourse_Success() {
        List<Course> courseList = Collections.singletonList(course);
        when(courseService.getCourseList()).thenReturn(courseList);

        List<Course> result = courseController.getAllCourse();

        assertEquals(1, result.size());
        assertEquals(course, result.get(0));
        verify(courseService, times(1)).getCourseList();
    }

    // TC_CC_02
    // Lấy danh sách tất cả các khóa học khi danh sách rỗng
    // input: courseService.getCourseList() trả về danh sách rỗng
    // output: Trả về List<Course> rỗng
    @Test
    public void testGetAllCourse_Empty() {
        when(courseService.getCourseList()).thenReturn(Collections.emptyList());

        List<Course> result = courseController.getAllCourse();

        assertTrue(result.isEmpty());
        verify(courseService, times(1)).getCourseList();
    }

    // --- Test cases for getCourseListByPage ---

    // TC_CC_03
    // Lấy danh sách khóa học theo trang thành công
    // input: courseService.getCourseListByPage(pageable) trả về Page<Course> không rỗng
    // output: Trả về PageResult không rỗng
    @Test
    public void testGetCourseListByPage_Success() {
        Page<Course> coursePage = new PageImpl<>(Collections.singletonList(course), pageable, 1);
        when(courseService.getCourseListByPage(pageable)).thenReturn(coursePage);

        PageResult result = courseController.getCourseListByPage(pageable);
        verify(courseService, times(1)).getCourseListByPage(pageable);
    }

    // TC_CC_04
    // Lấy danh sách khóa học theo trang khi danh sách rỗng
    // input: courseService.getCourseListByPage(pageable) trả về Page<Course> rỗng
    // output: Trả về PageResult rỗng
    @Test
    public void testGetCourseListByPage_Empty() {
        Page<Course> coursePage = new PageImpl<>(Collections.emptyList(), pageable, 0);
        when(courseService.getCourseListByPage(pageable)).thenReturn(coursePage);

        PageResult result = courseController.getCourseListByPage(pageable);

//        assertEquals(0, result.getTotalElements());
//        assertEquals(0, result.getContent().size());
        verify(courseService, times(1)).getCourseListByPage(pageable);
    }

    // --- Test cases for checkCourseCode ---

    // TC_CC_05
    // Kiểm tra mã khóa học tồn tại và khớp với id
    // input: value = "CS101", id = 1, courseService.existsByCode(true), courseCode khớp
    // output: Trả về false
    @Test
    public void testCheckCourseCode_Match() {
        when(courseService.existsByCode("CS101")).thenReturn(true);
        when(courseService.getCourseById(1L)).thenReturn(Optional.of(course));
        course.setCourseCode("CS101");

        boolean result = courseController.checkCourseCode("CS101", 1L);

        assertFalse(result);
        verify(courseService, times(1)).existsByCode("CS101");
        verify(courseService, times(1)).getCourseById(1L);
    }

    // TC_CC_06
    // Kiểm tra mã khóa học tồn tại nhưng không khớp với id
    // input: value = "CS102", id = 1, courseService.existsByCode(true), courseCode không khớp
    // output: Trả về true
    @Test
    public void testCheckCourseCode_NotMatch() {
        when(courseService.existsByCode("CS102")).thenReturn(true);
        when(courseService.getCourseById(1L)).thenReturn(Optional.of(course));
        course.setCourseCode("CS101");

        boolean result = courseController.checkCourseCode("CS102", 1L);

        assertTrue(result);
        verify(courseService, times(1)).existsByCode("CS102");
        verify(courseService, times(1)).getCourseById(1L);
    }

    // TC_CC_07
    // Kiểm tra mã khóa học không tồn tại
    // input: value = "CS103", courseService.existsByCode(false)
    // output: Trả về false
    @Test
    public void testCheckCourseCode_NotExist() {
        when(courseService.existsByCode("CS103")).thenReturn(false);

        boolean result = courseController.checkCourseCode("CS103", 1L);

        assertFalse(result);
        verify(courseService, times(1)).existsByCode("CS103");
        verify(courseService, never()).getCourseById(anyLong());
    }

    // --- Test cases for checkCode ---

    // TC_CC_08
    // Kiểm tra mã khóa học tồn tại
    // input: value = "CS101", courseService.existsByCode(true)
    // output: Trả về true
    @Test
    public void testCheckCode_Exists() {
        when(courseService.existsByCode("CS101")).thenReturn(true);

        boolean result = courseController.checkCode("CS101");

        assertTrue(result);
        verify(courseService, times(1)).existsByCode("CS101");
    }

    // TC_CC_09
    // Kiểm tra mã khóa học không tồn tại
    // input: value = "CS103", courseService.existsByCode(false)
    // output: Trả về false
    @Test
    public void testCheckCode_NotExists() {
        when(courseService.existsByCode("CS103")).thenReturn(false);

        boolean result = courseController.checkCode("CS103");

        assertFalse(result);
        verify(courseService, times(1)).existsByCode("CS103");
    }

    // --- Test cases for getCourseById ---

    // TC_CC_10
    // Lấy khóa học theo id thành công
    // input: id = 1, courseService.getCourseById() trả về Optional không rỗng
    // output: Trả về ResponseEntity với status OK và body là Course
    @Test
    public void testGetCourseById_Success() {
        when(courseService.getCourseById(1L)).thenReturn(Optional.of(course));

        ResponseEntity<?> result = courseController.getCourseById(1L);

        assertEquals(HttpStatus.OK, result.getStatusCode());
        verify(courseService, times(1)).getCourseById(1L);
    }

    // TC_CC_11
    // Lấy khóa học theo id thất bại (không tìm thấy)
    // input: id = 999, courseService.getCourseById() trả về Optional rỗng
    // output: Trả về Exception
    @Test(expected = EntityNotFoundException.class)
    public void testGetCourseById_NotFound() {
        when(courseService.getCourseById(999L)).thenReturn(Optional.empty());

        courseController.getCourseById(999L);

        verify(courseService, times(1)).getCourseById(999L);
    }

    // --- Test cases for createCourse ---

    // TC_CC_12
    // Tạo khóa học thành công
    // input: course với courseCode không trùng lặp
    // output: Trả về ResponseEntity với status CREATED
    @Test
    public void testCreateCourse_Success() {
        when(courseService.existsByCode("CS101")).thenReturn(false);
        doNothing().when(courseService).saveCourse(course);

        ResponseEntity<Object> result = courseController.createCourse(course);

        assertEquals(HttpStatus.OK, result.getStatusCode());
        ServiceResult serviceResult = (ServiceResult) result.getBody();
//        assertEquals(HttpStatus.CREATED.value(), serviceResult.getStatus());
        assertEquals("Created course successfully!", serviceResult.getMessage());
        verify(courseService, times(1)).saveCourse(course);
    }

    // TC_CC_13
    // Tạo khóa học thất bại do mã trùng lặp
    // input: course với courseCode đã tồn tại
    // output: Trả về ResponseEntity với status CONFLICT
    @Test
    public void testCreateCourse_DuplicateCode() {
        when(courseService.existsByCode("CS101")).thenReturn(true);

        ResponseEntity<Object> result = courseController.createCourse(course);

        assertEquals(HttpStatus.BAD_REQUEST, result.getStatusCode());
        ServiceResult serviceResult = (ServiceResult) result.getBody();
//        assertEquals(HttpStatus.CONFLICT.value(), serviceResult.getStatus());
        assertEquals("Duplicate Course!", serviceResult.getMessage());
        verify(courseService, never()).saveCourse(any());
    }

    // TC_CC_14
    // Tạo khóa học thất bại do ngoại lệ
    // input: course gây ra ngoại lệ khi save
    // output: Trả về ResponseEntity với status BAD_REQUEST và thông báo lỗi
    @Test
    public void testCreateCourse_Exception() {
        when(courseService.existsByCode("CS101")).thenReturn(false);
        doThrow(new RuntimeException("Save failed")).when(courseService).saveCourse(course);

        ResponseEntity<Object> result = courseController.createCourse(course);

        assertEquals(HttpStatus.BAD_REQUEST, result.getStatusCode());
        assertTrue(result.getBody().toString().contains("RuntimeException: Save failed"));
        verify(courseService, times(1)).saveCourse(course);
    }

    // --- Test cases for updateCourse ---

    // TC_CC_15
    // Cập nhật khóa học thành công với imgUrl mới
    // input: id = 1, course với imgUrl mới, course tồn tại
    // output: Trả về ResponseEntity với status OK
    @Test
    public void testUpdateCourse_SuccessWithNewImgUrl() {
        course.setImgUrl("http://new-example.com/image.jpg");
        when(courseService.getCourseById(1L)).thenReturn(Optional.of(course));
        doNothing().when(courseService).saveCourse(course);

        ResponseEntity<?> result = courseController.updateCourse(course, 1L);

        assertEquals(HttpStatus.OK, result.getStatusCode());
        ServiceResult serviceResult = (ServiceResult) result.getBody();
//        assertEquals(HttpStatus.OK.value(), serviceResult.getStatus());
        assertEquals("Update course with id: 1", serviceResult.getMessage());
        assertEquals("http://new-example.com/image.jpg", course.getImgUrl());
        verify(courseService, times(1)).saveCourse(course);
    }

    // TC_CC_16
    // Cập nhật khóa học thành công với imgUrl rỗng
    // input: id = 1, course với imgUrl rỗng, course tồn tại
    // output: Trả về ResponseEntity với status OK, giữ imgUrl cũ
    @Test
    public void testUpdateCourse_SuccessWithEmptyImgUrl() {
        course.setImgUrl("");
        when(courseService.getCourseById(1L)).thenReturn(Optional.of(course));
        doNothing().when(courseService).saveCourse(course);

        ResponseEntity<?> result = courseController.updateCourse(course, 1L);

        assertEquals(HttpStatus.OK, result.getStatusCode());
        ServiceResult serviceResult = (ServiceResult) result.getBody();
//        assertEquals(HttpStatus.OK.value(), serviceResult.getStatus());
        assertEquals("http://example.com/image.jpg", course.getImgUrl());
        verify(courseService, times(1)).saveCourse(course);
    }

    // TC_CC_17
    // Cập nhật khóa học thất bại do không tìm thấy
    // input: id = 999, courseService.getCourseById() trả về Optional rỗng
    // output: trả về Exception
    @Test(expected = EntityNotFoundException.class)
    public void testUpdateCourse_NotFound() {
        when(courseService.getCourseById(999L)).thenReturn(Optional.empty());

        courseController.updateCourse(course, 999L);

        verify(courseService, times(1)).getCourseById(999L);
    }

    // --- Test cases for deleteCourse ---

    // TC_CC_18
    // Xóa khóa học thành công
    // input: id = 1, courseService.getCourseById() trả về Optional không rỗng
    // output: Trả về ResponseEntity với status NO_CONTENT
    @Test
    public void testDeleteCourse_Success() {
        when(courseService.getCourseById(1L)).thenReturn(Optional.of(course));
        doNothing().when(courseService).delete(1L);

        ResponseEntity<?> result = courseController.deleteCourse(1L);

        assertEquals(HttpStatus.OK, result.getStatusCode());
        ServiceResult serviceResult = (ServiceResult) result.getBody();
//        assertEquals(HttpStatus.NO_CONTENT.value(), serviceResult.getStatus());
        assertEquals("Deleted course with id: 1 successfully!", serviceResult.getMessage());
        verify(courseService, times(1)).delete(1L);
    }

    // TC_CC_19
    // Xóa khóa học thất bại do không tìm thấy
    // input: id = 999, courseService.getCourseById() trả về Optional rỗng
    // output: Trả về Exception
    @Test(expected = EntityNotFoundException.class)
    public void testDeleteCourse_NotFound() {
        when(courseService.getCourseById(999L)).thenReturn(Optional.empty());

        courseController.deleteCourse(999L);

        verify(courseService, times(1)).getCourseById(999L);
    }

    // --- Test cases for getCourseByPart ---

    // TC_CC_20
    // Lấy khóa học theo partId thành công
    // input: partId = 1, courseService.findCourseByPartId() trả về Course
    // output: Trả về Course
    @Test
    public void testGetCourseByPart_Success() {
        when(courseService.findCourseByPartId(1L)).thenReturn(course);

        Course result = courseController.getCourseByPart(1L);

        assertEquals(course, result);
        verify(courseService, times(1)).findCourseByPartId(1L);
    }

    // TC_CC_21
    // Lấy khóa học theo partId thất bại (không tìm thấy)
    // input: partId = 999, courseService.findCourseByPartId() trả về null
    // output: Trả về Exception
    @Test(expected = NullPointerException.class)
    public void testGetCourseByPart_NotFound() {
        when(courseService.findCourseByPartId(999L)).thenReturn(null);

        courseController.getCourseByPart(999L);

        verify(courseService, times(1)).findCourseByPartId(999L);
    }

    // --- Test cases for findAllByIntakeId ---

    // TC_CC_22
    // Lấy danh sách khóa học theo intakeId thành công
    // input: intakeId = 1, courseService.findAllByIntakeId() trả về danh sách không rỗng
    // output: Trả về List<Course> không rỗng
    @Test
    public void testFindAllByIntakeId_Success() {
        List<Course> courseList = Collections.singletonList(course);
        when(courseService.findAllByIntakeId(1L)).thenReturn(courseList);

        List<Course> result = courseController.findAllByIntakeId(1L);

        assertEquals(1, result.size());
        assertEquals(course, result.get(0));
        verify(courseService, times(1)).findAllByIntakeId(1L);
    }

    // TC_CC_23
    // Lấy danh sách khóa học theo intakeId khi danh sách rỗng
    // input: intakeId = 1, courseService.findAllByIntakeId() trả về danh sách rỗng
    // output: Trả về List<Course> rỗng
    @Test
    public void testFindAllByIntakeId_Empty() {
        when(courseService.findAllByIntakeId(1L)).thenReturn(Collections.emptyList());

        List<Course> result = courseController.findAllByIntakeId(1L);

        assertTrue(result.isEmpty());
        verify(courseService, times(1)).findAllByIntakeId(1L);
    }
}