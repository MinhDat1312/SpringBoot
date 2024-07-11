package vn.hoidanit.jobhunter.service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import vn.hoidanit.jobhunter.domain.Job;
import vn.hoidanit.jobhunter.domain.Skill;
import vn.hoidanit.jobhunter.domain.Subscriber;
import vn.hoidanit.jobhunter.domain.response.email.ResEmailJob;
import vn.hoidanit.jobhunter.repository.JobRepository;
import vn.hoidanit.jobhunter.repository.SkillRepository;
import vn.hoidanit.jobhunter.repository.SubscriberRepository;

@Service
public class SubscriberService {
    private SubscriberRepository subscriberRepository;
    private SkillRepository skillRepository;
    private JobRepository jobRepository;
    private EmailService emailService;

    public SubscriberService(SubscriberRepository subscriberRepository, SkillRepository skillRepository,
            JobRepository jobRepository, EmailService emailService) {
        this.subscriberRepository = subscriberRepository;
        this.skillRepository = skillRepository;
        this.jobRepository = jobRepository;
        this.emailService = emailService;
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

    public void sendSubscribersEmailJobs() {
        List<Subscriber> listSubs = this.subscriberRepository.findAll();
        if (listSubs != null && listSubs.size() > 0) {
            for (Subscriber sub : listSubs) {
                List<Skill> listSkills = sub.getSkills();
                if (listSkills != null && listSkills.size() > 0) {
                    List<Job> listJobs = this.jobRepository.findBySkillsIn(listSkills);
                    if (listJobs != null && listJobs.size() > 0) {
                        List<ResEmailJob> arr = listJobs.stream().map(
                                job -> this.convertJobToSendEmail(job)).collect(Collectors.toList());
                        this.emailService.sendEmailFromTemplateSync(
                                sub.getEmail(),
                                "Cơ hội việc làm hot đang chờ đón bạn, khám phá ngay",
                                "job",
                                sub.getName(),
                                arr);
                    }
                }
            }
        }
    }

    public ResEmailJob convertJobToSendEmail(Job job) {
        ResEmailJob res = new ResEmailJob();
        res.setName(job.getName());
        res.setSalary(job.getSalary());
        res.setCompany(new ResEmailJob.CompanyEmail(job.getCompany().getName()));
        List<Skill> skills = job.getSkills();
        List<ResEmailJob.SkillEmail> s = skills.stream().map(skill -> new ResEmailJob.SkillEmail(skill.getName()))
                .collect(Collectors.toList());
        res.setSkills(s);
        return res;
    }

    public Subscriber handleGetSubscribersSkill(String email) {
        return this.subscriberRepository.findByEmail(email);
    }
}
