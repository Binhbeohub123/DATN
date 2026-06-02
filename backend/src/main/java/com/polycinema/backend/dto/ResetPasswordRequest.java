package com.polycinema.backend.dto;

import lombok.Data;

import jakarta.validation.constraints.NotBlank;


@Data
public class ResetPasswordRequest {

    @NotBlank(message = "Email không được để trống")
    private String email;

    @NotBlank(message = "OTP không được để trống")
    private String otp;

    @NotBlank(message = "Mật khẩu mới không được để trống")
    private String newPassword;
}