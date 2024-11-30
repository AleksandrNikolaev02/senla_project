package com.example.demo.contoller;

import com.example.demo.dto.CourseDTO;
import com.example.demo.dto.CourseRegisterDTO;
import com.example.demo.dto.CreateCourseDTO;
import com.example.demo.dto.DeleteCourseDTO;
import com.example.demo.dto.UpdateCourseDTO;
import com.example.demo.mapper.CourseMapper;
import com.example.demo.service.CourseService;
import com.example.demo.util.Util;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.PutMapping;

@RestController
@Slf4j
@AllArgsConstructor
@RequestMapping("/course")
public class CourseController {
    private final CourseService courseService;
    private final CourseMapper mapper;
    private final Util util;

    @PostMapping("/create")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'TEACHER')")
    @Operation(summary = "Создать курс")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Course successfully created!"),
            @ApiResponse(responseCode = "404", description = "User not found!")})
    public ResponseEntity<String> createCourse(@Validated @RequestBody CreateCourseDTO dto,
                                               @RequestHeader(name="Authorization") String token) {
        Integer user_id = util.getIdFromToken(token);
        courseService.createCourse(mapper.courseDtoToCourse(dto), user_id);
        return ResponseEntity.status(201).body("Course successfully created!");
    }

    @DeleteMapping("/delete")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'TEACHER')")
    @Operation(summary = "Удалить курс")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Course successfully deleted!"),
            @ApiResponse(responseCode = "404", description = "Course not found!"),
            @ApiResponse(responseCode = "403", description = "You are not the creator of the course!")})
    public ResponseEntity<String> deleteCourse(@Validated @RequestBody DeleteCourseDTO dto,
                                               @RequestHeader(name="Authorization") String token) {
        Integer user_id = util.getIdFromToken(token);
        courseService.deleteCourse(dto.getCourse_id(), user_id);
        return ResponseEntity.ok("Course successfully deleted!");
    }

    @PutMapping("/update")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'TEACHER')")
    @Operation(summary = "Обновить курс")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Course update is successful!"),
            @ApiResponse(responseCode = "404", description = "Course not found!"),
            @ApiResponse(responseCode = "403", description = "You are not the creator of the course!")})
    public ResponseEntity<String> updateCourse(@Validated @RequestBody UpdateCourseDTO dto,
                                               @RequestHeader(name="Authorization") String token) {
        Integer user_id = util.getIdFromToken(token);
        courseService.updateCourse(dto, user_id);
        return ResponseEntity.ok("Course update is successful!");
    }

    @GetMapping("/all")
    @Operation(summary = "Получить список всех курсов")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200",
                         content = @Content(mediaType = "application/json",
                         array = @ArraySchema(schema = @Schema(implementation = CourseDTO.class)))) })
    public Page<CourseDTO> getListAllCourses(@RequestParam(name = "title", required = false) String title,
                                             @RequestParam(name = "teacherName", required = false) String teacherName,
                                             @PageableDefault Pageable pageable) {
        return courseService.getCourses(title, teacherName, pageable).map(mapper::courseToCourseDTO);
    }

    @PostMapping("/register")
    @PreAuthorize("hasAuthority('STUDENT')")
    @Operation(summary = "Зарегистрироваться на курс")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Пользователь с id = <id> добавлен на курс"),
            @ApiResponse(responseCode = "404", description = "Такого курса не существует!"),
            @ApiResponse(responseCode = "404", description = "Такого пользователя не существует!")})
    public ResponseEntity<String> registerUserToCourse(@Validated @RequestBody CourseRegisterDTO dto,
                                                       @RequestHeader(name="Authorization") String token) {
        Integer user_id = util.getIdFromToken(token);
        courseService.registerUserToCourse(user_id, dto.getCourseId());
        return ResponseEntity.status(201).body(String.format("User with id = %s added to course", user_id));
    }
}
