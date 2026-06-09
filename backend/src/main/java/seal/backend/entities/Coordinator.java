package seal.backend.entities;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import seal.backend.enums.Role;

@Entity
@Getter
@Setter
@Table(name = "coordinators")
public class Coordinator extends User {
  public Coordinator() {}

  public Coordinator(User user) {
    super();
    this.setFullName(user.getFullName());
    this.setEmail(user.getEmail());
    this.setPasswordHash(user.getPasswordHash());
    setRole(Role.COORDINATOR);
  }
}
