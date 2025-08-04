package org.mbc.board.security;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.mbc.board.domain.Member;
import org.mbc.board.domain.MemberRole;
import org.mbc.board.repository.MemberRepository;
import org.mbc.board.security.dto.MemberSecurityDTO;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Log4j2
@Service
@RequiredArgsConstructor
public class CustomOAuth2UserSerivce extends DefaultOAuth2UserService {
    // oauth2로 로그인한 객체를 User 객체로 변환하는 클래스
    //                              extends DefaultOAuth2UserService 상속받아 처리

    // p 755추가 (카톡 로그인시 db에 같은 이메일이 있는지 처리)
    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;


    @Override // DefaultOAuth2UserService 기본적인 oauth2 클래스를 재정의
    public OAuth2User loadUser(OAuth2UserRequest userRequest)
            throws OAuth2AuthenticationException {

        log.info("CustomOAuth2UserSerivce.loadUser메서드 실행 : " + userRequest);

        // loadUser()에서 카카오 서비스와 연동된 결과를 OAuth2UserRequest로 처리함
        // 원하는 이메일 정보를 추출

        ClientRegistration clientRegistration = userRequest.getClientRegistration();
        String clientName = clientRegistration.getClientName();

        log.info("NAME : " + clientName); // 클라이언트 이름을 가져와 출력 NAME : kakao

        OAuth2User oAuth2User = super.loadUser(userRequest);
        Map<String, Object> paramMap = oAuth2User.getAttributes(); // 카카오에서 주는 객체들(닉네임,이메일)

//        p751 제외
//        paramMap.forEach((k,v) -> {
//            //       String, Object
//            log.info("-------------------------------------");
//            log.info(k + ":" + v);  // 객체출력 테스트
//
//        });

        // paramMap에 있는 이메일 정보를 수집
        String email = null;

        switch(clientName){
            case "kakao":
                email = getKakaoEmail(paramMap);  // 하단에 메서드 추가
                break;

            case "Naver":
                // 네이버 인증
                break;
            case "Google":
                //구글인증
                break;
        }
        log.info("============================");
        log.info("EMAIL : " + email);
        log.info("============================");


        return generateDTO(email, paramMap);  // 하단에 메서드 추가 p756
        //return super.loadUser(userRequest); // super는 부모객체
    }

    private MemberSecurityDTO generateDTO(String email, Map<String, Object> params) {
        // db에 있는 이메일과 카카오에서 받은 이메일을 분석

        Optional<Member> result = memberRepository.findByEmail(email); // db에 email을 찾아옴

        // 데이터베이스에 해당 이메일이 없으면
        if(result.isEmpty()){
            // 회원 추가
            Member member = Member.builder()
                    .mid(email)
                    .mpw(passwordEncoder.encode("1111"))
                    .email(email)
                    .social(true) // 카카오 로그인이 시도되면 회원가입시 체크
                    .build();
            member.addRole(MemberRole.USER); // 처음로그인하면 일반사용자 롤
            memberRepository.save(member); // db에 저장됨!

            // db저장 성공 후에 프론트로 dto를 보냄
            MemberSecurityDTO memberSecurityDTO = new MemberSecurityDTO(
                    email, "1111", email, false, true, Arrays.asList(new SimpleGrantedAuthority("ROLE_USER")));

            memberSecurityDTO.setProps(params);

            return memberSecurityDTO;

        }else {
            // 데이터베이스에 해당 이메일이 있으면!!!!
            // 또 회원가입할 필요가 없다!!!!
            Member member = result.get(); // member엔티티 값을 가져와
            MemberSecurityDTO memberSecurityDTO = new MemberSecurityDTO(
                    member.getMid(),
                    member.getMpw(),
                    member.getEmail(),
                    member.isDel(),
                    member.isSocial(),
                    member.getRoleSet().stream().map(memberRole ->
                                    new SimpleGrantedAuthority("ROLE_"+memberRole.name()))
                            .collect(Collectors.toList())
            );


            return memberSecurityDTO;
        }
    }


    // 카카오 이메일 가져오기 용 메서드
    private String getKakaoEmail(Map<String, Object> paramMap) {
        log.info("CustomOAuth2UserSerivce.getKakaoEmail메서 실행....");
        log.info("카카오 로그인 됨..... 이메일주소 수집용.....");

        Object value = paramMap.get("kakao_account"); // 키값을 가져와 value 처리

        log.info(value);
        //kakao_account:{profile_nickname_needs_agreement=false,
        // profile={nickname=김기원, is_default_nickname=false}, has_email=true,
        // email_needs_agreement=false, is_email_valid=true,
        // is_email_verified=true, email=lonen@nate.com}

        LinkedHashMap accountMap = (LinkedHashMap) value; // map 종류중 하나

        String email = (String) accountMap.get("email");  // email=lonen@nate.com

        log.info("찾아온 메일주소: " + email);

     return email;
    }

}
