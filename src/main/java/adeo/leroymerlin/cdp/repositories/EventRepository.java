package adeo.leroymerlin.cdp.repositories;

import adeo.leroymerlin.cdp.models.Event;
import org.springframework.data.repository.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Transactional(readOnly = true)
public interface EventRepository extends Repository<Event, Long> {

    // Implement findById
    Event findById(Long eventId);

    void deleteById(Long eventId);

    List<Event> findAllBy();
}
