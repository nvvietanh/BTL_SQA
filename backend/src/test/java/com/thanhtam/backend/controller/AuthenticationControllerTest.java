package com.thanhtam.backend.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.thanhtam.backend.config.JwtUtils;
import com.thanhtam.backend.dto.LoginUser;
import com.thanhtam.backend.dto.OperationStatusDto;
import com.thanhtam.backend.dto.PasswordResetDto;
import com.thanhtam.backend.dto.PasswordResetRequest;
import com.thanhtam.backend.payload.response.JwtResponse;
import com.thanhtam.backend.service.UserService;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.http.*;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Transactional
@Rollback
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
public class AuthenticationControllerTest {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private ObjectMapper objectMapper;
    private Logger logger = LoggerFactory.getLogger(AuthenticationController.class);

    JwtUtils jwtUtils;

    private AuthenticationManager authenticationManager;
    @Autowired
    private UserService userService;

    @Autowired
    

    private String adminToken, lecturerToken, studentToken;

    public void setupAdminToken() throws Exception {
        // Tạo payload cho API đăng nhập role admin
        String loginPayloadAdmin = "{ \"username\": \"thanhtam28ss\", \"password\": \"Abcd@12345\" }";

        // Tạo header với Content-Type
        HttpHeaders headers = new HttpHeaders();
        headers.set("Content-Type", "application/json");

        // Tạo request entity
        HttpEntity<String> requestAdmin = new HttpEntity<>(loginPayloadAdmin, headers);

        // Gọi API đăng nhập cho admin
        ResponseEntity<String> responseAdmin = restTemplate.postForEntity(
                getRootUrl() + "/signin", // Đường dẫn API đăng nhập
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
        System.out.println("Admin token: " + adminToken);
    }

    public void setupLecturerToken() throws Exception {
        // Tạo payload cho API đăng nhập role lecturer
        String loginPayloadLecturer = "{ \"username\": \"tamht298\", \"password\": \"Abcd@12345\" }";

        // Tạo header với Content-Type
        HttpHeaders headers = new HttpHeaders();
        headers.set("Content-Type", "application/json");

        // Tạo request entity
        HttpEntity<String> requestLecturer = new HttpEntity<>(loginPayloadLecturer, headers);

        // Gọi API đăng nhập cho lecturer
        ResponseEntity<String> responseLecturer = restTemplate.postForEntity(
                getRootUrl() + "/signin",
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
        System.out.println("Lecturer token: " + lecturerToken);
    }

    public void setupStudentToken() throws Exception {
        // Tạo payload cho API đăng nhập role student
        String loginPayloadStudent = "{ \"username\": \"nvvanh\", \"password\": \"Abcd@12345\" }";

        // Tạo header với Content-Type
        HttpHeaders headers = new HttpHeaders();
        headers.set("Content-Type", "application/json");

        HttpEntity<String> requestStudent = new HttpEntity<>(loginPayloadStudent, headers);

        // Gọi API đăng nhập cho student
        ResponseEntity<String> responseStudent = restTemplate.postForEntity(
                getRootUrl() + "/signin",
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
        System.out.println("Student token: " + studentToken);
    }

    private String getRootUrl() {
        return "/api/auth";
    }    

    @Test
    public void testAuthenticateUser_Success() throws Exception {
        // Tạo đối tượng user đăng nhập có trong hệ thống và username password là đúng
        LoginUser loginUser = new LoginUser();
        String username = "thanhtam28ss";
        String password = "Abcd@12345";
        loginUser.setUsername(username);
        loginUser.setPassword(password);

        // khởi tạo bản tin http
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<LoginUser> request = new HttpEntity<>(loginUser, headers);

        // gửi request đền endpoint /api/auth và nhận response
        ResponseEntity<String> response = restTemplate.postForEntity(getRootUrl() + "/signin", request, String.class);

        // Mong đợi status code = 200
        Assert.assertEquals(HttpStatus.OK, response.getStatusCode());
        JwtResponse jwtResponse = objectMapper.readValue(response.getBody(), JwtResponse.class);
        // Mong đợi jwt token không null
        Assert.assertNotNull(jwtResponse.getAccessToken());
        // Mong đợi jwt token không rỗng
        Assert.assertTrue(jwtResponse.getAccessToken().length() > 0);
        // Mong đợi username trả về là trùng khớp với username đăng nhập
        Assert.assertEquals(username, jwtResponse.getUsername());
    }

    @Test
    public void testAuthenticateUser_UserNotFound() throws Exception {
        LoginUser loginUser = new LoginUser();
        loginUser.setUsername("not_exist_user_99999");
        loginUser.setPassword("123456789");

        // khởi tạo bản tin http
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<LoginUser> request = new HttpEntity<>(loginUser, headers);

        // gửi request
        ResponseEntity<String> response = restTemplate.postForEntity(getRootUrl() + "/signin", request, String.class);

        // Mong đợi trả trả về 401 hoặc 400
        Assert.assertTrue(
                response.getStatusCode() == HttpStatus.BAD_REQUEST ||
                        response.getStatusCode() == HttpStatus.UNAUTHORIZED);
    }

    @Test
    public void testAuthenticateUser_UserDeleted() throws Exception {
        // Giả sử đã có user bị xóa (isDeleted = true) trong DB, ví dụ: username = "deleted_user"
        LoginUser loginUser = new LoginUser();
        loginUser.setUsername("1624801040051"); // user có deleted = 1 trong CSDL
        loginUser.setPassword("Abcd@12345"); // mật khẩu đúng

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<LoginUser> request = new HttpEntity<>(loginUser, headers);

        ResponseEntity<String> response = restTemplate.postForEntity(getRootUrl() + "/signin", request, String.class);

        // Mong đợi trả về bad request
        Assert.assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        // Mong đợi response body có chứa thông báo rõ ràng
        Assert.assertTrue(
                (response.getBody() != null &&
                        (response.getBody().contains("deleted user") ||
                        response.getBody().contains("người dùng đã xóa")))
        );
    }

    @Test
    public void testAuthenticateUser_WrongPassword() throws Exception {
        LoginUser loginUser = new LoginUser();
        loginUser.setUsername("thanhtam28ss"); // username có tồn tại trong db
        loginUser.setPassword("wrong_password_9991"); // mật khấu sai

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<LoginUser> request = new HttpEntity<>(loginUser, headers);

        ResponseEntity<String> response = restTemplate.postForEntity(getRootUrl() + "/signin", request, String.class);

        // Mong đợi trả trả về 401 hoặc 400
        Assert.assertTrue(
                response.getStatusCode() == HttpStatus.BAD_REQUEST ||
                        response.getStatusCode() == HttpStatus.UNAUTHORIZED);
    }

//    @Test
//    public void testAuthenticateUser_UserDeletedAfterAuth() throws Exception {
//        // Giả sử có user hợp lệ, nhưng sau khi đăng nhập thành công, user bị set isDeleted = true trong DB
//
//        LoginUser loginUser = new LoginUser();
//        loginUser.setUsername("user_deleted_after_auth");
//        loginUser.setPassword("correct_password");
//
//        HttpHeaders headers = new HttpHeaders();
//        headers.setContentType(MediaType.APPLICATION_JSON);
//        HttpEntity<LoginUser> request = new HttpEntity<>(loginUser, headers);
//
//        ResponseEntity<String> response = restTemplate.postForEntity(getRootUrl() + "/signin", request, String.class);
//
//        Assert.assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
//    }



    @Test
    public void testResetPasswordRequest_Success() throws Exception {
        // Email này tồn tại trong hệ thống và có thể gửi mail thành công
        PasswordResetRequest requestDto = new PasswordResetRequest();
        requestDto.setEmail("vietanh.caothu@gmail.com");

        // khởi tạo bản tin http
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<PasswordResetRequest> request = new HttpEntity<>(requestDto, headers);

        // gửi request đến endpoint /api/auth/password-reset-request
        ResponseEntity<String> response = restTemplate.postForEntity(getRootUrl() + "/password-reset-request", request, String.class);

        // Mong đợi status code = 200
        Assert.assertEquals(HttpStatus.OK, response.getStatusCode());
        OperationStatusDto result = objectMapper.readValue(response.getBody(), OperationStatusDto.class);
        // Mong đợi operationName là "REQUEST_PASSWORD_RESET"
        Assert.assertEquals("REQUEST_PASSWORD_RESET", result.getOperationName());
        // Mong đợi operationResult là "SUCCESS"
        Assert.assertEquals("SUCCESS", result.getOperationResult());
    }

    @Test
    public void testResetPasswordRequest_Fail_EmailNotExist() throws Exception {
        // Email không tồn tại trong hệ thống
        PasswordResetRequest requestDto = new PasswordResetRequest();
        requestDto.setEmail("not_exist_email_9999@gmail.com");

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<PasswordResetRequest> request = new HttpEntity<>(requestDto, headers);

        ResponseEntity<String> response = restTemplate.postForEntity(getRootUrl() + "/password-reset-request", request, String.class);

        Assert.assertEquals(HttpStatus.OK, response.getStatusCode());
        OperationStatusDto result = objectMapper.readValue(response.getBody(), OperationStatusDto.class);
        Assert.assertEquals("REQUEST_PASSWORD_RESET", result.getOperationName());
        Assert.assertEquals("ERROR", result.getOperationResult());
    }

    @Test
    public void testResetPasswordRequest_Fail_EmailNull() throws Exception {
        // Email null
        PasswordResetRequest requestDto = new PasswordResetRequest();
        requestDto.setEmail(null);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<PasswordResetRequest> request = new HttpEntity<>(requestDto, headers);

        ResponseEntity<String> response = restTemplate.postForEntity(getRootUrl() + "/password-reset-request", request, String.class);

        System.out.println(response.getStatusCode().value());

        // Mong đợi lỗi 400 do validate fail hoặc ERROR nếu backend không validate
        Assert.assertTrue(
            response.getStatusCode() == HttpStatus.BAD_REQUEST ||
            (response.getStatusCode() == HttpStatus.OK && response.getBody().contains("ERROR"))
        );
    }

    @Test
    public void testResetPasswordRequest_Fail_EmailEmpty() throws Exception {
        // Email rỗng
        PasswordResetRequest requestDto = new PasswordResetRequest();
        requestDto.setEmail("");

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<PasswordResetRequest> request = new HttpEntity<>(requestDto, headers);

        ResponseEntity<String> response = restTemplate.postForEntity(getRootUrl() + "/password-reset-request", request, String.class);

        // Mong đợi lỗi 400 do validate fail hoặc ERROR nếu backend không validate
        Assert.assertTrue(
            response.getStatusCode() == HttpStatus.BAD_REQUEST ||
            (response.getStatusCode() == HttpStatus.OK && response.getBody().contains("ERROR"))
        );
    }

    @Test
    @Transactional
    @Rollback
    public void testResetPasswordRequest_Loop_MultipleRequests() throws Exception {
        // Gửi nhiều request liên tiếp với các email khác nhau
        String[] emails = {
            "vietanh.caothu@gmail.com",
            "not_exist_email_9999@gmail.com",
            "",
            null
        };
        for (String email : emails) {
            PasswordResetRequest requestDto = new PasswordResetRequest();
            requestDto.setEmail(email);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<PasswordResetRequest> request = new HttpEntity<>(requestDto, headers);

            ResponseEntity<String> response = restTemplate.postForEntity(getRootUrl() + "/password-reset-request", request, String.class);

            System.out.println(email);
            System.out.println(response.getStatusCode().value());

            // Chỉ cần không lỗi 500 là được, các trường hợp đã test ở trên
            Assert.assertTrue(
                response.getStatusCode() == HttpStatus.OK ||
                response.getStatusCode() == HttpStatus.BAD_REQUEST
            );
        }
    }

    @Test
    @Transactional
    @Rollback
    public void testResetPassword_Success() throws Exception {

        // tạo token hợp lệ
//        userService.requestPasswordReset("vietanh.caothu@gmail.com");

        // Lấy token từ DB
        String token = "eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiIzNDkiLCJleHAiOjE3NDczNDUyMzJ9.rYD7U5zNATTYVmqukccVEZkcL94pnSOEWp9ffBIVKoKYdp9_rPIHnx-hr68OZAfCz4TaW96s3soVhP6xgAGM_w";

        // Token hợp lệ, password hợp lệ
        PasswordResetDto dto = new PasswordResetDto();
        dto.setToken(token); // Token này phải tồn tại và hợp lệ trong DB
        dto.setPassword("NewPassword@123999");

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<PasswordResetDto> request = new HttpEntity<>(dto, headers);

        ResponseEntity<String> response = restTemplate.postForEntity(getRootUrl() + "/password-reset", request, String.class);

        Assert.assertEquals(HttpStatus.OK, response.getStatusCode());
        OperationStatusDto result = objectMapper.readValue(response.getBody(), OperationStatusDto.class);
        Assert.assertEquals("PASSWORD_RESET", result.getOperationName());
        Assert.assertEquals("SUCCESS", result.getOperationResult());
    }

    @Test
    @Transactional
    @Rollback
    public void testResetPassword_Fail_InvalidToken() throws Exception {
        // Token không tồn tại hoặc đã hết hạn
        PasswordResetDto dto = new PasswordResetDto();
        dto.setToken("invalid_or_expired_token");
        dto.setPassword("NewPassword@123");

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<PasswordResetDto> request = new HttpEntity<>(dto, headers);

        ResponseEntity<String> response = restTemplate.postForEntity(getRootUrl() + "/password-reset", request, String.class);

        Assert.assertEquals(HttpStatus.OK, response.getStatusCode());
        OperationStatusDto result = objectMapper.readValue(response.getBody(), OperationStatusDto.class);
        Assert.assertEquals("PASSWORD_RESET", result.getOperationName());
        Assert.assertEquals("ERROR", result.getOperationResult());
    }

    @Test
    @Transactional
    @Rollback
    public void testResetPassword_Fail_NullToken() throws Exception {
        // Token null
        PasswordResetDto dto = new PasswordResetDto();
        dto.setToken(null);
        dto.setPassword("NewPassword@123");

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<PasswordResetDto> request = new HttpEntity<>(dto, headers);

        ResponseEntity<String> response = restTemplate.postForEntity(getRootUrl() + "/password-reset", request, String.class);

        Assert.assertTrue(
            response.getStatusCode() == HttpStatus.BAD_REQUEST ||
            (response.getStatusCode() == HttpStatus.OK && response.getBody().contains("ERROR"))
        );
    }

    @Test
    @Transactional
    @Rollback
    public void testResetPassword_Fail_EmptyToken() throws Exception {
        // Token rỗng
        PasswordResetDto dto = new PasswordResetDto();
        dto.setToken("");
        dto.setPassword("NewPassword@123");

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<PasswordResetDto> request = new HttpEntity<>(dto, headers);

        ResponseEntity<String> response = restTemplate.postForEntity(getRootUrl() + "/password-reset", request, String.class);

        Assert.assertTrue(
            response.getStatusCode() == HttpStatus.BAD_REQUEST ||
            (response.getStatusCode() == HttpStatus.OK && response.getBody().contains("ERROR"))
        );
    }

    @Test
    @Transactional
    @Rollback
    public void testResetPassword_Fail_NullPassword() throws Exception {
        // Password null
        PasswordResetDto dto = new PasswordResetDto();
        dto.setToken("valid_token_123");
        dto.setPassword(null);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<PasswordResetDto> request = new HttpEntity<>(dto, headers);

        ResponseEntity<String> response = restTemplate.postForEntity(getRootUrl() + "/password-reset", request, String.class);

        Assert.assertTrue(
            response.getStatusCode() == HttpStatus.BAD_REQUEST ||
            (response.getStatusCode() == HttpStatus.OK && response.getBody().contains("ERROR"))
        );
    }

    @Test
    @Transactional
    @Rollback
    public void testResetPassword_Fail_EmptyPassword() throws Exception {
        // Password rỗng
        PasswordResetDto dto = new PasswordResetDto();
        dto.setToken("valid_token_123");
        dto.setPassword("");

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<PasswordResetDto> request = new HttpEntity<>(dto, headers);

        ResponseEntity<String> response = restTemplate.postForEntity(getRootUrl() + "/password-reset", request, String.class);

        Assert.assertTrue(
            response.getStatusCode() == HttpStatus.BAD_REQUEST ||
            (response.getStatusCode() == HttpStatus.OK && response.getBody().contains("ERROR"))
        );
    }

    @Test
    @Transactional
    @Rollback
    public void testResetPassword_Loop_MultipleRequests() throws Exception {
        // Gửi nhiều request liên tiếp với các token và password khác nhau
        String[] tokens = { "valid_token_123", "invalid_token", "", null };
        String[] passwords = { "NewPassword@123", "", null };

        for (String token : tokens) {
            for (String password : passwords) {
                PasswordResetDto dto = new PasswordResetDto();
                dto.setToken(token);
                dto.setPassword(password);

                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.APPLICATION_JSON);
                HttpEntity<PasswordResetDto> request = new HttpEntity<>(dto, headers);

                ResponseEntity<String> response = restTemplate.postForEntity(getRootUrl() + "/password-reset", request, String.class);

                // Không được lỗi 500, các trường hợp đã test ở trên
                Assert.assertTrue(
                    response.getStatusCode() == HttpStatus.OK ||
                    response.getStatusCode() == HttpStatus.BAD_REQUEST
                );
            }
        }
    }

    @Test
    @Transactional
    @Rollback
    public void testResetPassword_Success_DirectCall() {
        PasswordResetDto dto = new PasswordResetDto();
        dto.setToken("eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiIzNDkiLCJleHAiOjE3NDczNDYzMzZ9.LafURu6yp3Ug92MPt-_Rp4SSsm91C1NWv5NRs0dZq6eukgLGQtTjkqpA0riZlT4ZVwYNCjbMCbMKPbg0HmXPRQ");
        dto.setPassword("Abcd@12345");

//        AuthenticationController controller = new AuthenticationController(jwtUtils, authenticationManager, userService);
        // OperationStatusDto result = authController.resetPassword(dto);

        // Assert.assertEquals("PASSWORD_RESET", result.getOperationName());
        // Assert.assertEquals("SUCCESS", result.getOperationResult());
    }

}
