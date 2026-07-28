package seal.backend.entities;

import jakarta.annotation.Nonnull;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.util.UUID;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import seal.openapi.model.TeamTemplateDto;

@Entity
@Table(name = "team_templates")
@NoArgsConstructor
@RequiredArgsConstructor
@Data
public class TeamTemplate {
  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID id;

  @Column(nullable = false, columnDefinition = "TEXT")
  @Nonnull
  private String name;

  @Column(nullable = false, columnDefinition = "TEXT")
  @Nonnull
  private String description;

  public TeamTemplateDto toDto() {
    return new TeamTemplateDto(id, name, description);
  }
}
