package com.changeready.repository;

import com.changeready.entity.Invite;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface InviteRepository extends JpaRepository<Invite, Long> {

	Optional<Invite> findByToken(String token);

	Optional<Invite> findByTokenAndStatus(String token, Invite.InviteStatus status);

	List<Invite> findByCompanyId(Long companyId);

	List<Invite> findByCompanyIdAndStatus(Long companyId, Invite.InviteStatus status);

	List<Invite> findByStatus(Invite.InviteStatus status);

	List<Invite> findByExpiresAtBeforeAndStatus(LocalDateTime now, Invite.InviteStatus status);

	boolean existsByEmailAndCompanyIdAndStatus(String email, Long companyId, Invite.InviteStatus status);
}

