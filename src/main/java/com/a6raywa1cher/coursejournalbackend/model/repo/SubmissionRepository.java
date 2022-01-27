package com.a6raywa1cher.coursejournalbackend.model.repo;

import com.a6raywa1cher.coursejournalbackend.model.Submission;
import com.a6raywa1cher.coursejournalbackend.model.embed.SubmissionId;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

@RepositoryRestResource
public interface SubmissionRepository extends CrudRepository<Submission, SubmissionId> {
}