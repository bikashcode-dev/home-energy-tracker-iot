package com.homeenergy.deviceservice.service;
import com.homeenergy.deviceservice.dto.DeviceDto;
import com.homeenergy.deviceservice.entity.Device;
import com.homeenergy.deviceservice.exception.DeviceNotFoundException;
import com.homeenergy.deviceservice.repository.DeviceRepository;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

@Service
@Component
public class DeviceService {

    private DeviceRepository deviceRepository;

    public DeviceService(DeviceRepository deviceRepository) {
        this.deviceRepository = deviceRepository;
    }

    public DeviceDto getDeviceById(Long id) {
        Device device = deviceRepository
                .findById(id)
                .orElseThrow(() -> new DeviceNotFoundException("Device not found"));
        return mapToDto(device);
    }


    private DeviceDto mapToDto(Device inputDevice) {
        DeviceDto deviceDto = new DeviceDto();
        deviceDto.setId(inputDevice.getId());
        deviceDto.setName(inputDevice.getName());
        deviceDto.setType(inputDevice.getType());
        deviceDto.setLocation(inputDevice.getLocation());
        deviceDto.setUserId(inputDevice.getUserId());
        return deviceDto;
    }



    public DeviceDto createDevice(DeviceDto deviceDto) {
        Device device = new Device();
        device.setName(deviceDto.getName());
        device.setType(deviceDto.getType());
        device.setLocation(deviceDto.getLocation());
        device.setUserId(deviceDto.getUserId());

        // Save the device entity to the database
         final Device savedDevice = deviceRepository.save(device);
        return mapToDto(savedDevice);
    }

    public DeviceDto updateDevice(Long id, DeviceDto inputDeviceDto) {
        Device existingDevice = deviceRepository.findById(id)
                .orElseThrow(() ->
                        new DeviceNotFoundException("Device not found with id: " + id));

        existingDevice.setName(inputDeviceDto.getName());
        existingDevice.setType(inputDeviceDto.getType());
        existingDevice.setLocation(inputDeviceDto.getLocation());
        existingDevice.setUserId(inputDeviceDto.getUserId());

        final Device updatedDevice = deviceRepository.save(existingDevice);
        return mapToDto(updatedDevice);
    }

    public void deleteDevice(Long id) {
        if (!deviceRepository.existsById(id)) {
            throw new DeviceNotFoundException("Device not found with id: " + id);
        }
        deviceRepository.deleteById(id);
    }
}
