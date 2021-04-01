package com.cos.costagram.service;

import org.springframework.stereotype.Service;

import com.cos.costagram.domain.follow.FollowRepository;
import com.cos.costagram.domain.image.ImageRepository;
import com.cos.costagram.domain.user.User;
import com.cos.costagram.domain.user.UserRepository;
import com.cos.costagram.web.dto.user.UserProfileRespDto;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Service
public class UserService {

	private final UserRepository userRepository;
	private final FollowRepository followRepository;
	private final ImageRepository imageRepository;
	
	public UserProfileRespDto 회원프로필(int userId, int principalId) {
		UserProfileRespDto userProfileRespDto = new UserProfileRespDto();
		User userEntity = userRepository.findById(userId).orElseThrow(()->{
			return new IllegalArgumentException();
		});
		
		int followCount = followRepository.followCount(userId);
		int followState = followRepository.followState(userId, principalId);
			
		userProfileRespDto.setFollowState(followState);
		userProfileRespDto.setFollowCount(followCount);
		userProfileRespDto.setImageCount(userEntity.getImages().size());
		
		userEntity.getImages().forEach((image)->{
			image.setLikeCount(image.getLikes().size());
		});
		
		userProfileRespDto.setUser(userEntity);
		
		return userProfileRespDto;
	}
	
}
