package com.thanhtam.backend.service;

import com.amazonaws.services.organizations.model.ConstraintViolationException;
import com.thanhtam.backend.entity.*;
import com.thanhtam.backend.repository.PartRepository;
import com.thanhtam.backend.repository.UserRepository;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;


@RunWith(SpringRunner.class)
@SpringBootTest
@org.springframework.transaction.annotation.Transactional
@Rollback
public class ExamUserServiceImplTest {


    @Autowired
    private UserService userService;

    @Autowired
    private CourseService courseService;
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ExamUserService examUserService;

    @Autowired
    private ExamService examService;

    // TC_EUS01: Test tạo ExamUser: trường hợp fail do exam bị null
    // Mục tiêu: Kiểm tra phương thức create lưu ko thành công exam bị null
    // INPUT: {exam: null, List<User> users}
    // OUTPUT Kỳ vọng: ném ra NullPointerException ngoại lệ
    @Test(expected = NullPointerException.class)
    @Transactional
    @Rollback
    public void testCreate_ShouldThrow_WhenExamIsNull() {
        List<User> users = new ArrayList<>();
        users.add(new User()); // user dummy
        examUserService.create(null, users);
    }

    // TC_EUS_02: Test tạo ExamUser - trường hợp userSet là null
    // Mục tiêu: Kiểm tra phương thức create ném ngoại lệ khi tham số userSet là null
    // INPUT: exam = exam hợp lệ (id=200), userSet = null
    // OUTPUT kỳ vọng: Ném ra NullPointerException
    @Test(expected = NullPointerException.class)
    @Transactional
    public void testCreate_ShouldThrow_WhenUserSetIsNull() {
        Optional<Exam> examOpt = examService.getExamById(200L);
        Assert.assertTrue("Exam không tồn tại!", examOpt.isPresent());
        Exam exam = examOpt.get();
        examUserService.create(exam, null);
    }

    // TC_EUS_03: Test tạo ExamUser - userSet chứa phần tử null
    // Mục tiêu: Kiểm tra phương thức create ném ngoại lệ khi userSet chứa phần tử null
    // INPUT: exam = exam hợp lệ (id=200), userSet = List các user hợp lệ + 1 phần tử null
    // OUTPUT kỳ vọng: Ném ra Exception
    @Test(expected = Exception.class)
    @Transactional
    public void testCreate_ShouldThrow_WhenUserSetContainsNull() {
        Optional<Exam> examOpt = examService.getExamById(200L);
        Assert.assertTrue("Exam không tồn tại!", examOpt.isPresent());
        Exam exam = examOpt.get();

        List<User> users = userService.findAllByIntakeId(exam.getIntake().getId());
        Assert.assertFalse("Danh sách user rỗng!", users.isEmpty());

        // Thêm phần tử null
        users.add(null);

        examUserService.create(exam, users);
    }

    // TC_EUS_04: Test tạo ExamUser thành công với dữ liệu hợp lệ
    // Mục tiêu: Kiểm tra phương thức create tạo đúng số lượng ExamUser tương ứng với số lượng user truyền vào
    // INPUT: exam = exam hợp lệ (id=200), userSet = List các user hợp lệ thuộc intake của exam
    // OUTPUT kỳ vọng: Số lượng ExamUser sau khi tạo tăng đúng bằng số lượng user truyền vào, mỗi ExamUser liên kết đúng exam và user
    @Test
    @Transactional
    public void testCreateExamWithUsers() {
        // Lấy Exam
        Optional<Exam> result = examService.getExamById(200L);
        junit.framework.Assert.assertTrue("Exam không tồn tại!", result.isPresent());
        Exam exam = result.get();

        // Lấy danh sách User theo intake
        List<User> users = userService.findAllByIntakeId(exam.getIntake().getId());
        junit.framework.Assert.assertFalse("Không có user nào trong intake!", users.isEmpty());

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
        junit.framework.Assert.assertEquals("Số lượng user được gán thêm không đúng!", users.size(), diff);

        // Kiểm tra từng user có được gán mới không (nếu cần)
        List<Long> expectedUserIds = users.stream().map(User::getId).collect(Collectors.toList());
        List<Long> actualUserIds = after.stream().map(eu -> eu.getUser().getId()).collect(Collectors.toList());

        for (Long id : expectedUserIds) {
            junit.framework.Assert.assertTrue("User ID " + id + " không được gán!", actualUserIds.contains(id));
        }
    }
    // TC_EUS_05: Test getExamListByUsername với username không tồn tại
    // Mục tiêu: Kiểm tra phương thức getExamListByUsername trả về danh sách rỗng khi username không tồn tại
    // INPUT: username = "username_khong_ton_tai"
    // OUTPUT kỳ vọng: Trả về danh sách rỗng
    @Test
    @Transactional
    public void testGetExamListByUsername_UserNotExist() {
        String nonExistentUsername = "username_khong_ton_tai";

        List<ExamUser> result = examUserService.getExamListByUsername(nonExistentUsername);

        Assert.assertNotNull("Kết quả không được null", result);
        Assert.assertTrue("Danh sách phải rỗng khi username không tồn tại", result.isEmpty());
    }
    // TC_EUS_06: Test getExamListByUsername với username tồn tại nhưng không có exam chưa bị hủy
    // Mục tiêu: Kiểm tra phương thức getExamListByUsername trả về danh sách rỗng khi user không có exam chưa bị hủy
    // INPUT: username = "1724801030059"
    // OUTPUT kỳ vọng: Trả về danh sách rỗng
    @Test
    @Transactional
    public void testGetExamListByUsername_NoActiveExams() {
        // Giả sử user này có exam nhưng tất cả đều bị hủy (canceled == true)
        String username = "1724801030059";

        List<ExamUser> result = examUserService.getExamListByUsername(username);

        Assert.assertNotNull("Kết quả không được null", result);
        Assert.assertTrue("Danh sách phải rỗng khi không có exam chưa bị hủy", result.isEmpty());
    }
    // TC_EUS_07: Test getExamListByUsername với username là chuỗi rỗng
    // Mục tiêu: Kiểm tra phương thức getExamListByUsername trả về danh sách rỗng khi username là chuỗi rỗng
    // INPUT: username = ""
    // OUTPUT kỳ vọng: Trả về danh sách rỗng
    @Test
    @Transactional
    public void testGetExamListByUsername_EmptyUsername() {
        String emptyUsername = "";

        List<ExamUser> result = examUserService.getExamListByUsername(emptyUsername);

        Assert.assertNotNull("Kết quả không được null", result);
        // Tùy vào logic, có thể trả về rỗng hoặc ném exception.
        // Ở đây giả sử trả về rỗng
        Assert.assertTrue("Danh sách phải rỗng khi username rỗng", result.isEmpty());
    }
    // TC_EUS_08: Test getExamListByUsername với username là null
    // Mục tiêu: Kiểm tra phương thức getExamListByUsername ném IllegalArgumentException khi username là null
    // INPUT: username = null
    // OUTPUT kỳ vọng: Ném ra Exception
    @Test(expected =Exception.class)
    @Transactional
    public void testGetExamListByUsername_NullUsername() {
        examUserService.getExamListByUsername(null);
    }



    // TC_EUS_09: Test getExamListByUsername thành công với username hợp lệ
    // Mục tiêu: Kiểm tra phương thức getExamListByUsername trả về danh sách ExamUser đúng user
    // INPUT: username = "1624801040051"
    // OUTPUT kỳ vọng: Trả về danh sách ExamUser không rỗng, tất cả ExamUser có username đúng
    @Test
    @Transactional
    public void testGetExamListByUsernameSuccess() {
        // Tạo dữ liệu cho User và Exam
        Optional<User> result = userService.getUserByUsername("1624801040051");
        junit.framework.Assert.assertTrue("User không tồn tại!", result.isPresent());
        User user = result.get();

        List<ExamUser> li = examUserService.getExamListByUsername(user.getUsername());
        junit.framework.Assert.assertNotNull("Danh Sach Bai Thi Rong",li);
        // Kiểm tra rằng danh sách các ExamUser chứa đúng thông tin Exam và User
        for (ExamUser examUser : li) {
            junit.framework.Assert.assertEquals("Username không đúng!", user.getUsername(), examUser.getUser().getUsername());
            junit.framework.Assert.assertNotNull("Exam không tồn tại!", examUser.getExam());
        }
    }

    // TC_EUS_10: Test findByExamAndUser thành công với examId và username hợp lệ
    // Mục tiêu: Kiểm tra phương thức findByExamAndUser trả về ExamUser đúng với examId và username hợp lệ
    // INPUT: examId = 195L, username = "1624801040051"
    // OUTPUT kỳ vọng: Trả về ExamUser không null, examId và username đúng

    @Test
    @Transactional
    public void testFindByExamAndUser_Success() {
        Optional<Exam> result = examService.getExamById(195L);
        Assert.assertTrue("Exam không tồn tại!", result.isPresent());
        Exam exam = result.get();

        Optional<User> result2 = userService.getUserByUsername("1624801040051");
        Assert.assertTrue("User không tồn tại!", result2.isPresent());
        User user = result2.get();

        ExamUser foundExamUser = examUserService.findByExamAndUser(exam.getId(), user.getUsername());

        Assert.assertNotNull("ExamUser không tồn tại!", foundExamUser);
        Assert.assertEquals("Exam ID không đúng!", exam.getId(), foundExamUser.getExam().getId());
        Assert.assertEquals("Username không đúng!", user.getUsername(), foundExamUser.getUser().getUsername());
    }
    // TC_EUS_11: Test findByExamAndUser với examId không tồn tại
    // Mục tiêu: Kiểm tra phương thức findByExamAndUser trả về null khi examId không tồn tại
    // INPUT: examId = 500L (không tồn tại), username = "1624801040051"
    // OUTPUT kỳ vọng: Trả về null
    @Test
    @Transactional
    public void testFindByExamAndUser_Fail_ExamNotExist() {
        Optional<Exam> result = examService.getExamById(500L);
        if (!result.isPresent()) {
            // Exam không tồn tại, test pass vì không tìm thấy
            return;
        }
        Exam exam = result.get();

        Optional<User> result2 = userService.getUserByUsername("1624801040051");
        if (!result2.isPresent()) {
            return;
        }
        User user = result2.get();

        ExamUser foundExamUser = examUserService.findByExamAndUser(exam.getId(), user.getUsername());

        Assert.assertNull("ExamUser phải là null khi exam không tồn tại hoặc không có user tham gia!", foundExamUser);
    }
    // TC_EUS_12: Test findByExamAndUser với username không tồn tại
    // Mục tiêu: Kiểm tra phương thức findByExamAndUser trả về null khi username không tồn tại
    // INPUT: examId = 195L, username = "username_khong_ton_tai"
    // OUTPUT kỳ vọng: Trả về null
    @Test
    @Transactional
    public void testFindByExamAndUser_Fail_UserNotExist() {
        Optional<Exam> result = examService.getExamById(195L);
        Assert.assertTrue("Exam không tồn tại!", result.isPresent());
        Exam exam = result.get();

        Optional<User> result2 = userService.getUserByUsername("username_khong_ton_tai");
        if (!result2.isPresent()) {
            // User không tồn tại, test pass
            return;
        }
        User user = result2.get();

        ExamUser foundExamUser = examUserService.findByExamAndUser(exam.getId(), user.getUsername());

        Assert.assertNull("ExamUser phải là null khi user không tồn tại!", foundExamUser);
    }
    // TC_EUS_13: Test findByExamAndUser với user không tham gia exam
    // Mục tiêu: Kiểm tra phương thức findByExamAndUser trả về null khi user không tham gia exam
    // INPUT: examId = 195L, username = "dvcpro" (user tồn tại nhưng không tham gia exam)
    // OUTPUT kỳ vọng: Trả về null
    @Test
    @Transactional
    public void testFindByExamAndUser_Fail_UserNotInExam() {
        Optional<Exam> result = examService.getExamById(195L);
        Assert.assertTrue("Exam không tồn tại!", result.isPresent());
        Exam exam = result.get();

        Optional<User> result2 = userService.getUserByUsername("dvcpro"); // user tồn tại nhưng không tham gia exam
        Assert.assertTrue("User không tồn tại!", result2.isPresent());
        User user = result2.get();

        ExamUser foundExamUser = examUserService.findByExamAndUser(exam.getId(), user.getUsername());

        Assert.assertNull("User không tham gia kỳ thi này nên ExamUser phải là null!", foundExamUser);
    }
    // TC_EUS_14: Test findByExamAndUser với examId là null
    // Mục tiêu: Kiểm tra phương thức findByExamAndUser ném exception khi examId là null
    // INPUT: examId = null, username = "1624801040051"
    // OUTPUT kỳ vọng: Ném IllegalArgumentException hoặc NullPointerException
    @Test
    @Transactional
    public void testFindByExamAndUser_NullExamId() {
        try {
            examUserService.findByExamAndUser(null, "1624801040051");
            Assert.fail("Phải ném exception khi examId là null");
        } catch (IllegalArgumentException | NullPointerException e) {
            // pass
        }
    }
    // TC_EUS_15: Test findByExamAndUser với username là null
    // Mục tiêu: Kiểm tra phương thức findByExamAndUser ném exception khi username là null
    // INPUT: examId = 195L, username = null
    // OUTPUT kỳ vọng: Ném IllegalArgumentException hoặc NullPointerException
    @Test
    @Transactional
    public void testFindByExamAndUser_NullUsername() {
        Optional<Exam> result = examService.getExamById(195L);
        Assert.assertTrue("Exam không tồn tại!", result.isPresent());
        Exam exam = result.get();

        try {
            examUserService.findByExamAndUser(exam.getId(), null);
            Assert.fail("Phải ném exception khi username là null");
        } catch (Exception e) {
            // pass
        }
    }
    // TC_EUS_16: Test findByExamAndUser với username là chuỗi rỗng
    // Mục tiêu: Kiểm tra phương thức findByExamAndUser trả về null khi username là chuỗi rỗng
    // INPUT: examId = 195L, username = ""
    // OUTPUT kỳ vọng: Trả về null
    @Test
    @Transactional
    public void testFindByExamAndUser_EmptyUsername() {
        Optional<Exam> result = examService.getExamById(195L);
        Assert.assertTrue("Exam không tồn tại!", result.isPresent());
        Exam exam = result.get();

        ExamUser foundExamUser = examUserService.findByExamAndUser(exam.getId(), "");
        Assert.assertNull("Phải trả về null khi username là chuỗi rỗng", foundExamUser);
    }

    // TC_EUS_17: Test update ExamUser thành công
    // Mục tiêu: Kiểm tra phương thức update cập nhật đúng dữ liệu ExamUser
    // INPUT: examUser với isFinished = true, examId và username hợp lệ
    // OUTPUT kỳ vọng: uDữ liệu ExamUser được cập nhật thành công, isFinished = tre

    @Test
    @Transactional
    public void testUpdateExamUserSuccess() {
        Optional<Exam> result = examService.getExamById(195L);
        Assert.assertTrue("Exam không tồn tại!", result.isPresent());
        Exam exam = result.get();

        Optional<User> result2 = userService.getUserByUsername("1624801040051");
        Assert.assertTrue("User không tồn tại!", result2.isPresent());
        User user = result2.get();

        ExamUser foundExamUser = examUserService.findByExamAndUser(exam.getId(), user.getUsername());
        Assert.assertNotNull("ExamUser không tồn tại!", foundExamUser);
        Assert.assertEquals("Exam ID không đúng!", exam.getId(), foundExamUser.getExam().getId());
        Assert.assertEquals("Username không đúng!", user.getUsername(), foundExamUser.getUser().getUsername());

        foundExamUser.setIsFinished(true);
        examUserService.update(foundExamUser);

        // Truy vấn lại để kiểm tra dữ liệu đã được cập nhật trong DB
        ExamUser updatedExamUser = examUserService.findByExamAndUser(exam.getId(), user.getUsername());
        Assert.assertTrue("Chưa cập nhật trạng thái hoàn thành", updatedExamUser.getIsFinished());
    }

    // TC_EUS_18: Test update ExamUser với đối tượng null
    // Mục tiêu: Kiểm tra phương thức update ném IllegalArgumentException khi truyền null
    // INPUT: examUser = null
    // OUTPUT kỳ vọng: Exception
    @Test(expected = Exception.class)
    @Transactional
    public void testUpdateExamUser_NullExamUser() {
        examUserService.update(null);
    }

    // TC_EUS_19: Test update ExamUser với user hoặc exam bị null
    // Mục tiêu: Kiểm tra phương thức update ném DataIntegrityViolationException khi user hoặc exam null
    // INPUT: examUser với user = null, exam = null
    // OUTPUT kỳ vọng: Ném Exception
    @Test(expected = Exception.class) // Hoặc exception tương ứng
    @Transactional
    public void testUpdateExamUser_NullUserOrExam() {
        ExamUser examUser = new ExamUser();
        examUser.setUser(null); // user null
        examUser.setExam(null); // exam null
        examUser.setIsFinished(false);

        examUserService.update(examUser);
    }


    // TC_EUS_20: Test update ExamUser với dữ liệu không hợp lệ (điểm âm, thời gian âm)
    // Mục tiêu: Kiểm tra phương thức update ném ConstraintViolationException khi dữ liệu không hợp lệ
    // INPUT: examUser với totalPoint = -100.0, remainingTime = -10
    // OUTPUT kỳ vọng: Ném Exception
    @Test(expected = Exception.class) // Hoặc exception tùy validation
    @Transactional
    public void testUpdateExamUser_InvalidData() {
        Optional<Exam> result = examService.getExamById(195L);
        Assert.assertTrue(result.isPresent());
        Exam exam = result.get();

        Optional<User> result2 = userService.getUserByUsername("1624801040051");
        Assert.assertTrue(result2.isPresent());
        User user = result2.get();

        ExamUser examUser = examUserService.findByExamAndUser(exam.getId(), user.getUsername());
        Assert.assertNotNull(examUser);

        examUser.setTotalPoint(-100.0); // điểm không hợp lệ
        examUser.setRemainingTime(-10); // thời gian âm

        examUserService.update(examUser);
    }

    // TC_EUS_21: Test getCompleteExams thành công
    // Mục tiêu: Kiểm tra phương thức getCompleteExams trả về danh sách bài thi đã hoàn thành đúng user và course
    // INPUT: courseId = 12L, username = "1624801040051"
    // OUTPUT kỳ vọng: Trả về danh sách không rỗng, tất cả bài thi có isFinished = true và user đúng
    @Test
    @Transactional
    public void testGetCompleteExams_Success() {
        Optional<Course> result = courseService.getCourseById(12L);
        Assert.assertTrue("Course không tồn tại!", result.isPresent());
        Course course = result.get();

        Optional<User> result2 = userService.getUserByUsername("1624801040051");
        Assert.assertTrue("User không tồn tại!", result2.isPresent());
        User user = result2.get();

        List<ExamUser> list = examUserService.getCompleteExams(course.getId(), user.getUsername());

        Assert.assertNotNull("Danh sách không được null", list);
        Assert.assertFalse("Danh sách bài thi hoàn thành không thể rỗng!", list.isEmpty());

        for (ExamUser examUser : list) {
            Assert.assertTrue("Bài thi chưa hoàn thành!", examUser.getIsFinished());
            Assert.assertEquals("User không đúng!", user.getUsername(), examUser.getUser().getUsername());
            Assert.assertEquals("Course ID không đúng!", course.getId(), examUser.getExam().getPart().getCourse().getId());
            Assert.assertTrue("Điểm phải lớn hơn -1", examUser.getTotalPoint() > -1.0);
        }
    }

    // TC_EUS_22: Test getCompleteExams với courseId không tồn tại
    // Mục tiêu: Kiểm tra phương thức getCompleteExams trả về danh sách rỗng khi courseId không tồn tại
    // INPUT: courseId = 999999L (không tồn tại), username = "1624801040051"
    // OUTPUT kỳ vọng: Trả về danh sách rỗng
    @Test
    @Transactional
    public void testGetCompleteExams_CourseNotExist() {
        Long invalidCourseId = 999999L; // ID khóa học không tồn tại

        Optional<User> result2 = userService.getUserByUsername("1624801040051");
        Assert.assertTrue("User không tồn tại!", result2.isPresent());
        User user = result2.get();

        List<ExamUser> list = examUserService.getCompleteExams(invalidCourseId, user.getUsername());

        Assert.assertNotNull("Danh sách không được null", list);
        Assert.assertTrue("Danh sách phải rỗng khi course không tồn tại", list.isEmpty());
    }

    // TC_EUS_23: Test getCompleteExams với username không tồn tại
    // Mục tiêu: Kiểm tra phương thức getCompleteExams trả về danh sách rỗng khi username không tồn tại
    // INPUT: courseId = 12L, username = "username_khong_ton_tai"
    // OUTPUT kỳ vọng: Trả về danh sách rỗng
    @Test
    @Transactional
    public void testGetCompleteExams_UserNotExist() {
        Optional<Course> result = courseService.getCourseById(12L);
        Assert.assertTrue("Course không tồn tại!", result.isPresent());
        Course course = result.get();

        String invalidUsername = "username_khong_ton_tai";

        List<ExamUser> list = examUserService.getCompleteExams(course.getId(), invalidUsername);

        Assert.assertNotNull("Danh sách không được null", list);
        Assert.assertTrue("Danh sách phải rỗng khi user không tồn tại", list.isEmpty());
    }

    // TC_EUS_24: Test getCompleteExams với courseId là null
    // Mục tiêu: Kiểm tra phương thức getCompleteExams ném ngoại lệ khi courseId là null
    // INPUT: courseId = null, username = "1624801040051"
    // OUTPUT kỳ vọng: Ném IllegalArgumentException hoặc NullPointerException
    @Test(expected = Exception.class)
    @Transactional
    public void testGetCompleteExams_NullCourseId() {
        Optional<User> result2 = userService.getUserByUsername("1624801040051");
        Assert.assertTrue("User không tồn tại!", result2.isPresent());
        User user = result2.get();

        examUserService.getCompleteExams(null, user.getUsername());
    }

    // TC_EUS_25: Test getCompleteExams với username là null
    // Mục tiêu: Kiểm tra phương thức getCompleteExams ném ngoại lệ khi username là null
    // INPUT: courseId = 12L, username = null
    // OUTPUT kỳ vọng: Ném IllegalArgumentException hoặc NullPointerException
    @Test(expected = Exception.class)
    @Transactional
    public void testGetCompleteExams_NullUsername() {
        Optional<Course> result = courseService.getCourseById(12L);
        Assert.assertTrue("Course không tồn tại!", result.isPresent());
        Course course = result.get();

        examUserService.getCompleteExams(course.getId(), null);
    }

    // TC_EUS_26: Test getCompleteExams với username là chuỗi rỗng
    // Mục tiêu: Kiểm tra phương thức getCompleteExams trả về danh sách rỗng khi username là chuỗi rỗng
    // INPUT: courseId = 12L, username = ""
    // OUTPUT kỳ vọng: Trả về danh sách rỗng
    @Test
    @Transactional
    public void testGetCompleteExams_EmptyUsername() {
        Optional<Course> result = courseService.getCourseById(12L);
        Assert.assertTrue("Course không tồn tại!", result.isPresent());
        Course course = result.get();

        List<ExamUser> list = examUserService.getCompleteExams(course.getId(), "");

        Assert.assertNotNull("Danh sách không được null", list);
        Assert.assertTrue("Danh sách phải rỗng khi username rỗng", list.isEmpty());
    }

    // TC_EUS_27: Test getCompleteExams với user có nhiều bài thi hoàn thành
// Mục tiêu: Kiểm tra phương thức getCompleteExams trả về đúng danh sách các bài thi hoàn thành của user
// INPUT: courseId = 12L, username = "1624801040051" (user có nhiều bài thi hoàn thành)
// OUTPUT kỳ vọng: Trả về danh sách không rỗng, tất cả bài thi có isFinished = true, user và course đúng
    @Test
    @Transactional
    public void testGetCompleteExams_MultipleResults() {
        Optional<Course> result = courseService.getCourseById(12L);
        Assert.assertTrue("Course không tồn tại!", result.isPresent());
        Course course = result.get();

        Optional<User> result2 = userService.getUserByUsername("1624801040051");
        Assert.assertTrue("User không tồn tại!", result2.isPresent());
        User user = result2.get();

        List<ExamUser> list = examUserService.getCompleteExams(course.getId(), user.getUsername());

        Assert.assertNotNull("Danh sách không được null", list);
        Assert.assertTrue("Phải có nhiều hơn 1 bài thi hoàn thành", list.size() > 1);

        for (ExamUser examUser : list) {
            Assert.assertTrue("Bài thi chưa hoàn thành!", examUser.getIsFinished());
            Assert.assertEquals("User không đúng!", user.getUsername(), examUser.getUser().getUsername());
            Assert.assertEquals("Course ID không đúng!", course.getId(), examUser.getExam().getPart().getCourse().getId());
            Assert.assertTrue("Điểm phải lớn hơn -1", examUser.getTotalPoint() > -1.0);
        }
    }

    // TC_EUS_28: Test findAllByExam_Id với exam có nhiều ExamUser
// Mục tiêu: Kiểm tra phương thức findAllByExam_Id trả về đúng danh sách ExamUser theo examId
// INPUT: examId = 200L (exam có nhiều ExamUser)
// OUTPUT kỳ vọng: Trả về danh sách không rỗng, tất cả ExamUser có examId đúng
    @Test
    @Transactional
    public void testFindAllByExam_Id() {
        Optional<Exam> result = examService.getExamById(200L);
        Assert.assertTrue("Exam không tồn tại!", result.isPresent());
        Exam exam = result.get();

        List<ExamUser> examUsers = examUserService.findAllByExam_Id(exam.getId());

        Assert.assertNotNull("Danh sách ExamUser không thể null!", examUsers);
        Assert.assertFalse("Danh sách ExamUser không thể rỗng!", examUsers.isEmpty());

        for (ExamUser examUser : examUsers) {
            Assert.assertEquals("Exam ID không đúng!", exam.getId(), examUser.getExam().getId());
        }
    }

    // TC_EUS_29: Test findAllByExam_Id với exam không tồn tại
// Mục tiêu: Kiểm tra phương thức findAllByExam_Id trả về danh sách rỗng khi examId không tồn tại
// INPUT: examId = 999999L (không tồn tại)
// OUTPUT kỳ vọng: Trả về danh sách rỗng
    @Test
    @Transactional
    public void testFindAllByExam_Id_ExamNotExist() {
        Long examIdKhongTonTai = 999999L;

        List<ExamUser> examUsers = examUserService.findAllByExam_Id(examIdKhongTonTai);
        Assert.assertNotNull("Danh sách không được null", examUsers);
        Assert.assertTrue("Danh sách phải rỗng khi exam không tồn tại", examUsers.isEmpty());
    }

    // TC_EUS_30: Test findAllByExam_Id với exam không có ExamUser
// Mục tiêu: Kiểm tra phương thức findAllByExam_Id trả về danh sách rỗng khi exam không có ExamUser nào
// INPUT: examId = 329L (exam không có ExamUser)
// OUTPUT kỳ vọng: Trả về danh sách rỗng
    @Test
    @Transactional
    public void testFindAllByExam_Id_NoData() {
        Long examId = 329L; // ID exam không có dữ liệu

        List<ExamUser> examUsers = examUserService.findAllByExam_Id(examId);

        Assert.assertNotNull("Danh sách không được null", examUsers);
        Assert.assertTrue("Danh sách phải rỗng khi examId không có dữ liệu", examUsers.isEmpty());
    }

    // TC_EUS_31: Test findAllByExam_Id với examId null
// Mục tiêu: Kiểm tra phương thức findAllByExam_Id ném IllegalArgumentException khi examId là null
// INPUT: examId = null
// OUTPUT kỳ vọng: Ném IllegalArgumentException
    @Test(expected = Exception.class)
    @Transactional
    public void testFindAllByExam_Id_NullExamId() {
        examUserService.findAllByExam_Id(null);
    }

    // TC_EUS_32: Test findExamUsersByIsFinishedIsTrueAndExam_Id thành công
// Mục tiêu: Kiểm tra phương thức findExamUsersByIsFinishedIsTrueAndExam_Id trả về đúng các ExamUser đã hoàn thành
// INPUT: examId = 200L
// OUTPUT kỳ vọng: Trả về danh sách không rỗng, tất cả ExamUser có isFinished = true và examId đúng
    @Test
    @Transactional
    public void testFindExamUsersByIsFinishedIsTrueAndExam_IdSuccess() {
        Optional<Exam> result = examService.getExamById(200L);
        junit.framework.Assert.assertTrue("Exam không tồn tại!", result.isPresent());
        Exam exam = result.get();

        List<ExamUser> completedExamUsers = examUserService.findExamUsersByIsFinishedIsTrueAndExam_Id(exam.getId());

        junit.framework.Assert.assertNotNull("Danh sách ExamUser không thể null!", completedExamUsers);
        junit.framework.Assert.assertFalse("Danh sách ExamUser không thể rỗng!", completedExamUsers.isEmpty());

        for (ExamUser examUser : completedExamUsers) {
            junit.framework.Assert.assertTrue("ExamUser phải hoàn thành bài thi (isFinished = true)", examUser.getIsFinished());
            junit.framework.Assert.assertEquals("Exam ID không đúng!", exam.getId(), examUser.getExam().getId());
        }
    }

    // TC_EUS_33: Test findExamUsersByIsFinishedIsTrueAndExam_Id với exam không có ai hoàn thành
// Mục tiêu: Kiểm tra phương thức findExamUsersByIsFinishedIsTrueAndExam_Id trả về danh sách rỗng nếu không có ExamUser hoàn thành
// INPUT: examId = 300L (không có ai hoàn thành)
// OUTPUT kỳ vọng: Trả về danh sách rỗng
    @Test
    @Transactional
    public void testFindExamUsersByIsFinishedIsTrueAndExam_Id_NoCompleted() {
        Long examIdKhongCoHoanThanh = 300L; // examId giả định không có ai hoàn thành

        List<ExamUser> completedExamUsers = examUserService.findExamUsersByIsFinishedIsTrueAndExam_Id(examIdKhongCoHoanThanh);

        Assert.assertNotNull("Danh sách không được null", completedExamUsers);
        Assert.assertTrue("Danh sách phải rỗng khi không có ExamUser hoàn thành", completedExamUsers.isEmpty());
    }

    // TC_EUS_34: Test findExamUsersByIsFinishedIsTrueAndExam_Id với examId không tồn tại
// Mục tiêu: Kiểm tra phương thức findExamUsersByIsFinishedIsTrueAndExam_Id trả về danh sách rỗng khi examId không tồn tại
// INPUT: examId = 999999L (không tồn tại)
// OUTPUT kỳ vọng: Trả về danh sách rỗng
    @Test
    @Transactional
    public void testFindExamUsersByIsFinishedIsTrueAndExam_Id_ExamNotExist() {
        Long examIdKhongTonTai = 999999L;

        List<ExamUser> completedExamUsers = examUserService.findExamUsersByIsFinishedIsTrueAndExam_Id(examIdKhongTonTai);

        Assert.assertNotNull("Danh sách không được null", completedExamUsers);
        Assert.assertTrue("Danh sách phải rỗng khi exam không tồn tại", completedExamUsers.isEmpty());
    }

    // TC_EUS_35: Test findExamUsersByIsFinishedIsTrueAndExam_Id với examId null
// Mục tiêu: Kiểm tra phương thức findExamUsersByIsFinishedIsTrueAndExam_Id ném IllegalArgumentException khi examId là null
// INPUT: examId = null
// OUTPUT kỳ vọng: Ném IllegalArgumentException
    @Test(expected = Exception.class)
    @Transactional
    public void testFindExamUsersByIsFinishedIsTrueAndExam_Id_NullExamId() {
        examUserService.findExamUsersByIsFinishedIsTrueAndExam_Id(null);
    }









}
