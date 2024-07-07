package vn.hoidanit.jobhunter.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import vn.hoidanit.jobhunter.domain.Subscriber;
import vn.hoidanit.jobhunter.service.SubscriberService;
import vn.hoidanit.jobhunter.util.exception.IdInvalidException;

@RestController
@RequestMapping("/api/v1")
public class SubscriberController {
    private SubscriberService subscriberService;

    public SubscriberController(SubscriberService subscriberService) {
        this.subscriberService = subscriberService;
    }

    @PostMapping("/subscribers")
    public ResponseEntity<Subscriber> createSubscriber(@Valid @RequestBody Subscriber subscriber)
            throws IdInvalidException {
        if (this.subscriberService.handleGetSubscriberByEmail(subscriber.getEmail()) == null) {
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(this.subscriberService.handleCreateSubscriber(subscriber));
        } else {
            throw new IdInvalidException("Email exists");
        }
    }

    @PutMapping("/subscribers")
    public ResponseEntity<Subscriber> updateSubscriber(@RequestBody Subscriber subscriber)
            throws IdInvalidException {
        if (this.subscriberService.handleGetSubscriberById(subscriber.getId()) != null) {
            return ResponseEntity.ok()
                    .body(this.subscriberService.handleUpdateSubscriber(subscriber));
        } else {
            throw new IdInvalidException("Subscriber doesn't exist");
        }
    }
}
