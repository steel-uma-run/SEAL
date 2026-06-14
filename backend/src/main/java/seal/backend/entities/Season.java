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
import java.time.OffsetDateTime;
import java.util.UUID;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import seal.backend.enums.SeasonStatus;
import seal.backend.enums.Semester;

@Entity
@Table(name = "seasons")
@Getter
@Setter
@NoArgsConstructor
@RequiredArgsConstructor
public class Season {
  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID id;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  @Nonnull
  private Semester semester;

  @Column(nullable = false)
  @Nonnull
  private Integer year;

  @Column(columnDefinition = "timestamptz", nullable = false)
  @Nonnull
  private OffsetDateTime startTime;

  @Column(columnDefinition = "timestamptz", nullable = false)
  @Nonnull
  private OffsetDateTime endTime;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  @Nonnull
  private SeasonStatus status;
}
