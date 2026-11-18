package com.scamshield.admin.dto;

import java.util.List;
import java.util.function.Function;

import org.springframework.data.domain.Page;

public record AdminPageResponse<T>(List<T> content, int page, int size, long totalElements, int totalPages) {
    public static <S, T> AdminPageResponse<T> from(Page<S> source, Function<S, T> mapper) {
        return new AdminPageResponse<>(
                source.getContent().stream().map(mapper).toList(),
                source.getNumber(),
                source.getSize(),
                source.getTotalElements(),
                source.getTotalPages());
    }
}
