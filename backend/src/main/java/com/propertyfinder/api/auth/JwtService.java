package com.propertyfinder.api.auth;

import jakarta.enterprise.context.ApplicationScoped;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import jakarta.transaction.Transactional;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Base64;
import java.util.UUID;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

@ApplicationScoped
public class JwtService {
  @ConfigProperty(name="app.jwt.secret") String secret;
  @ConfigProperty(name="app.jwt.ttl-seconds") long ttl;

  @Transactional
  public AuthDtos.AuthResponse response(UserEntity user) {
    long now=Instant.now().getEpochSecond(); UUID sessionId=UUID.randomUUID(); long expires=now+ttl;
    SessionEntity session=new SessionEntity(); session.id=sessionId; session.userId=user.id; session.expiresAt=Instant.ofEpochSecond(expires); session.persist();
    String header=encode("{\"alg\":\"HS256\",\"typ\":\"JWT\"}"), payload=encode("{\"sub\":\""+user.id+"\",\"sid\":\""+sessionId+"\",\"role\":\""+user.role+"\",\"exp\":"+expires+"}");
    return new AuthDtos.AuthResponse(header+"."+payload+"."+sign(header+"."+payload),ttl,user.id,user.fullName,user.role.name());
  }

  public Claims verify(String token) {
    try { String[] parts=token.split("\\."); if(parts.length!=3 || !sign(parts[0]+"."+parts[1]).equals(parts[2]))return null; String json=new String(Base64.getUrlDecoder().decode(parts[1]),StandardCharsets.UTF_8);
      long exp=Long.parseLong(json.replaceAll(".*\\\"exp\\\":(\\d+).*","$1")); UUID sid=UUID.fromString(json.replaceAll(".*\\\"sid\\\":\\\"([^\"]+)\\\".*","$1")); long uid=Long.parseLong(json.replaceAll(".*\\\"sub\\\":\\\"(\\d+)\\\".*","$1")); String role=json.replaceAll(".*\\\"role\\\":\\\"(\\w+)\\\".*","$1");
      SessionEntity session=SessionEntity.findById(sid); if(exp<Instant.now().getEpochSecond() || session==null || session.revokedAt!=null || session.expiresAt.isBefore(Instant.now()))return null; return new Claims(uid,role,sid,exp);
    } catch(Exception e){return null;}
  }
  private String sign(String value){try{Mac mac=Mac.getInstance("HmacSHA256");mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8),"HmacSHA256"));return Base64.getUrlEncoder().withoutPadding().encodeToString(mac.doFinal(value.getBytes(StandardCharsets.UTF_8)));}catch(Exception e){throw new IllegalStateException(e);}}
  private String encode(String value){return Base64.getUrlEncoder().withoutPadding().encodeToString(value.getBytes(StandardCharsets.UTF_8));}
  public record Claims(long userId,String role,UUID sessionId,long expiresAt) {}
}
