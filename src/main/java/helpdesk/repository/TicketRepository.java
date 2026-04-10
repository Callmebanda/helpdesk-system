package helpdesk.repository;

import helpdesk.model.Ticket;
import helpdesk.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TicketRepository extends JpaRepository<Ticket, Long> {
    List<Ticket> findByUser(User user);
    List<Ticket> findByAssignedTechnician(User assignedTechnician);
}