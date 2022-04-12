package com.fairbilling;

import java.time.LocalTime;

public class UserSession {
	
	private String userId;
	private LocalTime startTime;
	private LocalTime endTime;
	
	public UserSession() {}
	
	public UserSession(String userId) {
		this.userId = userId;
	}
	
	public UserSession(String userId, LocalTime startTime, LocalTime endTime) {
		this.userId = userId;
		this.startTime = startTime;
		this.endTime = endTime;
	}
	
	// Generated getters and setters
	public String getUserId() {
		return userId;
	}
	public void setUserId(String userId) {
		this.userId = userId;
	}
	public LocalTime getStartTime() {
		return startTime;
	}
	public void setStartTime(LocalTime startTime) {
		this.startTime = startTime;
	}
	public LocalTime getEndTime() {
		return endTime;
	}
	public void setEndTime(LocalTime endTime) {
		this.endTime = endTime;
	}

	// Generated toString
	@Override
	public String toString() {
		return "UserSession [userId=" + userId + ", startTime=" + startTime + ", endTime=" + endTime + "]";
	}
	
	
}
