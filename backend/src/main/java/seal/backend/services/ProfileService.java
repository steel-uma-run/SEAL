package seal.backend.services;

import seal.backend.responses.ProfileResponse;

public interface ProfileService {
  // I think it's reasonable to assume that this will never receive a
  // non-existent email?
  public ProfileResponse get(String email);
}
