package com.zuehlke.securesoftwaredevelopment.domain;

public class Rating {

    private int bookId;

    private int userId;

    private int rating;

    /** Used by Jackson when a rating is posted as JSON. */
    public Rating() {
    }

    public Rating(int bookId, int userId, int rating) {
        this.bookId = bookId;
        this.userId = userId;
        this.rating = rating;
    }

    public int getBookId() {
        return bookId;
    }

    public void setBookId(int bookId) {
        this.bookId = bookId;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public int getRating() {
        return rating;
    }

    public void setRating(int rating) {
        this.rating = rating;
    }
}
