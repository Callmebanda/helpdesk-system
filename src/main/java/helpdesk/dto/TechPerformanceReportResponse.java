package helpdesk.dto;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;
import java.util.List;

@Getter
@Builder
public class TechPerformanceReportResponse {

    private String technicianUsername;
    private LocalDate dateFrom;
    private LocalDate dateTo;

    private long totalAssigned;
    private long pending;
    private long inProgress;
    private long resolved;
    private long escalated;
    private long overdue;

    private Double averageResolutionHours;

    private List<AdminTicketResponse> tickets;
}