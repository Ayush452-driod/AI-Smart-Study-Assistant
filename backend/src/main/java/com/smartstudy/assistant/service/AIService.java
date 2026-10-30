
package com.smartstudy.assistant.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import com.smartstudy.assistant.dto.AIRequest;
import com.smartstudy.assistant.model.StudyHistory;
import com.smartstudy.assistant.model.User;
import com.smartstudy.assistant.repository.StudyHistoryRepository;

import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class AIService {

    private final GeminiService geminiService;
    private final UserService userService;
    private final StudyHistoryRepository historyRepository;
    private final ObjectMapper objectMapper;

    // Temporary storage for active quizzes
    private final Map<String, QuizSession> activeQuizzes =
            new ConcurrentHashMap<>();

    public AIService(
            GeminiService geminiService,
            UserService userService,
            StudyHistoryRepository historyRepository,
            ObjectMapper objectMapper) {

        this.geminiService = geminiService;
        this.userService = userService;
        this.historyRepository = historyRepository;
        this.objectMapper = objectMapper;
    }


    /* ASK AI */

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
                + "Use clear headings and subheadings. "
                + "Question: " + topic;

        String answer = geminiService.ask(prompt);

        save(email, "EXPLANATION", topic, answer);

        return Map.of("answer", answer);
    }


    /* GENERATE NOTES */

    public Map<String, String> generateNotes(
            String email,
            AIRequest request) {

        String topic = clean(request.getTopic());

        if (topic.isBlank()) {
            throw new RuntimeException("Enter a topic.");
        }

        String prompt =
                "Create clear exam-oriented notes for this topic. "
                + "Use meaningful headings, subheadings, bullet points, "
                + "important terms, examples and a short summary. "
                + "Topic: " + topic;

        String notes = geminiService.ask(prompt);

        save(email, "NOTES", topic, notes);

        return Map.of("notes", notes);
    }


    /* GENERATE INTERACTIVE QUIZ */

    public Map<String, Object> generateQuiz(
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
                        : request.getDifficulty().toLowerCase();

        if (!List.of("easy", "medium", "hard").contains(difficulty)) {
            throw new RuntimeException("Invalid quiz difficulty.");
        }

        String prompt =
                "Generate exactly " + count
                + " multiple-choice questions about " + topic
                + " at " + difficulty + " difficulty.\n\n"

                + "Return ONLY valid JSON. No Markdown or code fences.\n"

                + """
                {
                  "questions": [
                    {
                      "question": "Question text",
                      "options": [
                        {"id": "A", "text": "Option A"},
                        {"id": "B", "text": "Option B"},
                        {"id": "C", "text": "Option C"},
                        {"id": "D", "text": "Option D"}
                      ],
                      "correctAnswer": "B",
                      "explanation": "Explain why the correct answer is right."
                    }
                  ]
                }
                """

                + "\nRules:\n"
                + "1. Every question must have exactly four options.\n"
                + "2. Each question must have exactly one correct answer.\n"
                + "3. Correct answer must be A, B, C or D.\n"
                + "4. Options must be factually accurate and distinct.\n"
                + "5. Explanations must be simple and student-friendly.\n"
                + "6. Generate exactly " + count + " questions.";


        try {

            // Ask Gemini to generate quiz
            String aiResponse = geminiService.ask(prompt);

            if (aiResponse == null || aiResponse.isBlank()) {
                throw new RuntimeException(
                        "Gemini returned an empty quiz response."
                );
            }

            // Remove accidental Markdown code fences
            String json = aiResponse
                    .replaceFirst("(?s)^\\s*```(?:json)?\\s*", "")
                    .replaceFirst("\\s*```\\s*$", "")
                    .trim();

            // Parse JSON
            JsonNode root = objectMapper.readTree(json);

            JsonNode questions = root.get("questions");

            if (questions == null ||
                    !questions.isArray() ||
                    questions.size() != count) {

                throw new RuntimeException(
                        "AI generated an invalid number of questions."
                );
            }

            List<Map<String, Object>> publicQuestions =
                    new ArrayList<>();

            List<Map<String, String>> answerData =
                    new ArrayList<>();


            for (JsonNode question : questions) {

                String questionText =
                        question.path("question").asText("").trim();

                String correctAnswer =
                        question.path("correctAnswer")
                                .asText("")
                                .trim()
                                .toUpperCase();

                JsonNode options = question.get("options");

                String explanation =
                        question.path("explanation")
                                .asText("")
                                .trim();

                if (questionText.isBlank()
                        || explanation.isBlank()
                        || options == null
                        || !options.isArray()
                        || options.size() != 4
                        || !List.of("A", "B", "C", "D")
                                .contains(correctAnswer)) {

                    throw new RuntimeException(
                            "AI generated an invalid question."
                    );
                }

                List<Map<String, String>> publicOptions =
                        new ArrayList<>();

                Set<String> optionIds = new HashSet<>();

                for (JsonNode option : options) {

                    String id = option.path("id")
                            .asText("")
                            .trim()
                            .toUpperCase();

                    String text = option.path("text")
                            .asText("")
                            .trim();

                    if (!List.of("A", "B", "C", "D").contains(id)
                            || text.isBlank()
                            || !optionIds.add(id)) {

                        throw new RuntimeException(
                                "AI generated invalid quiz options."
                        );
                    }

                    publicOptions.add(Map.of(
                            "id", id,
                            "text", text
                    ));
                }

                if (!optionIds.equals(
                        Set.of("A", "B", "C", "D"))) {

                    throw new RuntimeException(
                            "Quiz must contain options A, B, C and D."
                    );
                }

                // Public question: no correct answer is exposed
                publicQuestions.add(Map.of(
                        "question", questionText,
                        "options", publicOptions
                ));

                // Private answer: stored only on backend
                answerData.add(Map.of(
                        "correctAnswer", correctAnswer,
                        "explanation", explanation
                ));
            }


            // Create unique quiz ID
            String quizId = UUID.randomUUID().toString();

            // Store answers on backend
            activeQuizzes.put(
                    quizId,
                    new QuizSession(email, answerData)
            );


            // Save safe history summary
            save(
                    email,
                    "QUIZ",
                    topic,
                    "Interactive quiz generated with "
                            + count + " questions. Difficulty: "
                            + difficulty
            );


            // Return only questions and options
            Map<String, Object> response = new HashMap<>();

            response.put("quizId", quizId);
            response.put("topic", topic);
            response.put("difficulty", difficulty);
            response.put("questions", publicQuestions);

            return response;

        } catch (Exception e) {

            // Preserve meaningful error messages
            if (e instanceof RuntimeException) {
                throw (RuntimeException) e;
            }

            throw new RuntimeException(
                    "Unable to generate quiz: " + e.getMessage(),
                    e
            );
        }
    }


    /* CHECK SELECTED ANSWER */

    public Map<String, Object> checkQuizAnswer(
            String email,
            String quizId,
            int questionIndex,
            String selectedOption) {

        QuizSession session = activeQuizzes.get(quizId);

        if (session == null || !session.email.equals(email)) {
            throw new RuntimeException(
                    "Quiz not found or expired."
            );
        }

        if (questionIndex < 0 ||
                questionIndex >= session.answers.size()) {

            throw new RuntimeException("Invalid question number.");
        }

        String selected = clean(selectedOption).toUpperCase();

        if (!List.of("A", "B", "C", "D").contains(selected)) {
            throw new RuntimeException("Invalid selected option.");
        }

        synchronized (session) {

            if (session.submitted.containsKey(questionIndex)) {
                throw new RuntimeException(
                        "You have already answered this question."
                );
            }

            Map<String, String> answer =
                    session.answers.get(questionIndex);

            String correctAnswer = answer.get("correctAnswer");

            boolean isCorrect = selected.equals(correctAnswer);

            session.submitted.put(questionIndex, selected);

            if (isCorrect) {
                session.score++;
            }

            Map<String, Object> response = new HashMap<>();

            response.put("correct", isCorrect);
            response.put("correctAnswer", correctAnswer);
            response.put("explanation", answer.get("explanation"));
            response.put("score", session.score);
            response.put("answered", session.submitted.size());
            response.put("totalQuestions", session.answers.size());

            response.put(
                    "completed",
                    session.submitted.size() == session.answers.size()
            );

            return response;
        }
    }


    /* QUIZ SESSION */

    private static class QuizSession {

        private final String email;

        private final List<Map<String, String>> answers;

        private final Map<Integer, String> submitted =
                new HashMap<>();

        private int score = 0;

        private QuizSession(
                String email,
                List<Map<String, String>> answers) {

            this.email = email;
            this.answers = answers;
        }
    }


    /* SAVE HISTORY */

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


    /* CLEAN INPUT */

    private String clean(String value) {
        return value == null ? "" : value.trim();
    }
}
