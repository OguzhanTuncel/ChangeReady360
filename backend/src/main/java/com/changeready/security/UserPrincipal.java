package com.changeready.security;

import com.changeready.entity.Role;
import com.changeready.entity.User;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Collections;

@Getter
@AllArgsConstructor
public class UserPrincipal implements UserDetails {

	private Long id;
	private String email;
	private String password;
	private Role role;
	private Long companyId;
	private Boolean active;
	private Collection<? extends GrantedAuthority> authorities;

	public static UserPrincipal create(User user) {
		GrantedAuthority authority = new SimpleGrantedAuthority("ROLE_" + user.getRole().name());
		return new UserPrincipal(
			user.getId(),
			user.getEmail(),
			user.getPasswordHash(),
			user.getRole(),
			user.getCompany().getId(),
			user.getActive(),
			Collections.singletonList(authority)
		);
	}

	@Override
	public Collection<? extends GrantedAuthority> getAuthorities() {
		return authorities;
	}

	@Override
	public String getPassword() {
		return password;
	}

	@Override
	public String getUsername() {
		return email;
	}

	@Override
	public boolean isAccountNonExpired() {
		return true;
	}

	@Override
	public boolean isAccountNonLocked() {
		return true;
	}

	@Override
	public boolean isCredentialsNonExpired() {
		return true;
	}

	@Override
	public boolean isEnabled() {
		return active;
	}
}

