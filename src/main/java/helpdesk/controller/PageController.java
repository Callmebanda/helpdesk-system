package helpdesk.controller;

import helpdesk.dto.AdminTicketResponse;
import helpdesk.dto.AdminTicketSummaryResponse;
import helpdesk.dto.CreateTicketRequest;
import helpdesk.dto.TicketFormRequest;
import helpdesk.dto.TicketResponse;
import helpdesk.model.DeviceType;
import helpdesk.model.IssueCategory;
import helpdesk.service.TicketService;
import helpdesk.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import helpdesk.dto.TicketActivityResponse;
import helpdesk.service.TicketActivityService;
import java.util.List;

@Controller
@RequiredArgsConstructor
public class PageController {

    private final TicketService ticketService;
    private final UserService userService;
    private final TicketActivityService ticketActivityService;

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

    @GetMapping("/user/tickets/new")
    public String newTicketPage(Authentication authentication, Model model) {
        model.addAttribute("username", authentication.getName());
        model.addAttribute("ticketForm", new TicketFormRequest());
        model.addAttribute("deviceTypes", DeviceType.values());
        model.addAttribute("issueCategories", IssueCategory.values());

        return "user-ticket-form";
    }

    @GetMapping("/user/tickets/{id}")
    public String userTicketDetail(@PathVariable Long id,
                                   Authentication authentication,
                                   Model model) {
        String username = authentication.getName();

        TicketResponse ticket = ticketService.getMyTicketById(id, username);
        List<TicketActivityResponse> activities =
                ticketActivityService.getUserVisibleActivities(id, username);

        model.addAttribute("username", username);
        model.addAttribute("ticket", ticket);
        model.addAttribute("activities", activities);

        return "user-ticket-detail";
    }

    @PostMapping("/user/tickets/new")
    public String submitTicket(@Valid @ModelAttribute("ticketForm") TicketFormRequest ticketForm,
                               BindingResult bindingResult,
                               Authentication authentication,
                               Model model) {

        if (bindingResult.hasErrors()) {
            model.addAttribute("username", authentication.getName());
            model.addAttribute("deviceTypes", DeviceType.values());
            model.addAttribute("issueCategories", IssueCategory.values());
            return "user-ticket-form";
        }

        CreateTicketRequest request = new CreateTicketRequest();
        request.setDeviceType(ticketForm.getDeviceType());
        request.setIssueCategory(ticketForm.getIssueCategory());
        request.setAssetNumber(ticketForm.getAssetNumber());
        request.setProblemTitle(ticketForm.getProblemTitle());
        request.setDescription(ticketForm.getDescription());
        request.setOtherIssue(ticketForm.getOtherIssue());

        ticketService.createTicket(authentication.getName(), request);

        return "redirect:/user/dashboard";
    }
}