package seal.backend.config;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;
import seal.backend.entities.User;
import seal.backend.enums.Role;
import seal.backend.repositories.StudentRepository;
import seal.backend.repositories.UserRepository;

@Component
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {
  private final UserRepository userRepository;
  private final StudentRepository studentRepository;

  @Override
  public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
    User user =
        userRepository
            .findByEmail(email)
            .orElseThrow(() -> new UsernameNotFoundException("This account does not exist."));

    switch (user.getRole()) {
      case Role.STUDENT -> user = studentRepository.findByEmail(email).get();
    }

    return new UserDetailsImpl(user);
  }
}
