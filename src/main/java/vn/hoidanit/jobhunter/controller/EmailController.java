package vn.hoidanit.jobhunter.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import vn.hoidanit.jobhunter.service.EmailService;
import vn.hoidanit.jobhunter.service.SubscriberService;

@RestController
@RequestMapping("/api/v1")
public class EmailController {
    private EmailService emailService;
    private SubscriberService subscriberService;

    public EmailController(EmailService emailService, SubscriberService subscriberService) {
        this.emailService = emailService;
        this.subscriberService = subscriberService;
    }

    @GetMapping("/email")
    public String sendSimpleEmail() {
        // this.emailService.handleSendEmail();

        // this.emailService.sendEmailSync("nguyenthangdat84@gmail.com", "Send email
        // with template",
        // "<h1><b>MinhDat</b></h1>", false, true);

        this.subscriberService.sendSubscribersEmailJobs();
        return "ok";
    }
}
