package helpdesk.service;

import helpdesk.dto.KnowledgeArticleResponse;
import helpdesk.model.ActivityType;
import helpdesk.model.DeviceType;
import helpdesk.model.IssueCategory;
import helpdesk.model.KnowledgeArticle;
import helpdesk.model.Ticket;
import helpdesk.model.TicketStatus;
import helpdesk.repository.KnowledgeArticleRepository;
import helpdesk.repository.TicketRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class KnowledgeBaseService {

    private final KnowledgeArticleRepository knowledgeArticleRepository;
    private final TicketRepository ticketRepository;
    private final TicketActivityService ticketActivityService;

    @Transactional
    public KnowledgeArticleResponse publishFromTicket(Long ticketId, String publishedBy) {
        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new RuntimeException("Ticket not found"));

        if (ticket.getStatus() != TicketStatus.RESOLVED) {
            throw new RuntimeException("Only resolved tickets can be published to the knowledge base");
        }

        if (ticket.getResolutionNote() == null || ticket.getResolutionNote().isBlank()) {
            throw new RuntimeException("Resolution note is required before publishing to the knowledge base");
        }

        if (knowledgeArticleRepository.existsBySourceTicketId(ticketId)) {
            throw new RuntimeException("This ticket has already been published to the knowledge base");
        }

        KnowledgeArticle article = KnowledgeArticle.builder()
                .sourceTicketId(ticket.getId())
                .deviceType(ticket.getDeviceType())
                .issueCategory(ticket.getIssueCategory())
                .title(ticket.getProblemTitle())
                .symptoms(buildSymptoms(ticket))
                .solution(ticket.getResolutionNote())
                .publishedBy(publishedBy)
                .build();

        KnowledgeArticle savedArticle = knowledgeArticleRepository.save(article);

        ticketActivityService.logActivity(
                ticket,
                ActivityType.KNOWLEDGE_PUBLISHED,
                publishedBy,
                "Published to knowledge base",
                false
        );

        return mapToResponse(savedArticle);
    }

    @Transactional(readOnly = true)
    public List<KnowledgeArticleResponse> getArticles(DeviceType deviceType, IssueCategory issueCategory) {
        List<KnowledgeArticle> articles;

        if (deviceType != null && issueCategory != null) {
            articles = knowledgeArticleRepository.findByDeviceTypeAndIssueCategoryOrderByCreatedAtDesc(
                    deviceType, issueCategory
            );
        } else if (deviceType != null) {
            articles = knowledgeArticleRepository.findByDeviceTypeOrderByCreatedAtDesc(deviceType);
        } else if (issueCategory != null) {
            articles = knowledgeArticleRepository.findByIssueCategoryOrderByCreatedAtDesc(issueCategory);
        } else {
            articles = knowledgeArticleRepository.findAllByOrderByCreatedAtDesc();
        }

        return articles.stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public KnowledgeArticleResponse getArticleById(Long id) {
        KnowledgeArticle article = knowledgeArticleRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Knowledge article not found"));

        return mapToResponse(article);
    }

    private String buildSymptoms(Ticket ticket) {
        StringBuilder symptoms = new StringBuilder();

        if (ticket.getDescription() != null && !ticket.getDescription().isBlank()) {
            symptoms.append(ticket.getDescription());
        }

        if (ticket.getOtherIssue() != null && !ticket.getOtherIssue().isBlank()) {
            if (symptoms.length() > 0) {
                symptoms.append("\n");
            }
            symptoms.append("Other issue: ").append(ticket.getOtherIssue());
        }

        if (symptoms.length() == 0) {
            symptoms.append(ticket.getProblemTitle());
        }

        return symptoms.toString();
    }

    private KnowledgeArticleResponse mapToResponse(KnowledgeArticle article) {
        return KnowledgeArticleResponse.builder()
                .id(article.getId())
                .sourceTicketId(article.getSourceTicketId())
                .deviceType(article.getDeviceType())
                .issueCategory(article.getIssueCategory())
                .title(article.getTitle())
                .symptoms(article.getSymptoms())
                .solution(article.getSolution())
                .publishedBy(article.getPublishedBy())
                .createdAt(article.getCreatedAt())
                .updatedAt(article.getUpdatedAt())
                .build();
    }
}