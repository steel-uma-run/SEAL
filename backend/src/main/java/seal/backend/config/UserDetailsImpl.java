package seal.backend.config;

import java.util.Collection;
import lombok.Data;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import seal.backend.entities.User;

@Data
public class UserDetailsImpl implements UserDetails {
  private final User user;
  private final Collection<? extends GrantedAuthority> authorities;

  @Override
  public String getPassword() {
    return user.getPasswordHash();
  }

  @Override
  public String getUsername() {
    return user.getEmail();
  }
}
