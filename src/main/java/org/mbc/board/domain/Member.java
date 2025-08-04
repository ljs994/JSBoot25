package org.mbc.board.domain;

import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import lombok.*;

import java.util.HashSet;
import java.util.Set;

@Entity
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@ToString(exclude="roleSet") // 연관된 테이블 제외
public class Member extends BaseEntity {
    // 회원가입용 엔티티

    @Id //기본키 선언
    private String mid; // pk용 (로그인id)
    private String mpw; // 암호
    private String email; // 이메일
    private boolean del ; // 회원탈퇴

    private boolean social ; // 카톡,구글 로그인 기법 유무

    // 회원롤 관리(user,admin)
    @ElementCollection(fetch = FetchType.LAZY)
    @Builder.Default
    private Set<MemberRole> roleSet = new HashSet<MemberRole>();
    // Set 로또 구슬 주머니

    // 회원 수정용 메서드 추가 (Setter가 없다)
    public void changePassword(String mpw) {
        this.mpw = mpw;
    }

    // 회원 이메일 변경 메서드
    public void changeEmail(String email) {
        this.email = email;
    }

    // 회원 탈퇴용 메서드 
    public void changeDel(boolean del) {
        this.del = del;
    }
    
    // 권한변경용 메서드 (기본은 user권한인데 admin 권한추가가 가능)
    public void addRole(MemberRole memberRole) {
        this.roleSet.add(memberRole);
    }
    
    // 권한초기화용
    public void clearRoles() {
        this.roleSet.clear(); // Set<>에 객체 삭제
    }

    public void changeSocial(boolean social) {
        this.social = social;
    }
}
