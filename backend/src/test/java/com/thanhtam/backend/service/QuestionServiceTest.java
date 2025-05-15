package com.thanhtam.backend.service;

import com.thanhtam.backend.dto.AnswerSheet;
import com.thanhtam.backend.dto.ExamQuestionPoint;
import com.thanhtam.backend.entity.Choice;
import com.thanhtam.backend.entity.Part;
import com.thanhtam.backend.entity.Question;
import com.thanhtam.backend.entity.QuestionType;
import com.thanhtam.backend.repository.QuestionRepository;
import com.thanhtam.backend.ultilities.DifficultyLevel;
import com.thanhtam.backend.ultilities.EQTypeCode;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit test cho QuestionServiceImpl
 * Sử dụng cơ sở dữ liệu thật với @AutoConfigureTestDatabase
 * Tự động rollback sau mỗi test case với @Transactional
 */
@RunWith(SpringRunner.class)
@SpringBootTest
@Transactional
@Rollback
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
public class QuestionServiceTest {

    @Autowired
    private QuestionRepository questionRepository;

    @Autowired
    private QuestionService questionService;

    @Autowired
    private PartService partService;

    @Autowired
    private QuestionTypeService questionTypeService;

    /**
     * Khởi tạo dữ liệu test trước khi chạy các test case
     */
    @Before
    public void setUp() {

        questionService = new QuestionServiceImpl(questionRepository);

        // Kiểm tra các service không null
        Assert.assertNotNull("QuestionService bị null!", questionService);
        Assert.assertNotNull("QuestionRepository bị null!", questionRepository);
        Assert.assertNotNull("PartService bị null!", partService);
        Assert.assertNotNull("QuestionTypeService bị null!", questionTypeService);
    }

    

    /**
     * TC_QS_01: Test lấy câu hỏi theo ID - trường hợp tồn tại
     * Mục tiêu: Kiểm tra phương thức getQuestionById trả về câu hỏi khi ID tồn tại
     * Input: ID = 8L (tồn tại trong DB)
     * Output kỳ vọng: Optional<Question> có giá trị
     */
    @Test
    @Transactional
    public void testGetQuestionById_Success() {
        // Lấy câu hỏi có id = 8 từ DB
        Optional<Question> question = questionService.getQuestionById(8L);
        
        // Kiểm tra kết quả
        Assert.assertTrue("QuestionType không tồn tại!", question.isPresent());
        Assert.assertEquals(8L, question.get().getId().longValue());
    }

    /**
     * TC_QS_02: Test lấy câu hỏi theo ID - trường hợp không tồn tại
     * Mục tiêu: Kiểm tra phương thức getQuestionById trả về Optional.empty() khi ID không tồn tại
     * Input: ID = 999L (không tồn tại trong DB)
     * Output kỳ vọng: Optional.empty()
     */
    @Test
    @Transactional
    public void testGetQuestionById_NotFound() {
        // Thử lấy câu hỏi không tồn tại
        Optional<Question> question = questionService.getQuestionById(999L);
        
        // Kiểm tra kết quả
        Assert.assertFalse("Question không nên tồn tại!", question.isPresent());
    }

    /**
     * TC_QS_03: Test lấy danh sách câu hỏi theo Part
     * Mục tiêu: Kiểm tra phương thức getQuestionByPart trả về danh sách câu hỏi theo Part
     * Input: part (lấy từ DB)
     * Output kỳ vọng: List<Question> chứa các câu hỏi thuộc Part
     */
    @Test
    @Transactional
    public void testGetQuestionByPart_Success() {
        // Lấy part có id = 8 từ DB
        Optional<Part> partOpt = partService.findPartById(8L);
        Assert.assertTrue("Part không tồn tại!", partOpt.isPresent());
        
        // Lấy danh sách câu hỏi theo part
        List<Question> questions = questionService.getQuestionByPart(partOpt.get());
        
        // Kiểm tra kết quả
        Assert.assertNotNull("Danh sách câu hỏi không được null!", questions);
        Assert.assertFalse("Danh sách câu hỏi không được rỗng!", questions.isEmpty());
        
        // Kiểm tra tất cả câu hỏi đều thuộc part
        for (Question question : questions) {
            Assert.assertEquals(8L, question.getPart().getId().longValue());
        }
    }

    /**
     * TC_QS_04: Test lấy danh sách câu hỏi từ danh sách điểm
     * Mục tiêu: Kiểm tra phương thức getQuestionPointList trả về danh sách câu hỏi từ danh sách điểm
     * Input: List<ExamQuestionPoint> 
     * Output kỳ vọng: List<Question> với câu hỏi có id = 8 và 9
     */
    @Test
    @Transactional
    public void testGetQuestionPointList_Success() {
        // Tạo danh sách điểm
        List<ExamQuestionPoint> examQuestionPoints = new ArrayList<>();
        
        // Thêm điểm cho câu hỏi có id = 8
        ExamQuestionPoint point1 = new ExamQuestionPoint();
        point1.setQuestionId(8L);
        point1.setPoint(5);
        examQuestionPoints.add(point1);
        
        // Thêm điểm cho câu hỏi có id = 9 (nếu có)
        if (questionService.getQuestionById(9L).isPresent()) {
            ExamQuestionPoint point2 = new ExamQuestionPoint();
            point2.setQuestionId(9L);
            point2.setPoint(10);
            examQuestionPoints.add(point2);
        }
        
        // Lấy danh sách câu hỏi từ danh sách điểm
        List<Question> questions = questionService.getQuestionPointList(examQuestionPoints);
        
        // Kiểm tra kết quả
        Assert.assertNotNull("Danh sách câu hỏi không được null!", questions);
        Assert.assertEquals(examQuestionPoints.size(), questions.size());
        
        // Kiểm tra id của câu hỏi
        for (int i = 0; i < questions.size(); i++) {
            Assert.assertEquals(examQuestionPoints.get(i).getQuestionId(), questions.get(i).getId());
        }
    }

    /**
     * TC_QS_05: Test chuyển đổi danh sách câu hỏi thành danh sách phiếu trả lời
     * Mục tiêu: Kiểm tra phương thức convertFromQuestionList chuyển đổi Question thành AnswerSheet
     * Input: List<Question>
     * Output kỳ vọng: List<AnswerSheet>
     */
    @Test
    @Transactional
    public void testConvertFromQuestionList_Success() {
        // Lấy danh sách câu hỏi từ DB
        List<Question> questions = questionService.getQuestionList();
        Assert.assertFalse("Danh sách câu hỏi không được rỗng!", questions.isEmpty());
        
        // Chuyển đổi danh sách câu hỏi thành danh sách phiếu trả lời
        List<AnswerSheet> answerSheets = questionService.convertFromQuestionList(questions);
        
        // Kiểm tra kết quả
        Assert.assertNotNull("Danh sách phiếu trả lời không được null!", answerSheets);
        Assert.assertEquals(questions.size(), answerSheets.size());
        
        // Kiểm tra thông tin của phiếu trả lời
        for (int i = 0; i < answerSheets.size(); i++) {
            AnswerSheet answerSheet = answerSheets.get(i);
            Question question = questions.get(i);
            
            Assert.assertEquals(question.getId().longValue(), answerSheet.getQuestionId().longValue());
            // Assert.assertEquals(question.getPoint(), answerSheet.getPoint());
            
            // Kiểm tra tất cả các lựa chọn đều được đặt isCorrected = 0
            for (Choice choice : answerSheet.getChoices()) {
                Assert.assertEquals(0, choice.getIsCorrected());
            }
        }
    }

    /**
     * TC_QS_06: Test lưu câu hỏi mức EASY
     * Mục tiêu: Kiểm tra phương thức save tự động tính điểm cho câu hỏi mức EASY
     * Input: Question với difficultyLevel = EASY
     * Output kỳ vọng: point = 5
     */
    @Test
    @Transactional
    @Rollback
    public void testSaveQuestion_EASY() {
        // Tạo câu hỏi mới với mức EASY
        Question question = new Question();
        question.setQuestionText("Test Question EASY");
        question.setDifficultyLevel(DifficultyLevel.EASY);
        
        // Lấy part và questionType từ DB
        Optional<Part> partOpt = partService.findPartById(1L);
        Optional<QuestionType> questionTypeOpt = questionTypeService.getQuestionTypeById(1L);
        
        Assert.assertTrue("Part không tồn tại!", partOpt.isPresent());
        Assert.assertTrue("QuestionType không tồn tại!", questionTypeOpt.isPresent());
        
        question.setPart(partOpt.get());
        question.setQuestionType(questionTypeOpt.get());
        
        // Lưu câu hỏi
        questionService.save(question);
        
        // Kiểm tra kết quả
        Optional<Question> savedQuestion = questionService.getQuestionById(question.getId());
        Assert.assertTrue("Câu hỏi không được lưu!", savedQuestion.isPresent());
        Assert.assertEquals(5, savedQuestion.get().getPoint());
    }

    /**
     * TC_QS_07: Test lưu câu hỏi mức MEDIUM
     * Mục tiêu: Kiểm tra phương thức save tự động tính điểm cho câu hỏi mức MEDIUM
     * Input: Question với difficultyLevel = MEDIUM
     * Output kỳ vọng: point = 10
     */
    @Test
    @Transactional
    @Rollback
    public void testSaveQuestion_MEDIUM() {
        // Tạo câu hỏi mới với mức MEDIUM
        Question question = new Question();
        question.setQuestionText("Test Question MEDIUM");
        question.setDifficultyLevel(DifficultyLevel.MEDIUM);
        
        // Lấy part và questionType từ DB
        Optional<Part> partOpt = partService.findPartById(1L);
        Optional<QuestionType> questionTypeOpt = questionTypeService.getQuestionTypeById(1L);
        
        Assert.assertTrue("Part không tồn tại!", partOpt.isPresent());
        Assert.assertTrue("QuestionType không tồn tại!", questionTypeOpt.isPresent());
        
        question.setPart(partOpt.get());
        question.setQuestionType(questionTypeOpt.get());
        
        // Lưu câu hỏi
        questionService.save(question);
        
        // Kiểm tra kết quả
        Optional<Question> savedQuestion = questionService.getQuestionById(question.getId());
        Assert.assertTrue("Câu hỏi không được lưu!", savedQuestion.isPresent());
        Assert.assertEquals(10, savedQuestion.get().getPoint());
    }

    /**
     * TC_QS_08: Test lưu câu hỏi mức HARD
     * Mục tiêu: Kiểm tra phương thức save tự động tính điểm cho câu hỏi mức HARD
     * Input: Question với difficultyLevel = HARD
     * Output kỳ vọng: point = 15
     */
    @Test
    @Transactional
    @Rollback
    public void testSaveQuestion_HARD() {
        // Tạo câu hỏi mới với mức HARD
        Question question = new Question();
        question.setQuestionText("Test Question HARD");
        question.setDifficultyLevel(DifficultyLevel.HARD);
        
        // Lấy part và questionType từ DB
        Optional<Part> partOpt = partService.findPartById(1L);
        Optional<QuestionType> questionTypeOpt = questionTypeService.getQuestionTypeById(1L);
        
        Assert.assertTrue("Part không tồn tại!", partOpt.isPresent());
        Assert.assertTrue("QuestionType không tồn tại!", questionTypeOpt.isPresent());
        
        question.setPart(partOpt.get());
        question.setQuestionType(questionTypeOpt.get());
        
        // Lưu câu hỏi
        questionService.save(question);
        
        // Kiểm tra kết quả
        Optional<Question> savedQuestion = questionService.getQuestionById(question.getId());
        Assert.assertTrue("Câu hỏi không được lưu!", savedQuestion.isPresent());
        Assert.assertEquals(15, savedQuestion.get().getPoint());
    }

    /**
     * TC_QS_09: Test cập nhật thông tin câu hỏi
     * Mục tiêu: Kiểm tra phương thức update cập nhật thông tin câu hỏi
     * Input: Question đã được cập nhật
     * Output kỳ vọng: Gọi save() để lưu câu hỏi
     */
    @Test
    @Transactional
    @Rollback
    public void testUpdateQuestion_Success() {
        // Lấy câu hỏi có id = 8 từ DB
        Optional<Question> questionOpt = questionService.getQuestionById(8L);
        Assert.assertTrue("Câu hỏi không tồn tại!", questionOpt.isPresent());
        
        // Cập nhật thông tin câu hỏi
        Question question = questionOpt.get();
        String originalText = question.getQuestionText();
        question.setQuestionText("Updated Question Text");
        
        // Cập nhật câu hỏi
        questionService.update(question);
        
        // Kiểm tra kết quả
        Optional<Question> updatedQuestion = questionService.getQuestionById(8L);
        Assert.assertTrue("Câu hỏi không tồn tại sau khi cập nhật!", updatedQuestion.isPresent());
        Assert.assertEquals("Updated Question Text", updatedQuestion.get().getQuestionText());
        
        // Khôi phục lại dữ liệu ban đầu
        question.setQuestionText(originalText);
        questionService.update(question);
    }

    /**
     * TC_QS_10: Test xóa câu hỏi
     * Mục tiêu: Kiểm tra phương thức delete xóa câu hỏi
     * Input: ID = 8L
     * Output kỳ vọng: Gọi deleteById() để xóa câu hỏi
     */
    @Test
    @Transactional
    @Rollback
    public void testDeleteQuestion_Success() {
        // Lấy câu hỏi có id = 8 từ DB
        Optional<Question> questionOpt = questionService.getQuestionById(8L);
        Assert.assertTrue("Câu hỏi không tồn tại!", questionOpt.isPresent());
        
        // Xóa câu hỏi
        questionService.delete(8L);
        
        // Kiểm tra câu hỏi đã bị xóa
        Optional<Question> deletedQuestion = questionService.getQuestionById(8L);
        Assert.assertFalse("Câu hỏi vẫn còn tồn tại!", deletedQuestion.isPresent());
    }

    /**
     * TC_QS_11: Test lấy danh sách câu hỏi theo Part có phân trang
     * Mục tiêu: Kiểm tra phương thức findQuestionsByPart trả về danh sách câu hỏi theo Part có phân trang
     * Input: Pageable và Part
     * Output kỳ vọng: Page<Question>
     */
    @Test
    @Transactional
    public void testFindQuestionsByPart_Success() {
        // Lấy part có id = 73 từ DB
        Optional<Part> partOpt = partService.findPartById(73L);
        Assert.assertTrue("Part không tồn tại!", partOpt.isPresent());
        
        // Tạo Pageable
        Pageable pageable = PageRequest.of(0, 10);
        
        // Lấy danh sách câu hỏi theo part có phân trang
        Page<Question> questionPage = questionService.findQuestionsByPart(pageable, partOpt.get());
        
        // Kiểm tra kết quả
        Assert.assertNotNull("Page câu hỏi không được null!", questionPage);
        Assert.assertFalse("Page câu hỏi không được rỗng!", questionPage.getContent().isEmpty());
        
        // Kiểm tra tất cả câu hỏi đều thuộc part
        for (Question question : questionPage.getContent()) {
            Assert.assertEquals(73L, question.getPart().getId().longValue());
        }
    }

    /**
     * TC_QS_12: Test lấy danh sách câu hỏi chưa bị xóa theo Part có phân trang
     * Mục tiêu: Kiểm tra phương thức findQuestionsByPartAndDeletedFalse trả về danh sách câu hỏi chưa bị xóa theo Part
     * Input: Pageable và Part
     * Output kỳ vọng: Page<Question> chứa các câu hỏi chưa bị xóa
     */
    @Test
    @Transactional
    public void testFindQuestionsByPartAndDeletedFalse_Success() {
        // Lấy part có id = 73 từ DB
        Optional<Part> partOpt = partService.findPartById(73L);
        Assert.assertTrue("Part không tồn tại!", partOpt.isPresent());
        
        // Tạo Pageable
        Pageable pageable = PageRequest.of(0, 10);
        
        // Lấy danh sách câu hỏi chưa bị xóa theo part có phân trang
        Page<Question> questionPage = questionService.findQuestionsByPartAndDeletedFalse(pageable, partOpt.get());
        
        // Kiểm tra kết quả
        Assert.assertNotNull("Page câu hỏi không được null!", questionPage);
        Assert.assertFalse("Page câu hỏi không được rỗng!", questionPage.getContent().isEmpty());
        
        // Kiểm tra tất cả câu hỏi đều chưa bị xóa
        for (Question question : questionPage.getContent()) {
            Assert.assertFalse("Câu hỏi đã bị xóa!", question.isDeleted());
        }
    }

    /**
     * TC_QS_13: Test lấy nội dung câu hỏi theo ID
     * Mục tiêu: Kiểm tra phương thức findQuestionTextById trả về nội dung câu hỏi theo ID
     * Input: questionId = 8L
     * Output kỳ vọng: String chứa nội dung câu hỏi
     */
    @Test
    @Transactional
    public void testFindQuestionTextById_Success() {
        // Lấy câu hỏi có id = 8 từ DB
        Optional<Question> questionOpt = questionService.getQuestionById(8L);
        Assert.assertTrue("Câu hỏi không tồn tại!", questionOpt.isPresent());
        
        // Lấy nội dung câu hỏi
        String questionText = questionService.findQuestionTextById(8L);
        
        // Kiểm tra kết quả
        Assert.assertNotNull("Nội dung câu hỏi không được null!", questionText);
        System.out.println(questionText);
        System.out.println(questionOpt.get().getQuestionText());
        Assert.assertTrue(questionOpt.get().getQuestionText().equals(questionText));
    }

    /**
     * TC_QS_14: Test lấy danh sách tất cả câu hỏi
     * Mục tiêu: Kiểm tra phương thức getQuestionList trả về danh sách tất cả câu hỏi
     * Input: Không có
     * Output kỳ vọng: List<Question> chứa tất cả câu hỏi
     */
    @Test
    @Transactional
    public void testGetQuestionList_Success() {
        // Lấy danh sách tất cả câu hỏi
        List<Question> questions = questionService.getQuestionList();
        
        // Kiểm tra kết quả
        Assert.assertNotNull("Danh sách câu hỏi không được null!", questions);
        Assert.assertFalse("Danh sách câu hỏi không được rỗng!", questions.isEmpty());
    }

    /**
     * TC_QS_15: Test lấy danh sách câu hỏi chưa bị xóa theo Part null
     * Mục tiêu: Kiểm tra phương thức findQuestionsByPartAndDeletedFalse xử lý trường hợp part null
     * Input: Pageable và Part = null
     * Output kỳ vọng: Page<Question> rỗng
     */
    @Test
    @Transactional
    public void testFindQuestionsByPartAndDeletedFalse_NullPart() {
        // Tạo Pageable
        Pageable pageable = PageRequest.of(0, 10);
        
        // Lấy danh sách câu hỏi chưa bị xóa theo part null
        Page<Question> questionPage = questionService.findQuestionsByPartAndDeletedFalse(pageable, null);
        
        // Kiểm tra kết quả
        Assert.assertNotNull("Page câu hỏi không được null!", questionPage);
        Assert.assertTrue("Page câu hỏi phải rỗng!", questionPage.getContent().isEmpty());
    }

    /**
     * TC_QS_16: Test lấy danh sách câu hỏi theo Part null
     * Mục tiêu: Kiểm tra phương thức findQuestionsByPart xử lý trường hợp part null
     * Input: Pageable và Part = null
     * Output kỳ vọng: Page<Question> rỗng
     */
    @Test
    @Transactional
    public void testFindQuestionsByPart_NullPart() {
        // Tạo Pageable
        Pageable pageable = PageRequest.of(0, 10);
        
        // Lấy danh sách câu hỏi theo part null
        Page<Question> questionPage = questionService.findQuestionsByPart(pageable, null);
        
        // Kiểm tra kết quả
        Assert.assertNotNull("Page câu hỏi không được null!", questionPage);
        Assert.assertTrue("Page câu hỏi phải rỗng!", questionPage.getContent().isEmpty());
    }

    /**
     * TC_QS_17: Test lấy danh sách câu hỏi theo Part null
     * Mục tiêu: Kiểm tra phương thức getQuestionByPart xử lý trường hợp part null
     * Input: Part = null
     * Output kỳ vọng: List<Question> rỗng
     */
    @Test
    @Transactional
    public void testGetQuestionByPart_NullPart() {
        // Lấy danh sách câu hỏi theo part null
        List<Question> questions = questionService.getQuestionByPart(null);
        
        // Kiểm tra kết quả
        Assert.assertNotNull("Danh sách câu hỏi không được null!", questions);
        Assert.assertTrue("Danh sách câu hỏi phải rỗng!", questions.isEmpty());
    }

    /**
     * TC_QS_18: Test lấy danh sách câu hỏi từ danh sách điểm rỗng
     * Mục tiêu: Kiểm tra phương thức getQuestionPointList xử lý trường hợp danh sách rỗng
     * Input: List<ExamQuestionPoint> rỗng
     * Output kỳ vọng: List<Question> rỗng
     */
    @Test
    @Transactional
    public void testGetQuestionPointList_EmptyList() {
        // Tạo danh sách điểm rỗng
        List<ExamQuestionPoint> examQuestionPoints = new ArrayList<>();
        
        // Lấy danh sách câu hỏi từ danh sách điểm rỗng
        List<Question> questions = questionService.getQuestionPointList(examQuestionPoints);
        
        // Kiểm tra kết quả
        Assert.assertNotNull("Danh sách câu hỏi không được null!", questions);
        Assert.assertTrue("Danh sách câu hỏi phải rỗng!", questions.isEmpty());
    }

    /**
     * TC_QS_19: Test chuyển đổi danh sách câu hỏi rỗng thành danh sách phiếu trả lời
     * Mục tiêu: Kiểm tra phương thức convertFromQuestionList xử lý trường hợp danh sách rỗng
     * Input: List<Question> rỗng
     * Output kỳ vọng: List<AnswerSheet> rỗng
     */
    @Test
    @Transactional
    public void testConvertFromQuestionList_EmptyList() {
        // Tạo danh sách câu hỏi rỗng
        List<Question> questions = new ArrayList<>();
        
        // Chuyển đổi danh sách câu hỏi rỗng thành danh sách phiếu trả lời
        List<AnswerSheet> answerSheets = questionService.convertFromQuestionList(questions);
        
        // Kiểm tra kết quả
        Assert.assertNotNull("Danh sách phiếu trả lời không được null!", answerSheets);
        Assert.assertTrue("Danh sách phiếu trả lời phải rỗng!", answerSheets.isEmpty());
    }

    /**
     * TC_QS_20: Test lấy danh sách câu hỏi từ danh sách điểm với ID không tồn tại
     * Mục tiêu: Kiểm tra phương thức getQuestionPointList xử lý trường hợp ID không tồn tại
     * Input: List<ExamQuestionPoint> với ID không tồn tại
     * Output kỳ vọng: NoSuchElementException
     */
    @Test
    @Transactional
    public void testGetQuestionPointList_QuestionNotFound() {
        // Tạo danh sách điểm với questionID không tồn tại
        List<ExamQuestionPoint> examQuestionPoints = new ArrayList<>();
        ExamQuestionPoint point = new ExamQuestionPoint();
        point.setQuestionId(9999L); // ID không tồn tại
        point.setPoint(5);
        examQuestionPoints.add(point);

        // Lấy danh sách câu hỏi từ danh sách điểm với questionID không tồn tại
        try {
            questionService.getQuestionPointList(examQuestionPoints);
            Assert.fail("Phải ném ra NoSuchElementException!");
        } catch (NoSuchElementException e) {
            // Đúng như mong đợi
        }
    }

    /**
     * TC_QS_21: Test chuyển đổi danh sách câu hỏi thành danh sách phiếu trả lời với câu hỏi không có choices
     * Mục tiêu: Kiểm tra phương thức convertFromQuestionList xử lý trường hợp câu hỏi không có choices
     * Input: List<Question> với một câu hỏi không có choices
     * Output kỳ vọng: List<AnswerSheet> với choices là null
     */
    @Test
    @Transactional
    public void testConvertFromQuestionList_QuestionWithoutChoices() {
        // Tạo danh sách câu hỏi với một câu hỏi không có choices
        Question question = new Question();
        question.setId(1L);
        question.setChoices(null); // Không có choices
        question.setPoint(10);

        List<Question> questions = new ArrayList<>();
        questions.add(question);

        try {
            // Chuyển đổi danh sách câu hỏi thành danh sách phiếu trả lời
            List<AnswerSheet> answerSheets = questionService.convertFromQuestionList(questions);

            // Kiểm tra kết quả
            Assert.assertNotNull("Danh sách phiếu trả lời không được null!", answerSheets);
            Assert.assertEquals(1, answerSheets.size());
            Assert.assertNull("Choices phải là null!", answerSheets.get(0).getChoices());
        } catch (NullPointerException e) {
            Assert.fail("Không nên ném ra exception: " + e.getMessage());
        }
    }

    /**
     * TC_QS_22: Test lưu câu hỏi với difficultyLevel null
     * Mục tiêu: Kiểm tra phương thức save xử lý trường hợp difficultyLevel null
     * Input: Question với difficultyLevel = null
     * Output kỳ vọng: Điểm mặc định là 0
     * Lưu ý: Câu hỏi vẫn được lưu thành công
     * Chú thích: Trong thực tế, có thể không nên cho phép lưu câu hỏi với difficultyLevel null
     */
    @Test
    @Transactional
    @Rollback
    public void testSaveQuestion_NullDifficultyLevel() {
        // Tạo câu hỏi mới với difficultyLevel = null
        Question question = new Question();
        question.setQuestionText("Test Question NULL Difficulty");
        question.setDifficultyLevel(null); // Null difficulty level

        // Lấy part và questionType từ DB
        Optional<Part> partOpt = partService.findPartById(1L);
        Optional<QuestionType> questionTypeOpt = questionTypeService.getQuestionTypeById(1L);

        Assert.assertTrue("Part không tồn tại!", partOpt.isPresent());
        Assert.assertTrue("QuestionType không tồn tại!", questionTypeOpt.isPresent());

        question.setPart(partOpt.get());
        question.setQuestionType(questionTypeOpt.get());

        // Lưu câu hỏi
        try {
            questionService.save(question);

            // Kiểm tra kết quả
            Optional<Question> savedQuestion = questionService.getQuestionById(question.getId());
            Assert.assertTrue("Câu hỏi không được lưu!", savedQuestion.isPresent());
            Assert.assertEquals(0, savedQuestion.get().getPoint()); // Điểm mặc định là 0
        } catch (Exception e) {

        }
    }

    /**
     * TC_QS_23: Test lấy nội dung câu hỏi theo ID không tồn tại
     * Mục tiêu: Kiểm tra phương thức findQuestionTextById xử lý trường hợp id không tồn tại
     * Input: questionId = 999L (không tồn tại)
     * Output kỳ vọng: null
     */
    @Test
    @Transactional
    public void testFindQuestionTextById_NotFound() {
        // Lấy nội dung câu hỏi không tồn tại
        String questionText = questionService.findQuestionTextById(999L);
        
        // Kiểm tra kết quả
        Assert.assertNull("Nội dung câu hỏi phải là null!", questionText);
    }

    /**
     * TC_QS_24: Test lưu câu hỏi null
     * Mục tiêu: Kiểm tra phương thức save xử lý trường hợp question null
     * Input: Question = null
     * Output kỳ vọng: NullPointerException
     */
    @Test
    @Transactional
    @Rollback
    public void testSaveQuestion_NullQuestion() {
        // Thử lưu câu hỏi null
        try {
            questionService.save(null);
            Assert.fail("Phải ném ra NullPointerException!");
        } catch (NullPointerException e) {
            // Đúng như mong đợi
        }
    }

    /**
     * TC_QS_25: Test cập nhật câu hỏi null
     * Mục tiêu: Kiểm tra phương thức update xử lý trường hợp question null
     * Input: Question = null
     * Output kỳ vọng: NullPointerException
     */
    @Test
    @Transactional
    @Rollback
    public void testUpdateQuestion_NullQuestion() {
        // Thử cập nhật câu hỏi null
        try {
            questionService.update(null);
            Assert.fail("Phải ném ra NullPointerException!");
        } catch (NullPointerException e) {
            // Đúng như mong đợi
        }
    }

    /**
     * TC_QS_26: Test cập nhật câu hỏi không tồn tại
     * Mục tiêu: Kiểm tra phương thức update xử lý trường hợp câu hỏi không tồn tại
     * Input: Question với id không tồn tại trong CSDL
     * Output kỳ vọng: Exception
     */
    @Test
    @Transactional
    @Rollback
    public void testUpdateQuestion_NotFound() {
        // Tạo câu hỏi mới không tồn tại trong DB
        Question question = new Question();
        question.setId(9999L); // ID không tồn tại
        question.setQuestionText("Nonexistent Question");

        // Thử cập nhật câu hỏi
        try {
            questionService.update(question);
            Assert.fail("Phải ném ra Exception!");
        } catch (Exception e) {
            // Đúng như mong đợi
        }
    }

    /**
     * TC_QS_27: Test xóa câu hỏi với id null
     * Mục tiêu: Kiểm tra phương thức delete xử lý trường hợp id null
     * Input: ID = null
     * Output kỳ vọng: IllegalArgumentException
     */
    @Test
    @Transactional
    @Rollback
    public void testDeleteQuestion_NullId() {
        // Thử xóa câu hỏi với id null
        try {
            questionService.delete(new Long(null));
            Assert.fail("Phải ném ra IllegalArgumentException!");
        } catch (IllegalArgumentException e) {
            // Đúng như mong đợi
        }
    }

    /**
     * TC_QS_28: Test xóa câu hỏi không tồn tại
     * Mục tiêu: Kiểm tra phương thức delete xử lý trường hợp câu hỏi không tồn tại
     * Input: ID = 9999L (không tồn tại trong DB)
     * Output kỳ vọng: NoSuchElementException
     */
    @Test
    @Transactional
    @Rollback
    public void testDeleteQuestion_NotFound() {
        // Thử xóa câu hỏi với ID không tồn tại
        try {
            questionService.delete(999L); // ID không tồn tại
            Assert.fail("Phải ném ra RuntimeException!");
        } catch (RuntimeException e) {
            // Đúng như mong đợi
        }
    }
    
    /**
     * TC_QS_29: Test lấy câu hỏi theo questionType null
     * Mục tiêu: Kiểm tra phương thức getQuestionByQuestionType xử lý trường hợp questionType null
     * Input: questionType = null
     * Output kỳ vọng: IllegalArgumentException hoặc NullPointerException
     */
    @Test
    @Transactional
    public void testGetQuestionByQuestionType_NullType() {
        // Gọi phương thức với questionType = null
        try {
            questionService.getQuestionByQuestionType(null);
            Assert.fail("Phải ném ra IllegalArgumentException hoặc NullPointerException!");
        } catch (IllegalArgumentException | NullPointerException e) {
            // Đúng như mong đợi
        }
    }

    /**
     * TC_QS_30: Test lấy câu hỏi theo questionType có tồn tại
     * Mục tiêu: Kiểm tra phương thức getQuestionByQuestionType trả về danh sách câu hỏi theo questionType
     * Input: questionType (lấy từ DB)
     * Output kỳ vọng: List<Question> chứa các câu hỏi thuộc questionType
     */
    @Test
    @Transactional
    public void testGetQuestionByQuestionType_ExistsWithQuestions() {
        // Lấy questionType từ DB (ID = 1 tồn tại)
        Optional<QuestionType> questionTypeOpt = questionTypeService.getQuestionTypeById(1L);
        Assert.assertTrue("QuestionType không tồn tại!", questionTypeOpt.isPresent());

        // Gọi phương thức
        List<Question> questions = questionService.getQuestionByQuestionType(questionTypeOpt.get());


        // Kiểm tra kết quả
        Assert.assertNotNull("Danh sách câu hỏi không được null!", questions);
        Assert.assertFalse("Danh sách câu hỏi không được rỗng!", questions.isEmpty());
        questions.forEach(e -> {
            System.out.println(e.getQuestionType().getId());
            Assert.assertEquals(e.getQuestionType().getId(), questionTypeOpt.get().getId());
        });
    }

    // @Test
    // @Transactional
    // public void testGetQuestionByQuestionType_ExistsWithoutQuestions() {
    //     // Tạo questionType mới không có câu hỏi
    //     QuestionType questionType = new QuestionType();
    //     questionType.setTypeCode("TestQT");;
    //     questionTypeService.save(questionType);

    //     // Gọi phương thức
    //     List<Question> questions = questionService.getQuestionByQuestionType(questionType);

    //     // Kiểm tra kết quả
    //     Assert.assertNotNull("Danh sách câu hỏi không được null!", questions);
    //     Assert.assertTrue("Danh sách câu hỏi phải rỗng!", questions.isEmpty());
    // }

    /**
     * TC_QS_31: Test lấy câu hỏi theo questionType không tồn tại
     * Mục tiêu: Kiểm tra phương thức getQuestionByQuestionType xử lý trường hợp questionType không tồn tại
     * Input: questionType (không tồn tại trong DB)
     * Output kỳ vọng: Danh sách câu hỏi rỗng
     */
    @Test
    @Transactional
    public void testGetQuestionByQuestionType_NotExists() {
        // Tạo questionType không tồn tại trong DB
        QuestionType questionType = new QuestionType();
        questionType.setId(999L); // ID không tồn tại
        questionType.setTypeCode(EQTypeCode.TF);
        questionType.setDescription("Test Question Type");

        // Gọi phương thức
        List<Question> questions = questionService.getQuestionByQuestionType(questionType);

        // Kiểm tra kết quả
        Assert.assertNotNull("Danh sách câu hỏi không được null!", questions);
        Assert.assertTrue("Danh sách câu hỏi phải rỗng!", questions.isEmpty());
    }

    /**
     * TC_QS_32: Test lấy danh sách câu hỏi theo partId và username
     * Mục tiêu: Kiểm tra phương thức findQuestionsByPart_IdAndCreatedBy_UsernameAndDeletedFalse trả về danh sách câu hỏi theo partId và username
     * Input: Pageable, partId, username
     * Output kỳ vọng: Page<Question> chứa các câu hỏi theo partId và username
     */
    @Test
    public void testFindQuestionsByPart_IdAndCreatedBy_UsernameAndDeletedFalse_Success() {
        // Lấy part hợp lệ và username hợp lệ
        Long partId = 75L; // ID của part hợp lệ

        Pageable pageable = PageRequest.of(0, 10);
        String username = "tamht298"; // Tên người dùng hợp lệ

        Page<Question> page = questionService.findQuestionsByPart_IdAndCreatedBy_UsernameAndDeletedFalse(pageable, partId, username);
        Assert.assertNotNull(page);
        for (Question q : page.getContent()) {
            Assert.assertEquals(partId, q.getPart().getId());
            Assert.assertEquals(username, q.getCreatedBy().getUsername());
            Assert.assertFalse(q.isDeleted());
        }
    }

    /**
     * TC_QS_33: Test lấy danh sách câu hỏi theo partId và username không có câu hỏi nào chưa xóa
     * Mục tiêu: Kiểm tra phương thức findQuestionsByPart_IdAndCreatedBy_UsernameAndDeletedFalse trả về danh sách câu hỏi theo partId và username không có câu hỏi nào chưa xóa
     * Input: Pageable, partId, username không có câu hỏi nào chưa xóa
     * Output kỳ vọng: Page<Question> rỗng
     */
    @Test
    public void testFindQuestionsByPart_IdAndCreatedBy_UsernameAndDeletedFalse_NoResult() {
        // Lấy part hợp lệ và username không có câu hỏi nào chưa xóa
        Long partId = 75L; // ID của part hợp lệ


        String nonExistUsername = "user_khong_ton_tai_" + System.currentTimeMillis();

        Pageable pageable = PageRequest.of(0, 10);
        Page<Question> page = questionService.findQuestionsByPart_IdAndCreatedBy_UsernameAndDeletedFalse(pageable, partId, nonExistUsername);
        Assert.assertNotNull(page);
        Assert.assertTrue(page.getContent().isEmpty());
    }

    /**
     * TC_QS_34: Test lấy danh sách câu hỏi theo partId và username không tồn tại
     * Mục tiêu: Kiểm tra phương thức findQuestionsByPart_IdAndCreatedBy_UsernameAndDeletedFalse trả về danh sách câu hỏi theo partId và username không tồn tại
     * Input: Pageable, partId không tồn tại, username hợp lệ
     * Output kỳ vọng: Page<Question> rỗng
     */
    @Test
    public void testFindQuestionsByPart_IdAndCreatedBy_UsernameAndDeletedFalse_InvalidPart() {
        // partId không tồn tại
        Long invalidPartId = -999L;
        String username = "thanhtam28ss";
        Pageable pageable = PageRequest.of(0, 10);
        Page<Question> page = questionService.findQuestionsByPart_IdAndCreatedBy_UsernameAndDeletedFalse(pageable, invalidPartId, username);
        Assert.assertNotNull(page);
        Assert.assertTrue(page.getContent().isEmpty());
    }

    /**
     * TC_QS_35: Test lấy danh sách câu hỏi theo partId và username
     * Mục tiêu: Kiểm tra phương thức findQuestionsByPart_IdAndCreatedBy_Username trả về danh sách câu hỏi theo partId và username
     * Input: Pageable, partId, username
     * Output kỳ vọng: Page<Question> chứa các câu hỏi theo partId và username
     */
    @Test
    public void testFindQuestionsByPart_IdAndCreatedBy_Username_Success() {
        // Lấy part hợp lệ và username hợp lệ
        Long partId = 75L; // ID của part hợp lệ

        String username = "tamht298"; // Tên người dùng hợp lệ
        
        Assert.assertNotNull("Không tìm thấy username phù hợp!", username);

        Pageable pageable = PageRequest.of(0, 10);

        Page<Question> page = questionService.findQuestionsByPart_IdAndCreatedBy_Username(pageable, partId, username);
        Assert.assertNotNull(page);
        for (Question q : page.getContent()) {
            Assert.assertEquals(partId, q.getPart().getId());
            Assert.assertEquals(username, q.getCreatedBy().getUsername());
        }
    }

    /**
     * TC_QS_36: Test lấy danh sách câu hỏi theo partId và username không có câu hỏi nào
     * Mục tiêu: Kiểm tra phương thức findQuestionsByPart_IdAndCreatedBy_Username trả về danh sách câu hỏi theo partId và username không có câu hỏi nào
     * Input: Pageable, partId, username không có câu hỏi nào
     * Output kỳ vọng: Page<Question> rỗng
     */
    @Test
    public void testFindQuestionsByPart_IdAndCreatedBy_Username_NoResult() {
        // Lấy part hợp lệ và username không có câu hỏi nào
        Long partId = 75L; // ID của part hợp lệ
        String nonExistUsername = "user_khong_ton_tai_" + System.currentTimeMillis();

        Pageable pageable = PageRequest.of(0, 10);
        Page<Question> page = questionService.findQuestionsByPart_IdAndCreatedBy_Username(pageable, partId, nonExistUsername);
        Assert.assertNotNull(page);
        Assert.assertTrue(page.getContent().isEmpty());
    }

    /**
     * TC_QS_37: Test lấy danh sách câu hỏi theo partId và username không tồn tại
     * Mục tiêu: Kiểm tra phương thức findQuestionsByPart_IdAndCreatedBy_Username trả về danh sách câu hỏi theo partId và username không tồn tại
     * Input: Pageable, partId không tồn tại, username hợp lệ
     * Output kỳ vọng: Page<Question> rỗng
     */
    @Test
    public void testFindQuestionsByPart_IdAndCreatedBy_Username_InvalidPart() {
        // partId không tồn tại
        Long invalidPartId = 99999L;
        String username = "tamht298"; // Tên người dùng hợp lệ
        Pageable pageable = PageRequest.of(0, 10);
        Page<Question> page = questionService.findQuestionsByPart_IdAndCreatedBy_Username(pageable, invalidPartId, username);
        Assert.assertNotNull(page);
        Assert.assertTrue(page.getContent().isEmpty());
    }

    /**
     * TC_QS_38: Test lấy danh sách tất cả câu hỏi
     * Mục tiêu: Kiểm tra phương thức findAllQuestions trả về danh sách tất cả câu hỏi
     * Input: Pageable
     * Output kỳ vọng: Page<Question> chứa tất cả câu hỏi
     */
    @Test
    @Transactional
    public void testFindAllQuestions_WithQuestions() {
        // Tạo Pageable
        Pageable pageable = PageRequest.of(0, 5);

        // Gọi phương thức
        Page<Question> questionsPage = questionService.findAllQuestions(pageable);

        // Kiểm tra kết quả
        Assert.assertNotNull("Danh sách câu hỏi không được null!", questionsPage);
        Assert.assertFalse("Danh sách câu hỏi không được rỗng!", questionsPage.getContent().isEmpty());
    }

    /**
     * TC_QS_39: Test lấy danh sách câu hỏi theo username
     * Mục tiêu: Kiểm tra phương thức findQuestionsByCreatedBy_Username trả về danh sách câu hỏi theo username
     * Input: Pageable, username
     * Output kỳ vọng: Page<Question> chứa các câu hỏi theo username
     */
    @Test
    public void testFindQuestionsByCreatedBy_Username_Success() {
        // Lấy username hợp lệ từ DB
        Pageable pageable = PageRequest.of(0, 10);
        Page<Question> allQuestions = questionService.findAllQuestions(pageable);

        String username = "tamht298"; // Tên người dùng hợp lệ
        for (Question q : allQuestions.getContent()) {
            if (q.getCreatedBy() != null) {
                username = q.getCreatedBy().getUsername();
                break;
            }
        }
        Assert.assertNotNull("Không tìm thấy username phù hợp!", username);

        Page<Question> page = questionService.findQuestionsByCreatedBy_Username(pageable, username);
        Assert.assertNotNull(page);
        for (Question q : page.getContent()) {
            Assert.assertEquals(username, q.getCreatedBy().getUsername());
        }
    }

    /**
     * TC_QS_40: Test lấy danh sách câu hỏi theo username không có câu hỏi nào
     * Mục tiêu: Kiểm tra phương thức findQuestionsByCreatedBy_Username trả về danh sách câu hỏi theo username không có câu hỏi nào
     * Input: Pageable, username không có câu hỏi nào
     * Output kỳ vọng: Page<Question> rỗng
     */
    @Test
    public void testFindQuestionsByCreatedBy_Username_NoResult() {
        // Username không tồn tại
        String nonExistUsername = "user_khong_ton_tai_" + System.currentTimeMillis();
        Pageable pageable = PageRequest.of(0, 10);
        Page<Question> page = questionService.findQuestionsByCreatedBy_Username(pageable, nonExistUsername);
        Assert.assertNotNull(page);
        Assert.assertTrue(page.getContent().isEmpty());
    }

    /**
     * TC_QS_41: Test lấy danh sách tất cả câu hỏi không có câu hỏi nào
     * Mục tiêu: Kiểm tra phương thức findAllQuestions trả về danh sách rỗng
     * Input: Pageable
     * Output kỳ vọng: Page<Question> rỗng
     */
    @Test
    @Transactional
    @Rollback
    public void testFindAllQuestions_NoQuestions() {
        // Xóa tất cả câu hỏi trong cơ sở dữ liệu
        questionRepository.deleteAll();

        // Tạo Pageable
        Pageable pageable = PageRequest.of(0, 5);

        // Gọi phương thức
        Page<Question> questionsPage = questionService.findAllQuestions(pageable);

        // Kiểm tra kết quả
        Assert.assertNotNull("Danh sách câu hỏi không được null!", questionsPage);
        Assert.assertTrue("Danh sách câu hỏi phải rỗng!", questionsPage.getContent().isEmpty());
    }

}
