package com.cos.costagram.utils;

import java.util.ArrayList;
import java.util.List;

import com.cos.costagram.domain.image.Image;
import com.cos.costagram.domain.tag.Tag;

public class TagUtils {
	
	public static List<Tag> parsingToTagObject(String tags, Image imageEntity){
		String temp[] = tags.split("#");
		List<Tag> list = new ArrayList<>();
		
		for(String tagName : temp) {
			Tag tag = Tag.builder()
					.name(tagName.trim())
					.image(imageEntity)
					.build();
			list.add(tag);
		}
		
		return list;
	}
	
}
