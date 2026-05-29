package seal.backend.model;

import seal.backend.enums.Role;
import seal.backend.enums.Status;

public class Profile {
    private String email;
    private String password;
    private String fullName;
    private String studentCode;
    private StudentType studentType;
    private Role role;
    private Status status = Status.ACTIVE; // TODO: work on this

    public Profile() {
    }

    public Profile(String email, String password, String fullName, String studentCode, StudentType studentType) {
        this.email = email;
        this.password = password;
        this.fullName = fullName;
        this.studentCode = studentCode;
        this.studentType = studentType;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
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

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }
}
