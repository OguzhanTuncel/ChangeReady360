package com.changeready.service;

import com.changeready.dto.measure.MeasureResponse;
import com.changeready.entity.Measure;
import com.changeready.repository.MeasureRepository;
import com.changeready.security.UserPrincipal;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class MeasureServiceImpl implements MeasureService {

	private final MeasureRepository measureRepository;

	public MeasureServiceImpl(MeasureRepository measureRepository) {
		this.measureRepository = measureRepository;
	}

	@Override
	public List<MeasureResponse> getActiveMeasures(UserPrincipal userPrincipal) {
		List<Measure.MeasureStatus> activeStatuses = List.of(
			Measure.MeasureStatus.OPEN,
			Measure.MeasureStatus.IN_PROGRESS
		);
		
		List<Measure> measures = measureRepository.findByCompanyIdAndStatusIn(
			userPrincipal.getCompanyId(),
			activeStatuses
		);
		
		return measures.stream()
			.map(this::toResponse)
			.collect(Collectors.toList());
	}

	private MeasureResponse toResponse(Measure measure) {
		MeasureResponse response = new MeasureResponse();
		response.setId(measure.getId());
		response.setTitle(measure.getTitle());
		response.setDescription(measure.getDescription());
		response.setStatus(measure.getStatus().name());
		response.setCreatedAt(measure.getCreatedAt());
		response.setUpdatedAt(measure.getUpdatedAt());
		return response;
	}
}

