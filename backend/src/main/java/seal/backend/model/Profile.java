package seal.backend.model;

public class Profile {
    private String email;
    private String password;
    private String fullName;
    private String studentCode;
    private String studentType;
    private String role = "student";
    private String status = "active";

    public Profile() {
    }

    public Profile(String email, String password, String fullName, String studentCode, String studentType) {
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

    public String getStudentType() {
        return studentType;
    }

    public void setStudentType(String studentType) {
        this.studentType = studentType;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}