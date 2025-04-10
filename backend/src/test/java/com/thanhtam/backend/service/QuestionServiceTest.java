package com.thanhtam.backend.service;

import com.thanhtam.backend.entity.Choice;
import com.thanhtam.backend.entity.Part;
import com.thanhtam.backend.entity.Question;
import com.thanhtam.backend.entity.QuestionType;
import com.thanhtam.backend.repository.QuestionRepository;
import com.thanhtam.backend.ultilities.DifficultyLevel;

import com.thanhtam.backend.ultilities.EQTypeCode;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest
@Transactional // Rollback sau mỗi test case
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
public class QuestionServiceTest {

    @Autowired
    private QuestionService questionService;

    @Autowired
    private QuestionTypeService questionTypeService;

    @Autowired PartService partService;

    @Autowired
    private QuestionRepository questionRepository;

    @Test
    public void testSaveQuestion_EasyLevel() {
        Question question = new Question();
        question.setQuestionText("What is Java?");
        QuestionType questionType1 = questionTypeService
                .getQuestionTypeByCode(EQTypeCode.MC).get();

        Choice choice1 = new Choice(null, "OOP Programming", 1);
        Choice choice2 = new Choice(null, "Functional Programming", 0);
        List<Choice> choices = Arrays.asList(choice1, choice2);
        Part part = partService.findPartById(1L).get();

        question.setChoices(choices);
        question.setQuestionType(questionType1);
        question.setPart(part);
        question.setDeleted(false);
        questionService.save(question);

        question.setDifficultyLevel(DifficultyLevel.EASY);
        question.setPoint(0); // Ban đầu chưa set điểm

        questionService.save(question);

        Question savedQuestion = questionRepository
                .findById(question.getId()).orElse(null);

        assertNotNull(savedQuestion);
        assertEquals(5, savedQuestion.getPoint()); // EASY -> 5 điểm
    }

    @Test
    public void testSaveQuestion_MediumLevel() {
        Question question = new Question();
        question.setQuestionText("Explain OOP concepts.");
        question.setDifficultyLevel(DifficultyLevel.MEDIUM);

        questionService.save(question);

        Question savedQuestion = questionRepository.findById(question.getId()).orElse(null);

        assertNotNull(savedQuestion);
        assertEquals(10, savedQuestion.getPoint()); // MEDIUM -> 10 điểm
    }

    @Test
    public void testSaveQuestion_HardLevel() {
        Question question = new Question();
        question.setQuestionText("Describe the Spring Boot lifecycle.");
        question.setDifficultyLevel(DifficultyLevel.HARD);

        questionService.save(question);

        Question savedQuestion = questionRepository.findById(question.getId()).orElse(null);

        assertNotNull(savedQuestion);
        assertEquals(15, savedQuestion.getPoint()); // HARD -> 15 điểm
    }

    @Test
    public void testSaveQuestion_UnknownDifficulty_DefaultPoint() {
        Question question = new Question();
        question.setQuestionText("Unknown difficulty test case.");
        question.setDifficultyLevel(null); // Không có difficulty level

        questionService.save(question);

        Question savedQuestion = questionRepository.findById(question.getId()).orElse(null);

        assertNotNull(savedQuestion);
        assertEquals(0, savedQuestion.getPoint()); // Mặc định -> 0 điểm
    }

    @Test
    public void testSaveQuestion_WithChoices() {
        Question question = new Question();
        question.setQuestionText("Which are Java features?");
        question.setDifficultyLevel(DifficultyLevel.MEDIUM);

        Choice choice1 = new Choice(null, "OOP", 1);
        Choice choice2 = new Choice(null, "Functional Programming", 0);
        List<Choice> choices = Arrays.asList(choice1, choice2);
        question.setChoices(choices);

        questionService.save(question);

        Question savedQuestion = questionRepository.findById(question.getId()).orElse(null);

        assertNotNull(savedQuestion);
        assertEquals(2, savedQuestion.getChoices().size()); // Kiểm tra số lượng đáp án
        assertEquals(10, savedQuestion.getPoint()); // MEDIUM -> 10 điểm
    }
}
