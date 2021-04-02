package com.cos.costagram.service;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.cos.costagram.domain.follow.FollowRepository;
import com.cos.costagram.domain.image.ImageRepository;
import com.cos.costagram.domain.user.User;
import com.cos.costagram.domain.user.UserRepository;
import com.cos.costagram.web.dto.user.UserProfileRespDto;
import com.cos.costagram.web.dto.user.UserUpdateReqDto;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Service
public class UserService {

	private final UserRepository userRepository;
	private final FollowRepository followRepository;
	private final BCryptPasswordEncoder bCryptPasswordEncoder;
	
	@Transactional
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
	
	@Transactional
	public User 회원수정(int id, User user) {
		User userEntity = userRepository.findById(id).get();
		
		userEntity.setName(user.getName());
		userEntity.setBio(user.getBio());
		userEntity.setEmail(user.getEmail());
		userEntity.setGender(user.getGender());
		userEntity.setPhone(user.getPhone());
		userEntity.setWebsite(user.getWebsite());
		
		String rawPassword = user.getPassword();
		String encPassword = bCryptPasswordEncoder.encode(rawPassword);
		userEntity.setPassword(encPassword);
		
		return userEntity;
	}
	
}
