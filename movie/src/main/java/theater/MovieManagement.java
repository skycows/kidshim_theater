package theater;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.PostPersist;
import javax.persistence.Table;

import lombok.Getter;
import lombok.Setter;

import org.springframework.beans.BeanUtils;

@Getter
@Setter
@Entity
@Table(name = "MovieManagement_table")
public class MovieManagement {

  @Id
  @GeneratedValue(strategy = GenerationType.AUTO)
  private Long id;
  private String movieId;
  private String title;
  private String status;

  /**
   * Some javadoc.
   *
   * @since Some javadoc. // OK
   * @version Some javadoc. // Violation - wrong order
   * @see Some javadoc. // Violation - wrong order
   * @author Some javadoc. // Violation - wrong order
   */
  @PostPersist
  public void onPostPersist() {
    MovieRegistered movieRegistered = new MovieRegistered();
    BeanUtils.copyProperties(this, movieRegistered);
    movieRegistered.publishAfterCommit();

    System.out.println("========== New movie registered : " + movieRegistered.getTitle());
  }
}
