package com.homeenergy.userservice.controller;


import com.homeenergy.userservice.dto.UserDto;
import com.homeenergy.userservice.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/user")
public class UserController {

   private  final UserService userService;

   public UserController(UserService userService) {
       this.userService = userService;
   }

   @PostMapping
    public ResponseEntity<UserDto> createUser( @RequestBody UserDto userDto) {
       UserDto userCreated = userService.createUser( userDto);
       return ResponseEntity.status(HttpStatus.CREATED).body(userCreated);
   }
}
