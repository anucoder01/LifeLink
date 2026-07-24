package com.lifelink.request.dto;

import com.lifelink.request.RequestResponseStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class RespondToRequestDto {

    /**
     * Allowed values: ACCEPTED or DECLINED.
     * Donors can only respond with one of these two statuses.
     */
    @NotNull(message = "Response status is required")
    private RequestResponseStatus status;
}
