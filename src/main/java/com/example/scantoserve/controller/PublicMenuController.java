package com.example.scantoserve.controller;

import com.example.scantoserve.common.ApiResponse;
import com.example.scantoserve.dto.PublicMenuResponseDto;
import com.example.scantoserve.service.PublicMenuService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/public/menu")
@RequiredArgsConstructor
public class PublicMenuController {

    private final PublicMenuService publicMenuService;

    @GetMapping("/{qrToken}")
    public ResponseEntity<ApiResponse<PublicMenuResponseDto>> getPublicMenu(
            @PathVariable String qrToken) {

        PublicMenuResponseDto response =
                publicMenuService.getPublicMenu(qrToken);

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Menu fetched successfully",
                        response
                )
        );
    }
}