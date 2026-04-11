package helpdesk.controller;

import helpdesk.dto.AdminTicketResponse;
import helpdesk.dto.AdminTicketSummaryResponse;
import helpdesk.dto.AssignTicketRequest;
import helpdesk.dto.TicketActivityResponse;
import helpdesk.dto.UpdateTicketNotesRequest;
import helpdesk.dto.UpdateTicketPriorityRequest;
import helpdesk.model.DeviceType;
import helpdesk.model.IssueCategory;
import helpdesk.model.TicketPriority;
import helpdesk.model.TicketStatus;
import helpdesk.service.TicketActivityService;
import helpdesk.service.TicketService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/tickets")
@RequiredArgsConstructor
public class AdminTicketController {

    private final TicketService ticketService;
    private final TicketActivityService ticketActivityService;

    @GetMapping
    public List<AdminTicketResponse> getAllTickets() {
        return ticketService.getAllTickets();
    }

    @PatchMapping("/{id}/status")
    public AdminTicketResponse updateStatus(@PathVariable Long id,
                                            @RequestBody Map<String, String> request,
                                            Principal principal) {
        TicketStatus status = TicketStatus.valueOf(request.get("status"));
        return ticketService.updateStatus(id, status, principal.getName());
    }

    @PatchMapping("/{id}/notes")
    public AdminTicketResponse updateNotes(@PathVariable Long id,
                                           @RequestBody UpdateTicketNotesRequest request,
                                           Principal principal) {
        return ticketService.updateNotes(id, request, principal.getName());
    }

    @PatchMapping("/{id}/assign")
    public AdminTicketResponse assignTicket(@PathVariable Long id,
                                            @Valid @RequestBody AssignTicketRequest request,
                                            Principal principal) {
        return ticketService.assignTicket(id, request, principal.getName());
    }

    @GetMapping("/summary")
    public AdminTicketSummaryResponse getTicketSummary() {
        return ticketService.getTicketSummary();
    }

    @GetMapping("/search")
    public List<AdminTicketResponse> searchTickets(
            @RequestParam(required = false) TicketStatus status,
            @RequestParam(required = false) DeviceType deviceType,
            @RequestParam(required = false) IssueCategory issueCategory,
            @RequestParam(required = false) TicketPriority priority,
            @RequestParam(required = false) String assignedTechnicianUsername,
            @RequestParam(required = false) String department,
            @RequestParam(required = false) Boolean overdue
    ) {
        return ticketService.searchTickets(
                status,
                deviceType,
                issueCategory,
                priority,
                assignedTechnicianUsername,
                department,
                overdue
        );
    }

    @PatchMapping("/{id}/priority")
    public AdminTicketResponse updatePriority(@PathVariable Long id,
                                              @Valid @RequestBody UpdateTicketPriorityRequest request,
                                              Principal principal) {
        return ticketService.updatePriority(id, request, principal.getName());
    }

    @GetMapping("/{id}/activity")
    public List<TicketActivityResponse> getTicketActivities(@PathVariable Long id) {
        return ticketActivityService.getAdminActivities(id);
    }
}