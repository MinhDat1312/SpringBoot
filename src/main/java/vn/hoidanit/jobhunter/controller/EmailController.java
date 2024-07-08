package vn.hoidanit.jobhunter.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import vn.hoidanit.jobhunter.service.EmailService;

@RestController
@RequestMapping("/api/v1")
public class EmailController {
    private EmailService emailService;

    public EmailController(EmailService emailService) {
        this.emailService = emailService;
    }

    @GetMapping("/email")
    public String sendSimpleEmail() {
        // this.emailService.handleSendEmail();

        // this.emailService.sendEmailSync("nguyenthangdat84@gmail.com", "Send email
        // with template",
        // "<h1><b>MinhDat</b></h1>", false, true);

        this.emailService.sendEmailFromTemplateSync("nguyenthangdat84@gmail.com", "Send email with template", "job");
        return "ok";
    }
}
