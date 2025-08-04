package org.mbc.board.controller;


import lombok.extern.log4j.Log4j2;
import net.coobird.thumbnailator.Thumbnailator;
import org.mbc.board.dto.upload.UploadFileDTO;
import org.mbc.board.dto.upload.UploadResultDTO;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

@RestController
@Log4j2
public class UpDownController {

    @Value("${org.mbc.upload.path}") //import org.springframework.beans.factory.annotation.Value;
    private String uploadPath; // c://upload (미리 폴더 생성필수)

    @PostMapping(value="/upload", consumes= MediaType.MULTIPART_FORM_DATA_VALUE)
    // http://localhost:/80/upload           프론트에서 파일처리용 form 을 사용
    public List<UploadResultDTO> upload(UploadFileDTO uploadFileDTO){
        // String에서 List<UploadResultDTO> p605
        log.info(uploadFileDTO);

        if(uploadFileDTO.getFiles() != null){

            final List<UploadResultDTO> list = new ArrayList<>();  // p605 추가
            //    부모 계층                         자식 계층

            // 파일이 있으면
            uploadFileDTO.getFiles().forEach(
                    multipartFile -> {
                        // log.info(multipartFile.getOriginalFilename());
                        // 원본 파일명을 출력!!

                        String originalName = multipartFile.getOriginalFilename();
                        log.info(originalName); // 원본파일명 출력

                        String uuid = UUID.randomUUID().toString(); // 랜덤문자열이 생성
                        Path savePath = Paths.get(uploadPath, uuid+"_"+originalName);
                        //import java.nio.file.Path;
                        //                                uuid_원본파일명!!!

                        boolean image = false ; // UploadResultDTO에 이미지 유무를 판단용

                        try{
                            multipartFile.transferTo(savePath); // 실제 저장용
                            // c://upload에 파일이 저장됨!!!

                            // 이미지 파일이 들어오면 섬네일 처리용
                            if(Files.probeContentType(savePath).startsWith("image")){
                                image = true; // 이미지인 경우 true 처리  p606 추가
                                // 파일 헤더에 image를 찾음 == true;
                                File thumbFile = new File(uploadPath,"s_"+uuid+"_"+originalName);
                                //섬네일파일생성
                                // import java.io.File;    패턴만듬 s_uuid_파일명 이 됨!!!
                                Thumbnailator.createThumbnail(savePath.toFile(), thumbFile, 200,200);
                                //                                                    최대 가로픽셀 세로픽셀
                                //                                                    정사각형으로 만들어지지 않는다.

                            } // 섬네일 처리 if문 종료

                        }catch(IOException e){
                            e.printStackTrace(); // 저장시 예외발생 출력용
                        } // 파일 처리 종료

                        list.add(UploadResultDTO.builder()
                                        .uuid(uuid)
                                        .fileName(originalName)
                                        .img(image)
                                        .build());
                    } // multipartFile 람다식 종료
            ); // forEach 종료

            return list;

        } // if문 종료

        return null;
    }// upload 메서드 종료

    @GetMapping("/view/{fileName}")
    public ResponseEntity<Resource> viewFileGET(@PathVariable String fileName){
        // import org.springframework.core.io.Resource;
        // 파일이름이 들어오면 응답용 객체에 resocerce를 담아서 보낸다.

        Resource resource = new FileSystemResource(uploadPath+File.separator+fileName);
        //                                        c://upload//파일명  File.separator 분리기!!
        String resourceName = resource.getFilename();  // 위에서 만든 경로가 문자열로 들어감

        HttpHeaders headers = new HttpHeaders();
        // import org.springframework.http.HttpHeaders;

        try{
            headers.add("Content-Type",Files.probeContentType(resource.getFile().toPath()));
            // HttpHeader에 추가 -> 파일이 넘어간다를 명시
        }catch(Exception e){
            //e.printStackTrace(); 콘솔에 찍음
            return ResponseEntity.internalServerError().build();  // 프론트에 보냄
            // 응담 객체에 서버에러임을 보냄
        }
        return ResponseEntity.ok().headers(headers).body(resource);
        //        응답객체    정상(200)  헤더에 헤더내용 바디에 파일정보를 리턴 함!
    }

@DeleteMapping("/remove/{fileName}")
public Map<String,Boolean> removeFile(@PathVariable String fileName){
// 파일삭제용 메서드 파일 이름이 넘어오면 디스크에서 삭제를 진행한다.
// 만약 이미지라면 섬네일 까지 삭제
    Resource resource = new FileSystemResource(uploadPath+File.separator+fileName);
    // 파일 경로와 파일명이 완성이 된다.
    String resourceName = resource.getFilename();

    Map<String, Boolean> resultMap = new HashMap<>();
    // 부모 객체                          자식 객체

    boolean removed = false;

    try{ // 예외가 발생가능성 있는 코드
        String contentType = Files.probeContentType(resource.getFile().toPath());
        removed = resource.getFile().delete(); // 삭제처리 됨!
        // ture           삭제가 성공

        if(contentType.startsWith("image")){
            // 사진이면 섬네일 까지 삭제
            File thumbnailFile = new File(uploadPath+File.separator+"s_"+fileName);
            thumbnailFile.delete(); // 섬네일 파일 삭제용 코드
        } // 섬네일 삭제 if문 종료

    }catch (Exception e){  // 예외 처리용
        log.error(e.getMessage()); // 예외발생시 로그 출력
    } // 삭제 기능 종료

    resultMap.put("result",removed);
    //                      삭제 되면 ture
    return resultMap;
}


} // 컨트롤러 클래스 종료
