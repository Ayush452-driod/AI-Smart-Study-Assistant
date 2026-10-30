package com.smartstudy.assistant.controller;

import com.smartstudy.assistant.dto.AIRequest;
import com.smartstudy.assistant.service.AIService;

import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import com.smartstudy.assistant.dto.QuizAnswerRequest;

@RestController
@RequestMapping("/api/ai")
public class AIController {

    private final AIService aiService;

    public AIController(AIService aiService) {
        this.aiService = aiService;
    }

    @PostMapping("/explain")
    public ResponseEntity<?> explain(
            Authentication authentication,
            @RequestBody AIRequest request) {

        return ResponseEntity.ok(
                aiService.explain(
                        authentication.getName(),
                        request
                )
        );
    }

    @PostMapping("/notes")
    public ResponseEntity<?> notes(
            Authentication authentication,
            @RequestBody AIRequest request) {

        return ResponseEntity.ok(
                aiService.generateNotes(
                        authentication.getName(),
                        request
                )
        );
    }

    @PostMapping("/quiz")
    public ResponseEntity<?> quiz(
            Authentication authentication,
            @RequestBody AIRequest request) {

        return ResponseEntity.ok(
                aiService.generateQuiz(
                        authentication.getName(),
                        request
                )
        );
    }

    
@PostMapping("/quiz/answer")
public ResponseEntity<?> checkQuizAnswer(
        Authentication authentication,
        @RequestBody QuizAnswerRequest request) {

    if (request.getQuizId() == null ||
            request.getQuestionIndex() == null ||
            request.getSelectedOption() == null) {

        return ResponseEntity.badRequest()
                .body(Map.of("message", "Missing quiz answer details."));
    }

    return ResponseEntity.ok(
            aiService.checkQuizAnswer(
                    authentication.getName(),
                    request.getQuizId(),
                    request.getQuestionIndex(),
                    request.getSelectedOption()
            )
    );
}

}
