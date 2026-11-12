package com.zuehlke.securesoftwaredevelopment.controller;

import com.zuehlke.securesoftwaredevelopment.domain.Comment;
import com.zuehlke.securesoftwaredevelopment.domain.User;
import com.zuehlke.securesoftwaredevelopment.repository.CommentRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@Controller
public class CommentController {

    static final int MAX_COMMENT_LENGTH = 500;

    private final CommentRepository commentRepository;

    public CommentController(CommentRepository commentRepository) {
        this.commentRepository = commentRepository;
    }

    /**
     * Stores the comment exactly as typed. XSS is prevented by encoding on output (Thymeleaf th:text,
     * textContent in JavaScript) and by the Content-Security-Policy, not by HTML-escaping on input,
     * which used to double-encode comments such as "Tom &amp; Jerry".
     */
    @PostMapping(value = "/comments", consumes = "application/json")
    @PreAuthorize("hasAuthority('ADD_COMMENT')")
    public ResponseEntity<Void> createComment(@RequestBody Comment comment, @AuthenticationPrincipal User user) {
        String text = comment.getComment() == null ? "" : comment.getComment().trim();
        if (text.isEmpty() || text.length() > MAX_COMMENT_LENGTH) {
            return ResponseEntity.badRequest().build();
        }

        comment.setComment(text);
        comment.setUserId(user.getId());
        commentRepository.create(comment);
        return ResponseEntity.noContent().build();
    }
}
