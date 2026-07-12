package seal.backend.entities;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.ManyToMany;
import java.util.ArrayList;
import java.util.List;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Entity
@NoArgsConstructor
@SuperBuilder
@Getter
@Setter
public class Lecturer extends User {
  @ManyToMany(mappedBy = "mentors", fetch = FetchType.LAZY)
  @Builder.Default
  private List<Track> mentoredTracks = new ArrayList<>(); // for Mentor

  @ManyToMany(mappedBy = "judges", fetch = FetchType.LAZY)
  @Builder.Default
  private List<Track> judgedTracks = new ArrayList<>();
}
