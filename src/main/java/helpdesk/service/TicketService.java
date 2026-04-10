package helpdesk.service;

import helpdesk.dto.CreateTicketRequest;
import helpdesk.dto.TicketResponse;
import helpdesk.model.Ticket;
import helpdesk.model.TicketStatus;
import helpdesk.model.User;
import helpdesk.repository.TicketRepository;
import helpdesk.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TicketService {

    private final TicketRepository ticketRepository;
    private final UserRepository userRepository;

    @Transactional
    public TicketResponse createTicket(String username, CreateTicketRequest request) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Ticket ticket = Ticket.builder()
                .user(user)
                .deviceType(request.getDeviceType())
                .problemTitle(request.getProblemTitle())
                .description(request.getDescription())
                .status(TicketStatus.PENDING)
                .build();

        Ticket savedTicket = ticketRepository.save(ticket);
        return mapToResponse(savedTicket);
    }

    @Transactional(readOnly = true)
    public List<TicketResponse> getMyTickets(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        return ticketRepository.findByUser(user)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<TicketResponse> getAllTickets() {
        return ticketRepository.findAll()
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Transactional
    public TicketResponse updateStatus(Long id, TicketStatus status) {
        Ticket ticket = ticketRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Ticket not found"));

        ticket.setStatus(status);

        if (status == TicketStatus.RESOLVED) {
            ticket.setResolvedAt(LocalDateTime.now());
        } else {
            ticket.setResolvedAt(null);
        }

        Ticket savedTicket = ticketRepository.save(ticket);
        return mapToResponse(savedTicket);
    }

    private TicketResponse mapToResponse(Ticket ticket) {
        User user = ticket.getUser();

        return TicketResponse.builder()
                .id(ticket.getId())
                .username(user.getUsername())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .department(user.getDepartment())
                .officeNumber(user.getOfficeNumber())
                .floorNumber(user.getFloorNumber())
                .telephoneExtension(user.getTelephoneExtension())
                .building(user.getBuilding())
                .deviceType(ticket.getDeviceType())
                .problemTitle(ticket.getProblemTitle())
                .description(ticket.getDescription())
                .status(ticket.getStatus())
                .createdAt(ticket.getCreatedAt())
                .updatedAt(ticket.getUpdatedAt())
                .resolvedAt(ticket.getResolvedAt())
                .build();
    }
}