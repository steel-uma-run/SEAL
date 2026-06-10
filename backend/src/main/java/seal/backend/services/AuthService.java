package seal.backend.services;

import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import seal.backend.entities.User;
import seal.openapi.model.LoginRequestPayload;
import seal.openapi.model.LoginResponsePayload;
import seal.openapi.model.RegisterRequestPayload;

@Service
public interface AuthService {
  public void register(RegisterRequestPayload request);

  public LoginResponsePayload login(LoginRequestPayload request);

  public User getCurrentUser(String email) throws UsernameNotFoundException;
}
