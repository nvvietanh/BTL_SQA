package com.thanhtam.backend.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.thanhtam.backend.dto.PageResult;
import com.thanhtam.backend.dto.ServiceResult;
import com.thanhtam.backend.entity.Choice;
import com.thanhtam.backend.entity.Part;
import com.thanhtam.backend.entity.Question;
import com.thanhtam.backend.entity.QuestionType;
import com.thanhtam.backend.service.PartService;
import com.thanhtam.backend.service.QuestionService;
import com.thanhtam.backend.service.QuestionTypeService;
import com.thanhtam.backend.ultilities.DifficultyLevel;
import com.thanhtam.backend.ultilities.EQTypeCode;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.jupiter.api.BeforeAll;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Transactional
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
public class QuestionControllerTest {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private QuestionService questionService;

    @Autowired
    private PartService partService;

    @Autowired
    private QuestionTypeService questionTypeService;

    private static String adminToken;
    private static String lecturerToken;
    private static String studentToken;

    private String getRootUrl() {
        return "http://localhost:" + port + "/api";
    }

    // private String getToken() {
    //     return "eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJ0aGFuaHRhbTI4c3MiLCJpYXQiOjE3NDQyOTQ4MTIsImV4cCI6MTc0NDM4MTIxMiwicm9sZSI6W3siYXV0aG9yaXR5IjoiUk9MRV9BRE1JTiJ9XX0.dlO69r0W_COp4s0Y1zZ1e4cGUE-56KPuAW5WeebauMCzT0Q8Ct3tXw_flMBuSQuqIRQ4tcy_JNju-wjL5X5F0w";
    // }

//    @BeforeClass
//    @BeforeAll
    @Before
    public void setupToken() throws IOException {
        // Tạo payload cho API đăng nhập role admin
        String loginPayloadAdmin = "{ \"username\": \"thanhtam28ss\", \"password\": \"Abcd@12345\" }";

        // Tạo header với Content-Type
        HttpHeaders headers = new HttpHeaders();
        headers.set("Content-Type", "application/json");

        // Tạo request entity
        HttpEntity<String> requestAdmin = new HttpEntity<>(loginPayloadAdmin, headers);

        // Gọi API đăng nhập cho admin
        ResponseEntity<String> responseAdmin = restTemplate.postForEntity(
                getRootUrl() + "/auth/signin", // Đường dẫn API đăng nhập
                requestAdmin,
                String.class
        );

        // Kiểm tra trạng thái HTTP
        if (responseAdmin.getStatusCodeValue() != 200) {
            throw new RuntimeException("Đăng nhập admin thất bại với mã trạng thái: " + responseAdmin.getStatusCodeValue());
        }

        // Parse token từ response role admin
        String responseBodyAdmin = responseAdmin.getBody();
        adminToken = objectMapper.readTree(responseBodyAdmin).get("accessToken").asText();

        // Tạo payload cho API đăng nhập role lecturer
        String loginPayloadLecturer = "{ \"username\": \"tamht298\", \"password\": \"Abcd@12345\" }";
        HttpEntity<String> requestLecturer = new HttpEntity<>(loginPayloadLecturer, headers);

        // Gọi API đăng nhập cho lecturer
        ResponseEntity<String> responseLecturer = restTemplate.postForEntity(
                getRootUrl() + "/auth/signin",
                requestLecturer,
                String.class
        );

        // Kiểm tra trạng thái HTTP
        if (responseLecturer.getStatusCodeValue() != 200) {
            throw new RuntimeException("Đăng nhập lecturer thất bại với mã trạng thái: " + responseLecturer.getStatusCodeValue());
        }

        // Parse token từ response role lecturer
        String responseBodyLecturer = responseLecturer.getBody();
        lecturerToken = objectMapper.readTree(responseBodyLecturer).get("accessToken").asText();

        // Tạo payload cho API đăng nhập role student
        String loginPayloadStudent = "{ \"username\": \"nvvanh\", \"password\": \"Abcd@12345\" }";
        HttpEntity<String> requestStudent = new HttpEntity<>(loginPayloadStudent, headers);

        // Gọi API đăng nhập cho student
        ResponseEntity<String> responseStudent = restTemplate.postForEntity(
                getRootUrl() + "/auth/signin",
                requestStudent,
                String.class
        );

        // Kiểm tra trạng thái HTTP
        if (responseStudent.getStatusCodeValue() != 200) {
            throw new RuntimeException("Đăng nhập student thất bại với mã trạng thái: " + responseStudent.getStatusCodeValue());
        }

        // Parse token từ response role student
        String responseBodyStudent = responseStudent.getBody();
        studentToken = objectMapper.readTree(responseBodyStudent).get("accessToken").asText();

        // In ra token để kiểm tra
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
    public void testGetAllQuestions_Success() throws IOException {
//        setupToken();
        // Kiểm tra service không null
        Assert.assertNotNull("QuestionService bị null!", questionService);
        
        // Gọi API lấy danh sách questions
//        ResponseEntity<String> response = restTemplate.exchange(
//            getRootUrl() + "/questions",
//            HttpMethod.GET,
//            new HttpEntity<>(new HttpHeaders()),
//            String.class
//        );

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + getAdminToken());

        HttpEntity<String> entity = new HttpEntity<>(headers);

        ResponseEntity<String> response = restTemplate.exchange(
                getRootUrl() + "/questions",
                HttpMethod.GET,
                entity,
                String.class
        );

        // Kiểm tra kết quả
        Assert.assertEquals(200, response.getStatusCodeValue());
        
        // Parse response body
        ServiceResult serviceResult = objectMapper.readValue(response.getBody(), ServiceResult.class);
        Assert.assertEquals(200, serviceResult.getStatusCode());
        Assert.assertEquals("Get question bank successfully!", serviceResult.getMessage());
        
        // Kiểm tra dữ liệu
        List<Question> questions = objectMapper.convertValue(serviceResult.getData(), 
            objectMapper.getTypeFactory().constructCollectionType(List.class, Question.class));
        Assert.assertNotNull("Danh sách questions không được null!", questions);
        Assert.assertFalse("Danh sách questions không được rỗng!", questions.isEmpty());
    }

    @Test
    public void testGetQuestionById_Success() throws IOException {
        // Kiểm tra service không null
        Assert.assertNotNull("QuestionService bị null!", questionService);
        
        // Lấy question có id = 1 từ DB
        Optional<Question> questionOpt = questionService.getQuestionById(8L);
        Assert.assertTrue("Question không tồn tại!", questionOpt.isPresent());
        
        // Gọi API lấy question theo id
        ResponseEntity<String> response = restTemplate.exchange(
            getRootUrl() + "/questions/1",
            HttpMethod.GET,
            new HttpEntity<>(new HttpHeaders()),
            String.class
        );
        
        // Kiểm tra kết quả
        Assert.assertEquals(200, response.getStatusCodeValue());
        
        // Parse response body
        Question question = objectMapper.readValue(response.getBody(), Question.class);
        Assert.assertNotNull("Question không được null!", question);
        Assert.assertEquals(1L, question.getId().longValue());
    }

    @Test
    public void testGetQuestionById_NotFound() throws IOException {
        // Kiểm tra service không null
        Assert.assertNotNull("QuestionService bị null!", questionService);
        
        // Gọi API lấy question không tồn tại
        ResponseEntity<String> response = restTemplate.exchange(
            getRootUrl() + "/questions/999999",
            HttpMethod.GET,
            new HttpEntity<>(new HttpHeaders()),
            String.class
        );
        
        // Kiểm tra kết quả
        Assert.assertEquals(200, response.getStatusCodeValue());
        
        // Parse response body
        ServiceResult serviceResult = objectMapper.readValue(response.getBody(), ServiceResult.class);
        Assert.assertEquals(404, serviceResult.getStatusCode());
        Assert.assertEquals("Not found with id: 999999", serviceResult.getMessage());
        Assert.assertNull("Data phải là null!", serviceResult.getData());
    }

    @Test
    public void testGetQuestionsByPart_Success() throws IOException {
        // Kiểm tra service không null
        Assert.assertNotNull("QuestionService bị null!", questionService);
        Assert.assertNotNull("PartService bị null!", partService);
        
        // Lấy part có id = 1 từ DB
        Optional<Part> partOpt = partService.findPartById(1L);
        Assert.assertTrue("Part không tồn tại!", partOpt.isPresent());
        
        // Gọi API lấy danh sách questions theo part
        ResponseEntity<String> response = restTemplate.exchange(
            getRootUrl() + "/parts/1/questions",
            HttpMethod.GET,
            new HttpEntity<>(new HttpHeaders()),
            String.class
        );
        
        // Kiểm tra kết quả
        Assert.assertEquals(200, response.getStatusCodeValue());
        
        // Parse response body
        PageResult pageResult = objectMapper.readValue(response.getBody(), PageResult.class);
        Assert.assertNotNull("PageResult không được null!", pageResult);
        
        // Kiểm tra dữ liệu
        Page<Question> questions = objectMapper.convertValue(pageResult.getData(), 
            objectMapper.getTypeFactory().constructParametricType(Page.class, Question.class));
        Assert.assertNotNull("Page questions không được null!", questions);
        Assert.assertFalse("Page questions không được rỗng!", questions.getContent().isEmpty());
    }

    /**
     * TC_QC_01: Kiểm tra API lấy danh sách câu hỏi theo partId
     * Mục tiêu: Kiểm tra API lấy danh sách câu hỏi theo partId = 0 (admin)
     * Input: partId = 0
     * Kết quả mong đợi: API trả về danh sách câu hỏi của tất cả các part
     * @throws IOException
     */
    @Test
    public void testGetQuestionsByPart_Admin_AllParts() throws IOException {
        // Gọi API lấy danh sách questions với partId = 0 (admin)
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + getAdminToken());
        HttpEntity<String> entity = new HttpEntity<>(headers);
    
        ResponseEntity<String> response = restTemplate.exchange(
                getRootUrl() + "/parts/0/questions",
                HttpMethod.GET,
                entity,
                String.class
        );
    
        // Kiểm tra kết quả
        Assert.assertEquals(200, response.getStatusCodeValue());
    
        // Parse response body
        PageResult pageResult = objectMapper.readValue(response.getBody(), PageResult.class);
        Assert.assertNotNull("PageResult không được null!", pageResult);
    }

    /**
     * TC_QC_02: Kiểm tra API lấy danh sách câu hỏi theo partId có trong cơ sở dữ liệu
     * Mục tiêu: Kiểm tra API lấy danh sách câu hỏi theo partId có trong cơ sở dữ liệu của người dùng là lecturer
     * Input: partId = 75 và role = LECTURER
     * Kết quả mong đợi: API trả về danh sách câu hỏi của partId = 75 và người dùng là lecturer
     * @throws IOException
     */
    
    @Test
    public void testGetQuestionsByPart_Lecturer_SpecificPart() throws IOException {
        // Gọi API lấy danh sách questions với partId cụ thể (lecturer)
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + getLecturerToken());
        HttpEntity<String> entity = new HttpEntity<>(headers);
    
        ResponseEntity<String> response = restTemplate.exchange(
                getRootUrl() + "/parts/75/questions",
                HttpMethod.GET,
                entity,
                String.class
        );
    
        // Kiểm tra kết quả
        Assert.assertEquals(200, response.getStatusCodeValue());
    
        // Parse response body
        PageResult pageResult = objectMapper.readValue(response.getBody(), PageResult.class);
        Assert.assertNotNull("PageResult không được null!", pageResult);
    }

    @Test
    public void testCreateQuestion_Success() throws IOException {
        // Kiểm tra service không null
        Assert.assertNotNull("QuestionService bị null!", questionService);
        Assert.assertNotNull("PartService bị null!", partService);
        Assert.assertNotNull("QuestionTypeService bị null!", questionTypeService);
        
        // Lấy part và questionType từ DB
        Optional<Part> partOpt = partService.findPartById(1L);
        Optional<QuestionType> questionTypeOpt = questionTypeService.getQuestionTypeByCode(EQTypeCode.MC);
        
        Assert.assertTrue("Part không tồn tại!", partOpt.isPresent());
        Assert.assertTrue("QuestionType không tồn tại!", questionTypeOpt.isPresent());
        
        // Tạo question mới
        Question question = new Question();
        question.setQuestionText("Test Question");
        question.setDifficultyLevel(DifficultyLevel.EASY);
        
        // Tạo choices
        Choice choice1 = new Choice();
        choice1.setChoiceText("Choice 1");
        choice1.setIsCorrected(1);
        
        Choice choice2 = new Choice();
        choice2.setChoiceText("Choice 2");
        choice2.setIsCorrected(0);
        
        question.setChoices(Arrays.asList(choice1, choice2));
        
        // Gọi API tạo question
        ResponseEntity<String> response = restTemplate.exchange(
            getRootUrl() + "/questions?questionType=MULTIPLE_CHOICE&partId=1",
            HttpMethod.POST,
            new HttpEntity<>(question, new HttpHeaders()),
            String.class
        );
        
        // Kiểm tra kết quả
        Assert.assertEquals(200, response.getStatusCodeValue());
        
        // Parse response body
        Question createdQuestion = objectMapper.readValue(response.getBody(), Question.class);
        Assert.assertNotNull("Question không được null!", createdQuestion);
        Assert.assertEquals("Test Question", createdQuestion.getQuestionText());
        Assert.assertEquals(DifficultyLevel.EASY, createdQuestion.getDifficultyLevel());
        Assert.assertEquals(5, createdQuestion.getPoint()); // EASY = 5 points
        Assert.assertEquals(1L, createdQuestion.getPart().getId().longValue());
        Assert.assertEquals(EQTypeCode.MC, createdQuestion.getQuestionType().getTypeCode());
    }

    @Test
    public void testUpdateQuestion_Success() throws IOException {
        // Kiểm tra service không null
        Assert.assertNotNull("QuestionService bị null!", questionService);
        
        // Lấy question có id = 1 từ DB
        Optional<Question> questionOpt = questionService.getQuestionById(1L);
        Assert.assertTrue("Question không tồn tại!", questionOpt.isPresent());
        
        // Tạo question mới để update
        Question question = new Question();
        question.setQuestionText("Updated Question");
        question.setDifficultyLevel(DifficultyLevel.MEDIUM);
        
        // Gọi API update question
        ResponseEntity<String> response = restTemplate.exchange(
            getRootUrl() + "/questions/1",
            HttpMethod.PUT,
            new HttpEntity<>(question, new HttpHeaders()),
            String.class
        );
        
        // Kiểm tra kết quả
        Assert.assertEquals(200, response.getStatusCodeValue());
        
        // Parse response body
        ServiceResult serviceResult = objectMapper.readValue(response.getBody(), ServiceResult.class);
        Assert.assertEquals(200, serviceResult.getStatusCode());
        Assert.assertEquals("Get question with id: 1", serviceResult.getMessage());
        
        // Kiểm tra dữ liệu
        Question updatedQuestion = objectMapper.convertValue(serviceResult.getData(), Question.class);
        Assert.assertNotNull("Question không được null!", updatedQuestion);
        Assert.assertEquals("Updated Question", updatedQuestion.getQuestionText());
        Assert.assertEquals(DifficultyLevel.MEDIUM, updatedQuestion.getDifficultyLevel());
        Assert.assertEquals(10, updatedQuestion.getPoint()); // MEDIUM = 10 points
    }

    @Test
    public void testDeleteQuestion_Success() {
        // Kiểm tra service không null
        Assert.assertNotNull("QuestionService bị null!", questionService);
        
        // Lấy question có id = 1 từ DB
        Optional<Question> questionOpt = questionService.getQuestionById(1L);
        Assert.assertTrue("Question không tồn tại!", questionOpt.isPresent());
        
        // Gọi API xóa question
        ResponseEntity<String> response = restTemplate.exchange(
            getRootUrl() + "/questions/1/deleted/true",
            HttpMethod.GET,
            new HttpEntity<>(new HttpHeaders()),
            String.class
        );
        
        // Kiểm tra kết quả
        Assert.assertEquals(204, response.getStatusCodeValue());
        
        // Kiểm tra question đã bị xóa
        Optional<Question> deletedQuestion = questionService.getQuestionById(1L);
        Assert.assertTrue("Question vẫn còn tồn tại!", deletedQuestion.isPresent());
        Assert.assertTrue("Question chưa được đánh dấu là đã xóa!", deletedQuestion.get().isDeleted());
    }
 }
// @Test
// public void testGetQuestionById_ValidId() throws Exception {
//     // Arrange
//     Long validId = 1L;
//     Question mockQuestion = new Question();
//     mockQuestion.setId(validId);
//     mockQuestion.setQuestionText("Sample Question");
//     Mockito.when(questionService.getQuestionById(validId)).thenReturn(Optional.of(mockQuestion));

//     // Act
//     ResponseEntity<?> response = questionController.getQuestionById(validId);

//     // Assert
//     Assert.assertEquals(HttpStatus.OK, response.getStatusCode());
//     Assert.assertTrue(response.getBody() instanceof Question);
//     Question returnedQuestion = (Question) response.getBody();
//     Assert.assertEquals(validId, returnedQuestion.getId());
//     Assert.assertEquals("Sample Question", returnedQuestion.getQuestionText());
// }

// @Test
// public void testGetQuestionById_InvalidId() throws Exception {
//     // Arrange
//     Long invalidId = 999L;
//     Mockito.when(questionService.getQuestionById(invalidId)).thenReturn(Optional.empty());

//     // Act
//     ResponseEntity<?> response = questionController.getQuestionById(invalidId);

//     // Assert
//     Assert.assertEquals(HttpStatus.OK, response.getStatusCode());
//     Assert.assertTrue(response.getBody() instanceof ServiceResult);
//     ServiceResult serviceResult = (ServiceResult) response.getBody();
//     Assert.assertEquals(404, serviceResult.getStatusCode());
//     Assert.assertEquals("Not found with id: " + invalidId, serviceResult.getMessage());
//     Assert.assertNull(serviceResult.getData());
// }
