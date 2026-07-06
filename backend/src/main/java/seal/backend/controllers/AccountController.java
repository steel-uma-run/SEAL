package seal.backend.controllers;

import jakarta.validation.constraints.NotNull;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import seal.backend.config.GlobalConfig;
import seal.backend.entities.User;
import seal.backend.enums.Role;
import seal.backend.repositories.StudentRepository;
import seal.backend.repositories.UserRepository;
import seal.backend.services.AccountService;
import seal.openapi.api.AccountApi;
import seal.openapi.model.UserDto;
import seal.openapi.model.UserRoleDto;
import seal.openapi.model.UserStatusDto;

@RestController
@RequiredArgsConstructor
@RequestMapping(GlobalConfig.API_BASE)
public class AccountController implements AccountApi {
  private final AccountService accountService;
  private final UserRepository userRepository;
  private final StudentRepository studentRepository;

  @Override
  @PreAuthorize("hasRole('COORDINATOR')")
  public ResponseEntity<UserDto[]> getAllAccounts() {
    List<User> users = userRepository.findAll();

    UserDto[] dtoArr =
        users.stream()
            .map(
                user -> {
                  UserStatusDto status = null;
                  if (user.getRole() == Role.STUDENT) {
                    status =
                        studentRepository
                            .findByUser(user)
                            .map(
                                student ->
                                    UserStatusDto.fromValue(student.getStudentStatus().name()))
                            .orElse(null);
                  }
                  return new UserDto(
                      user.getId(),
                      user.getEmail(),
                      user.getFullName(),
                      UserRoleDto.fromValue(user.getRole().name()),
                      status);
                })
            .toArray(UserDto[]::new);

    return ResponseEntity.ok(dtoArr);
  }

  @Override
  @PreAuthorize("hasRole('COORDINATOR')")
  public ResponseEntity<Void> approveAccount(
      @PathVariable(name = "accountId") @NotNull UUID accountId) {
    accountService.approve(accountId);
    return ResponseEntity.ok().build();
  }
}
