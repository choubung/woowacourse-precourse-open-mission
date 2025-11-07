package com.precourse.openMission.domain.memo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface MemoRepository extends JpaRepository<Memo, Long> {
    @Query("SELECT m FROM Memo m where m.scope = '전체공개' ORDER BY m.memoDate desc")
    List<Memo> findAllPublicDesc();
}
