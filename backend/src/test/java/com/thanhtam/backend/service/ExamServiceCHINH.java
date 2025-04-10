package com.thanhtam.backend.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.google.common.base.Strings;
import com.thanhtam.backend.dto.AnswerSheet;
import com.thanhtam.backend.dto.ChoiceList;
import com.thanhtam.backend.dto.ExamQuestionPoint;
import com.thanhtam.backend.dto.ExamResult;
import com.thanhtam.backend.entity.*;
import com.thanhtam.backend.repository.ExamRepository;
import com.thanhtam.backend.ultilities.ERole;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;
import junit.framework.Assert;


import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;


import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

@RunWith(SpringRunner.class)
@SpringBootTest
//@Transactional // Rollback dữ liệu sau mỗi test case
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
public class ExamServiceCHINH {

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

    @Test

    // TS_ES_01
    public void testCreateExam_Success() {
        Assert.assertNotNull("ExamService bị null!", examService);
        Assert.assertNotNull("UserService bị null!", userService);
        Assert.assertNotNull("IntakeService bị null!", intakeService);
        Assert.assertNotNull("PartService bị null!", partService);

        // Lấy thông tin user "admin" từ DB (stub lại UserService)
        User user = new User();
        user.setId(1L);
        user.setUsername("thanhtam28ss");
        Optional<User> foundUser = userService.getUserByUsername("thanhtam28ss");

        // Stub Intake
        Intake intake = new Intake();
        intake.setId(1L);
        Optional<Intake> foundIntake = intakeService.findById(new Long(1));

        Part part = new Part();
        part.setId(1L);

        Optional<Part> foundPart = partService.findPartById(1L);

        // Tạo mới Exam
        Exam exam = new Exam();
        exam.setTitle("Bài kiểm tra Java");
        System.out.println(foundIntake);
        System.out.println(foundPart);
        System.out.println(foundUser);
        exam.setIntake(foundIntake.get());
        exam.setPart(foundPart.get());
        exam.setCreatedBy(foundUser.get());
        exam.setShuffle(true);
        exam.setCanceled(false);
        exam.setBeginExam(new Date());
        exam.setFinishExam(new Date());
        exam.setQuestionData("'[{\"questionId\":136,\"point\":5},{\"questionId\":137,\"point\":5}," +
                "{\"questionId\":145,\"point\":15},{\"questionId\":147,\"point\":10}," +
                "{\"questionId\":148,\"point\":10},{\"questionId\":149,\"point\":15}," +
                "{\"questionId\":150,\"point\":5},{\"questionId\":151,\"point\":5}," +
                "{\"questionId\":152,\"point\":10},{\"questionId\":153,\"point\":10}," +
                "{\"questionId\":154,\"point\":10}]'");

        // Lưu exam vào DB thông qua ExamService thật
        Exam savedExam = examService.saveExam(exam);
        Assert.assertNotNull("Exam sau khi lưu bị null!", savedExam);
        Assert.assertNotNull("Exam ID sau khi lưu bị null!", savedExam.getId());

        // Lấy lại exam từ DB để kiểm tra
        Optional<Exam> examOpt = examRepository.findById(savedExam.getId());
        Assert.assertTrue("Exam không được lưu vào DB!", examOpt.isPresent());

        Exam examFromDb = examOpt.get();
        Assert.assertEquals("Bài kiểm tra Java", examFromDb.getTitle());
        Assert.assertFalse(examFromDb.isCanceled());
        Assert.assertTrue(examFromDb.isShuffle());

        // Kiểm tra các đối tượng liên kết
        Assert.assertNotNull(examFromDb.getIntake());
        Assert.assertNotNull(examFromDb.getPart());
        Assert.assertNotNull(examFromDb.getCreatedBy());
    }

    // TC_ES_02
    @Test
    public void testCreateExam_Fail_IDMismatch_1() {
        Assert.assertNotNull("ExamService bị null!", examService);
        Assert.assertNotNull("UserService bị null!", userService);
        Assert.assertNotNull("IntakeService bị null!", intakeService);
        Assert.assertNotNull("PartService bị null!", partService);

        // Stub UserService để trả về user hợp lệ
        User user = new User();
        user.setId(1000L);
        user.setUsername("thanhtam28ss");
//        org.mockito.Mockito.when(userService.getUserByUsername("thanhtam28ss"))
//                .thenReturn(Optional.of(user));
        User foundUser = userService.getUserByUsername("thanhtam28ss").orElse(null);

        // Stub IntakeService
        Intake intake = new Intake();
        intake.setId(1L);
//        org.mockito.Mockito.when(intakeService.findById(intake.getId()))
//                .thenReturn(Optional.of(intake));
        Intake foundIntake = intakeService.findById(intake.getId()).orElse(null);


        // Stub PartService
        Part part = new Part();
        part.setId(1000L);
//        org.mockito.Mockito.when(partService.findPartById(part.getId()))
//                .thenReturn(Optional.of(part));
        Part foundPart = partService.findPartById(part.getId()).orElse(null);

        Exam exam = new Exam();
        exam.setTitle("Bài kiểm tra Java - Fail ID Test");
        System.out.println(foundIntake);
        System.out.println(foundPart);
        System.out.println(foundUser);
        exam.setIntake(foundIntake);
        exam.setPart(foundPart);
        exam.setCreatedBy(foundUser);
        exam.setShuffle(true);
        exam.setCanceled(false);
        exam.setBeginExam(new Date());
        exam.setFinishExam(new Date());
        exam.setQuestionData("'[{\"questionId\":136,\"point\":5},{\"questionId\":137,\"point\":5}," +
                "{\"questionId\":145,\"point\":15},{\"questionId\":147,\"point\":10}," +
                "{\"questionId\":148,\"point\":10},{\"questionId\":149,\"point\":15}," +
                "{\"questionId\":150,\"point\":5},{\"questionId\":151,\"point\":5}," +
                "{\"questionId\":152,\"point\":10},{\"questionId\":153,\"point\":10}," +
                "{\"questionId\":154,\"point\":10}]'");

        Exam savedExam = examService.saveExam(exam);
        Assert.assertNull("Exam sau khi lưu bị null!", savedExam);
        Assert.assertNull("Exam ID sau khi lưu bị null!", savedExam.getId());
        // Lấy lại exam từ DB để kiểm tra
        Optional<Exam> examOpt = examRepository.findById(savedExam.getId());
        Assert.assertTrue("Exam không được lưu vào DB!", examOpt.isPresent());

        Exam examFromDb = examOpt.get();
        Assert.assertEquals("Bài kiểm tra Java", examFromDb.getTitle());
        Assert.assertFalse(examFromDb.isCanceled());
        Assert.assertTrue(examFromDb.isShuffle());

        // Kiểm tra các đối tượng liên kết
        Assert.assertNotNull(examFromDb.getIntake());
        Assert.assertNotNull(examFromDb.getPart());
        Assert.assertNotNull(examFromDb.getCreatedBy());
    }
    @Test
    public void testCreateExam_Fail_IDMismatch_2() {
        Assert.assertNotNull("ExamService bị null!", examService);
        Assert.assertNotNull("UserService bị null!", userService);
        Assert.assertNotNull("IntakeService bị null!", intakeService);
        Assert.assertNotNull("PartService bị null!", partService);

        // Stub UserService để trả về user hợp lệ
        User user = new User();
        user.setId(1L);
        user.setUsername("thanhtam28ss");

        User foundUser = userService.getUserByUsername("thanhtam28ss").orElse(null);

        // Stub IntakeService
        Intake intake = new Intake();
        intake.setId(1000L); // ID tồn tại trong DB

        Intake foundIntake = intakeService.findById(intake.getId()).orElse(null);

        // Stub PartService
        Part part = new Part();
        part.setId(1L); // ID không tồn tại trong DB
        Part foundPart = partService.findPartById(part.getId()).orElse(null);

        Exam exam = new Exam();
        exam.setTitle("Bài kiểm tra Java - Fail ID Test");
        System.out.println(foundIntake);
        System.out.println(foundPart);
        System.out.println(foundUser);
        exam.setIntake(foundIntake);
        exam.setPart(foundPart);
        exam.setCreatedBy(foundUser);
        exam.setShuffle(true);
        exam.setCanceled(false);
        exam.setBeginExam(new Date());
        exam.setFinishExam(new Date());
        exam.setQuestionData("'[{\"questionId\":136,\"point\":5},{\"questionId\":137,\"point\":5}," +
                "{\"questionId\":145,\"point\":15},{\"questionId\":147,\"point\":10}," +
                "{\"questionId\":148,\"point\":10},{\"questionId\":149,\"point\":15}," +
                "{\"questionId\":150,\"point\":5},{\"questionId\":151,\"point\":5}," +
                "{\"questionId\":152,\"point\":10},{\"questionId\":153,\"point\":10}," +
                "{\"questionId\":154,\"point\":10}]'");

        Exam savedExam = examService.saveExam(exam);
        Assert.assertNull("Exam sau khi lưu bị null!", savedExam);
        Assert.assertNull("Exam ID sau khi lưu bị null!", savedExam.getId());

    }
    //TC_ES_04
    @Test
    public void testCreateExam_Fail_IDMismatch_3() {
        Assert.assertNotNull("ExamService bị null!", examService);
        Assert.assertNotNull("UserService bị null!", userService);
        Assert.assertNotNull("IntakeService bị null!", intakeService);
        Assert.assertNotNull("PartService bị null!", partService);

        // Stub UserService để trả về user hợp lệ
        User user = new User();
        user.setId(1L);
        user.setUsername("thanhtam28ss");
        User foundUser = userService.getUserByUsername("thanhtam28ss").orElse(null);

        // Stub IntakeService
        Intake intake = new Intake();
        intake.setId(1000L); // ID không tồn tại trong DB

        Intake foundIntake = intakeService.findById(intake.getId()).orElse(null);

        // Stub PartService
        Part part = new Part();
        part.setId(1000L); // ID tồn tại trong DB

        Part foundPart = partService.findPartById(part.getId()).orElse(null);

        Exam exam = new Exam();
        exam.setTitle("Bài kiểm tra Java - Fail ID Test");
        System.out.println(foundIntake);
        System.out.println(foundPart);
        System.out.println(foundUser);
        exam.setIntake(foundIntake);
        exam.setPart(foundPart);
        exam.setCreatedBy(foundUser);
        exam.setShuffle(true);
        exam.setCanceled(false);
        exam.setBeginExam(new Date());
        exam.setFinishExam(new Date());
        exam.setQuestionData("'[{\"questionId\":136,\"point\":5},{\"questionId\":137,\"point\":5}," +
                "{\"questionId\":145,\"point\":15},{\"questionId\":147,\"point\":10}," +
                "{\"questionId\":148,\"point\":10},{\"questionId\":149,\"point\":15}," +
                "{\"questionId\":150,\"point\":5},{\"questionId\":151,\"point\":5}," +
                "{\"questionId\":152,\"point\":10},{\"questionId\":153,\"point\":10}," +
                "{\"questionId\":154,\"point\":10}]'");

        Exam savedExam = examService.saveExam(exam);
        Assert.assertNull("Exam sau khi lưu bị null!", savedExam);
        Assert.assertNull("Exam ID sau khi lưu bị null!", savedExam.getId());

    }
    // TC_ES_05
    @Test
    @Transactional
    public void testGetExamsByPage_Admin() {
        // Tạo người dùng ADMIN
        User foundUser = userService.getUserByUsername("thanhtam28ss").orElse(null);
        Assert.assertTrue(
                "User ko phai admin " + foundUser.getRoles(),
                foundUser.getRoles().stream()
                        .anyMatch(role -> role.getName().equals(ERole.ROLE_ADMIN))
        );
        // Tạo Pageable
        Pageable pageable = PageRequest.of(0, 10, Sort.by("id"));

        // Gọi phương thức lấy bài thi cho ADMIN
        Page<Exam> exams = examService.findAll(pageable);

        // Kiểm tra kết quả
        Assert.assertNotNull("Danh sách bài thi bị null",exams);

    }


    @Test
    @Transactional
    public void testGetExamsByPage_USER() {
        // Tạo người dùng USER
        User foundUser = userService.getUserByUsername("1524801040049").orElse(null);
        Assert.assertNotNull("USER không tồn tại", foundUser);

        // Kiểm tra nếu KHÔNG phải ADMIN hoặc LECTURER thì không được phép truy cập
        boolean isAdminOrLecturer = foundUser.getRoles().stream()
                .anyMatch(role ->
                        role.getName().equals(ERole.ROLE_ADMIN) ||
                                role.getName().equals(ERole.ROLE_LECTURER)
                );

        Assert.assertTrue("User không phải ADMIN hoặc LECTURER: " + foundUser.getRoles(), isAdminOrLecturer);

        // Tạo Pageable
        Pageable pageable = PageRequest.of(0, 10, Sort.by("id"));

        // Gọi phương thức lấy bài thi cho user là LECTURER (không phải admin)
        Page<Exam> exams = examService.findAllByCreatedBy_Username(pageable, foundUser.getUsername());

        // Kiểm tra kết quả
        Assert.assertNotNull("Danh sách bài thi bị null", exams);
    }
    // TC_ES_06
    @Test
    @Transactional
    public void testCancelExam() {
        // Tìm 1 kỳ thi
        Optional<Exam> result = examService.getExamById(195L);
        Assert.assertTrue("Exam ko ton tai",result.isPresent());
        Exam exam = result.get();
        examService.cancelExam(exam.getId());

        Assert.assertNotNull("Exam bị null sau khi huỷ", exam);
        Assert.assertTrue("Exam chưa được huỷ", exam.isCanceled());
    }
//    TC_ES_07
    @Test
    @Transactional
    public void testGetExamById_ExamNotFound() {
        //ID không tồn tại trong cơ sở dữ liệu
        Long examId = 1000L;
        // Gọi phương thức getExamById và trả về Optional.empty() nếu không tìm thấy kỳ thi
        Optional<Exam> result = examService.getExamById(examId);

        // Kiểm tra kết quả
        Assert.assertTrue( "Kỳ thi không nên tồn tại với ID: " + examId,result.isPresent());
    }

    //    TC_ES_08
    @Test
    @Transactional
    public void testGetExamById_ExamFound() {

        // Giả lập ID tồn tại trong cơ sở dữ liệu
        Long examId = 200L;  // Đây là ID giả lập

        // Gọi phương thức getExamById và trả về Optional.empty() nếu không tìm thấy kỳ thi
        Optional<Exam> result = examService.getExamById(examId);

        // Kiểm tra kết quả
        Assert.assertTrue( "Kỳ thi không nên tồn tại với ID: " + examId,result.isPresent());
    }

    @Test
    @Transactional
    public void testGetChoiceList_WithRandomData() {
        Question question = new Question();
        question.setId(101L);
        question.setQuestionText("Dummy question?");

        Choice choice1 = new Choice(1L, "Option A", 0);
        Choice choice2 = new Choice(2L, "Option B", 1);

        AnswerSheet answerSheet = new AnswerSheet();
        answerSheet.setQuestionId(101L);
        answerSheet.setChoices(Arrays.asList(choice1, choice2));
        answerSheet.setPoint(1);

        // Tạo ExamQuestionPoint (thông tin câu hỏi thi)
        ExamQuestionPoint examQuestionPoint = new ExamQuestionPoint(101L, 1);

        // Gọi hàm cần test
        List<ChoiceList> result = examService.getChoiceList(
                Arrays.asList(answerSheet),
                Arrays.asList(examQuestionPoint)
        );

        // Kiểm tra kết quả trả về
        Assert.assertEquals(1, result.size());
        ChoiceList choiceList = result.get(0);

        Assert.assertEquals(Long.valueOf(101L), choiceList.getQuestion().getId());

        Assert.assertEquals(2, choiceList.getChoices().size());
        Assert.assertNotNull(choiceList.getIsSelectedCorrected());

        Assert.assertEquals(Optional.of(1), Optional.of(choiceList.getPoint()));
    }

    //TC_US_09
    @Test
    @Transactional
    public void testGetExamsByPage_Lecturer() {
        // Tạo người dùng LECTURER
        User foundUser = userService.getUserByUsername("tamht298").orElse(null);
        Assert.assertNotNull("Giang vien không tồn tại", foundUser);

        // Kiểm tra user phải có role LECTURER
        Assert.assertTrue(
                "User không phải lecturer " + foundUser.getRoles(),
                foundUser.getRoles().stream()
                        .anyMatch(role -> role.getName().equals(ERole.ROLE_LECTURER))
        );

        // Tạo Pageable
        Pageable pageable = PageRequest.of(0, 10, Sort.by("id"));

        // Gọi phương thức lấy bài thi cho LECTURER
        Page<Exam> exams = examService.findAllByCreatedBy_Username(pageable, foundUser.getUsername());

        // Kiểm tra kết quả
        Assert.assertNotNull("Danh sách bài thi bị null", exams);
    }

// TC_ES_10
    @Test
    @Transactional
    public void testCreateExamWithUsers() {
        // Lấy Exam
        Optional<Exam> result = examService.getExamById(200L);
        Assert.assertTrue("Exam không tồn tại!", result.isPresent());
        Exam exam = result.get();

        // Lấy danh sách User theo intake
        List<User> users = userService.findAllByIntakeId(exam.getIntake().getId());
        Assert.assertFalse("Không có user nào trong intake!", users.isEmpty());

        // Đếm số lượng ExamUser trước khi gán
        List<ExamUser> before = examUserService.findAllByExam_Id(exam.getId());
        int beforeCount = before.size();

        // Gọi hàm cần test
        examUserService.create(exam, users);

        // Đếm số lượng sau khi gán
        List<ExamUser> after = examUserService.findAllByExam_Id(exam.getId());
        int afterCount = after.size();

        // So sánh số lượng tăng đúng bằng số lượng users
        int diff = afterCount - beforeCount;
        Assert.assertEquals("Số lượng user được gán thêm không đúng!", users.size(), diff);

        // Kiểm tra từng user có được gán mới không (nếu cần)
        List<Long> expectedUserIds = users.stream().map(User::getId).collect(Collectors.toList());
        List<Long> actualUserIds = after.stream().map(eu -> eu.getUser().getId()).collect(Collectors.toList());

        for (Long id : expectedUserIds) {
            Assert.assertTrue("User ID " + id + " không được gán!", actualUserIds.contains(id));
        }
    }
 // TC_ES_11
    @Test
    @Transactional
    public void testGetExamListByUsername() {
        // Tạo dữ liệu cho User và Exam
        Optional<User> result = userService.getUserByUsername("1624801040051");
        Assert.assertTrue("User không tồn tại!", result.isPresent());
        User user = result.get();

        List<ExamUser> li = examUserService.getExamListByUsername(user.getUsername());
        Assert.assertNotNull("Danh Sach Bai Thi Rong",li);
        // Kiểm tra rằng danh sách các ExamUser chứa đúng thông tin Exam và User
        for (ExamUser examUser : li) {
            Assert.assertEquals("Username không đúng!", user.getUsername(), examUser.getUser().getUsername());
            Assert.assertNotNull("Exam không tồn tại!", examUser.getExam());
        }
    }
 // TC_ES_12
    @Test
    @Transactional
    public void testFindByExamAndUser() {
        // Tạo dữ liệu  cho Exam
        Optional<Exam> result = examService.getExamById(195L);
        Assert.assertTrue("Exam không tồn tại!", result.isPresent());
        Exam exam = result.get();

        Optional<User> result2 = userService.getUserByUsername("1624801040051");
        Assert.assertTrue("User không tồn tại!", result2.isPresent());
        User user = result2.get();

        // Gọi hàm cần test
        ExamUser foundExamUser = examUserService.findByExamAndUser(exam.getId(), user.getUsername());
        // Kiểm tra rằng ExamUser đã được tìm thấy và đúng với dữ liệu
        Assert.assertNotNull("ExamUser không tồn tại!", foundExamUser);
        Assert.assertEquals("Exam ID không đúng!", exam.getId(), foundExamUser.getExam().getId());
        Assert.assertEquals("Username không đúng!", user.getUsername(), foundExamUser.getUser().getUsername());
    }

    @Test
    @Transactional
    public void testFindByExamAndUser_FAIL1() {
        // Tạo dữ liệu cho Exam
        Optional<Exam> result = examService.getExamById(500L);
        Assert.assertTrue("Exam không tồn tại!", result.isPresent());
        Exam exam = result.get();

        Optional<User> result2 = userService.getUserByUsername("1624801040051");
        Assert.assertTrue("User không tồn tại!", result2.isPresent());
        User user = result2.get();

        // Gọi hàm cần test
        ExamUser foundExamUser = examUserService.findByExamAndUser(exam.getId(), user.getUsername());
        // Kiểm tra rằng ExamUser đã được tìm thấy và đúng với dữ liệu
        Assert.assertNotNull("ExamUser không tồn tại!", foundExamUser);
        Assert.assertEquals("Exam ID không đúng!", exam.getId(), foundExamUser.getExam().getId());
        Assert.assertEquals("Username không đúng!", user.getUsername(), foundExamUser.getUser().getUsername());
    }

    @Test
    @Transactional
    public void testFindByExamAndUser_FAIL2() {
        // Tạo dữ liệu cho Exam
        Optional<Exam> result = examService.getExamById(195L);
        Assert.assertTrue("Exam không tồn tại!", result.isPresent());
        Exam exam = result.get();

        Optional<User> result2 = userService.getUserByUsername("162480104005115125");
        Assert.assertTrue("User không tồn tại!", result2.isPresent());
        User user = result2.get();

        // Gọi hàm cần test
        ExamUser foundExamUser = examUserService.findByExamAndUser(exam.getId(), user.getUsername());
        // Kiểm tra rằng ExamUser đã được tìm thấy và đúng với dữ liệu
        Assert.assertNotNull("ExamUser không tồn tại!", foundExamUser);
        Assert.assertEquals("Exam ID không đúng!", exam.getId(), foundExamUser.getExam().getId());
        Assert.assertEquals("Username không đúng!", user.getUsername(), foundExamUser.getUser().getUsername());
    }
    @Test
    @Transactional
    public void testFindByExamAndUser_FAIL3() {
        // Tạo dữ liệu cho Exam
        Optional<Exam> result = examService.getExamById(195L);
        Assert.assertTrue("Exam không tồn tại!", result.isPresent());
        Exam exam = result.get();

        Optional<User> result2 = userService.getUserByUsername("dvcpro");
        Assert.assertTrue("User không tồn tại!", result2.isPresent());
        User user = result2.get();

        // Gọi hàm cần test
        ExamUser foundExamUser = examUserService.findByExamAndUser(exam.getId(), user.getUsername());
        // Kiểm tra rằng ExamUser đã được tìm thấy và đúng với dữ liệu
        Assert.assertNotNull("User ko tham gia ki thi nay!", foundExamUser);
        Assert.assertEquals("Exam ID không đúng!", exam.getId(), foundExamUser.getExam().getId());
        Assert.assertEquals("Username không đúng!", user.getUsername(), foundExamUser.getUser().getUsername());
    }
    // TC_ES_13
    @Test
    @Transactional
    public void testUpdateExamUser() {
        // Tạo dữ liệu cho Exam
        Optional<Exam> result = examService.getExamById(195L);
        Assert.assertTrue("Exam không tồn tại!", result.isPresent());
        Exam exam = result.get();

        Optional<User> result2 = userService.getUserByUsername("1624801040051");
        Assert.assertTrue("User không tồn tại!", result2.isPresent());
        User user = result2.get();

        // Gọi hàm cần test
        ExamUser foundExamUser = examUserService.findByExamAndUser(exam.getId(), user.getUsername());
        // Kiểm tra rằng ExamUser đã được tìm thấy và đúng với dữ liệu
        Assert.assertNotNull("ExamUser không tồn tại!", foundExamUser);
        Assert.assertEquals("Exam ID không đúng!", exam.getId(), foundExamUser.getExam().getId());
        Assert.assertEquals("Username không đúng!", user.getUsername(), foundExamUser.getUser().getUsername());

        foundExamUser.setIsFinished(true);
        examUserService.update(foundExamUser);
        Assert.assertTrue("Chua Cap nhat user da hoan thanh bai thi",foundExamUser.getIsFinished());
    }

 //TC_ES_14
    @Test
    @Transactional
    public void testGetCompleteExams() {
        // Tạo dữ liệu cho Course
        Optional<Course> result = courseService.getCourseById(12L);
        Assert.assertTrue("Course không tồn tại!", result.isPresent());
        Course course = result.get();

        // Tạo dữ liệu cho User
        Optional<User> result2 = userService.getUserByUsername("1624801040051");
        Assert.assertTrue("User không tồn tại!", result2.isPresent());
        User user = result2.get();

        // Gọi hàm cần test
        List<ExamUser> list = examUserService.getCompleteExams(course.getId(), user.getUsername());

        // Kiểm tra rằng danh sách không rỗng, tức là người dùng tham gia khóa học
        Assert.assertNotNull("User không tham gia Course này", list);
        Assert.assertFalse("Danh sách bài thi hoàn thành không thể rỗng!", list.isEmpty());

        // Kiểm tra rằng danh sách chỉ chứa các bài thi đã hoàn thành
        for (ExamUser examUser : list) {
            Assert.assertTrue("Bài thi chưa hoàn thành!", examUser.getIsFinished()); // Kiểm tra bài thi đã hoàn thành
            Assert.assertEquals("User không đúng!", user.getUsername(), examUser.getUser().getUsername());
        }
    }
    @Test
    @Transactional
    public void testGetCompleteExams_FAIL1() {
        // Tạo dữ liệu cho Course
        Optional<Course> result = courseService.getCourseById(1222L);
        Assert.assertTrue("Course không tồn tại!", result.isPresent());
        Course course = result.get();

        // Tạo dữ liệu cho User
        Optional<User> result2 = userService.getUserByUsername("1624801040051");
        Assert.assertTrue("User không tồn tại!", result2.isPresent());
        User user = result2.get();

        // Gọi hàm cần test
        List<ExamUser> list = examUserService.getCompleteExams(course.getId(), user.getUsername());

        // Kiểm tra rằng danh sách không rỗng, tức là người dùng tham gia khóa học
        Assert.assertNotNull("User không tham gia Course này", list);
        Assert.assertFalse("Danh sách bài thi hoàn thành không thể rỗng!", list.isEmpty());

        // Kiểm tra rằng danh sách chỉ chứa các bài thi đã hoàn thành
        for (ExamUser examUser : list) {
            Assert.assertTrue("Bài thi chưa hoàn thành!", examUser.getIsFinished()); // Kiểm tra bài thi đã hoàn thành
            Assert.assertEquals("User không đúng!", user.getUsername(), examUser.getUser().getUsername());
        }
    }

    @Test
    @Transactional
    public void testGetCompleteExams_FAIL2() {
        // Tạo dữ liệu cho Course
        Optional<Course> result = courseService.getCourseById(12L);
        Assert.assertTrue("Course không tồn tại!", result.isPresent());
        Course course = result.get();

        // Tạo dữ liệu cho User
        Optional<User> result2 = userService.getUserByUsername("1624801040051123");
        Assert.assertTrue("User không tồn tại!", result2.isPresent());
        User user = result2.get();

        // Gọi hàm cần test
        List<ExamUser> list = examUserService.getCompleteExams(course.getId(), user.getUsername());

        // Kiểm tra rằng danh sách không rỗng, tức là người dùng tham gia khóa học
        Assert.assertNotNull("User không tham gia Course này", list);
        Assert.assertFalse("Danh sách bài thi hoàn thành không thể rỗng!", list.isEmpty());

        // Kiểm tra rằng danh sách chỉ chứa các bài thi đã hoàn thành
        for (ExamUser examUser : list) {
            Assert.assertTrue("Bài thi chưa hoàn thành!", examUser.getIsFinished()); // Kiểm tra bài thi đã hoàn thành
            Assert.assertEquals("User không đúng!", user.getUsername(), examUser.getUser().getUsername());
        }
    }

    @Test
    @Transactional
    public void testGetCompleteExams_FAIL3() {
        // Tạo dữ liệu cho Course
        Optional<Course> result = courseService.getCourseById(12L);
        Assert.assertTrue("Course không tồn tại!", result.isPresent());
        Course course = result.get();

        // Tạo dữ liệu cho User
        Optional<User> result2 = userService.getUserByUsername("dvcpro");
        Assert.assertTrue("User không tồn tại!", result2.isPresent());
        User user = result2.get();

        // Gọi hàm cần test
        List<ExamUser> list = examUserService.getCompleteExams(course.getId(), user.getUsername());

        // Kiểm tra rằng danh sách không rỗng, tức là người dùng tham gia khóa học
        Assert.assertNotNull("User Chua Hoan Thanh Coure nay", list);
        Assert.assertFalse("Danh sách bài thi hoàn thành không thể rỗng!", list.isEmpty());

        // Kiểm tra rằng danh sách chỉ chứa các bài thi đã hoàn thành
        for (ExamUser examUser : list) {
            Assert.assertTrue("Bài thi chưa hoàn thành!", examUser.getIsFinished()); // Kiểm tra bài thi đã hoàn thành
            Assert.assertEquals("User không đúng!", user.getUsername(), examUser.getUser().getUsername());
        }
    }

     // TC_ES_15
    @Test
    @Transactional
    public void testFindAllByExam_Id() {
        // Tạo dữ liệu cho Exam
        Optional<Exam> result = examService.getExamById(200L);
        Assert.assertTrue("Exam không tồn tại!", result.isPresent());
        Exam exam = result.get();

        // Gọi hàm cần test
        List<ExamUser> examUsers = examUserService.findAllByExam_Id(exam.getId());
        // Kiểm tra danh sách không rỗng
        Assert.assertNotNull("Danh sách ExamUser không thể null!", examUsers);
        Assert.assertFalse("Danh sách ExamUser không thể rỗng!", examUsers.isEmpty());
        // Kiểm tra từng ExamUser trong danh sách
        for (ExamUser examUser : examUsers) {
            // Kiểm tra mỗi ExamUser có examId chính xác
            Assert.assertEquals("Exam ID không đúng!", exam.getId(), examUser.getExam().getId());
        }

    }
     // TC_ES_16
    @Test
    @Transactional
    public void testFindExamUsersByIsFinishedIsTrueAndExam_Id() {
        // Tạo dữ liệu cho Exam
        Optional<Exam> result = examService.getExamById(200L);
        Assert.assertTrue("Exam không tồn tại!", result.isPresent());
        Exam exam = result.get();

        // Gọi hàm cần test
        List<ExamUser> completedExamUsers = examUserService.findExamUsersByIsFinishedIsTrueAndExam_Id(exam.getId());

        // Kiểm tra rằng chỉ có ExamUser đã hoàn thành bài thi (isFinished = true) được trả về
        Assert.assertNotNull("Danh sách ExamUser không thể null!", completedExamUsers);
        Assert.assertFalse("Danh sách ExamUser không thể rỗng!", completedExamUsers.isEmpty());

        // Kiểm tra rằng tất cả ExamUser trong danh sách đều có isFinished = true
        for (ExamUser examUser : completedExamUsers) {
            Assert.assertTrue("ExamUser phải hoàn thành bài thi (isFinished = true)", examUser.getIsFinished());
            Assert.assertEquals("Exam ID không đúng!", exam.getId(), examUser.getExam().getId());
        }
    }

}
