package seal.backend.requests;

import jakarta.validation.constraints.NotNull;
import java.time.OffsetDateTime;
import seal.backend.enums.Semester;

public record CreateSeasonRequest(
    @NotNull Semester semester,
    @NotNull Integer year,
    @NotNull OffsetDateTime startTime,
    @NotNull OffsetDateTime endTime) {}
