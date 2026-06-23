package seal.backend.services;

import org.springframework.stereotype.Service;
import seal.openapi.model.LoginRequestPayloadDto;
import seal.openapi.model.LoginResponsePayloadDto;
import seal.openapi.model.RegisterRequestPayloadDto;

@Service
public interface AuthService {
  public void register(RegisterRequestPayloadDto request);

  public LoginResponsePayloadDto login(LoginRequestPayloadDto request);
}
