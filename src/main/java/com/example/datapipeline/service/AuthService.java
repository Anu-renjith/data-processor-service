package com.example.datapipeline.service;


import com.example.datapipeline.dto.LoginDto;
import com.example.datapipeline.dto.RegisterDto;

public interface AuthService {
    String login(LoginDto loginDto);

    String register(RegisterDto registerDto);
}

