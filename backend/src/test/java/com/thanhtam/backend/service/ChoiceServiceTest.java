package com.thanhtam.backend.service;

import com.thanhtam.backend.entity.Choice;
import com.thanhtam.backend.repository.ChoiceRepository;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@RunWith(SpringRunner.class)
@SpringBootTest
@Transactional
@Rollback
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
public class ChoiceServiceTest {

    @Autowired
    private ChoiceService choiceService;

    @Autowired
    private ChoiceRepository choiceRepository;

    @Test
    public void testFindIsCorrectedById_Exist_Correct() {
        // Lấy một choice đúng từ DB
        List<Choice> choices = choiceRepository.findAll();
        Assert.assertFalse(choices.isEmpty());
        Choice correctChoice = choices.stream().filter(c -> c.getIsCorrected() == 1).findFirst().orElse(null);
        Assert.assertNotNull("Không có choice đúng trong DB!", correctChoice);

        Integer isCorrected = choiceService.findIsCorrectedById(correctChoice.getId());
        Assert.assertEquals(Integer.valueOf(1), isCorrected);
    }

    @Test
    public void testFindIsCorrectedById_Exist_Incorrect() {
        // Lấy một choice sai từ DB
        List<Choice> choices = choiceRepository.findAll();
        Assert.assertFalse(choices.isEmpty());
        Choice incorrectChoice = choices.stream().filter(c -> c.getIsCorrected() == 0).findFirst().orElse(null);
        Assert.assertNotNull("Không có choice sai trong DB!", incorrectChoice);

        Integer isCorrected = choiceService.findIsCorrectedById(incorrectChoice.getId());
        Assert.assertEquals(Integer.valueOf(0), isCorrected);
    }

    @Test
    public void testFindIsCorrectedById_NotExist() {
        // ID không tồn tại
        Long invalidId = -999L;
        Integer isCorrected = choiceService.findIsCorrectedById(invalidId);
        Assert.assertNull(isCorrected);
    }

    @Test
    public void testFindChoiceTextById_Exist() {
        // Lấy một choice bất kỳ từ DB
        List<Choice> choices = choiceRepository.findAll();
        Assert.assertFalse(choices.isEmpty());
        Choice choice = choices.get(0);

        String text = choiceService.findChoiceTextById(choice.getId());
        Assert.assertEquals(choice.getChoiceText(), text);
    }

    @Test
    public void testFindChoiceTextById_NotExist() {
        // ID không tồn tại
        Long invalidId = -999L;
        String text = choiceService.findChoiceTextById(invalidId);
        Assert.assertNull(text);
    }

    @Test
    public void testFindIsCorrectedById_Loop_AllChoices() {
        List<Choice> choices = choiceRepository.findAll();
        for (Choice c : choices) {
            int isCorrected = choiceService.findIsCorrectedById(c.getId());
            Assert.assertEquals(c.getIsCorrected(), isCorrected);
        }
    }

    @Test
    public void testFindChoiceTextById_Loop_AllChoices() {
        List<Choice> choices = choiceRepository.findAll();
        for (Choice c : choices) {
            String text = choiceService.findChoiceTextById(c.getId());
            Assert.assertEquals(c.getChoiceText(), text);
        }
    }
}