package seal.backend.requests;

import jakarta.validation.constraints.NotNull;
import seal.backend.enums.Semester;

public record CreateSeasonRequest(@NotNull Semester semester, @NotNull Integer year) {}
