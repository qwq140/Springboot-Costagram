package com.cos.costagram.domain.follow;


import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

public interface FollowRepository extends JpaRepository<Follow, Integer>{

	// insert, delete (@Transactional)
	// update (@Modifying)
	
	@Modifying
	@Query(value = "INSERT INTO follow(fromUserId, toUserId, createDate) VALUES(:fromUserId, :toUserId, now())", nativeQuery = true)
	int mFollow(int fromUserId, int toUserId);
	
	@Modifying
	@Query(value = "DELETE FROM follow where fromUserId = :fromUserId AND toUserId = :toUserId", nativeQuery = true)
	int mUnfollow(int fromUserId, int toUserId);
}
