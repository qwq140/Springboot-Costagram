package com.cos.costagram.web.dto.image;

import org.springframework.web.multipart.MultipartFile;

import com.cos.costagram.domain.image.Image;
import com.cos.costagram.domain.user.User;

import lombok.Data;

@Data
public class ImageReqDto {
	
	private MultipartFile file;
	private String caption;
	
	// #이 포함되어있어야함
	private String Tags; // #태그1 #태그2
	
	public Image toEntity(String postImageUrl, User userEntity) {
		return Image.builder()
				.caption(caption)
				.postImageUrl(postImageUrl)
				.user(userEntity)
				.build();
	}
}
