package com.example.scantoserve.service;

import com.example.scantoserve.dto.PublicMenuResponseDto;

public interface PublicMenuService {
    PublicMenuResponseDto getPublicMenu(String qrToken);
}
