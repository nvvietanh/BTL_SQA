package com.thanhtam.backend.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.thanhtam.backend.dto.*;
import com.thanhtam.backend.entity.*;
import com.thanhtam.backend.service.*;
import com.thanhtam.backend.ultilities.DifficultyLevel;
import com.thanhtam.backend.ultilities.ERole;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.junit4.SpringRunner;

import javax.persistence.EntityNotFoundException;
import java.io.IOException;
import java.text.ParseException;
import java.util.*;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@RunWith(SpringRunner.class)
public class ExamControllerTest1 {

    private static final Logger logger = LoggerFactory.getLogger(ExamController.class);

    @Mock
    private ExamService examService;
    @Mock
    private ExamUserService examUserService;
    @Mock
    private UserService userService;
    @Mock
    private QuestionService questionService;
    @Mock
    private IntakeService intakeService;
    @Mock
    private PartService partService;
    @Mock
    private ObjectMapper mockedMapper;

    private ObjectMapper realMapper = new ObjectMapper();

    @InjectMocks
    private ExamController examController;

    private User studentUser, adminUser, lecturerUser, anotherUser;
    private Exam exam, examFuture, examPast, examLocked, examForCancel, examCannotCancel;
    private ExamUser examUser, examUserFuture, examUserPast, examUserCompletedScoreNotSet,
            examUserCompletedScoreSet, examUserInProgress, examUserNotStartedCanStart,
            examUserNotStartedMissed, examUserNoAnswers;
    private Question question1, question2;
    private AnswerSheet answerSheetQ1Correct, answerSheetQ1Incorrect, answerSheetQ2Correct;
    private ExamQuestionPoint examQuestionPoint1, examQuestionPoint2;
    private Date currentDate;
    private Part part;
    private Course course;
    private Role studentRole, adminRole, lecturerRole;
    private Pageable pageable;
    private Intake intake;

    @Before
    public void setUp() throws JsonProcessingException {
        MockitoAnnotations.openMocks(this);
        examController.mapper = mockedMapper;

        currentDate = new Date();

        adminRole = new Role(1L, ERole.ROLE_ADMIN);
        lecturerRole = new Role(2L, ERole.ROLE_LECTURER);
        studentRole = new Role(3L, ERole.ROLE_STUDENT);

        adminUser = new User(); adminUser.setId(1L); adminUser.setUsername("admin"); adminUser.setRoles(Collections.singleton(adminRole));
        lecturerUser = new User(); lecturerUser.setId(2L); lecturerUser.setUsername("lecturer"); lecturerUser.setRoles(Collections.singleton(lecturerRole));
        studentUser = new User(); studentUser.setId(3L); studentUser.setUsername("student1"); studentUser.setRoles(Collections.singleton(studentRole));
        anotherUser = new User(); anotherUser.setId(4L); anotherUser.setUsername("student2"); anotherUser.setRoles(Collections.singleton(studentRole));

        course = new Course(1L, "COURSE101", "Test Course", "course.png", null);
        part = new Part(1L, "Part A", course);
        intake = new Intake(1L, "INTAKE01", "Intake 01");

        String defaultQuestionData = realMapper.writeValueAsString(Arrays.asList(
                new ExamQuestionPoint() {{ setQuestionId(1L); setPoint(10); }},
                new ExamQuestionPoint() {{ setQuestionId(2L); setPoint(5); }}
        ));

        exam = new Exam();
        exam.setId(1L); exam.setTitle("Current Exam"); exam.setQuestionData(defaultQuestionData);
        exam.setDurationExam(60); exam.setBeginExam(new Date(currentDate.getTime() - 3600000));
        exam.setFinishExam(new Date(currentDate.getTime() + 3600000));
        exam.setPart(part); exam.setIntake(intake); exam.setCreatedBy(adminUser); exam.setLocked(false);

        examFuture = new Exam();
        examFuture.setId(2L); examFuture.setTitle("Future Exam"); examFuture.setPart(part); examFuture.setIntake(intake);
        examFuture.setBeginExam(new Date(currentDate.getTime() + 7200000));
        examFuture.setFinishExam(new Date(currentDate.getTime() + 10800000)); examFuture.setLocked(false);  examFuture.setQuestionData("[]");

        examPast = new Exam();
        examPast.setId(3L); examPast.setTitle("Past Exam"); examPast.setPart(part); examPast.setIntake(intake);
        examPast.setBeginExam(new Date(currentDate.getTime() - 10800000));
        examPast.setFinishExam(new Date(currentDate.getTime() - 7200000)); examPast.setLocked(true); examPast.setQuestionData("[]");

        examLocked = new Exam();
        examLocked.setId(4L); examLocked.setTitle("Locked Exam"); examLocked.setPart(part); examLocked.setIntake(intake);
        examLocked.setBeginExam(new Date(currentDate.getTime() - 3600000));
        examLocked.setFinishExam(new Date(currentDate.getTime() + 3600000)); examLocked.setLocked(true); examLocked.setQuestionData("[]");

        examForCancel = new Exam(); examForCancel.setId(5L); examForCancel.setTitle("Cancellable Exam");
        examForCancel.setBeginExam(new Date(currentDate.getTime() + 3600000)); // Future
        examForCancel.setPart(part); examForCancel.setIntake(intake); examForCancel.setQuestionData("[]");

        examCannotCancel = new Exam(); examCannotCancel.setId(6L); examCannotCancel.setTitle("Uncancellable Exam");
        examCannotCancel.setBeginExam(new Date(currentDate.getTime() - 3600000)); // Past
        examCannotCancel.setPart(part); examCannotCancel.setIntake(intake); examCannotCancel.setQuestionData("[]");


        examUser = new ExamUser();
        examUser.setId(1L); examUser.setExam(exam); examUser.setUser(studentUser);
        examUser.setIsStarted(true); examUser.setIsFinished(false); examUser.setTotalPoint(-1.0);
        examUser.setRemainingTime(1800); examUser.setTimeStart(new Date(currentDate.getTime() - 1800000));
        examUser.setAnswerSheet(realMapper.writeValueAsString(Collections.singletonList(new AnswerSheet(1L, Collections.emptyList(), 10))));

        examUserFuture = new ExamUser(); examUserFuture.setId(2L); examUserFuture.setExam(examFuture); examUserFuture.setUser(studentUser);
        examUserPast = new ExamUser(); examUserPast.setId(3L); examUserPast.setExam(examPast); examUserPast.setUser(studentUser);

        examUserCompletedScoreNotSet = new ExamUser();
        examUserCompletedScoreNotSet.setId(4L); examUserCompletedScoreNotSet.setExam(exam); examUserCompletedScoreNotSet.setUser(studentUser);
        examUserCompletedScoreNotSet.setIsStarted(true); examUserCompletedScoreNotSet.setIsFinished(true);
        examUserCompletedScoreNotSet.setTotalPoint(-1.0);
        examUserCompletedScoreNotSet.setTimeStart(new Date(currentDate.getTime() - 3600000));
        examUserCompletedScoreNotSet.setTimeFinish(new Date(currentDate.getTime() - 60000));
        examUserCompletedScoreNotSet.setAnswerSheet(realMapper.writeValueAsString(Collections.singletonList(new AnswerSheet(1L, Collections.emptyList(), 10))));

        examUserCompletedScoreSet = new ExamUser();
        examUserCompletedScoreSet.setId(5L); examUserCompletedScoreSet.setExam(exam); examUserCompletedScoreSet.setUser(anotherUser);
        examUserCompletedScoreSet.setIsStarted(true); examUserCompletedScoreSet.setIsFinished(true);
        examUserCompletedScoreSet.setTotalPoint(20.0);
        examUserCompletedScoreSet.setAnswerSheet(realMapper.writeValueAsString(Collections.singletonList(new AnswerSheet(1L, Collections.emptyList(), 10))));

        examUserInProgress = new ExamUser();
        examUserInProgress.setId(6L); examUserInProgress.setExam(exam); examUserInProgress.setUser(studentUser);
        examUserInProgress.setIsStarted(true); examUserInProgress.setIsFinished(false);
        examUserInProgress.setTotalPoint(-1.0); examUserInProgress.setAnswerSheet("[]"); examUserInProgress.setRemainingTime(1800);

        examUserNotStartedCanStart = new ExamUser();
        examUserNotStartedCanStart.setId(7L); examUserNotStartedCanStart.setExam(examFuture); // Linked to future exam
        examUserNotStartedCanStart.setUser(anotherUser); examUserNotStartedCanStart.setIsStarted(false); examUserNotStartedCanStart.setIsFinished(false);
        examUserNotStartedCanStart.setAnswerSheet("[]");

        examUserNotStartedMissed = new ExamUser();
        examUserNotStartedMissed.setId(8L); examUserNotStartedMissed.setExam(examPast); // Linked to past exam
        examUserNotStartedMissed.setUser(studentUser); examUserNotStartedMissed.setIsStarted(false); examUserNotStartedMissed.setIsFinished(false);
        examUserNotStartedMissed.setAnswerSheet("[]");

        examUserNoAnswers = new ExamUser();
        examUserNoAnswers.setId(9L); examUserNoAnswers.setExam(exam); examUserNoAnswers.setUser(anotherUser);
        examUserNoAnswers.setIsStarted(true); examUserNoAnswers.setIsFinished(true); examUserNoAnswers.setTotalPoint(-1.0);
        examUserNoAnswers.setAnswerSheet("[]");


        QuestionType mcqType = new QuestionType(); mcqType.setId(1L); mcqType.setDescription("Multiple Choice Questions");
        question1 = new Question(); question1.setId(1L); question1.setQuestionText("Q1"); question1.setPoint(10); question1.setDifficultyLevel(DifficultyLevel.EASY); question1.setQuestionType(mcqType);
        question2 = new Question(); question2.setId(2L); question2.setQuestionText("Q2"); question2.setPoint(5); question2.setDifficultyLevel(DifficultyLevel.MEDIUM); question2.setQuestionType(mcqType);

        answerSheetQ1Correct = new AnswerSheet(1L, Collections.emptyList(), 10);
        answerSheetQ1Incorrect = new AnswerSheet(1L, Collections.emptyList(), 0);
        answerSheetQ2Correct = new AnswerSheet(2L, Collections.emptyList(), 5);

        examQuestionPoint1 = new ExamQuestionPoint(); examQuestionPoint1.setQuestionId(1L); examQuestionPoint1.setPoint(10);
        examQuestionPoint2 = new ExamQuestionPoint(); examQuestionPoint2.setQuestionId(2L); examQuestionPoint2.setPoint(5);

        pageable = PageRequest.of(0, 10);
    }

    private void setupMockAuthentication(String username, String roleName) {
        SecurityContextHolder.clearContext();
        long userId = 1L;
        if ("admin".equals(username)) userId = 1L;
        else if ("lecturer".equals(username)) userId = 2L;
        else if ("student1".equals(username)) userId = 3L;
        else if ("student2".equals(username)) userId = 4L;
        else userId = 5L; // Default for unknown

        UserDetailsImpl userDetails = new UserDetailsImpl(
                userId, username, username + "@example.com", "password",
                Collections.singletonList(new SimpleGrantedAuthority(roleName))
        );
        Authentication authentication = new UsernamePasswordAuthenticationToken(
                userDetails, null, userDetails.getAuthorities()
        );
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }

    //region TC_EC_GETEXAMSBYPAGE - Test cases for getExamsByPage
    /**
     * @Mã Unit Test: TC_EC_01
     * @Trường hợp test: Lấy danh sách exam với vai trò ADMIN.
     * @Input: Pageable. User đăng nhập là "admin" với vai trò "ROLE_ADMIN".
     * @Output kỳ vọng: PageResult chứa danh sách tất cả exam, gọi examService.findAll(). Nhánh if (isAdmin) được thực thi.
     */
    @Test
    public void testGetExamsByPage_AsAdmin_IfBranch() {
        setupMockAuthentication("admin", "ROLE_ADMIN");
        Page<Exam> adminExamsPage = new PageImpl<>(Collections.singletonList(exam), pageable, 1);
        when(userService.getUserName()).thenReturn("admin");
        when(userService.getUserByUsername("admin")).thenReturn(Optional.of(adminUser)); // adminUser có ROLE_ADMIN
        when(examService.findAll(pageable)).thenReturn(adminExamsPage);

        PageResult result = examController.getExamsByPage(pageable);

        assertNotNull(result);
        assertEquals(1, result.getData().size());
        verify(examService, times(1)).findAll(pageable);
        verify(examService, never()).findAllByCreatedBy_Username(any(Pageable.class), anyString());
    }

    /**
     * @Mã Unit Test: TC_EC_02
     * @Trường hợp test: Lấy danh sách exam với vai trò không phải ADMIN (ví dụ LECTURER).
     * @Input: Pageable. User đăng nhập là "lecturer" với vai trò "ROLE_LECTURER".
     * @Output kỳ vọng: PageResult chứa danh sách exam tạo bởi lecturer đó, gọi examService.findAllByCreatedBy_Username(). Nhánh else của if (isAdmin) được thực thi.
     */
    @Test
    public void testGetExamsByPage_AsLecturer_ElseBranch() {
        setupMockAuthentication("lecturer", "ROLE_LECTURER");
        Page<Exam> lecturerExamsPage = new PageImpl<>(Collections.singletonList(exam), pageable, 1);
        when(userService.getUserName()).thenReturn("lecturer");
        when(userService.getUserByUsername("lecturer")).thenReturn(Optional.of(lecturerUser)); // lecturerUser không có ROLE_ADMIN
        when(examService.findAllByCreatedBy_Username(pageable, "lecturer")).thenReturn(lecturerExamsPage);

        PageResult result = examController.getExamsByPage(pageable);

        assertNotNull(result);
        assertEquals(1, result.getData().size());
        verify(examService, never()).findAll(pageable);
        verify(examService, times(1)).findAllByCreatedBy_Username(pageable, "lecturer");
    }


    //region TC_EC_GETALLBYUSER - Test cases for getAllByUser
    /**
     * @Mã Unit Test: TC_EC_03
     * @Trường hợp test: User không có exam nào.
     * @Input: Username. examUserService.getExamListByUsername trả về danh sách rỗng.
     * @Output kỳ vọng: ResponseEntity OK với danh sách ExamUser rỗng. Vòng lặp examUserList.forEach không chạy.
     */
    @Test
    public void testGetAllByUser_NoExams_ForEachEmpty() {
        setupMockAuthentication("student1", "ROLE_STUDENT");
        when(examUserService.getExamListByUsername("student1")).thenReturn(Collections.emptyList());
        ResponseEntity<List<ExamUser>> response = examController.getAllByUser();
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody().isEmpty());
    }

    /**
     * @Mã Unit Test: TC_EC_04
     * @Trường hợp test: User có một exam chưa đến giờ bắt đầu (currentDate < beginExam).
     * @Input: Username. examUserService.getExamListByUsername trả về 1 ExamUser với exam.beginExam trong tương lai.
     * @Output kỳ vọng: exam.isLocked() là false. Vòng lặp forEach chạy 1 lần, nhánh `if (currentDate.compareTo(...) < 0)` đúng.
     */
    @Test
    public void testGetAllByUser_OneExam_BeforeBegin_IfBranch() {
        setupMockAuthentication("student1", "ROLE_STUDENT");
        when(examUserService.getExamListByUsername("student1")).thenReturn(Collections.singletonList(examUserFuture));
        ResponseEntity<List<ExamUser>> response = examController.getAllByUser();
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertFalse(response.getBody().get(0).getExam().isLocked());
    }

    /**
     * @Mã Unit Test: TC_EC_05
     * @Trường hợp test: User có một exam đã qua giờ bắt đầu (currentDate >= beginExam).
     * @Input: Username. examUserService.getExamListByUsername trả về 1 ExamUser với exam.beginExam trong quá khứ.
     * @Output kỳ vọng: exam.isLocked() là true. Vòng lặp forEach chạy 1 lần, nhánh `else` của `if (currentDate.compareTo(...) < 0)` đúng.
     */
    @Test
    public void testGetAllByUser_OneExam_AfterBegin_ElseBranch() {
        setupMockAuthentication("student1", "ROLE_STUDENT");
        when(examUserService.getExamListByUsername("student1")).thenReturn(Collections.singletonList(examUserPast));
        ResponseEntity<List<ExamUser>> response = examController.getAllByUser();
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody().get(0).getExam().isLocked());
    }
    //endregion

    //region TC_EC_GETEXAMUSERBYID - Test cases for getExamUserById
    /**
     * @Mã Unit Test: TC_EC_06
     * @Trường hợp test: Tìm thấy ExamUser.
     * @Input: examId, username. examUserService.findByExamAndUser trả về ExamUser.
     * @Output kỳ vọng: ResponseEntity OK với ExamUser. Nhánh `if (!examUser.isPresent())` là false.
     */
    @Test
    public void testGetExamUserById_Found_IfBranchFalse() throws ParseException {
        setupMockAuthentication("student1", "ROLE_STUDENT");
        when(userService.getUserName()).thenReturn("student1");
        when(examUserService.findByExamAndUser(1L, "student1")).thenReturn(examUser);
        ResponseEntity<ExamUser> response = examController.getExamUserById(1L);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(examUser, response.getBody());
    }

    /**
     * @Mã Unit Test: TC_EC_07
     * @Trường hợp test: Không tìm thấy ExamUser.
     * @Input: examId, username. examUserService.findByExamAndUser trả về null.
     * @Output kỳ vọng: ResponseEntity NOT_FOUND. Nhánh `if (!examUser.isPresent())` là true.
     */
    @Test
    public void testGetExamUserById_NotFound_IfBranchTrue() throws ParseException {
        setupMockAuthentication("student1", "ROLE_STUDENT");
        when(userService.getUserName()).thenReturn("student1");
        when(examUserService.findByExamAndUser(1L, "student1")).thenReturn(null);
        ResponseEntity<ExamUser> response = examController.getExamUserById(1L);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }
    //endregion

    //region TC_EC_GETALLQUESTIONS - Test cases for getAllQuestions

    /**
     * @Mã Unit Test: TC_EC_08
     * @Trường hợp test: Exam không tìm thấy.
     * @Input: examId không tồn tại.
     * @Output kỳ vọng: ResponseEntity NOT_FOUND. Nhánh `if (!exam.isPresent())` đúng.
     */
    @Test
    public void testGetAllQuestions_ExamNotFound() throws IOException {
        setupMockAuthentication("student1", "ROLE_STUDENT");
        when(userService.getUserName()).thenReturn("student1");
        when(examService.getExamById(1L)).thenReturn(Optional.empty());
        ResponseEntity<ExamQuestionList> response = examController.getAllQuestions(1L);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    /**
     * @Mã Unit Test: TC_EC_09
     * @Trường hợp test: Exam bị khóa.
     * @Input: examId hợp lệ, exam.isLocked() là true.
     * @Output kỳ vọng: ResponseEntity BAD_REQUEST. Nhánh `if (exam.get().isLocked() == true ...)` đúng.
     */
    @Test
    public void testGetAllQuestions_ExamIsLocked() throws IOException {
        setupMockAuthentication("student1", "ROLE_STUDENT");
        examLocked.setLocked(true); // Explicitly set for clarity
        when(userService.getUserName()).thenReturn("student1");
        when(examService.getExamById(examLocked.getId())).thenReturn(Optional.of(examLocked));
        ResponseEntity<ExamQuestionList> response = examController.getAllQuestions(examLocked.getId());
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    /**
     * @Mã Unit Test: TC_EC_10
     * @Trường hợp test: Exam chưa đến giờ bắt đầu.
     * @Input: examId hợp lệ, exam.beginExam > currentDate.
     * @Output kỳ vọng: ResponseEntity BAD_REQUEST. Nhánh `if (... || exam.get().getBeginExam().compareTo(currentTime) > 0)` đúng.
     */
    @Test
    public void testGetAllQuestions_ExamNotYetBegun() throws IOException {
        setupMockAuthentication("student1", "ROLE_STUDENT");
        examFuture.setLocked(false); // Ensure not locked for this specific condition
        when(userService.getUserName()).thenReturn("student1");
        when(examService.getExamById(examFuture.getId())).thenReturn(Optional.of(examFuture));
        ResponseEntity<ExamQuestionList> response = examController.getAllQuestions(examFuture.getId());
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    /**
     * @Mã Unit Test: TC_EC_11
     * @Trường hợp test: User đã bắt đầu bài thi, answerSheet hợp lệ.
     * @Input: examId, username. examUser.isStarted = true.
     * @Output kỳ vọng: ResponseEntity OK. Nhánh `if (examUser.getIsStarted().equals(true))` được thực thi. Vòng lặp `choiceUsers.forEach` chạy.
     */
    @Test
    public void testGetAllQuestions_UserStarted_ValidAnswerSheet() throws IOException {
        setupMockAuthentication("student1", "ROLE_STUDENT");
        examUser.setIsStarted(true);
        examUser.setAnswerSheet(realMapper.writeValueAsString(Arrays.asList(answerSheetQ1Correct, answerSheetQ2Correct)));
        when(userService.getUserName()).thenReturn("student1");
        when(examService.getExamById(1L)).thenReturn(Optional.of(exam));
        when(examUserService.findByExamAndUser(1L, "student1")).thenReturn(examUser);
        when(mockedMapper.readValue(examUser.getAnswerSheet(), new TypeReference<List<AnswerSheet>>() {}))
                .thenReturn(Arrays.asList(answerSheetQ1Correct, answerSheetQ2Correct));
        when(questionService.getQuestionById(1L)).thenReturn(Optional.of(question1));
        when(questionService.getQuestionById(2L)).thenReturn(Optional.of(question2));

        ResponseEntity<ExamQuestionList> response = examController.getAllQuestions(1L);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(2, response.getBody().getQuestions().size());
    }

    /**
     * @Mã Unit Test: TC_EC_12
     * @Trường hợp test: User mới làm bài, exam không shuffle.
     * @Input: examId, username. examUser.isStarted = false, exam.isShuffle = false.
     * @Output kỳ vọng: ResponseEntity OK. Nhánh `else` cuối cùng của `getAllQuestions` được thực thi.
     */
    @Test
    public void testGetAllQuestions_NewExam_ShuffleFalse() throws IOException {
        setupMockAuthentication("student1", "ROLE_STUDENT");
        exam.setShuffle(false); examUser.setIsStarted(false);
        // Tương tự testGetAllQuestions_NewExam_ShuffleTrue nhưng không có Collections.shuffle
        List<ExamQuestionPoint> eqpList = Arrays.asList(examQuestionPoint1, examQuestionPoint2);
        List<Question> qList = Arrays.asList(question1, question2);
        List<AnswerSheet> asList = Arrays.asList(answerSheetQ1Correct, answerSheetQ2Correct);
        String asJson = realMapper.writeValueAsString(asList);

        when(userService.getUserName()).thenReturn("student1");
        when(examService.getExamById(1L)).thenReturn(Optional.of(exam));
        when(examUserService.findByExamAndUser(1L, "student1")).thenReturn(examUser);
        when(mockedMapper.readValue(exam.getQuestionData(), new TypeReference<List<ExamQuestionPoint>>() {})).thenReturn(eqpList);
        when(questionService.getQuestionPointList(eqpList)).thenReturn(qList); // No anyList here
        when(questionService.convertFromQuestionList(qList)).thenReturn(asList);
        when(mockedMapper.writeValueAsString(asList)).thenReturn(asJson);
        when(questionService.getQuestionById(1L)).thenReturn(Optional.of(question1));
        when(questionService.getQuestionById(2L)).thenReturn(Optional.of(question2));

        ResponseEntity<ExamQuestionList> response = examController.getAllQuestions(1L);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(2, response.getBody().getQuestions().size());
        assertTrue(examUser.getIsStarted());
        verify(examUserService, times(2)).update(examUser);
    }


    //endregion

    //region TC_EC_CREATEEXAM - Test cases for createExam
     /**
     * @Mã Unit Test: TC_EC_13
     * @Trường hợp test: Tạo exam thành công với intake và part hợp lệ.
     * @Input: Exam object, intakeId, partId, isShuffle, locked.
     * @Output kỳ vọng: ResponseEntity OK với exam đã tạo. Các nhánh `if (intake.isPresent())` và `if (part.isPresent())` đều đúng.
     */
    @Test
    public void testCreateExam_Success_WithIntakeAndPart() throws IOException {
        setupMockAuthentication("admin", "ROLE_ADMIN");
        Exam examToSave = new Exam(); examToSave.setTitle("New Created Exam"); examToSave.setQuestionData("[]");
        List<User> usersInIntake = Collections.singletonList(studentUser);

        when(userService.getUserName()).thenReturn("admin");
        when(userService.getUserByUsername("admin")).thenReturn(Optional.of(adminUser));
        when(intakeService.findById(1L)).thenReturn(Optional.of(intake));
        when(partService.findPartById(1L)).thenReturn(Optional.of(part));
        when(userService.findAllByIntakeId(1L)).thenReturn(usersInIntake);
        when(examService.saveExam(any(Exam.class))).thenReturn(examToSave); // Giả lập save
        when(mockedMapper.readValue(eq("[]"), any(TypeReference.class))).thenReturn(Collections.emptyList());

        ResponseEntity<?> response = examController.createExam(examToSave, 1L, 1L, true, false);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        Exam createdExam = (Exam) response.getBody();
        assertEquals(adminUser, createdExam.getCreatedBy());
        assertEquals(intake, createdExam.getIntake());
        assertEquals(part, createdExam.getPart());
        assertTrue(createdExam.isShuffle());
        verify(examUserService).create(createdExam, usersInIntake);
    }

    /**
     * @Mã Unit Test: TC_EC_14
     * @Trường hợp test: Tạo exam thành công, intake không tồn tại.
     * @Input: Exam object, intakeId (không tồn tại), partId.
     * @Output kỳ vọng: ResponseEntity OK, exam.intake là null. Nhánh `if (intake.isPresent())` là false.
     */
    @Test
    public void testCreateExam_IntakeNotPresent() throws IOException {
        setupMockAuthentication("admin", "ROLE_ADMIN");
        Exam examToSave = new Exam(); examToSave.setTitle("Exam No Intake"); examToSave.setQuestionData("[]");
        when(userService.getUserName()).thenReturn("admin");
        when(userService.getUserByUsername("admin")).thenReturn(Optional.of(adminUser));
        when(intakeService.findById(1L)).thenReturn(Optional.empty()); // Intake không có
        when(partService.findPartById(1L)).thenReturn(Optional.of(part));
        when(mockedMapper.readValue(eq("[]"), any(TypeReference.class))).thenReturn(Collections.emptyList());


        ResponseEntity<?> response = examController.createExam(examToSave, 1L, 1L, false, false);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNull(((Exam) response.getBody()).getIntake());
    }


    //region TC_EC_GETEXAMBYID - Test cases for getExamById
    /**
     * @Mã Unit Test: TC_EC_15
     * @Trường hợp test: Tìm thấy exam.
     * @Input: examId hợp lệ.
     * @Output kỳ vọng: ResponseEntity OK với Exam. Nhánh `if (!exam.isPresent())` là false.
     */
    @Test
    public void testGetExamById_Found() {
        when(examService.getExamById(1L)).thenReturn(Optional.of(exam));
        ResponseEntity<Exam> response = examController.getExamById(1L);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(exam, response.getBody());
    }

    /**
     * @Mã Unit Test: TC_EC_16
     * @Trường hợp test: Không tìm thấy exam.
     * @Input: examId không tồn tại.
     * @Output kỳ vọng: ResponseEntity NO_CONTENT (do code gốc gọi .get() trên Optional rỗng sẽ gây lỗi, nên test này phải giả định code được sửa hoặc chấp nhận lỗi). Nếu code gốc được sửa, status code có thể là NOT_FOUND. Hiện tại, nó sẽ ném NoSuchElementException.
     */
    @Test(expected = NoSuchElementException.class)
    public void testGetExamById_NotFound_OriginalBehavior() {
        when(examService.getExamById(99L)).thenReturn(Optional.empty());
        examController.getExamById(99L); // Gây lỗi vì .get() trên Optional rỗng
    }
    //endregion

    //region TC_EC_SAVEUSEREXAMANSWER - Test cases for saveUserExamAnswer
/**
     * @Mã Unit Test: TC_EC_17
     * @Trường hợp test: ExamUser không tìm thấy.
     * @Input: examId, username không khớp.
     * @Output kỳ vọng: Trả về Exception. Nhánh `if (!examUser.isPresent())` là true.
     */
    @Test(expected = EntityNotFoundException.class)
    public void testSaveUserExamAnswer_ExamUserNotFound() throws JsonProcessingException {
        setupMockAuthentication("student1", "ROLE_STUDENT");
        when(examUserService.findByExamAndUser(1L, "student1")).thenReturn(null);
        examController.saveUserExamAnswer(Collections.emptyList(), 1L, false, 0);
    }

    /**
     * @Mã Unit Test: TC_EC_18
     * @Trường hợp test: Exam đã được user hoàn thành trước đó.
     * @Input: examId, username. examUser.isFinished = true.
     * @Output kỳ vọng: Trả về Exception . Nhánh `if (examUser.get().getIsFinished())` là true.
     */
    @Test(expected = ExceptionInInitializerError.class)
    public void testSaveUserExamAnswer_ExamAlreadyFinishedByUser() throws JsonProcessingException {
        setupMockAuthentication("student1", "ROLE_STUDENT");
        examUser.setIsFinished(true);
        when(examUserService.findByExamAndUser(1L, "student1")).thenReturn(examUser);
        examController.saveUserExamAnswer(Collections.emptyList(), 1L, false, 0);
    }

    /**
     * @Mã Unit Test: TC_EC_19
     * @Trường hợp test: Lưu câu trả lời thành công, đánh dấu exam là hoàn thành (isFinish = true).
     * @Input: answerSheets, examId, isFinish = true, remainingTime.
     * @Output kỳ vọng: examUser được cập nhật, isFinished = true, timeFinish được đặt. Nhánh `if (isFinish == true)` được thực thi.
     */
    @Test
    public void testSaveUserExamAnswer_Success_IsFinishTrue() throws JsonProcessingException {
        setupMockAuthentication("student1", "ROLE_STUDENT");
        String answersJson = realMapper.writeValueAsString(Collections.singletonList(answerSheetQ1Correct));
        when(examUserService.findByExamAndUser(1L, "student1")).thenReturn(examUser); // examUser.isFinished() ban đầu là false
        when(mockedMapper.writeValueAsString(Collections.singletonList(answerSheetQ1Correct))).thenReturn(answersJson);

        examController.saveUserExamAnswer(Collections.singletonList(answerSheetQ1Correct), 1L, true, 100);

        assertTrue(examUser.getIsFinished());
        assertNotNull(examUser.getTimeFinish());
        assertEquals(answersJson, examUser.getAnswerSheet());
        verify(examUserService).update(examUser);
    }

    /**
     * @Mã Unit Test: TC_EC_20
     * @Trường hợp test: Lưu câu trả lời thành công, exam chưa hoàn thành (isFinish = false).
     * @Input: answerSheets, examId, isFinish = false, remainingTime.
     * @Output kỳ vọng: examUser được cập nhật, isFinished = false, timeFinish là null. Nhánh `if (isFinish == true)` không được thực thi.
     */
    @Test
    public void testSaveUserExamAnswer_Success_IsFinishFalse() throws JsonProcessingException {
        setupMockAuthentication("student1", "ROLE_STUDENT");
        String answersJson = realMapper.writeValueAsString(Collections.emptyList());
        when(examUserService.findByExamAndUser(1L, "student1")).thenReturn(examUser);
        when(mockedMapper.writeValueAsString(Collections.emptyList())).thenReturn(answersJson);

        examController.saveUserExamAnswer(Collections.emptyList(), 1L, false, 2000);

        assertFalse(examUser.getIsFinished());
        assertNull(examUser.getTimeFinish());
        assertEquals(answersJson, examUser.getAnswerSheet());
        verify(examUserService).update(examUser);
    }

    }