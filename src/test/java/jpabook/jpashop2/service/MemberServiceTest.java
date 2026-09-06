package jpabook.jpashop2.service;

import jakarta.persistence.EntityManager;
import jpabook.jpashop2.domain.Member;
import jpabook.jpashop2.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class MemberServiceTest {

  @Autowired MemberService memberService;
  @Autowired EntityManager em;

  @BeforeEach
  void setUp() {
  }

  @Test
//  @Rollback(false)
  void join() {
    Member member = new Member();
    member.setName("kim");

    Member joinedMember = memberService.join(member);

    assertThat(member.equals(joinedMember)).isTrue();
  }

  @Test
  void failDuplicateMember() {
    Member member1 = new Member();
    member1.setName("kim");
    memberService.join(member1);

    Member member2 = new Member();
    member2.setName("kim");

    assertThatThrownBy(() -> memberService.join(member2))
        .isInstanceOf(IllegalStateException.class);
  }

  @Test
  void findMembers() {
    Member member1 = new Member();
    member1.setName("kim");
    memberService.join(member1);

    em.flush();
    em.clear();

    List<Member> members = memberService.findMembers();
    assertThat(members.size()).isEqualTo(1);
  }

  @Test
  void findOne() {
    Member member1 = new Member();
    member1.setName("kim");
    memberService.join(member1);

    em.flush();
    em.clear();

    Member one = memberService.findOne(member1.getId());
    assertThat(one).isNotNull();
  }
}