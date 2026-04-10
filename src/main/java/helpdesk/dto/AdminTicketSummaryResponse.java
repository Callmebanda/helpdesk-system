package helpdesk.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class AdminTicketSummaryResponse {

    private long totalTickets;
    private long pendingTickets;
    private long inProgressTickets;
    private long resolvedTickets;
    private long assignedTickets;
    private long unassignedTickets;
}