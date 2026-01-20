package com.example.squarespool.repository;

import com.example.squarespool.model.Board;
import com.example.squarespool.model.BoardStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BoardRepository extends JpaRepository<Board, Long> {
    List<Board> findByStatus(BoardStatus status);
}
