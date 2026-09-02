package com.hirezen.repository;

import com.hirezen.model.Job;
import com.hirezen.model.JobStatus;
import com.hirezen.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface JobRepository extends JpaRepository<Job, Long> {

    List<Job> findByStatusOrderByCreatedAtDesc(JobStatus status);

    List<Job> findByPostedByOrderByCreatedAtDesc(User postedBy);

    long countByPostedBy(User postedBy);

    long countByStatus(JobStatus status);

    long countByPostedByAndStatus(User postedBy, JobStatus status);

    @Query("select count(distinct j.companyName) from Job j")
    long countDistinctCompanyName();

    /** Powers the navbar search - matches title or company name. */
    List<Job> findByTitleContainingIgnoreCaseOrCompanyNameContainingIgnoreCase(String title, String companyName);
}
