package com.a6raywa1cher.coursejournalbackend.dto;

import com.a6raywa1cher.coursejournalbackend.model.AttendanceType;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Data
public class AttendanceConflictListDto {
    @Data
    static public class AttendanceConflict {
        private String conflictedTeacherFullName;

        private String conflictedCourseName;

        private Long studentId;

        private LocalDate attendedDate;

        private Integer attendedClass;

        private AttendanceType attendanceType;

        public AttendanceConflict(String conflictedTeacherFullName, String conflictedCourseName, Long studentId,
                                  LocalDate attendedDate, Integer attendedClass, AttendanceType attendanceType) {
            this.attendanceType = attendanceType;
            this.attendedClass = attendedClass;
            this.attendedDate = attendedDate;
            this.conflictedTeacherFullName = conflictedTeacherFullName;
            this.studentId = studentId;
            this.conflictedCourseName = conflictedCourseName;
        }
    }

    private List<AttendanceConflict> conflicts = new ArrayList<>();

    public void addAttendanceConflictToList(AttendanceConflict attendanceConflict) {
        conflicts.add(attendanceConflict);
    }

    public AttendanceConflict get(int index) {
        return conflicts.get(index);
    }
}
