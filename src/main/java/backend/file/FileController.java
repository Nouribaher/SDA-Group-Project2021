package backend.file;

import java.util.List;
import java.util.stream.Collectors;

import backend.user.ResourceNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

@RestController
@CrossOrigin("http://localhost:8080")
public class FileController {


    @Autowired
    private FileStorageService storageService;
    FileDBRepository fileDBRepository;

    @Autowired
    public FileController(FileDBRepository fileDBRepository) {
        this.fileDBRepository = fileDBRepository;
    }

// TODO - upload a file by a specific user (the file should have a owner)
    @PostMapping("/upload")
    public ResponseEntity<ResponseMessage> uploadFile(@RequestParam("file") MultipartFile file) {
        String message = "";
        try {
            storageService.store(file);

            message = "Uploaded the file successfully: " + file.getOriginalFilename();
            return ResponseEntity.status(HttpStatus.OK).body(new ResponseMessage(message));
        } catch (Exception e) {
            message = "Could not upload the file: " + file.getOriginalFilename() + "!";
            return ResponseEntity.status(HttpStatus.EXPECTATION_FAILED).body(new ResponseMessage(message));
        }
    }

    @GetMapping("/files")
    public ResponseEntity<List<ResponseFile>> getListFiles() {
        List<ResponseFile> files = storageService.getAllFiles().map(dbFile -> {
            String fileDownloadUri = ServletUriComponentsBuilder
                    .fromCurrentContextPath()
                    .path("/files/")
                    // .path(dbFile.getId())
                    .toUriString();

            return new ResponseFile(
                    dbFile.getName(),
                    fileDownloadUri,
                    dbFile.getType(),
                    dbFile.getData().length);
        }).collect(Collectors.toList());

        return ResponseEntity.status(HttpStatus.OK).body(files);
    }

    @GetMapping("/files/{id}")
    public ResponseEntity<byte[]> getFile(@PathVariable Long id) {
        FileDB fileDB = storageService.getFile(id);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fileDB.getName() + "\"")
                .body(fileDB.getData());
    }

    @DeleteMapping("/files/{id}")
    public ResponseEntity<FileDB> deleteFile(@PathVariable Long id) {
        FileDB fileDB = fileDBRepository.findById(id)
                .orElseThrow(ResourceNotFoundException::new);
        fileDBRepository.delete(fileDB);
        return ResponseEntity.ok(fileDB);
    }




    /**
     * Add a like to a picture providing the file/picture id
     * @param fileId: the id of the file
     * @return - status of process
     */
    @PostMapping("/likes/{fileId}")
    public ResponseEntity<FileDB> addLike(@PathVariable Long fileId) {

        FileDB fileDB = fileDBRepository.findById(fileId).orElseThrow(ResourceNotFoundException::new);
        int count = Integer.parseInt(fileDB.getLikes()) + 1;
        fileDB.setLikes(Integer.toString(count));
        fileDBRepository.save(fileDB);
        return ResponseEntity.ok(fileDB);
    }

    /**
     * Add a dislike to a picture providing the file/picture id
     * @param fileId: the id of the file
     * @return - status of process
     */
    @PostMapping("/dislikes/{fileId}")
    public ResponseEntity<FileDB> addDisLike(@PathVariable Long fileId) {

        FileDB fileDB = fileDBRepository.findById(fileId).orElseThrow(ResourceNotFoundException::new);
        int count = Integer.parseInt(fileDB.getLikes()) - 1;
        fileDB.setLikes(Integer.toString(count));
        fileDBRepository.save(fileDB);
        return ResponseEntity.ok(fileDB);
    }

    /**
     * Return likes from a specific file/picture
     * @param fileId: the id of the file
     * @return an int - number of likes
     */
    @GetMapping("/likes/{fileId}")
    public ResponseEntity<Integer> getLike(@PathVariable Long fileId) {

        FileDB fileDB = fileDBRepository.findById(fileId).orElseThrow(ResourceNotFoundException::new);
        int count = Integer.parseInt(fileDB.getLikes());
        return ResponseEntity.ok(count);
    }

}