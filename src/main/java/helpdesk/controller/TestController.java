package helpdesk.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class TestController {

    @GetMapping("/user/ping")
    public String userPing() {
        return "User endpoint works";
    }

    @GetMapping("/admin/ping")
    public String adminPing() {
        return "Admin endpoint works";
    }
}