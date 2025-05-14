package com.thanhtam.backend.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.thanhtam.backend.dto.*;
import com.thanhtam.backend.entity.*;
import com.thanhtam.backend.service.ExamService;
import com.thanhtam.backend.service.ExamUserService;
import com.thanhtam.backend.service.IntakeService;
import com.thanhtam.backend.service.PartService;
import com.thanhtam.backend.service.UserService;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.http.*;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.Optional;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Transactional
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
public class ExamControllerTest {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ExamService examService;

    @Autowired
    private ExamUserService examUserService;

    @Autowired
    private IntakeService intakeService;

    @Autowired
    private PartService partService;

    @Autowired
    private UserService userService;

    private static String adminToken;
    private static String lecturerToken;
    private static String studentToken;

    private String getRootUrl() {
        return "http://localhost:" + port + "/api";
    }

    @Before
    public void setupToken() throws IOException {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Content-Type", "application/json");

        // Admin login
        String loginPayloadAdmin = "{ \"username\": \"thanhtam28ss\", \"password\": \"Abcd@12345\" }";
        HttpEntity<String> requestAdmin = new HttpEntity<>(loginPayloadAdmin, headers);
        ResponseEntity<String> responseAdmin = restTemplate.postForEntity(
                getRootUrl() + "/auth/signin", requestAdmin, String.class);
        if (responseAdmin.getStatusCodeValue() != 200) {
            throw new RuntimeException("Đăng nhập admin thất bại với mã trạng thái: " + responseAdmin.getStatusCodeValue());
        }
        adminToken = objectMapper.readTree(responseAdmin.getBody()).get("accessToken").asText();

        // Lecturer login
        String loginPayloadLecturer = "{ \"username\": \"tamht298\", \"password\": \"Abcd@12345\" }";
        HttpEntity<String> requestLecturer = new HttpEntity<>(loginPayloadLecturer, headers);
        ResponseEntity<String> responseLecturer = restTemplate.postForEntity(
                getRootUrl() + "/auth/signin", requestLecturer, String.class);
        if (responseLecturer.getStatusCodeValue() != 200) {
            throw new RuntimeException("Đăng nhập lecturer thất bại với mã trạng thái: " + responseLecturer.getStatusCodeValue());
        }
        lecturerToken = objectMapper.readTree(responseLecturer.getBody()).get("accessToken").asText();

        // Student login
        String loginPayloadStudent = "{ \"username\": \"nvvanh\", \"password\": \"Abcd@12345\" }";
        HttpEntity<String> requestStudent = new HttpEntity<>(loginPayloadStudent, headers);
        ResponseEntity<String> responseStudent = restTemplate.postForEntity(
                getRootUrl() + "/auth/signin", requestStudent, String.class);
        if (responseStudent.getStatusCodeValue() != 200) {
            throw new RuntimeException("Đăng nhập student thất bại với mã trạng thái: " + responseStudent.getStatusCodeValue());
        }
        studentToken = objectMapper.readTree(responseStudent.getBody()).get("accessToken").asText();

        System.out.println("Admin Token: " + adminToken);
        System.out.println("Lecturer Token: " + lecturerToken);
        System.out.println("Student Token: " + studentToken);
    }

    private String getAdminToken() {
        return adminToken;
    }

    private String getLecturerToken() {
        return lecturerToken;
    }

    private String getStudentToken() {
        return studentToken;
    }

    @Test
    public void testGetExamsByPage_Admin_Success() throws IOException {
        Assert.assertNotNull("ExamService bị null!", examService);

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + getAdminToken());
        HttpEntity<String> entity = new HttpEntity<>(headers);

        ResponseEntity<String> response = restTemplate.exchange(
                getRootUrl() + "/exams?page=0&size=10",
                HttpMethod.GET,
                entity,
                String.class
        );

        Assert.assertEquals("Expected OK status", 200, response.getStatusCodeValue());
        PageResult pageResult = objectMapper.readValue(response.getBody(), PageResult.class);
        Assert.assertNotNull("PageResult không được null!", pageResult);
    }

    @Test
    public void testGetExamsByPage_Lecturer_Success() throws IOException {
        Assert.assertNotNull("ExamService bị null!", examService);

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + getLecturerToken());
        HttpEntity<String> entity = new HttpEntity<>(headers);

        ResponseEntity<String> response = restTemplate.exchange(
                getRootUrl() + "/exams?page=0&size=10",
                HttpMethod.GET,
                entity,
                String.class
        );

        Assert.assertEquals("Expected OK status", 200, response.getStatusCodeValue());
        PageResult pageResult = objectMapper.readValue(response.getBody(), PageResult.class);
        Assert.assertNotNull("PageResult không được null!", pageResult);
    }

    @Test
    public void testGetExamsByPage_Unauthorized_Student() throws IOException {
        Assert.assertNotNull("ExamService bị null!", examService);

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + getStudentToken());
        HttpEntity<String> entity = new HttpEntity<>(headers);

        ResponseEntity<String> response = restTemplate.exchange(
                getRootUrl() + "/exams?page=0&size=10",
                HttpMethod.GET,
                entity,
                String.class
        );

        Assert.assertEquals("Expected forbidden for student role", 403, response.getStatusCodeValue());
    }

    @Test
    public void testGetAllByUser_Success() throws IOException {
        Assert.assertNotNull("ExamUserService bị null!", examUserService);

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + getStudentToken());
        HttpEntity<String> entity = new HttpEntity<>(headers);

        ResponseEntity<String> response = restTemplate.exchange(
                getRootUrl() + "/exams/list-all-by-user",
                HttpMethod.GET,
                entity,
                String.class
        );

        Assert.assertEquals("Expected OK status", 200, response.getStatusCodeValue());
        List<ExamUser> examUsers = objectMapper.readValue(response.getBody(),
                objectMapper.getTypeFactory().constructCollectionType(List.class, ExamUser.class));
        Assert.assertNotNull("ExamUser list không được null!", examUsers);
    }

    @Test
    public void testGetExamUserById_Success() throws IOException {
        Assert.assertNotNull("ExamUserService bị null!", examUserService);

        // Assuming exam ID 1 exists and is associated with student "nvvanh"
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + getStudentToken());
        HttpEntity<String> entity = new HttpEntity<>(headers);

        ResponseEntity<String> response = restTemplate.exchange(
                getRootUrl() + "/exams/exam-user/1",
                HttpMethod.GET,
                entity,
                String.class
        );

        Assert.assertEquals("Expected OK status", 200, response.getStatusCodeValue());
        ExamUser examUser = objectMapper.readValue(response.getBody(), ExamUser.class);
        Assert.assertNotNull("ExamUser không được null!", examUser);
        Assert.assertEquals("Expected exam ID 1", 1L, examUser.getExam().getId().longValue());
    }

    @Test
    public void testGetExamUserById_NotFound() throws IOException {
        Assert.assertNotNull("ExamUserService bị null!", examUserService);

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + getStudentToken());
        HttpEntity<String> entity = new HttpEntity<>(headers);

        ResponseEntity<String> response = restTemplate.exchange(
                getRootUrl() + "/exams/exam-user/999999",
                HttpMethod.GET,
                entity,
                String.class
        );

        Assert.assertEquals("Expected NOT_FOUND status", 404, response.getStatusCodeValue());
        Assert.assertEquals("Expected error message", "Không tìm thấy exam user này", response.getBody());
    }

    @Test
    public void testGetAllQuestions_Success() throws IOException {
        Assert.assertNotNull("ExamService bị null!", examService);

        // Assuming exam ID 1 exists, is unlocked, and has questions
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + getStudentToken());
        HttpEntity<String> entity = new HttpEntity<>(headers);

        ResponseEntity<String> response = restTemplate.exchange(
                getRootUrl() + "/exams/1/questions",
                HttpMethod.GET,
                entity,
                String.class
        );

        Assert.assertEquals("Expected OK status", 200, response.getStatusCodeValue());
        ExamQuestionList examQuestionList = objectMapper.readValue(response.getBody(), ExamQuestionList.class);
        Assert.assertNotNull("ExamQuestionList không được null!", examQuestionList);
        Assert.assertNotNull("Questions không được null!", examQuestionList.getQuestions());
    }

    @Test
    public void testGetAllQuestions_NotFound() throws IOException {
        Assert.assertNotNull("ExamService bị null!", examService);

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + getStudentToken());
        HttpEntity<String> entity = new HttpEntity<>(headers);

        ResponseEntity<String> response = restTemplate.exchange(
                getRootUrl() + "/exams/999999/questions",
                HttpMethod.GET,
                entity,
                String.class
        );

        Assert.assertEquals("Expected NOT_FOUND status", 404, response.getStatusCodeValue());
        Assert.assertEquals("Expected error message", "Không tìm thấy exam này", response.getBody());
    }

    @Test
    public void testCreateExam_Success() throws IOException {
        Assert.assertNotNull("ExamService bị null!", examService);
        Assert.assertNotNull("IntakeService bị null!", intakeService);
        Assert.assertNotNull("PartService bị null!", partService);
        Assert.assertNotNull("UserService bị null!", userService);

        Optional<Intake> intake = intakeService.findById(1L);
        Optional<Part> part = partService.findPartById(1L);
        Assert.assertTrue("Intake không tồn tại!", intake.isPresent());
        Assert.assertTrue("Part không tồn tại!", part.isPresent());

        Exam exam = new Exam();
        exam.setTitle("Test Exam");
        exam.setBeginExam(new Date(System.currentTimeMillis() + 86400000)); // Tomorrow
        exam.setFinishExam(new Date(System.currentTimeMillis() + 86400000 * 2));
        exam.setDurationExam(60);
        exam.setQuestionData("[]");

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + getAdminToken());
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Exam> entity = new HttpEntity<>(exam, headers);

        ResponseEntity<String> response = restTemplate.exchange(
                getRootUrl() + "/exams?intakeId=1&partId=1&isShuffle=false&locked=false",
                HttpMethod.POST,
                entity,
                String.class
        );

        Assert.assertEquals("Expected OK status", 200, response.getStatusCodeValue());
        Exam createdExam = objectMapper.readValue(response.getBody(), Exam.class);
        Assert.assertNotNull("Exam không được null!", createdExam);
        Assert.assertEquals("Expected title", "Test Exam", createdExam.getTitle());
    }

    @Test
    public void testGetExamById_Success() throws IOException {
        Assert.assertNotNull("ExamService bị null!", examService);

        Optional<Exam> examOpt = examService.getExamById(1L);
        Assert.assertTrue("Exam không tồn tại!", examOpt.isPresent());

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + getAdminToken());
        HttpEntity<String> entity = new HttpEntity<>(headers);

        ResponseEntity<String> response = restTemplate.exchange(
                getRootUrl() + "/exams/1",
                HttpMethod.GET,
                entity,
                String.class
        );

        Assert.assertEquals("Expected OK status", 200, response.getStatusCodeValue());
        Exam exam = objectMapper.readValue(response.getBody(), Exam.class);
        Assert.assertNotNull("Exam không được null!", exam);
        Assert.assertEquals("Expected exam ID 1", 1L, exam.getId().longValue());
    }

    @Test
    public void testGetExamById_NotFound() throws IOException {
        Assert.assertNotNull("ExamService bị null!", examService);

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + getAdminToken());
        HttpEntity<String> entity = new HttpEntity<>(headers);

        ResponseEntity<String> response = restTemplate.exchange(
                getRootUrl() + "/exams/999999",
                HttpMethod.GET,
                entity,
                String.class
        );

        Assert.assertEquals("Expected NO_CONTENT status", 204, response.getStatusCodeValue());
        // Note: Should return ServiceResult with 404 for consistency
    }

    @Test
    public void testCancelExam_Success() throws IOException {
        Assert.assertNotNull("ExamService bị null!", examService);
        Assert.assertNotNull("UserService bị null!", userService);
        Assert.assertNotNull("IntakeService bị null!", intakeService);
        Assert.assertNotNull("PartService bị null!", partService);

        Exam exam = new Exam();
        exam.setTitle("Test Cancel Exam");
        exam.setBeginExam(new Date(System.currentTimeMillis() + 86400000)); // Tomorrow
        exam.setFinishExam(new Date(System.currentTimeMillis() + 86400000 * 2));
        exam.setDurationExam(60);
        exam.setQuestionData("[]");
        Optional<User> userOpt = userService.getUserByUsername("thanhtam28ss");
        Assert.assertTrue("User không tồn tại!", userOpt.isPresent());
        exam.setCreatedBy(userOpt.get());
        exam.setIntake(intakeService.findById(1L).get());
        exam.setPart(partService.findPartById(1L).get());
        examService.saveExam(exam);

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + getAdminToken());
        HttpEntity<String> entity = new HttpEntity<>(headers);

        ResponseEntity<String> response = restTemplate.exchange(
                getRootUrl() + "/exams/" + exam.getId() + "/cancel",
                HttpMethod.GET,
                entity,
                String.class
        );

        Assert.assertEquals("Expected OK status", 200, response.getStatusCodeValue());
        ServiceResult serviceResult = objectMapper.readValue(response.getBody(), ServiceResult.class);
        Assert.assertEquals("Expected success status", 200, serviceResult.getStatusCode());
        Assert.assertEquals("Expected success message", "Exam canceled successfully", serviceResult.getMessage());
        Optional<Exam> examOpt = examService.getExamById(exam.getId());
        Assert.assertTrue("Exam should exist", examOpt.isPresent());
        Assert.assertTrue("Exam should be canceled", examOpt.get().isCanceled());
    }

    @Test
    public void testCancelExam_NotFound() throws IOException {
        Assert.assertNotNull("ExamService bị null!", examService);

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + getAdminToken());
        HttpEntity<String> entity = new HttpEntity<>(headers);

        ResponseEntity<String> response = restTemplate.exchange(
                getRootUrl() + "/exams/999999/cancel",
                HttpMethod.GET,
                entity,
                String.class
        );

        Assert.assertEquals("Expected OK status", 200, response.getStatusCodeValue());
        ServiceResult serviceResult = objectMapper.readValue(response.getBody(), ServiceResult.class);
        Assert.assertEquals("Expected NOT_FOUND status", 404, serviceResult.getStatusCode());
        Assert.assertEquals("Expected error message", "Not found with id: 999999", serviceResult.getMessage());
        Assert.assertNull("Data phải là null!", serviceResult.getData());
        Optional<Exam> examOpt = examService.getExamById(999999L);
        Assert.assertFalse("Non-existent exam should not exist", examOpt.isPresent());
    }

    @Test
    public void testCancelExam_Unauthorized_Student() throws IOException {
        Assert.assertNotNull("ExamService bị null!", examService);

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + getStudentToken());
        HttpEntity<String> entity = new HttpEntity<>(headers);

        ResponseEntity<String> response = restTemplate.exchange(
                getRootUrl() + "/exams/1/cancel",
                HttpMethod.GET,
                entity,
                String.class
        );

        Assert.assertEquals("Expected forbidden for student role", 403, response.getStatusCodeValue());
    }

    @Test
    public void testGetResultExamAll_Success() throws IOException {
        Assert.assertNotNull("ExamService bị null!", examService);

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + getAdminToken());
        HttpEntity<String> entity = new HttpEntity<>(headers);

        ResponseEntity<String> response = restTemplate.exchange(
                getRootUrl() + "/exams/1/result/all",
                HttpMethod.GET,
                entity,
                String.class
        );

        Assert.assertEquals("Expected OK status", 200, response.getStatusCodeValue());
        List<ExamResult> examResults = objectMapper.readValue(response.getBody(),
                objectMapper.getTypeFactory().constructCollectionType(List.class, ExamResult.class));
        Assert.assertNotNull("ExamResults không được null!", examResults);
    }

    @Test
    public void testGetResultExamAll_NotFound() throws IOException {
        Assert.assertNotNull("ExamService bị null!", examService);

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + getAdminToken());
        HttpEntity<String> entity = new HttpEntity<>(headers);

        ResponseEntity<String> response = restTemplate.exchange(
                getRootUrl() + "/exams/999999/result/all",
                HttpMethod.GET,
                entity,
                String.class
        );

        Assert.assertEquals("Expected NOT_FOUND status", 404, response.getStatusCodeValue());
        Assert.assertEquals("Expected error message", "Không tìm thấy exam", response.getBody());
    }

    @Test
    public void testGetResultExam_Success() throws IOException {
        Assert.assertNotNull("ExamService bị null!", examService);

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + getStudentToken());
        HttpEntity<String> entity = new HttpEntity<>(headers);

        ResponseEntity<String> response = restTemplate.exchange(
                getRootUrl() + "/exams/1/result",
                HttpMethod.GET,
                entity,
                String.class
        );

        Assert.assertEquals("Expected OK status", 200, response.getStatusCodeValue());
        ExamResult examResult = objectMapper.readValue(response.getBody(), ExamResult.class);
        Assert.assertNotNull("ExamResult không được null!", examResult);
    }

    @Test
    public void testGetResultExamByUser_Success() throws IOException {
        Assert.assertNotNull("ExamService bị null!", examService);

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + getAdminToken());
        HttpEntity<String> entity = new HttpEntity<>(headers);

        ResponseEntity<String> response = restTemplate.exchange(
                getRootUrl() + "/exams/1/users/nvvanh/result",
                HttpMethod.GET,
                entity,
                String.class
        );

        Assert.assertEquals("Expected OK status", 200, response.getStatusCodeValue());
        ExamResult examResult = objectMapper.readValue(response.getBody(), ExamResult.class);
        Assert.assertNotNull("ExamResult không được null!", examResult);
    }

    @Test
    public void testGetQuestionTextByExamId_Success() throws IOException {
        Assert.assertNotNull("ExamService bị null!", examService);

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + getAdminToken());
        HttpEntity<String> entity = new HttpEntity<>(headers);

        ResponseEntity<String> response = restTemplate.exchange(
                getRootUrl() + "/exam/1/question-text",
                HttpMethod.GET,
                entity,
                String.class
        );

        Assert.assertEquals("Expected OK status", 200, response.getStatusCodeValue());
        List<ExamDetail> examDetails = objectMapper.readValue(response.getBody(),
                objectMapper.getTypeFactory().constructCollectionType(List.class, ExamDetail.class));
        Assert.assertNotNull("ExamDetails không được null!", examDetails);
    }

    @Test
    public void testGetExamCalendar_Success() throws IOException {
        Assert.assertNotNull("ExamUserService bị null!", examUserService);

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + getStudentToken());
        HttpEntity<String> entity = new HttpEntity<>(headers);

        ResponseEntity<String> response = restTemplate.exchange(
                getRootUrl() + "/exams/schedule",
                HttpMethod.GET,
                entity,
                String.class
        );

        Assert.assertEquals("Expected OK status", 200, response.getStatusCodeValue());
        List<ExamCalendar> examCalendars = objectMapper.readValue(response.getBody(),
                objectMapper.getTypeFactory().constructCollectionType(List.class, ExamCalendar.class));
        Assert.assertNotNull("ExamCalendars không được null!", examCalendars);
    }
}