package com.changeready.dto.reporting;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

/**
 * Helper-Klasse für JSON-Parsing der Template-Categories
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class CategoryStructure {
	private String name;
	private List<SubcategoryStructure> subcategories;
}

