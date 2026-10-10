package com.smartstudy.assistant.controller;

import com.smartstudy.assistant.service.HistoryService;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/history")
public class HistoryController {

    private final HistoryService historyService;

    public HistoryController(HistoryService historyService) {
        this.historyService = historyService;
    }

    @GetMapping
    public List<Map<String, Object>> history(
            Authentication authentication) {

        return historyService.getHistory(
                authentication.getName()
        );
    }
}
