package com.example.musicapp.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Document(collection = "songs")
public class Song {

    @Id
    private String id;
    private String title;
    private String youtubeUrl;
    private String driveFileId;
    private String thumbnail;
    private String format;
    private LocalDateTime uploadedAt;
    private List<String> userIds = new ArrayList<>();



    public Song() {}

    public Song(String title, String youtubeUrl, String driveFileId, String thumbnail, String format) {
        this.title = title;
        this.youtubeUrl = youtubeUrl;
        this.driveFileId = driveFileId;
        this.thumbnail = thumbnail;
        this.format = format;
        this.uploadedAt = LocalDateTime.now();
    }


    // Getters and setters
    public List<String> getUserIds() {
        return userIds;
    }

    public void setUserIds(List<String> userIds) {
        this.userIds = userIds;
    }
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getYoutubeUrl() {
        return youtubeUrl;
    }

    public void setYoutubeUrl(String youtubeUrl) {
        this.youtubeUrl = youtubeUrl;
    }

    public String getDriveFileId() {
        return driveFileId;
    }

    public void setDriveFileId(String driveFileId) {
        this.driveFileId = driveFileId;
    }

    public String getThumbnail() {
        return thumbnail;
    }

    public void setThumbnail(String thumbnail) {
        this.thumbnail = thumbnail;
    }

    public String getFormat() {
        return format;
    }

    public void setFormat(String format) {
        this.format = format;
    }

    public LocalDateTime getUploadedAt() {
        return uploadedAt;
    }

    public void setUploadedAt(LocalDateTime uploadedAt) {
        this.uploadedAt = uploadedAt;
    }
}

