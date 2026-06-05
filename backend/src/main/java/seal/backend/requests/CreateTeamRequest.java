package seal.backend.requests;

import java.util.UUID;
import lombok.Data;

@Data
public class CreateTeamRequest {
  private String name;
  private String description;
  private UUID seasonId;
  private UUID leaderId;
}
