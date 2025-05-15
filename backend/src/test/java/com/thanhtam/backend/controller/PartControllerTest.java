package com.thanhtam.backend.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.thanhtam.backend.dto.PageResult;
import com.thanhtam.backend.entity.Course;
import com.thanhtam.backend.entity.Part;
import com.thanhtam.backend.service.CourseService;
import com.thanhtam.backend.service.PartService;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.*;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Transactional
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
public class PartControllerTest {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private PartService partService;

    @Autowired
    private CourseService courseService;

    private String getRootUrl() {
        return "http://localhost:" + port + "/api";
    }

    private String getToken() {
        return "eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJ0aGFuaHRhbTI4c3MiLCJpYXQiOjE3NDQyOTQ4MTIsImV4cCI6MTc0NDM4MTIxMiwicm9sZSI6W3siYXV0aG9yaXR5IjoiUk9MRV9BRE1JTiJ9XX0.dlO69r0W_COp4s0Y1zZ1e4cGUE-56KPuAW5WeebauMCzT0Q8Ct3tXw_flMBuSQuqIRQ4tcy_JNju-wjL5X5F0w";
    }


    // TC_PC_01: Lấy danh sách phần học phân trang theo courseId (thành công)
    @Test
    public void testGetPartListByCourse_Paged_Success() throws Exception {
        // Giả sử đã có course id = 1
        Optional<Course> courseOpt = courseService.getCourseById(3L);
        Assert.assertTrue("Course không tồn tại!", courseOpt.isPresent());

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + getToken());
        HttpEntity<String> entity = new HttpEntity<>(headers);

        ResponseEntity<String> response = restTemplate.exchange(
                getRootUrl() + "/courses/3/parts?page=0&size=10",
                HttpMethod.GET,
                entity,
                String.class
        );

        Assert.assertEquals(200, response.getStatusCodeValue());
        PageResult pageResult = objectMapper.readValue(response.getBody(), PageResult.class);
        Assert.assertNotNull(pageResult.getData());
    }

    // TC_PC_02: Lấy danh sách phần học không phân trang theo courseId (thành công)
    @Test
    public void testGetPartListByCourse_Success() throws Exception {
        Optional<Course> courseOpt = courseService.getCourseById(1L);
        Assert.assertTrue("Course không tồn tại!", courseOpt.isPresent());

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + getToken());
        HttpEntity<String> entity = new HttpEntity<>(headers);

        ResponseEntity<String> response = restTemplate.exchange(
                getRootUrl() + "/courses/3/part-list",
                HttpMethod.GET,
                entity,
                String.class
        );

        Assert.assertEquals(200, response.getStatusCodeValue());
        List<?> parts = objectMapper.readValue(response.getBody(), List.class);
        Assert.assertNotNull(parts);
    }

    // TC_PC_03: Lấy part theo id (thành công)
    @Test
    public void testGetPartById_Success() throws Exception {
        Optional<Part> partOpt = partService.findPartById(1L);
        Assert.assertTrue("Part không tồn tại!", partOpt.isPresent());

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + getToken());
        HttpEntity<String> entity = new HttpEntity<>(headers);

        ResponseEntity<String> response = restTemplate.exchange(
                getRootUrl() + "/parts/1",
                HttpMethod.GET,
                entity,
                String.class
        );

        Assert.assertEquals(200, response.getStatusCodeValue());
        Assert.assertTrue(response.getBody().contains("\"id\":1"));
    }

    // TC_PC_04: Lấy part theo id không tồn tại (thất bại)
    @Test(expected = Exception.class)
    public void testGetPartById_NotFound() throws Exception {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + getToken());
        HttpEntity<String> entity = new HttpEntity<>(headers);

        restTemplate.exchange(
                getRootUrl() + "/parts/999999",
                HttpMethod.GET,
                entity,
                String.class
        );
    }

    // TC_PC_05: Cập nhật tên part (thành công)
    @Test
    public void testUpdatePartName_Success() throws Exception {
        Optional<Part> partOpt = partService.findPartById(1L);
        Assert.assertTrue("Part không tồn tại!", partOpt.isPresent());

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + getToken());
        headers.setContentType(MediaType.APPLICATION_JSON);

        String newName = "\"Updated Part Name\"";
        HttpEntity<String> entity = new HttpEntity<>(newName, headers);

        ResponseEntity<String> response = restTemplate.exchange(
                getRootUrl() + "/parts/1",
                HttpMethod.PATCH,
                entity,
                String.class
        );

        Assert.assertEquals(200, response.getStatusCodeValue());
        Assert.assertTrue(response.getBody().contains("Updated Part Name"));
    }

    // TC_PC_06: Tạo mới part cho course (thành công)
    @Test
    public void testCreatePartByCourse_Success() throws Exception {
        Optional<Course> courseOpt = courseService.getCourseById(1L);
        Assert.assertTrue("Course không tồn tại!", courseOpt.isPresent());

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + getToken());
        headers.setContentType(MediaType.APPLICATION_JSON);

        Part part = new Part();
        part.setName("New Part For Course");

        String body = objectMapper.writeValueAsString(part);
        HttpEntity<String> entity = new HttpEntity<>(body, headers);

        ResponseEntity<String> response = restTemplate.exchange(
                getRootUrl() + "/courses/1/parts",
                HttpMethod.POST,
                entity,
                String.class
        );

        // API không trả về gì nên chỉ kiểm tra status
        Assert.assertEquals(200, response.getStatusCodeValue());
    }

    // TC_PC_07: Lấy danh sách phần học với courseId không tồn tại (trả về empty)
    @Test
    public void testGetPartListByCourse_NonExistCourse() throws Exception {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + getToken());
        HttpEntity<String> entity = new HttpEntity<>(headers);

        ResponseEntity<String> response = restTemplate.exchange(
                getRootUrl() + "/courses/999999/part-list",
                HttpMethod.GET,
                entity,
                String.class
        );

        Assert.assertEquals(500, response.getStatusCodeValue()); // do service get().get() sẽ throw exception
    }

    // TC_PC_08: Cập nhật part với id không tồn tại (thất bại)
    @Test(expected = Exception.class)
    public void testUpdatePartName_NotFound() throws Exception {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + getToken());
        headers.setContentType(MediaType.APPLICATION_JSON);

        String newName = "\"Updated Part Name\"";
        HttpEntity<String> entity = new HttpEntity<>(newName, headers);

        restTemplate.exchange(
                getRootUrl() + "/parts/999999",
                HttpMethod.PATCH,
                entity,
                String.class
        );
    }
}
