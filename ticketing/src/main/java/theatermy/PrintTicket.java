package theatermy;

import javax.persistence.*;
import java.util.List;

@Entity
@Table(name="PrintTicket_table")
public class PrintTicket {

        @Id
        @GeneratedValue(strategy=GenerationType.AUTO)
        private Long id;


        public Long getId() {
            return id;
        }

        public void setId(Long id) {
            this.id = id;
        }

}
