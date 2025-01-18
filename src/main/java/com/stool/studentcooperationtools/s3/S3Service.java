package com.stool.studentcooperationtools.s3;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.CannedAccessControlList;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;import com.stool.studentcooperationtools.domain.file.FileType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.*;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class S3Service {

    @Value("${aws.s3.bucketName}")
    public String bucketName;

    private final AmazonS3 amazonS3;

    public void deleteFile(final String fileName){
        amazonS3.deleteObject(bucketName,fileName);
    }

    public HashMap<String, List<String>> uploadFile(final String originalFileName,final String fileCode){
        HashMap<String,List<String>> fileNameSet = new HashMap<>();
        String[] metaData = fileCode.split(",");
        String extension = getExtension(metaData[0]);
        byte[] fileBytes = Base64.getDecoder().decode(metaData[1]);
        uploadFileToS3(originalFileName, extension, fileBytes, fileNameSet);
        return fileNameSet;
    }

    private void uploadFileToS3(final String originalFileName, final String extension, final byte[] fileBytes, final HashMap<String, List<String>> fileNameSet) {
        try {
            File tempFile = File.createTempFile("File","." + extension);
            try(OutputStream outputStream = new FileOutputStream(tempFile)) {
                outputStream.write(fileBytes);
            }
            String fileName = UUID.randomUUID().toString();
            String thumbnailUrl = amazonS3.getUrl(bucketName,fileName).toString();
            fileNameSet.put(originalFileName,List.of(fileName, extension,thumbnailUrl));
            amazonS3.putObject(new PutObjectRequest(bucketName,fileName,tempFile)
                            .withMetadata(new ObjectMetadata(){{
                                setContentType(FileType.getMimeType(extension));
                            }})
                            .withCannedAcl(CannedAccessControlList.PublicRead));
            closeFile(tempFile);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static void closeFile(final File tempFile) {
        try(FileOutputStream fileOutputStream = new FileOutputStream(tempFile)) {
            fileOutputStream.close();
            // 파일 삭제시 전부 아웃풋 닫음
            if (tempFile.delete()) {
                log.info("File delete success");
            } else {
                log.info("File delete fail");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static String getExtension(final String fileCode){
        if(!StringUtils.hasText(fileCode)){
            throw new IllegalArgumentException("저장할 파일이 없습니다.");
        }
        return FileType.getFileExtension(fileCode);
    }

    public HashMap<String,String> uploadFile(List<MultipartFile> multipartFiles){
        HashMap<String,String> fileNameSet = new HashMap<>();
        // forEach 구문을 통해 multipartFiles 리스트로 넘어온 파일들을 순차적으로 fileNameList 에 추가
        multipartFiles.forEach(file -> {
            String originalFilename = file.getOriginalFilename();
            String fileName = createFileName(originalFilename);
            ObjectMetadata objectMetadata = createObjectMetaData(file);
            putObject(file, fileName, objectMetadata);
            fileNameSet.put(originalFilename,amazonS3.getUrl(bucketName,fileName).toString());
        });
        return fileNameSet;
    }

    //S3에 생성한 S3 파일을 업로드
    private void putObject(final MultipartFile file, final String fileName, final ObjectMetadata objectMetadata) {
        try(InputStream inputStream = file.getInputStream()){
            amazonS3.putObject(new PutObjectRequest(bucketName, fileName, inputStream, objectMetadata)
                    .withCannedAcl(CannedAccessControlList.PublicRead));
        } catch (IOException e){
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "파일 업로드에 실패했습니다.");
        }
    }

    //S3에 올리는 메타데이터를 생성
    private static ObjectMetadata createObjectMetaData(final MultipartFile file) {
        ObjectMetadata objectMetadata = new ObjectMetadata();
        objectMetadata.setContentLength(file.getSize());
        objectMetadata.setContentType(file.getContentType());
        return objectMetadata;
    }

    //UUID로 파일명을 난수화하고, 확장자를 붙인다.
    public String createFileName(String fileName){
        return UUID.randomUUID().toString().concat(getFileExtension(fileName));
    }

    //"."을 기준으로 파일 확장자를 추출(file.txt..)
    private String getFileExtension(String fileName){
        try{
            return fileName.substring(fileName.lastIndexOf("."));
        } catch (StringIndexOutOfBoundsException e){
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Wrong file extensions( fileName : " + fileName + ")");
        }
    }

    public UrlResource getUrlResource(final String fileName){
        return new UrlResource(amazonS3.getUrl(bucketName,fileName));
    }

    public String getContentDisposition(final String originalFileName){
        return "attachment; filename=\"" + originalFileName + "\"";
    }

    public static String getS3FileUrl(final String fileName){
        return "https://stool-s3.s3.ap-northeast-2.amazonaws.com/userFile/" + fileName;
    }
}
