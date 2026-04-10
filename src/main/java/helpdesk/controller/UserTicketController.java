package helpdesk.controller;

import helpdesk.dto.CreateTicketRequest;
import helpdesk.dto.TicketResponse;
import helpdesk.service.TicketService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api/user/tickets")
@RequiredArgsConstructor
public class UserTicketController {

    private final TicketService ticketService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public TicketResponse createTicket(@Valid @RequestBody CreateTicketRequest request,
                                       Principal principal) {
        return ticketService.createTicket(principal.getName(), request);
    }

    @GetMapping
    public List<TicketResponse> getMyTickets(Principal principal) {
        return ticketService.getMyTickets(principal.getName());
    }
}