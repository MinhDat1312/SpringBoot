package vn.hoidanit.jobhunter.service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import vn.hoidanit.jobhunter.domain.Skill;
import vn.hoidanit.jobhunter.domain.Subscriber;
import vn.hoidanit.jobhunter.repository.SkillRepository;
import vn.hoidanit.jobhunter.repository.SubscriberRepository;

@Service
public class SubscriberService {
    private SubscriberRepository subscriberRepository;
    private SkillRepository skillRepository;

    public SubscriberService(SubscriberRepository subscriberRepository, SkillRepository skillRepository) {
        this.subscriberRepository = subscriberRepository;
        this.skillRepository = skillRepository;
    }

    public Subscriber handleGetSubscriberByEmail(String email) {
        return this.subscriberRepository.findByEmail(email);
    }

    public Subscriber handleGetSubscriberById(long id) {
        Optional<Subscriber> optional = this.subscriberRepository.findById(id);
        return optional.isPresent() ? optional.get() : null;
    }

    public Subscriber handleCreateSubscriber(Subscriber subscriber) {
        if (subscriber.getSkills() != null) {
            List<Long> idSkills = subscriber.getSkills().stream().map(s -> s.getId()).collect(Collectors.toList());
            List<Skill> skills = this.skillRepository.findByIdIn(idSkills);
            subscriber.setSkills(skills);
        }
        return this.subscriberRepository.save(subscriber);
    }

    public Subscriber handleUpdateSubscriber(Subscriber subscriber) {
        Subscriber currentSubscriber = handleGetSubscriberById(subscriber.getId());

        if (subscriber.getSkills() != null) {
            List<Long> idSkills = subscriber.getSkills().stream().map(s -> s.getId()).collect(Collectors.toList());
            List<Skill> skills = this.skillRepository.findByIdIn(idSkills);
            subscriber.setSkills(skills);
        }

        currentSubscriber.setSkills(subscriber.getSkills());

        return this.subscriberRepository.save(currentSubscriber);
    }
}
