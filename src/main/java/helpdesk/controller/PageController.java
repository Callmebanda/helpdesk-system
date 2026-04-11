package helpdesk.controller;

import helpdesk.dto.AdminTicketResponse;
import helpdesk.dto.AdminTicketSummaryResponse;
import helpdesk.dto.TicketResponse;
import helpdesk.service.TicketService;
import helpdesk.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class PageController {

    private final TicketService ticketService;
    private final UserService userService;

    @GetMapping("/login")
    public String loginPage() {
        return "login";
    }

    @GetMapping("/dashboard")
    public String dashboard(Authentication authentication) {
        boolean isSupervisor = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("SUPERVISOR"));

        boolean isTechnician = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("TECHNICIAN"));

        if (isSupervisor) {
            return "redirect:/admin/dashboard";
        }

        if (isTechnician) {
            return "redirect:/tech/dashboard";
        }

        return "redirect:/user/dashboard";
    }

    @GetMapping("/user/dashboard")
    public String userDashboard(Authentication authentication, Model model) {
        String username = authentication.getName();
        List<TicketResponse> tickets = ticketService.getMyTickets(username);

        model.addAttribute("username", username);
        model.addAttribute("tickets", tickets);

        return "user-dashboard";
    }

    @GetMapping("/admin/dashboard")
    public String adminDashboard(Authentication authentication, Model model) {
        String username = authentication.getName();
        AdminTicketSummaryResponse summary = ticketService.getTicketSummary();
        List<AdminTicketResponse> tickets = ticketService.getAllTickets();

        model.addAttribute("username", username);
        model.addAttribute("summary", summary);
        model.addAttribute("tickets", tickets);

        return "admin-dashboard";
    }

    @GetMapping("/tech/dashboard")
    public String techDashboard(Authentication authentication, Model model) {
        String username = authentication.getName();
        List<AdminTicketResponse> tickets = ticketService.getAssignedTickets(username);

        model.addAttribute("username", username);
        model.addAttribute("tickets", tickets);

        return "tech-dashboard";
    }
}