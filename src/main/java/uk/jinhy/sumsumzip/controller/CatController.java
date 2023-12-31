package uk.jinhy.sumsumzip.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;
import uk.jinhy.sumsumzip.controller.cat.*;
import uk.jinhy.sumsumzip.service.*;

@Slf4j
@RequiredArgsConstructor
@RequestMapping("/cat")
@RestController
public class CatController {
    private final S3Service s3Service;
    private final UserService userService;
    private final CatService catService;

    @PostMapping("/upload")
    public UploadCatImageDTO uploadCatImage(
            @RequestPart(value = "image") MultipartFile imageFile,
            @RequestPart(value = "title") String title,
            @RequestPart(value = "description") String description,
            Authentication authentication
    ) {
        if (authentication == null) {
            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "토큰이 필요합니다."
            );
        }
        try {
            var email = (String) authentication.getPrincipal();
            var userId = userService.getUserIdByEmail(email);
            var imageURL = s3Service.saveFile(imageFile);
            catService.addCat(imageURL, userId, title, description);
            return new UploadCatImageDTO(
                    true,
                    imageURL
            );
        } catch (Exception error) {
            log.error(error.getMessage());
            return new UploadCatImageDTO(
                    false,
                    ""
            );
        }
    }

    @DeleteMapping("/{catId}")
    public void removeCatImage(
            @PathVariable Long catId,
            Authentication authentication
    ) {
        if (authentication == null) {
            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "토큰이 필요합니다."
            );
        }
        try {
            var email = (String) authentication.getPrincipal();
            var userId = userService.getUserIdByEmail(email);
            catService.deleteCat(userId, catId);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @GetMapping("")
    public GetCatDTO getCats(
            @RequestParam(value = "pageNumber", required = false) Long pageNumber
    ) {
        if (pageNumber == null) {
            return new GetCatDTO(
                    catService.getCats().stream().map(CatDTO::new).toList()
            );
        }
        return new GetCatDTO(
                catService.getCats(pageNumber).stream().map(CatDTO::new).toList()
        );
    }

    @GetMapping("/user/{userId}")
    public GetCatDTO getCatsByUserId(
            @PathVariable Long userId,
            @RequestParam(value = "pageNumber", required = false) Long pageNumber
    ) {
        if (pageNumber == null) {
            return new GetCatDTO(
                    catService.getCatsByUserId(userId).stream().map(CatDTO::new).toList()
            );
        }
        return new GetCatDTO(
                catService.getCatsByUserId(userId, pageNumber).stream().map(CatDTO::new).toList()
        );
    }

    @GetMapping("/{catId}")
    public CatDTO getCatById(
            @PathVariable Long catId
    ) {
        return new CatDTO(
                catService.getCatById(catId)
        );
    }

    @GetMapping("/follows")
    public GetCatDTO getCatsByFollowingList(
            Authentication authentication,
            @RequestParam(value = "pageNumber", required = false) Long pageNumber
    ) {
        if (authentication == null) {
            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "토큰이 필요합니다."
            );
        }
        try {
            var email = (String) authentication.getPrincipal();
            if (pageNumber == null) {
                return new GetCatDTO(
                        catService.getCatsByFollowingList(email, 0l).stream().map(CatDTO::new).toList()
                );
            }
            return new GetCatDTO(
                    catService.getCatsByFollowingList(email, pageNumber).stream().map(CatDTO::new).toList()
            );
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @GetMapping("/likes")
    public GetCatDTO getCatsByLikeList(
            Authentication authentication
    ) {
        if (authentication == null) {
            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "토큰이 필요합니다."
            );
        }
        try {
            var email = (String) authentication.getPrincipal();
            return new GetCatDTO(
                    catService.getCatsByLikeList(email).stream().map(CatDTO::new).toList()
            );
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @PutMapping("/like")
    public void like(
            LikeRequestDTO body,
            Authentication authentication
    ) {
        if (authentication == null) {
            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "토큰이 필요합니다."
            );
        }
        var email = (String) authentication.getPrincipal();
        try {
            catService.like(email, body.getCatId());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @PutMapping("/dislike")
    public void dislike(
            LikeRequestDTO body,
            Authentication authentication
    ) {
        if (authentication == null) {
            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "토큰이 필요합니다."
            );
        }
        var email = (String) authentication.getPrincipal();
        try {
            catService.dislike(email, body.getCatId());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @PostMapping("/comment")
    public void addComment(
            AddCommentRequestDTO body,
            Authentication authentication
    ) {
        if (authentication == null) {
            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "토큰이 필요합니다."
            );
        }
        var email = (String) authentication.getPrincipal();
        try {
            catService.addComment(email, body.getCatId(), body.getContent());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @DeleteMapping("/comment/{commentId}")
    public void deleteComment(
            @PathVariable Long commentId,
            Authentication authentication
    ) {
        if (authentication == null) {
            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "토큰이 필요합니다."
            );
        }
        try {
            catService.removeComment(commentId);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
