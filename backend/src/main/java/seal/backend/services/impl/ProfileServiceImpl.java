package seal.backend.services.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import seal.backend.entities.User;
import seal.backend.repositories.UserRepository;
import seal.backend.responses.ProfileResponse;
import seal.backend.services.ProfileService;

@Service
@RequiredArgsConstructor
public class ProfileServiceImpl implements ProfileService {
  private final UserRepository userRepository;

  public ProfileResponse get(String email) {
    User user = userRepository.findByEmail(email).get();
    ProfileResponse profile =
        new ProfileResponse(user.getEmail(), user.getFullName(), user.getRole());

    return profile;
  }
}
