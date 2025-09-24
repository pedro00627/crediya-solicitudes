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

    public LoanTypeRepositoryAdapter(final LoanTypeDataRepository repository, final LoanTypeMapper loanTypeMapper, final CacheManager cacheManager, final LoggerPort logger) {
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
    protected Object getEntityId(final LoanType entity) {
        return entity.getLoanTypeId();
    }

    @Override
    protected String getEntityName(final LoanType entity) {
        return entity.getName();
    }

    @Override
    protected Mono<LoanType> fetchByIdFromDatabase(final Object id) {
        return this.repository.findById((Integer) id)
                .map(this.loanTypeMapper::toDomain);
    }

    @Override
    protected Mono<LoanType> fetchByNameFromDatabase(final String name) {
        return this.repository.findByName(name)
                .map(this.loanTypeMapper::toDomain);
    }

    @Override
    public Flux<LoanType> findAllByIds(final Set<Integer> ids) {
        return this.repository.findAllByLoanTypeIdIn(ids).map(this.loanTypeMapper::toDomain);
    }

    // Implementación explícita de los métodos de LoanTypeGateway
    @Override
    public Mono<LoanType> findById(final Integer id) {
        return super.findById(id);
    }

    @Override
    public Mono<LoanType> findByName(final String loanTypeName) {
        return super.findByName(loanTypeName);
    }
}
