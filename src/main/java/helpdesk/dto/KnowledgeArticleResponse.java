package helpdesk.dto;

import helpdesk.model.DeviceType;
import helpdesk.model.IssueCategory;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class KnowledgeArticleResponse {

    private Long id;
    private Long sourceTicketId;
    private DeviceType deviceType;
    private IssueCategory issueCategory;
    private String title;
    private String symptoms;
    private String solution;
    private String publishedBy;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}