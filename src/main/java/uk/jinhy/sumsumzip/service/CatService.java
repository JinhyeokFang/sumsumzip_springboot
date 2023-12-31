package uk.jinhy.sumsumzip.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import uk.jinhy.sumsumzip.entity.*;
import uk.jinhy.sumsumzip.repository.*;

import java.util.List;

@Slf4j
@RequiredArgsConstructor
@Service
@Transactional
public class CatService {
    private final UserRepository userRepository;
    private final CatRepository catRepository;
    private final CatLikesRepository catLikesRepository;
    private final CommentRepository commentRepository;

    public void addCat(String url, Long userId, String title, String description) {
        var user = userRepository.findById(userId).get();
        var cat = Cat.builder()
                .url(url)
                .user(user)
                .title(title)
                .description(description)
                .build();
        catRepository.save(cat);
    }

    public void deleteCat(Long userId, Long catId) throws Exception {
        var cat = catRepository.findById(catId).get();
        if (cat.getUser().getId() != userId) {
            throw new Exception("삭제 권한이 없는 사용자입니다.");
        }
        catLikesRepository.deleteByCat(cat);
        commentRepository.deleteByCat(cat);
        catRepository.delete(cat);
    }

    public List<Cat> getCats(Long pageNumber) {
        var page = PageRequest.of(pageNumber.intValue(), 10, Sort.by(Sort.Direction.DESC, "id"));
        return catRepository.findAll(page).getContent();
    }
    public List<Cat> getCats() {
        return getCats(0l);
    }

    public List<Cat> getCatsByUserId(Long userId, Long pageNumber) {
        var page = PageRequest.of(pageNumber.intValue(), 10, Sort.by(Sort.Direction.DESC, "id"));
        return catRepository.findAllByUserId(page, userId).getContent();
    }

    public List<Cat> getCatsByUserId(Long userId) { return getCatsByUserId(userId, 0l); }

    public void like(String userEmail, Long catId) {
        var user = userRepository.findByEmail(userEmail).get();
        var cat = catRepository.findById(catId).get();
        var newLike = CatLikes.builder()
                .user(user)
                .cat(cat)
                .build();
        if (catLikesRepository.findByUserAndCat(user, cat).isEmpty()) {
            catLikesRepository.save(newLike);
        }
    }

    public void dislike(String userEmail, Long catId) {
        var user = userRepository.findByEmail(userEmail).get();
        var cat = catRepository.findById(catId).get();
        catLikesRepository.deleteAllByUserAndCat(user, cat);
    }

    public void addComment(String userEmail, Long catId, String content) {
        var user = userRepository.findByEmail(userEmail).get();
        var cat = catRepository.findById(catId).get();
        var comment = Comment.builder()
                .content(content)
                .user(user)
                .cat(cat)
                .build();
        commentRepository.save(comment);
    }

    public void editComment(Long commentId, String content) {
        var comment = commentRepository.findById(commentId);
        if (!comment.isEmpty()) {
            var newComment = comment.get();
            newComment.editContent(content);
            commentRepository.save(newComment);
        }
    }

    public void removeComment(Long commentId) {
        commentRepository.deleteById(commentId);
    }

    public Cat getCatById(Long catId) {
        return catRepository.findById(catId).get();
    }

    public List<Cat> getCatsByFollowingList(String email, Long pageNumber) {
        var user = userRepository.findByEmail(email).get();
        var followingList = user
                .getFollowing()
                .stream()
                .map(UserFollows::getFollowing)
                .map(User::getId)
                .toList();

        return catRepository.findByListOfUserId(PageRequest.of(pageNumber.intValue(), 10), followingList).getContent();
    }

    public List<Cat> getCatsByLikeList(String email) {
        var user = userRepository.findByEmail(email).get();
        return catLikesRepository
                .findByUser(user)
                .stream()
                .map(CatLikes::getCat)
                .toList();
    }
}
