package com.a6raywa1cher.coursejournalbackend.dto;

import com.a6raywa1cher.coursejournalbackend.model.AttendanceType;
import lombok.Data;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Data
public class TableDto {
    @Data
    public static class TableBodyElement {
        private Long studentId;
        private List<AttendanceType> attendances;
        private String studentName;
        private Long studentGroup;

        public TableBodyElement() {
            this.attendances = new ArrayList<>();
            this.studentId = null;
        }

        public TableBodyElement(Long studentId, List<AttendanceType> attendances, String studentName, Long studentGroup) {
            this.attendances = attendances;
            this.studentId = studentId;
            this.studentGroup = studentGroup;
            this.studentName = studentName;
        }
    }

    @Data
    static public class TableHeaderElement {
        private LocalDate date;
        private Integer classNumber;

        public TableHeaderElement() {
            this.date = null;
            this.classNumber = null;
        }

        public TableHeaderElement(LocalDate date, Integer classNumber) {
            this.classNumber = classNumber;
            this.date = date;
        }
    }

    private List<TableHeaderElement> header = new ArrayList<>();
    private List<TableBodyElement> body = new ArrayList<>();

    public void addTableHeaderElement(LocalDate date, Integer classNumber) {
        TableHeaderElement newHeaderElement = new TableHeaderElement();
        newHeaderElement.setDate(date);
        newHeaderElement.setClassNumber(classNumber);
        if (!header.contains(newHeaderElement)) {
            header.add(newHeaderElement);
        }
    }

    public void addTableBodyElement(long studentId, long attendanceLength, String studentName, long studentGroup) {
        TableBodyElement newBodyElement = new TableBodyElement();
        newBodyElement.setStudentId(studentId);
        newBodyElement.setStudentGroup(studentGroup);
        newBodyElement.setStudentName(studentName);
        List<AttendanceType> attendances = new ArrayList<>();
        for (int i = 0; i < attendanceLength; i++) {
            attendances.add(null);
        }
        newBodyElement.setAttendances(attendances);
        body.add(newBodyElement);
    }

    public void addAttendanceToBody(int studentIndex, int attendanceIndex, AttendanceType attendanceType) {
        body.get(studentIndex).attendances.set(attendanceIndex, attendanceType);
    }
}
