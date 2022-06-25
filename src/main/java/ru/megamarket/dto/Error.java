package ru.megamarket.dto;

import lombok.Builder;
import lombok.Data;

import javax.validation.constraints.NotNull;

@Data
@Builder
public class Error {

    @NotNull
    private Long code;
    @NotNull
    private String message;
}
