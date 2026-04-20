package helpdesk.repository;

import helpdesk.model.Ticket;
import helpdesk.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;

public interface TicketRepository extends JpaRepository<Ticket, Long>, JpaSpecificationExecutor<Ticket> {

    List<Ticket> findByUser(User user);

    List<Ticket> findByAssignedTechnician(User assignedTechnician);
}