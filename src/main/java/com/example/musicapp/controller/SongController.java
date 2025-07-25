package com.example.musicapp.controller;


import com.example.musicapp.model.Song;
import com.example.musicapp.service.SongService;
import com.google.firebase.auth.FirebaseToken;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/songs")
public class SongController extends AuthenticatedController{

    @Autowired
    private SongService songService;

    @PostMapping("/add")
    public Song addSong(@RequestBody Song song,HttpServletRequest request) {
        getAuthenticatedUser(request);
        return songService.save(song);
    }
    @GetMapping("/getAll")
    public List<Song> getAllSongs(HttpServletRequest request) {
        getAuthenticatedUser(request);
        return songService.getAll();
    }

    @GetMapping("/{id}")
    public Optional<Song> getSongById(@PathVariable String id, HttpServletRequest request) {
        getAuthenticatedUser(request);
        return songService.getById(id);
    }

    @GetMapping("/search")
    public List<Song> searchSongs(@RequestParam String title, HttpServletRequest request) {
        getAuthenticatedUser(request);
        return songService.search(title);
    }

    @DeleteMapping("/{id}")
    public void deleteSong(@PathVariable String id, HttpServletRequest request) {
        getAuthenticatedUser(request);
        songService.delete(id);
    }
}
