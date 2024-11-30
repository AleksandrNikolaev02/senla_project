package com.example.demo.service;

import com.example.demo.dto.UpdateCourseDTO;
import com.example.demo.exception.NoRightsException;
import com.example.demo.exception.NotFoundException;
import com.example.demo.interfaces.CourseServiceAPI;
import com.example.demo.model.Course;
import com.example.demo.model.User;
import com.example.demo.repository.CourseRepository;
import com.example.demo.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.Hibernate;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@Slf4j
@AllArgsConstructor
public class CourseService implements CourseServiceAPI {
    private final CourseRepository courseRepository;
    private final UserRepository userRepository;
    private final FileService fileService;

    @Cacheable(value = "courses")
    public List<Course> findAllCourses() {
        return courseRepository.findAll();
    }

    @CacheEvict(value = "courses", allEntries = true)
    public void createCourse(Course course, Integer user_id) {
        User course_creator = userRepository.findById(user_id)
                .orElseThrow(()-> new NotFoundException("User not found!"));

        course.setTeacher(course_creator);

        courseRepository.save(course);

        log.info("Процесс: создание курса. Курс {} успешно создан!", course);
    }

    @Transactional
    @CacheEvict(value = {"courses", "answers", "artifacts"}, allEntries = true)
    public void deleteCourse(Integer course_id, Integer user_id) {
        Course course = courseRepository.findById(course_id)
                .orElseThrow(() -> new NotFoundException("Course not found!"));

        if (!course.getTeacher().getId().equals(user_id)) {
            throw new NoRightsException("You are not the creator of the course!");
        }

        deleteCourseDirectories(course);

        courseRepository.delete(course);

        log.info("Процесс: удаление курса. Курс {} успешно удален!", course);
    }

    @CacheEvict(value = "courses", allEntries = true)
    public void updateCourse(UpdateCourseDTO dto, Integer user_id) {
        Course course = courseRepository.findById(dto.getCourse_id())
                .orElseThrow(() -> new NotFoundException("Course not found!"));

        if (!course.getTeacher().getId().equals(user_id)) {
            throw new NoRightsException("You are not the creator of the course!");
        }

        if (dto.getTitle() != null) {
            course.setTitle(dto.getTitle());
        }
        if (dto.getDescription() != null) {
            course.setDescription(dto.getDescription());
        }

        courseRepository.save(course);

        log.info("Процесс: обновление курса. Курс {} успешно обновлен!", course);
    }

    public Page<Course> getCourses(String title, String teacherName, Pageable pageable) {
        List<Course> courses = findAllCourses();
        Stream<Course> filteredStream = courses.stream();

        if (title != null) {
            filteredStream = filteredStream.filter(course ->
                    course.getTitle().toLowerCase().contains(title.toLowerCase()));
        }
        if (teacherName != null) {
            filteredStream = filteredStream.filter(course ->
                    course.getTeacher().getFullName().toLowerCase().contains(teacherName.toLowerCase()));
        }

        List<Course> filteredCourses = filteredStream.collect(Collectors.toList());
        return new PageImpl<>(filteredCourses, pageable, filteredCourses.size());
    }

    public void registerUserToCourse(Integer user_id, Integer course_id) {
        Course course = courseRepository.findById(course_id)
                .orElseThrow(() -> new NotFoundException("The course does not exist!"));
        User user = userRepository.findById(user_id)
                .orElseThrow(() -> new NotFoundException("The user does not exist!"));

        Hibernate.initialize(course);

        course.getStudents().add(user);
        courseRepository.save(course);

        log.info("Процесс: регистрация пользователя на курс. Пользователь добавлен на курс {}", course);
    }

    private void deleteCourseDirectories(Course course) {
        fileService.deleteDirectory(course.getId().toString());

        log.info("Процесс: удаление директории курса. Директория успешно удалена!");
    }
}
