package com.hirezen.service;

import com.hirezen.model.EmploymentType;
import com.hirezen.model.Job;
import com.hirezen.model.JobStatus;
import com.hirezen.model.User;
import com.hirezen.repository.JobRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class JobService {

    private final JobRepository jobRepository;

    @Transactional
    public Job postJob(User recruiter, String title, String description, String companyName,
                        String location, EmploymentType employmentType) {
        Job job = Job.builder()
                .title(title.trim())
                .description(description.trim())
                .companyName(companyName.trim())
                .location(location.trim())
                .employmentType(employmentType)
                .status(JobStatus.OPEN)
                .postedBy(recruiter)
                .build();

        Job saved = jobRepository.save(job);
        log.info("Job posted: '{}' at {} by {}", saved.getTitle(), saved.getCompanyName(), recruiter.getEmail());
        return saved;
    }

    /** All open jobs, newest first - what job seekers browse. */
    public List<Job> openJobs() {
        return jobRepository.findByStatusOrderByCreatedAtDesc(JobStatus.OPEN);
    }

    /** Every job a given recruiter has posted, regardless of status. */
    public List<Job> jobsPostedBy(User recruiter) {
        return jobRepository.findByPostedByOrderByCreatedAtDesc(recruiter);
    }

    public Job findById(Long id) {
        return jobRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Job not found: " + id));
    }

    /** Used by the navbar search - matches job title or company name. */
    public List<Job> search(String query) {
        return jobRepository.findByTitleContainingIgnoreCaseOrCompanyNameContainingIgnoreCase(query, query);
    }

    public long totalJobsBy(User recruiter) {
        return jobRepository.countByPostedBy(recruiter);
    }

    public long openJobsCountBy(User recruiter) {
        return jobRepository.countByPostedByAndStatus(recruiter, JobStatus.OPEN);
    }

    public long totalJobsCount() {
        return jobRepository.count();
    }

    public long totalOpenJobs() {
        return jobRepository.countByStatus(JobStatus.OPEN);
    }

    public long distinctCompanyCount() {
        return jobRepository.countDistinctCompanyName();
    }
}
