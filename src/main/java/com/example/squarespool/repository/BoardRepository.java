package com.example.squarespool.repository;

import com.example.squarespool.model.Board;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface BoardRepository extends JpaRepository<Board, Long> {
    @Query("select coalesce(max(b.boardNumber), 0) from Board b where b.sportType = :sportType and b.gameName = :gameName and b.priceCents = :priceCents")
    int findMaxBoardNumber(@Param("sportType") String sportType,
                           @Param("gameName") String gameName,
                           @Param("priceCents") int priceCents);
}
