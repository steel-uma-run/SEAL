package seal.backend.services;

import java.util.UUID;
import seal.openapi.model.StudentDto;

public interface AccountService {
  public Object[] getAllAccounts();

  public void approve(UUID id);

  public StudentDto[] getUnapprovedStudents();

  public Object createLecturer(CreateLecturerRequestDto request);
}
