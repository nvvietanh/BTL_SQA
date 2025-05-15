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
import com.thanhtam.backend.service.RoleService;
import com.thanhtam.backend.service.UserService;
import com.thanhtam.backend.ultilities.DifficultyLevel;
import com.thanhtam.backend.ultilities.EQTypeCode;
import com.thanhtam.backend.ultilities.ERole;

import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.jupiter.api.BeforeAll;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Transactional
@Rollback
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

    @Autowired
    private UserService userService;

    @Autowired
    private RoleService roleService;


    private QuestionController questionController;

    private static String adminToken;
    private static String lecturerToken;
    private static String studentToken;

    private String getRootUrl() {
        return "http://localhost:" + port + "/api";
    }

    // @Test
    @Before
    public void setUp() {

        // userService = Mockito.mock(UserService.class);

        questionController = new QuestionController(
            questionService, partService, questionTypeService, userService, roleService
        );
    }

    // private String getToken() {
    //     return "eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJ0aGFuaHRhbTI4c3MiLCJpYXQiOjE3NDQyOTQ4MTIsImV4cCI6MTc0NDM4MTIxMiwicm9sZSI6W3siYXV0aG9yaXR5IjoiUk9MRV9BRE1JTiJ9XX0.dlO69r0W_COp4s0Y1zZ1e4cGUE-56KPuAW5WeebauMCzT0Q8Ct3tXw_flMBuSQuqIRQ4tcy_JNju-wjL5X5F0w";
    // }

//    @BeforeClass
//    @BeforeAll
//    @Before
//    public void setupToken() throws IOException {
//        // Tạo payload cho API đăng nhập role admin
//        String loginPayloadAdmin = "{ \"username\": \"thanhtam28ss\", \"password\": \"Abcd@12345\" }";
//
//        // Tạo header với Content-Type
//        HttpHeaders headers = new HttpHeaders();
//        headers.set("Content-Type", "application/json");
//
//        // Tạo request entity
//        HttpEntity<String> requestAdmin = new HttpEntity<>(loginPayloadAdmin, headers);
//
//        // Gọi API đăng nhập cho admin
//        ResponseEntity<String> responseAdmin = restTemplate.postForEntity(
//                getRootUrl() + "/auth/signin", // Đường dẫn API đăng nhập
//                requestAdmin,
//                String.class
//        );
//
//        // Kiểm tra trạng thái HTTP
//        if (responseAdmin.getStatusCodeValue() != 200) {
//            throw new RuntimeException("Đăng nhập admin thất bại với mã trạng thái: " + responseAdmin.getStatusCodeValue());
//        }
//
//        // Parse token từ response role admin
//        String responseBodyAdmin = responseAdmin.getBody();
//        adminToken = objectMapper.readTree(responseBodyAdmin).get("accessToken").asText();
//
//        // Tạo payload cho API đăng nhập role lecturer
//        String loginPayloadLecturer = "{ \"username\": \"tamht298\", \"password\": \"Abcd@12345\" }";
//        HttpEntity<String> requestLecturer = new HttpEntity<>(loginPayloadLecturer, headers);
//
//        // Gọi API đăng nhập cho lecturer
//        ResponseEntity<String> responseLecturer = restTemplate.postForEntity(
//                getRootUrl() + "/auth/signin",
//                requestLecturer,
//                String.class
//        );
//
//        // Kiểm tra trạng thái HTTP
//        if (responseLecturer.getStatusCodeValue() != 200) {
//            throw new RuntimeException("Đăng nhập lecturer thất bại với mã trạng thái: " + responseLecturer.getStatusCodeValue());
//        }
//
//        // Parse token từ response role lecturer
//        String responseBodyLecturer = responseLecturer.getBody();
//        lecturerToken = objectMapper.readTree(responseBodyLecturer).get("accessToken").asText();
//
//        // Tạo payload cho API đăng nhập role student
//        String loginPayloadStudent = "{ \"username\": \"nvvanh\", \"password\": \"Abcd@12345\" }";
//        HttpEntity<String> requestStudent = new HttpEntity<>(loginPayloadStudent, headers);
//
//        // Gọi API đăng nhập cho student
//        ResponseEntity<String> responseStudent = restTemplate.postForEntity(
//                getRootUrl() + "/auth/signin",
//                requestStudent,
//                String.class
//        );
//
//        // Kiểm tra trạng thái HTTP
//        if (responseStudent.getStatusCodeValue() != 200) {
//            throw new RuntimeException("Đăng nhập student thất bại với mã trạng thái: " + responseStudent.getStatusCodeValue());
//        }
//
//        // Parse token từ response role student
//        String responseBodyStudent = responseStudent.getBody();
//        studentToken = objectMapper.readTree(responseBodyStudent).get("accessToken").asText();
//
//        // In ra token để kiểm tra
//        System.out.println("Admin Token: " + adminToken);
//        System.out.println("Lecturer Token: " + lecturerToken);
//        System.out.println("Student Token: " + studentToken);
//    }

    public void setUserAuthentication(String username, String role) {
        // Tạo một đối tượng Authentication với thông tin người dùng
        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
            username,
            null,
            Collections.singletonList(new SimpleGrantedAuthority(role))
        );

        // testUser = userRepository.findByUsername("username");

        // Đặt đối tượng Authentication vào SecurityContextHolder
        SecurityContextHolder.getContext().setAuthentication(authentication);;
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

        questionController = new QuestionController(
            questionService, partService, questionTypeService, userService, roleService
        );

        ResponseEntity<ServiceResult> response = questionController.getAllQuestion();

        Assert.assertEquals(200, response.getStatusCodeValue());
        ServiceResult serviceResult = response.getBody();
        Assert.assertNotNull(serviceResult);
        Assert.assertEquals(200, serviceResult.getStatusCode());
        Assert.assertEquals("Get question bank successfully!", serviceResult.getMessage());

        List<Question> questions = (List<Question>) serviceResult.getData();
        Assert.assertNotNull("Danh sách questions không được null!", questions);
        Assert.assertFalse("Danh sách questions không được rỗng!", questions.isEmpty());
    }

    @Test
    public void testGetQuestionById_Success() throws IOException {
        // Lấy một id hợp lệ từ DB
        List<Question> allQuestions = questionService.getQuestionList();
        Assert.assertFalse(allQuestions.isEmpty());
        Long validId = allQuestions.get(0).getId();

        ResponseEntity<?> response = questionController.getQuestionById(validId);

        Assert.assertEquals(200, response.getStatusCodeValue());
        Assert.assertTrue(response.getBody() instanceof Question);
        Question question = (Question) response.getBody();
        Assert.assertEquals(validId, question.getId());
    }

    @Test
    public void testGetQuestionById_NotFound() throws IOException {
        Long invalidId = 9999L;
        ResponseEntity<?> response = questionController.getQuestionById(invalidId);

//        Assert.assertEquals(200, response.getStatusCodeValue());
//        Assert.assertTrue(response.getBody() instanceof ServiceResult);
        ServiceResult serviceResult = (ServiceResult) response.getBody();
        Assert.assertEquals(404, serviceResult.getStatusCode());
        Assert.assertEquals("Not found with id: " + invalidId, serviceResult.getMessage());
        Assert.assertNull(serviceResult.getData());
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
        Pageable pageable = PageRequest.of(0, 10);
        Page<Question> page = new PageImpl<>(Arrays.asList(new Question(), new Question()));

        String adminUsername = "thanhtam28ss"; // Đổi thành username admin thực tế
        setUserAuthentication(adminUsername, "ROLE_ADMIN");

        PageResult result = questionController.getQuestionsByPart(pageable, 0L);
        Assert.assertNotNull(result);
        System.out.println(result.getData().getClass());
        Assert.assertTrue(result.getData() instanceof List);
        Assert.assertEquals(2, result.getData().size());
    }

    @Test
    public void testGetQuestionsByPart_Lecturer_AllParts() {
        // partId = 0, isAdmin = false
        Pageable pageable = PageRequest.of(0, 10);
        Page<Question> page = new PageImpl<>(Collections.singletonList(new Question()));

        String lecturerUsername = "tamht298"; // Đổi thành username lecturer thực tế
        setUserAuthentication(lecturerUsername, "ROLE_LECTURER");

        PageResult result = questionController.getQuestionsByPart(pageable, 0L);
        Assert.assertNotNull(result);
        Assert.assertTrue(result.getData() instanceof Page);
        Assert.assertEquals(1, ((Page<?>) result.getData()).getTotalElements());
    }

    @Test
    public void testGetQuestionsByPart_Admin_SpecificPart() {
        // partId != 0, isAdmin = true
        Pageable pageable = PageRequest.of(0, 10);

        String adminUsername = "thanhtam28ss";
        setUserAuthentication(adminUsername, "ROLE_ADMIN");

        PageResult result = questionController.getQuestionsByPart(pageable, 1L);
        Assert.assertNotNull(result);
        Assert.assertTrue(result.getData() instanceof List);
        Assert.assertTrue(((List<?>) result.getData()).size() > 0);
    }

    @Test
    public void testGetQuestionsByPart_Lecturer_SpecificPart() {
        // partId != 0, isAdmin = false
        Pageable pageable = PageRequest.of(0, 10);

        String lecturerUsername = "tamht298";
        setUserAuthentication(lecturerUsername, "ROLE_LECTURER");

        PageResult result = questionController.getQuestionsByPart(pageable, 1L);
        Assert.assertNotNull(result);
        Assert.assertTrue(result.getData() instanceof List);
        Assert.assertTrue(((List<?>) result.getData()).size() > 0);
    }

    @Test(expected = java.util.NoSuchElementException.class)
    public void testGetQuestionsByPart_Admin_SpecificPart_NotFound() {
        // partId != 0, isAdmin = true, part không tồn tại
        Pageable pageable = PageRequest.of(0, 10);

        String adminUsername = "thanhtam28ss";
        setUserAuthentication(adminUsername, "ROLE_ADMIN");

        PageResult results = questionController.getQuestionsByPart(pageable, 99999L);

        // Mong đợi không null nhưng không có câu hỏi nào
        Assert.assertNotNull(results);
        Assert.assertTrue(results.getData() instanceof List);
        Assert.assertTrue(((List<?>) results.getData()).isEmpty());

    }

    @Test(expected = java.util.NoSuchElementException.class)
    public void testGetQuestionsByPart_Lecturer_SpecificPart_NotFound() {
        // partId != 0, isAdmin = true, part không tồn tại
        Pageable pageable = PageRequest.of(0, 10);

        String lecturerUsername = "tamht298";
        setUserAuthentication(lecturerUsername, "ROLE_LECTURER");

        PageResult results = questionController.getQuestionsByPart(pageable, 99999L);

        // Mong đợi không null nhưng không có câu hỏi nào
        Assert.assertNotNull(results);
        Assert.assertTrue(results.getData() instanceof List);
        Assert.assertTrue(((List<?>) results.getData()).isEmpty());

    }

    @Test
    public void testGetQuestionsByPartNotDeleted_Admin() {
        // Đăng nhập admin (giả lập context nếu cần)
        String adminUsername = "thanhtam28ss"; // Đổi thành username admin thực tế
        // userService.setUserNameForTest(adminUsername);
        setUserAuthentication(adminUsername, "ROLE_ADMIN");

        Long partId = 1L; // Thay thế bằng ID của part hợp lệ trong DB

        Pageable pageable = PageRequest.of(0, 10);
        PageResult result = questionController.getQuestionsByPartNotDeleted(pageable, partId);

        Assert.assertNotNull(result);
        Assert.assertTrue(result.getData() instanceof org.springframework.data.domain.Page);
        List<Object> page = (List<Object>) result.getData();
        // Tất cả question phải có deleted=false
        page.forEach(q -> Assert.assertFalse(((Question) q).isDeleted()));
    }

    @Test
    public void testGetQuestionsByPartNotDeleted_Lecturer() {
        // Đăng nhập lecturer (giả lập context nếu cần)
        String lecturerUsername = "tamht298"; // Đổi thành username lecturer thực tế
        setUserAuthentication(lecturerUsername, "ROLE_LECTURER");

        // Lấy part hợp lệ
        // List<Part> parts = partService.getAllParts();
        // Assert.assertFalse("Không có part nào trong DB!", parts.isEmpty());
        // Long partId = parts.get(0).getId();
        Long partId = 75L; // Thay thế bằng ID của part hợp lệ trong DB

        Pageable pageable = PageRequest.of(0, 10);
        PageResult result = questionController.getQuestionsByPartNotDeleted(pageable, partId);

        Assert.assertNotNull(result);
        Assert.assertTrue(result.getData() instanceof org.springframework.data.domain.Page);
        List<Object> page = (List<Object>) result.getData();
        // Tất cả question phải có deleted=false và do lecturer tạo
        page.forEach(q -> {
            Assert.assertFalse(((Question) q).isDeleted());
            Assert.assertEquals(lecturerUsername, ((Question) q).getCreatedBy().getUsername());
        });
    }

    @Test
    public void testGetQuestionsByPartNotDeleted_Admin_InvalidPart() {
        // Đăng nhập admin
        String adminUsername = "thanhtam28ss";
        setUserAuthentication(adminUsername, "ROLE_ADMIN");

        Long invalidPartId = 998999L;
        Pageable pageable = PageRequest.of(0, 10);

        try {
            questionController.getQuestionsByPartNotDeleted(pageable, invalidPartId);
            Assert.fail("Phải ném ra NoSuchElementException khi partId không tồn tại!");
        } catch (Exception ex) {
            Assert.assertTrue(ex instanceof java.util.NoSuchElementException);
        }
    }

    @Test
public void testGetQuestionByQuestionType_Success() {
    // Lấy một questionType hợp lệ từ DB
    List<QuestionType> types = questionTypeService.getQuestionTypeList();
    Assert.assertFalse("Không có question type nào trong DB!", types.isEmpty());
    Long validTypeId = types.get(0).getId();

    ResponseEntity<?> response = questionController.getQuestionByQuestionType(validTypeId);

    Assert.assertEquals(200, response.getStatusCodeValue());
    Assert.assertTrue(response.getBody() instanceof ServiceResult);
    ServiceResult result = (ServiceResult) response.getBody();
    Assert.assertEquals(200, result.getStatusCode());
    Assert.assertTrue(result.getMessage().contains("Get question list with question type id: " + validTypeId));
    Assert.assertNotNull(result.getData());
    List<Object> questions = (List<Object>) result.getData();
    // Có thể kiểm tra thêm: tất cả question đều có questionType đúng
    questions.forEach(q -> Assert.assertEquals(validTypeId, ((Question) q).getQuestionType().getId()));
}

    @Test
    public void testGetQuestionByQuestionType_NotFound() {
        // ID không tồn tại
        Long invalidTypeId = 968999L;
        ResponseEntity<?> response = questionController.getQuestionByQuestionType(invalidTypeId);

        Assert.assertEquals(200, response.getStatusCodeValue());
        Assert.assertTrue(response.getBody() instanceof ServiceResult);
        ServiceResult result = (ServiceResult) response.getBody();
        Assert.assertEquals(404, result.getStatusCode());
        Assert.assertEquals("Not found question type with id: " + invalidTypeId, result.getMessage());
        Assert.assertNull(result.getData());
    }

    @Test
    @Transactional
    @Rollback
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
//        ResponseEntity<String> response = restTemplate.exchange(
//            getRootUrl() + "/questions?questionType=MULTIPLE_CHOICE&partId=1",
//            HttpMethod.POST,
//            new HttpEntity<>(question, new HttpHeaders()),
//            String.class
//        );
//
//        // Kiểm tra kết quả
//        Assert.assertEquals(200, response.getStatusCodeValue());
//
//        // Parse response body
        Question createdQuestion = questionController.createQuestion(question, "MC", 1L);

        Assert.assertNotNull("Question không được null!", createdQuestion);
        Assert.assertEquals("Test Question", createdQuestion.getQuestionText());
        Assert.assertEquals(DifficultyLevel.EASY, createdQuestion.getDifficultyLevel());
        Assert.assertEquals(5, createdQuestion.getPoint()); // EASY = 5 points
        Assert.assertEquals(1L, createdQuestion.getPart().getId().longValue());
        Assert.assertEquals(EQTypeCode.MC, createdQuestion.getQuestionType().getTypeCode());
    }

//     @Test
//     public void testUpdateQuestion_Success() throws IOException {
//         // Kiểm tra service không null
//         Assert.assertNotNull("QuestionService bị null!", questionService);

//         // Lấy question có id = 1 từ DB
//         Optional<Question> questionOpt = questionService.getQuestionById(8L);
//         Assert.assertTrue("Question không tồn tại!", questionOpt.isPresent());

//         // Tạo question mới để update
//         Question question = new Question();
//         question.setQuestionText("Updated Question");
//         question.setDifficultyLevel(DifficultyLevel.MEDIUM);

//         // Gọi API update question
// //        ResponseEntity<String> response = restTemplate.exchange(
// //            getRootUrl() + "/questions/1",
// //            HttpMethod.PUT,
// //            new HttpEntity<>(question, new HttpHeaders()),
// //            String.class
// //        );
// //
// //        // Kiểm tra kết quả
// //        Assert.assertEquals(200, response.getStatusCodeValue());
// //
// //        // Parse response body
// //        ServiceResult serviceResult = objectMapper.readValue(response.getBody(), ServiceResult.class);
// //        Assert.assertEquals(200, serviceResult.getStatusCode());
// //        Assert.assertEquals("Get question with id: 1", serviceResult.getMessage());

//         // Kiểm tra dữ liệu
// //        Question updatedQuestion = objectMapper.convertValue(serviceResult.getData(), Question.class);

//         ResponseEntity

//         Assert.assertNotNull("Question không được null!", updatedQuestion);
//         Assert.assertEquals("Updated Question", updatedQuestion.getQuestionText());
//         Assert.assertEquals(DifficultyLevel.MEDIUM, updatedQuestion.getDifficultyLevel());
//         Assert.assertEquals(10, updatedQuestion.getPoint()); // MEDIUM = 10 points
//     }

    @Test
    @Transactional
    @Rollback
    public void testUpdateQuestion_Success() {
        // Lấy một question hợp lệ từ DB
        List<Question> questions = questionService.getQuestionList();
        Assert.assertFalse("Không có câu hỏi nào trong DB!", questions.isEmpty());
        Question question = questions.get(0);

        String oldText = question.getQuestionText();
        question.setQuestionText("Câu hỏi đã cập nhật");

        ResponseEntity<?> response = questionController.updateQuestion(question, question.getId());

        // Kiểm tra status code và kiểu dữ liệu trả về có phải là ServiceResult không
        Assert.assertEquals(200, response.getStatusCodeValue());
        Assert.assertTrue(response.getBody() instanceof ServiceResult);

        // Kiểm tra nội dung của ServiceResult
        ServiceResult result = (ServiceResult) response.getBody();
        Assert.assertEquals(200, result.getStatusCode());
        Assert.assertEquals("Get question with id: " + question.getId(), result.getMessage());
        Question updated = (Question) result.getData();
        Assert.assertEquals("Câu hỏi đã cập nhật", updated.getQuestionText());

        // Khôi phục lại dữ liệu cũ
        question.setQuestionText(oldText);
        questionService.save(question);
    }

    @Test
    public void testUpdateQuestion_NotFound() {
        // ID không tồn tại
        Long invalidId = 99999L;
        Question question = new Question();
        question.setQuestionText("Không tồn tại");

        ResponseEntity<?> response = questionController.updateQuestion(question, invalidId);

        Assert.assertEquals(200, response.getStatusCodeValue());
        Assert.assertTrue(response.getBody() instanceof ServiceResult);
        ServiceResult result = (ServiceResult) response.getBody();
        Assert.assertEquals(404, result.getStatusCode());
        Assert.assertEquals("Not found with id: " + invalidId, result.getMessage());
        Assert.assertNull(result.getData());
    }

    @Test
    public void testUpdateQuestion_NullQuestion() {
        // Truyền vào question null
        Long validId = questionService.getQuestionList().get(0).getId();
        try {
            questionController.updateQuestion(null, validId);
            Assert.fail("Phải ném ra NullPointerException hoặc IllegalArgumentException");
        } catch (Exception ex) {
            Assert.assertTrue(ex instanceof NullPointerException || ex instanceof IllegalArgumentException);
        }
    }

    @Test
    public void testUpdateQuestion_EmptyText() {
        // Cập nhật với questionText rỗng
        Question question = questionService.getQuestionList().get(0);
        question.setQuestionText("");

        ResponseEntity<?> response = questionController.updateQuestion(question, question.getId());

        Assert.assertEquals(200, response.getStatusCodeValue());
        Assert.assertTrue(response.getBody() instanceof ServiceResult);
        ServiceResult result = (ServiceResult) response.getBody();
        Assert.assertEquals(200, result.getStatusCode());
        Question updated = (Question) result.getData();
        Assert.assertEquals("", updated.getQuestionText());
    }

    @Test
    public void testDeleteQuestion_Success() {
        // Kiểm tra service không null
        Assert.assertNotNull("QuestionService bị null!", questionService);

        // Gọi API xóa question id = 9L
        ResponseEntity<?> response = questionController.deleteTempQuestion(9L, false);

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
