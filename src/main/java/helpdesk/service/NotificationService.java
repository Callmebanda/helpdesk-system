package helpdesk.service;

import helpdesk.dto.NotificationResponse;
import helpdesk.model.*;
import helpdesk.repository.NotificationRepository;
import helpdesk.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;

    @Transactional
    public void notifySupervisorsDeviceReportSubmitted(DeviceReport report) {
        List<User> supervisors = userRepository.findByRoleAndEnabledTrue(Role.SUPERVISOR);

        for (User supervisor : supervisors) {
            createNotification(
                    supervisor,
                    NotificationType.DEVICE_REPORT_SUBMITTED,
                    "New device report submitted",
                    report.getReportedBy().getUsername() + " submitted a device report: " + report.getReportType(),
                    "/admin/device-reports"
            );
        }
    }

    @Transactional
    public void notifyUserDeviceReportReviewed(DeviceReport report) {
        String message = "Your device report #" + report.getId()
                + " was updated to " + report.getStatus();

        if (report.getReviewNote() != null && !report.getReviewNote().isBlank()) {
            message += ". Note: " + report.getReviewNote();
        }

        createNotification(
                report.getReportedBy(),
                NotificationType.DEVICE_REPORT_REVIEWED,
                "Device report updated",
                message,
                "/user/device-reports"
        );
    }

    @Transactional
    public void notifyTechnicianTicketAssigned(Ticket ticket) {
        if (ticket.getAssignedTechnician() == null) {
            return;
        }

        createNotification(
                ticket.getAssignedTechnician(),
                NotificationType.TICKET_ASSIGNED,
                "New ticket assignment",
                "Ticket #" + ticket.getId() + " (" + ticket.getProblemTitle() + ") was assigned to you.",
                "/tech/tickets/" + ticket.getId()
        );
    }

    @Transactional(readOnly = true)
    public List<NotificationResponse> getRecentUnreadNotifications(String username, int limit) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        return notificationRepository.findByRecipientAndReadFalseOrderByCreatedAtDesc(
                        user,
                        PageRequest.of(0, limit, Sort.by(Sort.Direction.DESC, "createdAt"))
                )
                .getContent()
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public long countUnreadNotifications(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        return notificationRepository.countByRecipientAndReadFalse(user);
    }

    @Transactional
    public void markAsRead(Long notificationId, String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Notification notification = notificationRepository.findByIdAndRecipient(notificationId, user)
                .orElseThrow(() -> new RuntimeException("Notification not found"));

        notification.setRead(true);
        notificationRepository.save(notification);
    }

    private void createNotification(User recipient,
                                    NotificationType type,
                                    String title,
                                    String message,
                                    String link) {
        Notification notification = Notification.builder()
                .recipient(recipient)
                .type(type)
                .title(title)
                .message(message)
                .link(link)
                .build();

        notificationRepository.save(notification);
    }

    private NotificationResponse mapToResponse(Notification notification) {
        return NotificationResponse.builder()
                .id(notification.getId())
                .type(notification.getType())
                .title(notification.getTitle())
                .message(notification.getMessage())
                .link(notification.getLink())
                .read(notification.isRead())
                .createdAt(notification.getCreatedAt())
                .build();
    }

    @Transactional
    public String openNotification(Long notificationId, String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Notification notification = notificationRepository.findByIdAndRecipient(notificationId, user)
                .orElseThrow(() -> new RuntimeException("Notification not found"));

        notification.setRead(true);
        notificationRepository.save(notification);

        if (notification.getLink() == null || notification.getLink().isBlank()) {
            return "/dashboard";
        }

        return notification.getLink();
    }
}