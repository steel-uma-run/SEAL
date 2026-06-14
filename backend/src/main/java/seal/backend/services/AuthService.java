package seal.backend.services;

import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import seal.backend.entities.User;
import seal.openapi.model.LoginRequestPayloadDto;
import seal.openapi.model.LoginResponsePayloadDto;
import seal.openapi.model.RegisterRequestPayloadDto;

@Service
public interface AuthService {
  public void register(RegisterRequestPayloadDto request);

  public LoginResponsePayloadDto login(LoginRequestPayloadDto request);

  public User getCurrentUser(String email) throws UsernameNotFoundException;
}
