package com.changeready.service;

import com.changeready.dto.auth.LoginRequest;
import com.changeready.dto.auth.LoginResponse;

public interface AuthService {
	LoginResponse login(LoginRequest loginRequest);
	void logout();
	boolean validatePassword(String password);
}

