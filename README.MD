# 인스타그램 클론 코딩

### 의존성

- Spring Boot DevTools
- Lombok
- Spring Data JPA
- MySQL Driver
- Spring Web
- Spring Security
- oauth2-client

```xml
<dependency>
	<groupId>org.qlrm</groupId>
	<artifactId>qlrm</artifactId>
	<version>2.1.1</version>
</dependency>

<!-- 시큐리티 태그 라이브러리 -->
<dependency>
	<groupId>org.springframework.security</groupId>
	<artifactId>spring-security-taglibs</artifactId>
</dependency>

<!-- JSP 템플릿 엔진 -->
<dependency>
	<groupId>org.apache.tomcat</groupId>
	<artifactId>tomcat-jasper</artifactId>
	<version>9.0.44</version>
</dependency>

<!-- JSTL -->
<dependency>
	<groupId>javax.servlet</groupId>
	<artifactId>jstl</artifactId>
</dependency>
```

### 데이터베이스
```sql
CREATE USER 'costa'@'%' identified by '1234';
GRANT ALL privileges on *.* to 'costa'@'%';
CREATE DATABASE costa;
```

### yml 설정

```yml
server:
  port: 8080
  servlet:
    context-path: /
    encoding:
      charset: utf-8
      enabled: true
    
spring:
  mvc:
    view:
      prefix: /WEB-INF/views/
      suffix: .jsp
      
  datasource:
    driver-class-name: com.mysql.cj.jdbc.Driver
    url: jdbc:mysql://localhost:3306/costa?serverTimezone=Asia/Seoul&useSSL=false&allowPublicKeyRetrieval=true
    username: costa
    password: 1234
    
  jpa:
    open-in-view: true
    hibernate:
      ddl-auto: update
      naming:
        physical-strategy: org.hibernate.boot.model.naming.PhysicalNamingStrategyStandardImpl
    show-sql: true
      
  servlet:
    multipart:
      enabled: true
      max-file-size: 10MB

  security:
    user:
      name: test
      password: 1234
    
    oauth2:
      client:
        registration:
          facebook:
            client-id: 274646820792424
            client-secret: 1e27da1263742df4f0e86128de181d23
            scope:
            - email
            - public_profile   

file:
  path: C:/Users/jap/Desktop/workspace/springwork/costagram/src/main/resources/upload/
```

### 태그 라이브러리
- <%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
- <%@ taglib prefix="sec" uri="http://www.springframework.org/security/tags"%>

### 영상 링크
https://youtu.be/LeJV736a7NU

### 시큐리티

```java
@RequiredArgsConstructor
@Configuration
@EnableWebSecurity
public class SecurityConfig extends WebSecurityConfigurerAdapter{

	private final OAuth2DetailsService oAuth2DetailsService;
	
	@Bean
	public BCryptPasswordEncoder encode() {
		return new BCryptPasswordEncoder();
	}
	
	// 모델 : Image, User, Likes, Follow, Tag : 인증 필요함.
	// auth 주소 : 인증 필요없음.
	// static 폴더
	@Override
	protected void configure(HttpSecurity http) throws Exception {
		http.csrf().disable();
		http.authorizeRequests()
			.antMatchers("/", "/user/**", "/image/**", "/follow/**, /comment/**").access("hasRole('ROLE_USER') or hasRole('ROLE_ADMIN')")
			.antMatchers("/admin/**").access("hasRole('ROLE_ADMIN')")
			.anyRequest()
			.permitAll()
			.and()
			.formLogin()
			.loginPage("/auth/loginForm")
			.loginProcessingUrl("/login") // post /login 주소를 디스패처 확인하면 필터가 낚아챔
			.defaultSuccessUrl("/")
			.and()
			.oauth2Login()
			.userInfoEndpoint()
			.userService(oAuth2DetailsService);
	}
}
```

### 회원가입
![joinform](https://user-images.githubusercontent.com/74044232/115328537-b8746180-a1cb-11eb-8910-4f86290323f7.png)


### 로그인
![login](https://user-images.githubusercontent.com/74044232/115329232-fc1b9b00-a1cc-11eb-9b13-33a7523a63ac.png)

- 페이스북 로그인
```java
private OAuth2User processOAuth2User(OAuth2UserRequest userRequest, OAuth2User oauth2User) {
	//1번 통합 클래스를 생성
	OAuth2UserInfo oAuth2UserInfo = null;
	System.out.println("뭐로 로그인?" + userRequest.getClientRegistration().getClientName());
	if(userRequest.getClientRegistration().getClientName().equals("Facebook")) {
		oAuth2UserInfo=new FacebookInfo(oauth2User.getAttributes());
	} 
		
	// 2번 최초 : 회원가입 + 로그인, 최초x : 로그인
	User userEntity = userRepository.findByUsername(oAuth2UserInfo.getUsername());
		
	UUID uuid = UUID.randomUUID();
	String encPassword = new BCryptPasswordEncoder().encode(uuid.toString());
				
		
	if(userEntity == null) { // DB에 없으면 최초 접속(회원가입)
		User user = User.builder()
				.username(oAuth2UserInfo.getUsername())
				.password(encPassword)
				.email(oAuth2UserInfo.getEmail())
				.role("USER")
				.build();
		userEntity = userRepository.save(user);
		return new PrincipalDetails(userEntity, oauth2User.getAttributes());
	}else {
		return new PrincipalDetails(userEntity, oauth2User.getAttributes());
	}
		
}
```

### 피드페이지(메인페이지)
![feed](https://user-images.githubusercontent.com/74044232/115329416-3b49ec00-a1cd-11eb-8642-89a2eef35e99.png)
팔로우한 유저들의 게시글들을 보는 페이지, 스크롤페이징처리가 되어있다.

- controller code
```java
@GetMapping("/image")
	public @ResponseBody CMRespDto<?> image(Model model, @AuthenticationPrincipal PrincipalDetails principalDetails, @PageableDefault(size=3, sort="id", direction = Sort.Direction.DESC) Pageable pageable) {
		
		Page<Image> pages = imageService.피드이미지(principalDetails.getUser().getId(),pageable);
	
		return new CMRespDto<>(1,pages); // MessageConverter 발동 = Jackson = 무한참조
	}
```
- service code
```java
public Page<Image> 피드이미지(int principalId, Pageable pageable){
		
	// 1. principalId 로 내가 팔로우하고 있는 사용자를 찾아야 됨. (한개이거나 컬렉션이거나)
	// select * from image where userId in (select toUserId from follow where fromUserId = 1);
		
	Page<Image> images = imageRepository.mFeed(principalId, pageable);
		
	// 좋아요 하트 색깔 로직
	images.forEach((image)->{
			
		int likeCount = image.getLikes().size();
		image.setLikeCount(likeCount);
		
		image.getLikes().forEach((like)->{
			if(like.getUser().getId() == principalId) {
				image.setLikeState(true);
			}
		});
	});
	
	return images;
}
```
- 좋아요 기능 (네이티브쿼리 사용)
좋아요 관련 네이티브쿼리 코드
```java
public interface LikesRepository extends JpaRepository<Likes, Integer>{
	// 데이터 변경이 있을때는 @Moidifying 어노테이션 넣기
	@Modifying
	@Query(value = "INSERT INTO likes(imageId, userId) VALUES(:imageId, :principalId)", nativeQuery = true)
	int mSave(int imageId, int principalId);
	
	@Modifying
	@Query(value = "DELETE FROM likes WHERE imageId = :imageId AND userId = :principalId", nativeQuery = true)
	int mDelete(int imageId, int principalId);
}
```
좋아요 비동기통신을 위한 ajax 코드
```javascript
function likeOrUnLike(imageId) {
  let _buttonI = event.target;

  if (_buttonI.classList.contains("far")) {
  
  	$.ajax({
  		type:"POST",
  		url:`/image/${imageId}/likes`,
  		dataType:"json"
  	}).done(res=>{
  		console.log(res);
  		 let likeCountStr  = $(`#like_count_${imageId}`).text();
		 let likeCount = Number(likeCountStr) + 1;
		 $(`#like_count_${imageId}`).text(likeCount);
  	});
  
    _buttonI.classList.add("fas");
    _buttonI.classList.add("active");
    _buttonI.classList.remove("far");
  } else {
  	$.ajax({
  		type:"DELETE",
  		url:`/image/${imageId}/likes`,
  		dataType:"json"
  	}).done(res=>{
  		console.log(res);
  		 let likeCountStr  = $(`#like_count_${imageId}`).text();
		 let likeCount = Number(likeCountStr) - 1;
		 $(`#like_count_${imageId}`).text(likeCount);
  	});
  	
    _buttonI.classList.remove("fas");
    _buttonI.classList.remove("active");
    _buttonI.classList.add("far");
  }
}
```
- 댓글 쓰기, 삭제

### 프로필 페이지
- 해당 유저의 프로필 정보를 보여주는 페이지로 자신의 프로필페이지일 경우 게시물등록, 프로필사진 변경, 회원정보 변경이 가능하다.
![profile](https://user-images.githubusercontent.com/74044232/115331106-494d3c00-a1d0-11eb-968a-42e1e51cd96b.png)

- 팔로우리스트
![Screenshot_26](https://user-images.githubusercontent.com/74044232/115337809-a5b65880-a1dc-11eb-978b-2e2357846322.png)
팔로우, 언팔로우 네이티브쿼리
```java
public interface FollowRepository extends JpaRepository<Follow, Integer>{
	
	@Modifying
	@Query(value = "INSERT INTO follow(fromUserId, toUserId, createDate) VALUES(:fromUserId, :toUserId, now())", nativeQuery = true)
	int mFollow(int fromUserId, int toUserId);
	
	@Modifying
	@Query(value = "DELETE FROM follow where fromUserId = :fromUserId AND toUserId = :toUserId", nativeQuery = true)
	int mUnfollow(int fromUserId, int toUserId);
}
```
팔로우리스트의 경우 user테이블 follow테이블 정보를 혼합해서 그려줘야하므로 qlrm을 사용
```java
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
```

- 프로필사진업로드
![Screenshot_27](https://user-images.githubusercontent.com/74044232/115337836-b23ab100-a1dc-11eb-85a6-9cda0f2a535e.png)

프로필사진업로드 관련 코드
```java
@Value("${file.path}") // application.yml에 접근
private String uploadFolder;

@Transactional
public User 회원사진변경(MultipartFile profileImageFile, PrincipalDetails principalDetails) {
	UUID uuid = UUID.randomUUID();
	String imageFileName = uuid+"_"+profileImageFile.getOriginalFilename();
	//System.out.println("파일명 : "+imageFileName);
	
	Path imageFilePath = Paths.get(uploadFolder+imageFileName);
	//System.out.println("파일패스 : "+imageFilePath);
	try {
		Files.write(imageFilePath, profileImageFile.getBytes());
	} catch (Exception e) {
		// TODO: handle exception
		e.printStackTrace();
	}
	
	User userEntity = userRepository.findById(principalDetails.getUser().getId()).get();
	userEntity.setProfileImageUrl(imageFileName);
	//더티체킹
	
	return userEntity;
}
```

- 회원정보변경, 로그아웃
![Screenshot_25](https://user-images.githubusercontent.com/74044232/115338723-64bf4380-a1de-11eb-9ebe-d83afbeaf7cf.png)
![profileSetting](https://user-images.githubusercontent.com/74044232/115338732-6983f780-a1de-11eb-9dca-97d557724466.png)

- 게시물등록
![upload](https://user-images.githubusercontent.com/74044232/115338817-933d1e80-a1de-11eb-83f2-cfd5c7d490e6.png)

- explore 페이지 (인기사진)
![explore](https://user-images.githubusercontent.com/74044232/115339079-0d6da300-a1df-11eb-8e6b-4018906e5d2e.png)

네이티브쿼리
```java
@Query(value = "select * from image where id in (select imageId from (select imageId, count(imageId) likeCount from likes group by imageId order by 2 desc) t) and userId != :principalId  ", nativeQuery = true)
List<Image> mPopular(int principalId);
```
