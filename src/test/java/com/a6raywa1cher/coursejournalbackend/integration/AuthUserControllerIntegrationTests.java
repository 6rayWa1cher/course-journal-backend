//package com.a6raywa1cher.coursejournalbackend.integration;
//
//import com.a6raywa1cher.coursejournalbackend.RequestContext;
//import com.a6raywa1cher.coursejournalbackend.model.UserRole;
//import com.a6raywa1cher.coursejournalbackend.service.StudentService;
//import com.fasterxml.jackson.databind.node.ObjectNode;
//import org.junit.jupiter.api.Test;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.test.context.ActiveProfiles;
//
//@ActiveProfiles("test")
//public class AuthUserControllerIntegrationTests extends AbstractIntegrationTests {
//    @Autowired
//    StudentService studentService;
//
//    RequestContext<Long> createGetAuthUserListContext() {
//        String username = faker.name().username();
//        String
//    }
//
//    @Test
//    void getAuthUserList__admin__valid() {
//
//    }
//
//    @Test
//    void getAuthUserList__notAdmin__invalid() {
//
//    }
//
//    @Test
//    void getAuthUserList__notAuthenticated__invalid() throws Exception {
//
//    }
//
//    // ================================================================================================================
//
//    RequestContext<Long> createGetAuthUserByIdContext() {
//
//    }
//
//    @Test
//    void getAuthUserById__self__valid() {
//
//    }
//
//    @Test
//    void getAuthUserById__otherAsAdmin__valid() {
//
//    }
//
//    @Test
//    void getAuthUserById__otherAsTeacher__invalid() {
//
//    }
//
//    @Test
//    void getAuthUserById__withCourseToken__invalid() {
//
//    }
//
//    @Test
//    void getAuthUserById__notAuthenticated__invalid() throws Exception {
//
//    }
//
//    // ================================================================================================================
//
//    RequestContext<Long> createGetAuthUserByEmployeeIdContext() {
//
//    }
//
//    @Test
//    void getAuthUserByEmployeeId__self__valid() {
//
//    }
//
//    @Test
//    void getAuthUserByEmployeeId__otherAsAdmin__valid() {
//
//    }
//
//    @Test
//    void getAuthUserByEmployeeId__otherAsTeacher__invalid() {
//
//    }
//
//    @Test
//    void getAuthUserByEmployeeId__withCourseToken__invalid() {
//
//    }
//
//    @Test
//    void getAuthUserByEmployeeId__notAuthenticated__invalid() throws Exception {
//
//    }
//
//    // ================================================================================================================
//
//    RequestContext<Long> createGetAuthUserByStudentIdContext() {
//
//    }
//
//    @Test
//    void getAuthUserByStudentId__self__valid() {
//
//    }
//
//    @Test
//    void getAuthUserByStudentId__otherAsAdmin__valid() {
//
//    }
//
//    @Test
//    void getAuthUserByStudentId__otherAsTeacher__invalid() {
//
//    }
//
//    @Test
//    void getAuthUserByStudentId__withCourseToken__invalid() {
//
//    }
//
//    @Test
//    void getAuthUserByStudentId__notAuthenticated__invalid() throws Exception {
//
//    }
//
//    // ================================================================================================================
//
//    RequestContext<ObjectNode> getCreateAuthUserRequest(UserRole userRole, long employeeId, long studentId) {
//
//    }
//
//    @Test
//    void createAuthUser__admin__asAdmin__valid() {}
//
//    @Test
//    void createAuthUser__teacher__asAdmin__valid() {}
//
//    @Test
//    void createAuthUser__headman__asAdmin__valid() {}
//
//    @Test
//    void createAuthUser__notAdmin__invalid() {}
//
//    @Test
//    void createAuthUser__studentConflict__invalid() {}
//
//    @Test
//    void createAuthUser__employeeConflict__invalid() {}
//
//    @Test
//    void createAuthUser__usernameConflict__invalid() {}
//
//    @Test
//    void createAuthUser__admin__withTarget__invalid() {}
//
//    @Test
//    void createAuthUser__teacher__withInvalidTarget__invalid() {}
//
//    @Test
//    void createAuthUser__headman__withInvalidTarget__invalid() {}
//
//    @Test
//    void createAuthUser__notFound__invalid() {}
//
//    @Test
//    void createAuthUser__notAuthenticated__invalid() throws Exception {
//
//    }
//
//    // ================================================================================================================
//
//    RequestContext<ObjectNode> getUpdateAuthUserRequest(UserRole userRole, long employeeId, long studentId) {
//
//    }
//
//    @Test
//    void updateAuthUser__self__valid() {
//
//    }
//
//    @Test
//    void updateAuthUser__otherAsAdmin__valid() {
//
//    }
//
//    @Test
//    void updateAuthUser__otherAsTeacher__invalid() {
//
//    }
//
//    @Test
//    void updateAuthUser__withCourseToken__invalid() {
//
//    }
//
//    @Test
//    void updateAuthUser__roleChange__invalid() {
//
//    }
//
//    @Test
//    void updateAuthUser__targetChange__invalid() {
//
//    }
//
//    @Test
//    void updateAuthUser__usernameConflict__invalid() {}
//
//    @Test
//    void updateAuthUser__notFound__invalid() {}
//
//    @Test
//    void updateAuthUser__notAuthenticated__invalid() throws Exception {
//
//    }
//
//    // ================================================================================================================
//
//    RequestContext<ObjectNode> getPatchAuthUserRequest(UserRole userRole, long employeeId, long studentId) {
//
//    }
//
//    @Test
//    void patchAuthUser__self__valid() {
//
//    }
//
//    @Test
//    void patchAuthUser__otherAsAdmin__valid() {
//
//    }
//
//    @Test
//    void patchAuthUser__otherAsTeacher__invalid() {
//
//    }
//
//    @Test
//    void patchAuthUser__withCourseToken__invalid() {
//
//    }
//
//    @Test
//    void patchAuthUser__roleChange__invalid() {
//
//    }
//
//    @Test
//    void patchAuthUser__targetChange__invalid() {
//
//    }
//
//    @Test
//    void patchAuthUser__usernameConflict__invalid() {}
//
//    @Test
//    void patchAuthUser__notFound__invalid() {}
//
//    @Test
//    void patchAuthUser__notAuthenticated__invalid() throws Exception {
//
//    }
//
//    // ================================================================================================================
//
//    @Test
//    void deleteAuthUser__self__valid() {
//
//    }
//
//    @Test
//    void deleteAuthUser__otherAsAdmin__valid() {
//
//    }
//
//    @Test
//    void deleteAuthUser__otherAsTeacher__invalid() {
//
//    }
//
//    @Test
//    void deleteAuthUser__withCourseToken__invalid() {
//
//    }
//
//    @Test
//    void deleteAuthUser__notFound__invalid() {}
//
//    @Test
//    void deleteAuthUser__notAuthenticated__invalid() throws Exception {
//
//    }
//}
