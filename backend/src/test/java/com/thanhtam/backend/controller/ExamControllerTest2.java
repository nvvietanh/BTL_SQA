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
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.IOException;
import java.util.*;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@RunWith(SpringRunner.class)
public class ExamControllerTest2 {

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
    private ObjectMapper mockedMapper; // Sẽ được inject vào examController

    private ObjectMapper realMapper = new ObjectMapper(); // Dùng để tạo JSON thực cho test data

    @InjectMocks
    private ExamController examController;

    // Khai báo các biến test data dùng chung
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
        examController.mapper = mockedMapper; // Gán mapper đã mock cho controller

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
        examUserNotStartedCanStart.setId(7L); examUserNotStartedCanStart.setExam(examFuture);
        examUserNotStartedCanStart.setUser(anotherUser); examUserNotStartedCanStart.setIsStarted(false); examUserNotStartedCanStart.setIsFinished(false);
        examUserNotStartedCanStart.setAnswerSheet("[]");

        examUserNotStartedMissed = new ExamUser();
        examUserNotStartedMissed.setId(8L); examUserNotStartedMissed.setExam(examPast);
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
        long userId = 1L; // Default ID
        if ("admin".equals(username)) userId = 1L;
        else if ("lecturer".equals(username)) userId = 2L;
        else if ("student1".equals(username)) userId = 3L;
        else if ("student2".equals(username)) userId = 4L;
        // Add more users if needed for specific tests

        UserDetailsImpl userDetails = new UserDetailsImpl(
                userId, username, username + "@example.com", "password",
                Collections.singletonList(new SimpleGrantedAuthority(roleName))
        );
        Authentication authentication = new UsernamePasswordAuthenticationToken(
                userDetails, null, userDetails.getAuthorities()
        );
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }

    //region TC_EC_GETRESULTEXAMALL - Test cases for getResultExamAll
    /**
     * @Mã Unit Test: TC_EC_GETRESULTEXAMALL_01
     * @Trường hợp test: Exam không tìm thấy.
     * @Input: examId không tồn tại.
     * @Output kỳ vọng: ResponseEntity NOT_FOUND. Nhánh `if (!exam.isPresent())` được kích hoạt.
     */
    @Test
    public void testGetResultExamAll_ExamNotFound_IfBranch() throws IOException {
        when(examService.getExamById(99L)).thenReturn(Optional.empty());
        ResponseEntity<?> response = examController.getResultExamAll(99L);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertEquals("Không tìm thấy exam", response.getBody());
    }

    /**
     * @Mã Unit Test: TC_EC_GETRESULTEXAMALL_02
     * @Trường hợp test: Không có ExamUser nào cho exam này.
     * @Input: examId hợp lệ, nhưng examUserService.findAllByExam_Id trả về danh sách rỗng.
     * @Output kỳ vọng: ResponseEntity OK với danh sách ExamResult rỗng. Vòng lặp `for (ExamUser examUser : examUserList)` không chạy.
     */
    @Test
    public void testGetResultExamAll_NoExamUsers_ForLoopEmpty() throws IOException {
        when(examService.getExamById(1L)).thenReturn(Optional.of(exam));
        when(examUserService.findAllByExam_Id(1L)).thenReturn(Collections.emptyList());
        when(mockedMapper.readValue(exam.getQuestionData(), new TypeReference<List<ExamQuestionPoint>>() {}))
                .thenReturn(Collections.emptyList());

        ResponseEntity<?> response = examController.getResultExamAll(1L);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        List<ExamResult> results = (List<ExamResult>) response.getBody();
        assertTrue(results.isEmpty());
    }

    /**
     * @Mã Unit Test: TC_EC_GETRESULTEXAMALL_03
     * @Trường hợp test: User không có câu trả lời (userChoices rỗng) và trạng thái exam là "chưa bắt đầu, còn hạn".
     * @Input: ExamUser với answerSheet rỗng, isStarted=false, exam.finishExam > now, exam.beginExam > now.
     * @Output kỳ vọng: TotalPoint là null, ExamStatus là 0. Nhánh `if (userChoices.isEmpty())` và nhánh `else if (examUser.getIsStarted().equals(false) && exam.get().getFinishExam().compareTo(now) == 1)` được kích hoạt.
     */
//    @Test
//    public void testGetResultExamAll_UserHasNoAnswers_IfUserChoicesEmpty_And_ExamStatusNotYetStarted_Branch() throws IOException {
//        Exam futureExamConfig = new Exam();
//        futureExamConfig.setId(1L);
//        futureExamConfig.setQuestionData("[]");
//        futureExamConfig.setBeginExam(new Date(currentDate.getTime() + 3600000));
//        futureExamConfig.setFinishExam(new Date(currentDate.getTime() + 7200000));
//
//        examUser_noAnswers.setExam(futureExamConfig);
//        examUser_noAnswers.setIsStarted(false);
//        examUser_noAnswers.setAnswerSheet("[]");
//
//        when(examService.getExamById(1L)).thenReturn(Optional.of(futureExamConfig));
//        when(examUserService.findAllByExam_Id(1L)).thenReturn(Collections.singletonList(examUser_noAnswers));
//        when(mockedMapper.readValue(futureExamConfig.getQuestionData(), new TypeReference<List<ExamQuestionPoint>>() {})).thenReturn(Collections.emptyList());
//        when(mockedMapper.readValue(eq("[]"), any(TypeReference.class))).thenReturn(Collections.emptyList());
//
//        ResponseEntity<?> response = examController.getResultExamAll(1L);
//        List<ExamResult> results = (List<ExamResult>) response.getBody();
//        ExamResult userResult = results.get(0);
//
//        assertNull(userResult.getTotalPoint());
//        assertEquals(0, userResult.getExamStatus());
//    }

    /**
     * @Mã Unit Test: TC_EC_GETRESULTEXAMALL_04
     * @Trường hợp test: User có câu trả lời, điểm cần được tính và cập nhật (totalPoint = -1), trạng thái exam "đang làm bài".
     * @Input: ExamUser với answerSheet, totalPoint = -1, isStarted=true, isFinished=false, exam.finishExam > now.
     * @Output kỳ vọng: TotalPoint được tính, examUser.totalPoint được cập nhật, ExamStatus là 1.
     */
    @Test
    public void testGetResultExamAll_UserHasAnswers_AndUpdateScore_ExamStatusInProgress() throws IOException {
        String uInProgressSheetJson = realMapper.writeValueAsString(Collections.singletonList(answerSheetQ1Correct));
        examUserInProgress.setAnswerSheet(uInProgressSheetJson);
        examUserInProgress.setTotalPoint(-1.0);
        exam.setFinishExam(new Date(currentDate.getTime() + 3600000)); // Vẫn còn thời gian

        List<ExamQuestionPoint> eqpList = Collections.singletonList(examQuestionPoint1);
        List<AnswerSheet> userChoices = Collections.singletonList(answerSheetQ1Correct);
        ChoiceList choiceListCorrect = new ChoiceList(question1, Collections.emptyList(), 10, true);
        List<ChoiceList> evaluatedChoices = Collections.singletonList(choiceListCorrect);

        when(examService.getExamById(1L)).thenReturn(Optional.of(exam));
        when(examUserService.findAllByExam_Id(1L)).thenReturn(Collections.singletonList(examUserInProgress));
        when(mockedMapper.readValue(exam.getQuestionData(), new TypeReference<List<ExamQuestionPoint>>() {})).thenReturn(eqpList);
        when(mockedMapper.readValue(uInProgressSheetJson, new TypeReference<List<AnswerSheet>>() {})).thenReturn(userChoices);
        when(examService.getChoiceList(userChoices, eqpList)).thenReturn(evaluatedChoices);

        ResponseEntity<?> response = examController.getResultExamAll(1L);
        List<ExamResult> results = (List<ExamResult>) response.getBody();
        ExamResult userResult = results.get(0);

        assertEquals(10.0, userResult.getTotalPoint(), 0.001);
        assertEquals(10.0, examUserInProgress.getTotalPoint(), 0.001);
        assertEquals(1, userResult.getExamStatus());
        verify(examUserService).update(examUserInProgress);
    }

    /**
     * @Mã Unit Test: TC_EC_GETRESULTEXAMALL_05
     * @Trường hợp test: User có câu trả lời, điểm đã được tính (totalPoint != -1), trạng thái exam "đã hoàn thành".
     * @Input: ExamUser với answerSheet, totalPoint != -1, isFinished=true.
     * @Output kỳ vọng: TotalPoint được tính lại, ExamStatus là -1. `examUserService.update` không được gọi.
     */
    @Test
    public void testGetResultExamAll_UserHasAnswers_ScoreSet_NoUpdate_ExamStatusCompleted() throws IOException {
        String uCompletedSheetJson = examUserCompletedScoreSet.getAnswerSheet();
        examUserCompletedScoreSet.setTotalPoint(20.0);
        examUserCompletedScoreSet.setIsFinished(true);

        List<ExamQuestionPoint> eqpList = Arrays.asList(examQuestionPoint1, examQuestionPoint2);
        List<AnswerSheet> userChoices = realMapper.readValue(uCompletedSheetJson, new TypeReference<List<AnswerSheet>>() {});
        ChoiceList cl1 = new ChoiceList(question1, null, 10, true);
        List<ChoiceList> evaluatedChoices = Collections.singletonList(cl1); // Giả sử chỉ tính lại 1 câu

        when(examService.getExamById(1L)).thenReturn(Optional.of(exam));
        when(examUserService.findAllByExam_Id(1L)).thenReturn(Collections.singletonList(examUserCompletedScoreSet));
        when(mockedMapper.readValue(exam.getQuestionData(), new TypeReference<List<ExamQuestionPoint>>() {})).thenReturn(eqpList);
        when(mockedMapper.readValue(uCompletedSheetJson, new TypeReference<List<AnswerSheet>>() {})).thenReturn(userChoices);
        when(examService.getChoiceList(userChoices, eqpList)).thenReturn(evaluatedChoices);

        ResponseEntity<?> response = examController.getResultExamAll(1L);
        List<ExamResult> results = (List<ExamResult>) response.getBody();
        ExamResult userResult = results.get(0);

        assertEquals(10.0, userResult.getTotalPoint(), 0.001);
        assertEquals(20.0, examUserCompletedScoreSet.getTotalPoint(), 0.001);
        assertEquals(-1, userResult.getExamStatus());
        verify(examUserService, never()).update(examUserCompletedScoreSet);
    }


    /**
     * @Mã Unit Test: TC_EC_GETRESULTEXAMALL_06
     * @Trường hợp test: User chưa làm bài, exam đã kết thúc.
     * @Input: ExamUser isStarted=false, exam.finishExam < now.
     * @Output kỳ vọng: ExamStatus là -2.
     */
    @Test
    public void testGetResultExamAll_UserMissedExam_ExamStatusMissed() throws IOException {
        exam.setFinishExam(new Date(currentDate.getTime() - 3600000));
        examUserNotStartedMissed.setExam(exam);
        examUserNotStartedMissed.setIsStarted(false);

        when(examService.getExamById(1L)).thenReturn(Optional.of(exam));
        when(examUserService.findAllByExam_Id(1L)).thenReturn(Collections.singletonList(examUserNotStartedMissed));
        when(mockedMapper.readValue(exam.getQuestionData(), new TypeReference<List<ExamQuestionPoint>>() {})).thenReturn(Collections.emptyList());
        when(mockedMapper.readValue(examUserNotStartedMissed.getAnswerSheet(), new TypeReference<List<AnswerSheet>>() {})).thenReturn(Collections.emptyList());

        ResponseEntity<?> response = examController.getResultExamAll(1L);
        List<ExamResult> results = (List<ExamResult>) response.getBody();
        assertEquals(-2, results.get(0).getExamStatus());
    }

    /**
     * @Mã Unit Test: TC_EC_GETRESULTEXAMALL_07
     * @Trường hợp test: Vòng lặp `for (ChoiceList choice : choiceLists)` không chạy (choiceLists rỗng).
     * @Input: ExamUser có answerSheet, nhưng `examService.getChoiceList` trả về danh sách rỗng.
     * @Output kỳ vọng: TotalPoint là 0.0.
     */
    @Test
    public void testGetResultExamAll_InnerForLoopChoiceListEmpty() throws IOException {
        String uSheetJson = realMapper.writeValueAsString(Collections.singletonList(answerSheetQ1Correct));
        examUserCompletedScoreNotSet.setAnswerSheet(uSheetJson);
        examUserCompletedScoreNotSet.setTotalPoint(-1.0);

        List<ExamQuestionPoint> eqpList = Collections.singletonList(examQuestionPoint1);
        List<AnswerSheet> userChoices = Collections.singletonList(answerSheetQ1Correct);

        when(examService.getExamById(1L)).thenReturn(Optional.of(exam));
        when(examUserService.findAllByExam_Id(1L)).thenReturn(Collections.singletonList(examUserCompletedScoreNotSet));
        when(mockedMapper.readValue(exam.getQuestionData(), new TypeReference<List<ExamQuestionPoint>>() {})).thenReturn(eqpList);
        when(mockedMapper.readValue(uSheetJson, new TypeReference<List<AnswerSheet>>() {})).thenReturn(userChoices);
        when(examService.getChoiceList(userChoices, eqpList)).thenReturn(Collections.emptyList()); // choiceLists rỗng

        ResponseEntity<?> response = examController.getResultExamAll(1L);
        List<ExamResult> results = (List<ExamResult>) response.getBody();
        ExamResult userResult = results.get(0);
        assertEquals(0.0, userResult.getTotalPoint(), 0.001);
        verify(examUserService).update(examUserCompletedScoreNotSet);
    }


    /**
     * @Mã Unit Test: TC_EC_GETRESULTEXAMALL_08
     * @Trường hợp test: IOException khi gọi convertQuestionJsonToObject.
     * @Input: examId hợp lệ.
     * @Output kỳ vọng: Ném IOException.
     */
    @Test(expected = IOException.class)
    public void testGetResultExamAll_IOExceptionOnConvertQuestion() throws IOException {
        when(examService.getExamById(1L)).thenReturn(Optional.of(exam));
        when(mockedMapper.readValue(exam.getQuestionData(), new TypeReference<List<ExamQuestionPoint>>() {}))
                .thenThrow(new IOException("Simulated error parsing question data"));
        examController.getResultExamAll(1L);
    }

    /**
     * @Mã Unit Test: TC_EC_GETRESULTEXAMALL_09
     * @Trường hợp test: IOException khi gọi convertAnswerJsonToObject.
     * @Input: examId hợp lệ, examUser có answerSheet không hợp lệ.
     * @Output kỳ vọng: Ném IOException.
     */
    @Test(expected = IOException.class)
    public void testGetResultExamAll_IOExceptionOnConvertAnswer() throws IOException {
        examUserCompletedScoreNotSet.setAnswerSheet("this is invalid json");
        when(examService.getExamById(1L)).thenReturn(Optional.of(exam));
        when(examUserService.findAllByExam_Id(1L)).thenReturn(Collections.singletonList(examUserCompletedScoreNotSet));
        when(mockedMapper.readValue(exam.getQuestionData(), new TypeReference<List<ExamQuestionPoint>>() {}))
                .thenReturn(Collections.singletonList(examQuestionPoint1));
        when(mockedMapper.readValue(eq("this is invalid json"), any(TypeReference.class)))
                .thenThrow(new IOException("Simulated error parsing answer sheet"));
        examController.getResultExamAll(1L);
    }

    //endregion

    //region TC_EC_GETRESULTEXAMQUESTIONSREPORT - Test cases for getResultExamQuestionsReport
    /**
     * @Mã Unit Test: TC_EC_GETRESULTEXAMQSREPORT_01
     * @Trường hợp test: Exam không tìm thấy.
     * @Input: examId không tồn tại.
     * @Output kỳ vọng: ResponseEntity NOT_FOUND. Nhánh `if (!exam.isPresent())` được kích hoạt.
     */
    @Test
    public void testGetResultExamQuestionsReport_ExamNotFound_IfBranch() throws IOException {
        when(examService.getExamById(99L)).thenReturn(Optional.empty());
        ResponseEntity<?> response = examController.getResultExamQuestionsReport(99L);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    /**
     * @Mã Unit Test: TC_EC_GETRESULTEXAMQSREPORT_02
     * @Trường hợp test: Không có user nào hoàn thành exam.
     * @Input: examId hợp lệ, examUserService.findExamUsersByIsFinishedIsTrueAndExam_Id trả về danh sách rỗng.
     * @Output kỳ vọng: ResponseEntity OK với thông báo. Nhánh `if (finishedExamUser.size() == 0)` được kích hoạt.
     */
    @Test
    public void testGetResultExamQuestionsReport_NoFinishedUsers_IfBranch() throws IOException {
        when(examService.getExamById(1L)).thenReturn(Optional.of(exam));
        when(examUserService.findExamUsersByIsFinishedIsTrueAndExam_Id(1L)).thenReturn(Collections.emptyList());
        ResponseEntity<?> response = examController.getResultExamQuestionsReport(1L);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Chưa có người dùng thực hiện bài kiểm tra", response.getBody());
    }

    /**
     * @Mã Unit Test: TC_EC_GETRESULTEXAMQSREPORT_03
     * @Trường hợp test: User đầu tiên không có choice nào (firstChoiceList rỗng).
     * @Input: finishedExamUser có 1 user, user đó không có choice được đánh giá.
     * @Output kỳ vọng: ResponseEntity OK với danh sách QuestionExamReport rỗng. Nhánh `if (questionExamReports.size() == 0)` được kích hoạt. Vòng lặp `for (ChoiceList choice : firstChoiceList)` không chạy.
     */
    @Test
    public void testGetResultExamQuestionsReport_FirstUserEmptyChoiceList_ReportEmpty_IfBranch() throws IOException {
        examUserCompletedScoreSet.setAnswerSheet("[]"); // User đầu tiên không có câu trả lời
        List<ExamUser> finishedUsers = Collections.singletonList(examUserCompletedScoreSet);
        List<ExamQuestionPoint> eqpList = Collections.singletonList(examQuestionPoint1);

        when(examService.getExamById(1L)).thenReturn(Optional.of(exam));
        when(examUserService.findExamUsersByIsFinishedIsTrueAndExam_Id(1L)).thenReturn(finishedUsers);
        when(mockedMapper.readValue(exam.getQuestionData(), new TypeReference<List<ExamQuestionPoint>>() {})).thenReturn(eqpList);
        when(mockedMapper.readValue(eq("[]"), any(TypeReference.class))).thenReturn(Collections.emptyList());
        when(examService.getChoiceList(Collections.emptyList(), eqpList)).thenReturn(Collections.emptyList());

        ResponseEntity<?> response = examController.getResultExamQuestionsReport(1L);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        List<QuestionExamReport> reports = (List<QuestionExamReport>) response.getBody();
        assertTrue(reports.isEmpty());
    }


    /**
     * @Mã Unit Test: TC_EC_GETRESULTEXAMQSREPORT_04
     * @Trường hợp test: Một user, một câu trả lời đúng.
     * @Input: finishedExamUser có 1 user, firstChoiceList có 1 choice đúng.
     * @Output kỳ vọng: Report có 1 question, correctTotal = 1. Vòng lặp for (ChoiceList...) đầu tiên chạy 1 lần (nhánh if đúng). Vòng lặp for (int i=1...) không chạy.
     */
    @Test
    public void testGetResultExamQuestionsReport_OneUser_OneCorrectChoice_ForLoopAndInnerIf() throws IOException {
        String u1SheetJson = realMapper.writeValueAsString(Collections.singletonList(answerSheetQ1Correct));
        examUserCompletedScoreSet.setAnswerSheet(u1SheetJson);
        List<ExamUser> finishedUsers = Collections.singletonList(examUserCompletedScoreSet);
        List<ExamQuestionPoint> eqpList = Collections.singletonList(examQuestionPoint1);
        List<AnswerSheet> u1Answers = Collections.singletonList(answerSheetQ1Correct);
        ChoiceList clCorrect = new ChoiceList(question1, null, 10, true);
        List<ChoiceList> firstUserChoices = Collections.singletonList(clCorrect);

        when(examService.getExamById(1L)).thenReturn(Optional.of(exam));
        when(examUserService.findExamUsersByIsFinishedIsTrueAndExam_Id(1L)).thenReturn(finishedUsers);
        when(mockedMapper.readValue(exam.getQuestionData(), new TypeReference<List<ExamQuestionPoint>>() {})).thenReturn(eqpList);
        when(mockedMapper.readValue(u1SheetJson, new TypeReference<List<AnswerSheet>>() {})).thenReturn(u1Answers);
        when(examService.getChoiceList(u1Answers, eqpList)).thenReturn(firstUserChoices);

        ResponseEntity<?> response = examController.getResultExamQuestionsReport(1L);
        List<QuestionExamReport> reports = (List<QuestionExamReport>) response.getBody();
        assertEquals(1, reports.size());
        assertEquals(1, reports.get(0).getCorrectTotal());
    }

    /**
     * @Mã Unit Test: TC_EC_GETRESULTEXAMQSREPORT_05
     * @Trường hợp test: Một user, một câu trả lời sai.
     * @Input: finishedExamUser có 1 user, firstChoiceList có 1 choice sai.
     * @Output kỳ vọng: Report có 1 question, correctTotal = 0. Vòng lặp for (ChoiceList...) đầu tiên chạy 1 lần (nhánh else). Vòng lặp for (int i=1...) không chạy.
     */
    @Test
    public void testGetResultExamQuestionsReport_OneUser_OneIncorrectChoice_InnerElse() throws IOException {
        String u1SheetJson = realMapper.writeValueAsString(Collections.singletonList(answerSheetQ1Incorrect));
        examUserCompletedScoreSet.setAnswerSheet(u1SheetJson);
        List<ExamUser> finishedUsers = Collections.singletonList(examUserCompletedScoreSet);
        List<ExamQuestionPoint> eqpList = Collections.singletonList(examQuestionPoint1);
        List<AnswerSheet> u1Answers = Collections.singletonList(answerSheetQ1Incorrect);
        ChoiceList clIncorrect = new ChoiceList(question1, null, 0, false);
        List<ChoiceList> firstUserChoices = Collections.singletonList(clIncorrect);

        when(examService.getExamById(1L)).thenReturn(Optional.of(exam));
        when(examUserService.findExamUsersByIsFinishedIsTrueAndExam_Id(1L)).thenReturn(finishedUsers);
        when(mockedMapper.readValue(exam.getQuestionData(), new TypeReference<List<ExamQuestionPoint>>() {})).thenReturn(eqpList);
        when(mockedMapper.readValue(u1SheetJson, new TypeReference<List<AnswerSheet>>() {})).thenReturn(u1Answers);
        when(examService.getChoiceList(u1Answers, eqpList)).thenReturn(firstUserChoices);

        ResponseEntity<?> response = examController.getResultExamQuestionsReport(1L);
        List<QuestionExamReport> reports = (List<QuestionExamReport>) response.getBody();
        assertEquals(1, reports.size());
        assertEquals(0, reports.get(0).getCorrectTotal());
    }

    /**
     * @Mã Unit Test: TC_EC_GETRESULTEXAMQSREPORT_06
     * @Trường hợp test: Nhiều user, user đầu trả lời đúng, user sau cũng được tính là đúng (do lỗi logic).
     * @Input: finishedExamUser có 2 user. User đầu trả lời Q1 đúng.
     * @Output kỳ vọng: Report cho Q1 có correctTotal = 2. Cả 2 vòng for đều chạy.
     */
    @Test
    public void testGetResultExamQuestionsReport_MultipleUsers_LogicBug_AllCorrect() throws IOException {
        String u1SheetJson = realMapper.writeValueAsString(Collections.singletonList(answerSheetQ1Correct));
        examUserCompletedScoreSet.setAnswerSheet(u1SheetJson); // User1 đúng Q1
        ExamUser anotherFinishedUser = new ExamUser();
        anotherFinishedUser.setId(10L); anotherFinishedUser.setExam(exam); anotherFinishedUser.setUser(anotherUser);
        anotherFinishedUser.setIsFinished(true);
        anotherFinishedUser.setAnswerSheet(realMapper.writeValueAsString(Collections.singletonList(answerSheetQ1Incorrect))); // User2 sai Q1

        List<ExamUser> finishedUsers = Arrays.asList(examUserCompletedScoreSet, anotherFinishedUser);
        List<ExamQuestionPoint> eqpList = Collections.singletonList(examQuestionPoint1);
        List<AnswerSheet> u1Answers = Collections.singletonList(answerSheetQ1Correct);
        ChoiceList clCorrect = new ChoiceList(question1, null, 10, true);
        List<ChoiceList> firstUserChoices = Collections.singletonList(clCorrect); // firstChoiceList (từ User1)

        when(examService.getExamById(1L)).thenReturn(Optional.of(exam));
        when(examUserService.findExamUsersByIsFinishedIsTrueAndExam_Id(1L)).thenReturn(finishedUsers);
        when(mockedMapper.readValue(exam.getQuestionData(), new TypeReference<List<ExamQuestionPoint>>() {})).thenReturn(eqpList);
        when(mockedMapper.readValue(u1SheetJson, new TypeReference<List<AnswerSheet>>() {})).thenReturn(u1Answers); // Cả 2 lần đều dùng u1SheetJson
        when(examService.getChoiceList(u1Answers, eqpList)).thenReturn(firstUserChoices);

        ResponseEntity<?> response = examController.getResultExamQuestionsReport(1L);
        List<QuestionExamReport> reports = (List<QuestionExamReport>) response.getBody();
        assertEquals(1, reports.size());
        assertEquals(2, reports.get(0).getCorrectTotal()); // Lỗi: User2 đáng lẽ sai nhưng được tính đúng theo User1
    }


    /**
     * @Mã Unit Test: TC_EC_GETRESULTEXAMQSREPORT_07
     * @Trường hợp test: `questionExamReportsList.get(0)` ném `IndexOutOfBoundsException`
     * @Input: `firstChoiceList` chứa questionId không có trong `questionExamReports` ban đầu.
     * @Output kỳ vọng: Ném `IndexOutOfBoundsException`.
     */
    @Test(expected = IndexOutOfBoundsException.class)
    public void testGetResultExamQuestionsReport_StreamFilterReturnsEmpty_IndexOutOfBounds() throws IOException {
        String u1SheetJson = realMapper.writeValueAsString(Collections.singletonList(answerSheetQ1Correct));
        examUserCompletedScoreSet.setAnswerSheet(u1SheetJson);
        ExamUser user2 = new ExamUser(); user2.setId(10L);user2.setIsFinished(true);user2.setExam(exam);user2.setUser(anotherUser);
        // User2's answer sheet doesn't matter much here due to the bug, but set it for completeness
        user2.setAnswerSheet(realMapper.writeValueAsString(Collections.singletonList(answerSheetQ2Correct)));


        List<ExamUser> finishedUsers = Arrays.asList(examUserCompletedScoreSet, user2);
        List<ExamQuestionPoint> eqpList = Arrays.asList(examQuestionPoint1, examQuestionPoint2); // Exam has Q1 and Q2
        List<AnswerSheet> u1Answers = Collections.singletonList(answerSheetQ1Correct); // User1 answered Q1

        // firstChoiceList (from user1) only contains Q1
        ChoiceList clQ1 = new ChoiceList(question1, null, 10, true);
        List<ChoiceList> firstUserChoices = Collections.singletonList(clQ1);

        // Now, to cause the error, when iterating for user2, the inner loop still iterates `firstUserChoices`.
        // If `firstUserChoices` was hypothetically modified to include a choice for Q2 *after*
        // `questionExamReports` was built (based only on Q1 from user1), then for that Q2 choice,
        // the filter `item.getQuestion().getId() == choice.getQuestion().getId()` would yield an empty list.
        // This setup is a bit tricky due to the reuse of `firstChoiceList`.

        // Let's refine:
        // User1 answers Q1. questionExamReports will have only Q1.
        // The second loop iterates user2. It *should* use user2's answers, but uses user1's.
        // The inner loop iterates `firstChoiceList` (which is from user1, only Q1).
        // So, `item.getQuestion().getId() == choice.getQuestion().getId()` will always find Q1.
        // The IndexOutOfBounds can only happen if `questionExamReports` itself is empty AND `firstChoiceList` is not,
        // which is covered by `testGetResultExamQuestionsReport_FirstUserEmptyChoiceList_ReportEmpty_IfBranch`.

        // To force IndexOutOfBoundsException at `questionExamReportsList.get(0)`:
        // `questionExamReports` must be non-empty.
        // `firstChoiceList` (iterated in the second outer loop) must contain a `ChoiceList choice`
        // such that `choice.getQuestion().getId()` does not exist as a `question.getId()` in any
        // `QuestionExamReport` within `questionExamReports`.

        // Setup:
        // User1 answers Q1 (correct). `questionExamReports` will contain one entry for Q1.
        // For the second loop (i=1), `firstChoiceList` (which is still derived from User1's answers for Q1)
        // is iterated. Let's say we *add* a fake choice to this list for Q99 (which is not in `questionExamReports`).
        Question q99 = new Question(); q99.setId(99L);
        ChoiceList fakeChoiceForQ99 = new ChoiceList(q99, null, 0, false);
        List<ChoiceList> firstUserChoicesExtended = new ArrayList<>(firstUserChoices);
        firstUserChoicesExtended.add(fakeChoiceForQ99);


        when(examService.getExamById(1L)).thenReturn(Optional.of(exam));
        when(examUserService.findExamUsersByIsFinishedIsTrueAndExam_Id(1L)).thenReturn(finishedUsers);
        when(mockedMapper.readValue(exam.getQuestionData(), new TypeReference<List<ExamQuestionPoint>>() {})).thenReturn(eqpList);
        when(mockedMapper.readValue(u1SheetJson, new TypeReference<List<AnswerSheet>>() {})).thenReturn(u1Answers);
        // This mock will be used for the first loop (building questionExamReports)
        // AND for the inner loop of the second outer loop (due to the bug)
        when(examService.getChoiceList(u1Answers, eqpList)).thenReturn(firstUserChoicesExtended);


        examController.getResultExamQuestionsReport(1L); // Should throw due to Q99 not being in reports from user1
    }


    //endregion

    //region Test Cases for convertQuestionJsonToObject (Direct test)
    /**
     * @Mã Unit Test: TC_EC_CONVERTQUESTIONJSON_01
     * @Trường hợp test: Chuyển đổi JSON hợp lệ từ exam.questionData.
     * @Input: Optional<Exam> chứa exam với questionData hợp lệ.
     * @Output kỳ vọng: List<ExamQuestionPoint> tương ứng.
     */
    @Test
    public void testConvertQuestionJsonToObject_ValidJson() throws IOException {
        String validJson = realMapper.writeValueAsString(Collections.singletonList(examQuestionPoint1));
        exam.setQuestionData(validJson);
        List<ExamQuestionPoint> expected = Collections.singletonList(examQuestionPoint1);
        when(mockedMapper.readValue(validJson, new TypeReference<List<ExamQuestionPoint>>() {})).thenReturn(expected);

        List<ExamQuestionPoint> actual = examController.convertQuestionJsonToObject(Optional.of(exam));
        assertEquals(expected.size(), actual.size());
        assertEquals(expected.get(0).getQuestionId(), actual.get(0).getQuestionId());
    }

    /**
     * @Mã Unit Test: TC_EC_CONVERTQUESTIONJSON_02
     * @Trường hợp test: exam.questionData là JSON không hợp lệ.
     * @Input: Optional<Exam> chứa exam với questionData không hợp lệ.
     * @Output kỳ vọng: Ném IOException.
     */
    @Test(expected = IOException.class)
    public void testConvertQuestionJsonToObject_InvalidJson() throws IOException {
        String invalidJson = "this is not json";
        exam.setQuestionData(invalidJson);
        when(mockedMapper.readValue(invalidJson, new TypeReference<List<ExamQuestionPoint>>() {}))
                .thenThrow(new IOException("Simulated Parse error"));
        examController.convertQuestionJsonToObject(Optional.of(exam));
    }

    /**
     * @Mã Unit Test: TC_EC_CONVERTQUESTIONJSON_03
     * @Trường hợp test: Optional<Exam> rỗng.
     * @Input: Optional.empty().
     * @Output kỳ vọng: Ném NoSuchElementException (do .get() trong hàm).
     */
    @Test(expected = NoSuchElementException.class)
    public void testConvertQuestionJsonToObject_EmptyOptionalExam() throws IOException {
        examController.convertQuestionJsonToObject(Optional.empty());
    }
    //endregion

    //region Test Cases for convertAnswerJsonToObject (Direct test)
    /**
     * @Mã Unit Test: TC_EC_CONVERTANSWERJSON_01
     * @Trường hợp test: examUser.answerSheet là null.
     * @Input: ExamUser với answerSheet là null.
     * @Output kỳ vọng: Trả về danh sách rỗng. Nhánh `if (Strings.isNullOrEmpty(...))` đúng.
     */
    @Test
    public void testConvertAnswerJsonToObject_NullSheet_IfBranch() throws IOException {
        examUser.setAnswerSheet(null);
        List<AnswerSheet> result = examController.convertAnswerJsonToObject(examUser);
        assertTrue(result.isEmpty());
    }

    /**
     * @Mã Unit Test: TC_EC_CONVERTANSWERJSON_02
     * @Trường hợp test: examUser.answerSheet là chuỗi rỗng.
     * @Input: ExamUser với answerSheet là "".
     * @Output kỳ vọng: Trả về danh sách rỗng. Nhánh `if (Strings.isNullOrEmpty(...))` đúng.
     */
    @Test
    public void testConvertAnswerJsonToObject_EmptySheet_IfBranch() throws IOException {
        examUser.setAnswerSheet("");
        List<AnswerSheet> result = examController.convertAnswerJsonToObject(examUser);
        assertTrue(result.isEmpty());
    }

    /**
     * @Mã Unit Test: TC_EC_CONVERTANSWERJSON_03
     * @Trường hợp test: examUser.answerSheet là JSON hợp lệ.
     * @Input: ExamUser với answerSheet hợp lệ.
     * @Output kỳ vọng: List<AnswerSheet> tương ứng. Nhánh `else` của `if (Strings.isNullOrEmpty(...))` được thực thi.
     */
    @Test
    public void testConvertAnswerJsonToObject_ValidJson_ElseBranch() throws IOException {
        String validJson = realMapper.writeValueAsString(Collections.singletonList(answerSheetQ1Correct));
        examUser.setAnswerSheet(validJson);
        List<AnswerSheet> expected = Collections.singletonList(answerSheetQ1Correct);
        when(mockedMapper.readValue(validJson, new TypeReference<List<AnswerSheet>>() {})).thenReturn(expected);

        List<AnswerSheet> actual = examController.convertAnswerJsonToObject(examUser);
        assertEquals(expected.size(), actual.size());
    }

    /**
     * @Mã Unit Test: TC_EC_CONVERTANSWERJSON_04
     * @Trường hợp test: examUser.answerSheet là JSON không hợp lệ.
     * @Input: ExamUser với answerSheet không hợp lệ.
     * @Output kỳ vọng: Ném IOException.
     */
    @Test(expected = IOException.class)
    public void testConvertAnswerJsonToObject_InvalidJson_Exception() throws IOException {
        String invalidJson = "not a json";
        examUser.setAnswerSheet(invalidJson);
        when(mockedMapper.readValue(invalidJson, new TypeReference<List<AnswerSheet>>() {}))
                .thenThrow(new IOException("Simulated Parse error"));
        examController.convertAnswerJsonToObject(examUser);
    }
    //endregion

    // Các hàm còn lại (getResultExam, getResultExamByUser, getQuestionTextByExamId, getExamCalendar, cancelExam)
    // sẽ được thêm test case vào đây, tương tự như các hàm đã làm.
    // Tôi sẽ tiếp tục với getResultExam trước.

    //region Test Cases for getResultExam
    /**
     * @Mã Unit Test: TC_EC_GETRESULTEXAM_01
     * @Trường hợp test: Exam không tìm thấy.
     * @Input: examId không tồn tại.
     * @Output kỳ vọng: ResponseEntity NOT_FOUND. Nhánh `if (!exam.isPresent())` đúng.
     */
    @Test
    public void testGetResultExam_ExamNotFound_IfBranch() throws IOException {
        setupMockAuthentication("student1", "ROLE_STUDENT");
        when(userService.getUserName()).thenReturn("student1");
        when(examService.getExamById(99L)).thenReturn(Optional.empty());
        ResponseEntity<?> response = examController.getResultExam(99L);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    /**
     * @Mã Unit Test: TC_EC_GETRESULTEXAM_02
     * @Trường hợp test: ExamUser không tìm thấy (null).
     * @Input: examId hợp lệ, username hợp lệ, nhưng examUserService.findByExamAndUser trả về null.
     * @Output kỳ vọng: Ném NullPointerException khi cố gắng gọi convertAnswerJsonToObject(null).
     */
    @Test(expected = NullPointerException.class)
    public void testGetResultExam_ExamUserNull_NPE() throws IOException {
        setupMockAuthentication("student1", "ROLE_STUDENT");
        when(userService.getUserName()).thenReturn("student1");
        when(examService.getExamById(1L)).thenReturn(Optional.of(exam));
        when(mockedMapper.readValue(exam.getQuestionData(), new TypeReference<List<ExamQuestionPoint>>() {}))
                .thenReturn(Collections.emptyList()); // Giả lập convertQuestionJsonToObject thành công
        when(examUserService.findByExamAndUser(1L, "student1")).thenReturn(null);
        examController.getResultExam(1L);
    }

    /**
     * @Mã Unit Test: TC_EC_GETRESULTEXAM_03
     * @Trường hợp test: User không có câu trả lời (userChoices rỗng), điểm đã được tính trước đó.
     * @Input: examUser.answerSheet rỗng, examUser.totalPoint != -1.
     * @Output kỳ vọng: TotalPoint là 0.0 (do choiceLists rỗng). `examUserService.update` không được gọi. Vòng `for (ChoiceList...)` không chạy.
     */
    @Test
    public void testGetResultExam_NoChoices_ForLoopEmpty_ScoreNotUpdated_IfBranch() throws IOException {
        setupMockAuthentication("student1", "ROLE_STUDENT");
        examUser.setAnswerSheet("[]");
        examUser.setTotalPoint(15.0); // Điểm đã có

        when(userService.getUserName()).thenReturn("student1");
        when(examService.getExamById(1L)).thenReturn(Optional.of(exam));
        when(mockedMapper.readValue(exam.getQuestionData(), new TypeReference<List<ExamQuestionPoint>>() {})).thenReturn(Collections.emptyList());
        when(examUserService.findByExamAndUser(1L, "student1")).thenReturn(examUser);
        when(mockedMapper.readValue(eq("[]"), any(TypeReference.class))).thenReturn(Collections.emptyList());
        when(examService.getChoiceList(anyList(), anyList())).thenReturn(Collections.emptyList());

        ResponseEntity<?> response = examController.getResultExam(1L);
        ExamResult result = (ExamResult) response.getBody();
        assertEquals(0.0, result.getTotalPoint(), 0.001);
        verify(examUserService, never()).update(examUser);
    }

    /**
     * @Mã Unit Test: TC_EC_GETRESULTEXAM_04
     * @Trường hợp test: User có câu trả lời, điểm cần cập nhật (totalPoint = -1).
     * @Input: examUser.answerSheet có dữ liệu, examUser.totalPoint = -1.
     * @Output kỳ vọng: TotalPoint được tính, examUser.totalPoint được cập nhật. Vòng `for (ChoiceList...)` chạy, nhánh `if (choice.getIsSelectedCorrected())` và `if (examUser.getTotalPoint() == -1)` được bao phủ.
     */
    @Test
    public void testGetResultExam_ChoicesPresent_TotalPointMinusOne_ScoreUpdated_IfBranch() throws IOException {
        setupMockAuthentication("student1", "ROLE_STUDENT");
        examUser.setTotalPoint(-1.0);
        String answerSheetJson = realMapper.writeValueAsString(Arrays.asList(answerSheetQ1Correct, answerSheetQ1Incorrect)); // 1 đúng, 1 sai
        examUser.setAnswerSheet(answerSheetJson);

        List<ExamQuestionPoint> eqpList = Arrays.asList(examQuestionPoint1, examQuestionPoint2); // Giả sử exam có 2 câu hỏi
        List<AnswerSheet> userChoices = Arrays.asList(answerSheetQ1Correct, answerSheetQ1Incorrect);
        ChoiceList choice1 = new ChoiceList(question1, Collections.emptyList(), 10, true); // Đúng
        ChoiceList choice2 = new ChoiceList(question2, Collections.emptyList(), 0, false); // Sai, điểm câu hỏi là 5
        List<ChoiceList> evaluatedChoices = Arrays.asList(choice1, choice2);

        when(userService.getUserName()).thenReturn("student1");
        when(examService.getExamById(1L)).thenReturn(Optional.of(exam));
        when(mockedMapper.readValue(exam.getQuestionData(), new TypeReference<List<ExamQuestionPoint>>() {})).thenReturn(eqpList);
        when(examUserService.findByExamAndUser(1L, "student1")).thenReturn(examUser);
        when(mockedMapper.readValue(answerSheetJson, new TypeReference<List<AnswerSheet>>() {})).thenReturn(userChoices);
        when(examService.getChoiceList(userChoices, eqpList)).thenReturn(evaluatedChoices);

        ResponseEntity<?> response = examController.getResultExam(1L);
        ExamResult result = (ExamResult) response.getBody();
        assertEquals(10.0, result.getTotalPoint(), 0.001); // Chỉ câu 1 đúng
        assertEquals(10.0, examUser.getTotalPoint(), 0.001);
        verify(examUserService).update(examUser);
    }
    //endregion

    //region Test Cases for getResultExamByUser
    /**
     * @Mã Unit Test: TC_EC_GETRESULTEXAMBYUSER_01
     * @Trường hợp test: User không tìm thấy trong DB khi gọi `userService.getUserByUsername`.
     * @Input: examId, username không tồn tại.
     * @Output kỳ vọng: Ném `NoSuchElementException`.
     */
    @Test(expected = NoSuchElementException.class)
    public void testGetResultExamByUser_UserNotFoundInDB_Exception() throws IOException {
        when(examService.getExamById(1L)).thenReturn(Optional.of(exam)); // Giả sử exam tìm thấy
        when(userService.getUserByUsername("unknownUser")).thenReturn(Optional.empty());
        examController.getResultExamByUser(1L, "unknownUser");
    }

    /**
     * @Mã Unit Test: TC_EC_GETRESULTEXAMBYUSER_02
     * @Trường hợp test: Thành công, tính toán remainingTime.
     * @Input: examId, username hợp lệ.
     * @Output kỳ vọng: ResponseEntity OK, ExamResult chứa thông tin chính xác bao gồm remainingTime.
     */
    @Test
    public void testGetResultExamByUser_Success_RemainingTimeCalculated() throws IOException {
        String usernameToTest = "testUser";
        User specificUser = new User(); specificUser.setId(10L); specificUser.setUsername(usernameToTest);
        ExamUser specificExamUser = new ExamUser();
        specificExamUser.setUser(specificUser); specificExamUser.setExam(exam);
        specificExamUser.setTotalPoint(-1.0); specificExamUser.setRemainingTime(1000); // Còn 1000 giây
        specificExamUser.setAnswerSheet(realMapper.writeValueAsString(Collections.singletonList(answerSheetQ1Correct)));
        specificExamUser.setTimeStart(new Date()); specificExamUser.setTimeFinish(new Date());

        List<ExamQuestionPoint> eqpList = Collections.singletonList(examQuestionPoint1);
        List<AnswerSheet> userChoices = Collections.singletonList(answerSheetQ1Correct);
        ChoiceList choiceCorrect = new ChoiceList(question1, Collections.emptyList(), 10, true);
        List<ChoiceList> evaluatedChoices = Collections.singletonList(choiceCorrect);

        when(examService.getExamById(1L)).thenReturn(Optional.of(exam)); // exam.durationExam = 60
        when(userService.getUserByUsername(usernameToTest)).thenReturn(Optional.of(specificUser));
        when(mockedMapper.readValue(exam.getQuestionData(), new TypeReference<List<ExamQuestionPoint>>() {})).thenReturn(eqpList);
        when(examUserService.findByExamAndUser(1L, usernameToTest)).thenReturn(specificExamUser);
        when(mockedMapper.readValue(specificExamUser.getAnswerSheet(), new TypeReference<List<AnswerSheet>>() {})).thenReturn(userChoices);
        when(examService.getChoiceList(userChoices, eqpList)).thenReturn(evaluatedChoices);

        ResponseEntity<?> response = examController.getResultExamByUser(1L, usernameToTest);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        ExamResult result = (ExamResult) response.getBody();
        assertEquals(10.0, result.getTotalPoint(), 0.001);
        assertEquals(3600 - 1000, result.getRemainingTime()); // 60*60 - 1000
    }
    //endregion

    //region Test Cases for getQuestionTextByExamId
    /**
     * @Mã Unit Test: TC_EC_GETQUESTIONTEXT_01
     * @Trường hợp test: Exam không tìm thấy và `.get()` được gọi trên Optional rỗng (do `convertQuestionJsonToObject`).
     * @Input: examId không tồn tại.
     * @Output kỳ vọng: Ném `NoSuchElementException`.
     */
    @Test(expected = NoSuchElementException.class)
    public void testGetQuestionTextByExamId_ExamNotFoundAndGetCalled_Exception() throws IOException {
        when(examService.getExamById(99L)).thenReturn(Optional.empty());
        examController.getQuestionTextByExamId(99L);
    }

    /**
     * @Mã Unit Test: TC_EC_GETQUESTIONTEXT_02
     * @Trường hợp test: Exam tìm thấy, nhưng `convertQuestionJsonToObject` trả về danh sách `ExamQuestionPoint` rỗng.
     * @Input: examId hợp lệ, exam.questionData rỗng.
     * @Output kỳ vọng: Trả về danh sách `ExamDetail` rỗng. Vòng lặp `forEach` không chạy.
     */
    @Test
    public void testGetQuestionTextByExamId_EmptyQuestionPoints_ForEachLoopEmpty() throws IOException {
        exam.setQuestionData("[]"); // Dữ liệu câu hỏi rỗng
        when(examService.getExamById(1L)).thenReturn(Optional.of(exam));
        when(mockedMapper.readValue(eq("[]"), any(TypeReference.class))).thenReturn(Collections.emptyList());
        List<ExamDetail> result = examController.getQuestionTextByExamId(1L);
        assertTrue(result.isEmpty());
    }

    /**
     * @Mã Unit Test: TC_EC_GETQUESTIONTEXT_03
     * @Trường hợp test: `questionService.getQuestionById` trả về `Optional.empty()`.
     * @Input: examId hợp lệ, một `examQuestionPoint.questionId` không tồn tại trong `questionService`.
     * @Output kỳ vọng: Ném `NoSuchElementException` khi gọi `.get()`.
     */
    @Test(expected = NoSuchElementException.class)
    public void testGetQuestionTextByExamId_QuestionServiceReturnsEmpty_Exception() throws IOException {
        List<ExamQuestionPoint> eqpList = Collections.singletonList(examQuestionPoint1); // Chứa questionId=1
        when(examService.getExamById(1L)).thenReturn(Optional.of(exam));
        when(mockedMapper.readValue(exam.getQuestionData(), new TypeReference<List<ExamQuestionPoint>>() {})).thenReturn(eqpList);
        when(questionService.getQuestionById(1L)).thenReturn(Optional.empty()); // questionId=1 không tìm thấy
        examController.getQuestionTextByExamId(1L);
    }

    /**
     * @Mã Unit Test: TC_EC_GETQUESTIONTEXT_04
     * @Trường hợp test: `question.getDifficultyLevel()` là null.
     * @Input: Question có `difficultyLevel` là null.
     * @Output kỳ vọng: Ném `NullPointerException` khi gọi `.toString()`.
     */
    @Test(expected = NullPointerException.class)
    public void testGetQuestionTextByExamId_DifficultyLevelNull_NPE() throws IOException {
        List<ExamQuestionPoint> eqpList = Collections.singletonList(examQuestionPoint1);
        question1.setDifficultyLevel(null);
        when(examService.getExamById(1L)).thenReturn(Optional.of(exam));
        when(mockedMapper.readValue(exam.getQuestionData(), new TypeReference<List<ExamQuestionPoint>>() {})).thenReturn(eqpList);
        when(questionService.getQuestionById(1L)).thenReturn(Optional.of(question1));
        examController.getQuestionTextByExamId(1L);
    }

    /**
     * @Mã Unit Test: TC_EC_GETQUESTIONTEXT_05
     * @Trường hợp test: `question.getQuestionType()` là null.
     * @Input: Question có `questionType` là null.
     * @Output kỳ vọng: Ném `NullPointerException` khi gọi `.getDescription()`.
     */
    @Test(expected = NullPointerException.class)
    public void testGetQuestionTextByExamId_QuestionTypeNull_NPE() throws IOException {
        List<ExamQuestionPoint> eqpList = Collections.singletonList(examQuestionPoint1);
        question1.setQuestionType(null);
        when(examService.getExamById(1L)).thenReturn(Optional.of(exam));
        when(mockedMapper.readValue(exam.getQuestionData(), new TypeReference<List<ExamQuestionPoint>>() {})).thenReturn(eqpList);
        when(questionService.getQuestionById(1L)).thenReturn(Optional.of(question1));
        examController.getQuestionTextByExamId(1L);
    }

    /**
     * @Mã Unit Test: TC_EC_GETQUESTIONTEXT_06
     * @Trường hợp test: Thành công với nhiều câu hỏi.
     * @Input: exam.questionData chứa nhiều ExamQuestionPoint.
     * @Output kỳ vọng: Danh sách ExamDetail tương ứng. Vòng lặp forEach chạy nhiều lần.
     */
    @Test
    public void testGetQuestionTextByExamId_Success_MultipleQuestions_ForEachMultiple() throws IOException {
        List<ExamQuestionPoint> eqpList = Arrays.asList(examQuestionPoint1, examQuestionPoint2);
        when(examService.getExamById(1L)).thenReturn(Optional.of(exam));
        when(mockedMapper.readValue(exam.getQuestionData(), new TypeReference<List<ExamQuestionPoint>>() {})).thenReturn(eqpList);
        when(questionService.getQuestionById(1L)).thenReturn(Optional.of(question1));
        when(questionService.getQuestionById(2L)).thenReturn(Optional.of(question2));

        List<ExamDetail> results = examController.getQuestionTextByExamId(1L);
        assertEquals(2, results.size());
        assertEquals(question1.getQuestionText(), results.get(0).getQuestionText());
        assertEquals(question2.getQuestionText(), results.get(1).getQuestionText());
    }
    //endregion

    //region TC_EC_GETEXAMCALENDAR - Test cases for getExamCalendar
    /**
     * @Mã Unit Test: TC_EC_GETEXAMCALENDAR_01
     * @Trường hợp test: User không có exam nào trong lịch.
     * @Input: userService.getExamListByUsername trả về danh sách rỗng.
     * @Output kỳ vọng: Trả về danh sách ExamCalendar rỗng. Vòng lặp `forEach` không chạy.
     */
    @Test
    public void testGetExamCalendar_NoExamUsers_ForEachEmpty() {
        setupMockAuthentication("student1", "ROLE_STUDENT");
        when(userService.getUserName()).thenReturn("student1");
        when(examUserService.getExamListByUsername("student1")).thenReturn(Collections.emptyList());
        List<ExamCalendar> result = examController.getExamCalendar();
        assertTrue(result.isEmpty());
    }

    /**
     * @Mã Unit Test: TC_EC_GETEXAMCALENDAR_02
     * @Trường hợp test: Exam đã kết thúc và user chưa bắt đầu (Missed).
     * @Input: ExamUser có exam.finishExam < now và isStarted = false.
     * @Output kỳ vọng: ExamCalendar có completeString="Missed", isCompleted=-2. Nhánh `if (examUser.getExam().getFinishExam().compareTo(now) < 0 && examUser.getIsStarted().equals(false))` đúng.
     */
    @Test
    public void testGetExamCalendar_StatusMissed() {
        setupMockAuthentication("student1", "ROLE_STUDENT");
        examUserNotStartedMissed.getExam().setFinishExam(new Date(currentDate.getTime() - 1000)); // Đảm bảo đã kết thúc
        when(userService.getUserName()).thenReturn("student1");
        when(examUserService.getExamListByUsername("student1")).thenReturn(Collections.singletonList(examUserNotStartedMissed));

        List<ExamCalendar> result = examController.getExamCalendar();
        assertEquals(1, result.size());
        assertEquals("Missed", result.get(0).getCompleteString());
        assertEquals(-2, result.get(0).getIsCompleted());
    }

    /**
     * @Mã Unit Test: TC_EC_GETEXAMCALENDAR_03
     * @Trường hợp test: Exam chưa bắt đầu (Not yet started).
     * @Input: ExamUser có isStarted = false và exam.beginExam > now.
     * @Output kỳ vọng: ExamCalendar có completeString="Not yet started", isCompleted=0. Nhánh `else if (examUser.getIsStarted().equals(false) && examUser.getExam().getBeginExam().compareTo(now) == 1)` đúng.
     */
    @Test
    public void testGetExamCalendar_StatusNotYetStarted() {
        setupMockAuthentication("student1", "ROLE_STUDENT");
        examUserNotStartedCanStart.getExam().setBeginExam(new Date(currentDate.getTime() + 1000)); // Bắt đầu trong tương lai
        when(userService.getUserName()).thenReturn("student1");
        when(examUserService.getExamListByUsername("student1")).thenReturn(Collections.singletonList(examUserNotStartedCanStart));

        List<ExamCalendar> result = examController.getExamCalendar();
        assertEquals(1, result.size());
        assertEquals("Not yet started", result.get(0).getCompleteString());
        assertEquals(0, result.get(0).getIsCompleted());
    }

    /**
     * @Mã Unit Test: TC_EC_GETEXAMCALENDAR_04
     * @Trường hợp test: Exam đã hoàn thành (Completed).
     * @Input: ExamUser có isFinished = true.
     * @Output kỳ vọng: ExamCalendar có completeString="Completed", isCompleted=-1. Nhánh `else if (examUser.getIsFinished().equals(true))` đúng.
     */
    @Test
    public void testGetExamCalendar_StatusCompleted() {
        setupMockAuthentication("student1", "ROLE_STUDENT");
        examUserCompletedScoreSet.setIsFinished(true); // Đảm bảo đã hoàn thành
        when(userService.getUserName()).thenReturn("student1");
        when(examUserService.getExamListByUsername("student1")).thenReturn(Collections.singletonList(examUserCompletedScoreSet));

        List<ExamCalendar> result = examController.getExamCalendar();
        assertEquals(1, result.size());
        assertEquals("Completed", result.get(0).getCompleteString());
        assertEquals(-1, result.get(0).getIsCompleted());
    }

    /**
     * @Mã Unit Test: TC_EC_GETEXAMCALENDAR_05
     * @Trường hợp test: Exam đang làm (Doing).
     * @Input: ExamUser có isStarted = true, isFinished = false, và exam chưa kết thúc.
     * @Output kỳ vọng: ExamCalendar có completeString="Doing", isCompleted=1. Nhánh `else` cuối cùng được kích hoạt.
     */
    @Test
    public void testGetExamCalendar_StatusDoing() {
        setupMockAuthentication("student1", "ROLE_STUDENT");
        examUserInProgress.setIsStarted(true); examUserInProgress.setIsFinished(false);
        examUserInProgress.getExam().setFinishExam(new Date(currentDate.getTime() + 3600000)); // Vẫn còn hạn
        when(userService.getUserName()).thenReturn("student1");
        when(examUserService.getExamListByUsername("student1")).thenReturn(Collections.singletonList(examUserInProgress));

        List<ExamCalendar> result = examController.getExamCalendar();
        assertEquals(1, result.size());
        assertEquals("Doing", result.get(0).getCompleteString());
        assertEquals(1, result.get(0).getIsCompleted());
    }

    /**
     * @Mã Unit Test: TC_EC_GETEXAMCALENDAR_06
     * @Trường hợp test: Dữ liệu không nhất quán, Part hoặc Course là null.
     * @Input: ExamUser có exam.part hoặc exam.part.course là null.
     * @Output kỳ vọng: Ném NullPointerException.
     */
    @Test(expected = NullPointerException.class)
    public void testGetExamCalendar_NullPartOrCourse_NPE() {
        setupMockAuthentication("student1", "ROLE_STUDENT");
        Exam examWithNullPart = new Exam(); examWithNullPart.setId(99L); examWithNullPart.setTitle("Null Part Exam");
        // examWithNullPart.setPart(null); // Gây NPE khi gọi getCourse()
        ExamUser euNullPart = new ExamUser(); euNullPart.setExam(examWithNullPart); euNullPart.setUser(studentUser);
        when(userService.getUserName()).thenReturn("student1");
        when(examUserService.getExamListByUsername("student1")).thenReturn(Collections.singletonList(euNullPart));
        examController.getExamCalendar();
    }
    //endregion

    //region TC_EC_CANCELEXAM - Test cases for cancelExam
    /**
     * @Mã Unit Test: TC_EC_CANCELEXAM_01
     * @Trường hợp test: Có thể hủy exam (exam.beginExam > now).
     * @Input: examId của exam chưa bắt đầu.
     * @Output kỳ vọng: examService.cancelExam(id) được gọi. Nhánh `if (exam.getBeginExam().compareTo(now) > 0)` đúng.
     */
    @Test
    public void testCancelExam_CanCancel_IfBranch() {
        setupMockAuthentication("admin", "ROLE_ADMIN"); // Giả sử admin thực hiện
        when(userService.getUserName()).thenReturn("admin");
        when(userService.getUserByUsername("admin")).thenReturn(Optional.of(adminUser));
        when(examService.getExamById(examForCancel.getId())).thenReturn(Optional.of(examForCancel)); // examForCancel bắt đầu trong tương lai

        examController.cancelExam(examForCancel.getId());
        verify(examService, times(1)).cancelExam(examForCancel.getId());
    }

    /**
     * @Mã Unit Test: TC_EC_CANCELEXAM_02
     * @Trường hợp test: Không thể hủy exam (exam.beginExam <= now).
     * @Input: examId của exam đã bắt đầu hoặc đã qua.
     * @Output kỳ vọng: examService.cancelExam(id) không được gọi. Nhánh `if` sai.
     */
    @Test
    public void testCancelExam_CannotCancel_IfBranchFalse() {
        setupMockAuthentication("admin", "ROLE_ADMIN");
        when(userService.getUserName()).thenReturn("admin");
        when(userService.getUserByUsername("admin")).thenReturn(Optional.of(adminUser));
        when(examService.getExamById(examCannotCancel.getId())).thenReturn(Optional.of(examCannotCancel)); // examCannotCancel đã bắt đầu

        examController.cancelExam(examCannotCancel.getId());
        verify(examService, never()).cancelExam(anyLong());
    }

    /**
     * @Mã Unit Test: TC_EC_CANCELEXAM_03
     * @Trường hợp test: User không tìm thấy.
     * @Input: examId, username không tồn tại.
     * @Output kỳ vọng: Ném NoSuchElementException.
     */
    @Test(expected = NoSuchElementException.class)
    public void testCancelExam_UserNotFound_Exception() {
        setupMockAuthentication("unknown", "ROLE_ADMIN");
        when(userService.getUserName()).thenReturn("unknown");
        when(userService.getUserByUsername("unknown")).thenReturn(Optional.empty());
        examController.cancelExam(1L);
    }

    /**
     * @Mã Unit Test: TC_EC_CANCELEXAM_04
     * @Trường hợp test: Exam không tìm thấy.
     * @Input: examId không tồn tại.
     * @Output kỳ vọng: Ném NoSuchElementException.
     */
    @Test(expected = NoSuchElementException.class)
    public void testCancelExam_ExamNotFound_Exception() {
        setupMockAuthentication("admin", "ROLE_ADMIN");
        when(userService.getUserName()).thenReturn("admin");
        when(userService.getUserByUsername("admin")).thenReturn(Optional.of(adminUser));
        when(examService.getExamById(99L)).thenReturn(Optional.empty());
        examController.cancelExam(99L);
    }
    //endregion
}