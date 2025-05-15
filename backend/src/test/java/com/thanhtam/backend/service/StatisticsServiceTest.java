package com.thanhtam.backend.service;

import com.thanhtam.backend.entity.*;
import com.thanhtam.backend.repository.*;
import com.thanhtam.backend.ultilities.DifficultyLevel;
import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.Test;
import org.junit.jupiter.api.Assertions;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Transactional
@Rollback
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
public class StatisticsServiceTest {

    @Autowired
    private ExamUserRepository examUserRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ExamRepository examRepository;

    @Autowired
    private StatisticsService dashboardService;

    private User testUser;
    private Exam testExam;

    @Autowired
    private QuestionRepository questionRepository;

    @Autowired
    private QuestionTypeRepository questionTypeRepository;

    @Autowired
    private PartRepository partRepository;

    private Part part;

    @Before
    public void setUp() {
        examUserRepository.deleteAll();
        userRepository.deleteAll();
        examRepository.deleteAll();

        testUser = new User();
        testUser.setUsername("testuser");
        testUser.setEmail("test@example.com");
        testUser.setPassword("123456");
        userRepository.save(testUser);

        testExam = new Exam();
        examRepository.save(testExam);

        questionRepository.deleteAll();
        questionTypeRepository.deleteAll();
        partRepository.deleteAll();

        part = new Part();
        part.setName("Reading");
        partRepository.save(part);
    }

    /**
     * TC_EXU_01: Không có dữ liệu tuần này và tuần trước
     * Mục tiêu: Đảm bảo trả về 0.00 khi không có dữ liệu nào
     * Input: danh sách ExamUser rỗng
     * Output kỳ vọng: 0.00
     */
    @Test
    public void testGetChangeExamUser_NoData() {
        double change = dashboardService.getChangeExamUser();
        Assertions.assertEquals(0.00, change);
    }

    /**
     * TC_EXU_02: Có dữ liệu tuần trước, không có tuần này
     * Mục tiêu: Tính toán đúng tỷ lệ giảm 100%
     * Input: 2 bài thi kết thúc ở tuần trước
     * Output kỳ vọng: -200.00
     */
    @Test
    @Transactional
    public void testGetChangeExamUser_OnlyLastWeek() {
        saveExamUserWithTime(DateTime.now().minusWeeks(1).toDate());
        saveExamUserWithTime(DateTime.now().minusWeeks(1).plusDays(1).toDate());

        double change = dashboardService.getChangeExamUser();
        Assertions.assertEquals(-200.00, change);
    }

    /**
     * TC_EXU_03: Có dữ liệu tuần này, không có tuần trước
     * Mục tiêu: Tính toán đúng tỷ lệ tăng 100%
     * Input: 3 bài thi kết thúc trong tuần này
     * Output kỳ vọng: 300.00
     */
    @Test
    @Transactional
    public void testGetChangeExamUser_OnlyCurrentWeek() {
        saveExamUserWithTime(DateTime.now().toDate());
        saveExamUserWithTime(DateTime.now().minusDays(1).toDate());
        saveExamUserWithTime(DateTime.now().minusDays(2).toDate());

        double change = dashboardService.getChangeExamUser();
        Assertions.assertEquals(300.00, change);
    }

    /**
     * TC_EXU_04: Có cả dữ liệu tuần này và tuần trước
     * Mục tiêu: Tính đúng % thay đổi giữa hai tuần
     * Input: 2 tuần trước, 4 tuần này
     * Output kỳ vọng: 100.00
     */
    @Test
    @Transactional
    public void testGetChangeExamUser_BothWeeks() {
        // Tuần trước
        saveExamUserWithTime(DateTime.now().minusWeeks(1).toDate());
        saveExamUserWithTime(DateTime.now().minusWeeks(1).plusDays(1).toDate());

        // Tuần này
        saveExamUserWithTime(DateTime.now().toDate());
        saveExamUserWithTime(DateTime.now().minusDays(1).toDate());
        saveExamUserWithTime(DateTime.now().minusDays(2).toDate());
        saveExamUserWithTime(DateTime.now().minusDays(3).toDate());

        double change = dashboardService.getChangeExamUser();
        Assertions.assertEquals(100.00, change);
    }

    /**
     * TC_Q_01: Không có dữ liệu tuần này và tuần trước
     * Output kỳ vọng: 0.00
     */
    @Test
    public void testGetChangeQuestion_NoData() {
        double change = dashboardService.getChangeQuestion();
        Assertions.assertEquals(0.00, change);
    }

    /**
     * TC_Q_02: Chỉ có dữ liệu tuần trước
     * Output kỳ vọng: -200.00
     */
    @Test
    public void testGetChangeQuestion_OnlyLastWeek() {
        saveQuestionWithDate(DateTime.now().minusWeeks(1).toDate());
        saveQuestionWithDate(DateTime.now().minusWeeks(1).toDate());

        double change = dashboardService.getChangeQuestion();
        Assertions.assertEquals(-200.00, change);
    }

    /**
     * TC_Q_03: Chỉ có dữ liệu tuần này
     * Output kỳ vọng: 300.00
     */
    @Test
    public void testGetChangeQuestion_OnlyCurrentWeek() {
        saveQuestionWithDate(DateTime.now().toDate());
        saveQuestionWithDate(DateTime.now().minusDays(1).toDate());
        saveQuestionWithDate(DateTime.now().minusDays(2).toDate());

        double change = dashboardService.getChangeQuestion();
        Assertions.assertEquals(300.00, change);
    }

    /**
     * TC_Q_04: Có dữ liệu ở cả 2 tuần
     * Input: 2 tuần trước, 4 tuần này
     * Output kỳ vọng: 100.00
     */
    @Test
    public void testGetChangeQuestion_BothWeeks() {
        // Tuần trước
        saveQuestionWithDate(DateTime.now().minusWeeks(1).toDate());
        saveQuestionWithDate(DateTime.now().minusWeeks(1).toDate());

        // Tuần này
        saveQuestionWithDate(DateTime.now().toDate());
        saveQuestionWithDate(DateTime.now().toDate());
        saveQuestionWithDate(DateTime.now().toDate());
        saveQuestionWithDate(DateTime.now().toDate());

        double change = dashboardService.getChangeQuestion();
        Assertions.assertEquals(100.00, change);
    }

    /**
     * TC_ACC_01: Không có dữ liệu tuần này và tuần trước
     * Mục tiêu: Trả về 0.00 khi không có dữ liệu
     */
    @Test
    public void testGetChangeAccount_NoData() {
        userRepository.deleteAll();
        double change = dashboardService.getChangeAccount();
        Assertions.assertEquals(0.00, change);
    }

    /**
     * TC_ACC_02: Chỉ có dữ liệu tuần trước
     * Mục tiêu: Tính đúng tỷ lệ giảm 100%
     */
    @Test
    @Transactional
    public void testGetChangeAccount_OnlyLastWeek() {
        userRepository.deleteAll();
        saveUserWithDate("user1", DateTime.now().minusWeeks(1).toDate());
        saveUserWithDate("user2", DateTime.now().minusWeeks(1).toDate());
        System.out.println(userRepository.findAll().size() + " user size");
        double change = dashboardService.getChangeAccount();
        Assertions.assertEquals(-200.00, change);
    }

    /**
     * TC_ACC_03: Chỉ có dữ liệu tuần này
     * Mục tiêu: Tính đúng tỷ lệ tăng 100%
     */
    @Test
    @Transactional
    public void testGetChangeAccount_OnlyCurrentWeek() {
        userRepository.deleteAll();
        saveUserWithDate("user1", DateTime.now().toDate());
        saveUserWithDate("user2", DateTime.now().minusDays(1).toDate());
        saveUserWithDate("user3", DateTime.now().minusDays(2).toDate());

        double change = dashboardService.getChangeAccount();
        Assertions.assertEquals(300.00, change);
    }

    /**
     * TC_ACC_04: Có dữ liệu cả 2 tuần
     * Mục tiêu: Tính đúng % thay đổi giữa hai tuần
     * Input: 2 tuần trước, 4 tuần này → tăng 100%
     */
    @Test
    @Transactional
    public void testGetChangeAccount_BothWeeks() {
        userRepository.deleteAll();
        // Tuần trước
        saveUserWithDate("user1", DateTime.now().minusWeeks(1).toDate());
        saveUserWithDate("user2", DateTime.now().minusWeeks(1).toDate());

        // Tuần này
        saveUserWithDate("user3", DateTime.now().toDate());
        saveUserWithDate("user4", DateTime.now().toDate());
        saveUserWithDate("user5", DateTime.now().toDate());
        saveUserWithDate("user6", DateTime.now().toDate());
        System.out.println(userRepository.findAll().size() + " user size");
        double change = dashboardService.getChangeAccount();
        Assertions.assertEquals(100.00, change);
    }

    /**
     * TC_EXAM_01: Không có dữ liệu tuần này và tuần trước
     * Mục tiêu: Trả về 0.00 khi không có kỳ thi bị hủy nào
     */
    @Test
    public void testGetChangeExam_NoData() {
        examRepository.deleteAll();
        double change = dashboardService.getChangeExam();
        Assertions.assertEquals(0.00, change);
    }

    /**
     * TC_EXAM_02: Chỉ có dữ liệu tuần trước
     * Mục tiêu: Trả về âm 100% mỗi bản ghi
     */
    @Test
    @Transactional
    public void testGetChangeExam_OnlyLastWeek() {
        examRepository.deleteAll();
        saveExamWithDate(DateTime.now().minusWeeks(1).toDate(), true);
        saveExamWithDate(DateTime.now().minusWeeks(1).toDate(), true);

        double change = dashboardService.getChangeExam();
        Assertions.assertEquals(-200.00, change);
    }

    /**
     * TC_EXAM_03: Chỉ có dữ liệu tuần này
     * Mục tiêu: Trả về dương 100% mỗi bản ghi
     */
    @Test
    @Transactional
    public void testGetChangeExam_OnlyCurrentWeek() {
        examRepository.deleteAll();
        saveExamWithDate(DateTime.now().toDate(), true);
        saveExamWithDate(DateTime.now().toDate(), true);

        double change = dashboardService.getChangeExam();
        Assertions.assertEquals(200.00, change);
    }

    /**
     * TC_EXAM_04: Có cả tuần này và tuần trước
     * Mục tiêu: Tính đúng tỷ lệ thay đổi
     * Tuần trước: 2, tuần này: 4 → tăng 100%
     */
    @Test
    @Transactional
    public void testGetChangeExam_BothWeeks() {
        examRepository.deleteAll();
        // Tuần trước
        saveExamWithDate(DateTime.now().minusWeeks(1).toDate(), true);
        saveExamWithDate(DateTime.now().minusWeeks(1).toDate(), true);

        // Tuần này
        saveExamWithDate(DateTime.now().toDate(), true);
        saveExamWithDate(DateTime.now().toDate(), true);
        saveExamWithDate(DateTime.now().toDate(), true);
        saveExamWithDate(DateTime.now().toDate(), true);

        double change = dashboardService.getChangeExam();
        Assertions.assertEquals(100.00, change);
    }

    /**
     * TC_EXAM_05: Có kỳ thi không bị hủy → không tính
     */
    @Test
    @Transactional
    public void testGetChangeExam_IgnoresNotCanceled() {
        // Bị hủy
        saveExamWithDate(DateTime.now().toDate(), true);
        // Không bị hủy
        saveExamWithDate(DateTime.now().toDate(), false);

        double change = dashboardService.getChangeExam();
        Assertions.assertEquals(100.00, change); // chỉ có 1 bị hủy
    }

    /**
     * TC_EXAMUSER_01: Không có dữ liệu
     * Mục tiêu: Trả về danh sách 7 số 0
     */
    @Test
    public void testCountExamUserLastedSevenDaysTotal_NoData() {
        List<Long> result = dashboardService.countExamUserLastedSevenDaysTotal();
        Assertions.assertEquals(Arrays.asList(0L, 0L, 0L, 0L, 0L, 0L, 0L), result);
    }

    /**
     * TC_EXAMUSER_02: Có dữ liệu trong các ngày khác nhau
     * Mục tiêu: Trả về đúng số lượng theo từng ngày
     */
    @Test
    @Transactional
    public void testCountExamUserLastedSevenDaysTotal_WithData() {
        // Hôm nay
        saveExamUserWithFinishDate(DateTime.now().toDate()); // index 6

        // Hôm qua
        saveExamUserWithFinishDate(DateTime.now().minusDays(1).toDate()); // index 5
        saveExamUserWithFinishDate(DateTime.now().minusDays(1).toDate());

        // 2 ngày trước
        saveExamUserWithFinishDate(DateTime.now().minusDays(2).toDate()); // index 4

        List<Long> result = dashboardService.countExamUserLastedSevenDaysTotal();

        // index: 0  1  2  3  4  5  6
        // ngày:   -6 -5 -4 -3 -2 -1 0
        Assertions.assertEquals(Arrays.asList(0L, 0L, 0L, 0L, 1L, 2L, 1L), result);
    }

    /**
     * TC_EXAMUSER_03: Dữ liệu ngoài 7 ngày không tính
     */
    @Test
    @Transactional
    public void testCountExamUserLastedSevenDaysTotal_OlderDataIgnored() {
        saveExamUserWithFinishDate(DateTime.now().minusDays(8).toDate()); // không tính
        saveExamUserWithFinishDate(DateTime.now().minusDays(9).toDate());

        List<Long> result = dashboardService.countExamUserLastedSevenDaysTotal();
        Assertions.assertEquals(Arrays.asList(0L, 0L, 0L, 0L, 0L, 0L, 0L), result);
    }

    private void saveExamUserWithFinishDate(Date finishDate) {
        ExamUser examUser = new ExamUser();
        examUser.setTimeFinish(finishDate);
        examUserRepository.save(examUser);
    }

    private void saveExamWithDate(Date createdDate, boolean canceled) {
        Exam exam = new Exam();
        exam.setCreatedDate(createdDate);
        exam.setCanceled(canceled);
        examRepository.save(exam);
    }

    private void saveUserWithDate(String username, Date createdDate) {
        User user = new User();
        user.setUsername(username);
        user.setCreatedDate(createdDate);
        user.setDeleted(false);
        userRepository.save(user);
    }

    private void saveQuestionWithDate(Date createdDate) {
        Question question = new Question();
        question.setQuestionText("Test question?");
        question.setDifficultyLevel(DifficultyLevel.EASY); // hoặc null nếu cần
        question.setPoint(5);
        question.setDeleted(false);

        questionRepository.save(question);
    }

    private void saveExamUserWithTime(Date timeFinish) {
        ExamUser examUser = new ExamUser();
        examUser.setUser(testUser);
        examUser.setExam(testExam);
        examUser.setIsStarted(true);
        examUser.setTimeStart(new Date(timeFinish.getTime() - 600000));
        examUser.setTimeFinish(timeFinish);
        examUser.setIsFinished(true);
        examUser.setAnswerSheet("{}");
        examUser.setRemainingTime(0);
        examUser.setTotalPoint(10.0);
        examUserRepository.save(examUser);
    }

}
