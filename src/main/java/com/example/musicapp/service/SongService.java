package com.example.musicapp.service;


import com.example.musicapp.model.Song;
import com.example.musicapp.repository.SongRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class SongService {

    @Autowired
    private SongRepository songRepository;

    public Song save(Song song) {
        return songRepository.save(song);
    }

    public List<Song> getAll() {
        return songRepository.findAll();
    }

    public Optional<Song> getById(String id) {
        return songRepository.findById(id);
    }

    public List<Song> search(String title) {
        return songRepository.findByTitleContainingIgnoreCase(title);
    }

    public void delete(String id) {
        songRepository.deleteById(id);
    }
    public Optional<String> checkOrLinkSong(String youtubeUrl, String uid) {
        Optional<Song> songOpt = songRepository.findByYoutubeUrl(youtubeUrl);

        if (songOpt.isPresent()) {
            Song song = songOpt.get();

            if (song.getUserIds().contains(uid)) {
                return Optional.of("exists");  // Already linked to this user
            } else {
                song.getUserIds().add(uid);
                songRepository.save(song);
                return Optional.of("linked");  // Just linked now
            }
        }
        return Optional.empty();  // Song doesn't exist at all
    }

}

