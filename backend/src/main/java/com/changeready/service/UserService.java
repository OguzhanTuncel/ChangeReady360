package com.changeready.service;

import com.changeready.dto.user.UserRequest;
import com.changeready.dto.user.UserResponse;

import java.util.List;

public interface UserService {
	UserResponse create(UserRequest request, Long companyId);
	UserResponse createCompanyAdmin(UserRequest request, Long companyId);
	List<UserResponse> findAllByCompany(Long companyId);
	UserResponse findById(Long id, Long companyId);
	UserResponse update(Long id, UserRequest request, Long companyId);
}

