package com.thanhtam.backend.controller;

import com.thanhtam.backend.dto.PageResult;
import com.thanhtam.backend.entity.Course;
import com.thanhtam.backend.entity.Part;
import com.thanhtam.backend.service.CourseService;
import com.thanhtam.backend.service.PartService;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.InvalidDataAccessApiUsageException;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityNotFoundException;
import javax.validation.ConstraintViolationException;
import java.util.Collections;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Transactional
@Rollback
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
public class PartControllerTest {

    @Autowired
    private PartService partService;

    @Autowired
    private CourseService courseService;

    private PartController partController;

    @Before
    public void setUp() {
        partController = new PartController(partService, courseService);
    }

    private void setUserAuthentication(String username, String role) {
        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                username,
                null,
                Collections.singletonList(new SimpleGrantedAuthority(role))
        );
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }

    // ==================== getPartListByCourse (paged) ====================

    // TC_PC_01: Lấy danh sách part theo course có phân trang
// Mục tiêu: Kiểm tra lấy danh sách part thành công theo course, có phân trang
// Input: Pageable(0,10), CourseID=3
// Output kỳ vọng: Danh sách List Part có phân trang với CourseID=3
    @Test
    public void testGetPartListByCoursePaged_Success() {
        Pageable pageable = PageRequest.of(0, 10);

        Optional<Course> courseOpt = courseService.getCourseById(3L);
        Assert.assertTrue("Course không tồn tại để test", courseOpt.isPresent());
        Long courseId = courseOpt.get().getId();

        setUserAuthentication("adminUser", "ROLE_ADMIN");
        PageResult result = partController.getPartListByCourse(pageable, courseId);

        Assert.assertNotNull("PageResult không được null", result);
        Assert.assertNotNull("Dữ liệu trong PageResult không được null", result.getData());
    }

    // TC_PC_02: Lấy danh sách part theo courseId không tồn tại (paged)
// Mục tiêu: Đảm bảo trả về danh sách rỗng khi courseId không tồn tại
// Input: Pageable(0,10), CourseID=999999 (không tồn tại)
// Output kỳ vọng: Danh sách rỗng, không ném exception
    @Test
    public void testGetPartListByCoursePaged_NonExistCourse_ReturnEmpty() {
        Pageable pageable = PageRequest.of(0, 10);
        Long invalidCourseId = 999999L;

        setUserAuthentication("adminUser", "ROLE_ADMIN");
        PageResult result = partController.getPartListByCourse(pageable, invalidCourseId);
        Assert.assertNotNull("PageResult không được null", result);
        Assert.assertTrue("Dữ liệu phải rỗng khi course không tồn tại",
                result.getData() == null || ((List<?>) result.getData()).isEmpty());
    }



// ==================== getPartListByCourse (list) ====================

    // TC_PC_03: Lấy danh sách part theo course (không phân trang)
// Mục tiêu: Kiểm tra lấy danh sách part thành công theo course
// Input: CourseID=3
// Output kỳ vọng: Danh sách Part không rỗng, đúng course
    @Test
    public void testGetPartListByCourse_Success() {
        Optional<Course> courseOpt = courseService.getCourseById(3L);
        Assert.assertTrue("Course không tồn tại để test", courseOpt.isPresent());
        Long courseId = courseOpt.get().getId();

        setUserAuthentication("adminUser", "ROLE_ADMIN");
        List<Part> parts = partController.getPartListByCourse(courseId);

        Assert.assertNotNull("Danh sách Part không được null", parts);
        Assert.assertFalse("Danh sách Part không được rỗng", parts.isEmpty());
    }

    // TC_PC_04: Lấy danh sách part theo courseId không tồn tại (list)
// Mục tiêu: Đảm bảo trả về danh sách rỗng khi courseId không tồn tại
// Input: CourseID=999999 (không tồn tại)
// Output kỳ vọng: Danh sách rỗng, không ném exception
    @Test
    public void testGetPartListByCourse_NonExistCourse_ReturnEmptyList() {
        Long invalidCourseId = 999999L;

        setUserAuthentication("adminUser", "ROLE_ADMIN");
        List<Part> parts = partController.getPartListByCourse(invalidCourseId);

        Assert.assertNotNull("Danh sách trả về không được null", parts);
        Assert.assertTrue("Danh sách phải rỗng khi course không tồn tại", parts.isEmpty());
    }

// ==================== getPartById ====================

    // TC_PC_05: Lấy part theo id thành công
// Mục tiêu: Lấy đúng part theo id hợp lệ
// Input: PartID hợp lệ
// Output kỳ vọng: Trả về Optional chứa Part đúng id
    @Test
    public void testGetPartById_Success() {
        Optional<Part> partOpt = partService.findPartById(1L);
        Assert.assertTrue("Part không tồn tại để test", partOpt.isPresent());
        Long partId = partOpt.get().getId();

        setUserAuthentication("adminUser", "ROLE_ADMIN");
        ResponseEntity<?> response = partController.getPartById(partId);

        Assert.assertEquals(200, response.getStatusCodeValue());
        Optional<Part> part = (Optional<Part>) response.getBody();
        Assert.assertTrue("Phải có Part trong body", part.isPresent());
        Assert.assertEquals(partId, part.get().getId());
    }

    // TC_PC_06: Lấy part theo id không tồn tại
// Mục tiêu: Đảm bảo trả về Optional.empty() khi part không tồn tại
// Input: PartID=999999 (không tồn tại)
// Output kỳ vọng:  ném exception  EntityNotFoundException
    @Test(expected = EntityNotFoundException.class)
    public void testGetPartById_NotFound_ShouldThrowException() {
        Long invalidPartId = 999999L;

        setUserAuthentication("adminUser", "ROLE_ADMIN");
        partController.getPartById(invalidPartId);
    }


    // TC_PC_07: Lấy part theo id là null
// Mục tiêu: Đảm bảo xử lý đúng khi id truyền vào là null (trả về lỗi hoặc exception phù hợp)
// Input: PartID = null
// Output kỳ vọng: Ném IllegalArgumentException hoặc MethodArgumentNotValidException hoặc tương tự
    @Test(expected = InvalidDataAccessApiUsageException.class) // hoặc exception phù hợp với controller của bạn
    public void testGetPartById_NullId_ShouldThrowException() {
        Long nullPartId = null;

        setUserAuthentication("adminUser", "ROLE_ADMIN");
        partController.getPartById(nullPartId);
    }
// ==================== updatePartName ====================

    // TC_PC_08: Cập nhật tên part thành công
// Mục tiêu: Đảm bảo cập nhật tên part thành công với dữ liệu hợp lệ
// Input: PartID hợp lệ, newName hợp lệ
// Output kỳ vọng: Trả về part với tên mới
    @Test
    public void testUpdatePartName_Success() {
        Optional<Part> partOpt = partService.findPartById(1L);
        Assert.assertTrue("Part không tồn tại để test", partOpt.isPresent());
        Long partId = partOpt.get().getId();

        String newName = "Updated Part Name";

        setUserAuthentication("adminUser", "ROLE_ADMIN");
        ResponseEntity<?> response = partController.updatePartName(partId, newName);

        Assert.assertEquals(200, response.getStatusCodeValue());
        Part part = (Part) response.getBody();
        Assert.assertEquals(newName, part.getName());
    }

    // TC_PC_09: Cập nhật tên part với tên rỗng
// Mục tiêu: Đảm bảo ném exception khi cập nhật tên part rỗng
// Input: PartID hợp lệ, newName = ""
// Output kỳ vọng: Ném ConstraintViolationException hoặc exception phù hợp
    @Test(expected = Exception.class)
    public void testUpdatePartName_EmptyName() {
        Optional<Part> partOpt = partService.findPartById(1L);
        Assert.assertTrue("Part không tồn tại để test", partOpt.isPresent());
        Long partId = partOpt.get().getId();

        String newName = ""; // Giả định có @Valid kiểm tra tên không rỗng

        setUserAuthentication("adminUser", "ROLE_ADMIN");
        partController.updatePartName(partId, newName);
    }
    // TC_PC_10: Cập nhật tên part với giá trị null
// Mục tiêu: Đảm bảo ném exception khi cập nhật tên part là null (theo ràng buộc @NotNull hoặc @Valid)
// Input: PartID hợp lệ, newName = null
// Output kỳ vọng: Ném ConstraintViolationException hoặc exception phù hợp
    @Test(expected = ConstraintViolationException.class)
    public void testUpdatePartName_NullName_ShouldThrowException() {
        Optional<Part> partOpt = partService.findPartById(1L);
        Assert.assertTrue("Part không tồn tại để test", partOpt.isPresent());
        Long partId = partOpt.get().getId();

        String newName = null; // Tên null

        setUserAuthentication("adminUser", "ROLE_ADMIN");
        partController.updatePartName(partId, newName);
    }


    // TC_PC_11: Tạo part mới theo course thành công
// Mục tiêu: Đảm bảo tạo part thành công với dữ liệu hợp lệ
// Input: CourseID hợp lệ, Part name hợp lệ
// Output kỳ vọng: Part mới được lưu vào DB
    @Test
    public void testCreatePartByCourse_Success() {
        Optional<Course> courseOpt = courseService.getCourseById(3L);
        Assert.assertTrue("Course không tồn tại để test", courseOpt.isPresent());
        Long courseId = courseOpt.get().getId();

        Part part = new Part();
        part.setName("New Part");

        setUserAuthentication("adminUser", "ROLE_ADMIN");
        partController.createPartByCourse(part, courseId);

        List<Part> parts = partService.getPartListByCourse(courseOpt.get());
        Assert.assertTrue(parts.stream().anyMatch(p -> "New Part".equals(p.getName())));
    }

    // TC_PC_12: Tạo part mới với tên rỗng
// Mục tiêu: Đảm bảo ném exception khi tạo part với tên rỗng
// Input: CourseID hợp lệ, Part name = ""
// Output kỳ vọng: Ném ConstraintViolationException
    @Test(expected = ConstraintViolationException.class)
    public void testCreatePartByCourse_EmptyName() {
        Optional<Course> courseOpt = courseService.getCourseById(3L);
        Assert.assertTrue("Course không tồn tại để test", courseOpt.isPresent());
        Long courseId = courseOpt.get().getId();

        Part part = new Part();
        part.setName(""); // Tên rỗng

        setUserAuthentication("adminUser", "ROLE_ADMIN");
        partController.createPartByCourse(part, courseId);
    }
    // TC_PC_12: Tạo part mới với tên rỗng
// Mục tiêu: Đảm bảo ném exception khi tạo part với tên rỗng
// Input: CourseID hợp lệ, Part name = ""
// Output kỳ vọng: Ném ConstraintViolationException
    @Test(expected = Exception.class)
    public void testCreatePartByCourse_NULLName() {
        Optional<Course> courseOpt = courseService.getCourseById(3L);
        Assert.assertTrue("Course không tồn tại để test", courseOpt.isPresent());
        Long courseId = courseOpt.get().getId();

        Part part = new Part();
        part.setName(null); // Tên rỗng

        setUserAuthentication("adminUser", "ROLE_ADMIN");
        partController.createPartByCourse(part, courseId);
    }

    // TC_PC_13: Tạo part mới với courseId không tồn tại
// Mục tiêu: Đảm bảo ném exception khi tạo part với courseId không tồn tại
// Input: CourseID=999999 (không tồn tại), Part name hợp lệ
// Output kỳ vọng: Ném EntityNotFoundException hoặc exception phù hợp
    @Test(expected = javax.persistence.EntityNotFoundException.class)
    public void testCreatePartByCourse_NonExistCourse_ShouldThrowException() {
        Long invalidCourseId = 999999L;
        Part part = new Part();
        part.setName("Fail Part");

        setUserAuthentication("adminUser", "ROLE_ADMIN");

        // Gọi hàm, mong đợi ném exception do course không tồn tại
        partController.createPartByCourse(part, invalidCourseId);
    }


}