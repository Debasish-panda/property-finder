package com.propertyfinder.api.auth;

import io.quarkus.elytron.security.common.BcryptUtil;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;
import java.security.SecureRandom;
import java.time.Instant;
import java.util.Locale;
import java.util.UUID;

@ApplicationScoped
public class AuthService {
  private final SecureRandom random = new SecureRandom();
  private final JwtService jwt;
  public AuthService(JwtService jwt) { this.jwt = jwt; }

  @Transactional
  public UserEntity signup(AuthDtos.SignupRequest request) {
    String email = normalize(request.email()), mobile = normalize(request.mobile());
    if (email == null && mobile == null) throw error(Response.Status.BAD_REQUEST, "Email or mobile is required");
    if (request.password() == null || request.password().length() < 8) throw error(Response.Status.BAD_REQUEST, "Password must contain at least 8 characters");
    if ((email != null && UserEntity.count("lower(email)", email) > 0) || (mobile != null && UserEntity.count("mobile", mobile) > 0)) throw error(Response.Status.CONFLICT, "An account already exists for this identifier");
    UserEntity user = new UserEntity(); user.fullName=request.fullName().trim(); user.email=email; user.mobile=mobile; user.passwordHash=BcryptUtil.bcryptHash(request.password()); user.role="BROKER".equalsIgnoreCase(request.role())?UserEntity.Role.BROKER:UserEntity.Role.USER; user.persist(); return user;
  }

  @Transactional public AuthDtos.AuthResponse passwordLogin(AuthDtos.PasswordLoginRequest request) {
    UserEntity user=find(request.identifier());
    if(user==null || !user.enabled || user.passwordHash==null || !BcryptUtil.matches(request.password(), user.passwordHash)) throw error(Response.Status.UNAUTHORIZED,"Invalid credentials");
    return jwt.response(user);
  }

  @Transactional public String issueOtp(String identifier) {
    String normalized=normalize(identifier); UserEntity user=find(normalized);
    if(user==null || !user.enabled) throw error(Response.Status.NOT_FOUND,"No account found for this identifier");
    String code="%06d".formatted(random.nextInt(1_000_000)); OtpChallenge challenge=new OtpChallenge(); challenge.id=UUID.randomUUID(); challenge.identifier=normalized; challenge.codeHash=BcryptUtil.bcryptHash(code); challenge.expiresAt=Instant.now().plusSeconds(300); challenge.persist();
    System.out.printf("[DEV OTP] identifier=%s code=%s%n",normalized,code); return challenge.id.toString();
  }

  @Transactional public AuthDtos.AuthResponse verifyOtp(AuthDtos.OtpVerifyRequest request) {
    String identifier=normalize(request.identifier()); UserEntity user=find(identifier); OtpChallenge challenge=OtpChallenge.find("identifier = ?1 and consumedAt is null order by createdAt desc",identifier).firstResult();
    if(user==null || challenge==null || challenge.expiresAt.isBefore(Instant.now()) || challenge.attempts>=5 || !BcryptUtil.matches(request.code(),challenge.codeHash)) { if(challenge!=null) challenge.attempts++; throw error(Response.Status.UNAUTHORIZED,"Invalid or expired OTP"); }
    challenge.consumedAt=Instant.now(); return jwt.response(user);
  }

  @Transactional public void logout(JwtService.Claims claims) { if(claims!=null) { SessionEntity session=SessionEntity.findById(claims.sessionId()); if(session!=null) { session.revokedAt=Instant.now(); } } }
  public UserEntity find(String identifier) { String value=normalize(identifier); if(value==null)return null; return value.contains("@")?UserEntity.find("lower(email)",value).firstResult():UserEntity.find("mobile",value).firstResult(); }
  static String normalize(String value){return value==null||value.isBlank()?null:value.trim().toLowerCase(Locale.ROOT);}
  static WebApplicationException error(Response.Status status,String message){return new WebApplicationException(message,status);}
}
