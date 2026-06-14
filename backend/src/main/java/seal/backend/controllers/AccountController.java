package seal.backend.controllers;

import jakarta.validation.constraints.NotNull;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import seal.backend.config.GlobalConfig;
import seal.backend.services.AccountService;
import seal.openapi.api.AccountApi;

@RestController
@RequiredArgsConstructor
@RequestMapping(GlobalConfig.API_BASE)
public class AccountController implements AccountApi {
  private final AccountService accountService;

  @Override
  @PreAuthorize("hasRole('COORDINATOR')")
  public ResponseEntity<Void> approveAccount(
      @PathVariable(name = "accountId") @NotNull UUID accountId) {
    accountService.approve(accountId);
    return ResponseEntity.ok().build();
  }
}
