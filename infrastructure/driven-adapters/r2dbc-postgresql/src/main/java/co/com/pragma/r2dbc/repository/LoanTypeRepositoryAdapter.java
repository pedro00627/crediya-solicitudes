package co.com.pragma.r2dbc.repository;

import co.com.pragma.model.loantype.LoanType;
import co.com.pragma.model.loantype.gateways.LoanTypeGateway;
import co.com.pragma.model.log.gateways.LoggerPort;
import co.com.pragma.r2dbc.interfaces.LoanTypeDataRepository;
import co.com.pragma.r2dbc.mapper.LoanTypeMapper;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Set;

@Repository
public class LoanTypeRepositoryAdapter extends AbstractCachedRepositoryAdapter<LoanType> implements LoanTypeGateway {

    private final LoanTypeDataRepository repository;
    private final LoanTypeMapper loanTypeMapper;

    public LoanTypeRepositoryAdapter(LoanTypeDataRepository repository, LoanTypeMapper loanTypeMapper, CacheManager cacheManager, LoggerPort logger) {
        super(cacheManager, logger);
        this.repository = repository;
        this.loanTypeMapper = loanTypeMapper;
    }

    @Override
    protected String getCacheName() {
        return "loan_types";
    }

    @Override
    protected String getEntityNameForLogging() {
        return "TIPO DE PRÉSTAMO";
    }

    @Override
    protected Class<LoanType> getDomainClass() {
        return LoanType.class;
    }

    @Override
    protected Object getEntityId(LoanType entity) {
        return entity.getLoanTypeId();
    }

    @Override
    protected String getEntityName(LoanType entity) {
        return entity.getName();
    }

    @Override
    protected Mono<LoanType> fetchByIdFromDatabase(Object id) {
        return repository.findById((Integer) id)
                .map(loanTypeMapper::toDomain);
    }

    @Override
    protected Mono<LoanType> fetchByNameFromDatabase(String name) {
        return repository.findByName(name)
                .map(loanTypeMapper::toDomain);
    }

    @Override
    public Flux<LoanType> findAllByIds(Set<Integer> ids) {
        return repository.findAllByLoanTypeIdIn(ids).map(loanTypeMapper::toDomain);    }

    // Implementación explícita de los métodos de LoanTypeGateway
    @Override
    public Mono<LoanType> findById(Integer id) {
        return super.findById(id);
    }

    @Override
    public Mono<LoanType> findByName(String loanTypeName) {
        return super.findByName(loanTypeName);
    }
}
