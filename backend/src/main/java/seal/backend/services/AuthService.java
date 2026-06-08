package seal.backend.services;

import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import seal.backend.entities.User;
import seal.backend.exceptions.EmailExistsException;
import seal.backend.requests.LoginRequest;
import seal.backend.requests.RegisterRequest;
import seal.backend.responses.LoginResponse;

@Service
public interface AuthService {
  public void register(RegisterRequest registerRequest) throws EmailExistsException;

  public LoginResponse login(LoginRequest request);

  public User getCurrentUser(String email) throws UsernameNotFoundException;
}
