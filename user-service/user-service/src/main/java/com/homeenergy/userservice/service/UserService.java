package com.homeenergy.userservice.service;

import com.homeenergy.userservice.dto.UserDto;
import com.homeenergy.userservice.entity.User;
import com.homeenergy.userservice.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;


@Slf4j
@Service
public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }


    // Create a new user
    public UserDto createUser( UserDto input) {
        log.info("UserService createUser Creating User : {} ", input);

        // Create a new User entity from the UserDto
        final User createdUser = User.builder()
                .name(input.getName())
                .surname(input.getSurname())
                .email(input.getEmail())
                .address(input.getAddress())
                .alerting(input.isAlerting())
                .energyAlertingThreshold(input.getEnergyAlertingThreshold())
                .build();

        // Save the user entity to the database
        final User savedUser = userRepository.save(createdUser);
        return toDto(savedUser);

    }

    // Convert a User entity to a UserDto
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


    // Retrieve a user by ID
    public UserDto getUserById(Long id) {
        log.info("UserService getUserById Fetching User by ID : {} ", id);
        // Fetch the user entity from the database by ID and convert it to UserDto
        return userRepository.findById(id)
                .map(this::toDto)
                .orElse(null);
    }








    // Update an existing user
    public void updateUser(Long id, UserDto userDto) {
        log.info("UserService updateUser Updating User with ID : {} ", id);


        // Fetch the existing user entity from the database
        User updateUser = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("User not found with ID: " + id));


        // Update the user entity with the new values from the UserDto
        updateUser.setName(userDto.getName());
        updateUser.setSurname(userDto.getSurname());
        updateUser.setEmail(userDto.getEmail());
        updateUser.setAddress(userDto.getAddress());
        updateUser.setAlerting(userDto.isAlerting());
        updateUser.setEnergyAlertingThreshold(userDto.getEnergyAlertingThreshold());


        // Save the updated user entity
        userRepository.save(updateUser);

    }


    // Delete a user by ID
    public void deleteUser(Long id) {
        log.info(" Deleting User with ID : {} ", id);
        // Fetch the user entity to be deleted from the database
        User deleteUser = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("User not found with ID: " + id));
        userRepository.delete(deleteUser);
    }
}
