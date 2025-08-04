package org.mbc.board.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import lombok.*;

import java.io.Serializable;

@Entity
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@ToString(exclude = "board") // board테이블 제외
public class BoardImage implements Comparable<BoardImage>{
    //                             @OneToMany처리에 순번에 맞게 정렬하기 위함
    // changeBoard()를 이용해서 Board객체를 나중에 지정할 수 있게
    // Board 엔티티 삭제시 BoardImage 객체의 참조도 변경

    @Id // pk
    private String uuid ;
    private String fileName ;
    private int ord ;

    @ManyToOne  // fk로 선언 됨!!! p612
    private Board board ; // 연습용으로 @ManyToOne

    @Override // 재정의
    public int compareTo(BoardImage other) {
        return this.ord - other.ord;  // 실행 순번용
    }

    public void changeBoard(Board board) {
        this.board = board;  // board 엔티티 변경시 같이 변경용
    }
}
