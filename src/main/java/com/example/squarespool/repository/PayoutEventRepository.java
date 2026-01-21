package com.example.squarespool.repository;

import com.example.squarespool.model.PayoutEvent;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PayoutEventRepository extends JpaRepository<PayoutEvent, Long> {
    void deleteByBoardId(Long boardId);
}
