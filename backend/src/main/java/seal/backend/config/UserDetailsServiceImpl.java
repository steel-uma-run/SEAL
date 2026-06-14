package seal.backend.config;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;
import seal.backend.entities.User;
import seal.backend.repositories.UserRepository;

@Component
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {
  private final UserRepository userRepository;

  @Override
  public UserDetails loadUserByUsername(String email) {
    User user =
        userRepository
            .findByEmail(email)
            .orElseThrow(() -> new UsernameNotFoundException("This account does not exist."));

    return new UserDetailsImpl(
        user,
        List.of("ROLE_" + user.getRole().name(), user.getRole().name()).stream()
            .map(SimpleGrantedAuthority::new)
            .toList());
  }
}
