package com.homeenergy.userservice;

import com.homeenergy.userservice.entity.User;
import com.homeenergy.userservice.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
@Disabled
@Slf4j
@SpringBootTest
class UserServiceApplicationTests {

    private static final int NUMBER_OF_USER = 50;

    @Autowired
    public UserRepository userRepository;

	@Test
	void contextLoads() {
	}


    @Test
    void addUserToDB(){
        for(int i=1;i<NUMBER_OF_USER;i++){
            User user = User.builder()
                    .name("User" + i)
                    .surname("surname" + i)
                    .email("user" + i + "@example.com")
                    .address(i + "Example st")
                    .alerting(i % 2 == 0)
                    .energyAlertingThreshold(1000.0 + i)
                    .build();

            userRepository.save(user);
        }
        log.info("User Repository  populated su: {}" ,userRepository.findAll());
    }

}
