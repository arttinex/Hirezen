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

    /**
     * Updates an existing job's editable fields. Ownership (only the
     * recruiter who posted it can edit) is checked in JobController before
     * this is called, not here.
     */
    @Transactional
    public Job updateJob(Job job, String title, String description, String companyName,
                          String location, EmploymentType employmentType) {
        job.setTitle(title.trim());
        job.setDescription(description.trim());
        job.setCompanyName(companyName.trim());
        job.setLocation(location.trim());
        job.setEmploymentType(employmentType);
        return jobRepository.save(job);
    }

    /** Ownership is checked in JobController before this is called. */
    @Transactional
    public void deleteJob(Job job) {
        jobRepository.delete(job);
        log.info("Job deleted: '{}' at {}", job.getTitle(), job.getCompanyName());
    }

    /** Marks the job filled - it stops appearing in the seeker-facing Browse Jobs list (which only shows OPEN). */
    @Transactional
    public Job markAsHired(Job job) {
        job.setStatus(JobStatus.HIRED);
        return jobRepository.save(job);
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
