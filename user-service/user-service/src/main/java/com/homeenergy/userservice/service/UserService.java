package com.homeenergy.userservice.service;

import com.homeenergy.userservice.dto.UserDto;
import com.homeenergy.userservice.entity.User;
import com.homeenergy.userservice.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.beans.IndexedPropertyDescriptor;

@Slf4j
@Service
public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }


    public UserDto createUser( UserDto input) {
        log.info("UserService createUser Creating User : {} ", input);

        final User createdUser = User.builder()
                .name(input.getName())
                .surname(input.getSurname())
                .email(input.getEmail())
                .address(input.getAddress())
                .alerting(input.isAlerting())
                .energyAlertingThreshold(input.getEnergyAlertingThreshold())
                .build();

        final User savedUser = userRepository.save(createdUser);
        return toDto(savedUser);

    }

    private UserDto toDto(User user) {
        return UserDto.builder()
                .id(user.getId())
                .name(user.getName())
                .surname(user.getSurname())
                .email(user.getEmail())
                .address(user.getAddress())
                .alerting(user.isAlerting())
                .energyAlertingThreshold(user.getEnergyAlertingThreshold())
                .build();
    }

    public UserDto getUserById(Long id) {
        log.info("UserService getUserById Fetching User by ID : {} ", id);
        return userRepository.findById(id)
                .map(this::toDto)
                .orElse(null);
    }
}
