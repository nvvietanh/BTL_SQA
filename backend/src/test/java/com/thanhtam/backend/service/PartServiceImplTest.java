package com.thanhtam.backend.service;

import com.thanhtam.backend.entity.Course;
import com.thanhtam.backend.entity.Part;
import com.thanhtam.backend.repository.PartRepository;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@RunWith(SpringRunner.class)
@SpringBootTest
//@Transactional
//@Rollback
public class PartServiceImplTest {

    @Autowired
    private PartService partService;

    @Autowired
    private PartRepository partRepository;

    private Course existingCourse;
    private Course nonExistingCourse;

    @Before
    public void setUp() {
        existingCourse = new Course();
        existingCourse.setId(1L); // Giả định course id=1 tồn tại trong DB thật

        nonExistingCourse = new Course();
        nonExistingCourse.setId(99999L); // Id không tồn tại
    }

    // TC_PS_01: Test lưu phần học: trường hợp thành công
    // Mục tiêu: Kiểm tra phương thức savePart lưu thành công phần học
    // INPUT: part(name:"Test Part Save",Course(id=1))
    // OUTPUT Kỳ vọng: lưu part vào db
    @Test
    @Transactional
    @Rollback
    public void testSavePart_Success() {
        Part part = new Part();
        part.setName("Test Part Save");
        part.setCourse(existingCourse);

        partService.savePart(part);

        Assert.assertNotNull("Part ID should not be null after save", part.getId());

        Optional<Part> found = partService.findPartById(part.getId());

        Assert.assertTrue("Expected part to be found after save", found.isPresent());
        Assert.assertEquals("Part name should match saved name", "Test Part Save", found.get().getName());
    }
    // TC_PS_02: Test lưu phần học: trường hợp fail do name bị Null
    // Mục tiêu: Kiểm tra phương thức savePart lưu ko thành công do trường name bị null
    // INPUT: part(name: null,Course(id=1))
    // OUTPUT Kỳ vọng: ném ra Exception ngoại lệ

    @Transactional
    @Rollback
    @Test(expected = Exception.class)
    public void testSavePart_Fail_NullName() {
        Part part = new Part();
        part.setName(null);
        part.setCourse(existingCourse);

        partService.savePart(part);
    }
    // TC_PS_03: Test lưu phần học: trường hợp fail do course bị Null
    // Mục tiêu: Kiểm tra phương thức savePart lưu ko thành công do trường course bị null
    // INPUT: part(name: "Fail Null Course",Course: null)
    // OUTPUT Kỳ vọng: ném ra Exception ngoại lệ

    @Transactional
    @Rollback
    @Test(expected = Exception.class)
    public void testSavePart_Fail_NullCourse() {
        Part part = new Part();
        part.setName("Fail Null Course");
        part.setCourse(null);

        partService.savePart(part);
    }
    // TC_PS_04: Test lưu phần học: trường hợp fail do course bị Null, name bị null
    // Mục tiêu: Kiểm tra phương thức savePart lưu ko thành công do trường course bị null, name bị null
    // INPUT: part(name: null,Course: null)
    // OUTPUT Kỳ vọng: ném ra Exception ngoại lệ

    @Transactional
    @Rollback
    @Test(expected = Exception.class)
    public void testSavePart_Fail_NullCourseNullName() {
        Part part = new Part();
        part.setName(null);
        part.setCourse(null);
        partService.savePart(part);
    }

    // TC_PS_05: Test lưu phần học: trường hợp fail do name rỗng
    // Mục tiêu: Kiểm tra phương thức savePart lưu ko thành công do trường name rỗng
    // INPUT: part(name: "",Course(id=1))
    // OUTPUT Kỳ vọng: ném ra Exception ngoại lệ
    @Transactional
    @Rollback
    @Test(expected = Exception.class)
    public void testSavePart_Fail_EmptyName() {
        Part part = new Part();
        part.setName("");
        part.setCourse(existingCourse);

        partService.savePart(part);
    }

    // TC_PS_06: Test lưu phần học: trường hợp fail do name chỉ có khoảng trắng
    // Mục tiêu: Kiểm tra phương thức savePart lưu ko thành công do trường name chỉ có khoảng trắng
    // INPUT: part(name: "   ",Course(id=1))
    // OUTPUT Kỳ vọng: ném ra Exception ngoại lệ
    @Transactional
    @Rollback
    @Test(expected = Exception.class)
    public void testSavePart_Fail_BlankName() {
        Part part = new Part();
        part.setName("   ");
        part.setCourse(existingCourse);

        partService.savePart(part);
    }

    // TC_PS_07: Test lưu phần học: trường hợp fail do course ko tồn tại
    // Mục tiêu: Kiểm tra phương thức savePart lưu ko thành công do course ko tồn tại
    // INPUT: part(name: "Valid Name",Course(999999L))
    // OUTPUT Kỳ vọng: ném ra Exception ngoại lệ

    @Transactional
    @Rollback
    @Test(expected = Exception.class)
    public void testSavePart_Fail_CourseNotExist() {
        Course fakeCourse = new Course();
        fakeCourse.setId(999999L); // giả sử không tồn tại
        Part part = new Part();
        part.setName("Valid Name");
        part.setCourse(fakeCourse);

        partService.savePart(part);
    }

    // TC_PS_08: Test lấy danh sách phần học theo course có phân trang thành công
    // Mục tiêu: Kiểm tra phương thức getPartLisByCourse trả về trang dữ liệu phần học đúng courseId
    // INPUT: pageable(page=0, size=10), courseId =1
    // OUTPUT kỳ vọng: trả về Page<Part> không rỗng, tất cả phần học có courseId=1
    @Test
    public void testGetPartLisByCourse_Success() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Part> parts = partService.getPartLisByCourse(pageable, existingCourse.getId());

        Assert.assertNotNull("Page<Part> should not be null", parts);

        for (Part p : parts.getContent()) {
            Assert.assertNotNull("Part.course should not be null", p.getCourse());
            Assert.assertEquals("Part.course.id should match existingCourse id",
                    existingCourse.getId(), p.getCourse().getId());
        }
    }

    // TC_PS_09: Test lấy danh sách phần học theo course không tồn tại (phân trang)
    // Mục tiêu: Kiểm tra phương thức getPartLisByCourse trả về trang rỗng khi courseId không tồn tại
    // INPUT: pageable(page=0, size=10), courseId = 99999L
    // OUTPUT kỳ vọng: trả về Page<Part> rỗng
    @Test
    public void testGetPartLisByCourse_Fail_NonExistingCourse() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Part> parts = partService.getPartLisByCourse(pageable, nonExistingCourse.getId());

        Assert.assertNotNull("Page<Part> should not be null", parts);
        Assert.assertTrue("Expected empty page for non-existing course", parts.getContent().isEmpty());
    }

    // TC_PS_10: Test lấy danh sách phần học theo course không phân trang thành công
    // Mục tiêu: Kiểm tra phương thức getPartListByCourse trả về danh sách phần học đúng course
    // INPUT: course(id=1)
    // OUTPUT kỳ vọng: trả về List<Part> không rỗng, tất cả phần học thuộc đúng course(id=1)
    @Test
    public void testGetPartListByCourse_Success() {
        List<Part> parts = partService.getPartListByCourse(existingCourse);

        Assert.assertNotNull("List<Part> should not be null", parts);

        for (Part p : parts) {
            Assert.assertNotNull("Part.course should not be null", p.getCourse());
            Assert.assertEquals("Part.course.id should match existingCourse id",
                    existingCourse.getId(), p.getCourse().getId());
        }
    }

    // TC_PS_11: Test lấy danh sách phần học theo course không tồn tại (không phân trang)
    // Mục tiêu: Kiểm tra phương thức getPartListByCourse trả về danh sách rỗng khi course không tồn tại
    // INPUT: course(id=99999L)
    // OUTPUT kỳ vọng: trả về List<Part> rỗng
    @Test
    public void testGetPartListByCourse_Fail_NonExistingCourse() {
        List<Part> parts = partService.getPartListByCourse(nonExistingCourse);

        Assert.assertNotNull("List<Part> should not be null", parts);
        Assert.assertTrue("Expected empty list for non-existing course", parts.isEmpty());
    }
}
