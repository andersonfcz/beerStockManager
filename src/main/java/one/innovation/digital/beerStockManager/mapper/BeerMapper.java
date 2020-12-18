package one.innovation.digital.beerStockManager.mapper;

import one.innovation.digital.beerStockManager.dto.BeerDTO;
import one.innovation.digital.beerStockManager.entity.Beer;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper
public interface BeerMapper {
    BeerMapper INSTANCE = Mappers.getMapper(BeerMapper.class);

    Beer toModel(BeerDTO beerDTO);
    BeerDTO toDTO(Beer beer);
}
