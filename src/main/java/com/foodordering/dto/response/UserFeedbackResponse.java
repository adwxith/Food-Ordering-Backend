package com.foodordering.dto.response;

import java.time.LocalDateTime;

public class UserFeedbackResponse {
    private int rating;
    private String comments;
    private LocalDateTime createdAt;

    public UserFeedbackResponse(int rating, String comments, LocalDateTime createdAt) {
        this.rating = rating;
        this.comments = comments;
        this.createdAt = createdAt;
    }

	public int getRating() {
		return rating;
	}

	public void setRating(int rating) {
		this.rating = rating;
	}

	public String getComments() {
		return comments;
	}

	public void setComments(String comments) {
		this.comments = comments;
	}

	public LocalDateTime getCreatedAt() {
		return createdAt;
	}

	public void setCreatedAt(LocalDateTime createdAt) {
		this.createdAt = createdAt;
	}

    
}
