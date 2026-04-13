package helpdesk.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class TechTicketSummaryResponse {

    private long totalAssigned;
    private long pending;
    private long inProgress;
    private long resolved;
    private long overdue;
}