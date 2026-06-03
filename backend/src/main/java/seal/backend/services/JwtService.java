package seal.backend.services;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import java.util.Date;
import javax.crypto.SecretKey;

public class JwtService {
  // TODO: don't hardcode this
  private static final String SECRET = "m9F8oBeNdrHHd7Y2VbSsJGPC6cOV1HUj7pKzNh7be+w=";
  private static final long EXPIRATION_TIME = 3600000L;

  public static String sign(String email) {
    long now = System.currentTimeMillis();
    Date currentTime = new Date(now);
    Date expTime = new Date(now + EXPIRATION_TIME);

    return Jwts.builder()
        .subject(email)
        .issuedAt(currentTime)
        .expiration(expTime)
        .signWith(getSignKey())
        .compact();
  }

  public static String extractEmail(String token) {
    Jws<Claims> jws = Jwts.parser().verifyWith(getSignKey()).build().parseSignedClaims(token);
    return jws.getPayload().getSubject();
  }

  private static SecretKey getSignKey() {
    byte[] keyBytes = Decoders.BASE64.decode(SECRET);
    return Keys.hmacShaKeyFor(keyBytes);
  }
}
