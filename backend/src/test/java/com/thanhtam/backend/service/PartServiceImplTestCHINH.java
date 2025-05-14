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
import org.springframework.dao.DataIntegrityViolationException;
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
@Transactional
@Rollback
public class PartServiceImplTestCHINH {

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
    // TC_PS_01
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
    // TC_PS_02
    @Test(expected = Exception.class)
    public void testSavePart_Fail_NullName() {
        Part part = new Part();
        part.setName(null);
        part.setCourse(existingCourse);

        partService.savePart(part);
    }
    // TC_PS_03
    @Test(expected =  Exception.class)
    public void testSavePart_Fail_NullCourse() {
        Part part = new Part();
        part.setName("Fail Null Course");
        part.setCourse(null);

        partService.savePart(part);
    }
    // TC_PS_04
    @Test(expected =  Exception.class)
    public void testSavePart_Fail_NullCourseNullName() {
        Part part = new Part();
        part.setName(null);
        part.setCourse(null);
        partService.savePart(part);
    }
    // TC_PS_05
    @Test
    public void testGetPartLisByCourse_Success() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Part> parts = partService.getPartLisByCourse(pageable, existingCourse.getId());

        Assert.assertNotNull("Page<Part> should not be null", parts);

        // Kiểm tra tất cả phần tử trả về đều thuộc course có id đúng
        for (Part p : parts.getContent()) {
            Assert.assertNotNull("Part.course should not be null", p.getCourse());
            Assert.assertEquals("Part.course.id should match existingCourse id",
                    existingCourse.getId(), p.getCourse().getId());
        }
    }
    // TC_PS_06
    @Test
    public void testGetPartLisByCourse_Fail_NonExistingCourse() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Part> parts = partService.getPartLisByCourse(pageable, nonExistingCourse.getId());

        Assert.assertNotNull("Page<Part> should not be null", parts);
        Assert.assertTrue("Expected empty page for non-existing course", parts.getContent().isEmpty());
    }
    // TC_PS_07
    @Test
    public void testGetPartListByCourse_Success() {
        List<Part> parts = partService.getPartListByCourse(existingCourse);

        Assert.assertNotNull("List<Part> should not be null", parts);

        // Kiểm tra tất cả phần tử trả về đều thuộc course có id đúng
        for (Part p : parts) {
            Assert.assertNotNull("Part.course should not be null", p.getCourse());
            Assert.assertEquals("Part.course.id should match existingCourse id",
                    existingCourse.getId(), p.getCourse().getId());
        }
    }

    @Test
    public void testGetPartListByCourse_Fail_NonExistingCourse() {
        List<Part> parts = partService.getPartListByCourse(nonExistingCourse);

        Assert.assertNotNull("List<Part> should not be null", parts);
        Assert.assertTrue("Expected empty list for non-existing course", parts.isEmpty());
    }

    @Test
    public void testFindPartById_Success() {
        Part part = new Part();
        part.setName("FindById Success");
        part.setCourse(existingCourse);

        partService.savePart(part);

        Optional<Part> found = partService.findPartById(part.getId());

        Assert.assertTrue("Expected part to be found", found.isPresent());
        Assert.assertEquals("Part name should match", part.getName(), found.get().getName());

        Assert.assertNotNull("Part.course should not be null", found.get().getCourse());
        Assert.assertEquals("Part.course.id should match existingCourse id",
                existingCourse.getId(), found.get().getCourse().getId());
    }

    @Test
    public void testFindPartById_Fail_NotFound() {
        Optional<Part> found = partService.findPartById(999999L);

        Assert.assertFalse("Expected Optional.empty() for non-existing Part id", found.isPresent());
    }

    @Test
    public void testExistsById_Success() {
        Part part = new Part();
        part.setName("ExistsById Success");
        part.setCourse(existingCourse);

        partService.savePart(part);

        boolean exists = partService.existsById(part.getId());

        Assert.assertTrue("Expected existsById to return true", exists);
    }

    @Test
    public void testExistsById_Fail_NotFound() {
        boolean exists = partService.existsById(999999L);

        Assert.assertFalse("Expected existsById to return false", exists);
    }
}
