package com.cos.costagram.web;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;

import com.cos.costagram.config.auth.PrincipalDetails;
import com.cos.costagram.domain.comment.Comment;
import com.cos.costagram.domain.image.Image;
import com.cos.costagram.service.CommentService;
import com.cos.costagram.service.ImageService;
import com.cos.costagram.service.LikesService;
import com.cos.costagram.web.dto.CMRespDto;
import com.cos.costagram.web.dto.image.ImageReqDto;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Controller
public class ImageController {


	private final ImageService imageService;
	private final LikesService likesService;
	private final CommentService commentService;
	
	@GetMapping({"/", "/image/feed"})
	public String feed() {
		
		return "image/feed";
	}
	
	@GetMapping("/image")
	public @ResponseBody CMRespDto<?> image(Model model, @AuthenticationPrincipal PrincipalDetails principalDetails, @PageableDefault(size=3, sort="id", direction = Sort.Direction.DESC) Pageable pageable) {
		
		Page<Image> pages = imageService.피드이미지(principalDetails.getUser().getId(),pageable);
	
		return new CMRespDto<>(1,pages); // MessageConverter 발동 = Jackson = 무한참조
	}
	
	@GetMapping("/image/explore")
	public String explore(Model model, @AuthenticationPrincipal PrincipalDetails principalDetails) {
		
		List<Image> images = imageService.인기사진(principalDetails.getUser().getId());
		model.addAttribute("images", images);
		return "image/explore";
	}
	
	@GetMapping("/image/upload")
	public String upload() {
		return "image/upload";
	}
	
	@PostMapping("/image")
	public String image(ImageReqDto imageReqDto, @AuthenticationPrincipal PrincipalDetails principalDetails) {
		imageService.사진업로드(imageReqDto,principalDetails);
		
		return "redirect:/user/"+principalDetails.getUser().getId();
	}
	
	@PostMapping("/image/{imageId}/likes")
	public @ResponseBody CMRespDto<?> like(@PathVariable int imageId, @AuthenticationPrincipal PrincipalDetails principalDetails){
		likesService.좋아요(imageId, principalDetails.getUser().getId());
		
		return new CMRespDto<>(1,null);
	}
	
	@DeleteMapping("/image/{imageId}/likes")
	public @ResponseBody CMRespDto<?> unLike(@PathVariable int imageId, @AuthenticationPrincipal PrincipalDetails principalDetails){
		likesService.싫어요(imageId, principalDetails.getUser().getId());
		
		return new CMRespDto<>(1,null);
	}
	
	@PostMapping("/image/{imageId}/comment")
	public @ResponseBody CMRespDto<?> save(@PathVariable int imageId, @RequestBody String content, @AuthenticationPrincipal PrincipalDetails principalDetails){   // content, imageId, userId(세션)
		System.out.println("진입함 ~~~~~~~~~~~~~~~~~~~~~~");
		Comment commentEntity = commentService.댓글쓰기(principalDetails.getUser(), content, imageId);
		
		return new CMRespDto<>(1, commentEntity);
	}
}
