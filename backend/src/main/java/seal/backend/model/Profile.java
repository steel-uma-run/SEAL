package seal.backend.model;

import seal.backend.enums.Role;
import seal.backend.enums.Status;

public class Profile {
    private String email;
    private String password;
    private String fullName;
    private Role role;
    private Status status = Status.ACTIVE; // TODO: work on this

    // This class is not intended to be instantiated by outsiders. Use
    // a specific inherited class instead (Student, Judge, etc.)
    protected Profile(String email, String password, String fullName, Role role) {
        this.email = email;
        this.password = password;
        this.fullName = fullName;
        this.role = role;
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
