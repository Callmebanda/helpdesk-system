package helpdesk.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "knowledge_articles")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class KnowledgeArticle {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true)
    private Long sourceTicketId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DeviceType deviceType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private IssueCategory issueCategory;

    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String symptoms;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String solution;

    @Column(nullable = false)
    private String publishedBy;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}