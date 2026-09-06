package jpabook.jpashop2.service;

import jpabook.jpashop2.domain.Member;
import jpabook.jpashop2.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class MemberService {
  private final MemberRepository memberRepository;

  /**
   * 회원 가입
   */
  @Transactional
  public Member join(Member member) {
    validateDuplicateMember(member);
    return memberRepository.save(member);
  }

  private void validateDuplicateMember(Member member) {
    List<Member> findMembers = memberRepository.findByName(member.getName());
    if (!findMembers.isEmpty()) {
      throw new IllegalStateException("이미 존재하는 회원입니다.");
    }
  }

  public List<Member> findMembers() {
    return memberRepository.findAll();
  }

  public Member findOne(Long memberId) {
    return memberRepository.findOne(memberId);
  }

  @Transactional
  public Member update(Long id, String name) {
    Member member = memberRepository.findOne(id);
    member.setName(name);
    return member;
  }
}
