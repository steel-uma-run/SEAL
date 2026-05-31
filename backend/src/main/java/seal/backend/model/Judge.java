package seal.backend.model;

import seal.backend.enums.Role;

public class Judge extends User {
    public Judge(String email, String password, String fullName) {
        super(email, password, fullName, Role.JUDGE);
    }
}
