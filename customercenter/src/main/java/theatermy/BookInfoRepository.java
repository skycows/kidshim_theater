package theatermy;

import java.util.Optional;

import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

@RepositoryRestResource(collectionResourceRel = "bookInfos", path = "bookInfos")
public interface BookInfoRepository extends PagingAndSortingRepository<BookInfo, String> {

  Optional<BookInfo> findByBookId(String bookId);
}
