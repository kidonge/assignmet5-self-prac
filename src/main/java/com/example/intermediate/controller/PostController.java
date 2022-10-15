package com.example.intermediate.controller;

import com.example.intermediate.controller.request.PostRequestDto;
import com.example.intermediate.controller.response.ResponseDto;
import com.example.intermediate.service.PostService;
import javax.servlet.http.HttpServletRequest;

import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RequestMapping(value = "/api")
@RequiredArgsConstructor
@RestController
public class PostController {

  private final PostService postService;

  @ApiImplicitParams({
          @ApiImplicitParam(
                  name = "Refresh-Token",
                  required = true,
                  dataType = "string",
                  paramType = "header"
          )
  })

  // 게시물 작성
  @PostMapping(value = "/auth/post")
  public ResponseDto<?> createPost(@RequestBody PostRequestDto requestDto,
      HttpServletRequest request) {
    return postService.createPost(requestDto, request);
  }

  // 게시물 id로 게시물 조회
  @GetMapping(value = "/post/{id}")
  public ResponseDto<?> getPost(@PathVariable Long id) {
    return postService.getPost(id);
  }


  // 내가 쓴 게시물 조회 ----------------------------------------------------------------
  @GetMapping(value = "/auth/posts") // 내가 썼다는 것을 알기위해 Member의 id? nickname으로 조회(?)
  public ResponseDto<?> getMyPost(HttpServletRequest request){
    return postService.getMyPost(request);
  }



  // 모든 게시물 조회
  @GetMapping(value = "/post")
  public ResponseDto<?> getAllPosts() {
    return postService.getAllPost();
  }

  // 게시물 수정
  @PutMapping(value = "/auth/post/{id}")
  public ResponseDto<?> updatePost(@PathVariable Long id, @RequestBody PostRequestDto postRequestDto,
      HttpServletRequest request) {
    return postService.updatePost(id, postRequestDto, request);
  }

  // 게시물 삭제
  @DeleteMapping(value = "/auth/post/{id}")
  public ResponseDto<?> deletePost(@PathVariable Long id,
      HttpServletRequest request) {
    return postService.deletePost(id, request);
  }

}
