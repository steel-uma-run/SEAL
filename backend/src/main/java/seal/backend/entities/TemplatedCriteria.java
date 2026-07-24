package seal.backend.entities;

import jakarta.annotation.Nonnull;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.util.UUID;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;

// TemplatedCriteria: là criteria thuộc về 1 template nào đó

@Entity
@Table(name = "templated_criteria")
@NoArgsConstructor
@RequiredArgsConstructor
@Data
public class TemplatedCriteria {
  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID id;

  @Column(columnDefinition = "TEXT", nullable = false)
  @Nonnull
  private String name;

  @Column(columnDefinition = "TEXT", nullable = false)
  @Nonnull
  private String description;

  @Column(nullable = false)
  @Nonnull
  private Integer weight;

  @ManyToOne(optional = false)
  @Nonnull
  private CriteriaTemplate template;
}
