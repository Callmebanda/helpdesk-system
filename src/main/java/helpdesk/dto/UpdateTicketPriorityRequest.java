package helpdesk.dto;

import helpdesk.model.TicketPriority;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateTicketPriorityRequest {

    @NotNull
    private TicketPriority priority;
}