package seal.backend.controllers;

import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import seal.backend.config.GlobalConfig;
import seal.backend.entities.HackathonEvent;
import seal.backend.entities.Student;
import seal.backend.services.CoordinatorService;
import seal.openapi.api.CoordinatorApi;
import seal.openapi.model.StudentDto;
import seal.openapi.model.UserRoleDto;
import seal.openapi.model.UserStatusDto;

@RestController
@RequiredArgsConstructor
@RequestMapping(GlobalConfig.API_BASE)
public class CoordinatorController implements CoordinatorApi {
  private final CoordinatorService coordinatorService;

  @Override
  @PreAuthorize("hasAuthority('COORDINATOR')")
  public ResponseEntity<StudentDto[]> getUnapprovedStudents() {
    List<Student> unapprovedStudents = coordinatorService.getUnapprovedStudents();

    StudentDto[] dtoArr = unapprovedStudents.stream()
        .map(
            student -> {
              UUID[] interested = student.getEvents().stream().map(HackathonEvent::getId).toArray(UUID[]::new);

              return new StudentDto(
                  student.getId(),
                  student.getUser().getEmail(),
                  student.getUser().getFullName(),
                  UserRoleDto.fromValue(student.getUser().getRole().name()),
                  UserStatusDto.fromValue(student.getStudentStatus().name()),
                  student.getStudentId(),
                  student.isExternal(),
                  null,
                  student.getTeam() != null ? student.getTeam().getId() : null,
                  interested);
            })
        .toArray(StudentDto[]::new);

    return ResponseEntity.ok(dtoArr);
  }
}
