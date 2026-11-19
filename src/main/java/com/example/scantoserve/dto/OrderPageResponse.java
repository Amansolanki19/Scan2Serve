
package com.example.scantoserve.dto;

import java.util.List;

public record OrderPageResponse<T>(
        List<T> content,
        int pageNumber,
        int pageSize,
        long totalElements,
        int totalPages,
        boolean last
) {}
