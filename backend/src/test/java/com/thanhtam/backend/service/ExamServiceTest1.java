package com.thanhtam.backend.service;

import com.thanhtam.backend.entity.Exam;
import com.thanhtam.backend.entity.Intake;
import com.thanhtam.backend.entity.Part;
import com.thanhtam.backend.entity.User;
import com.thanhtam.backend.repository.ExamRepository;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;
import junit.framework.Assert;

import java.util.Date;
import java.util.Optional;

@RunWith(SpringRunner.class)
@SpringBootTest
@Transactional // Rollback dữ liệu sau mỗi test case
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
public class ExamServiceTest1 {

    @Autowired
    private ExamRepository examRepository;

    // Sử dụng bean thực của ExamService
    @Autowired
    private ExamService examService;

    @Autowired
    private UserService userService;

    @Autowired
    private IntakeService intakeService;

    @Autowired
    private PartService partService;

    @Test
    public void testCreateExam_Success() {
        Assert.assertNotNull("ExamService bị null!", examService);
        Assert.assertNotNull("UserService bị null!", userService);
        Assert.assertNotNull("IntakeService bị null!", intakeService);
        Assert.assertNotNull("PartService bị null!", partService);

        // Lấy thông tin user "admin" từ DB (stub lại UserService)
        User user = new User();
        user.setId(1L);
        user.setUsername("thanhtam28ss");
//        org.mockito.Mockito.when(userService.getUserByUsername(user.getUsername()))
//                .thenReturn(Optional.of(user));

        Optional<User> foundUser = userService.getUserByUsername("thanhtam28ss");

        // Stub Intake
        Intake intake = new Intake();
        intake.setId(1L);
//        org.mockito.Mockito.when(intakeService.findById(1L))
//                .thenReturn(Optional.of(intake));

        Optional<Intake> foundIntake = intakeService.findById(new Long(1));


        // Stub Part
        Part part = new Part();
        part.setId(1L);
//        org.mockito.Mockito.when(partService.findPartById(1L))
//                .thenReturn(Optional.of(part));

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
        intake.setId(1L); // tồn tại trong CSDL
//        org.mockito.Mockito.when(intakeService.findById(intake.getId()))
//                .thenReturn(Optional.of(intake));
        Intake foundIntake = intakeService.findById(intake.getId()).orElse(null);


        // Stub PartService
        Part part = new Part();
        part.setId(1000L); // không tồn tại trong CSDL
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
        Assert.assertNull("Exam được lưu khi part = " + foundPart, savedExam);
        Assert.assertNull("Exam được lưu khi part = " + foundPart, savedExam.getId());
        // Lấy lại exam từ DB để kiểm tra
        Optional<Exam> examOpt = examRepository.findById(savedExam.getId());
        Assert.assertFalse("Exam được lưu trong khi part = " + foundPart, examOpt.isPresent());

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
//        org.mockito.Mockito.when(userService.getUserByUsername("thanhtam28ss"))
//                .thenReturn(Optional.of(user));
        User foundUser = userService.getUserByUsername("thanhtam28ss").orElse(null);

        // Stub IntakeService
        Intake intake = new Intake();
        intake.setId(1000L); // ID không tồn tại trong DB
//        org.mockito.Mockito.when(intakeService.findById(intake.getId()))
//                .thenReturn(Optional.of(intake));
        Intake foundIntake = intakeService.findById(intake.getId()).orElse(null);

        // Stub PartService
        Part part = new Part();
        part.setId(1L); // ID tồn tại trong DB
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
        Assert.assertNull("Exam được lưu trong khi intake = " + foundIntake, savedExam);
        Assert.assertNull("Exam được lưu trong khi intake = " + foundIntake, savedExam.getId());

    }
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
//        org.mockito.Mockito.when(userService.getUserByUsername("thanhtam28ss"))
//                .thenReturn(Optional.of(user));
        User foundUser = userService.getUserByUsername("thanhtam28ss").orElse(null);

        // Stub IntakeService
        Intake intake = new Intake();
        intake.setId(1000L); // ID không tồn tại trong DB
//        org.mockito.Mockito.when(intakeService.findById(intake.getId()))
//                .thenReturn(Optional.of(intake));
        Intake foundIntake = intakeService.findById(intake.getId()).orElse(null);

        // Stub PartService
        Part part = new Part();
        part.setId(1000L); // ID tồn tại trong DB
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
        Assert.assertNull("Exam được lưu trong khi intake = " + foundIntake + ", part = " + foundPart, savedExam);
        Assert.assertNull("Exam được lưu trong khi intake = " + foundIntake + ", part = " + foundPart, savedExam.getId());

    }
}
