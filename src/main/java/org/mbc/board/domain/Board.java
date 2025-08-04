package org.mbc.board.domain;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.BatchSize;

import java.util.HashSet;
import java.util.Set;

@Entity // 데이터베이스 테이블 관련 객체
@Getter
@Builder // 빌더 패턴 세터 대신 활용
@AllArgsConstructor // 모든 필드값으로 생성자 만듬
@NoArgsConstructor // 기본생성자 
@ToString(exclude = "imageSet")  // 객체 주소가 아닌 값을 출력 p613 추가 (exclude = "imageSet")
public class Board extends BaseEntity{ //  extends BaseEntity (날짜 관련된 jpa 연결)
    
    @Id // pk로 선언용 ( notnull, unique, indexing )
    @GeneratedValue(strategy = GenerationType.IDENTITY) // 자동번호 생성용
    private Long bno ;  // 게시물 번호

    @Column(length = 500, nullable = false) // nn
    private String title;  //제목

    @Column(length = 2000, nullable = false)
    private String content; //내용 

    @Column(length = 50, nullable = false)
    private String writer; // 작성자

    //Hibernate:
    //    create table board (
    //        bno bigint not null auto_increment,
    //        content varchar(255),
    //        title varchar(255),
    //        writer varchar(255),
    //        primary key (bno)
    //    ) engine=InnoDB

    // baseEntity 상속 후 변경 코드
    //Hibernate:
    //    alter table if exists board
    //       add column moddate datetime(6)
    //Hibernate:
    //    alter table if exists board
    //       add column regdate datetime(6)
    //Hibernate:
    //    alter table if exists board
    //       modify column content varchar(2000) not null
    //Hibernate:
    //    alter table if exists board
    //       modify column title varchar(500) not null
    //Hibernate:
    //    alter table if exists board
    //       modify column writer varchar(50) not null

    public void change(String title, String content){
        // 제목과 내용만 수정하는 메서드 (세터 대체용)
        this.title = title;
        this.content = content;
    }

    // p613 추가 이미지 처리용
    @Builder.Default
    // @OneToMany  // 데이터베이스에 관계를 board가 주인이다..
    // board테이블에 bno와 boardImage 테이블에 pk가 불일치 하기때문에
    // board와 boardImage 2개의 테이블을 연결하는 또다른 테이블이 생성됨
    @OneToMany(mappedBy = "board", // BoardImage엔티티의 board 변수
               cascade = {CascadeType.ALL},fetch = FetchType.LAZY, orphanRemoval = true) // p618 추가
              // 영속성을 all 모든 관여        지연로딩 ( 연관된 테이블을 필요시만 참조)
              //                                                   orphanRemoval = true 실제로 삭제용
              // https://choiblack.tistory.com/48                부모가 사라진 고아 자식 객체를 삭제한다.!!!
    @BatchSize(size = 20) // N+1 문제 해결용 코드
    private Set<BoardImage> imageSet = new HashSet<BoardImage>();
    // Set은 구슬(로또) 주머니 같은 객체 같은 객체는 1개만 보관
    // 인덱스가 없다. 들어가는 순서와 나오는 순서가 다름

    //Hibernate:
    //    create table board_image (
    //        uuid varchar(255) not null,
    //        file_name varchar(255),
    //        ord integer not null,
    //        board_bno bigint,  // @OneToMany(mappedBy = "board")
    //        primary key (uuid)
    //    ) engine=InnoDB
    
    //Hibernate: 
    //    alter table if exists board_image 
    //       add constraint FKo4dbcmbib7vwlk8eplv2cwbe2 
    //       foreign key (board_bno)   // board테이블과 fk 선언됨
    //       references board (bno)


    // 이미지 추가용 메서드
    public void addImage(String uuid, String fileName){

        BoardImage boardImage = BoardImage.builder()
                .uuid(uuid)     // 파일명 랜덤 처리
                .fileName(fileName) // 진짜 파일명
                .board(this)        // 연결된 게시물 정보
                .ord(imageSet.size()) // 순서배정
                .build();
        imageSet.add(boardImage); // 구슬 주머니에 이미지를 담는다.
    }

    public void clearImages(){

        imageSet.forEach(boardImage -> boardImage.changeBoard(null));

        this.imageSet.clear();
    }



}
