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
@Table(name = "submissions")
@Data
public class Submission {
  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID id;

  @Column(columnDefinition = "timestamptz", nullable = false)
  @Nonnull
  private OffsetDateTime submitTime;

  @Column(columnDefinition = "TEXT", nullable = true)
  private String description;

  @Column(columnDefinition = "TEXT", nullable = true)
  private String gitLink;

  @Column(columnDefinition = "TEXT", nullable = true)
  private String ytLink;

  @Column(columnDefinition = "TEXT", nullable = true)
  private String slideLink;

  @Column(columnDefinition = "TEXT", nullable = true)
  private String title;
}
