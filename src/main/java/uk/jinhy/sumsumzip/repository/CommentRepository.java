package uk.jinhy.sumsumzip.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import uk.jinhy.sumsumzip.entity.Cat;
import uk.jinhy.sumsumzip.entity.Comment;

public interface CommentRepository  extends JpaRepository<Comment, Long> {
    Long deleteByCat(Cat cat);
}
