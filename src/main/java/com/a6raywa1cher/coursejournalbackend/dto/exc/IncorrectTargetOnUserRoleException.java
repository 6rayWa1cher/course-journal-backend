package com.a6raywa1cher.coursejournalbackend.dto.exc;

import com.a6raywa1cher.coursejournalbackend.model.IdEntity;
import com.a6raywa1cher.coursejournalbackend.model.UserRole;
import com.a6raywa1cher.coursejournalbackend.utils.CommonUtils;
import com.a6raywa1cher.coursejournalbackend.utils.EntityUtils;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class IncorrectTargetOnUserRoleException extends RuntimeException {
    public IncorrectTargetOnUserRoleException(UserRole userRole, IdEntity<Long> target, String reason) {
        super("Incorrect target %s=%s on user role %s: %s".formatted(
                CommonUtils.getSimpleClassName(target), EntityUtils.getId(target), userRole, reason
        ));
    }
}
