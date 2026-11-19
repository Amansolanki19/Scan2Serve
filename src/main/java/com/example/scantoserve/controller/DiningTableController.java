package com.example.scantoserve.controller;


import com.example.scantoserve.common.ApiResponse;
import com.example.scantoserve.configuration.CustomUserDetails;
import com.example.scantoserve.dto.DiningTableRequestDto;
import com.example.scantoserve.dto.DiningTableResponseDto;
import com.example.scantoserve.service.DiningTableService;
import com.example.scantoserve.service.QRCodeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ContentDisposition;

import java.util.List;

@RestController
@RequestMapping("/api/restaurants/{restaurantId}/tables")
@RequiredArgsConstructor
public class DiningTableController {

    private final DiningTableService diningTableService;
    private final QRCodeService qrCodeService;
    // Create a dining table
    @PostMapping
    public ResponseEntity<ApiResponse<DiningTableResponseDto>> createTable(
            @PathVariable Long restaurantId,
            @Valid @RequestBody DiningTableRequestDto request,
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        DiningTableResponseDto response = diningTableService.createTable(
                restaurantId,
                request,
                userDetails.getUsername()
        );

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(
                        "Dining table created successfully",
                        response
                ));
    }

    // Get all tables of a restaurant
    @GetMapping
    public ResponseEntity<ApiResponse<List<DiningTableResponseDto>>> getRestaurantTables(
            @PathVariable Long restaurantId,
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        List<DiningTableResponseDto> response =
                diningTableService.getRestaurantTables(
                        restaurantId,
                        userDetails.getUsername()
                );

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Dining tables fetched successfully",
                        response
                )
        );
    }

    @GetMapping("/{tableId}/qr")
    public ResponseEntity<byte[]> downloadTableQRCode(
            @PathVariable Long restaurantId,
            @PathVariable Long tableId,
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        DiningTableResponseDto table = diningTableService.getTableById(
                restaurantId,
                tableId,
                userDetails.getUsername()
        );

        byte[] qrImage = qrCodeService.generateQRCode(
                table.qrToken()
        );

        HttpHeaders headers = new HttpHeaders();

        headers.setContentType(MediaType.IMAGE_PNG);

        headers.setContentDisposition(
                ContentDisposition.inline()
                        .filename("table-qr-" + table.tableNumber() + ".png")
                        .build()
        );

        return new ResponseEntity<>(
                qrImage,
                headers,
                HttpStatus.OK
        );
    }

    // Get a specific table
    @GetMapping("/{tableId}")
    public ResponseEntity<ApiResponse<DiningTableResponseDto>> getTableById(
            @PathVariable Long restaurantId,
            @PathVariable Long tableId,
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        DiningTableResponseDto response = diningTableService.getTableById(
                restaurantId,
                tableId,
                userDetails.getUsername()
        );

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Dining table fetched successfully",
                        response
                )
        );
    }
}
