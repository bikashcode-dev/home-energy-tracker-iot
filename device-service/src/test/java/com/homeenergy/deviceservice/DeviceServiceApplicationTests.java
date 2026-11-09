
package com.homeenergy.deviceservice;

import com.homeenergy.deviceservice.entity.Device;
import com.homeenergy.deviceservice.model.DeviceType;
import com.homeenergy.deviceservice.repository.DeviceRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest
class DeviceServiceApplicationTests {

    private static final int DEVICE_COUNT = 200;
    private static final int USER_COUNT = 10;

    @Autowired
    private DeviceRepository deviceRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @PersistenceContext
    private EntityManager entityManager;

    @Disabled
    @Test
    @Transactional
    void shouldCreateDevicesForUsers() {

        String testRunId = UUID.randomUUID().toString();
        List<Long> userIds = new ArrayList<>();

        // Create test users
        for (int i = 1; i <= USER_COUNT; i++) {

            String email = "device-test-" + testRunId + "-" + i + "@example.com";

            jdbcTemplate.update("""
                    INSERT INTO `user`
                    (name, surname, email, address, alerting, energy_alerting_threshold)
                    VALUES (?, ?, ?, ?, ?, ?)
                    """,
                    "TestUser" + i,
                    "Integration",
                    email,
                    "Test Location",
                    true,
                    100.0
            );

            Long userId = jdbcTemplate.queryForObject(
                    "SELECT id FROM `user` WHERE email = ?",
                    Long.class,
                    email
            );

            assertNotNull(userId);
            userIds.add(userId);
        }

        // Create test devices
        for (int i = 1; i <= DEVICE_COUNT; i++) {

            Device device = Device.builder()
                    .name("Device-" + testRunId + "-" + i)
                    .type(DeviceType.values()[(i - 1) % DeviceType.values().length])
                    .location("Location-" + ((i % 3) + 1))
                    .userId(userIds.get((i - 1) % userIds.size()))
                    .build();

            deviceRepository.save(device);
        }

        entityManager.flush();

        Long createdDeviceCount = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM device WHERE name LIKE ?",
                Long.class,
                "Device-" + testRunId + "-%"
        );

        assertNotNull(createdDeviceCount);
        assertEquals(DEVICE_COUNT, createdDeviceCount);
    }
}
