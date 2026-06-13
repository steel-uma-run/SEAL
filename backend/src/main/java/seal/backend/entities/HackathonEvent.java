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
import jakarta.persistence.Table;
import java.time.OffsetDateTime;
import java.util.UUID;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import seal.backend.enums.EventStatus;

@Entity
@Table(name = "seasons")
@Data
@NoArgsConstructor
@RequiredArgsConstructor
public class HackathonEvent {
  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID id;

  @Column(columnDefinition = "TEXT", nullable = false)
  @Nonnull
  private String name;

  @Column(columnDefinition = "TEXT", nullable = false)
  @Nonnull
  private String description;

  @Column(columnDefinition = "timestamptz", nullable = false)
  @Nonnull
  private OffsetDateTime startTime;

  @Column(columnDefinition = "timestamptz", nullable = false)
  @Nonnull
  private OffsetDateTime endTime;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  @Nonnull
  private EventStatus status;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "season_id", nullable = false)
  @Nonnull
  private Season season;
}
