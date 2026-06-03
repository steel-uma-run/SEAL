package seal.backend.entities;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;

import seal.backend.enums.Role;

@Entity
@Table(name = "coordinators")
public class Coordinator extends User {
    public Coordinator() {}

    public Coordinator(User user) {
        super(user.getFullName(), user.getEmail(), user.getPasswordHash());
        setRole(Role.COORDINATOR);
    }
}
