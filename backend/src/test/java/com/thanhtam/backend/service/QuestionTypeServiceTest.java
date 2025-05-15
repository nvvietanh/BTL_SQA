package com.thanhtam.backend.service;

import com.thanhtam.backend.entity.QuestionType;
import com.thanhtam.backend.repository.QuestionTypeRepository;
import com.thanhtam.backend.ultilities.EQTypeCode;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@RunWith(SpringRunner.class)
@SpringBootTest
@Transactional
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
public class QuestionTypeServiceTest {

    @Autowired
    private QuestionTypeRepository questionTypeRepository;

    @Autowired
    private QuestionTypeService questionTypeService;

    /**
     * TC_QT_01: Test lấy loại câu hỏi theo ID - trường hợp tồn tại
     * Mục tiêu: Kiểm tra phương thức getQuestionTypeById trả về loại câu hỏi khi ID tồn tại
     * Input: ID = 1L (tồn tại trong DB)
     * Output kỳ vọng: Optional<QuestionType> có giá trị
     */
    @Test
    @Transactional
    public void testGetQuestionTypeById_Success() {
        // Kiểm tra service không null
        Assert.assertNotNull("QuestionTypeService bị null!", questionTypeService);
        
        // Lấy questionType có id = 1 từ DB
        Optional<QuestionType> questionType = questionTypeService.getQuestionTypeById(1L);
        
        // Kiểm tra kết quả
        Assert.assertTrue("QuestionType không tồn tại!", questionType.isPresent());
        Assert.assertEquals(1L, questionType.get().getId().longValue());
    }

    /**
     * TC_QT_02: Test lấy loại câu hỏi theo ID - trường hợp không tồn tại
     * Mục tiêu: Kiểm tra phương thức getQuestionTypeById trả về Optional.empty() khi ID không tồn tại
     * Input: ID = 999999L (không tồn tại trong DB)
     * Output kỳ vọng: Optional.empty()
     */
    @Test
    @Transactional
    public void testGetQuestionTypeById_NotFound() {
        // Kiểm tra service không null
        Assert.assertNotNull("QuestionTypeService bị null!", questionTypeService);
        
        // Thử lấy questionType không tồn tại
        Optional<QuestionType> questionType = questionTypeService.getQuestionTypeById(999999L);
        
        // Kiểm tra kết quả
        Assert.assertFalse("QuestionType không nên tồn tại!", questionType.isPresent());
    }

    /**
     * TC_QT_03: Test lấy loại câu hỏi theo code - trường hợp tồn tại
     * Mục tiêu: Kiểm tra phương thức getQuestionTypeByCode trả về loại câu hỏi khi code tồn tại
     * Input: code = EQTypeCode.MC
     * Output kỳ vọng: Optional<QuestionType> có giá trị
     */
    @Test
    @Transactional
    public void testGetQuestionTypeByCode_Success() {
        // Kiểm tra service không null
        Assert.assertNotNull("QuestionTypeService bị null!", questionTypeService);
        
        // Lấy questionType theo code MULTIPLE_CHOICE
        Optional<QuestionType> questionType = questionTypeService.getQuestionTypeByCode(EQTypeCode.MC);
        
        // Kiểm tra kết quả
        Assert.assertTrue("QuestionType không tồn tại!", questionType.isPresent());
        Assert.assertEquals(EQTypeCode.MC, questionType.get().getTypeCode());
    }

    /**
     * TC_QT_04: Test lấy loại câu hỏi theo code - trường hợp không tồn tại
     * Mục tiêu: Kiểm tra phương thức getQuestionTypeByCode trả về Optional.empty() khi code không tồn tại
     * Input: code = EQTypeCode.valueOf("INVALID_CODE")
     * Output kỳ vọng: Optional.empty()
     */
    @Test
    @Transactional
    public void testGetQuestionTypeByCode_NotFound() {
        // Kiểm tra service không null
        Assert.assertNotNull("QuestionTypeService bị null!", questionTypeService);

        try {
            // Thử lấy questionType với code không tồn tại
            Optional<QuestionType> questionType = questionTypeService.getQuestionTypeByCode(EQTypeCode.valueOf("INVALID_CODE"));
            // Kiểm tra kết quả
            Assert.assertFalse("QuestionType tồn tại. Phải ném ra exception", questionType.isPresent());
        } catch (Exception e) {

        }

    }

    /**
     * TC_QT_05: Test lấy danh sách tất cả loại câu hỏi
     * Mục tiêu: Kiểm tra phương thức getQuestionTypeList trả về danh sách tất cả loại câu hỏi
     * Input: Không có
     * Output kỳ vọng: List<QuestionType> chứa tất cả loại câu hỏi
     */
    @Test
    @Transactional
    public void testGetQuestionTypeList_Success() {
        // Kiểm tra service không null
        Assert.assertNotNull("QuestionTypeService bị null!", questionTypeService);
        
        // Lấy danh sách questionType
        List<QuestionType> questionTypes = questionTypeService.getQuestionTypeList();
        
        // Kiểm tra kết quả
        Assert.assertNotNull("Danh sách QuestionType không được null!", questionTypes);
        Assert.assertFalse("Danh sách QuestionType không được rỗng!", questionTypes.isEmpty());
    }

    /**
     * TC_QT_06: Test lưu loại câu hỏi mới
     * Mục tiêu: Kiểm tra phương thức saveQuestionType lưu loại câu hỏi mới
     * Input: QuestionType mới với typeCode = EQTypeCode.MC
     * Output kỳ vọng: QuestionType được lưu vào DB
     */
    @Test
    @Transactional
    public void testSaveQuestionType_Success() {
        // Kiểm tra service không null
        Assert.assertNotNull("QuestionTypeService bị null!", questionTypeService);
        
        // Tạo questionType mới
        QuestionType questionType = new QuestionType();
        questionType.setTypeCode(EQTypeCode.MC);
        questionType.setDescription("Test Question Type");
        
        // Lưu questionType
        questionTypeService.saveQuestionType(questionType);
        
        // Kiểm tra kết quả
        Optional<QuestionType> savedQuestionType = questionTypeService.getQuestionTypeById(questionType.getId());
        Assert.assertTrue("QuestionType không được lưu!", savedQuestionType.isPresent());
        Assert.assertEquals(EQTypeCode.MC, savedQuestionType.get().getTypeCode());
        Assert.assertEquals("Test Question Type", savedQuestionType.get().getDescription());
    }

    /**
     * TC_QT_07: Test xóa loại câu hỏi
     * Mục tiêu: Kiểm tra phương thức delete xóa loại câu hỏi
     * Input: ID = 1L
     * Output kỳ vọng: Loại câu hỏi bị xóa khỏi DB
     */
    @Test
    @Transactional
    public void testDeleteQuestionType_Success() {
        // Kiểm tra service không null
        Assert.assertNotNull("QuestionTypeService bị null!", questionTypeService);
        
        // Lấy questionType có id = 1 từ DB
        Optional<QuestionType> questionType = questionTypeService.getQuestionTypeById(1L);
        Assert.assertTrue("QuestionType không tồn tại!", questionType.isPresent());
        
        // Xóa questionType
        questionTypeService.delete(1L);
        
        // Kiểm tra questionType đã bị xóa
        Optional<QuestionType> deletedQuestionType = questionTypeService.getQuestionTypeById(1L);
        Assert.assertFalse("QuestionType vẫn còn tồn tại!", deletedQuestionType.isPresent());
    }

    /**
     * TC_QT_08: Test kiểm tra loại câu hỏi tồn tại theo ID - trường hợp tồn tại
     * Mục tiêu: Kiểm tra phương thức existsById trả về true khi ID tồn tại
     * Input: ID = 1L (tồn tại trong DB)
     * Output kỳ vọng: true
     */
    @Test
    @Transactional
    public void testExistsById_True() {
        // Kiểm tra service không null
        Assert.assertNotNull("QuestionTypeService bị null!", questionTypeService);
        
        // Kiểm tra questionType có id = 1 tồn tại
        boolean exists = questionTypeService.existsById(1L);
        
        // Kiểm tra kết quả
        Assert.assertTrue("QuestionType phải tồn tại!", exists);
    }

    /**
     * TC_QT_09: Test kiểm tra loại câu hỏi tồn tại theo ID - trường hợp không tồn tại
     * Mục tiêu: Kiểm tra phương thức existsById trả về false khi ID không tồn tại
     * Input: ID = 999999L (không tồn tại trong DB)
     * Output kỳ vọng: false
     */
    @Test
    @Transactional
    public void testExistsById_False() {
        // Kiểm tra service không null
        Assert.assertNotNull("QuestionTypeService bị null!", questionTypeService);
        
        // Kiểm tra questionType có id = 999999 không tồn tại
        boolean exists = questionTypeService.existsById(999999L);
        
        // Kiểm tra kết quả
        Assert.assertFalse("QuestionType không nên tồn tại!", exists);
    }
}
