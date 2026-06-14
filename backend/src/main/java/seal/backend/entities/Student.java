package seal.backend.entities;

import jakarta.annotation.Nonnull;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import java.util.UUID;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import seal.backend.enums.StudentStatus;
import seal.backend.enums.StudentType;

@Entity
@Table(name = "students")
@NoArgsConstructor
@RequiredArgsConstructor
@Data
public class Student {
  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID id;

  @OneToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "user_id", nullable = false)
  @Nonnull
  private User user;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  @Nonnull
  private StudentType studentType;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  @Nonnull
  private StudentStatus studentStatus;

  @Column(nullable = false)
  @Nonnull
  private String studentId;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "team_id", nullable = true)
  private Team team;
}
