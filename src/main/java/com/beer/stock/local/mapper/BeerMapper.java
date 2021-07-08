package com.beer.stock.local.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import com.beer.stock.local.dto.BeerDTO;
import com.beer.stock.local.entity.Beer;

@Mapper
public interface BeerMapper {

    BeerMapper INSTANCE = Mappers.getMapper(BeerMapper.class);

    Beer toModel(BeerDTO beerDTO);

    BeerDTO toDTO(Beer beer);
}

