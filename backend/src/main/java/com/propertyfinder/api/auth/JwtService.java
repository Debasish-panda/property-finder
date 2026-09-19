package com.propertyfinder.api.auth;

import io.smallrye.config.ConfigMapping;
import jakarta.enterprise.context.ApplicationScoped;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Base64;

@ApplicationScoped
public class JwtService {
  @ConfigProperty(name="app.jwt.secret") String secret;
  @ConfigProperty(name="app.jwt.ttl-seconds") long ttl;
  public AuthDtos.AuthResponse response(UserEntity user) { long now = Instant.now().getEpochSecond(); String h = enc("{\"alg\":\"HS256\",\"typ\":\"JWT\"}"); String p = enc("{\"sub\":\""+user.id+"\",\"role\":\""+user.role+"\",\"exp\":"+(now+ttl)+"}"); return new AuthDtos.AuthResponse(h+"."+p+"."+sign(h+"."+p), ttl, user.id, user.fullName, user.role.name()); }
  public Claims verify(String token) { try { String[] parts=token.split("\\."); if(parts.length!=3 || !sign(parts[0]+"."+parts[1]).equals(parts[2])) return null; String json=new String(Base64.getUrlDecoder().decode(parts[1]), StandardCharsets.UTF_8); long exp=Long.parseLong(json.replaceAll(".*\\\"exp\\\":(\\d+).*", "$1")); if(exp < Instant.now().getEpochSecond()) return null; return new Claims(Long.parseLong(json.replaceAll(".*\\\"sub\\\":\\\"(\\d+)\\\".*", "$1")), json.replaceAll(".*\\\"role\\\":\\\"(\\w+)\\\".*", "$1")); } catch(Exception e) { return null; } }
  private String sign(String value) { try { Mac mac=Mac.getInstance("HmacSHA256"); mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256")); return Base64.getUrlEncoder().withoutPadding().encodeToString(mac.doFinal(value.getBytes(StandardCharsets.UTF_8))); } catch(Exception e) { throw new IllegalStateException(e); } }
  private String enc(String value) { return Base64.getUrlEncoder().withoutPadding().encodeToString(value.getBytes(StandardCharsets.UTF_8)); }
  public record Claims(long userId, String role) {}
}
