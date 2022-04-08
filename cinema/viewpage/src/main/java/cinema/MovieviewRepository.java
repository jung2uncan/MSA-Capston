package cinema;

import org.springframework.data.repository.CrudRepository;
//import org.springframework.data.repository.query.Param;

import java.util.List;

public interface MovieviewRepository extends CrudRepository<Movieview, Long> {

    List<Movieview> findByRsvId(Long rsvId);
    List<Movieview> findByPayId(Long payId);

}