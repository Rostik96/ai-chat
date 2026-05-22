package dev.rost.aichat;

import java.time.LocalDateTime;
import java.util.List;

record ChatView(Long id, String title, LocalDateTime createdAt, List<Entry> entries) {

    record Entry(String role, String content) {}
}
