package com.example.squarespool.repository;

import com.example.squarespool.model.Square;
import com.example.squarespool.model.SquareStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface SquareRepository extends JpaRepository<Square, Long> {
    List<Square> findByBoardId(Long boardId);

    Optional<Square> findByBoardIdAndIdx(Long boardId, int idx);

    long countByBoardIdAndStatus(Long boardId, SquareStatus status);

    @Query("select s from Square s where s.status = 'RESERVED' and s.reservedUntil < :now")
    List<Square> findExpiredReservations(@Param("now") Instant now);
}
