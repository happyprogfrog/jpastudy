package jpabook.jpastudy.api;

import jakarta.validation.Valid;
import jpabook.jpastudy.domain.Member;
import jpabook.jpastudy.service.MemberService;
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

    /**
     * 등록 V1:
     * - 요청 값으로 Member 엔티티를 직접 받는다.
     * 문제점:
     * - 엔티티에 프레젠테이션 계층을 위한 로직이 추가된다.
     * - 엔티티에 API 검증을 위한 로직이 들어간다(@NotEmpty 등).
     * - 실무에서는 회원 엔티티를 위한 다양한 API 가 만들어지는데, 한 엔티티에 각각의 API 를 위한 모든 요청 요구사항을 담기는 어렵다.
     * - 엔티티가 변경되면 API 스펙이 변한다.
     * 결론:
     * - API 요청 스펙에 맞춰 별도의 DTO 를 파라미터로 받는다.
     */
    @PostMapping("/api/v1/member")
    public CreateMemberResponse saveMemberV1(@RequestBody @Valid Member member) {
        Long id = memberService.join(member);
        return new CreateMemberResponse(id);
    }

    /**
     * 등록 V2:
     * - 요청 값으로 Member 엔티티 대신에 별도의 DTO 를 받는다.
     */
    @PostMapping("/api/v2/member")
    public CreateMemberResponse saveMemberV2(@RequestBody @Valid CreateMemberRequest request) {
        Member member = new Member();
        member.setName(request.getName());
        Long id = memberService.join(member);
        return new CreateMemberResponse(id);
    }

    @PostMapping("/api/v2/members/{memberId}")
    public UpdateMemberResponse updateMemberV2(
            @PathVariable Long memberId,
            @RequestBody @Valid UpdateMemberRequest request) {
        memberService.update(memberId, request.getName());
        Member member = memberService.findOne(memberId);
        return new UpdateMemberResponse(member.getId(), member.getName());
    }

    /**
     * 조회 V1:
     * - 응답 값으로 엔티티를 직접 외부에 노출한다.
     * 문제점:
     * - 엔티티에 프레젠테이션 계층을 위한 로직이 추가된다.
     * - 기본적으로 엔티티의 모든 값이 노출된다.
     * - 응답 스펙을 맞추기 위해 로직이 추가된다(@JsonIgnore, 별도의 뷰 로직 등).
     * - 실무에서는 같은 엔티티에 대해 API 가 용도에 따라 다양하게 만들어지는데, 한 엔티티에 각각의 API 를 위한 프레젠테이션 응답 로직을 담기는 어렵다.
     * - 엔티티가 변경되면 API 스펙이 변한다.
     * - 컬렉션을 직접 만환하면 향후 API 스펙을 변경하기 어렵다.
     * 결론:
     * - API 응답 스펙에 맞춰 별도의 DTO 를 반환한다.
     */
    @GetMapping("/api/v1/members")
    public List<Member> getMembersV1() {
        return memberService.findMembers();
    }

    /**
     * 조회 V2:
     * - 응답 값으로 엔티티가 아닌 별도의 DTO 를 반환한다.
     */
    @GetMapping("/api/v2/members")
    public Result getMemberV2() {
        List<Member> findMembers = memberService.findMembers();
        List<MemberDto> collect = findMembers.stream()
                .map(m -> new MemberDto(m.getName()))
                .collect(Collectors.toList());

        return new Result(collect.size(), collect);
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
    static class CreateMemberRequest {
        private String name;
    }

    @Data
    static class CreateMemberResponse {
        private Long id;

        public CreateMemberResponse(Long id) {
            this.id = id;
        }
    }
}
