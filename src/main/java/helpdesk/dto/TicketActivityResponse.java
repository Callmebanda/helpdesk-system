package helpdesk.dto;

import helpdesk.model.ActivityType;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class TicketActivityResponse {

    private Long id;
    private ActivityType activityType;
    private String actorUsername;
    private String description;
    private boolean visibleToUser;
    private LocalDateTime createdAt;
}