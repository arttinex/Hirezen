package com.hirezen.repository;

import com.hirezen.model.Application;
import com.hirezen.model.ApplicationStatus;
import com.hirezen.model.Job;
import com.hirezen.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ApplicationRepository extends JpaRepository<Application, Long> {

    Optional<Application> findByJobAndSeeker(Job job, User seeker);

    @Query("select a from Application a join fetch a.job where a.seeker = :seeker order by a.appliedAt desc")
    List<Application> findBySeekerOrderByAppliedAtDesc(@Param("seeker") User seeker);

    @Query("select a from Application a join fetch a.seeker where a.job = :job order by a.appliedAt desc")
    List<Application> findByJobOrderByAppliedAtDesc(@Param("job") Job job);

    long countByJob(Job job);

    long countBySeeker(User seeker);

    long countBySeekerAndStatus(User seeker, ApplicationStatus status);

    long countByJob_PostedBy(User postedBy);

    long countByJob_PostedByAndStatus(User postedBy, ApplicationStatus status);

    /** Used when a recruiter deletes a job - applications referencing it must go first to satisfy the FK constraint. */
    void deleteByJob(Job job);
}
