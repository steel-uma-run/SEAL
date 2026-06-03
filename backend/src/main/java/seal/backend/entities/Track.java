package seal.backend.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "tracks")
public class Track {
  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID id;

  @Column(columnDefinition = "timestamptz", nullable = false)
  private OffsetDateTime startTime;

  @Column(columnDefinition = "timestamptz", nullable = false)
  private OffsetDateTime endTime;

  @Column(columnDefinition = "TEXT", nullable = true)
  private String description;

  public UUID getId() {
    return id;
  }

  public OffsetDateTime getStartTime() {
    return startTime;
  }

  public void setStartTime(OffsetDateTime startTime) {
    this.startTime = startTime;
  }

  public OffsetDateTime getEndTime() {
    return endTime;
  }

  public void setEndTime(OffsetDateTime endTime) {
    this.endTime = endTime;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }
}
