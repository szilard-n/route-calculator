package com.example.routecalculator.controller;

import com.example.routecalculator.dto.RouteResponse;
import com.example.routecalculator.exception.RouteCalculationException;
import com.example.routecalculator.service.RouteService;
import jakarta.validation.constraints.Pattern;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/routing")
@RequiredArgsConstructor
@Validated
public class RouteController {

    private final RouteService routeService;

    @GetMapping("/{origin}/{destination}")
    public ResponseEntity<RouteResponse> getRoute(@PathVariable @Pattern(regexp = "^[A-Z]{3}$") String origin,
                                                  @PathVariable @Pattern(regexp = "^[A-Z]{3}$") String destination) {
        return ResponseEntity.ok(routeService.getRoute(origin, destination));
    }
}
