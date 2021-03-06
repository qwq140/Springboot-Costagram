package com.cos.costagram.service;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.Query;

import org.qlrm.mapper.JpaResultMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.cos.costagram.domain.follow.Follow;
import com.cos.costagram.domain.follow.FollowRepository;
import com.cos.costagram.web.dto.follow.FollowListRespDto;

import lombok.RequiredArgsConstructor;


@RequiredArgsConstructor
@Service
public class FollowService {
	
	private final FollowRepository followRepository;
	private final EntityManager em;
	
	@Transactional
	public int 팔로우(int fromUserId, int toUserId) {
		return followRepository.mFollow(fromUserId, toUserId);
	}
	
	@Transactional
	public int 언팔로우(int fromUserId, int toUserId) {
		return followRepository.mUnFollow(fromUserId, toUserId);
	}
	
	@Transactional(readOnly = true)
	public List<FollowListRespDto> 팔로우리스트(int pageUserId, int principalId){
		StringBuffer sb = new StringBuffer();
		sb.append("select u.id userId, u.username, u.profileImageUrl, ");
		sb.append("if( (select true from follow where fromUserId = ? and toUserId = u.id), true, false) followState, "); // principalDetails.user.id
		sb.append("if( u.id = ?, true, false ) equalState "); // principalDetails.user.id
		sb.append("from follow f inner join user u on u.id = f.toUserId ");
		sb.append("where f.fromUserId = ? "); // pageUserId
		
		Query q = em.createNativeQuery(sb.toString())
				.setParameter(1, principalId)
				.setParameter(2, principalId)
				.setParameter(3, pageUserId);
		
		JpaResultMapper result = new JpaResultMapper();
		List<FollowListRespDto> followList = result.list(q, FollowListRespDto.class);
		
		return followList;
	}
	
}
