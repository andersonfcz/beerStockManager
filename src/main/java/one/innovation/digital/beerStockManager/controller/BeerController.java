package one.innovation.digital.beerStockManager.controller;

import lombok.AllArgsConstructor;
import one.innovation.digital.beerStockManager.dto.BeerDTO;
import one.innovation.digital.beerStockManager.exception.BeerAlreadyRegisteredException;
import one.innovation.digital.beerStockManager.exception.BeerNotFoundException;
import one.innovation.digital.beerStockManager.service.BeerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/v1/beers")
@AllArgsConstructor(onConstructor = @__(@Autowired))
public class BeerController {

    private final BeerService beerService;

    @GetMapping
    public List<BeerDTO> getAll() {
        return beerService.getAll();
    }

    @GetMapping("/{name}")
    public BeerDTO getByName(@PathVariable String name) throws BeerNotFoundException {
        return beerService.getByName(name);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public BeerDTO createBeer(@RequestBody @Valid BeerDTO beerDTO) throws BeerAlreadyRegisteredException {
        return beerService.createBeer(beerDTO);
    }

    @PutMapping("/{id}")
    public BeerDTO updateBeer(@RequestBody @Valid BeerDTO beerDTO, @ PathVariable Long id) throws BeerNotFoundException {
        return beerService.updateBeer(beerDTO, id);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteById(@PathVariable Long id) throws BeerNotFoundException {
        beerService.deleteById(id);
    }
}
