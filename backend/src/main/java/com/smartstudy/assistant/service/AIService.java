package com.smartstudy.assistant.service;

import com.smartstudy.assistant.dto.AIRequest;
import com.smartstudy.assistant.model.StudyHistory;
import com.smartstudy.assistant.model.User;
import com.smartstudy.assistant.repository.StudyHistoryRepository;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class AIService {

    private final GeminiService geminiService;
    private final UserService userService;
    private final StudyHistoryRepository historyRepository;

    public AIService(
            GeminiService geminiService,
            UserService userService,
            StudyHistoryRepository historyRepository) {

        this.geminiService = geminiService;
        this.userService = userService;
        this.historyRepository = historyRepository;
    }

    public Map<String, String> explain(
            String email,
            AIRequest request) {

        String topic = clean(request.getTopic());

        if (topic.isBlank()) {
            throw new RuntimeException("Enter a question or topic.");
        }

        String prompt =
                "Explain this study question in simple student-friendly language. "
                + "Include definition, key points and one example when useful. "
                + "Question: " + topic;

        String answer = geminiService.ask(prompt);

        save(email, "EXPLANATION", topic, answer);

        return Map.of("answer", answer);
    }

    public Map<String, String> generateNotes(
            String email,
            AIRequest request) {

        String topic = clean(request.getTopic());

        if (topic.isBlank()) {
            throw new RuntimeException("Enter a topic.");
        }

        String prompt =
                "Create clear exam-oriented notes for this topic. "
                + "Use headings, bullet points, important terms and a short summary. "
                + "Topic: " + topic;

        String notes = geminiService.ask(prompt);

        save(email, "NOTES", topic, notes);

        return Map.of("notes", notes);
    }

    public Map<String, String> generateQuiz(
            String email,
            AIRequest request) {

        String topic = clean(request.getTopic());

        if (topic.isBlank()) {
            throw new RuntimeException("Enter a topic.");
        }

        int count = request.getNumberOfQuestions() == null
                ? 5
                : Math.max(
                        1,
                        Math.min(request.getNumberOfQuestions(), 10)
                );

        String difficulty =
                request.getDifficulty() == null
                        ? "medium"
                        : request.getDifficulty();

        String prompt =
                "Create " + count
                + " multiple-choice questions about "
                + topic
                + " at "
                + difficulty
                + " difficulty. "
                + "Give four options A-D and an Answer line after each question.";

        String quiz = geminiService.ask(prompt);

        save(email, "QUIZ", topic, quiz);

        return Map.of("quiz", quiz);
    }

    private void save(
            String email,
            String type,
            String topic,
            String content) {

        User user = userService.getByEmail(email);

        StudyHistory history = new StudyHistory();

        history.setUser(user);
        history.setType(type);
        history.setTopic(topic);
        history.setContent(content);

        historyRepository.save(history);
    }

    private String clean(String value) {
        return value == null ? "" : value.trim();
    }
}
