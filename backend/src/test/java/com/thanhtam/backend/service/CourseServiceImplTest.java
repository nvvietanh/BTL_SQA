package com.thanhtam.backend.service;

import com.thanhtam.backend.entity.Course;
import com.thanhtam.backend.repository.CourseRepository;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.junit4.SpringRunner;

import javax.transaction.Transactional;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Transactional
public class CourseServiceImplTest {

    @Autowired
    private CourseServiceImpl courseService;

    @Autowired
    private CourseRepository courseRepository;

    private static Long createdCourseId;
    // Thực hiện tạo lưu 1 Course đúng
//    TC_CS_06
    @Test
    @Rollback
    public void testSaveCourse_Success() {
        Course course = new Course();
        course.setCourseCode("IMPL_TEST_01");
        course.setName("Impl Test");
        course.setImgUrl("http://example.com/img.png");
        course.setIntakes(Collections.emptyList());

        courseService.saveCourse(course);
        Long id = course.getId();
        Assert.assertNotNull(id);

        Optional<Course> opt = courseRepository.findById(id);
        Assert.assertTrue(opt.isPresent());
        createdCourseId = id;
    }
//    TC_CS_07
    @Test
    @Rollback
    public void testSaveCourse_DuplicateCode() {
        Course c1 = new Course();
        c1.setCourseCode("IMPL_DUP");
        c1.setName("Course 1");
        c1.setImgUrl("url1");
        c1.setIntakes(Collections.emptyList());
        courseService.saveCourse(c1);

        Course c2 = new Course();
        c2.setCourseCode("IMPL_DUP"); // trùng code
        c2.setName("Course 2");
        c2.setImgUrl("url2");
        c2.setIntakes(Collections.emptyList());

        try {
            courseService.saveCourse(c2);
            Assert.fail("Should throw exception for duplicate courseCode");
        } catch (RuntimeException e) {
            Assert.assertTrue(e.getMessage().toLowerCase().contains("already exists"));
        }
    }
//    TC_CS_04
    @Test
    public void testGetCourseList() {
        List<Course> list = courseService.getCourseList();
        Assert.assertNotNull(list);
    }
//    TC_CS_05
    @Test
    public void testGetCourseListByPage() {
        Page<Course> page = courseService.getCourseListByPage(PageRequest.of(0, 5));
        Assert.assertNotNull(page);
    }
//    TC_CS_02
    @Test
    public void testGetCourseById_Found() {
        Course course = new Course();
        course.setCourseCode("IMPL_FIND");
        course.setName("Find Me");
        course.setImgUrl("img");
        course.setIntakes(Collections.emptyList());
        courseService.saveCourse(course);

        Optional<Course> opt = courseService.getCourseById(course.getId());
        Assert.assertTrue(opt.isPresent());
    }
//    TC_CS_03
    @Test
    public void testGetCourseById_NotFound() {
        Optional<Course> opt = courseService.getCourseById(999999L);
        Assert.assertFalse(opt.isPresent());
    }

//TC_CS_08
    @Test
    @Rollback
    public void testDeleteCourse() {
        Course course = new Course();
        course.setCourseCode("IMPL_DELETE");
        course.setName("To Delete");
        course.setImgUrl("img");
        course.setIntakes(Collections.emptyList());
        courseService.saveCourse(course);
        Long id = course.getId();

        courseService.delete(id);

        Optional<Course> opt = courseService.getCourseById(id);
        Assert.assertFalse(opt.isPresent());
    }
// TC_CS_09
    @Test
    public void testExistsByCode() {
        Course course = new Course();
        course.setCourseCode("IMPL_EXISTS_CODE");
        course.setName("Check Exists");
        course.setImgUrl("img");
        course.setIntakes(Collections.emptyList());
//        courseService.saveCourse(course);

        boolean exists = courseService.existsByCode("IMPL_EXISTS_CODE");
        Assert.assertTrue(exists);
    }
//    TC_CS_10
    @Test
    public void testExistsById() {
        Course course = new Course();
        course.setCourseCode("IMPL_EXISTS_ID");
        course.setName("Check Exists");
        course.setImgUrl("img");
        course.setIntakes(Collections.emptyList());
        courseService.saveCourse(course);

        boolean exists = courseService.existsById(course.getId());
        Assert.assertTrue(exists);
    }
//    TC_CS_11
    @Test
    public void testFindAllByIntakeId() {
        List<Course> list = courseService.findAllByIntakeId(1L);
        Assert.assertNotNull(list);
    }
//    TC_CS_12
    @Test
    public void testFindCourseByPartId() {
        Course course = courseService.findCourseByPartId(1L);
        if (course != null) {
            Assert.assertNotNull(course.getId());
        }
    }
//    TC_CS_01
    @Test
    public void testConstructorAssignment() {
        CourseRepository mockRepo = org.mockito.Mockito.mock(CourseRepository.class);
        CourseServiceImpl service = new CourseServiceImpl(mockRepo);

        Assert.assertNotNull(service);
    }



}
