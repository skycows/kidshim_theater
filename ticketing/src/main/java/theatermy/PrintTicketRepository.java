package theatermy;

import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface PrintTicketRepository extends CrudRepository<PrintTicket, Long> {


}