package com.example.demo.service;

import com.example.demo.annoration.Loggable;
import com.example.demo.annoration.Timed;
import com.example.demo.config.TopicConfig;
import com.example.demo.dto.UpdateCourseDTO;
import com.example.demo.exception.NoRightsException;
import com.example.demo.interfaces.CourseServiceAPI;
import com.example.demo.model.Course;
import com.example.demo.model.User;
import com.example.demo.repository.CourseRepository;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.hibernate.Hibernate;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@AllArgsConstructor
public class CourseService implements CourseServiceAPI {
    private final CourseRepository courseRepository;
    private final KafkaTemplate<String, String> kafkaTemplate;
    private TopicConfig config;
    private EntityFindService entityFindService;

    @Cacheable(value = "courses")
    @Timed
    public Page<Course> findAllCourses(Pageable pageable) {
        return courseRepository.findAll(pageable);
    }

    @CacheEvict(value = "courses", allEntries = true)
    @Loggable(process = "создание курса")
    public void createCourse(Course course, Integer userId) {
        User courseCreator = entityFindService.findUserById(userId);

        saveCourseRelativeToCreator(course, courseCreator);
    }

    private void saveCourseRelativeToCreator(Course course, User courseCreator) {
        course.setTeacher(courseCreator);

        saveCourse(course);
    }

    @Transactional
    @Loggable(process = "удаление курса")
    @CacheEvict(value = {"courses", "answers", "artifacts"}, allEntries = true)
    public void deleteCourse(Integer courseId, Integer userId) {
        Course course = entityFindService.findCourseById(courseId);

        validateUserPermissions(course, userId);

        deleteCourseDirectoriesInMinio(course);
        deleteCourseDirectoriesInDb(course);
    }

    private void validateUserPermissions(Course course, Integer userId) {
        if (!isCourseCreator(course, userId)) {
            throw new NoRightsException("You are not the creator of the course!");
        }
    }

    private boolean isCourseCreator(Course course, Integer userId) {
        return course.getTeacher().getId().equals(userId);
    }

    @CacheEvict(value = "courses", allEntries = true)
    @Loggable(process = "обновление курса")
    public void updateCourse(UpdateCourseDTO dto, Integer userId) {
        Course course = entityFindService.findCourseById(dto.getCourseId());

        validateUserPermissions(course, userId);

        updateCourseFromDto(course, dto);

        saveCourse(course);
    }

    private void updateCourseFromDto(Course course, UpdateCourseDTO dto) {
        if (dto.getTitle() != null) {
            course.setTitle(dto.getTitle());
        }
        if (dto.getDescription() != null) {
            course.setDescription(dto.getDescription());
        }
    }

    @Timed
    public Page<Course> getCourses(@Nullable String title, @Nullable String teacherName, Pageable pageable) {
        Page<Course> courses = findAllCourses(pageable);

        Stream<Course> filteredStream = validateStreamCourse(title, teacherName, courses.stream());

        List<Course> filteredCourses = filteredStream.collect(Collectors.toList());
        return new PageImpl<>(filteredCourses, pageable, filteredCourses.size());
    }

    private Stream<Course> validateStreamCourse(@Nullable String title,
                                                @Nullable String teacherName,
                                                Stream<Course> courseStream) {
        Stream<Course> filteredStream = courseStream;
        if (title != null) {
            filteredStream = courseStream.filter(course ->
                    course.getTitle().toLowerCase().contains(title.toLowerCase()));
        }
        if (teacherName != null) {
            filteredStream = courseStream.filter(course ->
                    course.getTeacher().getFullName().toLowerCase().contains(teacherName.toLowerCase()));
        }

        return filteredStream;
    }

    @Loggable(process = "регистрация пользователя на курс")
    @Timed
    public void registerUserToCourse(Integer userId, Integer courseId) {
        Course course = entityFindService.findCourseById(courseId);
        User user = entityFindService.findUserById(userId);

        saveCourseRelativeOnRegisterUser(course, user);
    }

    private void saveCourseRelativeOnRegisterUser(Course course, User newUser) {
        Hibernate.initialize(course);

        course.getStudents().add(newUser);
        saveCourse(course);
    }

    @Loggable(process = "удаление директории курса")
    private void deleteCourseDirectoriesInMinio(Course course) {
        kafkaTemplate.send(config.getDeleteFileRequest(), course.getId().toString());
    }

    private void deleteCourseDirectoriesInDb(Course course) {
        courseRepository.delete(course);
    }

    private void saveCourse(Course course) {
        courseRepository.save(course);
    }
}
