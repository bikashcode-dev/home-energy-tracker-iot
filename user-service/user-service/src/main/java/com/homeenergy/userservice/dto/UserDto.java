package com.homeenergy.userservice.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Data
@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
// This class represents a Data Transfer Object (DTO) for the User entity.
// It is used to transfer user data between different layers of the application,
// such as the controller and service layers.
public class UserDto {

    private Long id;
    private String name;
    private String surname;

    @NotBlank(message = "Email is required")
    @Email(message = "Please provide a valid email address")
    private String email;
    private String address;
    private boolean alerting;
    private double energyAlertingThreshold;
}
