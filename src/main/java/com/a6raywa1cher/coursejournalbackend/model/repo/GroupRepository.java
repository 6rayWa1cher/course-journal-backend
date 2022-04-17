package com.a6raywa1cher.coursejournalbackend.model.repo;

import com.a6raywa1cher.coursejournalbackend.model.Group;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface GroupRepository extends CrudRepository<Group, Long> {
}
