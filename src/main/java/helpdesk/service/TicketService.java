package helpdesk.service;

import helpdesk.dto.*;
import helpdesk.model.ActivityType;
import helpdesk.model.DeviceType;
import helpdesk.model.IssueCategory;
import helpdesk.model.Role;
import helpdesk.model.Ticket;
import helpdesk.model.TicketPriority;
import helpdesk.model.TicketStatus;
import helpdesk.model.User;
import helpdesk.repository.TicketRepository;
import helpdesk.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import helpdesk.dto.UserTicketSummaryResponse;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class TicketService {

    private final TicketRepository ticketRepository;
    private final UserRepository userRepository;
    private final TicketActivityService ticketActivityService;

    @Transactional
    public TicketResponse createTicket(String username, CreateTicketRequest request) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        TicketPriority defaultPriority = TicketPriority.MEDIUM;
        LocalDateTime now = LocalDateTime.now();

        Ticket ticket = Ticket.builder()
                .user(user)
                .createdByUsername(username)
                .deviceType(request.getDeviceType())
                .issueCategory(request.getIssueCategory())
                .assetNumber(request.getAssetNumber())
                .problemTitle(request.getProblemTitle())
                .description(request.getDescription())
                .otherIssue(request.getOtherIssue())
                .priority(defaultPriority)
                .dueAt(calculateDueAt(defaultPriority, now))
                .status(TicketStatus.PENDING)
                .build();
        Ticket savedTicket = ticketRepository.save(ticket);

        ticketActivityService.logActivity(
                savedTicket,
                ActivityType.TICKET_CREATED,
                username,
                "Ticket created",
                true
        );

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
    public AdminTicketResponse updateStatus(Long id, TicketStatus status, String actorUsername) {
        Ticket ticket = ticketRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Ticket not found"));

        ticket.setStatus(status);

        if (status == TicketStatus.RESOLVED) {
            ticket.setResolvedAt(LocalDateTime.now());
        } else {
            ticket.setResolvedAt(null);
        }

        Ticket savedTicket = ticketRepository.save(ticket);

        ticketActivityService.logActivity(
                savedTicket,
                ActivityType.STATUS_CHANGED,
                actorUsername,
                "Status changed to " + status,
                true
        );

        return mapToAdminResponse(savedTicket);
    }

    @Transactional
    public AdminTicketResponse updateNotes(Long id,
                                           UpdateTicketNotesRequest request,
                                           String actorUsername) {
        Ticket ticket = ticketRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Ticket not found"));

        String oldResolutionNote = ticket.getResolutionNote();
        String oldInternalNote = ticket.getInternalNote();

        ticket.setResolutionNote(request.getResolutionNote());
        ticket.setInternalNote(request.getInternalNote());

        Ticket savedTicket = ticketRepository.save(ticket);

        if (!Objects.equals(oldResolutionNote, savedTicket.getResolutionNote())) {
            ticketActivityService.logActivity(
                    savedTicket,
                    ActivityType.RESOLUTION_NOTE_UPDATED,
                    actorUsername,
                    "Resolution note updated",
                    true
            );
        }

        if (!Objects.equals(oldInternalNote, savedTicket.getInternalNote())) {
            ticketActivityService.logActivity(
                    savedTicket,
                    ActivityType.INTERNAL_NOTE_UPDATED,
                    actorUsername,
                    "Internal note updated",
                    false
            );
        }

        return mapToAdminResponse(savedTicket);
    }

    @Transactional
    public AdminTicketResponse assignTicket(Long id,
                                            AssignTicketRequest request,
                                            String actorUsername) {
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

        ticketActivityService.logActivity(
                savedTicket,
                ActivityType.ASSIGNED,
                actorUsername,
                "Ticket assigned to technician " + technician.getUsername(),
                true
        );

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
    public AdminTicketResponse updateAssignedTicketStatus(Long id,
                                                          TicketStatus status,
                                                          String technicianUsername) {
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

        ticketActivityService.logActivity(
                savedTicket,
                ActivityType.STATUS_CHANGED,
                technicianUsername,
                "Status changed to " + status,
                true
        );

        return mapToAdminResponse(savedTicket);
    }

    @Transactional
    public AdminTicketResponse updateAssignedTicketNotes(Long id,
                                                         UpdateTicketNotesRequest request,
                                                         String technicianUsername) {
        Ticket ticket = ticketRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Ticket not found"));

        validateAssignedTechnician(ticket, technicianUsername);

        String oldResolutionNote = ticket.getResolutionNote();
        String oldInternalNote = ticket.getInternalNote();

        ticket.setResolutionNote(request.getResolutionNote());
        ticket.setInternalNote(request.getInternalNote());

        Ticket savedTicket = ticketRepository.save(ticket);

        if (!Objects.equals(oldResolutionNote, savedTicket.getResolutionNote())) {
            ticketActivityService.logActivity(
                    savedTicket,
                    ActivityType.RESOLUTION_NOTE_UPDATED,
                    technicianUsername,
                    "Resolution note updated",
                    true
            );
        }

        if (!Objects.equals(oldInternalNote, savedTicket.getInternalNote())) {
            ticketActivityService.logActivity(
                    savedTicket,
                    ActivityType.INTERNAL_NOTE_UPDATED,
                    technicianUsername,
                    "Internal note updated",
                    false
            );
        }

        return mapToAdminResponse(savedTicket);
    }

    @Transactional(readOnly = true)
    public TicketResponse getMyTicketById(Long id, String username) {
        Ticket ticket = ticketRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Ticket not found"));

        if (!ticket.getUser().getUsername().equals(username)) {
            throw new RuntimeException("You can only view your own tickets");
        }

        return mapToUserResponse(ticket);
    }

    @Transactional(readOnly = true)
    public AdminTicketResponse getTicketByIdForAdmin(Long id) {
        Ticket ticket = ticketRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Ticket not found"));

        return mapToAdminResponse(ticket);
    }

    @Transactional(readOnly = true)
    public AdminTicketResponse getAssignedTicketById(Long id, String technicianUsername) {
        Ticket ticket = ticketRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Ticket not found"));

        validateAssignedTechnician(ticket, technicianUsername);

        return mapToAdminResponse(ticket);
    }

    @Transactional(readOnly = true)
    public TechTicketSummaryResponse getTechnicianTicketSummary(String technicianUsername) {
        User technician = userRepository.findByUsername(technicianUsername)
                .orElseThrow(() -> new RuntimeException("Technician not found"));

        List<Ticket> tickets = ticketRepository.findByAssignedTechnician(technician);

        long totalAssigned = tickets.size();
        long pending = tickets.stream()
                .filter(ticket -> ticket.getStatus() == TicketStatus.PENDING)
                .count();
        long inProgress = tickets.stream()
                .filter(ticket -> ticket.getStatus() == TicketStatus.IN_PROGRESS)
                .count();
        long resolved = tickets.stream()
                .filter(ticket -> ticket.getStatus() == TicketStatus.RESOLVED)
                .count();
        long overdue = tickets.stream()
                .filter(this::isOverdue)
                .count();

        return TechTicketSummaryResponse.builder()
                .totalAssigned(totalAssigned)
                .pending(pending)
                .inProgress(inProgress)
                .resolved(resolved)
                .overdue(overdue)
                .build();
    }

    @Transactional(readOnly = true)
    public UserTicketSummaryResponse getUserTicketSummary(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        List<Ticket> tickets = ticketRepository.findByUser(user);

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
        long overdueTickets = tickets.stream()
                .filter(this::isOverdue)
                .count();

        return UserTicketSummaryResponse.builder()
                .totalTickets(totalTickets)
                .pendingTickets(pendingTickets)
                .inProgressTickets(inProgressTickets)
                .resolvedTickets(resolvedTickets)
                .overdueTickets(overdueTickets)
                .build();
    }

    @Transactional(readOnly = true)
    public List<TicketResponse> searchMyTickets(String username,
                                                TicketStatus status,
                                                DeviceType deviceType,
                                                TicketPriority priority,
                                                Boolean overdue) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        return ticketRepository.findByUser(user)
                .stream()
                .filter(ticket -> status == null || ticket.getStatus() == status)
                .filter(ticket -> deviceType == null || ticket.getDeviceType() == deviceType)
                .filter(ticket -> priority == null || ticket.getPriority() == priority)
                .filter(ticket -> overdue == null || isOverdue(ticket) == overdue)
                .map(this::mapToUserResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<AdminTicketResponse> searchAssignedTickets(String technicianUsername,
                                                           TicketStatus status,
                                                           TicketPriority priority,
                                                           Boolean overdue) {
        User technician = userRepository.findByUsername(technicianUsername)
                .orElseThrow(() -> new RuntimeException("Technician not found"));

        return ticketRepository.findByAssignedTechnician(technician)
                .stream()
                .filter(ticket -> status == null || ticket.getStatus() == status)
                .filter(ticket -> priority == null || ticket.getPriority() == priority)
                .filter(ticket -> overdue == null || isOverdue(ticket) == overdue)
                .map(this::mapToAdminResponse)
                .toList();
    }

    @Transactional
    public AdminTicketResponse updatePriority(Long id,
                                              UpdateTicketPriorityRequest request,
                                              String actorUsername) {
        Ticket ticket = ticketRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Ticket not found"));

        ticket.setPriority(request.getPriority());

        LocalDateTime baseTime = ticket.getCreatedAt() != null
                ? ticket.getCreatedAt()
                : LocalDateTime.now();

        ticket.setDueAt(calculateDueAt(request.getPriority(), baseTime));

        Ticket savedTicket = ticketRepository.save(ticket);

        ticketActivityService.logActivity(
                savedTicket,
                ActivityType.PRIORITY_CHANGED,
                actorUsername,
                "Priority changed to " + request.getPriority(),
                true
        );

        return mapToAdminResponse(savedTicket);
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
        long overdueTickets = tickets.stream()
                .filter(this::isOverdue)
                .count();

        return AdminTicketSummaryResponse.builder()
                .totalTickets(totalTickets)
                .pendingTickets(pendingTickets)
                .inProgressTickets(inProgressTickets)
                .resolvedTickets(resolvedTickets)
                .assignedTickets(assignedTickets)
                .unassignedTickets(unassignedTickets)
                .overdueTickets(overdueTickets)
                .build();
    }

    @Transactional(readOnly = true)
    public List<AdminTicketResponse> searchTickets(TicketStatus status,
                                                   DeviceType deviceType,
                                                   IssueCategory issueCategory,
                                                   TicketPriority priority,
                                                   String assignedTechnicianUsername,
                                                   String department,
                                                   Boolean overdue) {

        return ticketRepository.findAll(Sort.by(Sort.Direction.DESC, "createdAt"))
                .stream()
                .filter(ticket -> status == null || ticket.getStatus() == status)
                .filter(ticket -> deviceType == null || ticket.getDeviceType() == deviceType)
                .filter(ticket -> issueCategory == null || ticket.getIssueCategory() == issueCategory)
                .filter(ticket -> priority == null || ticket.getPriority() == priority)
                .filter(ticket -> assignedTechnicianUsername == null || assignedTechnicianUsername.isBlank()
                        || (ticket.getAssignedTechnician() != null
                        && ticket.getAssignedTechnician().getUsername().equalsIgnoreCase(assignedTechnicianUsername)))
                .filter(ticket -> department == null || department.isBlank()
                        || (ticket.getUser().getDepartment() != null
                        && ticket.getUser().getDepartment().equalsIgnoreCase(department)))
                .filter(ticket -> overdue == null || isOverdue(ticket) == overdue)
                .map(this::mapToAdminResponse)
                .toList();
    }

    @Transactional
    public AdminTicketResponse createTicketForUser(String actorUsername,
                                                   String targetUsername,
                                                   CreateTicketRequest request) {
        User targetUser = userRepository.findByUsername(targetUsername)
                .orElseThrow(() -> new RuntimeException("Target user not found"));

        TicketPriority defaultPriority = TicketPriority.MEDIUM;
        LocalDateTime now = LocalDateTime.now();

        Ticket ticket = Ticket.builder()
                .user(targetUser)
                .createdByUsername(actorUsername)
                .deviceType(request.getDeviceType())
                .issueCategory(request.getIssueCategory())
                .assetNumber(request.getAssetNumber())
                .problemTitle(request.getProblemTitle())
                .description(request.getDescription())
                .otherIssue(request.getOtherIssue())
                .priority(defaultPriority)
                .dueAt(calculateDueAt(defaultPriority, now))
                .status(TicketStatus.PENDING)
                .build();

        Ticket savedTicket = ticketRepository.save(ticket);

        ticketActivityService.logActivity(
                savedTicket,
                ActivityType.TICKET_CREATED,
                actorUsername,
                "Ticket created by " + actorUsername + " for user " + targetUsername,
                true
        );

        return mapToAdminResponse(savedTicket);
    }

    @Transactional(readOnly = true)
    public Page<AdminTicketResponse> searchTicketsPage(TicketStatus status,
                                                       DeviceType deviceType,
                                                       IssueCategory issueCategory,
                                                       TicketPriority priority,
                                                       String assignedTechnicianUsername,
                                                       String department,
                                                       Boolean overdue,
                                                       int page,
                                                       int size) {

        int safePage = Math.max(page, 0);
        int safeSize = size < 1 ? 10 : Math.min(size, 50);

        Pageable pageable = PageRequest.of(
                safePage,
                safeSize,
                Sort.by(Sort.Direction.DESC, "createdAt")
        );

        Specification<Ticket> specification = (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (status != null) {
                predicates.add(criteriaBuilder.equal(root.get("status"), status));
            }

            if (deviceType != null) {
                predicates.add(criteriaBuilder.equal(root.get("deviceType"), deviceType));
            }

            if (issueCategory != null) {
                predicates.add(criteriaBuilder.equal(root.get("issueCategory"), issueCategory));
            }

            if (priority != null) {
                predicates.add(criteriaBuilder.equal(root.get("priority"), priority));
            }

            if (assignedTechnicianUsername != null && !assignedTechnicianUsername.isBlank()) {
                Join<Ticket, User> technicianJoin = root.join("assignedTechnician", JoinType.LEFT);

                predicates.add(criteriaBuilder.equal(
                        criteriaBuilder.lower(technicianJoin.get("username")),
                        assignedTechnicianUsername.trim().toLowerCase()
                ));
            }

            if (department != null && !department.isBlank()) {
                Join<Ticket, User> userJoin = root.join("user", JoinType.LEFT);

                predicates.add(criteriaBuilder.equal(
                        criteriaBuilder.lower(userJoin.get("department")),
                        department.trim().toLowerCase()
                ));
            }

            if (overdue != null) {
                Predicate overduePredicate = criteriaBuilder.and(
                        criteriaBuilder.notEqual(root.get("status"), TicketStatus.RESOLVED),
                        criteriaBuilder.lessThan(root.get("dueAt"), LocalDateTime.now())
                );

                if (overdue) {
                    predicates.add(overduePredicate);
                } else {
                    predicates.add(criteriaBuilder.not(overduePredicate));
                }
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };

        return ticketRepository.findAll(specification, pageable)
                .map(this::mapToAdminResponse);
    }

    private void validateAssignedTechnician(Ticket ticket, String technicianUsername) {
        if (ticket.getAssignedTechnician() == null) {
            throw new RuntimeException("Ticket is not assigned to any technician");
        }

        if (!ticket.getAssignedTechnician().getUsername().equals(technicianUsername)) {
            throw new RuntimeException("You are not assigned to this ticket");
        }
    }

    private LocalDateTime calculateDueAt(TicketPriority priority, LocalDateTime baseTime) {
        return switch (priority) {
            case LOW -> baseTime.plusHours(72);
            case MEDIUM -> baseTime.plusHours(24);
            case HIGH -> baseTime.plusHours(8);
            case CRITICAL -> baseTime.plusHours(2);
        };
    }

    private boolean isOverdue(Ticket ticket) {
        return ticket.getStatus() != TicketStatus.RESOLVED
                && ticket.getDueAt() != null
                && ticket.getDueAt().isBefore(LocalDateTime.now());
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
                        ticket.getAssignedTechnician() != null
                                ? ticket.getAssignedTechnician().getUsername()
                                : null
                )
                .assignedAt(ticket.getAssignedAt())
                .status(ticket.getStatus())
                .createdAt(ticket.getCreatedAt())
                .updatedAt(ticket.getUpdatedAt())
                .resolvedAt(ticket.getResolvedAt())
                .priority(ticket.getPriority())
                .dueAt(ticket.getDueAt())
                .overdue(isOverdue(ticket))
                .createdByUsername(ticket.getCreatedByUsername())
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
                .priority(ticket.getPriority())
                .dueAt(ticket.getDueAt())
                .overdue(isOverdue(ticket))
                .createdByUsername(ticket.getCreatedByUsername())
                .build();
    }
}