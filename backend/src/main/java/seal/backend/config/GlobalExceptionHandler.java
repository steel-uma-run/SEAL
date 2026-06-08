package seal.backend.config;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@RestControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {
  @ExceptionHandler(IllegalArgumentException.class)
  public ProblemDetail handleIllegalArgumentException(IllegalArgumentException ex) {
    ProblemDetail problemDetail =
        ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, ex.getMessage());

    return problemDetail;
  }

  // Custom MethodArgumentNotValidException handler to return actual, real, readable error messages
  // instead of just "Invalid request content."
  @Override
  protected ResponseEntity<Object> handleMethodArgumentNotValid(
      MethodArgumentNotValidException ex,
      HttpHeaders headers,
      HttpStatusCode status,
      WebRequest request) {
    List<FieldError> errors = ex.getFieldErrors();
    Map<String, Object> errorDetails = new HashMap<>();

    for (FieldError err : errors) {
      errorDetails.put(err.getField(), err.getDefaultMessage());
    }

    ProblemDetail body = ex.getBody();
    body.setStatus(HttpStatus.UNPROCESSABLE_CONTENT);
    body.setProperty("errors", errorDetails);

    return ResponseEntity.unprocessableContent().body(body);
  }
}
