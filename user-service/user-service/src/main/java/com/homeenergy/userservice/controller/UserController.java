package com.homeenergy.userservice.controller;


import com.homeenergy.userservice.dto.UserDto;
import com.homeenergy.userservice.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/user")
public class UserController {

   private  final UserService userService;

   public UserController(UserService userService) {
       this.userService = userService;
   }

   @PostMapping
    public ResponseEntity<UserDto> createUser( @RequestBody UserDto userDto) {
       UserDto created  = userService.createUser( userDto);
       return new ResponseEntity<>(created, HttpStatus.CREATED);
   }


   public ResponseEntity<UserDto> getUserById(@PathVariable Long id) {
       UserDto userDto = userService.getUserById(id);
         if(userDto == null) {
             return new ResponseEntity<>(HttpStatus.NOT_FOUND);
         }
         return new ResponseEntity<>(userDto, HttpStatus.OK);


   }

}
