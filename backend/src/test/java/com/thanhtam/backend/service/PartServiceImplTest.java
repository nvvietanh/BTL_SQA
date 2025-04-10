package com.thanhtam.backend.service;

import com.thanhtam.backend.entity.Course;
import com.thanhtam.backend.entity.Part;
import com.thanhtam.backend.repository.PartRepository;
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
import java.util.List;
import java.util.Optional;
@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Transactional
public class PartServiceImplTest {

    @Autowired
    private PartServiceImpl partService;

    @Autowired
    private PartRepository partRepository;

    @Autowired
    private CourseService courseService;
    // TC_PS_01
    @Test
    @Rollback
    public void testSavePart_Success() {
        Course newCourse = new Course();
        newCourse.setCourseCode("COURSE_SAVE_PART");
        newCourse.setName("Save Part Course");
        courseService.saveCourse(newCourse);

        Part newPart = new Part();
        newPart.setName("Saved Part");
        newPart.setCourse(newCourse);

        partService.savePart(newPart);

        Long id = newPart.getId();
        Assert.assertNotNull(id);

        Optional<Part> savedPart = partRepository.findById(id);
        Assert.assertTrue(savedPart.isPresent());
        Assert.assertEquals("Saved Part", savedPart.get().getName());
        Assert.assertEquals(newCourse.getId(), savedPart.get().getCourse().getId());
    }
//    TC_PS_02
    @Test
    @Rollback
    public void testGetPartListByCourse_WithPagination() {
        Course pagedCourse = new Course();
        pagedCourse.setCourseCode("COURSE_PAGINATION");
        pagedCourse.setName("Paged Course");
        courseService.saveCourse(pagedCourse);

        for (int i = 0; i < 3; i++) {
            Part part = new Part();
            part.setName("Paged Part " + i);
            part.setCourse(pagedCourse);
            partService.savePart(part);
        }

        Page<Part> partPage = partService.getPartLisByCourse(PageRequest.of(0, 2), pagedCourse.getId());
        Assert.assertNotNull(partPage);
        Assert.assertEquals(2, partPage.getContent().size());
    }
    // TC_PS_03
    @Test
    @Rollback
    public void testGetPartListByCourse() {
        Course listCourse = new Course();
        listCourse.setCourseCode("COURSE_LIST_PART");
        listCourse.setName("List Part Course");
        courseService.saveCourse(listCourse);
//
//        Part partA = new Part();
//        partA.setName("List Part A");
//        partA.setCourse(listCourse);
//        partService.savePart(partA);
//
//        Part partB = new Part();
//        partB.setName("List Part B");
//        partB.setCourse(listCourse);
//        partService.savePart(partB);

        List<Part> parts = partService.getPartListByCourse(listCourse);
        Assert.assertEquals(2, parts.size());
    }
    // TC_PS_04
    @Test
    @Rollback
    public void testFindPartById_Found() {
        Course findCourse = new Course();
        findCourse.setCourseCode("COURSE_FIND");
        findCourse.setName("Find Part Course");
//        courseService.saveCourse(findCourse);

        Part findPart = new Part();
        findPart.setName("Findable Part");
        findPart.setCourse(findCourse);
        partService.savePart(findPart);

        Optional<Part> found = partService.findPartById(findPart.getId());
        Assert.assertTrue(found.isPresent());
        Assert.assertEquals("Findable Part", found.get().getName());
    }
    // TC_PS_05
    @Test
    @Rollback
    public void testExistsById_ShouldReturnTrue() {
        Course existsCourse = new Course();
        existsCourse.setCourseCode("COURSE_EXISTS");
        existsCourse.setName("Exists Check Course");
        courseService.saveCourse(existsCourse);

        Part existsPart = new Part();
        existsPart.setName("Existing Part");
        existsPart.setCourse(existsCourse);
        partService.savePart(existsPart);

        boolean exists = partService.existsById(existsPart.getId());
        Assert.assertTrue(exists);
    }

//TC_PS_06
    @Test
    @Rollback
    public void testConstructorAssignment() {
        PartRepository mockRepo = org.mockito.Mockito.mock(PartRepository.class);
        PartServiceImpl mockService = new PartServiceImpl(mockRepo);
        Assert.assertNotNull(mockService);
    }

}
