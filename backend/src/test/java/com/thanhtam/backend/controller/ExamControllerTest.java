package com.thanhtam.backend.controller;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.thanhtam.backend.dto.PageResult;
import com.thanhtam.backend.dto.ServiceResult;
import com.thanhtam.backend.entity.Exam;
import com.thanhtam.backend.entity.ExamUser;
import com.thanhtam.backend.entity.Intake;
import com.thanhtam.backend.entity.Part;
import com.thanhtam.backend.entity.User;
import com.thanhtam.backend.service.ExamService;
import com.thanhtam.backend.service.ExamUserService;
import com.thanhtam.backend.service.IntakeService;
import com.thanhtam.backend.service.PartService;
import com.thanhtam.backend.service.UserService;
import com.thanhtam.backend.ultilities.ERole;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
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
    private UserService userService;

    @Autowired
    private IntakeService intakeService;

    @Autowired
    private PartService partService;

    @Autowired
    private ExamUserService examUserService;

    private static String adminToken;
    private static String lecturerToken;
    private static String studentToken;

    private String getRootUrl() {
        return "http://localhost:" + port + "/api";
    }

    @Before
    public void setupToken() throws IOException {
        // Login as admin
        String loginPayloadAdmin = "{ \"username\": \"thanhtam28ss\", \"password\": \"Abcd@12345\" }";
        HttpHeaders headers = new HttpHeaders();
        headers.set("Content-Type", "application/json");
        HttpEntity<String> requestAdmin = new HttpEntity<>(loginPayloadAdmin, headers);
        ResponseEntity<String> responseAdmin = restTemplate.postForEntity(
                getRootUrl() + "/auth/signin",
                requestAdmin,
                String.class
        );
        if (responseAdmin.getStatusCodeValue() != 200) {
            throw new RuntimeException("Admin login failed with status code: " + responseAdmin.getStatusCodeValue());
        }
        adminToken = objectMapper.readTree(responseAdmin.getBody()).get("accessToken").asText();

        // Login as lecturer
        String loginPayloadLecturer = "{ \"username\": \"tamht298\", \"password\": \"Abcd@12345\" }";
        HttpEntity<String> requestLecturer = new HttpEntity<>(loginPayloadLecturer, headers);
        ResponseEntity<String> responseLecturer = restTemplate.postForEntity(
                getRootUrl() + "/auth/signin",
                requestLecturer,
                String.class
        );
        if (responseLecturer.getStatusCodeValue() != 200) {
            throw new RuntimeException("Lecturer login failed with status code: " + responseLecturer.getStatusCodeValue());
        }
        lecturerToken = objectMapper.readTree(responseLecturer.getBody()).get("accessToken").asText();

        // Login as student
        String loginPayloadStudent = "{ \"username\": \"nvvanh\", \"password\": \"Abcd@12345\" }";
        HttpEntity<String> requestStudent = new HttpEntity<>(loginPayloadStudent, headers);
        ResponseEntity<String> responseStudent = restTemplate.postForEntity(
                getRootUrl() + "/auth/signin",
                requestStudent,
                String.class
        );
        if (responseStudent.getStatusCodeValue() != 200) {
            throw new RuntimeException("Student login failed with status code: " + responseStudent.getStatusCodeValue());
        }
        studentToken = objectMapper.readTree(responseStudent.getBody()).get("accessToken").asText();
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
        Assert.assertNotNull("ExamService is null!", examService);

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + getAdminToken());
        HttpEntity<String> entity = new HttpEntity<>(headers);

        ResponseEntity<String> response = restTemplate.exchange(
                getRootUrl() + "/exams",
                HttpMethod.GET,
                entity,
                String.class
        );

        Assert.assertEquals(200, response.getStatusCodeValue());

        PageResult pageResult = objectMapper.readValue(response.getBody(), PageResult.class);
        Assert.assertNotNull("PageResult should not be null!", pageResult);
        Assert.assertNotNull("Data in PageResult should not be null!", pageResult.getData());
    }

    @Test
    public void testGetExamsByPage_Lecturer_Success() throws IOException {
        Assert.assertNotNull("ExamService is null!", examService);

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + getLecturerToken());
        HttpEntity<String> entity = new HttpEntity<>(headers);

        ResponseEntity<String> response = restTemplate.exchange(
                getRootUrl() + "/exams",
                HttpMethod.GET,
                entity,
                String.class
        );

        Assert.assertEquals(200, response.getStatusCodeValue());

        PageResult pageResult = objectMapper.readValue(response.getBody(), PageResult.class);
        Assert.assertNotNull("PageResult should not be null!", pageResult);
        Assert.assertNotNull("Data in PageResult should not be null!", pageResult.getData());
    }

    @Test
    public void testGetAllByUser_Student_Success() throws IOException {
        Assert.assertNotNull("ExamUserService is null!", examUserService);

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + getStudentToken());
        HttpEntity<String> entity = new HttpEntity<>(headers);

        ResponseEntity<String> response = restTemplate.exchange(
                getRootUrl() + "/exams/list-all-by-user",
                HttpMethod.GET,
                entity,
                String.class
        );

        Assert.assertEquals(200, response.getStatusCodeValue());
        List<ExamUser> examUsers = objectMapper.readValue(response.getBody(), objectMapper.getTypeFactory().constructCollectionType(List.class, ExamUser.class));
        Assert.assertNotNull("ExamUser list should not be null", examUsers);
    }


    @Test
    public void testGetExamUserById_Student_Success() throws IOException {
        Assert.assertNotNull("ExamUserService is null!", examUserService);
        // Assuming an examUser exists for the student for examId 1
        // You might need to create one first if it doesn't exist or use an existing one
        Long existingExamId = 1L; // Replace with an actual existing exam ID assigned to the student

        // Create ExamUser for testing if necessary, or ensure one exists
        // Optional<User> studentUser = userService.getUserByUsername("nvvanh");
        // Optional<Exam> exam = examService.getExamById(existingExamId);
        // if (studentUser.isPresent() && exam.isPresent()) {
        //    if (examUserService.findByExamAndUser(existingExamId, studentUser.get().getUsername()) == null) {
        //        ExamUser eu = new ExamUser();
        //        eu.setExam(exam.get());
        //        eu.setUser(studentUser.get());
        //        eu.setIsStarted(false);
        //        eu.setIsFinished(false);
        //        eu.setRemainingTime(exam.get().getDurationExam() * 60);
        //        examUserService.save(eu); // Assuming a save method exists
        //    }
        // }


        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + getStudentToken());
        HttpEntity<String> entity = new HttpEntity<>(headers);

        ResponseEntity<String> response = restTemplate.exchange(
                getRootUrl() + "/exams/exam-user/" + existingExamId,
                HttpMethod.GET,
                entity,
                String.class
        );

        Assert.assertEquals(200, response.getStatusCodeValue());
        ExamUser examUser = objectMapper.readValue(response.getBody(), ExamUser.class);
        Assert.assertNotNull("ExamUser should not be null", examUser);
        Assert.assertEquals(existingExamId, examUser.getExam().getId());
    }

    @Test
    public void testGetExamById_Success() throws IOException {
        Assert.assertNotNull("ExamService is null!", examService);
        // Assuming an exam with ID 1 exists
        Long existingExamId = 1L;
        Optional<Exam> examOpt = examService.getExamById(existingExamId);
        Assert.assertTrue("Exam with ID " + existingExamId + " should exist for this test", examOpt.isPresent());


        HttpHeaders headers = new HttpHeaders();
        // No specific role needed for this endpoint as per ExamController, but let's use admin
        headers.set("Authorization", "Bearer " + getAdminToken());
        HttpEntity<String> entity = new HttpEntity<>(headers);

        ResponseEntity<String> response = restTemplate.exchange(
                getRootUrl() + "/exams/" + existingExamId,
                HttpMethod.GET,
                entity,
                String.class
        );

        Assert.assertEquals(200, response.getStatusCodeValue());
        Exam exam = objectMapper.readValue(response.getBody(), Exam.class);
        Assert.assertNotNull("Exam should not be null", exam);
        Assert.assertEquals(existingExamId, exam.getId());
    }

    @Test
    public void testGetExamById_NotFound() throws IOException {
        Assert.assertNotNull("ExamService is null!", examService);
        Long nonExistentExamId = 9999L;

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + getAdminToken());
        HttpEntity<String> entity = new HttpEntity<>(headers);

        ResponseEntity<String> response = restTemplate.exchange(
                getRootUrl() + "/exams/" + nonExistentExamId,
                HttpMethod.GET,
                entity,
                String.class
        );
        // As per ExamController, it returns the exam object or NO_CONTENT (204) if not found.
        // If it wraps in ServiceResult for not found, then this needs adjustment.
        // Based on `return new ResponseEntity<>(exam.get(), HttpStatus.NO_CONTENT);` it should be 204
        Assert.assertEquals(204, response.getStatusCodeValue()); // NO_CONTENT
    }

//    @Test
//    public void testCreateExam_Admin_Success() throws IOException {
//        Assert.assertNotNull("ExamService is null!", examService);
//        Assert.assertNotNull("IntakeService is null!", intakeService);
//        Assert.assertNotNull("PartService is null!", partService);
//        Assert.assertNotNull("UserService is null!", userService);
//
//        Optional<Intake> intakeOpt = intakeService.findById(1L); // Assuming intake with ID 1 exists
//        Assert.assertTrue("Intake with ID 1 should exist", intakeOpt.isPresent());
//        Optional<Part> partOpt = partService.findPartById(1L); // Assuming part with ID 1 exists
//        Assert.assertTrue("Part with ID 1 should exist", partOpt.isPresent());
//
//        Exam newExam = new Exam();
//        newExam.setTitle("Test Exam API " + System.currentTimeMillis());
//        newExam.setDescription("Test Exam Description");
//        newExam.setBeginExam(new Date(System.currentTimeMillis() + 3600000)); // Starts in 1 hour
//        newExam.setFinishExam(new Date(System.currentTimeMillis() + 7200000)); // Finishes in 2 hours
//        newExam.setDurationExam(60); // 60 minutes
//        // Sample Question Data (ensure this structure matches what ExamController expects)
//        newExam.setQuestionData("[{\"questionId\":1,\"point\":10},{\"questionId\":2,\"point\":5}]");
//
//
//        HttpHeaders headers = new HttpHeaders();
//        headers.set("Authorization", "Bearer " + getAdminToken());
//        headers.set("Content-Type", "application/json");
//
//        HttpEntity<Exam> entity = new HttpEntity<>(newExam, headers);
//
//        String url = getRootUrl() + "/exams?intakeId=1&partId=1&isShuffle=true&locked=false";
//
//        ResponseEntity<String> response = restTemplate.exchange(
//                url,
//                HttpMethod.POST,
//                entity,
//                String.class
//        );
//
//        Assert.assertEquals(200, response.getStatusCodeValue());
//        Exam createdExam = objectMapper.readValue(response.getBody(), Exam.class);
//        Assert.assertNotNull("Created exam should not be null", createdExam);
//        Assert.assertEquals(newExam.getTitle(), createdExam.getTitle());
//        Assert.assertNotNull("Created exam ID should not be null", createdExam.getId());
//        Assert.assertEquals(1L, createdExam.getIntake().getId().longValue());
//        Assert.assertEquals(1L, createdExam.getPart().getId().longValue());
//        Assert.assertTrue("Exam should be set to shuffle", createdExam.isShuffle());
//    }

    @Test
    public void testGetExamCalendar_Student_Success() throws IOException {
        Assert.assertNotNull("ExamUserService is null!", examUserService);
        Assert.assertNotNull("UserService is null!", userService);

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + getStudentToken());
        HttpEntity<String> entity = new HttpEntity<>(headers);

        ResponseEntity<String> response = restTemplate.exchange(
                getRootUrl() + "/exams/schedule",
                HttpMethod.GET,
                entity,
                String.class
        );

        Assert.assertEquals(200, response.getStatusCodeValue());
        // Assuming ExamCalendar is the DTO returned
        List<Object> examCalendars = objectMapper.readValue(response.getBody(), objectMapper.getTypeFactory().constructCollectionType(List.class, Object.class)); // Replace Object.class with ExamCalendar.class if you have it
        Assert.assertNotNull("Exam calendar list should not be null", examCalendars);
        // Add more specific assertions if needed, e.g., checking the size or contents
    }

    // Note: Testing endpoints like /exams/{examId}/questions, /exams/{examId}/questions-by-user,
    // /exams/{examId}/result/all, /exams/{examId}/result/all/question-report, /exams/{examId}/result,
    // /exams/{examId}/users/{username}/result, /exam/{id}/question-text, and /exams/{id}/cancel
    // would require more complex setup, including managing exam states (started, finished, locked),
    // answer sheets, and potentially mocking external dependencies or ensuring specific data states
    // in the database. These are more involved integration tests.

    // Example for a simple GET endpoint that might be easier to test:
    @Test
    public void testGetQuestionTextByExamId_Success() throws IOException {
        Assert.assertNotNull("ExamService is null!", examService);
        // Assume an exam with ID 1 exists and has questionData set
        Long existingExamId = 1L;
        Optional<Exam> examOpt = examService.getExamById(existingExamId);
        Assert.assertTrue("Exam with ID " + existingExamId + " should exist", examOpt.isPresent());
        // Ensure examOpt.get().getQuestionData() is not null and is valid JSON for the test to pass fully.
        // For this test, we'll assume it's set up correctly in the DB.
        if (examOpt.get().getQuestionData() == null) {
            // Manually set some data if your test DB allows or skip this specific assertion path
            // examOpt.get().setQuestionData("[{\"questionId\":1,\"point\":10}]");
            // examService.saveExam(examOpt.get()); // This might not be ideal in a test without rollback
            System.out.println("Warning: Exam " + existingExamId + " has null questionData. Test might not be complete.");
        }


        HttpHeaders headers = new HttpHeaders();
        // No specific role, using admin for consistency
        headers.set("Authorization", "Bearer " + getAdminToken());
        HttpEntity<String> entity = new HttpEntity<>(headers);

        ResponseEntity<String> response = restTemplate.exchange(
                getRootUrl() + "/exam/" + existingExamId + "/question-text",
                HttpMethod.GET,
                entity,
                String.class
        );

        Assert.assertEquals(200, response.getStatusCodeValue());
        // Assuming ExamDetail is the DTO, replace Object.class if you have it
        List<Object> examDetails = objectMapper.readValue(response.getBody(), objectMapper.getTypeFactory().constructCollectionType(List.class, Object.class));
        Assert.assertNotNull("Exam details list should not be null", examDetails);
        // If examOpt.get().getQuestionData() was indeed like "[{\"questionId\":1,\"point\":10}]"
        // and question service can find question 1, then examDetails should not be empty.
    }

//    @Test
//    public void testCancelExam_Admin_BeforeBeginTime_Success() throws IOException {
//        Assert.assertNotNull("ExamService is null!", examService);
//        Assert.assertNotNull("UserService is null!", userService);
//
//        // Create a new exam that starts in the future for this test
//        Exam futureExam = new Exam();
//        futureExam.setTitle("Cancellable Exam " + System.currentTimeMillis());
//        futureExam.setDescription("This exam is to be cancelled.");
//        futureExam.setBeginExam(new Date(System.currentTimeMillis() + 2 * 3600000)); // Starts in 2 hours
//        futureExam.setFinishExam(new Date(System.currentTimeMillis() + 3 * 3600000)); // Finishes in 3 hours
//        futureExam.setDurationExam(60);
//        futureExam.setQuestionData("[]"); // Minimal valid question data
//
//        Optional<User> adminUser = userService.getUserByUsername("thanhtam28ss"); // Assuming admin user
//        Assert.assertTrue(adminUser.isPresent());
//        futureExam.setCreatedBy(adminUser.get());
//
//        Optional<Intake> intake = intakeService.findById(1L); // Assuming intake 1 exists
//        Assert.assertTrue(intake.isPresent());
//        futureExam.setIntake(intake.get());
//
//        Optional<Part> part = partService.findPartById(1L); // Assuming part 1 exists
//        Assert.assertTrue(part.isPresent());
//        futureExam.setPart(part.get());
//
//        Exam savedExam = examService.saveExam(futureExam); // saveExam should return the saved entity with ID
//        Assert.assertNotNull(savedExam.getId());
//
//
//        HttpHeaders headers = new HttpHeaders();
//        headers.set("Authorization", "Bearer " + getAdminToken());
//        HttpEntity<String> entity = new HttpEntity<>(headers);
//
//        ResponseEntity<Void> response = restTemplate.exchange(
//                getRootUrl() + "/exams/" + savedExam.getId() + "/cancel",
//                HttpMethod.GET, // The controller uses GET for cancel
//                entity,
//                Void.class
//        );
//        // The controller method is void, so successful execution usually means 200 OK or 204 No Content
//        // Depending on Spring MVC default behavior for void methods. Often it's 200.
//        Assert.assertEquals(200, response.getStatusCodeValue()); // Or 204 if that's how it's configured
//
//        // Verify the exam is marked as cancelled (or handled as per your examService.cancelExam logic)
//        Optional<Exam> cancelledExamOpt = examService.getExamById(savedExam.getId());
//        Assert.assertTrue(cancelledExamOpt.isPresent());
//        // Assert.assertTrue(cancelledExamOpt.get().isCanceled()); // Assuming a getCanceled() method and flag
//        // The current ExamController.cancelExam does not seem to set a flag on the exam entity itself,
//        // but calls examService.cancelExam(id). You'd need to check what that service method does
//        // (e.g., soft delete, set a status, etc.) and assert that change.
//        // For now, we just check the call was successful.
//    }


}