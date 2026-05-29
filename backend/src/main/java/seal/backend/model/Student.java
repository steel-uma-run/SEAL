package seal.backend.model;

import seal.backend.enums.Role;
import seal.backend.enums.StudentType;

public class Student extends Profile {
    private String studentCode;
    private StudentType studentType;

    public Student(String email, String password, String fullName, String studentCode, StudentType studentType) {
        super(email, password, fullName, Role.STUDENT);
        this.studentCode = studentCode;
        this.studentType = studentType;
    }

    public String getStudentCode() {
        return studentCode;
    }

    public void setStudentCode(String studentCode) {
        this.studentCode = studentCode;
    }

    public StudentType getStudentType() {
        return studentType;
    }

    public void setStudentType(StudentType studentType) {
        this.studentType = studentType;
    }
}
