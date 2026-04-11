package helpdesk.repository;

import helpdesk.model.Ticket;
import helpdesk.model.TicketActivity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TicketActivityRepository extends JpaRepository<TicketActivity, Long> {

    List<TicketActivity> findByTicketOrderByCreatedAtAsc(Ticket ticket);

    List<TicketActivity> findByTicketAndVisibleToUserTrueOrderByCreatedAtAsc(Ticket ticket);
}