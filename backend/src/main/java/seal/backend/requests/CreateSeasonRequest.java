package seal.backend.requests;

import jakarta.validation.constraints.NotNull;
import java.time.OffsetDateTime;

public record CreateSeasonRequest(
    @NotNull String description,
    @NotNull OffsetDateTime startTime,
    @NotNull OffsetDateTime endTime) {}
