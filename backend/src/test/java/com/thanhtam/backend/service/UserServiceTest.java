package com.thanhtam.backend.service;

import com.thanhtam.backend.config.JwtUtils;
import com.thanhtam.backend.dto.UserExport;
import com.thanhtam.backend.entity.*;
import com.thanhtam.backend.repository.IntakeRepository;
import com.thanhtam.backend.repository.PasswordResetTokenRepository;
import com.thanhtam.backend.repository.UserRepository;
import com.thanhtam.backend.ultilities.ERole;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

import static org.junit.Assert.*;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Transactional
@Rollback
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
public class UserServiceTest {
    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleService roleService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private IntakeRepository intakeRepository;

    @Autowired
    private PasswordResetTokenRepository passwordResetTokenRepository;

    private User userActive;
    private User userDeleted;

    @Before
    public void setUp() {
        // Tạo Role
        Role role = new Role();
        role.setName(ERole.ROLE_STUDENT); // Giả sử bạn có enum ERole
        Set<Role> roles = new HashSet<>();
        roles.add(role);

        // Tạo Profile
        Profile profile1 = new Profile();
        profile1.setFirstName("Test");
        profile1.setLastName("User");
        profile1.setImage("avatar1.jpg");

        Profile profile2 = new Profile();
        profile2.setFirstName("Deleted");
        profile2.setLastName("User");
        profile2.setImage("avatar2.jpg");

        // Tạo user đang hoạt động
        userActive = new User();
        userActive.setUsername("testuser");
        userActive.setEmail("testuser@example.com");
        userActive.setDeleted(false);
        userActive.setRoles(roles);
        userActive.setProfile(profile1);
        userActive.setIntake(null);

        userActive = userRepository.save(userActive);

        // Tạo user đã xóa
        userDeleted = new User();
        userDeleted.setUsername("deleteduser");
        userDeleted.setEmail("deleted@example.com");
        userDeleted.setDeleted(true);
        userDeleted.setRoles(roles);
        userDeleted.setProfile(profile2);
        userDeleted.setIntake(null);

        userDeleted = userRepository.save(userDeleted);

    }

    /**
     * TC_US_01: Kiểm tra lấy người dùng theo username khi tồn tại
     * Mục tiêu: Đảm bảo phương thức getUserByUsername trả về đúng người dùng với username được cung cấp
     * Input: username = "testuser"
     * Output kỳ vọng: Optional chứa User có email "testuser@example.com"
     */
    @Test
    public void testGetUserByUsernameExists() {
        Optional<User> user = userService.getUserByUsername("testuser");
        assertTrue(user.isPresent());
        assertEquals("testuser@example.com", user.get().getEmail());
    }

    /**
     * TC_US_02: Kiểm tra lấy người dùng theo username khi không tồn tại
     * Mục tiêu: Xác minh rằng getUserByUsername trả về Optional rỗng khi username không có trong DB
     * Input: username = "notexist"
     * Output kỳ vọng: Optional.empty()
     */
    @Test
    public void testGetUserByUsernameNotExists() {
        Optional<User> user = userService.getUserByUsername("notexist");
        assertFalse(user.isPresent());
    }

    /**
     * TC_US_03: Kiểm tra lấy username hiện tại từ SecurityContext
     * Mục tiêu: Đảm bảo phương thức getUserName lấy đúng tên người dùng từ context hiện tại
     * Input: SecurityContext với username "testuser"
     * Output kỳ vọng: "testuser"
     */
    @Test
    public void testGetUserName() {
        UsernamePasswordAuthenticationToken auth =
                new UsernamePasswordAuthenticationToken("testuser", null, Collections.emptyList());
        SecurityContextHolder.getContext().setAuthentication(auth);

        String username = userService.getUserName();
        assertEquals("testuser", username);
    }

    /**
     * TC_US_04: Kiểm tra tồn tại người dùng theo username - có tồn tại
     * Mục tiêu: Xác minh existsByUsername trả về true khi username tồn tại
     * Input: username = "testuser"
     * Output kỳ vọng: true
     */
    @Test
    public void testExistsByUsernameTrue() {
        assertTrue(userService.existsByUsername("testuser"));
    }

    /**
     * TC_US_05: Kiểm tra tồn tại người dùng theo username - không tồn tại
     * Mục tiêu: Xác minh existsByUsername trả về false khi username không tồn tại
     * Input: username = "nouser"
     * Output kỳ vọng: false
     */
    @Test
    public void testExistsByUsernameFalse() {
        assertFalse(userService.existsByUsername("nouser"));
    }

    /**
     * TC_US_06: Kiểm tra tồn tại người dùng theo email - có tồn tại
     * Mục tiêu: Xác minh existsByEmail trả về true khi email tồn tại
     * Input: email = "testuser@example.com"
     * Output kỳ vọng: true
     */
    @Test
    public void testExistsByEmailTrue() {
        assertTrue(userService.existsByEmail("testuser@example.com"));
    }

    /**
     * TC_US_07: Kiểm tra tồn tại người dùng theo email - không tồn tại
     * Mục tiêu: Xác minh existsByEmail trả về false khi email không tồn tại
     * Input: email = "noemail@example.com"
     * Output kỳ vọng: false
     */
    @Test
    public void testExistsByEmailFalse() {
        assertFalse(userService.existsByEmail("noemail@example.com"));
    }

    /**
     * TC_US_08: Kiểm tra phân trang danh sách tất cả người dùng
     * Mục tiêu: Đảm bảo phương thức findUsersByPage trả về danh sách có nội dung
     * Input: PageRequest trang 0, size 10
     * Output kỳ vọng: Page có ít nhất 2 người dùng
     */
    @Test
    public void testFindUsersByPage() {
        Page<User> page = userService.findUsersByPage(PageRequest.of(0, 10));
        assertTrue(page.hasContent());
        assertTrue(page.getContent().size() >= 2);
    }

    /**
     * TC_US_09: Kiểm tra phân trang người dùng chưa bị xóa
     * Mục tiêu: Đảm bảo phương thức findUsersDeletedByPage lọc đúng người dùng chưa bị xóa
     * Input: deleted = false, PageRequest trang 0, size 10
     * Output kỳ vọng: Tất cả user trong trang có deleted = false
     */
    @Test
    public void testFindUsersDeletedByPageFalse() {
        Page<User> page = userService.findUsersDeletedByPage(PageRequest.of(0, 10), false);
        assertEquals(10, page.getContent().stream().filter(u -> !u.isDeleted()).count());
    }

    /**
     * TC_US_10: Kiểm tra phân trang người dùng đã bị xóa
     * Mục tiêu: Đảm bảo phương thức findUsersDeletedByPage lọc đúng người dùng đã bị xóa
     * Input: deleted = true, PageRequest trang 0, size 10
     * Output kỳ vọng: Có đúng 1 user bị xóa trong danh sách
     */
    @Test
    public void testFindUsersDeletedByPageTrue() {
        Page<User> page = userService.findUsersDeletedByPage(PageRequest.of(0, 10), true);
        assertEquals(1, page.getContent().stream().filter(User::isDeleted).count());
    }

    /**
     * TC_US_11: Tìm người dùng chứa chuỗi "test" trong username và chưa bị xóa
     * Mục tiêu: Đảm bảo phương thức findAllByDeletedAndUsernameContains lọc chính xác theo điều kiện
     * Input: deleted = false, username chứa "test", PageRequest trang 0, size 10
     * Output kỳ vọng: Tổng số phần tử = 1, username = "testuser"
     */
    @Test
    public void testFindAllByDeletedAndUsernameContains_Found() {
        Page<User> page = userService.findAllByDeletedAndUsernameContains(false, "test", PageRequest.of(0, 10));
        assertEquals(1, page.getTotalElements());
        assertEquals("testuser", page.getContent().get(0).getUsername());
    }

    /**
     * TC_US_12: Tìm người dùng chứa chuỗi "nomatch" trong username và chưa bị xóa
     * Mục tiêu: Đảm bảo phương thức findAllByDeletedAndUsernameContains trả về rỗng nếu không tìm thấy
     * Input: deleted = false, username chứa "nomatch", PageRequest trang 0, size 10
     * Output kỳ vọng: Tổng số phần tử = 0
     */
    @Test
    public void testFindAllByDeletedAndUsernameContains_NotFound() {
        Page<User> page = userService.findAllByDeletedAndUsernameContains(false, "nomatch", PageRequest.of(0, 10));
        assertEquals(0, page.getTotalElements());
    }

    /**
     * TC_US_13: Tạo người dùng mới với role null
     * Mục tiêu: Kiểm tra khi không truyền role thì mặc định sẽ gán ROLE_STUDENT
     * Input: user có username, email, profile, nhưng không có roles
     * Output kỳ vọng: User được tạo có đúng 1 role là ROLE_STUDENT
     */
    @Test
    public void testCreateUser_DefaultRoleStudent() {
        User user = new User("studentuser", null, "student@example.com", new Profile());
        user.setRoles(null); // không truyền roles

        User savedUser = userService.createUser(user);

        assertNotNull(savedUser.getId());
        assertEquals("studentuser", savedUser.getUsername());
        assertEquals(1, savedUser.getRoles().size());
        assertTrue(savedUser.getRoles().stream().anyMatch(r -> r.getName().equals(ERole.ROLE_STUDENT)));
    }

    /**
     * TC_US_14: Tạo người dùng mới với role là ADMIN
     * Mục tiêu: Kiểm tra gán đúng role khi truyền vào ROLE_ADMIN
     * Input: user có ROLE_ADMIN
     * Output kỳ vọng: User được tạo có role là ROLE_ADMIN
     */
    @Test
    public void testCreateUser_WithRoleAdmin() {
        Role adminRole = roleService.findByName(ERole.ROLE_ADMIN).orElseThrow(() -> new RuntimeException("Role not found"));
        User user = new User("adminuser", null, "admin@example.com", new Profile());
        user.setRoles(new HashSet<>(Collections.singletonList(adminRole)));

        User savedUser = userService.createUser(user);

        assertNotNull(savedUser.getId());
        assertTrue(savedUser.getRoles().stream().anyMatch(r -> r.getName().equals(ERole.ROLE_ADMIN)));
    }

    /**
     * TC_US_15: Tạo người dùng mới với role là LECTURER
     * Mục tiêu: Kiểm tra gán đúng role khi truyền vào ROLE_LECTURER
     * Input: user có ROLE_LECTURER
     * Output kỳ vọng: User được tạo có role là ROLE_LECTURER
     */
    @Test
    public void testCreateUser_WithRoleLecturer() {
        Role lecturerRole = roleService.findByName(ERole.ROLE_LECTURER).orElseThrow(() -> new RuntimeException("Role not found"));
        User user = new User("lectureruser", null, "lecturer@example.com", new Profile());
        user.setRoles(new HashSet<>(Collections.singletonList(lecturerRole)));

        User savedUser = userService.createUser(user);

        assertNotNull(savedUser.getId());
        assertTrue(savedUser.getRoles().stream().anyMatch(r -> r.getName().equals(ERole.ROLE_LECTURER)));
    }

    /**
     * TC_US_16: Tạo người dùng mới với role không hợp lệ (không tồn tại)
     * Mục tiêu: Kiểm tra ném ngoại lệ khi role không tìm thấy trong hệ thống
     * Input: user có role không tồn tại trong DB
     * Output kỳ vọng: Ném RuntimeException
     */
//    @Test(expected = RuntimeException.class)
//    public void testCreateUser_WithInvalidRole_ThrowsException() {
//        Role invalidRole = new Role(); // không có ERole name hợp lệ
//        invalidRole.setName(null); // giả lập lỗi
//        User user = new User("invaliduser", null, "invalid@example.com", new Profile());
//        user.setRoles(new HashSet<>(Collections.singletonList(invalidRole)));
//
//        userService.createUser(user); // kỳ vọng ném lỗi
//    }

    /**
     * TC_US_16: Tìm user theo ID khi tồn tại
     * Mục tiêu: Đảm bảo phương thức findUserById trả về đúng user khi ID hợp lệ
     * Input: ID của user đã tạo
     * Output kỳ vọng: Optional chứa user
     */
    @Test
    public void testFindUserById_Exists() {
        User user = new User("findme", passwordEncoder.encode("findme"), "findme@example.com", new Profile());
        userRepository.save(user);

        Optional<User> found = userService.findUserById(user.getId());
        assertTrue(found.isPresent());
        assertEquals("findme", found.get().getUsername());
    }

    /**
     * TC_US_17: Tìm user theo ID không tồn tại
     * Mục tiêu: Kiểm tra trả về Optional.empty() khi user không tồn tại
     * Input: ID không tồn tại, ví dụ 9999L
     * Output kỳ vọng: Optional.empty()
     */
    @Test
    public void testFindUserById_NotFound() {
        Optional<User> found = userService.findUserById(9999L);
        assertFalse(found.isPresent());
    }

    /**
     * TC_US_18: Xuất danh sách user chưa bị xóa
     * Mục tiêu: Kiểm tra xuất danh sách user với deleted = false
     * Input: statusDelete = false
     * Output kỳ vọng: Danh sách UserExport chứa thông tin user chưa bị xóa
     */
    @Test
    public void testFindAllByDeletedToExport_NotDeleted() {
        List<UserExport> exports = userService.findAllByDeletedToExport(false);

        assertFalse(exports.isEmpty());
        assertTrue(exports.stream().anyMatch(e -> e.getUsername().equals("testuser")));
    }

    /**
     * TC_US_19: Xuất danh sách user đã bị xóa
     * Mục tiêu: Kiểm tra xuất danh sách user với deleted = true
     * Input: statusDelete = true
     * Output kỳ vọng: Danh sách UserExport chứa thông tin user đã bị xóa
     */
    @Test
    public void testFindAllByDeletedToExport_Deleted() {
        List<UserExport> exports = userService.findAllByDeletedToExport(true);

        assertFalse(exports.isEmpty());
        assertTrue(exports.stream().anyMatch(e -> e.getUsername().equals("deleteduser")));
    }

    /**
     * TC_US_20: Cập nhật username của user
     * Mục tiêu: Kiểm tra cập nhật username thành công
     * Input: user có ID hợp lệ, cập nhật username mới chưa tồn tại
     * Output kỳ vọng: Username thay đổi trong DB
     */
    @Test
    public void testUpdateUser_UsernameChanged() {
        userActive.setUsername("newusername");
        userService.updateUser(userActive);

        Optional<User> updated = userRepository.findById(userActive.getId());
        assertTrue(updated.isPresent());
        assertEquals("newusername", updated.get().getUsername());
    }

    /**
     * TC_US_21: Cập nhật email của user
     * Mục tiêu: Kiểm tra cập nhật email thành công
     * Input: user có ID hợp lệ, thay đổi email chưa tồn tại
     * Output kỳ vọng: Email thay đổi trong DB
     */
    @Test
    public void testUpdateUser_EmailChanged() {
        userActive.setEmail("newemail@example.com");
        userService.updateUser(userActive);

        Optional<User> updated = userRepository.findById(userActive.getId());
        assertTrue(updated.isPresent());
        assertEquals("newemail@example.com", updated.get().getEmail());
    }

    /**
     * TC_US_22: Cập nhật profile của user
     * Mục tiêu: Kiểm tra cập nhật thông tin trong profile thành công
     * Input: user có profile thay đổi firstName, lastName
     * Output kỳ vọng: Profile cập nhật đúng
     */
    @Test
    public void testUpdateUser_ProfileChanged() {
        Profile profile = userActive.getProfile();
        profile.setFirstName("Updated");
        profile.setLastName("User");
        userActive.setProfile(profile);
        userService.updateUser(userActive);

        Optional<User> updated = userRepository.findById(userActive.getId());
        assertTrue(updated.isPresent());
        assertEquals("Updated", updated.get().getProfile().getFirstName());
        assertEquals("User", updated.get().getProfile().getLastName());
    }

    /**
     * TC_US_23: Cập nhật role của user
     * Mục tiêu: Kiểm tra cập nhật role thành công
     * Input: user thay đổi role sang ADMIN
     * Output kỳ vọng: Vai trò của user là ROLE_ADMIN
     */
    @Test
    public void testUpdateUser_RoleChanged() {
        Role adminRole = roleService.findByName(ERole.ROLE_ADMIN).orElseThrow(()-> new RuntimeException("Role not found"));
        userActive.setRoles(new HashSet<>(Collections.singletonList(adminRole)));
        userService.updateUser(userActive);

        Optional<User> updated = userRepository.findById(userActive.getId());
        assertTrue(updated.isPresent());
        assertTrue(updated.get().getRoles().stream().anyMatch(r -> r.getName().equals(ERole.ROLE_ADMIN)));
    }

    /**
     * TC_US_24: Cập nhật user không tồn tại
     * Mục tiêu: Kiểm tra xử lý khi cập nhật user không có trong DB
     * Input: user có ID chưa được lưu
     * Output kỳ vọng: không có user mới được tạo
     */
    @Test
    public void testUpdateUser_UserNotExists_CreatesNew() {
        User newUser = new User("newuser", "pass", "new@example.com", new Profile());
        userService.updateUser(newUser);

        Optional<User> created = userRepository.findById(newUser.getId());
        assertFalse(created.isPresent());
    }

    /**
     * TC_US_25: Cập nhật email bị trùng với user khác
     * Mục tiêu: Đảm bảo hệ thống không cho phép cập nhật email trùng
     * Input: user1 đã có email, user2 cập nhật sang cùng email
     * Output kỳ vọng: xảy ra lỗi (DataIntegrityViolationException)
     */
    @Test(expected = Exception.class)
    public void testUpdateUser_EmailAlreadyExists_ThrowsException() {
        // Giả lập user2 có email giống user1
        User anotherUser = new User("user2", "pass", "dup@example.com", new Profile());
        userRepository.save(anotherUser);

        userActive.setEmail("dup@example.com");

        userService.updateUser(userActive);
    }

    /**
     * TC_US_26: Cập nhật username bị trùng với user khác
     * Mục tiêu: Không cho phép trùng username trong DB
     * Input: user1 đã tồn tại, user2 đổi sang username user1
     * Output kỳ vọng: lỗi trùng key (username unique)
     */
    @Test(expected = Exception.class)
    public void testUpdateUser_UsernameAlreadyExists_ThrowsException() {
        User other = new User("existingUser", "pass", "other@example.com", new Profile());
        userRepository.save(other);

        userActive.setUsername("existingUser");

        userService.updateUser(userActive);
    }


    /**
     * TC_US_27: Tìm tất cả user theo intakeId hợp lệ
     * Mục tiêu: Kiểm tra khi intakeId có user liên quan
     * Input: ID intake giả lập có user
     * Output kỳ vọng: Danh sách chứa user tương ứng
     */
    @Test
    public void testFindAllByIntakeId_Found() {
        Intake intake = new Intake();
        intakeRepository.save(intake);
        userActive.setIntake(intake);
        userRepository.save(userActive);

        List<User> result = userService.findAllByIntakeId(intake.getId());
        assertFalse(result.isEmpty());
        assertEquals(userActive.getId(), result.get(0).getId());
    }

    /**
     * TC_US_28: Tìm tất cả user theo intakeId không tồn tại
     * Mục tiêu: Kiểm tra trả về danh sách rỗng khi intakeId không có user nào
     * Input: ID intake không tồn tại hoặc không có user
     * Output kỳ vọng: Danh sách rỗng
     */
    @Test
    public void testFindAllByIntakeId_NotFound() {
        List<User> result = userService.findAllByIntakeId(999L);
        assertTrue(result.isEmpty());
    }

    /**
     * TC_US_29: Yêu cầu reset mật khẩu với email hợp lệ
     * Mục tiêu: Kiểm tra token được tạo và email được gửi khi user tồn tại
     * Input: email hợp lệ
     * Output kỳ vọng: true và token được lưu
     */
    @Test
    public void testRequestPasswordReset_EmailExists() throws Exception {
        boolean result = userService.requestPasswordReset(userActive.getEmail());

        assertTrue(result);
        List<PasswordResetToken> tokens = passwordResetTokenRepository.findAll();
        assertFalse(tokens.isEmpty());
        assertEquals(userActive.getId(), tokens.get(0).getUser().getId());
    }

    /**
     * TC_US_30: Yêu cầu reset mật khẩu với email không tồn tại
     * Mục tiêu: Kiểm tra khi email không khớp user nào thì return false
     * Input: email không tồn tại
     * Output kỳ vọng: false
     */
    @Test
    public void testRequestPasswordReset_EmailNotExists() throws Exception {
        boolean result = userService.requestPasswordReset("notfound@example.com");
        assertFalse(result);
    }

    /**
     * TC_US_31: Reset mật khẩu thành công với token hợp lệ
     * Mục tiêu: Kiểm tra mật khẩu được cập nhật thành công
     * Input: token hợp lệ, password mới
     * Output kỳ vọng: true và mật khẩu đã mã hóa được lưu
     */
    @Test
    public void testResetPassword_ValidToken() {
        String token = new JwtUtils().generatePasswordResetToken(userActive.getId());
        PasswordResetToken resetToken = new PasswordResetToken();
        resetToken.setToken(token);
        resetToken.setUser(userActive);
        passwordResetTokenRepository.save(resetToken);

        String newPassword = "newPassword123";
        boolean result = userService.resetPassword(token, newPassword);

        assertTrue(result);
        User updatedUser = userRepository.findById(userActive.getId()).get();
        assertTrue(passwordEncoder.matches(newPassword, updatedUser.getPassword()));
    }

    /**
     * TC_US_32: Reset mật khẩu với token đã hết hạn
     * Mục tiêu: Đảm bảo token hết hạn thì return false
     * Input: token expired, password mới
     * Output kỳ vọng: false
     */
    @Test(expected = Exception.class)
    public void testResetPassword_ExpiredToken() {
        String expiredToken = "eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJ0aGFuaHRhbTI4c3MiLCJpYXQiOjE3NDQyOTQ4MTIsImV4cCI6MTc0NDM4MTIxMiwicm9sZSI6W3siYXV0aG9yaXR5IjoiUk9MRV9BRE1JTiJ9XX0.dlO69r0W_COp4s0Y1zZ1e4cGUE-56KPuAW5WeebauMCzT0Q8Ct3tXw_flMBuSQuqIRQ4tcy_JNju-wjL5X5F0w";

        // Lưu token vào DB như thể user đã request reset trước đó
        PasswordResetToken resetToken = new PasswordResetToken();
        resetToken.setToken(expiredToken);
        resetToken.setUser(userActive);
        passwordResetTokenRepository.save(resetToken);

        // Thực hiện reset mật khẩu
        boolean result = userService.resetPassword(expiredToken, "newPass123");
    }

    /**
     * TC_US_33: Reset mật khẩu với token không tồn tại trong DB
     * Mục tiêu: Đảm bảo không có token trong DB thì return false
     * Input: token không tồn tại
     * Output kỳ vọng: false
     */
    @Test
    public void testResetPassword_TokenNotFound() {
        String token = new JwtUtils().generatePasswordResetToken(userActive.getId());
        boolean result = userService.resetPassword(token, "somePassword");
        assertFalse(result);
    }


    /**
     * TC_US_34: Tìm kiếm user bằng username chứa từ khóa
     * Mục tiêu: Kiểm tra tìm kiếm user bằng username
     * Input: username chứa từ khóa, email không khớp
     * Output kỳ vọng: trả về danh sách chứa user phù hợp
     */
    @Test
    public void testFindAllByUsernameContainsOrEmailContains_UsernameMatch() {
        Page<User> result = userService.findAllByUsernameContainsOrEmailContains("test", "noemailmatch", PageRequest.of(0, 10));
        assertTrue(result.hasContent());
        assertTrue(result.stream().anyMatch(u -> u.getUsername().contains("test")));
    }

    /**
     * TC_US_35: Tìm kiếm user bằng email chứa từ khóa
     * Mục tiêu: Kiểm tra tìm kiếm user bằng email
     * Input: email chứa từ khóa, username không khớp
     * Output kỳ vọng: trả về danh sách chứa user phù hợp
     */
    @Test
    public void testFindAllByUsernameContainsOrEmailContains_EmailMatch() {
        Page<User> result = userService.findAllByUsernameContainsOrEmailContains("nousername", "example", PageRequest.of(0, 10));
        assertTrue(result.hasContent());
        assertTrue(result.stream().anyMatch(u -> u.getEmail().contains("example")));
    }

    /**
     * TC_US_36: Tìm kiếm không có kết quả
     * Mục tiêu: Đảm bảo hệ thống xử lý đúng khi không có user khớp
     * Input: từ khóa không trùng username/email nào
     * Output kỳ vọng: danh sách rỗng
     */
    @Test
    public void testFindAllByUsernameContainsOrEmailContains_NoMatch() {
        Page<User> result = userService.findAllByUsernameContainsOrEmailContains("abcxyz", "nomatch", PageRequest.of(0, 10));
        assertFalse(result.hasContent());
    }

}
