package seal.backend.responses;

import java.util.UUID;
import lombok.Builder;

@Builder
public record CreateTrackResponse(UUID id, String name, String description, UUID seasonId) {}
