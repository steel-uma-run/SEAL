package seal.backend.services;

import java.util.UUID;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import seal.backend.exceptions.StudentNotPendingException;
import seal.backend.exceptions.UserNotStudentException;
import seal.backend.responses.ProfileResponse;

public interface ProfileService {
  // I think it's reasonable to assume that this will never receive a
  // non-existent email?
  public ProfileResponse get(String email);

  public void approve(UUID id)
      throws UsernameNotFoundException, UserNotStudentException, StudentNotPendingException;
}
