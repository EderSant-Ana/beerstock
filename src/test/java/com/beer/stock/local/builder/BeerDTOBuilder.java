package com.beer.stock.local.builder;

import com.beer.stock.local.dto.BeerDTO;
import com.beer.stock.local.enums.BeerType;

import lombok.Builder;

@Builder
public class BeerDTOBuilder {

	@Builder.Default
	private Long id = 1L;
	
	@Builder.Default
	private String name = "Brahma";

	@Builder.Default
	private String brand = "Ambev";

	@Builder.Default
	private Integer max = 50;

	@Builder.Default
	private Integer quantity = 10;

	@Builder.Default
	private BeerType type = BeerType.LAGER;	
	
	public BeerDTO toBeerDTO() {
		return new BeerDTO(id, name, brand, max, quantity, type);
	}
	
	
	
	
}
