package com.homeenergy.deviceservice.dto;

import com.homeenergy.deviceservice.entity.Device;
import com.homeenergy.deviceservice.model.DeviceType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class DeviceDto {

    private Long id;
    private String name;
    private DeviceType type;
    private String location;
    private Long userId;

}
