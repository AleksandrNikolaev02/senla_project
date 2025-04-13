package com.example.demo.service;

import com.example.demo.exception.NotFoundException;
import com.example.demo.model.Answer;
import com.example.demo.model.Artifact;
import com.example.demo.model.Course;
import com.example.demo.model.User;
import com.example.demo.repository.AnswerRepository;
import com.example.demo.repository.ArtifactRepository;
import com.example.demo.repository.CourseRepository;
import com.example.demo.repository.UserRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
public class EntityFindService {
    private UserRepository userRepository;
    private CourseRepository courseRepository;
    private ArtifactRepository artifactRepository;
    private AnswerRepository answerRepository;

    public Course findCourseById(Integer courseId) {
        return courseRepository.findById(courseId)
                .orElseThrow(() -> new NotFoundException("The course does not exist!"));
    }

    public User findUserById(Integer userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("The user does not exist!"));
    }

    public Artifact findArtifactById(Integer artifactId) {
        return artifactRepository.findById(artifactId)
                .orElseThrow(() -> new NotFoundException("The artifact does not exist!"));
    }

    public Answer findAnswerById(Integer answerId) {
        return answerRepository.findById(answerId)
                .orElseThrow(() -> new NotFoundException("The answer does not exist!"));
    }
}
