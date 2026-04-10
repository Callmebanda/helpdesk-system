package helpdesk.repository;

import helpdesk.model.DeviceType;
import helpdesk.model.IssueCategory;
import helpdesk.model.KnowledgeArticle;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface KnowledgeArticleRepository extends JpaRepository<KnowledgeArticle, Long> {

    boolean existsBySourceTicketId(Long sourceTicketId);

    List<KnowledgeArticle> findAllByOrderByCreatedAtDesc();

    List<KnowledgeArticle> findByDeviceTypeOrderByCreatedAtDesc(DeviceType deviceType);

    List<KnowledgeArticle> findByIssueCategoryOrderByCreatedAtDesc(IssueCategory issueCategory);

    List<KnowledgeArticle> findByDeviceTypeAndIssueCategoryOrderByCreatedAtDesc(DeviceType deviceType,
                                                                                IssueCategory issueCategory);
}