package org.mbc.board.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;


@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class BoardDTO {

    private Long bno;

    @NotEmpty
    @Size(min = 3, max = 100)
    private String title;

    @NotEmpty
    private String content;

    @NotEmpty
    private String writer;

    private LocalDateTime regDate;

    private LocalDateTime modDate;

    // p640 추가
    private List<String> fileNames; // 첨부파일 목록
    // 리포지토리에서 처리는 엔티티는
    // private Set<BoardImage> imageSet = new HashSet<BoardImage>();
    // dto를 엔티티로 변환하는 ModelMapper를 사용했었는데 다양한 처리를 위해서 custom
    // -> 서비스 계층에서 처리


}
