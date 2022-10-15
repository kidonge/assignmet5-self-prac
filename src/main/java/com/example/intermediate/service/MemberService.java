package com.example.intermediate.service;

import com.example.intermediate.controller.response.MemberResponseDto;
import com.example.intermediate.domain.Member;
import com.example.intermediate.domain.RefreshToken;
import com.example.intermediate.controller.request.LoginRequestDto;
import com.example.intermediate.controller.request.MemberRequestDto;
import com.example.intermediate.controller.response.ResponseDto;
import com.example.intermediate.controller.request.TokenDto;
import com.example.intermediate.jwt.TokenProvider;
import com.example.intermediate.repository.MemberRepository;
import java.util.Optional;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
public class MemberService {

  private final MemberRepository memberRepository;

  private final PasswordEncoder passwordEncoder;
  private final TokenProvider tokenProvider;

  @Transactional
  public ResponseDto<?> createMember(MemberRequestDto requestDto) {

    // 회원가입에 관련된 내용이 중복되는지 확인하는 메소드
    if (null != isPresentMember(requestDto.getNickname())) { // if(이미 nickname이 존재하면 true, 없다면 false)
      return ResponseDto.fail("DUPLICATED_NICKNAME", "중복된 닉네임 입니다."); // 이미 존재한다면 fail하고 끝 존재하지 않는다면 다음으로
    }

    if (!requestDto.getPassword().equals(requestDto.getPasswordConfirm())) { // 비밀번호와 비밀번호 체크하는 것이 다르다면
      return ResponseDto.fail("PASSWORDS_NOT_MATCHED", "비밀번호와 비밀번호 확인이 일치하지 않습니다."); // fail
    }

    // nickname이 존재하지 않았고 password 체크를 했다면 이쪽 진행
    // Member 엔티티를 만들어준다.
    Member member = Member.builder()
            .nickname(requestDto.getNickname())
                .password(passwordEncoder.encode(requestDto.getPassword())) // password는 암호화해서 Member에 넣어준다.
                    .build();

    memberRepository.save(member); // member 저장

    // ResponseDto에 success 중 data 부분 만드는 과정 (success나 error는 이미 success 메소드(?)를 부른 순간 되어있음)
    return ResponseDto.success(
        MemberResponseDto.builder()
            .id(member.getId())
            .nickname(member.getNickname())
            .createdAt(member.getCreatedAt())
            .modifiedAt(member.getModifiedAt())
            .build()
    );
  }

  @Transactional
  public ResponseDto<?> login(LoginRequestDto requestDto, HttpServletResponse response) { //
    Member member = isPresentMember(requestDto.getNickname());
    if (null == member) { // 만약 멤버가 없다면
      return ResponseDto.fail("MEMBER_NOT_FOUND",
          "사용자를 찾을 수 없습니다.");
    }

    if (!member.validatePassword(passwordEncoder, requestDto.getPassword())) { // 암호화된 패스워드와 입력받은 패스워드가 다르다면
      return ResponseDto.fail("INVALID_MEMBER", "사용자를 찾을 수 없습니다.");
    }

    TokenDto tokenDto = tokenProvider.generateTokenDto(member); // 토콘을 만들어준다.
    tokenToHeaders(tokenDto, response);

    return ResponseDto.success(
        MemberResponseDto.builder()
            .id(member.getId())
            .nickname(member.getNickname())
            .createdAt(member.getCreatedAt())
            .modifiedAt(member.getModifiedAt())
            .build()
    );
  }


  public ResponseDto<?> logout(HttpServletRequest request) {
    if (!tokenProvider.validateToken(request.getHeader("Refresh-Token"))) {
      return ResponseDto.fail("INVALID_TOKEN", "Token이 유효하지 않습니다.");
    }
    Member member = tokenProvider.getMemberFromAuthentication();
    if (null == member) {
      return ResponseDto.fail("MEMBER_NOT_FOUND",
          "사용자를 찾을 수 없습니다.");
    }

    return tokenProvider.deleteRefreshToken(member);
  }

  // 이미 가입되어있는지 확인하는 메소드, 가입이 되어있다면 nickname 반환 없다면 null 반환
  @Transactional(readOnly = true)
  public Member isPresentMember(String nickname) {
    Optional<Member> optionalMember = memberRepository.findByNickname(nickname);
    return optionalMember.orElse(null);
  }

  public void tokenToHeaders(TokenDto tokenDto, HttpServletResponse response) {
    response.addHeader("Authorization", "Bearer " + tokenDto.getAccessToken());
    response.addHeader("Refresh-Token", tokenDto.getRefreshToken());
    response.addHeader("Access-Token-Expire-Time", tokenDto.getAccessTokenExpiresIn().toString());
  }

}
