package helpdesk.controller;

import helpdesk.dto.AdminTicketResponse;
import helpdesk.dto.AssignTicketRequest;
import helpdesk.dto.UpdateTicketNotesRequest;
import helpdesk.model.TicketStatus;
import helpdesk.service.TicketService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import helpdesk.dto.AdminTicketSummaryResponse;
import helpdesk.model.DeviceType;
import helpdesk.model.IssueCategory;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/tickets")
@RequiredArgsConstructor
public class AdminTicketController {

    private final TicketService ticketService;

    @GetMapping
    public List<AdminTicketResponse> getAllTickets() {
        return ticketService.getAllTickets();
    }

    @PatchMapping("/{id}/status")
    public AdminTicketResponse updateStatus(@PathVariable Long id,
                                            @RequestBody Map<String, String> request) {
        TicketStatus status = TicketStatus.valueOf(request.get("status"));
        return ticketService.updateStatus(id, status);
    }

    @PatchMapping("/{id}/notes")
    public AdminTicketResponse updateNotes(@PathVariable Long id,
                                           @RequestBody UpdateTicketNotesRequest request) {
        return ticketService.updateNotes(id, request);
    }

    @PatchMapping("/{id}/assign")
    public AdminTicketResponse assignTicket(@PathVariable Long id,
                                            @Valid @RequestBody AssignTicketRequest request) {
        return ticketService.assignTicket(id, request);
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
            @RequestParam(required = false) String assignedTechnicianUsername,
            @RequestParam(required = false) String department
    ) {
        return ticketService.searchTickets(
                status,
                deviceType,
                issueCategory,
                assignedTechnicianUsername,
                department
        );
    }
}