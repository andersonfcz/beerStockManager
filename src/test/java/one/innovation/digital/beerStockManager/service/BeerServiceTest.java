package one.innovation.digital.beerStockManager.service;

import one.innovation.digital.beerStockManager.builder.BeerDTOBuilder;
import one.innovation.digital.beerStockManager.dto.BeerDTO;
import one.innovation.digital.beerStockManager.entity.Beer;
import one.innovation.digital.beerStockManager.exception.BeerAlreadyRegisteredException;
import one.innovation.digital.beerStockManager.exception.BeerNotFoundException;
import one.innovation.digital.beerStockManager.exception.BeerStockExceedException;
import one.innovation.digital.beerStockManager.mapper.BeerMapper;
import one.innovation.digital.beerStockManager.repository.BeerRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.hamcrest.core.IsNot.not;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class BeerServiceTest {

    private static final long INVALID_BEER_ID = 1L;

    @Mock
    private BeerRepository beerRepository;

    @InjectMocks
    private BeerService beerService;

    BeerMapper beerMapper = BeerMapper.INSTANCE;

    @Test
    void whenBeerInformedItShouldBeCreated() throws BeerAlreadyRegisteredException {
        BeerDTO beerDTO = BeerDTOBuilder.builder().build().toBeerDTO();
        Beer expectedBeerDTO = beerMapper.toModel(beerDTO);

        when(beerRepository.findByName(beerDTO.getName())).thenReturn(Optional.empty());
        when(beerRepository.save(expectedBeerDTO)).thenReturn(expectedBeerDTO);

        BeerDTO createdBeerDTO = beerService.createBeer(beerDTO);

        assertThat(createdBeerDTO.getId(), is(equalTo(beerDTO.getId())));
        assertThat(createdBeerDTO.getName(), is(equalTo(beerDTO.getName())));
        assertThat(createdBeerDTO.getQuantity(), is(equalTo(beerDTO.getQuantity())));
    }

    @Test
    void whenDuplicatedNamesAnExceptionShoudBeThrown() {
        BeerDTO beerDTO = BeerDTOBuilder.builder().build().toBeerDTO();
        Beer duplicatedBeerDTO = beerMapper.toModel(beerDTO);

        when(beerRepository.findByName(beerDTO.getName())).thenReturn(Optional.of(duplicatedBeerDTO));

        assertThrows(BeerAlreadyRegisteredException.class, () -> beerService.createBeer(beerDTO));
    }

    @Test
    void whenValidBeerNameIsGivenThenReturnABeer() throws BeerNotFoundException {
        // given
        BeerDTO expectedFoundBeerDTO = BeerDTOBuilder.builder().build().toBeerDTO();
        Beer expectedFoundBeer = beerMapper.toModel(expectedFoundBeerDTO);

        // when
        when(beerRepository.findByName(expectedFoundBeer.getName())).thenReturn(Optional.of(expectedFoundBeer));

        // then
        BeerDTO foundBeerDTO = beerService.getByName(expectedFoundBeerDTO.getName());

        assertThat(foundBeerDTO, is(equalTo(expectedFoundBeerDTO)));
    }

    @Test
    void whenNotRegisteredBeerNameIsGivenThenThrowAnException() {
        // given
        BeerDTO expectedFoundBeerDTO = BeerDTOBuilder.builder().build().toBeerDTO();

        // when
        when(beerRepository.findByName(expectedFoundBeerDTO.getName())).thenReturn(Optional.empty());

        // then
        assertThrows(BeerNotFoundException.class, () -> beerService.getByName(expectedFoundBeerDTO.getName()));
    }
    @Test
    void whenListBeerIsCalledThenReturnAListOfBeers() {
        // given
        BeerDTO expectedFoundBeerDTO = BeerDTOBuilder.builder().build().toBeerDTO();
        Beer expectedFoundBeer = beerMapper.toModel(expectedFoundBeerDTO);

        //when
        when(beerRepository.findAll()).thenReturn(Collections.singletonList(expectedFoundBeer));

        //then
        List<BeerDTO> foundListBeersDTO = beerService.getAll();

        assertThat(foundListBeersDTO, is(not(empty())));
        assertThat(foundListBeersDTO.get(0), is(equalTo(expectedFoundBeerDTO)));
    }

    @Test
    void whenListBeerIsCalledThenReturnAnEmptyListOfBeers() {
        //when
        when(beerRepository.findAll()).thenReturn(Collections.EMPTY_LIST);

        //then
        List<BeerDTO> foundListBeersDTO = beerService.getAll();

        assertThat(foundListBeersDTO, is(empty()));
    }

    @Test
    void whenExclusionIsCalledWithValidIdThenABeerShouldBeDeleted() throws BeerNotFoundException{
        // given
        BeerDTO expectedDeletedBeerDTO = BeerDTOBuilder.builder().build().toBeerDTO();
        Beer expectedDeletedBeer = beerMapper.toModel(expectedDeletedBeerDTO);

        // when
        when(beerRepository.findById(expectedDeletedBeerDTO.getId())).thenReturn(Optional.of(expectedDeletedBeer));
        doNothing().when(beerRepository).deleteById(expectedDeletedBeerDTO.getId());

        // then
        beerService.deleteById(expectedDeletedBeerDTO.getId());

        verify(beerRepository, times(1)).findById(expectedDeletedBeerDTO.getId());
        verify(beerRepository, times(1)).deleteById(expectedDeletedBeerDTO.getId());
    }

    @Test
    void whenIncrementIsCalledThenIncrementBeerStock() throws BeerNotFoundException, BeerStockExceedException {
        //given
        BeerDTO expectedBeerDTO = BeerDTOBuilder.builder().build().toBeerDTO();
        Beer expectedBeer = beerMapper.toModel(expectedBeerDTO);

        //when
        when(beerRepository.findById(expectedBeerDTO.getId())).thenReturn(Optional.of(expectedBeer));
        when(beerRepository.save(expectedBeer)).thenReturn(expectedBeer);

        int quantityToIncrement = 10;
        int expectedQuantityAfterIncrement = expectedBeerDTO.getQuantity() + quantityToIncrement;

        // then
        BeerDTO incrementedBeerDTO = beerService.increment(expectedBeerDTO.getId(), quantityToIncrement);

        assertThat(expectedQuantityAfterIncrement, equalTo(incrementedBeerDTO.getQuantity()));
        assertThat(expectedQuantityAfterIncrement, lessThan(expectedBeerDTO.getMax()));
    }

    @Test
    void whenIncrementIsGreatherThanMaxThenThrowException() {
        BeerDTO expectedBeerDTO = BeerDTOBuilder.builder().build().toBeerDTO();
        Beer expectedBeer = beerMapper.toModel(expectedBeerDTO);

        when(beerRepository.findById(expectedBeerDTO.getId())).thenReturn(Optional.of(expectedBeer));

        int quantityToIncrement = 80;
        assertThrows(BeerStockExceedException.class, () -> beerService.increment(expectedBeerDTO.getId(), quantityToIncrement));
    }

    @Test
    void whenIncrementAfterSumIsGreatherThanMaxThenThrowException() {
        BeerDTO expectedBeerDTO = BeerDTOBuilder.builder().build().toBeerDTO();
        Beer expectedBeer = beerMapper.toModel(expectedBeerDTO);

        when(beerRepository.findById(expectedBeerDTO.getId())).thenReturn(Optional.of(expectedBeer));

        int quantityToIncrement = 45;
        assertThrows(BeerStockExceedException.class, () -> beerService.increment(expectedBeerDTO.getId(), quantityToIncrement));
    }

    @Test
    void whenIncrementIsCalledWithInvalidIdThenThrowException() {
        int quantityToIncrement = 10;

        when(beerRepository.findById(INVALID_BEER_ID)).thenReturn(Optional.empty());

        assertThrows(BeerNotFoundException.class, () -> beerService.increment(INVALID_BEER_ID, quantityToIncrement));
    }

    @Test
    void whenDecrementIsCalledThenDecrementBeerStock() throws BeerNotFoundException, BeerStockExceedException {
        BeerDTO expectedBeerDTO = BeerDTOBuilder.builder().build().toBeerDTO();
        Beer expectedBeer = beerMapper.toModel(expectedBeerDTO);

        when(beerRepository.findById(expectedBeerDTO.getId())).thenReturn(Optional.of(expectedBeer));
        when(beerRepository.save(expectedBeer)).thenReturn(expectedBeer);

        int quantityToDecrement = 5;
        int expectedQuantityAfterDecrement = expectedBeerDTO.getQuantity() - quantityToDecrement;
        BeerDTO incrementedBeerDTO = beerService.decrement(expectedBeerDTO.getId(), quantityToDecrement);

        assertThat(expectedQuantityAfterDecrement, equalTo(incrementedBeerDTO.getQuantity()));
        assertThat(expectedQuantityAfterDecrement, greaterThan(0));
    }

    @Test
    void whenDecrementIsCalledToEmptyStockThenEmptyBeerStock() throws BeerNotFoundException, BeerStockExceedException {
        BeerDTO expectedBeerDTO = BeerDTOBuilder.builder().build().toBeerDTO();
        Beer expectedBeer = beerMapper.toModel(expectedBeerDTO);

        when(beerRepository.findById(expectedBeerDTO.getId())).thenReturn(Optional.of(expectedBeer));
        when(beerRepository.save(expectedBeer)).thenReturn(expectedBeer);

        int quantityToDecrement = 10;
        int expectedQuantityAfterDecrement = expectedBeerDTO.getQuantity() - quantityToDecrement;
        BeerDTO incrementedBeerDTO = beerService.decrement(expectedBeerDTO.getId(), quantityToDecrement);

        assertThat(expectedQuantityAfterDecrement, equalTo(0));
        assertThat(expectedQuantityAfterDecrement, equalTo(incrementedBeerDTO.getQuantity()));
    }

    @Test
    void whenDecrementIsLowerThanZeroThenThrowException() {
        BeerDTO expectedBeerDTO = BeerDTOBuilder.builder().build().toBeerDTO();
        Beer expectedBeer = beerMapper.toModel(expectedBeerDTO);

        when(beerRepository.findById(expectedBeerDTO.getId())).thenReturn(Optional.of(expectedBeer));

        int quantityToDecrement = 80;
        assertThrows(BeerStockExceedException.class, () -> beerService.decrement(expectedBeerDTO.getId(), quantityToDecrement));
    }

    @Test
    void whenDecrementIsCalledWithInvalidIdThenThrowException() {
        int quantityToDecrement = 10;

        when(beerRepository.findById(INVALID_BEER_ID)).thenReturn(Optional.empty());

        assertThrows(BeerNotFoundException.class, () -> beerService.decrement(INVALID_BEER_ID, quantityToDecrement));
    }

}
