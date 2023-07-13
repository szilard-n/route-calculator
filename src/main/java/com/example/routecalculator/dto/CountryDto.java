package com.example.routecalculator.dto;

import java.util.List;

public record CountryDto(
        String cca3,
        List<String> borders) {
}
