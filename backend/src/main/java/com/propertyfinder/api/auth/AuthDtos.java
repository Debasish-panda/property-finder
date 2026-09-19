package com.propertyfinder.api.auth;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public final class AuthDtos {
  private AuthDtos() {}
  public record SignupRequest(@NotBlank String fullName, String email, String mobile, @NotBlank String password,
                              @Pattern(regexp = "(?i)USER|BROKER") String role) {}
  public record PasswordLoginRequest(@NotBlank String identifier, @NotBlank String password) {}
  public record OtpRequest(@NotBlank String identifier) {}
  public record OtpVerifyRequest(@NotBlank String identifier, @NotBlank String code) {}
  public record AuthResponse(String token, long expiresIn, Long userId, String fullName, String role) {}
  public record MessageResponse(String message) {}
}
