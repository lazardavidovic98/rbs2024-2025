package com.zuehlke.securesoftwaredevelopment.controller;

import com.zuehlke.securesoftwaredevelopment.domain.Book;
import com.zuehlke.securesoftwaredevelopment.domain.NewBook;
import com.zuehlke.securesoftwaredevelopment.domain.Rating;
import com.zuehlke.securesoftwaredevelopment.domain.Tag;
import com.zuehlke.securesoftwaredevelopment.domain.User;
import com.zuehlke.securesoftwaredevelopment.repository.BookRepository;
import com.zuehlke.securesoftwaredevelopment.repository.CommentRepository;
import com.zuehlke.securesoftwaredevelopment.repository.RatingRepository;
import com.zuehlke.securesoftwaredevelopment.repository.TagRepository;
import com.zuehlke.securesoftwaredevelopment.repository.VoucherRepository;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.server.ResponseStatusException;

import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Controller
public class BooksController {

    private final BookRepository bookRepository;
    private final CommentRepository commentRepository;
    private final RatingRepository ratingRepository;
    private final VoucherRepository voucherRepository;
    private final TagRepository tagRepository;

    public BooksController(BookRepository bookRepository, CommentRepository commentRepository,
                           RatingRepository ratingRepository, TagRepository tagRepository,
                           VoucherRepository voucherRepository) {
        this.bookRepository = bookRepository;
        this.commentRepository = commentRepository;
        this.ratingRepository = ratingRepository;
        this.tagRepository = tagRepository;
        this.voucherRepository = voucherRepository;
    }

    @GetMapping("/")
    @PreAuthorize("hasAuthority('VIEW_BOOK_LIST')")
    public String home() {
        return "redirect:/books";
    }

    @GetMapping("/create-form")
    @PreAuthorize("hasAuthority('CREATE_BOOK')")
    public String createForm(Model model) {
        model.addAttribute("tags", tagRepository.getAll());
        return "create-form";
    }

    @GetMapping(value = "/api/books/search", produces = "application/json")
    @PreAuthorize("hasAuthority('VIEW_BOOK_LIST')")
    @ResponseBody
    public List<Book> search(@RequestParam("query") String query) {
        return bookRepository.search(query);
    }

    @GetMapping("/books")
    @PreAuthorize("hasAuthority('VIEW_BOOK_LIST')")
    public String showBook(@RequestParam(name = "id", required = false) Integer id, Model model,
                           @AuthenticationPrincipal User user) {
        if (id == null) {
            model.addAttribute("books", bookRepository.getAll());
            return "books";
        }

        Book book = bookRepository.get(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        model.addAttribute("book", book);

        List<Rating> ratings = ratingRepository.getAll(id);
        ratings.stream()
                .filter(rating -> rating.getUserId() == user.getId())
                .findFirst()
                .ifPresent(rating -> model.addAttribute("userRating", rating.getRating()));
        ratings.stream()
                .mapToInt(Rating::getRating)
                .average()
                .ifPresent(average -> model.addAttribute("averageRating", Math.round(average * 100) / 100.0));

        model.addAttribute("comments", commentRepository.getAllForBook(id));
        return "book";
    }

    @PostMapping("/books")
    @PreAuthorize("hasAuthority('CREATE_BOOK')")
    public String createBook(NewBook newBook) {
        List<Integer> requestedTags = newBook.getTags() == null ? Collections.emptyList() : newBook.getTags();
        List<Tag> tags = tagRepository.getAll().stream()
                .filter(tag -> requestedTags.contains(tag.getId()))
                .collect(Collectors.toList());
        long id = bookRepository.create(newBook, tags);
        return "redirect:/books?id=" + id;
    }

    @GetMapping("/buy-book/{id}")
    @PreAuthorize("hasAuthority('BUY_BOOK')")
    public String showBuyBook(@PathVariable("id") int id,
                              @RequestParam(required = false) boolean addressError,
                              @RequestParam(required = false) boolean bought,
                              @RequestParam(required = false) boolean voucherUsed,
                              Model model) {
        model.addAttribute("id", id);
        model.addAttribute("voucherUsed", voucherUsed);
        model.addAttribute("addressError", addressError);
        model.addAttribute("bought", bought && !addressError);
        return "buy-book";
    }

    @PostMapping("/buy-book/{id}")
    @PreAuthorize("hasAuthority('BUY_BOOK')")
    public String buyBook(@PathVariable("id") int id, String address, String voucher,
                          @AuthenticationPrincipal User user) {
        if (address == null || address.trim().length() < 10) {
            return "redirect:/buy-book/" + id + "?addressError=true";
        }

        String voucherUsed = "";
        if (voucher != null && !voucher.trim().isEmpty()
                && isVoucherOf(voucher.trim(), user.getUsername())
                && voucherRepository.redeem(voucher.trim())) {
            voucherUsed = "&voucherUsed=true";
        }
        return "redirect:/buy-book/" + id + "?bought=true" + voucherUsed;
    }

    /**
     * Vouchers are personal: the code format is 4 digits + username + 4 digits (e.g. 1234bruce5678).
     * The check is an exact match in Java instead of {@code LIKE '%username%'}, where a username
     * containing {@code %} or {@code _} would act as a wildcard.
     */
    private static boolean isVoucherOf(String code, String username) {
        return Pattern.matches("\\d{4}" + Pattern.quote(username) + "\\d{4}", code);
    }
}
