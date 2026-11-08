package com.homeenergy.deviceservice.exception;

public class DeviceNotFoundException extends RuntimeException {
    public DeviceNotFoundException(String message) {
        super("Device not found: " + message);
    }
}
