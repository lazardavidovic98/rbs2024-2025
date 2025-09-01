package com.zuehlke.securesoftwaredevelopment.controller;

import com.zuehlke.securesoftwaredevelopment.config.AuditLogger;
import com.zuehlke.securesoftwaredevelopment.domain.*;
import com.zuehlke.securesoftwaredevelopment.repository.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Controller
public class BooksController {

    private static final Logger LOG = LoggerFactory.getLogger(BooksController.class);
    private static final AuditLogger auditLogger = AuditLogger.getAuditLogger(BooksController.class);

    private BookRepository bookRepository;
    private CommentRepository commentRepository;
    private RatingRepository ratingRepository;
    private PersonRepository userRepository;
    private VoucherRepository voucherRepository;
    private TagRepository tagRepository;

    public BooksController(BookRepository bookRepository, CommentRepository commentRepository, RatingRepository ratingRepository, PersonRepository userRepository, TagRepository tagRepository, VoucherRepository voucherRepository) {
        this.bookRepository = bookRepository;
        this.commentRepository = commentRepository;
        this.ratingRepository = ratingRepository;
        this.userRepository = userRepository;
        this.tagRepository = tagRepository;
        this.voucherRepository = voucherRepository;
    }

    @GetMapping("/")
    @PreAuthorize("hasAuthority('VIEW_BOOK_LIST')")
    public String showSearch(Model model) {
        model.addAttribute("books", bookRepository.getAll());
        return "books";
    }

    @GetMapping("/create-form")
    @PreAuthorize("hasAuthority('CREATE_BOOK')")
    public String CreateForm(Model model) {
        model.addAttribute("tags", tagRepository.getAll());
        return "create-form";
    }

    @GetMapping(value = "/api/books/search", produces = "application/json")
    @PreAuthorize("hasAuthority('VIEW_BOOK_LIST')")
    @ResponseBody
    public List<Book> search(@RequestParam("query") String query) throws SQLException {
        return bookRepository.search(query);
    }

    @GetMapping("/books")
    @PreAuthorize("hasAuthority('VIEW_BOOK_LIST')")
    public String showBook(@RequestParam(name = "id", required = false) String id, Model model, Authentication authentication) {
        if (id == null) {
            model.addAttribute("books", bookRepository.getAll());
            return "books";
        }

        User user = (User) authentication.getPrincipal();
        List<Tag> tagList = this.tagRepository.getAll();

        model.addAttribute("book", bookRepository.get(Integer.parseInt(id), tagList));
        List<Comment> comments = commentRepository.getAll(id);
        List<Rating> ratings = ratingRepository.getAll(id);
        Optional<Rating> userRating = ratings.stream().filter(rating -> rating.getUserId() == user.getId()).findFirst();
        if (userRating.isPresent()) {
            model.addAttribute("userRating", userRating.get().getRating());
        }
        if (ratings.size() > 0) {
            Integer sumRating = ratings.stream().map(rating -> rating.getRating()).reduce(0, (total, rating) -> total + rating);
            Double avgRating = (double) sumRating / ratings.size();
            model.addAttribute("averageRating", avgRating);
        }

        List<ViewComment> commentList = new ArrayList<>();

        for (Comment comment : comments) {
            Person person = userRepository.get("" + comment.getUserId());
            commentList.add(new ViewComment(person.getFirstName() + " " + person.getLastName(), comment.getComment()));
        }

        model.addAttribute("comments", commentList);

        return "book";
    }

    @PostMapping("/books")
    @PreAuthorize("hasAuthority('CREATE_BOOK')")
    public String createBook(NewBook newBook) throws SQLException {
        List<Tag> tagList = this.tagRepository.getAll();
        List<Tag> tagsToInsert = newBook.getTags().stream().map(tagId -> tagList.stream().filter(tag -> tag.getId() == tagId).findFirst().get()).collect(Collectors.toList());
        Long id = bookRepository.create(newBook, tagsToInsert);
        return "redirect:/books?id=" + id;
    }

    @GetMapping("/buy-book/{id}")
    @PreAuthorize("hasAuthority('BUY_BOOK')")
    public String showBuyBook(
            @PathVariable("id") int id,
            @RequestParam(required = false) boolean addressError,
            @RequestParam(required = false) boolean bought,
            @RequestParam(required = false) boolean voucherUsed,
            Model model) {

        model.addAttribute("id", id);
        model.addAttribute("voucherUsed", voucherUsed);

        if (addressError) {
            model.addAttribute("addressError", true);
        } else if (bought) {
            model.addAttribute("bought", true);
        }

        return "buy-book";
    }

    @PostMapping("/buy-book/{id}")
    @PreAuthorize("hasAuthority('BUY_BOOK')")
    public String buyBook(@PathVariable("id") int id, String address, String voucher) {
        String voucherUsed = "";
        
        if (address.length() < 10) {
            return String.format("redirect:/buy-book/%s?addressError=true", id);
        }

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated()) {
            User user = (User) authentication.getPrincipal();

            // Only process voucher if it's not null or empty
            if (voucher != null && !voucher.trim().isEmpty()) {
                boolean exist = voucherRepository.checkIfVoucherExist(voucher);
                
                if (exist) {
                    if (voucherRepository.checkIfVoucherIsAssignedToUser(voucher, user.getId())) {
                        voucherRepository.deleteVoucher(voucher);
                        voucherUsed = "&voucherUsed=true";
                    }
                }
            }
        }

        return String.format("redirect:/buy-book/%s?bought=true%s", id, voucherUsed);
    }

}
