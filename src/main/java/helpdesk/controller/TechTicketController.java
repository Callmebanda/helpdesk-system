package helpdesk.controller;

import helpdesk.dto.AdminTicketResponse;
import helpdesk.dto.TicketActivityResponse;
import helpdesk.dto.UpdateTicketNotesRequest;
import helpdesk.model.TicketStatus;
import helpdesk.service.TicketActivityService;
import helpdesk.service.TicketService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/tech/tickets")
@RequiredArgsConstructor
public class TechTicketController {

    private final TicketService ticketService;
    private final TicketActivityService ticketActivityService;

    @GetMapping
    public List<AdminTicketResponse> getAssignedTickets(Principal principal) {
        return ticketService.getAssignedTickets(principal.getName());
    }

    @PatchMapping("/{id}/status")
    public AdminTicketResponse updateAssignedTicketStatus(@PathVariable Long id,
                                                          @RequestBody Map<String, String> request,
                                                          Principal principal) {
        TicketStatus status = TicketStatus.valueOf(request.get("status"));
        return ticketService.updateAssignedTicketStatus(id, status, principal.getName());
    }

    @PatchMapping("/{id}/notes")
    public AdminTicketResponse updateAssignedTicketNotes(@PathVariable Long id,
                                                         @RequestBody UpdateTicketNotesRequest request,
                                                         Principal principal) {
        return ticketService.updateAssignedTicketNotes(id, request, principal.getName());
    }

    @GetMapping("/{id}/activity")
    public List<TicketActivityResponse> getAssignedTicketActivity(@PathVariable Long id,
                                                                  Principal principal) {
        return ticketActivityService.getTechnicianActivities(id, principal.getName());
    }
}