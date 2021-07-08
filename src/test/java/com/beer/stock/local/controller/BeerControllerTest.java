package com.beer.stock.local.controller;

import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.servlet.view.json.MappingJackson2JsonView;

import com.beer.stock.local.builder.BeerDTOBuilder;
import com.beer.stock.local.dto.BeerDTO;
import com.beer.stock.local.dto.QuantityDTO;
import com.beer.stock.local.exception.BeerNotFoundException;
import com.beer.stock.local.service.BeerService;
//import static com.beer.stock.local.utils.JsonConvertionUtils.asJsonString;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

@ExtendWith(MockitoExtension.class)
public class BeerControllerTest {

	private static final String BEER_API_URL_PATH = "/api/v1/beers";
	private static final long VALID_BEER_ID = 1L;
	private static final long INVALID_BEER_ID = 2L;
	private static final String BEER_API_SUBPATH_INCREMENT_URL = "/increment";
	private static final String BEER_API_SUBPATH_DECREMENT_URL = "/decrement";

	private MockMvc mockMvc;

	@Mock
	private BeerService beerService;

	@InjectMocks
	private BeerController beerController;

	private ObjectMapper objectMapper = new ObjectMapper();

	@BeforeEach
	void setup() {
		mockMvc = MockMvcBuilders.standaloneSetup(beerController)
				.setCustomArgumentResolvers(new PageableHandlerMethodArgumentResolver())
				.setViewResolvers((s, locale) -> new MappingJackson2JsonView()).build();
	}

	@Test
	@DisplayName("POST beer")
	void whenPOSTIsCalledThenABeerIsCreated() throws JsonProcessingException, Exception {

		// given
		BeerDTO beerDTO = BeerDTOBuilder.builder().build().toBeerDTO();

		// when
		when(beerService.createBeer(beerDTO)).thenReturn(beerDTO);

		// then
		mockMvc.perform(post(BEER_API_URL_PATH).contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(beerDTO)))
				// .content(asJsonString(beerDTO)))
				.andExpect(status().isCreated()).andExpect(jsonPath("$.name", is(beerDTO.getName())))
				.andExpect(jsonPath("$.brand", is(beerDTO.getBrand())))
				.andExpect(jsonPath("$.type", is(beerDTO.getType().toString())));
	}

	@Test
	@DisplayName("POST beer without required fields")
	void whenPOSTIsCalledWithoudRrquiredFieldThenErrorIsReturned() throws Exception {

		// given
		BeerDTO beerDTO = BeerDTOBuilder.builder().build().toBeerDTO();
		beerDTO.setBrand(null);

		// then
		mockMvc.perform(post(BEER_API_URL_PATH).contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(beerDTO))).andExpect(status().isBadRequest());
	}

	@Test
	@DisplayName("GET beer by name")
	void whenGETIsCalledWithValidNameThenOkStatusIsReturned() throws Exception {

		// given
		BeerDTO beerDTO = BeerDTOBuilder.builder().build().toBeerDTO();

		// when
		when(beerService.findByName(beerDTO.getName())).thenReturn(beerDTO);

		// then
		mockMvc.perform(MockMvcRequestBuilders.get(BEER_API_URL_PATH + "/" + beerDTO.getName())
				.contentType(MediaType.APPLICATION_JSON)).andExpect(status().isOk())
				.andExpect(jsonPath("$.name", is(beerDTO.getName())))
				.andExpect(jsonPath("$.brand", is(beerDTO.getBrand())))
				.andExpect(jsonPath("$.type", is(beerDTO.getType().toString())));

	}

	@Test
	@DisplayName("GET beer by name BeerNotFoundException")
	void whenGETIsCalledWithoutRegisteredNameThenNotFoundStatusReturned() throws Exception {

		// given
		BeerDTO beerDTO = BeerDTOBuilder.builder().build().toBeerDTO();

		// when
		when(beerService.findByName(beerDTO.getName())).thenThrow(BeerNotFoundException.class);

		// then
		mockMvc.perform(MockMvcRequestBuilders.get(BEER_API_URL_PATH + "/" + beerDTO.getName())
				.contentType(MediaType.APPLICATION_JSON)).andExpect(status().isNotFound());
	}

	@Test
	@DisplayName("GET All beer")
	void whenGETListWithBeersIsCalledThenOkStatusIsReturned() throws Exception {

		// given
		BeerDTO beerDTO = BeerDTOBuilder.builder().build().toBeerDTO();
		List<BeerDTO> list = Collections.singletonList(beerDTO);

		// when
		when(beerService.listAll()).thenReturn(list);

		// then
		mockMvc.perform(MockMvcRequestBuilders.get(BEER_API_URL_PATH).contentType(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk()).andExpect(jsonPath("$[0].name", is(beerDTO.getName())))
				.andExpect(jsonPath("$[0].brand", is(beerDTO.getBrand())))
				.andExpect(jsonPath("$[0].type", is(beerDTO.getType().toString())));

	}

	@Test
	@DisplayName("GET Empty List beer")
	void whenGETListWithoutBeersIsCalledThenOkStatusIsReturned() throws Exception {

		// when
		when(beerService.listAll()).thenReturn(Collections.emptyList());

		// then
		mockMvc.perform(MockMvcRequestBuilders.get(BEER_API_URL_PATH).contentType(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk());
	}

	@Test
	@DisplayName("DELETE beer by Id")
	void whenDELETECalledWithValidIdTheNoContentStatusIdReturned() throws Exception {

		// given
		BeerDTO beerDTO = BeerDTOBuilder.builder().build().toBeerDTO();

		// when
		doNothing().when(beerService).deleteById(beerDTO.getId());

		// then
		mockMvc.perform(MockMvcRequestBuilders.delete(BEER_API_URL_PATH + "/" + beerDTO.getId())
				.contentType(MediaType.APPLICATION_JSON)).andExpect(status().isNoContent());
	}

	@Test
	@DisplayName("DELETE beer with Invalid Id")
	void whenDELETECalledWithInValidIdThenNotFoundStatusReturned() throws Exception {

		// when
		doThrow(BeerNotFoundException.class).when(beerService).deleteById(INVALID_BEER_ID);

		// then
		mockMvc.perform(MockMvcRequestBuilders.delete(BEER_API_URL_PATH + "/" + INVALID_BEER_ID)
				.contentType(MediaType.APPLICATION_JSON)).andExpect(status().isNotFound());
	}

	
	@DisplayName("PATCH Quantity")
	@Test
	void whenPatchIsCalledToIncrementThenOkStatusIsReturned() throws Exception{
	// given
	QuantityDTO quantityDTO = QuantityDTO.builder().quantity(10).build();

	BeerDTO beerDTO = BeerDTOBuilder.builder().build().toBeerDTO();
	beerDTO.setQuantity(beerDTO.getQuantity() + quantityDTO.getQuantity());
	// when
	when(beerService.increment(VALID_BEER_ID, quantityDTO.getQuantity())).thenReturn(beerDTO);
	
	//then
	mockMvc
		.perform(MockMvcRequestBuilders.patch(BEER_API_URL_PATH + "/" + VALID_BEER_ID + BEER_API_SUBPATH_INCREMENT_URL)
		.contentType(MediaType.APPLICATION_JSON)
		.content(objectMapper.writeValueAsString(quantityDTO)))
		.andExpect(status().isOk())
		.andExpect(jsonPath("$.name", is(beerDTO.getName())))
		.andExpect(jsonPath("$.brand", is(beerDTO.getBrand())))
		.andExpect(jsonPath("$.type", is(beerDTO.getType().toString())));
	}
}