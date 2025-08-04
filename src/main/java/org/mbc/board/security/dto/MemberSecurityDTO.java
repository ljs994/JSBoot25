package org.mbc.board.security.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.util.Collection;
import java.util.Map;

@Getter
@Setter
@ToString
public class MemberSecurityDTO extends User implements OAuth2User { //extends User 시큐리티 DUserDetails 용
                                        // p754 implements OAuth2User 카카오로그인 가능하게
    // 필드
    private String mid;
    private String mpw;
    private String email;
    private boolean del;
    private boolean social;

    // 소셜 로그인 정보 p754 추가
    private Map<String, Object> props; //
    //kakao_account:{profile_nickname_needs_agreement=false,
    // profile={nickname=김기원, is_default_nickname=false}, has_email=true,
    // email_needs_agreement=false, is_email_valid=true,
    // is_email_verified=true, email=lonen@nate.com}


    // 생성자
    public MemberSecurityDTO(String username, String password, String email,
                             boolean del, boolean social,
                             Collection<? extends GrantedAuthority> authorities) {
        //                   프론트에서 넘어오는 값은 다를수 있다.

        super(username, password, authorities);  // User 객체

        this.mid = username;
        this.mpw = password;
        this.email = email;
        this.del = del;
        this.social = social;
    }

    @Override  // p754 추가 (카카오 로그인 정보 가져오기)
    public Map<String, Object> getAttributes() {
        return this.getProps();
    }

    @Override // p754 추가 (db에 id 가져오기)
    public String getName() {
        return this.mid;
    }
}
