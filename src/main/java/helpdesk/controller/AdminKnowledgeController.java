package helpdesk.controller;

import helpdesk.dto.KnowledgeArticleResponse;
import helpdesk.model.DeviceType;
import helpdesk.model.IssueCategory;
import helpdesk.service.KnowledgeBaseService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api/admin/knowledge")
@RequiredArgsConstructor
public class AdminKnowledgeController {

    private final KnowledgeBaseService knowledgeBaseService;

    @PostMapping("/from-ticket/{ticketId}")
    @ResponseStatus(HttpStatus.CREATED)
    public KnowledgeArticleResponse publishFromTicket(@PathVariable Long ticketId, Principal principal) {
        return knowledgeBaseService.publishFromTicket(ticketId, principal.getName());
    }

    @GetMapping
    public List<KnowledgeArticleResponse> getArticles(
            @RequestParam(required = false) DeviceType deviceType,
            @RequestParam(required = false) IssueCategory issueCategory
    ) {
        return knowledgeBaseService.getArticles(deviceType, issueCategory);
    }

    @GetMapping("/{id}")
    public KnowledgeArticleResponse getArticleById(@PathVariable Long id) {
        return knowledgeBaseService.getArticleById(id);
    }
}