package seal.backend.filters;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import seal.backend.services.JwtService;
import tools.jackson.databind.ObjectMapper;

@Component
@RequiredArgsConstructor
public class JwtFilter extends OncePerRequestFilter {
  private final UserDetailsService userDetailsService;
  private final ObjectMapper objMapper;

  @Override
  protected void doFilterInternal(
      HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
      throws ServletException, IOException {
    String authorizationHeader = request.getHeader("Authorization");
    if (authorizationHeader == null) {
      filterChain.doFilter(request, response);
      return;
    }

    String token = authorizationHeader.replace("Bearer ", "");

    try {
      String subject = JwtService.extractEmail(token);
      UserDetails userDetails = userDetailsService.loadUserByUsername(subject);

      UsernamePasswordAuthenticationToken auth =
          new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());

      SecurityContextHolder.getContext().setAuthentication(auth);
      filterChain.doFilter(request, response);
    } catch (UsernameNotFoundException ex) {
      ProblemDetail problemDetail =
          ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, ex.getMessage());

      response.setStatus(HttpStatus.BAD_REQUEST.value());
      response.getWriter().write(objMapper.writeValueAsString(problemDetail));
    } catch (Exception ex) {
      ProblemDetail problemDetail = ProblemDetail.forStatus(HttpStatus.INTERNAL_SERVER_ERROR);
      logger.error(ex);

      response.setStatus(HttpStatus.BAD_REQUEST.value());
      response.getWriter().write(objMapper.writeValueAsString(problemDetail));
    }
  }
}
