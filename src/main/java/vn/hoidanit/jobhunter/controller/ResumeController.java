package vn.hoidanit.jobhunter.controller;

import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.turkraft.springfilter.boot.Filter;
import com.turkraft.springfilter.builder.FilterBuilder;
import com.turkraft.springfilter.converter.FilterSpecificationConverter;

import jakarta.validation.Valid;
import vn.hoidanit.jobhunter.domain.Company;
import vn.hoidanit.jobhunter.domain.Job;
import vn.hoidanit.jobhunter.domain.Resume;
import vn.hoidanit.jobhunter.domain.User;
import vn.hoidanit.jobhunter.domain.response.ResultPaginationDTO;
import vn.hoidanit.jobhunter.domain.response.resume.ResCreateResumeDTO;
import vn.hoidanit.jobhunter.domain.response.resume.ResResume;
import vn.hoidanit.jobhunter.domain.response.resume.ResUpdateResumeDTO;
import vn.hoidanit.jobhunter.service.ResumeService;
import vn.hoidanit.jobhunter.service.UserService;
import vn.hoidanit.jobhunter.util.SecurityUtil;
import vn.hoidanit.jobhunter.util.exception.IdInvalidException;
import org.springframework.web.bind.annotation.GetMapping;

@RestController
@RequestMapping("/api/v1")
public class ResumeController {
    private ResumeService resumeService;
    private UserService userService;
    private FilterSpecificationConverter filterSpecificationConverter;
    private FilterBuilder filterBuilder;

    public ResumeController(ResumeService resumeService, UserService userService,
            FilterSpecificationConverter filterSpecificationConverter, FilterBuilder filterBuilder) {
        this.resumeService = resumeService;
        this.userService = userService;
        this.filterSpecificationConverter = filterSpecificationConverter;
        this.filterBuilder = filterBuilder;
    }

    @PostMapping("/resumes")
    public ResponseEntity<ResCreateResumeDTO> createResume(@Valid @RequestBody Resume resume)
            throws IdInvalidException {
        if (this.resumeService.checkResumeExistByUserAndJob(resume) == false) {
            throw new IdInvalidException("User id or Job id doesn't exist");
        } else {
            return ResponseEntity.status(HttpStatus.CREATED).body(this.resumeService.handleCreateResume(resume));
        }
    }

    @PutMapping("/resumes")
    public ResponseEntity<ResUpdateResumeDTO> updateResume(@RequestBody Resume resume) throws IdInvalidException {
        Resume currentResume = this.resumeService.handleGetResumeById(resume.getId());

        if (currentResume != null) {
            currentResume.setStatus(resume.getStatus());
            return ResponseEntity.ok().body(this.resumeService.handleUpdateResume(currentResume));
        } else {
            throw new IdInvalidException("Resume doesn't exist");
        }
    }

    @DeleteMapping("/resumes/{id}")
    public ResponseEntity<Void> deleteResume(@PathVariable("id") String id) throws IdInvalidException {
        if (Pattern.compile("^[0-9]+$").matcher(id).matches()) {
            Resume currentResume = this.resumeService.handleGetResumeById(Long.parseLong(id));

            if (currentResume != null) {
                this.resumeService.handleDeleteResume(Long.parseLong(id));
                return ResponseEntity.ok().body(null);
            } else {
                throw new IdInvalidException("Resume doesn't exist");
            }
        } else {
            throw new IdInvalidException("Id is number");
        }
    }

    @GetMapping("/resumes/{id}")
    public ResponseEntity<ResResume> getResumeById(@PathVariable("id") String id) throws IdInvalidException {
        if (Pattern.compile("^[0-9]+$").matcher(id).matches()) {
            Resume currentResume = this.resumeService.handleGetResumeById(Long.parseLong(id));

            if (currentResume != null) {
                return ResponseEntity.ok().body(this.resumeService.handleGetResume(currentResume));
            } else {
                throw new IdInvalidException("Resume doesn't exist");
            }
        } else {
            throw new IdInvalidException("Id is number");
        }
    }

    @GetMapping("/resumes")
    public ResponseEntity<ResultPaginationDTO> getAllResumes(@Filter Specification<Resume> spec, Pageable pageable) {
        List<Long> jobIds = null;

        String email = SecurityUtil.getCurrentUserLogin().isPresent() ? SecurityUtil.getCurrentUserLogin().get() : "";
        User currentUser = this.userService.handleGetUserByEmail(email);
        if (currentUser != null) {
            Company currentCompany = currentUser.getCompany();
            if (currentCompany != null) {
                List<Job> jobs = currentCompany.getJobs();
                if (jobs != null && jobs.size() > 0) {
                    jobIds = jobs.stream().map(j -> j.getId()).collect(Collectors.toList());
                }
            }
        }

        Specification<Resume> jobSpec = filterSpecificationConverter
                .convert(filterBuilder.field("job").in(filterBuilder.input(jobIds)).get());

        return ResponseEntity.ok().body(this.resumeService.handleGetAllResumes(jobSpec.and(spec), pageable));
    }

    @PostMapping("/resumes/by-user")
    public ResponseEntity<ResultPaginationDTO> getResumesByUser(Pageable pageable) {
        return ResponseEntity.ok().body(this.resumeService.handleGetResumesByUser(pageable));
    }
}
