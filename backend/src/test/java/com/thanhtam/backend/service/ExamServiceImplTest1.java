package com.thanhtam.backend.service;

import com.thanhtam.backend.dto.AnswerSheet;
import com.thanhtam.backend.dto.ChoiceCorrect;
import com.thanhtam.backend.dto.ChoiceList;
import com.thanhtam.backend.dto.ExamQuestionPoint;
import com.thanhtam.backend.entity.Choice;
import com.thanhtam.backend.entity.Question;
import com.thanhtam.backend.entity.QuestionType;
import com.thanhtam.backend.ultilities.EQTypeCode;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class ExamServiceImplTest1 {

    @Mock
    private QuestionService questionService;

    @Mock
    private ChoiceService choiceService;

    @InjectMocks
    private ExamServiceImpl examService;

    private Question tfQuestion;
    private Question mcQuestion;
    private Question msQuestion;
    private Choice tfChoiceCorrect;
    private Choice tfChoiceIncorrect;
    private Choice mcChoiceCorrect;
    private Choice mcChoiceIncorrect;
    private Choice msChoiceCorrect;
    private Choice msChoiceIncorrect;

    @BeforeEach
    void setUp() {
        // Khởi tạo mock thủ công
        MockitoAnnotations.initMocks(this);

        // Thiết lập loại câu hỏi
        QuestionType tfType = new QuestionType();
        tfType.setTypeCode(EQTypeCode.TF);

        QuestionType mcType = new QuestionType();
        mcType.setTypeCode(EQTypeCode.MC);

        QuestionType msType = new QuestionType();
        msType.setTypeCode(EQTypeCode.MS);

        // Thiết lập câu hỏi
        tfQuestion = new Question();
        tfQuestion.setId(1L);
        tfQuestion.setQuestionType(tfType);

        mcQuestion = new Question();
        mcQuestion.setId(2L);
        mcQuestion.setQuestionType(mcType);

        msQuestion = new Question();
        msQuestion.setId(3L);
        msQuestion.setQuestionType(msType);

        // Thiết lập lựa chọn
        tfChoiceCorrect = new Choice();
        tfChoiceCorrect.setId(1L);
        tfChoiceCorrect.setChoiceText("True");

        tfChoiceIncorrect = new Choice();
        tfChoiceIncorrect.setId(2L);
        tfChoiceIncorrect.setChoiceText("False");

        mcChoiceCorrect = new Choice();
        mcChoiceCorrect.setId(3L);
        mcChoiceCorrect.setIsCorrected(1);

        mcChoiceIncorrect = new Choice();
        mcChoiceIncorrect.setId(4L);
        mcChoiceIncorrect.setIsCorrected(0);

        msChoiceCorrect = new Choice();
        msChoiceCorrect.setId(5L);
        msChoiceCorrect.setIsCorrected(1);

        msChoiceIncorrect = new Choice();
        msChoiceIncorrect.setId(6L);
        msChoiceIncorrect.setIsCorrected(0);
    }

    @AfterEach
    void tearDown() {
        // Reset mock để tránh rò rỉ trạng thái giữa các test
        Mockito.reset(questionService, choiceService);
    }

    // TC_ES_20
    // Lấy danh sách ChoiceList với câu hỏi True/False, lựa chọn đúng
    // input: userChoices = [AnswerSheet(questionId=1, choices=[Choice(id=1, choiceText="True")], point=1)], examQuestionPoints = []
    // output: Trả về List<ChoiceList> với 1 phần tử, isSelectedCorrected = true, isRealCorrect = 1
    @Test
    public void testGetChoiceList_TF_CorrectChoice() {
        AnswerSheet answerSheet = new AnswerSheet(1L, Arrays.asList(tfChoiceCorrect), 1);
        List<AnswerSheet> userChoices = Collections.singletonList(answerSheet);
        List<ExamQuestionPoint> examQuestionPoints = Collections.emptyList();

        when(questionService.getQuestionById(1L)).thenReturn(Optional.of(tfQuestion));
        when(choiceService.findChoiceTextById(1L)).thenReturn("True");

        List<ChoiceList> result = examService.getChoiceList(userChoices, examQuestionPoints);

        assertEquals(1, result.size());
        ChoiceList choiceList = result.get(0);
        assertEquals(tfQuestion, choiceList.getQuestion());
        assertEquals(1, choiceList.getPoint().intValue());
        assertTrue(choiceList.getIsSelectedCorrected());
        assertEquals(1, choiceList.getChoices().size());
        assertEquals(1, choiceList.getChoices().get(0).getIsRealCorrect().intValue());
        assertEquals(tfChoiceCorrect, choiceList.getChoices().get(0).getChoice());
    }

    // TC_ES_21
    // Lấy danh sách ChoiceList với câu hỏi True/False, lựa chọn sai
    // input: userChoices = [AnswerSheet(questionId=1, choices=[Choice(id=2, choiceText="False")], point=0)], examQuestionPoints = []
    // output: Trả về List<ChoiceList> với 1 phần tử, isSelectedCorrected = false, isRealCorrect = 0
    @Test
    public void testGetChoiceList_TF_IncorrectChoice() {
        AnswerSheet answerSheet = new AnswerSheet(1L, Arrays.asList(tfChoiceIncorrect), 0);
        List<AnswerSheet> userChoices = Collections.singletonList(answerSheet);
        List<ExamQuestionPoint> examQuestionPoints = Collections.emptyList();

        when(questionService.getQuestionById(1L)).thenReturn(Optional.of(tfQuestion));
        when(choiceService.findChoiceTextById(2L)).thenReturn("True");

        List<ChoiceList> result = examService.getChoiceList(userChoices, examQuestionPoints);

        assertEquals(1, result.size());
        ChoiceList choiceList = result.get(0);
        assertEquals(tfQuestion, choiceList.getQuestion());
        assertEquals(0, choiceList.getPoint().intValue());
        assertFalse(choiceList.getIsSelectedCorrected());
        assertEquals(1, choiceList.getChoices().size());
        assertEquals(0, choiceList.getChoices().get(0).getIsRealCorrect().intValue());
    }

    // TC_ES_22
    // Lấy danh sách ChoiceList với câu hỏi Multiple Choice, lựa chọn đúng
    // input: userChoices = [AnswerSheet(questionId=2, choices=[Choice(id=3, isCorrected=1)], point=1)], examQuestionPoints = []
    // output: Trả về List<ChoiceList> với 1 phần tử, isSelectedCorrected = true, isRealCorrect = 1
    @Test
    public void testGetChoiceList_MC_CorrectChoice() {
        AnswerSheet answerSheet = new AnswerSheet(2L, Arrays.asList(mcChoiceCorrect), 1);
        List<AnswerSheet> userChoices = Collections.singletonList(answerSheet);
        List<ExamQuestionPoint> examQuestionPoints = Collections.emptyList();

        when(questionService.getQuestionById(2L)).thenReturn(Optional.of(mcQuestion));
        when(choiceService.findIsCorrectedById(3L)).thenReturn(1);

        List<ChoiceList> result = examService.getChoiceList(userChoices, examQuestionPoints);

        assertEquals(1, result.size());
        ChoiceList choiceList = result.get(0);
        assertEquals(mcQuestion, choiceList.getQuestion());
        assertEquals(1, choiceList.getPoint().intValue());
        assertTrue(choiceList.getIsSelectedCorrected());
        assertEquals(1, choiceList.getChoices().size());
        assertEquals(1, choiceList.getChoices().get(0).getIsRealCorrect().intValue());
    }

    // TC_ES_23
    // Lấy danh sách ChoiceList với câu hỏi Multiple Choice, lựa chọn sai
    // input: userChoices = [AnswerSheet(questionId=2, choices=[Choice(id=4, isCorrected=0)], point=0)], examQuestionPoints = []
    // output: Trả về List<ChoiceList> với 1 phần tử, isSelectedCorrected = false, isRealCorrect = 0
    @Test
    public void testGetChoiceList_MC_IncorrectChoice() {
        AnswerSheet answerSheet = new AnswerSheet(2L, Arrays.asList(mcChoiceIncorrect), 0);
        List<AnswerSheet> userChoices = Collections.singletonList(answerSheet);
        List<ExamQuestionPoint> examQuestionPoints = Collections.emptyList();

        when(questionService.getQuestionById(2L)).thenReturn(Optional.of(mcQuestion));
        when(choiceService.findIsCorrectedById(4L)).thenReturn(0);

        List<ChoiceList> result = examService.getChoiceList(userChoices, examQuestionPoints);

        assertEquals(1, result.size());
        ChoiceList choiceList = result.get(0);
        assertEquals(mcQuestion, choiceList.getQuestion());
        assertEquals(0, choiceList.getPoint().intValue());
        assertFalse(choiceList.getIsSelectedCorrected());
        assertEquals(1, choiceList.getChoices().size());
        assertEquals(0, choiceList.getChoices().get(0).getIsRealCorrect().intValue());
    }

    // TC_ES_24
    // Lấy danh sách ChoiceList với câu hỏi Multiple Select, tất cả lựa chọn đúng
    // input: userChoices = [AnswerSheet(questionId=3, choices=[Choice(id=5, isCorrected=1)], point=1)], examQuestionPoints = []
    // output: Trả về List<ChoiceList> với 1 phần tử, isSelectedCorrected = true, isRealCorrect = 1
    @Test
    public void testGetChoiceList_MS_AllCorrect() {
        AnswerSheet answerSheet = new AnswerSheet(3L, Arrays.asList(msChoiceCorrect), 1);
        List<AnswerSheet> userChoices = Collections.singletonList(answerSheet);
        List<ExamQuestionPoint> examQuestionPoints = Collections.emptyList();

        when(questionService.getQuestionById(3L)).thenReturn(Optional.of(msQuestion));
        when(choiceService.findIsCorrectedById(5L)).thenReturn(1);

        List<ChoiceList> result = examService.getChoiceList(userChoices, examQuestionPoints);

        assertEquals(1, result.size());
        ChoiceList choiceList = result.get(0);
        assertEquals(msQuestion, choiceList.getQuestion());
        assertEquals(1, choiceList.getPoint().intValue());
        assertTrue(choiceList.getIsSelectedCorrected());
        assertEquals(1, choiceList.getChoices().size());
        assertEquals(1, choiceList.getChoices().get(0).getIsRealCorrect().intValue());
    }

    // TC_ES_25
    // Lấy danh sách ChoiceList với câu hỏi Multiple Select, có lựa chọn sai
    // input: userChoices = [AnswerSheet(questionId=3, choices=[Choice(id=6, isCorrected=0)], point=0)], examQuestionPoints = []
    // output: Trả về List<ChoiceList> với 1 phần tử, isSelectedCorrected = false, isRealCorrect = 1
    @Test
    public void testGetChoiceList_MS_SomeIncorrect() {
        AnswerSheet answerSheet = new AnswerSheet(3L, Arrays.asList(msChoiceIncorrect), 0);
        List<AnswerSheet> userChoices = Collections.singletonList(answerSheet);
        List<ExamQuestionPoint> examQuestionPoints = Collections.emptyList();

        when(questionService.getQuestionById(3L)).thenReturn(Optional.of(msQuestion));
        when(choiceService.findIsCorrectedById(6L)).thenReturn(1);

        List<ChoiceList> result = examService.getChoiceList(userChoices, examQuestionPoints);

        assertEquals(1, result.size());
        ChoiceList choiceList = result.get(0);
        assertEquals(msQuestion, choiceList.getQuestion());
        assertEquals(0, choiceList.getPoint().intValue());
        assertFalse(choiceList.getIsSelectedCorrected());
        assertEquals(1, choiceList.getChoices().size());
        assertEquals(1, choiceList.getChoices().get(0).getIsRealCorrect().intValue());
    }

    // TC_ES_26
    // Lấy danh sách ChoiceList với danh sách userChoices rỗng
    // input: userChoices = [], examQuestionPoints = []
    // output: Trả về List<ChoiceList> rỗng
    @Test
    public void testGetChoiceList_EmptyUserChoices() {
        List<AnswerSheet> userChoices = Collections.emptyList();
        List<ExamQuestionPoint> examQuestionPoints = Collections.emptyList();

        List<ChoiceList> result = examService.getChoiceList(userChoices, examQuestionPoints);

        assertTrue(result.isEmpty());
    }

    // TC_ES_27
    // Lấy danh sách ChoiceList với userChoices là null
    // input: userChoices = null, examQuestionPoints = []
    // output: trả về exception
    @Test
    public void testGetChoiceList_NullUserChoices() {
        List<ExamQuestionPoint> examQuestionPoints = Collections.emptyList();

        assertThrows(NullPointerException.class, () ->
                examService.getChoiceList(null, examQuestionPoints));
    }

    // TC_ES_28
    // Trường hợp câu hỏi không tồn tại (questionId không khớp)
    // input: userChoices = [AnswerSheet(questionId=999, choices=[Choice(id=10)], point=0)], examQuestionPoints = []
    // output: Trả về danh sách rỗng (hoặc bỏ qua câu hỏi không tồn tại)
    @Test
    public void testGetChoiceList_QuestionNotFound() {
        Choice unknownChoice = new Choice();
        unknownChoice.setId(101L);

        AnswerSheet answerSheet = new AnswerSheet(9991L, Arrays.asList(unknownChoice), 0);
        List<AnswerSheet> userChoices = Collections.singletonList(answerSheet);
        List<ExamQuestionPoint> examQuestionPoints = Collections.emptyList();

        when(questionService.getQuestionById(9991L)).thenReturn(Optional.empty());

        List<ChoiceList> result = examService.getChoiceList(userChoices, examQuestionPoints);

        // Kết quả mong đợi: bỏ qua câu hỏi không tồn tại => trả về danh sách rỗng
        assertTrue(result.isEmpty());
    }
    // TC_ES_29
    // Lấy danh sách ChoiceList với câu hỏi Multiple Choice, lựa chọn có isCorrected không khớp với isRealCorrect
    // input: userChoices = [AnswerSheet(questionId=2, choices=[Choice(id=3, isCorrected=1)], point=0)], examQuestionPoints = [], isRealCorrect = 0
    // output: Trả về List<ChoiceList> với 1 phần tử, isSelectedCorrected = false, isRealCorrect = 0
    @Test
    public void testGetChoiceList_MC_MismatchCorrectness() {
        AnswerSheet answerSheet = new AnswerSheet(2L, Arrays.asList(mcChoiceCorrect), 0);
        List<AnswerSheet> userChoices = Collections.singletonList(answerSheet);
        List<ExamQuestionPoint> examQuestionPoints = Collections.emptyList();

        when(questionService.getQuestionById(2L)).thenReturn(Optional.of(mcQuestion));
        when(choiceService.findIsCorrectedById(3L)).thenReturn(0); // isRealCorrect = 0, nhưng choice.getIsCorrected() = 1

        List<ChoiceList> result = examService.getChoiceList(userChoices, examQuestionPoints);

        assertEquals(1, result.size());
        ChoiceList choiceList = result.get(0);
        assertEquals(mcQuestion, choiceList.getQuestion());
        assertEquals(0, choiceList.getPoint().intValue());
        assertFalse(choiceList.getIsSelectedCorrected()); // Nhánh false được phủ
        assertEquals(1, choiceList.getChoices().size());
        assertEquals(0, choiceList.getChoices().get(0).getIsRealCorrect().intValue());
    }
    // TC_ES_30
    // Lấy danh sách ChoiceList với câu hỏi Multiple Select, có nhiều lựa chọn đúng và sai
    // input: userChoices = [AnswerSheet(questionId=3, choices=[Choice(id=5, isCorrected=1), Choice(id=6, isCorrected=0)], point=0.5)], examQuestionPoints = []
    // output: Trả về List<ChoiceList> với 1 phần tử, isSelectedCorrected = false, isRealCorrect có cả 1 và 0
    @Test
    public void testGetChoiceList_MS_MixedChoices() {
        AnswerSheet answerSheet = new AnswerSheet(3L, Arrays.asList(msChoiceCorrect, msChoiceIncorrect), 1);
        List<AnswerSheet> userChoices = Collections.singletonList(answerSheet);
        List<ExamQuestionPoint> examQuestionPoints = Collections.emptyList();

        when(questionService.getQuestionById(3L)).thenReturn(Optional.of(msQuestion));
        when(choiceService.findIsCorrectedById(5L)).thenReturn(1); // isRealCorrect = 1 (đúng)
        when(choiceService.findIsCorrectedById(6L)).thenReturn(0); // isRealCorrect = 0 (sai)

        List<ChoiceList> result = examService.getChoiceList(userChoices, examQuestionPoints);

        assertEquals(1, result.size());
        ChoiceList choiceList = result.get(0);
        assertEquals(msQuestion, choiceList.getQuestion());
        assertEquals(1, choiceList.getPoint(), 0.001);
        assertFalse(choiceList.getIsSelectedCorrected()); // Vì không phải tất cả lựa chọn đều đúng
        assertEquals(2, choiceList.getChoices().size());
        assertEquals(1, choiceList.getChoices().get(0).getIsRealCorrect().intValue()); // Choice đầu là đúng
        assertEquals(0, choiceList.getChoices().get(1).getIsRealCorrect().intValue()); // Choice sau là sai
    }

}