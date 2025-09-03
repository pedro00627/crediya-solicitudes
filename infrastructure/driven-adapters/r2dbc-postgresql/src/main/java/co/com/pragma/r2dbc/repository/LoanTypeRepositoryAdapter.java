package co.com.pragma.r2dbc.repository;

import co.com.pragma.model.loantype.LoanType;
import co.com.pragma.model.loantype.gateways.LoanTypeGateway;
import co.com.pragma.r2dbc.interfaces.LoanTypeDataRepository;
import co.com.pragma.r2dbc.mapper.LoanTypeMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

@Slf4j
@Repository
@RequiredArgsConstructor
public class LoanTypeRepositoryAdapter implements LoanTypeGateway {

    private final LoanTypeDataRepository repository;
    private final LoanTypeMapper loanTypeMapper;

    @Override
    public Mono<LoanType> findById(Integer id) {
        return Mono.fromCallable(() -> findByIdAndCache(id))
                .subscribeOn(Schedulers.boundedElastic())
                .flatMap(Mono::justOrEmpty);
    }

    @Cacheable(value = "loanTypes", key = "#id")
    public LoanType findByIdAndCache(Integer id) {
        log.info("==> CACHE MISS. Consultando TIPO DE PRÉSTAMO desde la BD con ID: {}", id);
        return repository.findById(id)
                .map(loanTypeMapper::toDomain)
                .block();
    }
}