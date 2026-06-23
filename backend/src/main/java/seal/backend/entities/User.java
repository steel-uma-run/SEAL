package seal.backend.entities;

import jakarta.annotation.Nonnull;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Inheritance;
import jakarta.persistence.InheritanceType;
import java.util.UUID;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import seal.backend.enums.Role;
import seal.openapi.model.RoleDto;
import seal.openapi.model.UserDto;

@Entity
@Inheritance(strategy = InheritanceType.TABLE_PER_CLASS)
@NoArgsConstructor
@Getter
@Setter
@SuperBuilder
public abstract class User {
  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID id;

  @Column(nullable = false, columnDefinition = "TEXT")
  @Nonnull
  private String fullName;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  @Nonnull
  private Role role;

  @Column(nullable = false, columnDefinition = "TEXT", unique = true)
  @Nonnull
  private String email;

  @Column(nullable = false, columnDefinition = "TEXT")
  @Nonnull
  private String passwordHash;

  // Converts this User to an Object representing a DTO, depending on Role.
  //
  // Student converts into StudentDto, Lecturer and Coordinator converts to
  // UserDto. These are typecasted to Object.
  public Object toDto() {
    if (role == Role.STUDENT) {
      return ((Student) this).toDto();
    }

    return new UserDto(id, email, RoleDto.fromValue(role.name()), fullName);
  }
}
