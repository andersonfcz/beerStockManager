package one.innovation.digital.beerStockManager.service;

import one.innovation.digital.beerStockManager.builder.BeerDTOBuilder;
import one.innovation.digital.beerStockManager.dto.BeerDTO;
import one.innovation.digital.beerStockManager.entity.Beer;
import one.innovation.digital.beerStockManager.exception.BeerAlreadyRegisteredException;
import one.innovation.digital.beerStockManager.exception.BeerNotFoundException;
import one.innovation.digital.beerStockManager.mapper.BeerMapper;
import one.innovation.digital.beerStockManager.repository.BeerRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class BeerServiceTest {

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
    void whenDuplicatedNamesAnExceptionShoudBeThrown() throws BeerAlreadyRegisteredException {
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
}
