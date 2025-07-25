package com.example.musicapp.repository;

import com.example.musicapp.model.Song;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

public interface SongRepository extends MongoRepository<Song, String> {
    Optional<Song> findByYoutubeUrl(String youtubeUrl);

    List<Song> findByTitleContainingIgnoreCase(String title);
}

