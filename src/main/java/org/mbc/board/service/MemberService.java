package org.mbc.board.service;

import org.mbc.board.dto.MemberJoinDTO;

public interface MemberService {
    // 회원가입시 해당아이디가 존재하는 경우 처리

    // jpa 기능 .save()
    // 이미 해당하는 mid가 있는 경우에는 insert가 되어야 하는데 update처리
    static class MidExistException extends Exception {
        // 시큐리티 기능
        // 만일 같은 아이디가 존재하면 예외를 발생
    }

    void join(MemberJoinDTO memberJoinDTO) throws MidExistException;
}
