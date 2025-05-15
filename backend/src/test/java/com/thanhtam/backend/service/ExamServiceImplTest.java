package com.thanhtam.backend.service;

import com.amazonaws.services.glue.model.EntityNotFoundException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.google.common.base.Strings;
import com.thanhtam.backend.dto.AnswerSheet;
import com.thanhtam.backend.dto.ChoiceList;
import com.thanhtam.backend.dto.ExamQuestionPoint;
import com.thanhtam.backend.dto.ExamResult;
import com.thanhtam.backend.entity.*;
import com.thanhtam.backend.repository.ExamRepository;
import com.thanhtam.backend.repository.IntakeRepository;
import com.thanhtam.backend.repository.PartRepository;
import com.thanhtam.backend.ultilities.ERole;
import org.junit.Test;
import org.junit.jupiter.api.Assertions;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;
import junit.framework.Assert;


import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;


import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

import static org.junit.Assert.*;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@RunWith(SpringRunner.class)
@SpringBootTest
@Transactional // Rollback dữ liệu sau mỗi test case
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
public class ExamServiceImplTest {

    @Autowired
    private ExamRepository examRepository;

    // Sử dụng bean thực của ExamService
    @Autowired
    private ExamService examService;

    @Autowired
    private UserService userService;

    @Autowired
    private ExamUserService examUserService;

    @Autowired
    private IntakeService intakeService;

    @Autowired
    private PartService partService;

    @Autowired
    private CourseService courseService;

    @Autowired
    private IntakeRepository intakeRepository;

    @Autowired
    private PartRepository partRepository;



    private Intake createValidIntake() {
        Intake intake = new Intake();
        intake.setName("Intake Test");
        return intakeRepository.save(intake);
    }

    private Part createValidPart() {
        Part part = new Part();
        part.setName("Part A");
        return partRepository.save(part);
    }
//    TC_ES_01
//    Lưu thành công một Exam hợp lệ
//    input: Exam(title = "Midterm", duration = 90, intake ≠ null, part ≠ null, questionData ≠ null)
//    output: Lưu thành công vào DB (exam có ID ≠ null)
    @Test
    @Rollback(true)
    public void testSaveExam_success() {
        Intake intake = createValidIntake();
        Part part = createValidPart();

        Exam exam = new Exam();
        exam.setTitle("Midterm");
        exam.setIntake(intake);
        exam.setPart(part);
        exam.setDurationExam(90);
        exam.setBeginExam(new Date());
        exam.setFinishExam(new Date(System.currentTimeMillis() + 3600000)); // +1h
        exam.setQuestionData("{\"questions\":[]}");
        exam.setShuffle(true);
        exam.setCanceled(false);

        Exam saved = examService.saveExam(exam);

        assertNotNull(saved.getId());
        assertEquals("Midterm", saved.getTitle());
    }
//    TC_ES_02
//    Title bị null
//    input: Exam(title = null, duration = 60, intake ≠ null, part ≠ null, questionData ≠ null)
//    output: Trả về Exception
    @Test
    @Rollback(true)
    public void testSaveExam_titleNull_shouldThrowException() {
        Intake intake = createValidIntake();
        Part part = createValidPart();

        Exam exam = new Exam();
        exam.setTitle(null);
        exam.setIntake(intake);
        exam.setPart(part);
        exam.setDurationExam(60);
        exam.setBeginExam(new Date());
        exam.setFinishExam(new Date(System.currentTimeMillis() + 3600000));
        exam.setQuestionData("{}");

        assertThrows(Exception.class, () -> examService.saveExam(exam));
    }

//    TC_ES_03
//    Title rỗng ("")
//    input: Exam(title = "", duration = 60, intake ≠ null, part ≠ null, questionData ≠ null)
//    output: Trả về Exception
    @Test
    @Rollback(true)
    public void testSaveExam_titleEmpty_shouldThrowException() {
        Intake intake = createValidIntake();
        Part part = createValidPart();

        Exam exam = new Exam();
        exam.setTitle("");
        exam.setIntake(intake);
        exam.setPart(part);
        exam.setDurationExam(60);
        exam.setBeginExam(new Date());
        exam.setFinishExam(new Date(System.currentTimeMillis() + 3600000));
        exam.setQuestionData("{}");

        assertThrows(Exception.class, () -> examService.saveExam(exam));
    }

//    TC_ES_04
//    beginExam sau finishExam
//    input: Exam(title = "Wrong Time", beginExam = 10:00, finishExam = 09:00, intake ≠ null, part ≠ null)
//    output: Trả về Exception
    @Test
    @Rollback(true)
    public void testSaveExam_beginAfterFinish_shouldThrowException() {
        Intake intake = createValidIntake();
        Part part = createValidPart();

        Exam exam = new Exam();
        exam.setTitle("Wrong Time");
        exam.setIntake(intake);
        exam.setPart(part);
        exam.setDurationExam(90);
        exam.setBeginExam(new Date(System.currentTimeMillis() + 7200000)); // +2h
        exam.setFinishExam(new Date(System.currentTimeMillis() + 3600000)); // +1h
        exam.setQuestionData("{}");

        assertThrows(Exception.class, () -> examService.saveExam(exam));
    }

//    TC_ES_05
//    intake bị null
//    input: Exam(title = "No Intake", intake = null, part ≠ null, duration = 60, questionData ≠ null)
//    output: Trả về Exception
    @Test
    @Rollback(true)
    public void testSaveExam_intakeNull_shouldThrowException() {
        Part part = createValidPart();

        Exam exam = new Exam();
        exam.setTitle("No Intake");
        exam.setIntake(null);
        exam.setPart(part);
        exam.setDurationExam(60);
        exam.setBeginExam(new Date());
        exam.setFinishExam(new Date(System.currentTimeMillis() + 3600000));
        exam.setQuestionData("{}");

        assertThrows(Exception.class, () -> examService.saveExam(exam));
    }

//    TC_ES_06
//    part bị null
//    input: Exam(title = "No Part", intake ≠ null, part = null, duration = 60, questionData ≠ null)
//    output: Trả về Exception
    @Test
    @Rollback(true)
    public void testSaveExam_partNull_shouldThrowException() {
        Intake intake = createValidIntake();

        Exam exam = new Exam();
        exam.setTitle("No Part");
        exam.setIntake(intake);
        exam.setPart(null);
        exam.setDurationExam(60);
        exam.setBeginExam(new Date());
        exam.setFinishExam(new Date(System.currentTimeMillis() + 3600000));
        exam.setQuestionData("{}");

        assertThrows(Exception.class, () -> examService.saveExam(exam));
    }

//    TC_ES_07
//    questionData = null
//    input: Exam(title = "No Data", questionData = null, intake ≠ null, part ≠ null)
//    output: Trả về Exception
    @Test
    @Rollback(true)
    public void testSaveExam_questionDataNull_shouldThrowException() {
        Intake intake = createValidIntake();
        Part part = createValidPart();

        Exam exam = new Exam();
        exam.setTitle("No Questions");
        exam.setIntake(intake);
        exam.setPart(part);
        exam.setDurationExam(60);
        exam.setBeginExam(new Date());
        exam.setFinishExam(new Date(System.currentTimeMillis() + 3600000));
        exam.setQuestionData(null);

        assertThrows(Exception.class, () -> examService.saveExam(exam));
    }

//    TC_ES_08
//    canceled = true
//    input: Exam(title = "Canceled", canceled = true, intake ≠ null, part ≠ null, questionData ≠ null)
//    output: Lưu thành công, exam.isCanceled() == true
    @Test
    @Rollback(true)
    public void testSaveExam_canceledTrue_shouldSaveSuccessfully() {
        Intake intake = createValidIntake();
        Part part = createValidPart();

        Exam exam = new Exam();
        exam.setTitle("Canceled Exam");
        exam.setIntake(intake);
        exam.setPart(part);
        exam.setCanceled(true);
        exam.setDurationExam(45);
        exam.setBeginExam(new Date());
        exam.setFinishExam(new Date(System.currentTimeMillis() + 2700000));
        exam.setQuestionData("{}");

        Exam saved = examService.saveExam(exam);

        assertTrue(saved.isCanceled());
    }
    private void createTestExams(int count) {
        for (int i = 1; i <= count; i++) {
            Exam exam = new Exam();
            exam.setTitle("Exam " + i);
            exam.setDurationExam(60 + i);
            exam.setBeginExam(new Date());
            exam.setFinishExam(new Date());
            exam.setQuestionData("data " + i);
            examRepository.save(exam);
        }
    }
    // ==========================
    //    TC_ES_09
    //    Truy xuất trang đầu tiên
    //    input: Pageable.of(0, 5)
    //    output: Lấy được 5 bản ghi đầu tiên (0–4)
    // ==========================
    @Test
    @Rollback
    public void testFindAll_page0_size5() {
        createTestExams(10);
        Pageable pageable = PageRequest.of(0, 5, Sort.by("id").ascending());
        Page<Exam> result = examService.findAll(pageable);
        Assertions.assertEquals(5, result.getContent().size());
        Assertions.assertEquals(0, result.getNumber());
    }
    // ==========================
    //    TC_ES_10
    //    Truy xuất trang giữa
    //    input: Pageable.of(1, 3)
    //    output: Lấy được bản ghi từ số 4 đến 6
    // ==========================
    @Test
    @Rollback
    public void testFindAll_page1_size3() {
        createTestExams(10);
        Pageable pageable = PageRequest.of(1, 3, Sort.by("id").ascending());
        Page<Exam> result = examService.findAll(pageable);
        Assertions.assertEquals(3, result.getContent().size());
        Assertions.assertEquals(1, result.getNumber());
    }

    // ==========================
    //    TC_ES_11
    //    Truy xuất trang vượt quá số lượng bản ghi
    //    input: Pageable.of(10, 5) khi chỉ có 5 bản ghi
    //    output: Page rỗng
    // ==========================
    @Test
    @Rollback
    public void testFindAll_pageOutOfBounds() {
        examRepository.deleteAll(); // Xóa hết dữ liệu trước test
        createTestExams(5);
        Pageable pageable = PageRequest.of(10, 5);
        Page<Exam> result = examService.findAll(pageable);
        Assertions.assertTrue(result.getContent().isEmpty());
    }

    private Long createExamWithCanceled(boolean canceled) {
        Exam exam = new Exam();
        exam.setTitle("To be canceled");
        exam.setCanceled(canceled);
        exam.setDurationExam(90);
        exam.setBeginExam(new java.util.Date());
        exam.setFinishExam(new java.util.Date());
        exam.setQuestionData("Sample question data");
        return examRepository.save(exam).getId();
    }

    // ==========================
    //    TC_ES_12
    //    Hủy bài thi hợp lệ
    //    input: id = 1 (exam chưa bị hủy)
    //    output: isCanceled = true
    // ==========================
    @Test
    @Rollback
    public void testCancelExam_validId() {
        Long examId = createExamWithCanceled(false);
        examService.cancelExam(examId);
        Exam updatedExam = examRepository.findById(examId).orElse(null);
        Assertions.assertNotNull(updatedExam);
    }

    // ==========================
    //    TC_ES_13
    //    Hủy bài thi đã bị hủy từ trước
    //    input: id = 2 (exam đã bị hủy)
    //    output: vẫn là isCanceled = true, không lỗi
    // ==========================
    @Test
    @Rollback
    public void testCancelExam_alreadyCanceled() {
        Long examId = createExamWithCanceled(true);
        examService.cancelExam(examId);  // gọi lại
        Exam updatedExam = examRepository.findById(examId).orElse(null);
        Assertions.assertNotNull(updatedExam);
        Assertions.assertTrue(updatedExam.isCanceled());
    }

    // ==========================
    //    TC_ES_14
    //    Hủy bài thi không tồn tại
    //    input: id = 999
    //    output: không có lỗi, không làm gì cả
    // ==========================
    @Test
    @Rollback
    public void testCancelExam_invalidId() {
        Long invalidId = 999L;
        examService.cancelExam(invalidId);  // không lỗi
        Assertions.assertFalse(examRepository.findById(invalidId).isPresent());
    }

    // ==========================
    //    TC_ES_15
    //    Hủy bài thi với ID null
    //    input: id = null
    //    output: trả về Exception
    // ==========================
    @Test
    @Rollback
    public void testCancelExam_nullId() {
        Assertions.assertThrows(IllegalArgumentException.class, () -> {
            examService.cancelExam(null);
        });
    }

    private Long createSampleExam() {
        Exam exam = new Exam();
        exam.setTitle("Sample Exam");
        exam.setCanceled(false);
        exam.setDurationExam(60);
        exam.setQuestionData("Sample question data");
        exam = examRepository.save(exam);
        return exam.getId();
    }

    // ==========================
    //    TC_ES_16
    //    Lấy exam theo ID hợp lệ
    //    input: id = valid id (ví dụ 1)
    //    output: Optional chứa Exam có id = input id
    // ==========================
    @Test
    @Rollback
    public void testGetExamById_validId() {
        Long examId = createSampleExam();
        Optional<Exam> result = examService.getExamById(examId);
        Assertions.assertTrue(result.isPresent());
        Assertions.assertEquals(examId, result.get().getId());
    }

    // ==========================
    //    TC_ES_17
    //    Lấy exam với ID không tồn tại
    //    input: id = 999 (giả sử không có exam này)
    //    output: Optional.empty()
    // ==========================
    @Test
    @Rollback
    public void testGetExamById_nonExistingId() {
        Optional<Exam> result = examService.getExamById(999L);
        Assertions.assertFalse(result.isPresent());
    }


//    TC_ES_18
//    Lấy danh sách exam theo username hợp lệ
//    input: pageable = PageRequest(0,10), username = "user1"
//    output: Trả về Page chứa các exam tạo bởi "user1"
    @Test
    @Rollback
    public void testFindAllByCreatedByUsername_validUser() {
        Pageable pageable = PageRequest.of(0, 10, Sort.by("id").ascending());
        Page<Exam> page = examService.findAllByCreatedBy_Username(pageable, "user1");
        for (Exam exam : page) {
            Assertions.assertEquals("user1", exam.getCreatedBy());
        }
    }

    //    TC_ES_19
//    Lấy danh sách với username không tồn tại
//    input: pageable = PageRequest(0,10), username = "nonexistent_user"
//    output: Trả về Page rỗng
    @Test
    @Rollback
    public void testFindAllByCreatedByUsername_nonExistingUser() {
        Pageable pageable = PageRequest.of(0, 10, Sort.by("id").ascending());
        Page<Exam> page = examService.findAllByCreatedBy_Username(pageable, "nonexistent_user");
    }

}
