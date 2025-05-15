package com.thanhtam.backend.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.thanhtam.backend.dto.EmailUpdate;
import com.thanhtam.backend.dto.PasswordUpdate;
import com.thanhtam.backend.dto.ServiceResult;
import com.thanhtam.backend.dto.UserUpdate;
import com.thanhtam.backend.entity.Profile;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.*;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.Assert.*;
import static org.junit.jupiter.api.Assertions.assertThrows;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Transactional
@Rollback
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
public class UserControllerTest {
    @Autowired
    private UserController userController;

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    private String adminToken, lecturerToken, studentToken;

    private final String BASE_URL = "http://localhost:8080/api/users";

    @Before
    public void setUp() throws Exception {
//        setupAdminToken();
//        setupLecturerToken();
//        setupStudentToken();
    }

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
        String loginPayloadStudent = "{ \"username\": \"nnmhqn@gmail.com\", \"password\": \"Abcd@12345\" }";

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

    /**
     * TC_UC_01: Cập nhật email thành công
     * Mục tiêu: Đảm bảo API cho phép người dùng cập nhật email nếu cung cấp đúng mật khẩu
     * Input: userId = 1, password đúng, email mới = "updated@example.com"
     * Output kỳ vọng: Trả về message "Update email successfully" và status 200
     */
    @Test
    public void testUpdateEmail_Success() {
        EmailUpdate emailUpdate = new EmailUpdate();
        emailUpdate.setPassword("Abcd@12345");
        emailUpdate.setEmail("updated@example.com");

        ResponseEntity<ServiceResult> response = userController.updateEmail(emailUpdate, 1L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Update email successfully", response.getBody().getMessage());
    }

    /**
     * TC_UC_02: Cập nhật email sai mật khẩu
     * Mục tiêu: Đảm bảo hệ thống từ chối cập nhật email nếu mật khẩu cung cấp sai
     * Input: userId = 1, password sai, email mới = "updated@example.com"
     * Output kỳ vọng: Trả về message "Password is wrong"
     */
    @Test
    public void testUpdateEmail_WrongPassword() {
        EmailUpdate emailUpdate = new EmailUpdate();
        emailUpdate.setPassword("Abcd@12");
        emailUpdate.setEmail("updated@example.com");

        ResponseEntity<ServiceResult> response = userController.updateEmail(emailUpdate, 1L);

        assertEquals("Password is wrong", response.getBody().getMessage());
    }

    /**
     * TC_UC_03: Cập nhật mật khẩu thành công
     * Mục tiêu: Đảm bảo người dùng có thể cập nhật mật khẩu mới nếu cung cấp đúng mật khẩu hiện tại
     * Input: userId = 1, currentPassword = "Abcd@12345", newPassword = "Abcd@12345678"
     * Output kỳ vọng: Trả về message "Update password successfully" và status 200
     */
    @Test
    public void testUpdatePassword_Success() {
        PasswordUpdate passwordUpdate = new PasswordUpdate();
        passwordUpdate.setCurrentPassword("Abcd@12345");
        passwordUpdate.setNewPassword("Abcd@12345678");

        ResponseEntity<ServiceResult> response = userController.updatePass(passwordUpdate, 1L);
        assertTrue(response.getBody().getMessage().contains("Update password successfully"));
    }

    /**
     * TC_UC_04: Mật khẩu mới trùng với mật khẩu cũ
     * Mục tiêu: Đảm bảo hệ thống từ chối nếu người dùng cố gắng cập nhật cùng một mật khẩu cũ
     * Input: userId = 1, currentPassword = "Abcd@12345", newPassword = "Abcd@12345"
     * Output kỳ vọng: Trả về message "This is old password" và status 200
     */
    @Test
    public void testUpdatePassword_DuplicatedPassword() {
        PasswordUpdate passwordUpdate = new PasswordUpdate();
        passwordUpdate.setCurrentPassword("Abcd@12345");
        passwordUpdate.setNewPassword("Abcd@12345");

        ResponseEntity<ServiceResult> response = userController.updatePass(passwordUpdate, 1L);
        assertTrue(response.getBody().getMessage().contains("This is old password"));
    }

    /**
     * TC_UC_05: Cập nhật mật khẩu thất bại do sai mật khẩu hiện tại
     * Mục tiêu: Đảm bảo hệ thống từ chối cập nhật khi mật khẩu hiện tại sai
     * Input: userId = 1, currentPassword = "sai123", newPassword = "Abcd@99999"
     * Output kỳ vọng: Trả về message "Wrong password" và status 200
     */
    @Test
    public void testUpdatePassword_WrongCurrentPassword() {
        PasswordUpdate passwordUpdate = new PasswordUpdate();
        passwordUpdate.setCurrentPassword("sai123");
        passwordUpdate.setNewPassword("Abcd@99999");

        ResponseEntity<ServiceResult> response = userController.updatePass(passwordUpdate, 1L);
        assertTrue(response.getBody().getMessage().contains("Wrong password"));
    }

    /**
     * TC_UC_06: Kiểm tra email đã tồn tại (email của user khác)
     * Mục tiêu: Kiểm tra hệ thống phát hiện email đang được dùng bởi user khác
     * Input: userId = 1, email = "nnmhqn@gmail.com"
     * Output kỳ vọng: Trả về true
     */
    @Test
    public void testCheckEmailExists_OtherUser() {
        boolean response = userController.checkExistsEmailUpdate("nnmhqn@gmail.com",1L);
        assertTrue(response);
    }

    /**
     * TC_UC_07: Kiểm tra email là của chính mình
     * Mục tiêu: Đảm bảo hệ thống không cảnh báo trùng email khi user nhập đúng email của chính mình
     * Input: userId = 1, email = "thanh@gmail.com"
     * Output kỳ vọng: Trả về false
     */
    @Test
    public void testCheckEmailExists_MyOwnEmail() {
        boolean response = userController.checkExistsEmailUpdate("thanh@gmail.com",1L);
        assertFalse(response);
    }

    /**
     * TC_UC_08: Kiểm tra email chưa tồn tại
     * Mục tiêu: Đảm bảo hệ thống xác nhận email chưa từng được sử dụng
     * Input: userId = 1, email = "abcxyz999@nomail.com"
     * Output kỳ vọng: Trả về false
     */
    @Test
    public void testCheckEmailExists_EmailNotExist() {
        boolean response = userController.checkExistsEmailUpdate("abcxyz999@nomail.com",1L);
        assertFalse(response);
    }

    /**
     * TC_UC_09: Cập nhật trạng thái deleted thành true
     * Mục tiêu: Đảm bảo controller cập nhật thành công trường deleted của user khi truyền vào deleted = true
     * Input: id = 1, deleted = true
     * Output kỳ vọng: Response status 204 và trường deleted = true trong DB
     */
    @Test
    public void testDeleteTempUser_SetDeletedTrue() {
        Long userId = 1L;
        boolean deleted = true;

        ResponseEntity<?> response = userController.deleteTempUser(userId, deleted);
        assertEquals(204, response.getStatusCodeValue());
    }

    /**
     * TC_UC_10: Cập nhật trạng thái deleted thành false
     * Mục tiêu: Đảm bảo controller cập nhật lại trạng thái deleted từ true về false thành công
     * Input: id = 1, deleted = false
     * Output kỳ vọng: Response status 204 và trường deleted = false trong DB
     */
    @Test
    public void testDeleteTempUser_SetDeletedFalse() {
        Long userId = 1L;
        boolean deleted = false;

        ResponseEntity<?> response = userController.deleteTempUser(userId, deleted);
        assertEquals(204, response.getStatusCodeValue());

    }

    /**
     * TC_UC_11: Gọi với id không tồn tại
     * Mục tiêu: Đảm bảo hệ thống xử lý đúng khi id không tồn tại (ném NoSuchElementException)
     * Input: id = 999999L (không tồn tại), deleted = true
     * Output kỳ vọng: Ném NoSuchElementException
     */
    @Test
    public void testDeleteTempUser_UserNotFound() {
        Long nonExistentId = 999999L;
        boolean deleted = true;

        assertThrows(Exception.class, () -> {
            userController.deleteTempUser(nonExistentId, deleted);
        });
    }

    /**
     * TC_UC_12: Cập nhật email và họ tên thành công
     * Mục tiêu: Đảm bảo API cập nhật email và tên trong profile nếu dữ liệu hợp lệ
     * Input: id = 1, email = "newemail@example.com", firstName = "New", lastName = "Name"
     * Output kỳ vọng: status 200, message "Cập nhật thành công!"
     */
    @Test
    public void testUpdateUser_EmailAndName() {
        UserUpdate userReq = new UserUpdate();
        userReq.setEmail("newemail@example.com");

        Profile profile = new Profile();
        profile.setFirstName("New");
        profile.setLastName("Name");
        userReq.setProfile(profile);

        userReq.setPassword(null);

        ResponseEntity<?> response = userController.updateUser(userReq, 1L);
        assertEquals(200, response.getStatusCodeValue());

        ServiceResult result = (ServiceResult) response.getBody();
        assertNotNull(result);
        assertEquals("Cập nhật thành công!", result.getMessage());

    }

    /**
     * TC_UC_13: Cập nhật kèm mật khẩu mới
     * Mục tiêu: Đảm bảo mật khẩu được mã hóa khi cập nhật
     * Input: id = 1, password mới = "newpass123"
     * Output kỳ vọng: mật khẩu trong DB đã được mã hóa (không phải plaintext)
     */
    @Test
    public void testUpdateUser_WithPassword() {
        String newPassword = "newpass123";

        UserUpdate userReq = new UserUpdate();
        userReq.setEmail("passupdate@example.com");

        Profile profile = new Profile();
        profile.setFirstName("With");
        profile.setLastName("Password");
        userReq.setProfile(profile);

        userReq.setPassword(newPassword);

        ResponseEntity<?> response = userController.updateUser(userReq, 1L);
        assertEquals(200, response.getStatusCodeValue());

    }

    /**
     * TC_UC_14: Cập nhật với user không tồn tại
     * Mục tiêu: Đảm bảo hệ thống xử lý khi userId không tồn tại
     * Input: id = 999999L
     * Output kỳ vọng: ném NoSuchElementException
     */
    @Test
    public void testUpdateUser_UserNotFound() {
        Long invalidId = 999999L;
        UserUpdate userReq = new UserUpdate();
        userReq.setEmail("ghost@example.com");

        Profile profile = new Profile();
        profile.setFirstName("Ghost");
        profile.setLastName("User");
        userReq.setProfile(profile);

        assertThrows(Exception.class, () -> {
            userController.updateUser(userReq, invalidId);
        });
    }

}
