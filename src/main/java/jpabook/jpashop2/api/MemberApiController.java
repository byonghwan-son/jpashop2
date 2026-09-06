package jpabook.jpashop2.api;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jpabook.jpashop2.domain.Address;
import jpabook.jpashop2.domain.Member;
import jpabook.jpashop2.service.MemberService;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequiredArgsConstructor
public class MemberApiController {
  private final MemberService memberService;

  @PostMapping("/api/v1/members")
  public CreateMemberResponse saveMemberV1(@RequestBody @Valid Member member) {
    Member joinedMember = memberService.join(member);
    return new CreateMemberResponse(joinedMember.getId());
  }

  /**
   * 도메인 모델의 Entity와 DTO로 분리
   * @param request 별도 수정 객체
   * @return 별도 정의 Response 객체
   */
  @PostMapping("/api/v2/members")
  public CreateMemberResponse saveMemberV2(@RequestBody @Valid CreateMemberRequest request) {
    Member member = new Member();
    member.setName(request.getName());
    Member joinedMember = memberService.join(member);
    return new CreateMemberResponse(joinedMember.getId());
  }

  @PutMapping("/api/v2/members/{id}")
  public UpdateMemberResponse updateMemberV2(@PathVariable Long id,
                                             @RequestBody @Valid UpdateMemberRequest request) {
    Member updatedMember = memberService.update(id, request.getName());
    return new UpdateMemberResponse(updatedMember.getId(), updatedMember.getName());
  }

  /**
   * 조회 V1: 응답 값으로 엔티티를 직접 외부에 노출한다.
   * 문제점
   * - 엔티티에 프레젠테이션 계층을 위한 로직이 추가된다.
   * - 기본적으로 엔티티의 모든 값이 노출된다.
   * - 응답 스펙을 맞추기 위해 로직이 추가된다. (@JsonIgnore, 별도의 뷰 로직 등등)
   * - 실무에서는 같은 엔티티에 대해 API가 용도에 따라 다양하게 만들어지는데, 한 엔티티에 각각의 API를 위한 프레젠테이션 응답 로직을 담기는 어렵다.
   * - 엔티티가 변경되면 API 스펙이 변한다.
   * - 추가로 컬렉션을 직접 반환하면 항후 API 스펙을 변경하기 어렵다.(별도의 Result 클래스 생성으로 해결)
   * 결론
   * - API 응답 스펙에 맞추어 별도의 DTO를 반환한다.
   */
  // 조회 V1: 안 좋은 버전, 모든 엔티티가 노출,
  // @JsonIgnore -> 이건 정말 최악, api가 이거 하나인가! 화면에 종속적이지 마라!
  @GetMapping("/api/v1/members")
  public List<Member> membersV1() {
    return memberService.findMembers();
  }

  /**
   * 엔티티를 DTO로 변환해서 반환한다.
   * 엔티티가 변해도 API 스펙이 변경되지 않는다.
   * 추가로 `Result` 클래스로 컬렉션을 감싸서 향후 필요한 필드를 추가할 수 있다.
   *
   * 조회 V2: 응답 값으로 엔티티가 아닌 별도의 DTO를 반환한다.
   */
  @GetMapping("/api/v2/members")
  public Result memverV2() {
    List<Member> findMembers = memberService.findMembers();
    List<MemberDto> collect = findMembers.stream()
        .map(m -> new MemberDto(m.getName(), m.getAddress()))
        .collect(Collectors.toList());
    return new Result(collect.size(), collect);
  }

  @Data
  @AllArgsConstructor
  static class CreateMemberResponse {
    private Long id;
  }

  @Data
  static class CreateMemberRequest {
    @NotEmpty
    private String name;
  }

  @Data
  static class UpdateMemberRequest {
    private String name;
  }

  @Data
  @AllArgsConstructor
  static class UpdateMemberResponse {
    private Long id;
    private String name;
  }

  @Data
  @AllArgsConstructor
  static class Result<T> {
    private int count;
    private T data;
  }

  @Data
  @AllArgsConstructor
  static class MemberDto {
    private String name;
    private Address address;
  }
}
