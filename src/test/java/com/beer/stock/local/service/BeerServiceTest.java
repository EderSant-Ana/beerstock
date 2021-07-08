package com.beer.stock.local.service;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.beer.stock.local.builder.BeerDTOBuilder;
import com.beer.stock.local.dto.BeerDTO;
import com.beer.stock.local.entity.Beer;
import com.beer.stock.local.exception.BeerAlreadyRegisteredException;
import com.beer.stock.local.exception.BeerNotFoundException;
import com.beer.stock.local.mapper.BeerMapper;
import com.beer.stock.local.repository.BeerRepository;



@ExtendWith(MockitoExtension.class)
public class BeerServiceTest {

	private static final long INVALID_BEER_ID = 1L;

	@Mock
	private BeerRepository beerRepository;

	private BeerMapper beerMapper = BeerMapper.INSTANCE;

	@InjectMocks
	private BeerService beerService;

	@BeforeEach
	void setup() {
		beerService = new BeerService(beerRepository, beerMapper);
	}

	@Test
	void whenBeerInformedThenItShouldBeCreated() throws BeerAlreadyRegisteredException {

		//given
		BeerDTO expectedBeerDTO = BeerDTOBuilder.builder().build().toBeerDTO();
		Beer expectedSavedBeer = beerMapper.toModel(expectedBeerDTO);

		// when
		when(beerRepository.findByName(expectedBeerDTO.getName())).thenReturn(Optional.empty());
		when(beerRepository.save(expectedSavedBeer)).thenReturn(expectedSavedBeer);

		//then
		BeerDTO createdBeerDTO = beerService.createBeer(expectedBeerDTO);

		assertEquals(expectedBeerDTO.getId(), createdBeerDTO.getId());
		assertEquals(expectedBeerDTO.getName(), createdBeerDTO.getName());
		
		assertThat(createdBeerDTO.getId(), is(equalTo(expectedBeerDTO.getId())));
		assertThat(createdBeerDTO.getName(), is(equalTo(expectedBeerDTO.getName())));
		assertThat(createdBeerDTO.getQuantity(), is(equalTo(expectedBeerDTO.getQuantity())));

		assertThat(createdBeerDTO.getQuantity(), is(greaterThan(2)));
		
		assertThat(createdBeerDTO, is(notNullValue()));
	}
	
	@Test
	void whenAlreadyRegisteredBeerInformedThenAnExceptionShouldBeThrown() throws BeerAlreadyRegisteredException {
		
		//given
		BeerDTO expectedBeerDTO = BeerDTOBuilder.builder().build().toBeerDTO();
		Beer duplicatedBeer = beerMapper.toModel(expectedBeerDTO);
		
		//when
		when(beerRepository.findByName(expectedBeerDTO.getName())).thenReturn(Optional.of(duplicatedBeer));
		
		//then
		assertThrows(BeerAlreadyRegisteredException.class, () -> beerService.createBeer(expectedBeerDTO));
	}
	
	@Test
	void whenValidBeerNameIsGivenThenReturnABeer() throws BeerNotFoundException {
		
		//given
		BeerDTO expectedFoundBeerDTO = BeerDTOBuilder.builder().build().toBeerDTO();
		Beer expectedFoundBeer = beerMapper.toModel(expectedFoundBeerDTO);
	
		//when
		when(beerRepository.findByName(expectedFoundBeer.getName())).thenReturn(Optional.of(expectedFoundBeer));
		
		//then
		BeerDTO foundedBeerDTO = beerService.findByName(expectedFoundBeerDTO.getName());
	
		assertThat(foundedBeerDTO, is(equalTo(expectedFoundBeerDTO)));			
	}
	
	@Test
	void whenNotRegisteredBeerNameIsGivenThenThrowAnException() throws BeerNotFoundException {
		
		//given
		BeerDTO expectedFoundBeerDTO = BeerDTOBuilder.builder().build().toBeerDTO();
	
		//when
		when(beerRepository.findByName(expectedFoundBeerDTO.getName())).thenReturn(Optional.empty());
		
		//then
		assertThrows(BeerNotFoundException.class, () -> beerService.findByName(expectedFoundBeerDTO.getName()));			
	}

	@Test
	void whenListBeerIsCalledThenReturnAListOfBeers() {
		
		//given
		BeerDTO expectedFoundBeerDTO = BeerDTOBuilder.builder().build().toBeerDTO();
		Beer expectedFoundBeer = beerMapper.toModel(expectedFoundBeerDTO);
		List<Beer> beerList = Collections.singletonList(expectedFoundBeer);
		
		//when
		when(beerRepository.findAll()).thenReturn(beerList);
		
		//then
		List<BeerDTO> foundedListBeerDTO = beerService.listAll();
		
		assertThat(foundedListBeerDTO.size(), is(greaterThan(0)));
		assertThat(foundedListBeerDTO, is(not(empty())));
		assertThat(foundedListBeerDTO.get(0), is(equalTo(expectedFoundBeerDTO)));
		
	}
	
	@Test
	void whenListBeerIsCalledThenReturnAnEmptyList() {
		
		//when
		when(beerRepository.findAll()).thenReturn(Collections.emptyList());
		
		//then
		List<BeerDTO> foundedListBeerDTO = beerService.listAll();
		
		assertThat(foundedListBeerDTO.size(), is(0));
		assertThat(foundedListBeerDTO, is(empty()));
		
	}
	
	@Test
	void whenExclusionIsCalledWithValidIdThenABeerShouldBeDeleted() throws BeerNotFoundException {
		BeerDTO expectedDeletedBeerDTO = BeerDTOBuilder.builder().build().toBeerDTO();
		Beer expectedDeletedBeer = beerMapper.toModel(expectedDeletedBeerDTO);
		
		// when
		when(beerRepository.findById(expectedDeletedBeerDTO.getId())).thenReturn(Optional.of(expectedDeletedBeer));
		doNothing().when(beerRepository).deleteById(expectedDeletedBeerDTO.getId());

		//then
		beerService.deleteById(expectedDeletedBeerDTO.getId());
		
		verify(beerRepository, times(1)).findById(expectedDeletedBeerDTO.getId());
		verify(beerRepository, times(1)).deleteById(expectedDeletedBeerDTO.getId());
	}
	
	@Test
	void whenExclusionIsCalledWithInValidIdThenThrowsBeerNotFoundException() throws BeerNotFoundException {
		
		// when
		when(beerRepository.findById(INVALID_BEER_ID)).thenReturn(Optional.empty());

		//then		
		assertThrows(BeerNotFoundException.class, () -> beerService.deleteById(INVALID_BEER_ID));
	}
}

