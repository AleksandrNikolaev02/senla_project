package com.example.file_service.util;

import com.example.dto.Event;
import com.example.file_service.config.TopicConfig;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class EventToTopicsStorage {
    private final Map<Event, String> eventToTopics;

    public EventToTopicsStorage(TopicConfig config) {
        eventToTopics = Map.of(
                Event.ANSWER, config.getFileAnswers(),
                Event.ARTIFACT, config.getFileResponses()
        );
    }

    public String getTopicByEvent(Event event) {
        return eventToTopics.get(event);
    }
}
