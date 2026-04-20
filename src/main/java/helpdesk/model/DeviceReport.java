package helpdesk.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "device_reports")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DeviceReport {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "reported_by_user_id")
    private User reportedBy;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "device_id")
    private Device device;

    private String assetNumber;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DeviceReportType reportType;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String description;

    private String building;

    private String officeNumber;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DeviceReportStatus status;

    private String reviewedByUsername;

    @Column(columnDefinition = "TEXT")
    private String reviewNote;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    private LocalDateTime reviewedAt;

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();

        if (this.status == null) {
            this.status = DeviceReportStatus.PENDING;
        }
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}