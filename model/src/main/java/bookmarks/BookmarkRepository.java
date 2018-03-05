package bookmarks;


import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Collection;

public interface BookmarkRepository extends JpaRepository<Bookmark, Long> {
    @Query
    Collection<Bookmark> findByAccountUsername(String username);
}
