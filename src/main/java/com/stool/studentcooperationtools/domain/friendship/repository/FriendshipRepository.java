package com.stool.studentcooperationtools.domain.friendship.repository;

import com.stool.studentcooperationtools.domain.friendship.Friendship;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface FriendshipRepository extends JpaRepository<Friendship, Long> {
    @Modifying
    @Query(value = "delete from Friendship f where f.me.id = :memberId and f.friend.id = :friendId")
    public void deleteByFriendId(@Param("memberId") Long memberId, @Param("friendId")Long friendId);
}
