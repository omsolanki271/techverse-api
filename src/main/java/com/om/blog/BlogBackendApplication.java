package com.om.blog;

import com.om.blog.entities.Post;
import com.om.blog.payloads.PostDto;
import org.modelmapper.ModelMapper;
import org.modelmapper.convention.MatchingStrategies;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class BlogBackendApplication {

	public static void main(String[] args) {
		SpringApplication.run(BlogBackendApplication.class, args);
	}

	@Bean
	public ModelMapper modelMapper()
	{
		ModelMapper modelMapper = new ModelMapper();

		modelMapper.getConfiguration()
				.setMatchingStrategy(MatchingStrategies.STRICT);

		// Disambiguate mediaId mapping so ModelMapper does not confuse Post.media.publicId with mediaId
		modelMapper.typeMap(Post.class, PostDto.class).addMappings(mapper -> {
			mapper.skip(PostDto::setMediaId);
		});

		return modelMapper;
	}

}
