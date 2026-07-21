package com.lifelink.common;

import lombok.AllArgsConstructor;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class ApiError {
    private String error;
    private String message;
    private LocalDateTime timestamp;
}
