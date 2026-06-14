package seal.backend.requests;

import jakarta.validation.constraints.NotNull;
import java.time.OffsetDateTime;
import java.util.UUID;

public record CreateEventRequest(
    @NotNull String name,
    @NotNull String description,
    @NotNull OffsetDateTime startTime,
    @NotNull OffsetDateTime endTime,
    @NotNull UUID seasonId) {}
