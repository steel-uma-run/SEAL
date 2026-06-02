package seal.backend.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.OffsetDateTime;

@Entity
@Table(name = "submissions")
public class Submission {
  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private java.util.UUID id;

  @Column(columnDefinition = "timestamptz", nullable = false)
  private OffsetDateTime submitTime;

  @Column(columnDefinition = "TEXT", nullable = true)
  private String description;

  @Column(columnDefinition = "TEXT", nullable = true)
  private String gitLink;

  @Column(columnDefinition = "TEXT", nullable = true)
  private String title;

  public java.util.UUID getId() {
    return id;
  }

  public void setId(java.util.UUID id) {
    this.id = id;
  }

  public OffsetDateTime getSubmitTime() {
    return submitTime;
  }

  public void setSubmitTime(OffsetDateTime submitTime) {
    this.submitTime = submitTime;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public String getGitLink() {
    return gitLink;
  }

  public void setGitLink(String gitLink) {
    this.gitLink = gitLink;
  }

  public String getTitle() {
    return title;
  }

  public void setTitle(String title) {
    this.title = title;
  }
}
