package com.example.intermediate.service;

import com.example.intermediate.domain.Member;
import com.example.intermediate.domain.UserDetailsImpl;
import com.example.intermediate.repository.MemberRepository;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {
  private final MemberRepository memberRepository;

  @Override
  public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
//      Member member = memberRepository.findByNickname(username).
//              orElseThrow(() -> new UsernameNotFoundException("Can't find " + username));
//
//      return new UserDetailsImpl(member);

      Optional<Member> member = memberRepository.findByNickname(username);
      if(member.isPresent()) {
          return new UserDetailsImpl(member.get());
      }
      else{
          throw new UsernameNotFoundException("Can't find " + username);
      }
  }
}
