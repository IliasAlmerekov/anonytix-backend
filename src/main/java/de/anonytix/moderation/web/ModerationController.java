package de.anonytix.moderation.web;

import de.anonytix.moderation.dto.ModerationDetail;
import de.anonytix.moderation.dto.ModerationSummary;
import de.anonytix.moderation.dto.RejectModerationRequest;
import de.anonytix.moderation.service.ModerationService;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/platform/moderation/submissions")
public class ModerationController {

    private final ModerationService service;

    public ModerationController(ModerationService service) {
        this.service = service;
    }

    @GetMapping
    public List<ModerationSummary> list(
            @RequestParam(defaultValue = "REVIEW_PENDING") String status) {
        return service.list(status);
    }

    @GetMapping("/{submissionId}")
    public ModerationDetail get(@PathVariable UUID submissionId) {
        return service.get(submissionId);
    }

    @PostMapping("/{submissionId}/approve")
    public ModerationSummary approve(@PathVariable UUID submissionId) {
        return service.approve(submissionId);
    }

    @PostMapping("/{submissionId}/reject")
    public ModerationSummary reject(
            @PathVariable UUID submissionId,
            @Valid @RequestBody RejectModerationRequest request) {
        return service.reject(submissionId, request.reason());
    }
}
