package seal.backend.model;

import seal.backend.enums.Role;

public class Coordinator extends Profile {
    public Coordinator(String email, String password, String fullName) {
        super(email, password, fullName, Role.COORDINATOR);
    }
}
