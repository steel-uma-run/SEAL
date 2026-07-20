package seal.backend.controllers;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import seal.backend.config.GlobalConfig;
import seal.backend.services.AccountService;
import seal.openapi.api.AccountsApi;
import seal.openapi.model.CreateLecturerRequestDto;
import seal.openapi.model.StudentDto;
import seal.openapi.model.UserDto;

@RestController
@RequiredArgsConstructor
@RequestMapping(GlobalConfig.API_BASE)
public class AccountController implements AccountsApi {
  private final AccountService accountService;

  @Override
  @PreAuthorize("hasAnyRole('COORDINATOR', 'STUDENT')")
  public ResponseEntity<Object[]> getAllAccounts() {
    return ResponseEntity.ok(accountService.getAllAccounts());
  }

  @Override
  @PreAuthorize("hasRole('COORDINATOR')")
  public ResponseEntity<Void> approveAccount(
      @PathVariable(name = "accountId") @NotNull UUID accountId) {
    accountService.approve(accountId);
    return ResponseEntity.noContent().build();
  }

  @Override
  @PreAuthorize("hasRole('COORDINATOR')")
  public ResponseEntity<StudentDto[]> getUnapprovedStudents() {
    return ResponseEntity.ok(accountService.getUnapprovedStudents());
  }

  @Override
  @PreAuthorize("hasRole('COORDINATOR')")
  public ResponseEntity<UserDto> createLecturer(
      @RequestBody @Valid @NotNull CreateLecturerRequestDto body) {
    return ResponseEntity.status(HttpStatus.CREATED)
        .body((UserDto) accountService.createLecturer(body));
  }
}
