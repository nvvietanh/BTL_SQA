package com.thanhtam.backend.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.thanhtam.backend.config.JwtUtils;
import com.thanhtam.backend.dto.LoginUser;
import com.thanhtam.backend.dto.OperationStatusDto;
import com.thanhtam.backend.dto.PasswordResetDto;
import com.thanhtam.backend.dto.PasswordResetRequest;
import com.thanhtam.backend.payload.response.JwtResponse;
import com.thanhtam.backend.service.UserService;
import com.thanhtam.backend.ultilities.RequestOperationName;
import com.thanhtam.backend.ultilities.RequestOperationStatus;

import org.checkerframework.checker.units.qual.A;
import org.junit.Assert;
import org.junit.Before;
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

    @Autowired
    JwtUtils jwtUtils;

    @Autowired
    private AuthenticationManager authenticationManager;
    
    @Autowired
    private UserService userService;

    @Autowired   
    private AuthenticationController authenticationController;

    String rspwToken;

    @Before
    public void setUp() {
        authenticationController = new AuthenticationController(jwtUtils, authenticationManager, userService);
        rspwToken = "eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiIzNDkiLCJleHAiOjE3NDc0MDI1Mjl9.OF0ZwisjNtRJE4jmdpVE0IRMMeLwZ2DiSEXhiZYEDSK0Jpb-ftiOSSCnRU94lbWlxMOlRATMMF9XB29O-qLtoQ";
    }

    private String getRootUrl() {
        return "/api/auth";
    }    

    @Test
    public void testAuthenticateUser_Success() {
        LoginUser loginUser = new LoginUser();
        loginUser.setUsername("thanhtam28ss");
        loginUser.setPassword("Abcd@12345");

        ResponseEntity<?> response = authenticationController.authenticateUser(loginUser);

        Assert.assertEquals(HttpStatus.OK, response.getStatusCode());
        Assert.assertTrue(response.getBody() instanceof JwtResponse);
        JwtResponse jwtResponse = (JwtResponse) response.getBody();
        Assert.assertNotNull(jwtResponse.getAccessToken());
        Assert.assertTrue(jwtResponse.getAccessToken().length() > 0);
        Assert.assertEquals("thanhtam28ss", jwtResponse.getUsername());
    }

    @Test
    public void testAuthenticateUser_UserNotFound() {
        LoginUser loginUser = new LoginUser();
        loginUser.setUsername("not_exist_user_99999");
        loginUser.setPassword("123456789");

        ResponseEntity<?> response = authenticationController.authenticateUser(loginUser);

        Assert.assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    public void testAuthenticateUser_UserDeleted() {
        // Đảm bảo có user bị xóa (isDeleted = true) trong DB, ví dụ: username = "deleted_user"
        LoginUser loginUser = new LoginUser();
        loginUser.setUsername("1624801040051"); // user có deleted = 1 trong CSDL
        loginUser.setPassword("Abcd@12345");

        ResponseEntity<?> response = authenticationController.authenticateUser(loginUser);

        Assert.assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    public void testAuthenticateUser_WrongPassword() {
        LoginUser loginUser = new LoginUser();
        loginUser.setUsername("thanhtam28ss");
        loginUser.setPassword("wrong_password_9991");

        try {
            authenticationController.authenticateUser(loginUser);
            Assert.fail("Phải ném ra exception khi mật khẩu sai");
        } catch (Exception ex) {
            // Có thể là BadCredentialsException hoặc AuthenticationException
            Assert.assertTrue(ex instanceof org.springframework.security.core.AuthenticationException
                    || ex instanceof RuntimeException);
        }
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
        // Email này phải tồn tại trong hệ thống
        PasswordResetRequest requestDto = new PasswordResetRequest();
        requestDto.setEmail("vietanh.caothu@gmail.com");

        OperationStatusDto result = authenticationController.resetPasswordRequest(requestDto);

        Assert.assertEquals(RequestOperationName.REQUEST_PASSWORD_RESET.name(), result.getOperationName());
        Assert.assertEquals(RequestOperationStatus.SUCCESS.name(), result.getOperationResult());
    }

    @Test
    public void testResetPasswordRequest_Fail_EmailNotExist() throws Exception {
        // Email không tồn tại trong hệ thống
        PasswordResetRequest requestDto = new PasswordResetRequest();
        requestDto.setEmail("not_exist_email_9999@gmail.com");

        OperationStatusDto result = authenticationController.resetPasswordRequest(requestDto);

        Assert.assertEquals(RequestOperationName.REQUEST_PASSWORD_RESET.name(), result.getOperationName());
        Assert.assertEquals(RequestOperationStatus.ERROR.name(), result.getOperationResult());
    }

    @Test
    public void testResetPasswordRequest_Fail_EmailNull() throws Exception {
        // Email null
        PasswordResetRequest requestDto = new PasswordResetRequest();
        requestDto.setEmail(null);

        OperationStatusDto result = authenticationController.resetPasswordRequest(requestDto);

        Assert.assertEquals(RequestOperationName.REQUEST_PASSWORD_RESET.name(), result.getOperationName());
        Assert.assertEquals(RequestOperationStatus.ERROR.name(), result.getOperationResult());
    }

    @Test
    public void testResetPasswordRequest_Fail_EmailEmpty() throws Exception {
        // Email rỗng
        PasswordResetRequest requestDto = new PasswordResetRequest();
        requestDto.setEmail("");

        OperationStatusDto result = authenticationController.resetPasswordRequest(requestDto);

        Assert.assertEquals(RequestOperationName.REQUEST_PASSWORD_RESET.name(), result.getOperationName());
        Assert.assertEquals(RequestOperationStatus.ERROR.name(), result.getOperationResult());
    }

    @Test
    public void testResetPassword_Success() {
        // Token hợp lệ phải tồn tại trong DB, bạn cần lấy token này từ DB hoặc tạo trước khi test
        String validToken = rspwToken;
        PasswordResetDto dto = new PasswordResetDto();
        dto.setToken(validToken);
        dto.setPassword("NewPassword@123999");

        OperationStatusDto result = authenticationController.resetPassword(dto);

        Assert.assertEquals(RequestOperationName.PASSWORD_RESET.name(), result.getOperationName());
        Assert.assertEquals(RequestOperationStatus.SUCCESS.name(), result.getOperationResult());
    }

    @Test
    public void testResetPassword_Fail_InvalidToken() {
        PasswordResetDto dto = new PasswordResetDto();
        dto.setToken("invalid_or_expired_token");
        dto.setPassword("NewPassword@123");

        OperationStatusDto result = authenticationController.resetPassword(dto);

        Assert.assertEquals(RequestOperationName.PASSWORD_RESET.name(), result.getOperationName());
        Assert.assertEquals(RequestOperationStatus.ERROR.name(), result.getOperationResult());
    }

    @Test
    public void testResetPassword_Fail_NullToken() {
        PasswordResetDto dto = new PasswordResetDto();
        dto.setToken(null);
        dto.setPassword("NewPassword@123");

        OperationStatusDto result = authenticationController.resetPassword(dto);

        Assert.assertEquals(RequestOperationName.PASSWORD_RESET.name(), result.getOperationName());
        Assert.assertEquals(RequestOperationStatus.ERROR.name(), result.getOperationResult());
    }

    @Test
    public void testResetPassword_Fail_EmptyToken() {
        PasswordResetDto dto = new PasswordResetDto();
        dto.setToken("");
        dto.setPassword("NewPassword@123");

        OperationStatusDto result = authenticationController.resetPassword(dto);

        Assert.assertEquals(RequestOperationName.PASSWORD_RESET.name(), result.getOperationName());
        Assert.assertEquals(RequestOperationStatus.ERROR.name(), result.getOperationResult());
    }

    @Test
    public void testResetPassword_Fail_NullPassword() {
        PasswordResetDto dto = new PasswordResetDto();
        dto.setToken(rspwToken);
        dto.setPassword(null);

        OperationStatusDto result = authenticationController.resetPassword(dto);

        Assert.assertEquals(RequestOperationName.PASSWORD_RESET.name(), result.getOperationName());
        Assert.assertEquals(RequestOperationStatus.ERROR.name(), result.getOperationResult());
    }

    @Test
    public void testResetPassword_Fail_EmptyPassword() {
        PasswordResetDto dto = new PasswordResetDto();
        dto.setToken(rspwToken);
        dto.setPassword("");

        OperationStatusDto result = authenticationController.resetPassword(dto);

        Assert.assertEquals(RequestOperationName.PASSWORD_RESET.name(), result.getOperationName());
        Assert.assertEquals(RequestOperationStatus.ERROR.name(), result.getOperationResult());
    }

//     @Test
//     @Transactional
//     @Rollback
//     public void testResetPassword_Success_DirectCall() {
//         PasswordResetDto dto = new PasswordResetDto();
//         dto.setToken("eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiIzNDkiLCJleHAiOjE3NDczNDYzMzZ9.LafURu6yp3Ug92MPt-_Rp4SSsm91C1NWv5NRs0dZq6eukgLGQtTjkqpA0riZlT4ZVwYNCjbMCbMKPbg0HmXPRQ");
//         dto.setPassword("Abcd@12345");

// //        AuthenticationController controller = new AuthenticationController(jwtUtils, authenticationManager, userService);
//         // OperationStatusDto result = authController.resetPassword(dto);

//         // Assert.assertEquals("PASSWORD_RESET", result.getOperationName());
//         // Assert.assertEquals("SUCCESS", result.getOperationResult());
//     }

}
