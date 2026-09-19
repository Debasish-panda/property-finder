package com.propertyfinder.api.auth;

import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;

@Path("/api/auth") @Consumes(MediaType.APPLICATION_JSON) @Produces(MediaType.APPLICATION_JSON)
public class AuthResource {
  @Inject AuthService auth;
  @POST @Path("/signup") public AuthDtos.AuthResponse signup(AuthDtos.SignupRequest request) { UserEntity user=auth.signup(request); return new AuthDtos.AuthResponse(null,0,user.id,user.fullName,user.role.name()); }
  @POST @Path("/login/password") public AuthDtos.AuthResponse password(AuthDtos.PasswordLoginRequest request) { return auth.passwordLogin(request); }
  @POST @Path("/login/otp/request") public AuthDtos.MessageResponse requestOtp(AuthDtos.OtpRequest request) { auth.issueOtp(request.identifier()); return new AuthDtos.MessageResponse("OTP sent"); }
  @POST @Path("/login/otp/verify") public AuthDtos.AuthResponse verify(AuthDtos.OtpVerifyRequest request) { return auth.verifyOtp(request); }
}
