package de.anonytix.feedback.web;

import de.anonytix.feedback.dto.PublicFormResponse;
import de.anonytix.feedback.dto.SubmissionResponse;
import de.anonytix.feedback.dto.SubmitFeedbackRequest;
import de.anonytix.feedback.service.FeedbackSubmissionService;
import de.anonytix.feedback.service.PublicFormService;
import jakarta.validation.Valid;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/public/invitations/{token}")
public class PublicFeedbackController {

    private final PublicFormService publicFormService;
    private final FeedbackSubmissionService submissionService;

    public PublicFeedbackController(
            PublicFormService publicFormService,
            FeedbackSubmissionService submissionService) {
        this.publicFormService = publicFormService;
        this.submissionService = submissionService;
    }

    @GetMapping("/form")
    PublicFormResponse form(
            @PathVariable String token,
            @RequestParam(required = false) UUID departmentId) {
        return publicFormService.load(token, departmentId);
    }

    @PostMapping("/submissions")
    ResponseEntity<SubmissionResponse> submit(
            @PathVariable String token,
            @Valid @RequestBody SubmitFeedbackRequest request) {
        return ResponseEntity.status(201).body(submissionService.submit(token, request));
    }
}
