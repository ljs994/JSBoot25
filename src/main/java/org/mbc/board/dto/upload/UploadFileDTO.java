package org.mbc.board.dto.upload;

import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Data
public class UploadFileDTO {

    private List<MultipartFile> files;
    // 리스트에 객체는 파일처리하는 객체로 지정!
    // postman 테스트시 key값 통일
}
