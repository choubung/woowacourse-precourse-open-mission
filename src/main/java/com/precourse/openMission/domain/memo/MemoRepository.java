package com.precourse.openMission.domain.memo;

import com.precourse.openMission.domain.user.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface MemoRepository extends JpaRepository<Memo, Long> {
    @Query("SELECT m FROM Memo m JOIN FETCH m.user WHERE m.scope = 'PUBLIC' ORDER BY m.memoDate DESC")
    List<Memo> findAllPublicDesc();

    @Query("SELECT m FROM Memo m JOIN FETCH m.user WHERE m.scope = 'PUBLIC' OR (m.scope = 'SECRET' AND m.user = :user) ORDER BY m.memoDate DESC")
    List<Memo> findAllPublicAndMySecretDesc(@Param("user") User user);

    @Query("SELECT m FROM Memo m JOIN FETCH m.user ORDER BY m.memoDate DESC")
    List<Memo> findAllDesc();
}
