package com.smartstudy.assistant.service;

import com.smartstudy.assistant.repository.StudyHistoryRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class HistoryService {

    private final StudyHistoryRepository repository;
    private final UserService userService;

    public HistoryService(
            StudyHistoryRepository repository,
            UserService userService) {

        this.repository = repository;
        this.userService = userService;
    }

    public List<Map<String, Object>> getHistory(String email) {

        var user = userService.getByEmail(email);

        return repository
                .findTop20ByUserIdOrderByCreatedAtDesc(user.getId())
                .stream()
                .map(item -> Map.of(
                        "id", (Object) item.getId(),
                        "type", item.getType(),
                        "topic", item.getTopic(),
                        "content", item.getContent(),
                        "createdAt", item.getCreatedAt().toString()
                ))
                .toList();
    }
}
