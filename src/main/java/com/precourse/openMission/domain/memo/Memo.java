package com.precourse.openMission.domain.memo;

import com.precourse.openMission.domain.BaseTimeEntity;
import com.precourse.openMission.domain.user.User;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@Entity
@Table(name = "memo")
public class Memo extends BaseTimeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "memo_id")
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MemoScope scope;

    @Column(length = 200, nullable = false)
    private String content;

    @Column(name = "memo_date", nullable = false)
    private LocalDateTime memoDate;

    @Builder
    public Memo(User user, String scope, String content, LocalDateTime memoDate) {
        this.user = user;
        this.scope = MemoScope.valueOf(scope);
        this.content = content;
        this.memoDate = memoDate;
    }

    public void update(String scope, String content, LocalDateTime memoDate) {
        this.scope = MemoScope.valueOf(scope);
        this.content = content;
        this.memoDate = memoDate;
    }
}
