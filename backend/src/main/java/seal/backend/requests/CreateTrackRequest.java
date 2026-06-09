package seal.backend.requests;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CreateTrackRequest(
    @NotNull(message = "Track's name cannot be null!")
        @NotBlank(message = "Track's name cannot be blank!")
        String name,
    @NotNull(message = "Track's description cannot be null!") String description) {}
