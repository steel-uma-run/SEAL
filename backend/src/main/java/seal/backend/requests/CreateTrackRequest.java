package seal.backend.requests;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CreateTrackRequest {
  @NotNull(message = "Track's name cannot be null!")
  @NotBlank(message = "Track's name cannot be blank!")
  private String name;

  private String description;
}
