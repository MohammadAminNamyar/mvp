package com.example.squarespool.repository;

import com.example.squarespool.model.Board;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface BoardRepository extends JpaRepository<Board, Long> {
    @Query("select distinct b.sportType from Board b where b.sportType is not null and b.sportType <> ''")
    List<String> findDistinctSportTypes();

    List<Board> findBySportTypeIgnoreCase(String sportType);

    List<Board> findByGameIdAndPriceCentsOrderByCreatedAtAsc(String gameId, int priceCents);

    long countByGameIdAndPriceCents(String gameId, int priceCents);
}
