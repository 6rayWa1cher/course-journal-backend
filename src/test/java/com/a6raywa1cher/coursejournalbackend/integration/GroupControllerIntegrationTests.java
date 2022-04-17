package com.a6raywa1cher.coursejournalbackend.integration;

import com.a6raywa1cher.coursejournalbackend.RequestContext;
import com.a6raywa1cher.coursejournalbackend.service.GroupService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ActiveProfiles;

@ActiveProfiles("test")
public class GroupControllerIntegrationTests extends AbstractIntegrationTests {
    @Autowired
    GroupService groupService;

}
