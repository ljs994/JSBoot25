package org.mbc.board.repository;

import jakarta.transaction.Transactional;
import lombok.extern.log4j.Log4j2;
import org.junit.jupiter.api.Test;
import org.mbc.board.domain.Board;
import org.mbc.board.domain.BoardImage;
import org.mbc.board.dto.BoardListReplyCountDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.test.annotation.Commit;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.IntStream;

@SpringBootTest // 메서드용 테스트 동작
@Log4j2 // 로그용
public class BoardRepositoryTests {
    // 영속성 계층에 테스트용

    @Autowired // 생성자 자동 주입
    private BoardRepository boardRepository;

    @Autowired // p626 추가
    private ReplyRepository replyRepository;

    @Test
    public void testInsert(){
        // 데이터베이스에 데이터 주입(c) 테스트 코드
        IntStream.rangeClosed(1,100).forEach(i -> {
            // i 변수에 1~100까지 100개의 정수를 반복해서 생성
            Board board = Board.builder()
                    .title("제목..."+i)  // board.setTitle()
                    .content("내용..."+i) // board.setContent()
                    .writer("user"+(i%10))  // board.setWriter()
                    .build(); // @Builder 용 (세터 대신 좀더 간단하고 가독성 좋게 )
                // log.info((board));
            Board result = boardRepository.save(board) ; // 데이터베이스에 기록하는 코드
            //                            .save 메서드는 jpa에서 상속한 메서드로 값을 저장하는 용도
            //                                          이미 값이 있으면 update를 진행한다.
            log.info("게시물 번호 출력 : " + result.getBno() + "게시물의 제목 : " + result.getTitle());

                }// forEach문 종료
        );// IntStream. 종료

    } // testInsert 메서드 종료

    @Test
    public void testSelect(){
        Long bno = 100L; // 게시물 번호가 100인 개체를 확인 해보자.

        Optional<Board> result = boardRepository.findById(bno);
        // Optional 널값이 나올 경우를 대비한 객체
        //                                    .findById(bno) ->  select * from board where bno = bno;
        // Hibernate:
        //    select
        //        b1_0.bno,
        //        b1_0.content,
        //        b1_0.moddate,
        //        b1_0.regdate,
        //        b1_0.title,
        //        b1_0.writer
        //    from
        //        board b1_0
        //    where
        //        b1_0.bno=?

        Board board = result.orElseThrow(); // 값이 있으면 넣어라

        log.info(bno + "가 데이터 베이스에 존재합니다. ");
        log.info(board) ; // Board(bno=100, title=제목...100, content=내용...100, writer=user0)
    }  // testSelect 메서드 종료

    @Test
    public void testUpdate(){

        Long bno = 100L; // 100번 게시물을 가져와서 수정후 테스트 종료

        Optional<Board> result = boardRepository.findById(bno); // bno 를 찾아서 result에 넣는다.

        Board board = result.orElseThrow(); // 가져온 값이 있으면 board 타입에 객체에 넣는다.

        board.change("수정테스트 제목", "수정테스트 내용"); // 제목과 내용만 수정할 수 있는 메서드

        boardRepository.save(board); // .save 메서드는 pk값이 없으면 insert, pk 있으면 update 함.

        //Hibernate:
        //    select
        //        b1_0.bno,
        //        b1_0.content,
        //        b1_0.moddate,
        //        b1_0.regdate,
        //        b1_0.title,
        //        b1_0.writer
        //    from
        //        board b1_0
        //    where
        //        b1_0.bno=?
        //Hibernate:
        //    select
        //        b1_0.bno,
        //        b1_0.content,
        //        b1_0.moddate,
        //        b1_0.regdate,
        //        b1_0.title,
        //        b1_0.writer
        //    from
        //        board b1_0
        //    where
        //        b1_0.bno=?
        //Hibernate:
        //    update
        //        board
        //    set
        //        content=?,
        //        moddate=?,
        //        title=?,
        //        writer=?
        //    where
        //        bno=?
    }

    @Test
    public void testDelete(){

        Long bno = 1L;

        boardRepository.deleteById(bno);
        //             .deleteById(bno) -> delecte from board where bno = bno

        // Hibernate:
        //    select
        //        b1_0.bno,
        //        b1_0.content,
        //        b1_0.moddate,
        //        b1_0.regdate,
        //        b1_0.title,
        //        b1_0.writer
        //    from
        //        board b1_0
        //    where
        //        b1_0.bno=?
        //Hibernate:
        //    delete
        //    from
        //        board
        //    where
        //        bno=?
    }

    @Test
    public void testPaging(){
        // .findAll() 는 모든 리스트를 출력하는 메서드 select * from board ;
        // 전체 리스트에 페이징과 정렬 기법도 추가 해보자.

        Pageable pageable = PageRequest.of(0,10, Sort.by("bno").descending());
        //                           시작번호,페이지당 데이터 개수
        //                                       번호를 기준으로 내림차순 정렬!!!
        // Hibernate:
        //    select
        //        b1_0.bno,
        //        b1_0.content,
        //        b1_0.moddate,
        //        b1_0.regdate,
        //        b1_0.title,
        //        b1_0.writer
        //    from
        //        board b1_0
        //    order by
        //        b1_0.bno desc  (bno를 기준으로 내림차순 정렬)
        //    limit
        //        ?, ?         (시작번호, 끝번호)

        //Hibernate:
        //    select
        //        count(b1_0.bno)   board 전체 리스트 수를 알아옴.
        //    from
        //        board b1_0
        Page<Board> result = boardRepository.findAll(pageable);
        // 1장에 종이에 Board 객체를 가지고 있는 결과는 result 에 담긴다.
        // Page 클래스는 다음페이지 존재 여부, 이전페이지 존재 여부, 전체 데이터 개수, 등등.... 계산을 한다.

        log.info("전체 게시물 수 : " + result.getTotalElements());  // 99
        log.info("총 페이지 수 : " + result.getTotalPages());       // 10
        log.info("현재 페이지 번호 : " + result.getNumber());       // 0
        log.info("페이지당 데이터 개수 : " + result.getSize() );     // 10
        log.info("다음페이지 여부 : " + result.hasNext());          // true
        log.info("시작페이지 여부 : " + result.isFirst());         // true

        // 콘솔에 결과를 출력해보자.
        List<Board> boardList = result.getContent(); // 페이징처리된 내용을 가져와라

        boardList.forEach(board -> log.info(board));
        //   forEach는 인덱스를 사용하지 않고 앞에서부터 객체를 리턴함
        //                board -> log.info(board)
        //                      람다식 1개의 명령어가 있을 때 활용

    }

    // 쿼리dsl 테스트 진행
    @Test
    public void testSearch1(){

        Pageable pageable = PageRequest.of(1,10, Sort.by("bno").descending());

        Page<Board> result = boardRepository.search1(pageable); //페이징 기법을 사용해서 title = 1 값을 찾아 오나?

        result.getContent().forEach(board -> log.info(board));

        //Hibernate:
        //    select
        //        b1_0.bno,
        //        b1_0.content,
        //        b1_0.moddate,
        //        b1_0.regdate,
        //        b1_0.title,
        //        b1_0.writer
        //    from
        //        board b1_0
        //    where
        //        b1_0.title like ? escape '!'  -> like 1  -> 조건이 1개일 경우

        //Hibernate:
        //    select
        //        b1_0.bno,
        //        b1_0.content,
        //        b1_0.moddate,
        //        b1_0.regdate,
        //        b1_0.title,
        //        b1_0.writer
        //    from
        //        board b1_0
        //    where
        //        (
        //            b1_0.title like ? escape '!'
        //            or b1_0.content like ? escape '!'   -> 조건이 2개 title, content (booleanBuilder)
        //        )
        //        and b1_0.bno>?  -> query.where(board.bno.gt(0L))
        //    order by
        //        b1_0.bno desc
        //    limit
        //        ?, ?   -> this.getQuerydsl().applyPagination(pageable, query);
        //  PageRequest.of(1,10, Sort.by("bno").descending());

    }

    @Test
    public void testSearchAll(){
        // 프론트에서 t가 선택되면 title, c가 선택되면 content, w가 선택되면 writer가 조건으로 제시됨

        String[] types = {"t", "w"};  // 검색 조건

        String keyword = "10";  // 검색 단어

        Pageable pageable = PageRequest.of(0,10, Sort.by("bno").descending());

        Page<Board> result = boardRepository.searchAll(types, keyword, pageable);

        //Hibernate:
        //    select
        //        b1_0.bno,
        //        b1_0.content,
        //        b1_0.moddate,
        //        b1_0.regdate,
        //        b1_0.title,
        //        b1_0.writer
        //    from
        //        board b1_0
        //    where
        //        (
        //            b1_0.title like ? escape '!'
        //            or b1_0.content like ? escape '!'
        //            or b1_0.writer like ? escape '!'      //   if( (types != null && types.length >0 ) && keyword !=null ){
        //        )
        //        and b1_0.bno>?
        //    order by
        //        b1_0.bno desc    // PageRequest.of(0,10, Sort.by("bno").descending());
        //    limit
        //        ?, ?


        log.info("전체 게시물 수 : " + result.getTotalElements());  // 99
        log.info("총 페이지 수 : " + result.getTotalPages());       // 10
        log.info("현재 페이지 번호 : " + result.getNumber());       // 0
        log.info("페이지당 데이터 개수 : " + result.getSize() );     // 10
        log.info("다음페이지 여부 : " + result.hasNext());          // true
        log.info("시작페이지 여부 : " + result.isFirst());         // true

        result.getContent().forEach(board -> log.info(board));

    }

    @Test
    public void testSearchReplyCount(){

        String[] types= {"t","c","w"};  // 제목, 내용, 작성자
        String keyword = "1";           // 제목이나 내용이나 작성자에 1값을 찾는다.

        Pageable pageable = PageRequest.of(0,10, Sort.by("bno").descending());

        Page<BoardListReplyCountDTO> result = boardRepository.searchWithReplyCount(types, keyword, pageable);

        log.info("전체 게시물 수 : " + result.getTotalElements());  // 20
        log.info("총 페이지 수 : " + result.getTotalPages());       // 2
        log.info("현재 페이지 번호 : " + result.getNumber());       // 0
        log.info("페이지당 데이터 개수 : " + result.getSize() );     // 10
        log.info("다음페이지 여부 : " + result.hasNext());          // true
        log.info("시작페이지 여부 : " + result.isFirst());         // true

        result.getContent().forEach(board -> log.info(board));
        // BoardListReplyCountDTO(bno=100, title=제목...100(수정테스트), writer=user0, regDate=2025-07-22T11:11:46.002548, replyCount=2)
    }

    //board 이미지처리 테스트
    @Test
    public void testInsertWithImage(){

        Board board = Board.builder()
                .title("이미지 테스트")
                .content("첨부파일테스트")
                .writer("tester")
                .build();

        // 첨부파일 더미데이터 string 처리
        for (int i=0 ; i < 3 ; i++ ){
            // 첨부파일 3개
            board.addImage(UUID.randomUUID().toString(), "file"+i+".jpg");
            // import java.util.UUID;   UUIDfile0.jpg UUIDfile1.jpg UUIDfile2.jpg

        }
        boardRepository.save(board);

        //Hibernate:
        //    insert
        //    into
        //        board
        //        (content, moddate, regdate, title, writer)
        //    values
        //        (?, ?, ?, ?, ?)

        //Hibernate:
        //    insert
        //    into
        //        board_image
        //        (board_bno, file_name, ord, uuid)
        //    values
        //        (?, ?, ?, ?)

        //Hibernate:
        //    insert
        //    into
        //        board_image
        //        (board_bno, file_name, ord, uuid)
        //    values
        //        (?, ?, ?, ?)

        //Hibernate:
        //    insert
        //    into
        //        board_image
        //        (board_bno, file_name, ord, uuid)
        //    values
        //        (?, ?, ?, ?)
    }

    @Test // 게시물 읽기 + 이미지
    @Transactional // import jakarta.transaction.Transactional;
    public void testReadWithImage(){

        Optional<Board> result = boardRepository.findById(1L);
        // board 테이블에 1번 게시물을 가져와라

        Board board = result.orElseThrow(); // 예외가 없으면 board 객체에 담는다.

        log.info(board); // 게시물 객체
        log.info("-----------------------");
        log.info(board.getImageSet()); // 첨부파일 객체
        // 지연 로딩 테스트
        // Hibernate:
        //    select
        //        b1_0.bno,
        //        b1_0.content,
        //        b1_0.moddate,
        //        b1_0.regdate,
        //        b1_0.title,
        //        b1_0.writer
        //    from
        //        board b1_0
        //    where
        //        b1_0.bno=?
        //2025-07-24T14:26:42.831+09:00  INFO 50080 --- [board] [    Test worker] o.m.b.repository.BoardRepositoryTests    : Board(bno=1, title=이미지 테스트, content=첨부파일테스트, writer=tester)
        //2025-07-24T14:26:42.834+09:00  INFO 50080 --- [board] [    Test worker] o.m.b.repository.BoardRepositoryTests    : -----------------------
        //
        //failed to lazily initialize a collection of role: org.mbc.board.domain.Board.imageSet: could not initialize proxy - no Session  //
        // 지연로딩시에 no Session 연결된 정보가 사라져서 @Transactional을 적용하면 추가적인 쿼리가 여러번 실행됨

        // @Transactional 이후에 정상실행된다. -> 다른방법을 사용해보겠다. @EntityGraph -> BoardRepository
        //Hibernate:
        //    select
        //        is1_0.board_bno,
        //        is1_0.uuid,
        //        is1_0.file_name,
        //        is1_0.ord
        //    from
        //        board_image is1_0
        //    where
        //        is1_0.board_bno=?
        //2025-07-24T14:29:52.481+09:00  INFO 42548 --- [board] [    Test worker] o.m.b.repository.BoardRepositoryTests    : [BoardImage(uuid=4d531914-b44b-466a-9f3d-ea42e2b315a2, fileName=file0.jpg, ord=0), BoardImage(uuid=f86a4399-feba-4b44-9088-f926643e51b3, fileName=file1.jpg, ord=1), BoardImage(uuid=6ea8ab57-3997-4b3d-9014-78748f4d5414, fileName=file2.jpg, ord=2)]
    }

    @Test
    public void testReadWithImagesEntityGraph(){
        Optional<Board> result = boardRepository.findByIdWithImage(1L);
        //                                       repository에 만든 JQPL 활용  @EntityGraph
        Board board = result.orElseThrow();
        log.info(board);
        log.info("-----------------------");
        for (BoardImage boardImage : board.getImageSet()) {
            log.info(boardImage);
        }
        //Hibernate:
        //    select
        //        b1_0.bno,
        //        b1_0.content,
        //        is1_0.board_bno,
        //        is1_0.uuid,
        //        is1_0.file_name,
        //        is1_0.ord,
        //        b1_0.moddate,
        //        b1_0.regdate,
        //        b1_0.title,
        //        b1_0.writer
        //    from
        //        board b1_0
        //    left join
        //        board_image is1_0
        //            on b1_0.bno=is1_0.board_bno
        //    where
        //        b1_0.bno=?

        //  @EntityGraph을 활용하니 지연로딩이지만 select 문이 한번에 이루어짐 !!! 결과는 빠름
    }

    @Transactional
    @Commit // 두 테이블이 결과가 둘다 ok(ture) 처리되면 영구 저장
    @Test
    public void testModifyImages(){

        Optional<Board> result = boardRepository.findByIdWithImage(1L);
        Board board = result.orElseThrow();

        board.clearImages(); // board 테이블에 연결된 Image 테이블을 전체 삭제

        for(int i=0 ; i < 2 ; i++ ){
            // 전에는 3개의 첨부지만 2로 수정 하려 함
            board.addImage(UUID.randomUUID().toString(), "updatefile"+i+".jpg");

        }
        boardRepository.save(board);
        //Hibernate:
        //    select
        //        b1_0.bno,
        //        b1_0.content,
        //        is1_0.board_bno,
        //        is1_0.uuid,
        //        is1_0.file_name,
        //        is1_0.ord,
        //        b1_0.moddate,
        //        b1_0.regdate,
        //        b1_0.title,
        //        b1_0.writer
        //    from
        //        board b1_0
        //    left join
        //        board_image is1_0
        //            on b1_0.bno=is1_0.board_bno
        //    where
        //        b1_0.bno=?    // board테이블과 image 테이블 모든 값을 가져와!!
        //Hibernate:
        //    select
        //        bi1_0.uuid,
        //        b1_0.bno,
        //        b1_0.content,
        //        b1_0.moddate,
        //        b1_0.regdate,
        //        b1_0.title,
        //        b1_0.writer,
        //        bi1_0.file_name,
        //        bi1_0.ord
        //    from
        //        board_image bi1_0
        //    left join
        //        board b1_0
        //            on b1_0.bno=bi1_0.board_bno
        //    where
        //        bi1_0.uuid=?
        //Hibernate:
        //    select
        //        bi1_0.uuid,
        //        b1_0.bno,
        //        b1_0.content,
        //        b1_0.moddate,
        //        b1_0.regdate,
        //        b1_0.title,
        //        b1_0.writer,
        //        bi1_0.file_name,
        //        bi1_0.ord
        //    from
        //        board_image bi1_0
        //    left join
        //        board b1_0
        //            on b1_0.bno=bi1_0.board_bno    // 3번의 같은 쿼리가 실행됨!!! (이미지가 3개라)
        //    where
        //        bi1_0.uuid=?
        //Hibernate:
        //    insert
        //    into
        //        board_image
        //        (board_bno, file_name, ord, uuid)
        //    values
        //        (?, ?, ?, ?)
        //Hibernate:
        //    insert
        //    into
        //        board_image
        //        (board_bno, file_name, ord, uuid)
        //    values
        //        (?, ?, ?, ?)              // 새로운 이미지가 2개  삽입
        //Hibernate:
        //    update
        //        board_image
        //    set
        //        board_bno=?,
        //        file_name=?,
        //        ord=?
        //    where
        //        uuid=?
        //Hibernate:
        //    update
        //        board_image
        //    set
        //        board_bno=?,
        //        file_name=?,
        //        ord=?
        //    where
        //        uuid=?
        //Hibernate:
        //    update
        //        board_image
        //    set
        //        board_bno=?,
        //        file_name=?,
        //        ord=?
        //    where
        //        uuid=?            // 업데이트가 3번 이루어짐 !!! (삭제대신 업데이트가 됨)
        //                             cascade = all로 설정한 영향
        //                             cascase에 orphanRemoval 값을 ture로 넣어야 실제 삭제됨

        //Hibernate:
        //    select
        //        b1_0.bno,
        //        b1_0.content,
        //        is1_0.board_bno,
        //        is1_0.uuid,
        //        is1_0.file_name,
        //        is1_0.ord,
        //        b1_0.moddate,
        //        b1_0.regdate,
        //        b1_0.title,
        //        b1_0.writer
        //    from
        //        board b1_0
        //    left join
        //        board_image is1_0
        //            on b1_0.bno=is1_0.board_bno
        //    where
        //        b1_0.bno=?
        //Hibernate:
        //    select
        //        bi1_0.uuid,
        //        b1_0.bno,
        //        b1_0.content,
        //        b1_0.moddate,
        //        b1_0.regdate,
        //        b1_0.title,
        //        b1_0.writer,
        //        bi1_0.file_name,
        //        bi1_0.ord
        //    from
        //        board_image bi1_0
        //    left join
        //        board b1_0
        //            on b1_0.bno=bi1_0.board_bno
        //    where
        //        bi1_0.uuid=?
        //Hibernate:
        //    select
        //        bi1_0.uuid,
        //        b1_0.bno,
        //        b1_0.content,
        //        b1_0.moddate,
        //        b1_0.regdate,
        //        b1_0.title,
        //        b1_0.writer,
        //        bi1_0.file_name,
        //        bi1_0.ord
        //    from
        //        board_image bi1_0
        //    left join
        //        board b1_0
        //            on b1_0.bno=bi1_0.board_bno
        //    where
        //        bi1_0.uuid=?
        //Hibernate:
        //    insert
        //    into
        //        board_image
        //        (board_bno, file_name, ord, uuid)
        //    values
        //        (?, ?, ?, ?)
        //Hibernate:
        //    insert
        //    into
        //        board_image
        //        (board_bno, file_name, ord, uuid)
        //    values
        //        (?, ?, ?, ?)
        //Hibernate:
        //    delete
        //    from
        //        board_image
        //    where
        //        uuid=?
        //Hibernate:
        //    delete
        //    from
        //        board_image
        //    where
        //        uuid=?
    }

    @Test
    @Transactional
    @Commit
    public void testRemoveAll(){
        // 1번 게시물을 삭제하면 댓글과 첨부파일이 모두 삭제되어야 함!!!

        Long bno = 1L;

        replyRepository.deleteByBoard_Bno(bno);  // 자식부터 삭제
        boardRepository.deleteById(bno);        // 부모가 삭제


        //Hibernate:
        //    select
        //        r1_0.rno,
        //        r1_0.board_bno,
        //        r1_0.moddate,
        //        r1_0.regdate,
        //        r1_0.reply_text,
        //        r1_0.replyer
        //    from
        //        reply r1_0
        //    left join
        //        board b1_0
        //            on b1_0.bno=r1_0.board_bno
        //    where
        //        b1_0.bno=?
        //Hibernate:
        //    select
        //        b1_0.bno,
        //        b1_0.content,
        //        b1_0.moddate,
        //        b1_0.regdate,
        //        b1_0.title,
        //        b1_0.writer
        //    from
        //        board b1_0
        //    where
        //        b1_0.bno=?
        //Hibernate:
        //    select
        //        is1_0.board_bno,
        //        is1_0.uuid,
        //        is1_0.file_name,
        //        is1_0.ord
        //    from
        //        board_image is1_0
        //    where
        //        is1_0.board_bno=?
        //Hibernate:
        //    delete
        //    from
        //        board_image
        //    where
        //        uuid=?
        //Hibernate:
        //    delete
        //    from
        //        board_image
        //    where
        //        uuid=?
        //Hibernate:
        //    delete
        //    from
        //        board
        //    where
        //        bno=?

        // 댓글 있는지 확인 -> 게시물 확인 -> 이미지 확인 -> 댓글이 없으니 skip
        // 이미지 2개 삭제 -> 게시물 삭제 !!!

    }

    @Test
    public void testInsertAll(){
        // 게시글과 첨부파일 더미데이터 추가용

        for(int i=1 ; i <= 100 ; i++ ){

            Board board = Board.builder()
                    .title("테스트 제목" + i)
                    .content("테스트 내용"+i)
                    .writer("writer"+i)
                    .build();

            for (int j=0 ; j < 3 ; j++){

                if(i % 5 == 0){
                    continue;  // 5의 배수 게시물에는 첨부파일이 없다.!!!!
                }
                board.addImage(UUID.randomUUID().toString(), i+"file"+j+".jpg");
            } // 첨부파일 더미데이터 for문 종료
            boardRepository.save(board);
        } // 게시물 더미데이터 for 종료
    } // testInsertAll 메서드 종료

    @Test // N+1 오류 발생 테스트
    @Transactional
    public void testSearchImageReplyCount(){
        // 리스트 페이지에서 댓글 수와 게시물목록 이미지가 처리되는 부분

        Pageable pageable = PageRequest.of(1,10, Sort.by("bno").descending());
        boardRepository.searchWithAll(null, null, pageable);
        //                           타입  키워드
        //Hibernate:
        //    select
        //        b1_0.bno,
        //        b1_0.content,
        //        b1_0.moddate,
        //        b1_0.regdate,
        //        b1_0.title,
        //        b1_0.writer
        //    from
        //        board b1_0
        //    left join
        //        reply r1_0
        //            on r1_0.board_bno=b1_0.bno
        //    order by
        //        b1_0.bno desc
        //    limit
        //        ?, ?
        //90
        //Hibernate:
        //    select
        //        is1_0.board_bno,
        //        is1_0.uuid,
        //        is1_0.file_name,
        //        is1_0.ord
        //    from
        //        board_image is1_0
        //    where
        //        is1_0.board_bno=?
        //[]
        //------------------------
        //89
        //Hibernate:
        //    select
        //        is1_0.board_bno,
        //        is1_0.uuid,
        //        is1_0.file_name,
        //        is1_0.ord
        //    from
        //        board_image is1_0
        //    where
        //        is1_0.board_bno=?
        //[BoardImage(uuid=944d0152-4dbe-4474-a987-cc3636893c0e, fileName=89file1.jpg, ord=1), BoardImage(uuid=0c57ac38-cb05-4b26-84df-78a0a56757ec, fileName=89file2.jpg, ord=2), BoardImage(uuid=4e54f627-40bb-4249-8c04-15122cd512f2, fileName=89file0.jpg, ord=0)]
        //------------------------
        //88
        //Hibernate:
        //    select
        //        is1_0.board_bno,
        //        is1_0.uuid,
        //        is1_0.file_name,
        //        is1_0.ord
        //    from
        //        board_image is1_0
        //    where
        //        is1_0.board_bno=?
        //[BoardImage(uuid=a6102df6-2f82-4b75-b549-a2efe706940e, fileName=88file2.jpg, ord=2), BoardImage(uuid=a8a6b4da-0a15-4f57-b6a2-81753af78c2b, fileName=88file0.jpg, ord=0), BoardImage(uuid=e680b4e5-48ec-430d-ba59-485f96620b0f, fileName=88file1.jpg, ord=1)]
        //------------------------
        //87
        //Hibernate:
        //    select
        //        is1_0.board_bno,
        //        is1_0.uuid,
        //        is1_0.file_name,
        //        is1_0.ord
        //    from
        //        board_image is1_0
        //    where
        //        is1_0.board_bno=?
        //[BoardImage(uuid=74562886-70ef-4ba8-b8f6-cfef1c5d3af2, fileName=87file0.jpg, ord=0), BoardImage(uuid=9c43e839-f274-4240-b2df-80b8becb86e5, fileName=87file2.jpg, ord=2), BoardImage(uuid=a05c0523-a10b-4233-a02a-3a9502294f34, fileName=87file1.jpg, ord=1)]
        //------------------------
        //86
        //Hibernate:
        //    select
        //        is1_0.board_bno,
        //        is1_0.uuid,
        //        is1_0.file_name,
        //        is1_0.ord
        //    from
        //        board_image is1_0
        //    where
        //        is1_0.board_bno=?
        //[BoardImage(uuid=96862e82-e812-41ec-8ad7-b0809834c53f, fileName=86file2.jpg, ord=2), BoardImage(uuid=ac71bd1b-4376-464b-b0f6-76a355ddc7f0, fileName=86file1.jpg, ord=1), BoardImage(uuid=aed0afc9-b2a9-4eeb-b278-462ae7590899, fileName=86file0.jpg, ord=0)]
        //------------------------
        //85
        //Hibernate:
        //    select
        //        is1_0.board_bno,
        //        is1_0.uuid,
        //        is1_0.file_name,
        //        is1_0.ord
        //    from
        //        board_image is1_0
        //    where
        //        is1_0.board_bno=?
        //[]
        //------------------------
        //84
        //Hibernate:
        //    select
        //        is1_0.board_bno,
        //        is1_0.uuid,
        //        is1_0.file_name,
        //        is1_0.ord
        //    from
        //        board_image is1_0
        //    where
        //        is1_0.board_bno=?
        //[BoardImage(uuid=4e894ff5-544c-4649-9b14-5beed89392a0, fileName=84file0.jpg, ord=0), BoardImage(uuid=5c144d47-9ff9-40d8-89e8-de75d262253d, fileName=84file1.jpg, ord=1), BoardImage(uuid=7157bf4a-a2e6-4189-aa38-e0b547145dbe, fileName=84file2.jpg, ord=2)]
        //------------------------
        //83
        //Hibernate:
        //    select
        //        is1_0.board_bno,
        //        is1_0.uuid,
        //        is1_0.file_name,
        //        is1_0.ord
        //    from
        //        board_image is1_0
        //    where
        //        is1_0.board_bno=?
        //[BoardImage(uuid=7dfc01ec-1bd3-44cd-888d-d37e27e18ae3, fileName=83file0.jpg, ord=0), BoardImage(uuid=5426a04c-1f4b-40e8-aa69-e7510b95d4d2, fileName=83file1.jpg, ord=1), BoardImage(uuid=80ef3d12-fac8-4c3f-880d-8619c71b1c0b, fileName=83file2.jpg, ord=2)]
        //------------------------
        //82
        //Hibernate:
        //    select
        //        is1_0.board_bno,
        //        is1_0.uuid,
        //        is1_0.file_name,
        //        is1_0.ord
        //    from
        //        board_image is1_0
        //    where
        //        is1_0.board_bno=?
        //[BoardImage(uuid=5ca2d60e-6e1d-4766-bf5a-275da0bab2ae, fileName=82file1.jpg, ord=1), BoardImage(uuid=79ace14a-f85a-494c-98ce-39d87f934353, fileName=82file2.jpg, ord=2), BoardImage(uuid=270f1579-a7a7-4c09-8048-e877ea8d17cf, fileName=82file0.jpg, ord=0)]
        //------------------------
        //81
        //Hibernate:
        //    select
        //        is1_0.board_bno,
        //        is1_0.uuid,
        //        is1_0.file_name,
        //        is1_0.ord
        //    from
        //        board_image is1_0
        //    where
        //        is1_0.board_bno=?
        //[BoardImage(uuid=84ba40fc-0b42-4e8d-ac07-ace04dfdc270, fileName=81file1.jpg, ord=1), BoardImage(uuid=777a45f9-91db-4569-b2a1-c1e36bf63632, fileName=81file2.jpg, ord=2), BoardImage(uuid=e27aec2c-0061-4e53-9cc4-1154fcd60fa0, fileName=81file0.jpg, ord=0)]

        // @BatchSize 이전
        // 이후
        //Hibernate:
        //    select
        //        b1_0.bno,
        //        b1_0.content,
        //        b1_0.moddate,
        //        b1_0.regdate,
        //        b1_0.title,
        //        b1_0.writer
        //    from
        //        board b1_0
        //    left join
        //        reply r1_0
        //            on r1_0.board_bno=b1_0.bno
        //    order by
        //        b1_0.bno desc
        //    limit
        //        ?, ?
        //90
        //Hibernate:
        //    select
        //        is1_0.board_bno,
        //        is1_0.uuid,
        //        is1_0.file_name,
        //        is1_0.ord
        //    from
        //        board_image is1_0
        //    where
        //        is1_0.board_bno in (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)

        // 목록을 처리하는 쿼리가 실행되고
        // board 객체의 bno를 출력한다.
        // 목록나온 10개의 board 객체의 bno값을 이용해서 in (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
        // 20개의 사이즈로 이미지 테이블을 조회한다.
    }

} // 클래스 종료
