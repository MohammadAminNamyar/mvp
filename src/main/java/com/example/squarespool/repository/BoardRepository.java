package com.example.squarespool.repository;

import com.example.squarespool.model.Board;
import com.example.squarespool.model.BoardStatus;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface BoardRepository extends JpaRepository<Board, Long> {
    @Query("select distinct b.sportType from Board b where b.sportType is not null and b.sportType <> ''")
    List<String> findDistinctSportTypes();

    List<Board> findBySportTypeIgnoreCase(String sportType);

    List<Board> findByGameIdAndPriceCentsOrderByCreatedAtAsc(String gameId, int priceCents);

    long countByGameIdAndPriceCents(String gameId, int priceCents);

    @Query("select distinct b.priceCents from Board b where b.gameId = :gameId order by b.priceCents")
    List<Integer> findDistinctPriceCentsByGameIdOrderByPriceCentsAsc(@Param("gameId") String gameId);

    @Query("select distinct b.priceCents from Board b order by b.priceCents")
    List<Integer> findDistinctPriceCentsOrderByPriceCentsAsc();

    List<Board> findByStatus(BoardStatus status);
}
