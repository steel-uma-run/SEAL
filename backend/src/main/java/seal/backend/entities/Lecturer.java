package seal.backend.entities;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import java.util.ArrayList;
import java.util.List;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Entity
@NoArgsConstructor
@SuperBuilder
public class Lecturer extends User {
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "track_id", nullable = true)
  private Track track; // for Judge

  @ManyToMany(mappedBy = "mentors", fetch = FetchType.LAZY)
  @Builder.Default
  private List<Track> mentoredTracks = new ArrayList<>(); // for Mentor
}
