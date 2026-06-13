package helpdesk.dto;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;
import java.util.List;

@Getter
@Builder
public class SupervisorPerformanceReportResponse {

    private LocalDate dateFrom;
    private LocalDate dateTo;

    private long totalTickets;
    private long assignedTickets;
    private long unassignedTickets;
    private long pendingTickets;
    private long inProgressTickets;
    private long resolvedTickets;
    private long escalatedTickets;
    private long overdueTickets;

    private Double averageResolutionHours;

    private List<SupervisorTechnicianPerformanceRowResponse> technicianRows;
    private List<AdminTicketResponse> tickets;
}