package helpdesk.service;

import helpdesk.dto.TicketActivityResponse;
import helpdesk.model.ActivityType;
import helpdesk.model.Ticket;
import helpdesk.model.TicketActivity;
import helpdesk.repository.TicketActivityRepository;
import helpdesk.repository.TicketRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TicketActivityService {

    private final TicketActivityRepository ticketActivityRepository;
    private final TicketRepository ticketRepository;

    @Transactional
    public void logActivity(Ticket ticket,
                            ActivityType activityType,
                            String actorUsername,
                            String description,
                            boolean visibleToUser) {

        TicketActivity activity = TicketActivity.builder()
                .ticket(ticket)
                .activityType(activityType)
                .actorUsername(actorUsername)
                .description(description)
                .visibleToUser(visibleToUser)
                .build();

        ticketActivityRepository.save(activity);
    }

    @Transactional(readOnly = true)
    public List<TicketActivityResponse> getAdminActivities(Long ticketId) {
        Ticket ticket = getTicket(ticketId);

        return ticketActivityRepository.findByTicketOrderByCreatedAtAsc(ticket)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<TicketActivityResponse> getUserVisibleActivities(Long ticketId, String username) {
        Ticket ticket = getTicket(ticketId);

        if (!ticket.getUser().getUsername().equals(username)) {
            throw new RuntimeException("You can only view activity for your own tickets");
        }

        return ticketActivityRepository.findByTicketAndVisibleToUserTrueOrderByCreatedAtAsc(ticket)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<TicketActivityResponse> getTechnicianActivities(Long ticketId, String technicianUsername) {
        Ticket ticket = getTicket(ticketId);

        if (ticket.getAssignedTechnician() == null) {
            throw new RuntimeException("Ticket is not assigned to any technician");
        }

        if (!ticket.getAssignedTechnician().getUsername().equals(technicianUsername)) {
            throw new RuntimeException("You are not assigned to this ticket");
        }

        return ticketActivityRepository.findByTicketOrderByCreatedAtAsc(ticket)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    private Ticket getTicket(Long ticketId) {
        return ticketRepository.findById(ticketId)
                .orElseThrow(() -> new RuntimeException("Ticket not found"));
    }

    private TicketActivityResponse mapToResponse(TicketActivity activity) {
        return TicketActivityResponse.builder()
                .id(activity.getId())
                .activityType(activity.getActivityType())
                .actorUsername(activity.getActorUsername())
                .description(activity.getDescription())
                .visibleToUser(activity.isVisibleToUser())
                .createdAt(activity.getCreatedAt())
                .build();
    }
}