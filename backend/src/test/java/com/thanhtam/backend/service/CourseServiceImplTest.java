package com.thanhtam.backend.service;

import com.amazonaws.services.organizations.model.ConstraintViolationException;
import com.thanhtam.backend.entity.Course;
import com.thanhtam.backend.entity.Part;
import com.thanhtam.backend.repository.CourseRepository;
import com.thanhtam.backend.repository.PartRepository;
import org.junit.Assert;
import org.junit.Test;
import org.junit.jupiter.api.Assertions;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

import javax.transaction.Transactional;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@RunWith(SpringRunner.class)
@SpringBootTest
@TestPropertySource(locations = "classpath:application.properties") // Hoặc application-test.properties
@Transactional
public class CourseServiceImplTest {
    @Autowired
    private PartRepository partRepository;

    @Autowired
    private CourseServiceImpl courseService;

    @Autowired
    private CourseRepository courseRepository;


//    TC_CS_01: Test lấy môn học theo ID - trường hợp có tồn tại
//    Mục tiêu: Kiểm tra phương thức getCourseById trả về Optional<Course> khi ID  tồn tại
//    Input: ID = 46L (tồn tại trong DB khi được thêm mới)
//    Output kỳ vọng: Optional<Course>
    @Test
    @Rollback(true) // Bắt buộc
    public void testGetCourseById_success() {
        try {
            // Tạo đối tượng Course với các thuộc tính
            Course course = new Course();
            course.setCourseCode("CS101");
            course.setName("Spring Boot");
            course.setImgUrl("http://example.com/springboot.png");

            // Lưu course vào DB
            Course saved = courseRepository.save(course);
            Long id = saved.getId();

            // Gọi hàm cần test
            Optional<Course> result = courseService.getCourseById(id);
            System.out.println(result.toString());
            // Kiểm tra kết quả
            Assertions.assertTrue(result.isPresent(), "Course phải tồn tại");
            Assertions.assertEquals("Spring Boot", result.get().getName(), "Tên khóa học không khớp");
            Assertions.assertEquals("CS101", result.get().getCourseCode(), "Mã khóa học không khớp");
            Assertions.assertEquals("http://example.com/springboot.png", result.get().getImgUrl(), "URL ảnh không khớp");

        } catch (Exception e) {
            e.printStackTrace();
            Assertions.fail("Lỗi xảy ra khi test: " + e.getMessage());
        }
        // Kiểm tra rollback
        System.out.println("Test completed, rollback should have occurred.");
    }


//    TC_CS_02: Test lấy môn học theo ID - trường hợp có ID không tồn tại
//    Mục tiêu: Kiểm tra phương thức getCourseById trả về Optional.empty() khi ID không tồn tại
//    Input: ID = 999999L (không tồn tại trong DB)
//    Output kỳ vọng: Optional.empty()
    @Test
    @Rollback(true)
    @Transactional
    public void testGetCourseById_notFound() {
        try {
            Long nonExistentId = 999999L;

            // Gọi hàm cần test
            Optional<Course> result = courseService.getCourseById(nonExistentId);

            // Kiểm tra kết quả
            Assertions.assertFalse(result.isPresent(), "Course không nên tồn tại với ID không có trong DB");

        } catch (Exception e) {
            e.printStackTrace();
            Assertions.fail("Lỗi xảy ra khi test: " + e.getMessage());
        }
    }

//    TC_CS_03: Test lấy danh sách môn học có trong database - trường hợp trong DB có dữ liệu
//    Mục tiêu: Kiểm tra phương thức GetCourseList trả về List<Course> khi DB có dữ liệu
//    Input: None
//    Output kỳ vọng: List<Course>
    @Test
    @Rollback(true)
    @Transactional
    public void testGetCourseList_success() {
        try {
            // Tạo và lưu 2 course mẫu
            Course course1 = new Course();
            course1.setCourseCode("CS101");
            course1.setName("Spring Boot");
            course1.setImgUrl("http://example.com/springboot.png");

            Course course2 = new Course();
            course2.setCourseCode("CS102");
            course2.setName("Java Core");
            course2.setImgUrl("http://example.com/java.png");

            courseRepository.save(course1);
            courseRepository.save(course2);

            // Gọi hàm cần test
            List<Course> courseList = courseService.getCourseList();

            // Kiểm tra kết quả
            Assertions.assertNotNull(courseList, "Danh sách không được null");
            Assertions.assertTrue(courseList.size() >= 2, "Phải có ít nhất 2 khóa học"); // >= vì có thể DB đã có sẵn dữ liệu

            // Optional: kiểm tra tồn tại tên cụ thể
            boolean containsSpringBoot = courseList.stream()
                    .anyMatch(c -> "Spring Boot".equals(c.getName()));
            boolean containsJavaCore = courseList.stream()
                    .anyMatch(c -> "Java Core".equals(c.getName()));

            Assertions.assertTrue(containsSpringBoot, "Không tìm thấy khóa học Spring Boot");
            Assertions.assertTrue(containsJavaCore, "Không tìm thấy khóa học Java Core");

        } catch (Exception e) {
            e.printStackTrace();
            Assertions.fail("Lỗi khi test getCourseList: " + e.getMessage());
        }
    }

//    TC_CS_04: Test lấy danh sách môn học có trong database - trường hợp trong DB không có dữ liệu
//    Mục tiêu: Kiểm tra phương thức GetCourseList trả về List<Course> = [] khi DB không có dữ liệu
//    Input: None
//    Output kỳ vọng: List<Course> = []
    @Test
    @Rollback(true)
    @Transactional
    public void testGetCourseList_empty() {
        try {
            // Xóa hết dữ liệu trước (đảm bảo DB trống)
            courseRepository.deleteAll();
            // Gọi hàm cần test
            List<Course> courseList = courseService.getCourseList();
            System.out.println(courseList.toString());
            // Kiểm tra kết quả
            Assertions.assertNotNull(courseList, "Danh sách không được null");
            Assertions.assertEquals(0, courseList.size(), "Danh sách phải rỗng");

        } catch (Exception e) {
            e.printStackTrace();
            Assertions.fail("Lỗi khi test getCourseList với DB rỗng: " + e.getMessage());
        }
    }

//    TC_CS_05: Test lấy danh sách môn học theo trang - trường hợp có dữ liệu
//    Mục tiêu: Kiểm tra phương thức getCourseListByPage trả về Page<Course>
//    Input: Pageable(0,3)
//    Output kỳ vọng: Page<Course>
    @Test
    @Rollback(true)
    @Transactional
    public void testGetCourseListByPage_success() {
        try {
            // Thêm 5 khóa học mẫu
            for (int i = 1; i <= 5; i++) {
                Course course = new Course();
                course.setCourseCode("CS10" + i);
                course.setName("Course " + i);
                course.setImgUrl("http://example.com/course" + i + ".png");
                courseRepository.save(course);
            }
            // Tạo pageable: page 0, size 3
            Pageable pageable = PageRequest.of(0, 3);

            // Gọi hàm cần test
            Page<Course> resultPage = courseService.getCourseListByPage(pageable);

            // Kiểm tra kết quả
            Assertions.assertNotNull(resultPage, "Page kết quả không được null");
            Assertions.assertEquals(3, resultPage.getContent().size(), "Phải trả về đúng số phần tử theo pageSize");
            Assertions.assertTrue(resultPage.getTotalElements() >= 5, "Tổng số phần tử phải >= 5");

        } catch (Exception e) {
            e.printStackTrace();
            Assertions.fail("Lỗi khi test getCourseListByPage: " + e.getMessage());
        }
    }

//    TC_CS_06: Test lấy danh sách môn học theo trang - không có dữ liệu
//    Mục tiêu: Kiểm tra phương thức getCourseListByPage trả về Page<Course> rỗng
//    Input: Pageable(0,5)
//    Output kỳ vọng: Page<Course> không có phần tử nào
    @Test
    @Rollback(true)
    @Transactional
    public void testGetCourseListByPage_empty() {
        try {
            // Xóa toàn bộ course trong DB
            courseRepository.deleteAll();

            // Tạo pageable: page 0, size 5
            Pageable pageable = PageRequest.of(0, 5);

            // Gọi hàm cần test
            Page<Course> resultPage = courseService.getCourseListByPage(pageable);

            // Kiểm tra
            Assertions.assertNotNull(resultPage, "Page không được null");
            Assertions.assertEquals(0, resultPage.getTotalElements(), "Không nên có phần tử nào");
            Assertions.assertTrue(resultPage.getContent().isEmpty(), "Danh sách phải rỗng");

        } catch (Exception e) {
            e.printStackTrace();
            Assertions.fail("Lỗi khi test getCourseListByPage với DB rỗng: " + e.getMessage());
        }
    }


//    TC_CS_07: Test lưu môn học - trường hợp lưu với đầy đủ thông tin, không bị trùng lặp
//    Mục tiêu: Kiểm tra phương thức SaveCourse có lưu thành công môn học không.
//    Input: Course(courseCode = "CS202", name ="Docker Basics", imgUrl = "http://example.com/docker.png"
//    Output kỳ vọng: môn học được lưu vào db
    @Test
    @Rollback(true)
    @Transactional
    public void testSaveCourse_success() {
        try {
            Course course = new Course();
            course.setCourseCode("CS202");
            course.setName("Docker Basics");
            course.setImgUrl("http://example.com/docker.png");

            courseService.saveCourse(course);

            // Truy xuất tất cả để kiểm tra (vì chưa có findByCourseCode)
            List<Course> allCourses = courseRepository.findAll();
            boolean exists = allCourses.stream()
                    .anyMatch(c -> "CS202".equals(c.getCourseCode())
                            && "Docker Basics".equals(c.getName())
                            && "http://example.com/docker.png".equals(c.getImgUrl()));

            Assertions.assertTrue(exists, "Course phải được lưu thành công");

        } catch (Exception e) {
            e.printStackTrace();
            Assertions.fail("Lỗi khi test saveCourse_success: " + e.getMessage());
        }
    }

//    TC_CS_08: Test lưu môn học - trường hợp courseCode để null
//    Mục tiêu: Kiểm tra phương thức SaveCourse có check trường hợp thiếu dữ liệu không
//    Input: Course(courseCode = null , name ="Docker Basics", imgUrl = "http://example.com/docker.png"
//    Output kỳ vọng: Trả về 1 ngoại lệ Exception
    @Test
    @Rollback(true)
    @Transactional
    public void testSaveCourse_invalidCourseCode_null() {
        Course course = new Course();
        course.setCourseCode(null);
        course.setName("Docker Basics");
        course.setImgUrl("http://example.com/docker.png");
        Assertions.assertThrows(Exception.class, () -> {
            courseService.saveCourse(course);

        });
    }

//    TC_CS_09: Test lưu môn học - trường hợp name để null
//    Mục tiêu: Kiểm tra phương thức SaveCourse có check trường hợp thiếu dữ liệu không
//    Input: Course(courseCode = "CS202" , name =null, imgUrl = "http://example.com/docker.png")
//    Output kỳ vọng: Trả về 1 ngoại lệ Exception
    @Test
    @Rollback(true)
    @Transactional
    public void testSaveCourse_invalidName_null() {
        Course course = new Course();
        course.setCourseCode("CS202");
        course.setName(null);
        course.setImgUrl("http://example.com/docker.png");

        Assertions.assertThrows(Exception.class, () -> {
            courseService.saveCourse(course);
        });
    }

//    TC_CS_09: Test lưu môn học - trường hợp name để null
//    Mục tiêu: Kiểm tra phương thức SaveCourse có check trường hợp thiếu dữ liệu không
//    Input: Course(courseCode = null , name ="Docker Basics", imgUrl = "http://example.com/docker.png"
//    Output kỳ vọng: Trả về 1 ngoại lệ Exception
    @Test
    public void testSaveCourse_invalidNameAndCode_null() {
        Course course = new Course();
        course.setCourseCode(null);
        course.setName(null);

        Assertions.assertThrows(Exception.class, () -> {
            courseService.saveCourse(course);
        });
    }

//    TC_CS_10: Test lưu môn học - trường hợp courseCode bị trùng
//    Mục tiêu: Kiểm tra phương thức SaveCourse có check trường hợp courseCode bị trùng không
//    Input: Course(courseCode = "CS204" , name ="Course A", imgUrl = "http://example.com/a.png"),
//            Course(courseCode = "CS204" , name ="Course B", imgUrl = "http://example.com/b.png",
//    Output kỳ vọng: Trả về 1 ngoại lệ Exception
    @Test
    @Rollback(true)
    @Transactional
    public void testSaveCourse_duplicateCourseCode_shouldFail() {
        try {
            Course course1 = new Course();
            course1.setCourseCode("CS204");
            course1.setName("Course A");
            course1.setImgUrl("http://example.com/a.png");

            courseService.saveCourse(course1);

            Course course2 = new Course();
            course2.setCourseCode("CS204"); // trùng
            course2.setName("Course B");
            course2.setImgUrl("http://example.com/b.png");

            courseService.saveCourse(course2);

            Assertions.fail("Phải ném lỗi vì trùng courseCode");

        } catch (Exception e) {
            Assertions.assertTrue(
                    e instanceof DataIntegrityViolationException
                            || e.getCause() instanceof ConstraintViolationException,
                    "Phải ném lỗi do vi phạm unique"
            );
        }
    }

//    TC_CS_11: Test thoát khi trùng sourceCode - trường hợp courseCode trùng với courseCode có trong database
//    Mục tiêu: Kiểm tra phương thức ExistsByCode có trả về True khi bị trùng không
//    Input: code = "CS301"
//    Output kỳ vọng: True
    @Test
    @Rollback(true)
    @Transactional
    public void testExistsByCode_true() {
        try {
            // Tạo một khóa học có courseCode cụ thể
            Course course = new Course();
            course.setCourseCode("CS301");
            course.setName("AI Fundamentals");
            course.setImgUrl("http://example.com/ai.png");

            courseRepository.save(course);

            // Gọi hàm existsByCode
            boolean exists = courseService.existsByCode("CS301");

            // Kiểm tra
            Assertions.assertTrue(exists, "Course phải tồn tại với mã CS301");

        } catch (Exception e) {
            e.printStackTrace();
            Assertions.fail("Lỗi khi test existsByCode_true: " + e.getMessage());
        }
    }
//    TC_CS_12: Test thoát khi trùng sourceCode - trường hợp courseCode không trùng với courseCode có trong database
//    Mục tiêu: Kiểm tra phương thức ExistsByCode có trả về False khi không  bị trùng không
//    Input: code = "NON_EXISTENT_CODE"
//    Output kỳ vọng: False
    @Test
    @Rollback(true)
    @Transactional
    public void testExistsByCode_false() {
        try {
            // Gọi hàm với mã không tồn tại
            boolean exists = courseService.existsByCode("NON_EXISTENT_CODE");

            // Kiểm tra
            Assertions.assertFalse(exists, "Course không nên tồn tại với mã này");

        } catch (Exception e) {
            e.printStackTrace();
            Assertions.fail("Lỗi khi test existsByCode_false: " + e.getMessage());
        }
    }
//    TC_CS_13: Test tìm Course bằng id của part - trường hợp partId hợp lệ, course chứa part
//    Mục tiêu: Kiểm tra phương thức findCourseByPartId có trả về đúng course chứa part không
//    Input: partId
//    Output kỳ vọng: tìm được course tương ứng
    @Test
    @Rollback(true)
    @Transactional
    public void testFindCourseByPartId_success() {
        try {
            // Tạo course
            Course course = new Course();
            course.setCourseCode("CS501");
            course.setName("Docker & Kubernetes");
            course.setImgUrl("http://example.com/docker.png");

            Course savedCourse = courseRepository.save(course);
            // Tạo part gắn với course
            Part part = new Part();
            part.setName("Intro to Docker");
            part.setCourse(savedCourse);  // khóa ngoại

            partRepository.save(part);
            Long partId = part.getId();

            // Gọi hàm cần test
            Course result = courseService.findCourseByPartId(partId);

            // Kiểm tra
            Assertions.assertNotNull(result, "Course không được null");
            Assertions.assertEquals("CS501", result.getCourseCode(), "Course code không đúng");
            Assertions.assertEquals(savedCourse.getId(), result.getId(), "Course ID không đúng");

        } catch (Exception e) {
            e.printStackTrace();
            Assertions.fail("Lỗi khi test findCourseByPartId_success: " + e.getMessage());
        }
    }

//    TC_CS_14: Test tìm Course bằng id của part - trường hợp partId không tồn tại
//    Mục tiêu: Kiểm tra phương thức findCourseByPartId không tìm được course khi partId không tồn tại
//    Input: partId = 99999L
//    Output kỳ vọng: trả về null
    @Test
    @Rollback(true)
    @Transactional
    public void testFindCourseByPartId_notFound() {
        try {
            // Gọi hàm với partId chưa tồn tại
            Course result = courseService.findCourseByPartId(99999L);

            // Kiểm tra kết quả
            Assertions.assertNull(result, "Phải trả về null khi partId không tồn tại");

        } catch (Exception e) {
            e.printStackTrace();
            Assertions.fail("Lỗi khi test findCourseByPartId_notFound: " + e.getMessage());
        }
    }





}