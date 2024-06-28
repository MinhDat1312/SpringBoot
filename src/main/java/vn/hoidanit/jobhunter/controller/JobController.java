package vn.hoidanit.jobhunter.controller;

import java.util.regex.Pattern;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.turkraft.springfilter.boot.Filter;

import jakarta.validation.Valid;
import vn.hoidanit.jobhunter.domain.Job;
import vn.hoidanit.jobhunter.domain.response.ResultPaginationDTO;
import vn.hoidanit.jobhunter.domain.response.job.ResCreateJobDTO;
import vn.hoidanit.jobhunter.domain.response.job.ResUpdateJobDTO;
import vn.hoidanit.jobhunter.service.JobService;
import vn.hoidanit.jobhunter.util.exception.IdInvalidException;

@RestController
@RequestMapping("/api/v1")
public class JobController {
    private JobService jobService;

    public JobController(JobService jobService) {
        this.jobService = jobService;
    }

    @PostMapping("/jobs")
    public ResponseEntity<ResCreateJobDTO> createJob(@Valid @RequestBody Job job) {
        return ResponseEntity.status(HttpStatus.CREATED).body(this.jobService.handleCreateJob(job));
    }

    @PutMapping("/jobs")
    public ResponseEntity<ResUpdateJobDTO> updateJob(@Valid @RequestBody Job job) throws IdInvalidException {
        if (this.jobService.handleGetJobById(job.getId()) != null) {
            return ResponseEntity.ok().body(this.jobService.handleUpdateJob(job));
        } else {
            throw new IdInvalidException("Job doesn't exist");
        }
    }

    @DeleteMapping("/jobs/{id}")
    public ResponseEntity<Void> deleteJob(@PathVariable("id") String id) throws IdInvalidException {
        if (Pattern.compile("^[0-9]+$").matcher(id).matches()) {
            if (this.jobService.handleGetJobById(Long.parseLong(id)) != null) {
                this.jobService.handleDeleteJob(Long.parseLong(id));
                return ResponseEntity.ok().body(null);
            } else {
                throw new IdInvalidException("Job doesn't exist");
            }
        } else {
            throw new IdInvalidException("Id is number");
        }
    }

    @GetMapping("/jobs")
    public ResponseEntity<ResultPaginationDTO> getAllJobs(@Filter Specification<Job> spec, Pageable pageable) {
        return ResponseEntity.ok().body(this.jobService.handleGetAllJobs(spec, pageable));
    }

    @GetMapping("/jobs/{id}")
    public ResponseEntity<Job> getJob(@PathVariable("id") long id) throws IdInvalidException {
        Job currentJob = this.jobService.handleGetJobById(id);
        if (currentJob == null) {
            throw new IdInvalidException("Job doesn't exist");
        }

        return ResponseEntity.ok().body(currentJob);
    }

}
