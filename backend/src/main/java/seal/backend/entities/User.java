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
import lombok.experimental.SuperBuilder;
import seal.backend.enums.Role;

@Entity
@Inheritance(strategy = InheritanceType.TABLE_PER_CLASS)
@NoArgsConstructor
@Getter
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
}
