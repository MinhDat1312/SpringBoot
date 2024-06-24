package vn.hoidanit.jobhunter.service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import vn.hoidanit.jobhunter.domain.Company;
import vn.hoidanit.jobhunter.domain.Job;
import vn.hoidanit.jobhunter.domain.Skill;
import vn.hoidanit.jobhunter.domain.response.ResultPaginationDTO;
import vn.hoidanit.jobhunter.domain.response.job.ResCreateJobDTO;
import vn.hoidanit.jobhunter.domain.response.job.ResUpdateJobDTO;
import vn.hoidanit.jobhunter.repository.CompanyRepository;
import vn.hoidanit.jobhunter.repository.JobRepository;
import vn.hoidanit.jobhunter.repository.SkillRepository;

@Service
public class JobService {
    private JobRepository jobRepository;
    private SkillRepository skillRepository;
    private CompanyRepository companyRepository;

    public JobService(JobRepository jobRepository, SkillRepository skillRepository,
            CompanyRepository companyRepository) {
        this.jobRepository = jobRepository;
        this.skillRepository = skillRepository;
        this.companyRepository = companyRepository;
    }

    public Job handleGetJobById(Long id) {
        Optional<Job> job = this.jobRepository.findById(id);

        return job.isPresent() ? job.get() : null;
    }

    public ResCreateJobDTO handleCreateJob(Job job) {
        if (job.getSkills() != null) {
            List<Long> idList = job.getSkills().stream().map(j -> j.getId()).collect(Collectors.toList());
            List<Skill> skills = this.skillRepository.findByIdIn(idList);

            job.setSkills(skills);
        }

        if (job.getCompany() != null) {
            Optional<Company> optional = this.companyRepository.findById(job.getCompany().getId());
            if (optional.isPresent()) {
                job.setCompany(optional.get());
            }
        }

        Job saveJob = this.jobRepository.save(job);

        ResCreateJobDTO resCreateJobDTO = new ResCreateJobDTO();
        resCreateJobDTO.setId(saveJob.getId());
        resCreateJobDTO.setName(saveJob.getName());
        resCreateJobDTO.setLocation(saveJob.getLocation());
        resCreateJobDTO.setSalary(saveJob.getSalary());
        resCreateJobDTO.setQuantity(saveJob.getQuantity());
        resCreateJobDTO.setLevel(saveJob.getLevel());
        resCreateJobDTO.setStartDate(saveJob.getStartDate());
        resCreateJobDTO.setEndDate(saveJob.getEndDate());
        resCreateJobDTO.setActive(saveJob.isActive());
        resCreateJobDTO.setCreatedAt(saveJob.getCreatedAt());
        resCreateJobDTO.setCreatedBy(saveJob.getCreatedBy());

        if (saveJob.getSkills() != null) {
            List<String> strSkills = saveJob.getSkills().stream().map(s -> s.getName()).collect(Collectors.toList());
            resCreateJobDTO.setSkills(strSkills);
        }

        return resCreateJobDTO;
    }

    public ResUpdateJobDTO handleUpdateJob(Job job) {
        Job currentJob = handleGetJobById(job.getId());
        if (job.getSkills() != null) {
            List<Long> idList = job.getSkills().stream().map(j -> j.getId()).collect(Collectors.toList());
            List<Skill> skills = this.skillRepository.findByIdIn(idList);

            currentJob.setSkills(skills);
        }

        if (job.getCompany() != null) {
            Optional<Company> optional = this.companyRepository.findById(job.getCompany().getId());
            if (optional.isPresent()) {
                currentJob.setCompany(optional.get());
            }
        }

        currentJob.setName(job.getName());
        currentJob.setSalary(job.getSalary());
        currentJob.setQuantity(job.getQuantity());
        currentJob.setLocation(job.getLocation());
        currentJob.setLevel(job.getLevel());
        currentJob.setStartDate(job.getStartDate());
        currentJob.setEndDate(job.getEndDate());
        currentJob.setActive(job.isActive());

        Job saveJob = this.jobRepository.save(currentJob);

        ResUpdateJobDTO resUpdateJobDTO = new ResUpdateJobDTO();
        resUpdateJobDTO.setId(saveJob.getId());
        resUpdateJobDTO.setName(saveJob.getName());
        resUpdateJobDTO.setLocation(saveJob.getLocation());
        resUpdateJobDTO.setSalary(saveJob.getSalary());
        resUpdateJobDTO.setQuantity(saveJob.getQuantity());
        resUpdateJobDTO.setLevel(saveJob.getLevel());
        resUpdateJobDTO.setStartDate(saveJob.getStartDate());
        resUpdateJobDTO.setEndDate(saveJob.getEndDate());
        resUpdateJobDTO.setActive(saveJob.isActive());
        resUpdateJobDTO.setUpdatedAt(saveJob.getUpdatedAt());
        resUpdateJobDTO.setUpdatedBy(saveJob.getUpdatedBy());

        if (saveJob.getSkills() != null) {
            List<String> strSkills = saveJob.getSkills().stream().map(s -> s.getName()).collect(Collectors.toList());
            resUpdateJobDTO.setSkills(strSkills);
        }

        return resUpdateJobDTO;
    }

    public void handleDeleteJob(long id) {
        this.jobRepository.deleteById(id);
    }

    public ResultPaginationDTO handleGetAllJobs(Specification<Job> spec, Pageable pageable) {
        Page<Job> page = this.jobRepository.findAll(spec, pageable);
        ResultPaginationDTO resultPaginationDTO = new ResultPaginationDTO();
        ResultPaginationDTO.Meta meta = new ResultPaginationDTO.Meta();

        meta.setPage(pageable.getPageNumber() + 1);
        meta.setPageSize(pageable.getPageSize());
        meta.setPages(page.getTotalPages());
        meta.setTotal(page.getTotalElements());

        resultPaginationDTO.setMeta(meta);
        resultPaginationDTO.setResult(page.getContent());

        return resultPaginationDTO;
    }
}
