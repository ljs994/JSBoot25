package org.mbc.board.repository;

import lombok.extern.log4j.Log4j2;
import org.junit.jupiter.api.Test;
import org.mbc.board.domain.Member;
import org.mbc.board.domain.MemberRole;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;
import java.util.stream.IntStream;

@SpringBootTest
@Log4j2
public class MemberRepositoryTests {

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private PasswordEncoder passwordEncoder; // 패스워드를 암호화 처리


    @Test
    public void insertMembers() {

        IntStream.rangeClosed(1,100).forEach(i -> {
            Member member = Member.builder()
                    .mid("member"+i)
                    .mpw(passwordEncoder.encode("1111"))
                    .email("email"+i+"@mbc.org")
                    .build();

            // 해당회원의 role이 적용
            member.addRole(MemberRole.USER); // 100명의 회원은 기본적으로 USER 롤을 갖는다.

            if(i>= 90) {
                member.addRole(MemberRole.ADMIN); // 회원에 90번이상은 admin도 갖는다.
            }
            memberRepository.save(member); // 1명씩 데이터베이스에 저장됨
        });  // 100명의 회원가입된다.


    } // 회원가입 테스트 종료


    @Test
    public void testRead(){

        Optional<Member> result = memberRepository.getWithRoles("member100");

        Member member = result.orElseThrow(); // 예외가 발생하지 않으면 객체에 넣어라

        log.info("------member100 회원의 롤을 출력해라------------------");
        log.info(member);
        log.info(member.getRoleSet());

        member.getRoleSet().forEach(memberRole -> {
            log.info(memberRole.name()); // enum에 이름을 출력
        });

    }


}
