package seal.backend.services;

import org.springframework.security.core.Authentication;
import seal.openapi.model.UserDto;

public interface ProfileService {
  public UserDto getSelfProfile(Authentication auth);
}
