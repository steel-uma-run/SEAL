package seal.backend.model;

import seal.backend.enums.Role;

public class Sysadmin extends User {
    public Sysadmin(String email, String password, String fullName) {
        super(email, password, fullName, Role.SYSADMIN);
    }
}
