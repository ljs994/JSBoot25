package org.mbc.board.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.mbc.board.domain.Member;
import org.mbc.board.domain.MemberRole;
import org.mbc.board.dto.MemberJoinDTO;
import org.mbc.board.repository.MemberRepository;
import org.modelmapper.ModelMapper;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Log4j2
@Service
@RequiredArgsConstructor // final붙은 필드를 생성자로
public class MemberServiceImpl implements MemberService {
    //                          인터페이스로 만든 추상메서드를 구현 클래스로 제공

    private final ModelMapper modelMapper;  // 엔티티를 dto변환
    private final MemberRepository memberRepository;  // member db 처리용
    private final PasswordEncoder passwordEncoder;    // 패스워드 암호화


    @Override
    public void join(MemberJoinDTO memberJoinDTO) throws MidExistException {
        // 기존에 id가 있는지 확인
        String mid = memberJoinDTO.getMid(); // 프론트에서 id가 넘어옴
        boolean exist = memberRepository.existsById(mid); // 기존에 id 있는지 찾고 t/f


        if(exist) {
            throw new MidExistException(); // 중복id 처리용 예외처리 발생
        }
        // 진짜 회원가입처리
        Member member = modelMapper.map(memberJoinDTO, Member.class);
        // 엔티티                              dto

        member.changePassword(passwordEncoder.encode(memberJoinDTO.getMpw()));
        member.addRole(MemberRole.USER);  // 일반회원으로

        log.info("=============================");
        log.info(member);
        log.info(member.getRoleSet());

        memberRepository.save(member);

    }
}
