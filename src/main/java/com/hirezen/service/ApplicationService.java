package com.hirezen.service;

import com.hirezen.model.Application;
import com.hirezen.model.ApplicationStatus;
import com.hirezen.model.Job;
import com.hirezen.model.User;
import com.hirezen.repository.ApplicationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ApplicationService {

    private final ApplicationRepository applicationRepository;

    @Transactional
    public Application apply(User seeker, Job job) {
        return applicationRepository.findByJobAndSeeker(job, seeker)
                .orElseGet(() -> {
                    Application saved = applicationRepository.save(Application.builder()
                            .job(job)
                            .seeker(seeker)
                            .status(ApplicationStatus.APPLIED)
                            .build());
                    log.info("{} applied to '{}' at {}", seeker.getEmail(), job.getTitle(), job.getCompanyName());
                    return saved;
                });
    }

    public List<Application> myApplications(User seeker) {
        return applicationRepository.findBySeekerOrderByAppliedAtDesc(seeker);
    }

    public List<Application> applicantsFor(Job job) {
        return applicationRepository.findByJobOrderByAppliedAtDesc(job);
    }

    public Set<Long> appliedJobIds(User seeker) {
        return applicationRepository.findBySeekerOrderByAppliedAtDesc(seeker).stream()
                .map(a -> a.getJob().getId())
                .collect(Collectors.toSet());
    }

    public long countForJob(Job job) {
        return applicationRepository.countByJob(job);
    }

    public long countForSeeker(User seeker) {
        return applicationRepository.countBySeeker(seeker);
    }

    public long shortlistedCountForSeeker(User seeker) {
        return applicationRepository.countBySeekerAndStatus(seeker, ApplicationStatus.SHORTLISTED);
    }

    public long rejectedCountForSeeker(User seeker) {
        return applicationRepository.countBySeekerAndStatus(seeker, ApplicationStatus.REJECTED);
    }

    public long applicantsCountForRecruiter(User recruiter) {
        return applicationRepository.countByJob_PostedBy(recruiter);
    }

    public long shortlistedCountForRecruiter(User recruiter) {
        return applicationRepository.countByJob_PostedByAndStatus(recruiter, ApplicationStatus.SHORTLISTED);
    }

    public long rejectedCountForRecruiter(User recruiter) {
        return applicationRepository.countByJob_PostedByAndStatus(recruiter, ApplicationStatus.REJECTED);
    }

    public long totalApplicationsCount() {
        return applicationRepository.count();
    }
}
