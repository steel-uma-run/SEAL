package seal.backend.entities;

import jakarta.annotation.Nonnull;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.Table;
import java.util.Set;
import java.util.UUID;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;

@Entity
@Table(name = "criteria")
@NoArgsConstructor
@RequiredArgsConstructor
@Data
public class Criteria {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID id;

  @Column(columnDefinition = "TEXT", nullable = false)
  @Nonnull
  private String name;

  @Column(nullable = false)
  private Integer weight;

  @ManyToMany(fetch = FetchType.LAZY)
  @JoinTable(
      name = "criteria_criteria_template",
      joinColumns = @JoinColumn(name = "criteria_id", nullable = false),
      inverseJoinColumns = @JoinColumn(name = "criteria_template_id", nullable = false))
  private Set<CriteriaTemplate> criteriaTemplates;
}
