package seal.backend.entities;

import jakarta.annotation.Nonnull;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import seal.openapi.model.CriteriaDto;
import seal.openapi.model.CriteriaTemplateDto;

@Entity
@Table(name = "criteria_templates")
@NoArgsConstructor
@RequiredArgsConstructor
@Data
public class CriteriaTemplate {
  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID id;

  @Column(columnDefinition = "TEXT", nullable = false)
  @Nonnull
  private String description;

  @OneToMany(mappedBy = "criteriaTemplate", fetch = FetchType.LAZY)
  private List<Criteria> criteria = new ArrayList<>();

  public CriteriaTemplateDto toDto() {
    return new CriteriaTemplateDto(
        id, description, criteria.stream().map(Criteria::toDto).toArray(CriteriaDto[]::new));
  }
}
