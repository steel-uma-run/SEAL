package seal.backend.entities;

import jakarta.annotation.Nonnull;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.OffsetDateTime;
import java.util.UUID;
import lombok.Data;

@Entity
@Table(name = "seasons")
@Data
public class Season {
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
}
