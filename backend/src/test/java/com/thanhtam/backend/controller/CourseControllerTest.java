package com.thanhtam.backend.controller;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.thanhtam.backend.dto.PageResult;
import com.thanhtam.backend.dto.ServiceResult;
import com.thanhtam.backend.entity.Course;
import com.thanhtam.backend.entity.Part;
import com.thanhtam.backend.service.CourseService;
import com.thanhtam.backend.service.S3Services;
import org.junit.Assert;
import org.junit.Before;
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

import javax.persistence.EntityNotFoundException;
import java.io.IOException;
import java.util.List;
import java.util.Optional;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Transactional
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
public class CourseControllerTest {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private CourseService courseService;
    @Autowired
    private S3Services s3Services;

    private static String adminToken;

    private String getRootUrl() {
        return "http://localhost:" + port + "/api";
    }

    @Before
    public void setupToken() throws IOException {
        // Đăng nhập lấy token admin
        String payload = "{ \"username\": \"thanhtam28ss\", \"password\": \"Abcd@12345\" }";
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> req = new HttpEntity<>(payload, headers);

        ResponseEntity<String> resp = restTemplate.postForEntity(
                getRootUrl() + "/auth/signin",
                req,
                String.class);
        Assert.assertEquals(200, resp.getStatusCodeValue());
        adminToken = objectMapper.readTree(resp.getBody()).get("accessToken").asText();
    }

    @Test
    public void testGetAllCourse_Success() throws IOException {
        // Chuẩn bị dữ liệu
        Course c = new Course();
        c.setCourseCode("TST1");
        c.setName("Test Course");
        courseService.saveCourse(c);

        // Gọi API
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + adminToken);
        ResponseEntity<String> response = restTemplate.exchange(
                getRootUrl() + "/course-list",
                HttpMethod.GET,
                new HttpEntity<>(headers),
                String.class);

        Assert.assertEquals(200, response.getStatusCodeValue());
        // Parse về List<Course>
        List<Course> list = objectMapper.readValue(
                response.getBody(),
                objectMapper.getTypeFactory().constructCollectionType(List.class, Course.class));
        Assert.assertFalse("Danh sách không rỗng", list.isEmpty());
    }

    @Test
    public void testGetCourseListByPage_Success() throws IOException {
        // Thêm 3 course
        for (int i = 0; i < 3; i++) {
            Course c = new Course();
            c.setCourseCode("P" + i);
            c.setName("Page" + i);
            courseService.saveCourse(c);
        }

        // Gọi API với pageSize=2
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + adminToken);
        HttpEntity<?> entity = new HttpEntity<>(headers);
        ResponseEntity<String> response = restTemplate.exchange(
                getRootUrl() + "/courses?page=0&size=2",
                HttpMethod.GET,
                entity,
                String.class);

        Assert.assertEquals(200, response.getStatusCodeValue());
        PageResult pr = objectMapper.readValue(response.getBody(), PageResult.class);
        Page<?> page = objectMapper.convertValue(
                pr.getData(),
                objectMapper.getTypeFactory().constructParametricType(Page.class, Course.class));
        Assert.assertEquals(2, page.getSize());
        Assert.assertTrue(page.getTotalElements() >= 3);
    }

    @Test
    public void testCheckCourseCode_NewAndExisting() {
        Course c = new Course();
        c.setCourseCode("CHK1");
        c.setName("Check 1");
        courseService.saveCourse(c);

        // 1. Code tồn tại, nhưng id đúng -> trả về false
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + adminToken);
        ResponseEntity<Boolean> r1 = restTemplate.exchange(
                getRootUrl() + "/courses/" + c.getId() + "/check-course-code?value=CHK1",
                HttpMethod.GET,
                new HttpEntity<>(headers),
                Boolean.class);
        Assert.assertFalse(r1.getBody());

        // 2. Code tồn tại nhưng id khác -> true
        ResponseEntity<Boolean> r2 = restTemplate.exchange(
                getRootUrl() + "/courses/" + (c.getId() + 1) + "/check-course-code?value=CHK1",
                HttpMethod.GET,
                new HttpEntity<>(headers),
                Boolean.class);
        Assert.assertTrue(r2.getBody());

        // 3. Code mới -> false
        ResponseEntity<Boolean> r3 = restTemplate.exchange(
                getRootUrl() + "/courses/check-course-code?value=NEW",
                HttpMethod.GET,
                new HttpEntity<>(headers),
                Boolean.class);
        Assert.assertFalse(r3.getBody());
    }

//    @Test
//    public void testGetCourseById_SuccessAndNotFound() throws IOException {
//        Course c = new Course();
//        c.setCourseCode("GET1");
//        c.setName("Get Test");
//        Course saved = courseService.saveCourse(c);
//
//        // Found
//        HttpHeaders headers = new HttpHeaders();
//        headers.set("Authorization", "Bearer " + adminToken);
//        ResponseEntity<String> resp1 = restTemplate.exchange(
//                getRootUrl() + "/courses/" + saved.getId(),
//                HttpMethod.GET,
//                new HttpEntity<>(headers),
//                String.class);
//        Assert.assertEquals(200, resp1.getStatusCodeValue());
//        // Body chứa Optional<Course>
//        Assert.assertTrue(resp1.getBody().contains("\"courseCode\":\"GET1\""));
//
//        // Not found -> EntityNotFoundException trả về 500 bởi default handler
//        try {
//            restTemplate.exchange(
//                    getRootUrl() + "/courses/999999",
//                    HttpMethod.GET,
//                    new HttpEntity<>(headers),
//                    String.class);
//            Assert.fail("Phải ném EntityNotFoundException");
//        } catch (Exception ex) {
//            Assert.assertTrue(ex.getMessage().contains("Not found with course id: 999999"));
//        }
//    }

    @Test
    public void testCreateCourse_SuccessAndDuplicate() throws IOException {
        // Success
        Course c = new Course();
        c.setCourseCode("CR1");
        c.setName("Create");
        HttpEntity<Course> req = new HttpEntity<>(c, new HttpHeaders());
        ResponseEntity<String> r1 = restTemplate.postForEntity(
                getRootUrl() + "/courses", req, String.class);
        Assert.assertEquals(200, r1.getStatusCodeValue());
        ServiceResult sr1 = objectMapper.readValue(r1.getBody(), ServiceResult.class);
        Assert.assertEquals(201, sr1.getStatusCode());

        // Duplicate
        ResponseEntity<String> r2 = restTemplate.postForEntity(
                getRootUrl() + "/courses", req, String.class);
        Assert.assertEquals(400, r2.getStatusCodeValue());
        ServiceResult sr2 = objectMapper.readValue(r2.getBody(), ServiceResult.class);
        Assert.assertEquals(409, sr2.getStatusCode());
    }

//    @Test
//    public void testUpdateCourse_SuccessAndNotFound() throws IOException {
//        Course c = new Course();
//        c.setCourseCode("UP1");
//        c.setName("Before");
//        Course saved = courseService.saveCourse(c);
//
//        // Success
//        Course upd = new Course();
//        upd.setImgUrl(""); // giữ lại url cũ
//        upd.setName("After");
//        HttpEntity<Course> req = new HttpEntity<>(upd, new HttpHeaders());
//        ResponseEntity<String> resp = restTemplate.exchange(
//                getRootUrl() + "/courses/" + saved.getId(),
//                HttpMethod.PATCH,
//                req,
//                String.class);
//        Assert.assertEquals(200, resp.getStatusCodeValue());
//        ServiceResult sr = objectMapper.readValue(resp.getBody(), ServiceResult.class);
//        Assert.assertEquals("Update course with id: " + saved.getId(), sr.getMessage());
//
//        // Not found
//        try {
//            restTemplate.exchange(
//                    getRootUrl() + "/courses/999999",
//                    HttpMethod.PATCH,
//                    req,
//                    String.class);
//            Assert.fail();
//        } catch (Exception ex) {
//            Assert.assertTrue(ex.getMessage().contains("Not found with course id: 999999"));
//        }
//    }

//    @Test
//    public void testDeleteCourse_SuccessAndNotFound() {
//        Course c = new Course();
//        c.setCourseCode("DL1");
//        c.setName("Del");
//        Course saved = courseService.saveCourse(c);
//
//        // Success
//        ResponseEntity<String> r1 = restTemplate.exchange(
//                getRootUrl() + "/courses/" + saved.getId(),
//                HttpMethod.DELETE,
//                null,
//                String.class);
//        Assert.assertEquals(200, r1.getStatusCodeValue());
//
//        // Not found
//        try {
//            restTemplate.exchange(
//                    getRootUrl() + "/courses/999999",
//                    HttpMethod.DELETE,
//                    null,
//                    String.class);
//            Assert.fail();
//        } catch (Exception ex) {
//            Assert.assertTrue(ex.getMessage().contains("Not found with course id:999999"));
//        }
//    }

//    @Test
//    public void testGetCourseByPartAndFindAllByIntakeId() throws IOException {
//        // Tạo Course và Part
//        Course c = new Course();
//        c.setCourseCode("P1"); c.setName("PartTest");
//        Course saved = courseService.saveCourse(c);
//
//        Part p = new Part();
//        p.setTitle("PT"); p.setCourse(saved);
//        // lưu vào DB
//        // dùng partService hoặc repository trực tiếp
//        // ...
//
//        // Giả sử partService đã lưu: id = pid
//        Long pid = p.getId();
//
//        // getCourseByPart
//        ResponseEntity<String> r1 = restTemplate.exchange(
//                getRootUrl() + "/courses/part/" + pid,
//                HttpMethod.GET,
//                null,
//                String.class);
//        Assert.assertEquals(200, r1.getStatusCodeValue());
//        Assert.assertTrue(r1.getBody().contains("\"courseCode\":\"P1\""));
//
//        // findAllByIntakeId (giả lập intakeId = 1L, chưa có dữ liệu -> trả về rỗng)
//        ResponseEntity<String> r2 = restTemplate.exchange(
//                getRootUrl() + "/intakes/1/courses",
//                HttpMethod.GET,
//                null,
//                String.class);
//        Assert.assertEquals(200, r2.getStatusCodeValue());
//        List<Course> list = objectMapper.readValue(
//                r2.getBody(),
//                objectMapper.getTypeFactory().constructCollectionType(List.class, Course.class));
//        Assert.assertTrue(list.isEmpty());
//    }
}
