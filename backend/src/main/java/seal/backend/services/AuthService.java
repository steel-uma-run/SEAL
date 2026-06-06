package seal.backend.services;

import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import seal.backend.entities.User;
import seal.backend.exceptions.EmailExistsException;
import seal.backend.requests.RegisterRequest;

@Service
public interface AuthService {
  public void register(RegisterRequest registerRequest) throws EmailExistsException;

  public String login(String email, String password);

  public User getCurrentUser(String email) throws UsernameNotFoundException;
}
