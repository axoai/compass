package org.zalando.compass.domain.persistence;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.Optional;

public interface Repository<T, K, C> {

    boolean create(T entity);
    
    default boolean exists(final K id) {
        return find(id).isPresent();
    }

    @Nonnull
    default T read(final K id) throws NotFoundException {
        return find(id).orElseThrow(NotFoundException::new);
    }

    Optional<T> find(K id);

    List<T> findAll();

    List<T> findAll(C criteria);

    boolean update(T entity);

    void delete(K id);

}