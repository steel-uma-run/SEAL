package seal.backend.model;

import seal.backend.enums.Role;

public class Sysadmin extends Profile {
    public Sysadmin(String email, String password, String fullName) {
        super(email, password, fullName, Role.SYSADMIN);
    }
}
