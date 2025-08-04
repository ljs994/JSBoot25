package org.mbc.board.dto.upload;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UploadResultDTO {
    // 여러개의 파일이 업로드되면 업로드 결과도 여러개가 발생
    // 여러정보를 반환해야 하므로  dto 처리 함
    // 업로드된 파일의 uuid 값과 파일 이름, 이미지 여부를 객체로 구성
    // getLink()메서드로 첨부파일의 경로를 보냄


    private String uuid;

    private String fileName;

    private boolean img;

    public String getLink(){
        // 이미지인경우에는 섬네일 처리
        if(img){
            return "s_"+uuid+"_"+fileName;  //이미지 섬네일 파일명
        }else{
            return uuid+"_"+fileName;
        } // if문 종료

    } // 메서드 종료

} // 클래스 종료
