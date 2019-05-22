package guru.sfg.brewery.services;

import guru.sfg.brewery.web.model.BeerDto;

import java.util.Optional;
import java.util.UUID;

public interface BeerService {

    Optional<BeerDto> getBeerById(UUID uuid);
}
