package com.StartUp.controller;

import com.StartUp.dtos.ai.AiDtos;
import com.StartUp.dtos.chat.ChatDtos;
import com.StartUp.service.AiService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/ai")
@RequiredArgsConstructor
@Tag(name = "AI Features", description = "AI-powered features: chat, job matching, cover letters, profile tips, application screening")
public class AiController {

    private final AiService aiService;


    @Operation(summary = "Chat with the Breaddy AI assistant")
    @PostMapping("/chat")
    public ResponseEntity<ChatDtos.ChatResponse> chat(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestBody ChatDtos.ChatRequest request) {
        return ResponseEntity.ok(aiService.chat(userDetails.getUsername(), request));
    }

    @Operation(summary = "Get chat history")
    @GetMapping("/history")
    public ResponseEntity<List<ChatDtos.ChatHistoryResponse>> getHistory(
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(aiService.getHistory(userDetails.getUsername()));
    }

    @Operation(summary = "Clear chat history")
    @DeleteMapping("/history")
    public ResponseEntity<Void> clearHistory(
            @AuthenticationPrincipal UserDetails userDetails) {
        aiService.clearHistory(userDetails.getUsername());
        return ResponseEntity.noContent().build();
    }

    @Operation(
        summary = "AI Job Match Score",
        description = "Returns a 0-100 compatibility score between the logged-in student's profile and a specific job, with strengths, gaps, and a summary."
    )
    @GetMapping("/job-match/{jobId}")
    public ResponseEntity<AiDtos.JobMatchResponse> getJobMatch(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long jobId) {
        return ResponseEntity.ok(aiService.getJobMatch(userDetails.getUsername(), jobId));
    }


    @Operation(
        summary = "AI Cover Letter Generator",
        description = "Generates a personalized, ready-to-use cover letter based on the student's profile and the target job."
    )
    @GetMapping("/cover-letter/{jobId}")
    public ResponseEntity<AiDtos.CoverLetterResponse> generateCoverLetter(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long jobId) {
        return ResponseEntity.ok(aiService.generateCoverLetter(userDetails.getUsername(), jobId));
    }


    @Operation(
        summary = "AI Profile Tips",
        description = "Analyzes the student's current profile and returns a strength score (0-100) plus prioritized, actionable improvement tips."
    )
    @GetMapping("/profile-tips")
    public ResponseEntity<AiDtos.ProfileTipsResponse> getProfileTips(
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(aiService.getProfileTips(userDetails.getUsername()));
    }



    @Operation(
        summary = "AI Application Screener",
        description = "For employers: ranks all applicants for a job by fit score with reasoning, so you can focus on the best candidates first."
    )
    @GetMapping("/screen-applications/{jobId}")
    public ResponseEntity<AiDtos.ApplicationScreeningResponse> screenApplications(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long jobId) {
        return ResponseEntity.ok(aiService.screenApplications(userDetails.getUsername(), jobId));
    }
}
