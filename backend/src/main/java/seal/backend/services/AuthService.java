package seal.backend.services;

import org.springframework.stereotype.Service;
import seal.backend.exceptions.EmailExistsException;

@Service
public interface AuthService {
  public void register(String email, String name, String password, boolean isExternal)
      throws EmailExistsException;

  public String login(String email, String password);
}
