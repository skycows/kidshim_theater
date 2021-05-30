package theater;

import lombok.Data;

import javax.persistence.*;

@Entity
@Table(name = "RetrieveMovie_table")
@Data
public class RetrieveMovie {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
}
