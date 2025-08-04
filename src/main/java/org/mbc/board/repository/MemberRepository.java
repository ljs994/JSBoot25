package org.mbc.board.repository;


import org.mbc.board.domain.Member;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

public interface MemberRepository extends JpaRepository<Member, String> {
    //                                    jpa사용        엔티티  pk 타입

    @EntityGraph(attributePaths = "roleSet") // 연관된 롤을 가져옴
    @Query("select m from Member m where m.mid = :mid and m.social=false")
    // member 테이블에 있는 정보에 mid를 가져오는데 소셜로그인은 false
    Optional<Member> getWithRoles(String mid); // id가 들어가면 해당 롤이 나옴


    // email을 이용해서 회원 정보를 찾을 수 있도록
    @EntityGraph(attributePaths = "roleSet") // 연관된 롤을 가져옴 (USER,ADMIN)
    Optional<Member> findByEmail(String email);

    @Modifying
    @Transactional
    @Query("update Member m set m.mpw =:mpw where m.mid = :mid")
    void updatePassword(@Param("mpw") String password, @Param("mid") String mid);

    Optional<Member> findByMid(String mid);
}
