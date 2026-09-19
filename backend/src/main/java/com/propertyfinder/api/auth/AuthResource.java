package com.propertyfinder.api.auth;

import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.*;

@Path("/api/auth") @Consumes(MediaType.APPLICATION_JSON) @Produces(MediaType.APPLICATION_JSON)
public class AuthResource {
  @Inject AuthService auth; @Inject JwtService jwt;
  @POST @Path("/signup") public AuthDtos.AuthResponse signup(AuthDtos.SignupRequest request){UserEntity user=auth.signup(request);return new AuthDtos.AuthResponse(null,0,user.id,user.fullName,user.role.name());}
  @POST @Path("/login/password") public AuthDtos.AuthResponse password(AuthDtos.PasswordLoginRequest request){return auth.passwordLogin(request);}
  @POST @Path("/login/otp/request") public AuthDtos.MessageResponse requestOtp(AuthDtos.OtpRequest request){auth.issueOtp(request.identifier());return new AuthDtos.MessageResponse("OTP sent");}
  @POST @Path("/login/otp/verify") public AuthDtos.AuthResponse verify(AuthDtos.OtpVerifyRequest request){return auth.verifyOtp(request);}
  @POST @Path("/logout") public Response logout(@HeaderParam("Authorization") String authorization){JwtService.Claims claims=claims(authorization);if(claims==null)return Response.status(Response.Status.UNAUTHORIZED).entity(new AuthDtos.MessageResponse("Invalid or expired session")).build();auth.logout(claims);return Response.ok(new AuthDtos.MessageResponse("Logged out successfully")).build();}
  @GET @Path("/session") public Response session(@HeaderParam("Authorization") String authorization){JwtService.Claims claims=claims(authorization);if(claims==null)return Response.status(Response.Status.UNAUTHORIZED).entity(new AuthDtos.MessageResponse("Session expired or invalid")).build();UserEntity user=UserEntity.findById(claims.userId());if(user==null||!user.enabled)return Response.status(Response.Status.UNAUTHORIZED).build();long remaining=Math.max(0,claims.expiresAt()-java.time.Instant.now().getEpochSecond());return Response.ok(new AuthDtos.SessionResponse(user.id,user.fullName,user.role.name(),remaining)).build();}
  private JwtService.Claims claims(String value){return value!=null&&value.startsWith("Bearer ")?jwt.verify(value.substring(7)):null;}
}
