package seal.backend.entities;

import jakarta.annotation.Nonnull;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;

@Entity
@Table(name = "tracks")
@NoArgsConstructor
@RequiredArgsConstructor
@Data
public class Track {
  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID id;

  @Column(nullable = false, columnDefinition = "TEXT")
  @Nonnull
  private String name;

  @Column(columnDefinition = "TEXT", nullable = false)
  @Nonnull
  private String description;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "event_id", nullable = false)
  @Nonnull
  private HackathonEvent event;

  // 1-1 hay 1-N?
  @OneToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "mentor_id", nullable = true)
  private Lecturer mentor;

  @OneToMany(mappedBy = "track", fetch = FetchType.LAZY)
  private List<Lecturer> judges = new ArrayList<>();
}
