package com.lifelink.common.util;

import com.lifelink.common.dto.PaginatedResponse;
import org.springframework.data.domain.Page;

/**
 * Utility class for converting Spring Data {@link Page} objects to {@link PaginatedResponse} DTOs.
 */
public class PaginationUtil {
    /**
     * Converts a {@link Page} of items into a {@link PaginatedResponse}.
     *
     * @param page the Spring Data page
     * @param <T>  the type of the content
     * @return a populated {@link PaginatedResponse}
     */
    public static <T> PaginatedResponse<T> fromPage(Page<T> page) {
        if (page == null) {
            return new PaginatedResponse<>();
        }
        return new PaginatedResponse<>(
                page.getContent(),
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages(),
                page.isLast()
        );
    }
}
