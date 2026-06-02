package seal.backend.config;

import java.util.ArrayList;
import java.util.Collection;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import seal.backend.entities.Coordinator;
import seal.backend.entities.Student;
import seal.backend.entities.User;

public class UserDetailsImpl implements UserDetails {
  private User user;

  public UserDetailsImpl(User user) {
    this.user = user;
  }

  @Override
  public Collection<? extends GrantedAuthority> getAuthorities() {
    ArrayList<GrantedAuthority> authorities = new ArrayList<>();

    if (user instanceof Student) {
      authorities.add(new SimpleGrantedAuthority("STUDENT"));
    }

    if (user instanceof Coordinator) {
      authorities.add(new SimpleGrantedAuthority("COORDINATOR"));
    }

    return authorities;
  }

  @Override
  public String getPassword() {
    return user.getPasswordHash();
  }

  @Override
  public String getUsername() {
    return user.getEmail();
  }
}
