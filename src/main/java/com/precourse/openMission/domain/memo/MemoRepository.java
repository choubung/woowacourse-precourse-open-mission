package com.precourse.openMission.domain.memo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface MemoRepository extends JpaRepository<Memo, Long> {
    @Query("SELECT m FROM Memo m JOIN FETCH m.user WHERE m.scope = 'PUBLIC' ORDER BY m.memoDate DESC")
    List<Memo> findAllPublicDesc();
}
