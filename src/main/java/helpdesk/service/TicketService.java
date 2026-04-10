package helpdesk.service;

import helpdesk.dto.AdminTicketResponse;
import helpdesk.dto.AssignTicketRequest;
import helpdesk.dto.CreateTicketRequest;
import helpdesk.dto.TicketResponse;
import helpdesk.dto.UpdateTicketNotesRequest;
import helpdesk.model.Role;
import helpdesk.model.Ticket;
import helpdesk.model.TicketStatus;
import helpdesk.model.User;
import helpdesk.repository.TicketRepository;
import helpdesk.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import helpdesk.dto.AdminTicketSummaryResponse;
import helpdesk.model.DeviceType;
import helpdesk.model.IssueCategory;
import org.springframework.data.domain.Sort;

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
                .issueCategory(request.getIssueCategory())
                .assetNumber(request.getAssetNumber())
                .problemTitle(request.getProblemTitle())
                .description(request.getDescription())
                .otherIssue(request.getOtherIssue())
                .status(TicketStatus.PENDING)
                .build();

        Ticket savedTicket = ticketRepository.save(ticket);
        return mapToUserResponse(savedTicket);
    }

    @Transactional(readOnly = true)
    public List<TicketResponse> getMyTickets(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        return ticketRepository.findByUser(user)
                .stream()
                .map(this::mapToUserResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<AdminTicketResponse> getAllTickets() {
        return ticketRepository.findAll()
                .stream()
                .map(this::mapToAdminResponse)
                .toList();
    }

    @Transactional
    public AdminTicketResponse updateStatus(Long id, TicketStatus status) {
        Ticket ticket = ticketRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Ticket not found"));

        ticket.setStatus(status);

        if (status == TicketStatus.RESOLVED) {
            ticket.setResolvedAt(LocalDateTime.now());
        } else {
            ticket.setResolvedAt(null);
        }

        Ticket savedTicket = ticketRepository.save(ticket);
        return mapToAdminResponse(savedTicket);
    }

    @Transactional
    public AdminTicketResponse updateNotes(Long id, UpdateTicketNotesRequest request) {
        Ticket ticket = ticketRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Ticket not found"));

        ticket.setResolutionNote(request.getResolutionNote());
        ticket.setInternalNote(request.getInternalNote());

        Ticket savedTicket = ticketRepository.save(ticket);
        return mapToAdminResponse(savedTicket);
    }

    @Transactional
    public AdminTicketResponse assignTicket(Long id, AssignTicketRequest request) {
        Ticket ticket = ticketRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Ticket not found"));

        User technician = userRepository.findByUsername(request.getTechnicianUsername())
                .orElseThrow(() -> new RuntimeException("Technician not found"));

        if (technician.getRole() != Role.TECHNICIAN) {
            throw new RuntimeException("Assigned user must have TECHNICIAN role");
        }

        if (!technician.isEnabled()) {
            throw new RuntimeException("Cannot assign ticket to a disabled technician");
        }

        ticket.setAssignedTechnician(technician);
        ticket.setAssignedAt(LocalDateTime.now());

        Ticket savedTicket = ticketRepository.save(ticket);
        return mapToAdminResponse(savedTicket);
    }

    @Transactional(readOnly = true)
    public List<AdminTicketResponse> getAssignedTickets(String technicianUsername) {
        User technician = userRepository.findByUsername(technicianUsername)
                .orElseThrow(() -> new RuntimeException("Technician not found"));

        return ticketRepository.findByAssignedTechnician(technician)
                .stream()
                .map(this::mapToAdminResponse)
                .toList();
    }

    @Transactional
    public AdminTicketResponse updateAssignedTicketStatus(Long id, TicketStatus status, String technicianUsername) {
        Ticket ticket = ticketRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Ticket not found"));

        validateAssignedTechnician(ticket, technicianUsername);

        ticket.setStatus(status);

        if (status == TicketStatus.RESOLVED) {
            ticket.setResolvedAt(LocalDateTime.now());
        } else {
            ticket.setResolvedAt(null);
        }

        Ticket savedTicket = ticketRepository.save(ticket);
        return mapToAdminResponse(savedTicket);
    }

    @Transactional
    public AdminTicketResponse updateAssignedTicketNotes(Long id,
                                                         UpdateTicketNotesRequest request,
                                                         String technicianUsername) {
        Ticket ticket = ticketRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Ticket not found"));

        validateAssignedTechnician(ticket, technicianUsername);

        ticket.setResolutionNote(request.getResolutionNote());
        ticket.setInternalNote(request.getInternalNote());

        Ticket savedTicket = ticketRepository.save(ticket);
        return mapToAdminResponse(savedTicket);
    }

    private void validateAssignedTechnician(Ticket ticket, String technicianUsername) {
        if (ticket.getAssignedTechnician() == null) {
            throw new RuntimeException("Ticket is not assigned to any technician");
        }

        if (!ticket.getAssignedTechnician().getUsername().equals(technicianUsername)) {
            throw new RuntimeException("You are not assigned to this ticket");
        }
    }

    private TicketResponse mapToUserResponse(Ticket ticket) {
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
                .issueCategory(ticket.getIssueCategory())
                .assetNumber(ticket.getAssetNumber())
                .problemTitle(ticket.getProblemTitle())
                .description(ticket.getDescription())
                .otherIssue(ticket.getOtherIssue())
                .resolutionNote(ticket.getResolutionNote())
                .assignedTechnicianUsername(
                        ticket.getAssignedTechnician() != null ? ticket.getAssignedTechnician().getUsername() : null
                )
                .assignedAt(ticket.getAssignedAt())
                .status(ticket.getStatus())
                .createdAt(ticket.getCreatedAt())
                .updatedAt(ticket.getUpdatedAt())
                .resolvedAt(ticket.getResolvedAt())
                .build();
    }

    private AdminTicketResponse mapToAdminResponse(Ticket ticket) {
        User user = ticket.getUser();
        User technician = ticket.getAssignedTechnician();

        return AdminTicketResponse.builder()
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
                .issueCategory(ticket.getIssueCategory())
                .assetNumber(ticket.getAssetNumber())
                .problemTitle(ticket.getProblemTitle())
                .description(ticket.getDescription())
                .otherIssue(ticket.getOtherIssue())
                .resolutionNote(ticket.getResolutionNote())
                .internalNote(ticket.getInternalNote())
                .assignedTechnicianUsername(technician != null ? technician.getUsername() : null)
                .assignedTechnicianFirstName(technician != null ? technician.getFirstName() : null)
                .assignedTechnicianLastName(technician != null ? technician.getLastName() : null)
                .assignedAt(ticket.getAssignedAt())
                .status(ticket.getStatus())
                .createdAt(ticket.getCreatedAt())
                .updatedAt(ticket.getUpdatedAt())
                .resolvedAt(ticket.getResolvedAt())
                .build();
    }
    @Transactional(readOnly = true)
    public AdminTicketSummaryResponse getTicketSummary() {
        List<Ticket> tickets = ticketRepository.findAll();

        long totalTickets = tickets.size();
        long pendingTickets = tickets.stream()
                .filter(ticket -> ticket.getStatus() == TicketStatus.PENDING)
                .count();
        long inProgressTickets = tickets.stream()
                .filter(ticket -> ticket.getStatus() == TicketStatus.IN_PROGRESS)
                .count();
        long resolvedTickets = tickets.stream()
                .filter(ticket -> ticket.getStatus() == TicketStatus.RESOLVED)
                .count();
        long assignedTickets = tickets.stream()
                .filter(ticket -> ticket.getAssignedTechnician() != null)
                .count();
        long unassignedTickets = tickets.stream()
                .filter(ticket -> ticket.getAssignedTechnician() == null)
                .count();

        return AdminTicketSummaryResponse.builder()
                .totalTickets(totalTickets)
                .pendingTickets(pendingTickets)
                .inProgressTickets(inProgressTickets)
                .resolvedTickets(resolvedTickets)
                .assignedTickets(assignedTickets)
                .unassignedTickets(unassignedTickets)
                .build();
    }

    @Transactional(readOnly = true)
    public List<AdminTicketResponse> searchTickets(TicketStatus status,
                                                   DeviceType deviceType,
                                                   IssueCategory issueCategory,
                                                   String assignedTechnicianUsername,
                                                   String department) {

        return ticketRepository.findAll(Sort.by(Sort.Direction.DESC, "createdAt"))
                .stream()
                .filter(ticket -> status == null || ticket.getStatus() == status)
                .filter(ticket -> deviceType == null || ticket.getDeviceType() == deviceType)
                .filter(ticket -> issueCategory == null || ticket.getIssueCategory() == issueCategory)
                .filter(ticket -> assignedTechnicianUsername == null || assignedTechnicianUsername.isBlank()
                        || (ticket.getAssignedTechnician() != null
                        && ticket.getAssignedTechnician().getUsername().equalsIgnoreCase(assignedTechnicianUsername)))
                .filter(ticket -> department == null || department.isBlank()
                        || (ticket.getUser().getDepartment() != null
                        && ticket.getUser().getDepartment().equalsIgnoreCase(department)))
                .map(this::mapToAdminResponse)
                .toList();
    }
}