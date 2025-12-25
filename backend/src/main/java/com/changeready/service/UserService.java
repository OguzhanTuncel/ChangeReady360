package com.changeready.service;

import com.changeready.dto.user.UserCreateRequest;
import com.changeready.dto.user.UserUpdateRequest;
import com.changeready.dto.user.UserResponse;

import java.util.List;

public interface UserService {
	UserResponse create(UserCreateRequest request, Long companyId);
	UserResponse createCompanyAdmin(UserCreateRequest request, Long companyId);
	List<UserResponse> findAllByCompany(Long companyId);
	UserResponse findById(Long id, Long companyId);
	UserResponse update(Long id, UserUpdateRequest request, Long companyId);
}

