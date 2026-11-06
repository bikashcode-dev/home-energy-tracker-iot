package com.homeenergy.userservice.dto;

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
    private String email;
    private String address;
    private boolean alerting;
    private double energyAlertingThreshold;
}
