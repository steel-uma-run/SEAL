package seal.backend.entities;

import jakarta.annotation.Nonnull;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.util.UUID;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import seal.backend.enums.Role;

@Entity
@Table(name = "users")
@NoArgsConstructor
@RequiredArgsConstructor
@Data
public class User {
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
