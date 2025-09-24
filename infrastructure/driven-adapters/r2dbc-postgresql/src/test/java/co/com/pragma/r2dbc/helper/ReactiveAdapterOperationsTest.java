package co.com.pragma.r2dbc.helper;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;
import org.reactivecommons.utils.ObjectMapper;
import org.reactivestreams.Publisher;
import org.springframework.data.repository.query.ReactiveQueryByExampleExecutor;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

class ReactiveAdapterOperationsTest {

    private DummyRepository repository;
    private ObjectMapper mapper;
    private ReactiveAdapterOperations<DummyEntity, DummyData, String, DummyRepository> operations;

    @BeforeEach
    void setUp() {
        this.repository = Mockito.mock(DummyRepository.class);
        this.mapper = Mockito.mock(ObjectMapper.class);
        this.operations = new ReactiveAdapterOperations<DummyEntity, DummyData, String, DummyRepository>(
                this.repository, this.mapper, DummyEntity::toEntity) {
        };
    }

    @Test
    void save() {
        final DummyEntity entity = new DummyEntity("1", "test");
        final DummyData data = new DummyData("1", "test");

        when(this.mapper.map(entity, DummyData.class)).thenReturn(data);
        when(this.repository.save(data)).thenReturn(Mono.just(data));

        StepVerifier.create(this.operations.save(entity))
                .expectNext(entity)
                .verifyComplete();
    }

    @Test
    void saveAllEntities() {
        final DummyEntity entity1 = new DummyEntity("1", "test1");
        final DummyEntity entity2 = new DummyEntity("2", "test2");
        final DummyData data1 = new DummyData("1", "test1");
        final DummyData data2 = new DummyData("2", "test2");

        when(this.mapper.map(entity1, DummyData.class)).thenReturn(data1);
        when(this.mapper.map(entity2, DummyData.class)).thenReturn(data2);
        when(this.repository.saveAll(ArgumentMatchers.<Publisher<DummyData>>any())).thenReturn(Flux.just(data1, data2));

        StepVerifier.create(this.operations.saveAllEntities(Flux.just(entity1, entity2)))
                .expectNext(entity1, entity2)
                .verifyComplete();
    }

    @Test
    void findById() {
        final DummyData data = new DummyData("1", "test");
        final DummyEntity entity = new DummyEntity("1", "test");

        when(this.repository.findById("1")).thenReturn(Mono.just(data));

        StepVerifier.create(this.operations.findById("1"))
                .expectNext(entity)
                .verifyComplete();
    }

    @Test
    void findByExample() {
        final DummyEntity entity = new DummyEntity("1", "test");
        final DummyData data = new DummyData("1", "test");

        when(this.mapper.map(entity, DummyData.class)).thenReturn(data);
        when(this.repository.findAll(any())).thenReturn(Flux.just(data));

        StepVerifier.create(this.operations.findByExample(entity))
                .expectNext(entity)
                .verifyComplete();
    }

    @Test
    void findAll() {
        final DummyData data1 = new DummyData("1", "test1");
        final DummyData data2 = new DummyData("2", "test2");
        final DummyEntity entity1 = new DummyEntity("1", "test1");
        final DummyEntity entity2 = new DummyEntity("2", "test2");

        when(this.repository.findAll()).thenReturn(Flux.just(data1, data2));

        StepVerifier.create(this.operations.findAll())
                .expectNext(entity1, entity2)
                .verifyComplete();
    }

    interface DummyRepository extends ReactiveCrudRepository<DummyData, String>, ReactiveQueryByExampleExecutor<DummyData> {
    }

    record DummyEntity(String id, String name) {

        public static DummyEntity toEntity(final DummyData data) {
            return new DummyEntity(data.id(), data.name());
        }

        @Override
        public boolean equals(final Object o) {
            if (this == o) return true;
            if (null == o || this.getClass() != o.getClass()) return false;
            final DummyEntity that = (DummyEntity) o;
            return this.id.equals(that.id) && this.name.equals(that.name);
        }

    }

    record DummyData(String id, String name) {

        @Override
        public boolean equals(final Object o) {
            if (this == o) return true;
            if (null == o || this.getClass() != o.getClass()) return false;
            final DummyData that = (DummyData) o;
            return this.id.equals(that.id) && this.name.equals(that.name);
        }

    }
}
