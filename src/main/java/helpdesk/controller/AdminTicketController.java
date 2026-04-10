package helpdesk.controller;

import helpdesk.dto.TicketResponse;
import helpdesk.model.TicketStatus;
import helpdesk.service.TicketService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/tickets")
@RequiredArgsConstructor
public class AdminTicketController {

    private final TicketService ticketService;

    @GetMapping
    public List<TicketResponse> getAllTickets() {
        return ticketService.getAllTickets();
    }

    @PatchMapping("/{id}/status")
    public TicketResponse updateStatus(@PathVariable Long id,
                                       @RequestBody Map<String, String> request) {
        TicketStatus status = TicketStatus.valueOf(request.get("status"));
        return ticketService.updateStatus(id, status);
    }
}