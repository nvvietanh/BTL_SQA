package com.thanhtam.backend.controller;

import com.thanhtam.backend.entity.QuestionType;
import com.thanhtam.backend.service.QuestionTypeService;
import com.thanhtam.backend.ultilities.EQTypeCode;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@RunWith(SpringRunner.class)
@SpringBootTest
@Transactional
@Rollback
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
public class QuestionTypeControllerTest {

    @Autowired
    private QuestionTypeService questionTypeService;

    private QuestionTypeController questionTypeController;

    @Before
    public void setUp() {
        questionTypeController = new QuestionTypeController(questionTypeService);
    }

    /**
     * TC_QTC_1: Test lấy danh sách tất cả các loại câu hỏi thành công
     * Mục tiêu: Kiểm tra xem phương thức getAllQuestionType() có trả về danh sách các loại câu hỏi hay không
     * Input: 
     * Output kỳ vọng: Danh sách các loại câu hỏi không null
     */
    @Test
    public void testGetAllQuestionType_Success() {
        List<QuestionType> types = questionTypeController.getAllQuestionType();
        Assert.assertNotNull(types);
        // Loop coverage: kiểm tra từng phần tử
        for (QuestionType type : types) {
            Assert.assertNotNull(type.getId());
            Assert.assertNotNull(type.getTypeCode());
        }
    }

    /**
     * TC_QTC_2: Test lấy loại câu hỏi theo ID thành công
     * Mục tiêu: Kiểm tra xem phương thức getQuestionTypeById() có trả về loại câu hỏi đúng hay không
     * Input: ID của loại câu hỏi
     * Output kỳ vọng: Loại câu hỏi không null và ID trùng khớp
     */
    @Test
    public void testGetQuestionTypeById_Success() {
        List<QuestionType> types = questionTypeService.getQuestionTypeList();
        Assert.assertFalse(types.isEmpty());
        Long validId = types.get(0).getId();

        QuestionType type = questionTypeController.getQuestionTypeById(validId);
        Assert.assertNotNull(type);
        Assert.assertEquals(validId, type.getId());
    }

    /**
     * TC_QTC_3: Test lấy loại câu hỏi theo ID không tồn tại
     * Mục tiêu: Kiểm tra xem phương thức getQuestionTypeById() có ném ra ngoại lệ khi ID không tồn tại hay không
     * Input: ID không tồn tại
     * Output kỳ vọng: Ném ra NoSuchElementException
     */
    @Test(expected = java.util.NoSuchElementException.class)
    public void testGetQuestionTypeById_NotFound() {
        Long invalidId = -999L;
        try {
          questionTypeController.getQuestionTypeById(invalidId);
          Assert.fail("Phải ném ra Expected NoSuchElementException");
        } catch (java.util.NoSuchElementException e) {
            
        }
    }

    /**
     * TC_QTC_4: Test lấy loại câu hỏi theo mã loại câu hỏi thành công
     * Mục tiêu: Kiểm tra xem phương thức getQuestionTypeByTypeCode() có trả về loại câu hỏi đúng hay không
     * Input: Mã loại câu hỏi
     * Output kỳ vọng: Loại câu hỏi không null và mã loại câu hỏi trùng khớp
     */
    @Test
    public void testGetQuestionTypeByTypeCode_Success() {
        List<QuestionType> types = questionTypeService.getQuestionTypeList();
        Assert.assertFalse(types.isEmpty());
        EQTypeCode validCode = types.get(0).getTypeCode();

        QuestionType type = questionTypeController.getQuestionTypeByTypeCode(validCode.name());
        Assert.assertNotNull(type);
        Assert.assertEquals(validCode, type.getTypeCode());
    }

    /**
     * TC_QTC_5: Test lấy loại câu hỏi theo mã loại câu hỏi không tồn tại
     * Mục tiêu: Kiểm tra xem phương thức getQuestionTypeByTypeCode() có ném ra ngoại lệ khi mã loại câu hỏi không tồn tại hay không
     * Input: Mã loại câu hỏi không tồn tại
     * Output kỳ vọng: Ném ra IllegalArgumentException
     */
    @Test(expected = IllegalArgumentException.class)
    public void testGetQuestionTypeByTypeCode_InvalidCode() {
        try {
            questionTypeController.getQuestionTypeByTypeCode("INVALID_CODE");
            Assert.fail("Phải ném ra Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
        
        }
    }
    
}