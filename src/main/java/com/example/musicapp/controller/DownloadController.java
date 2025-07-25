package com.example.musicapp.controller;


import com.example.musicapp.model.Song;
import com.example.musicapp.service.GoogleDriveService;
import com.example.musicapp.service.SongService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.firebase.auth.FirebaseToken;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import java.security.GeneralSecurityException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/download")
public class DownloadController extends AuthenticatedController {

    private static final String PYTHON_API_URL = "http://localhost:5001/download";

    @Autowired
    private GoogleDriveService driveService;

    @Autowired
    private SongService songService;

    /**
     * This endpoint requires authentication.
     * Make sure the SecurityConfig has not permitted "/download".
     * FirebaseTokenFilter must verify the token before this is accessed.
     */

    @PostMapping
    public ResponseEntity<String> downloadAndUpload(
            @RequestBody Map<String, String> payload,
            HttpServletRequest request) {

        FirebaseToken user = (FirebaseToken) request.getAttribute("firebaseUser");
        String uid = user.getUid();
        String videoUrl = payload.get("url");
        Optional<String> result = songService.checkOrLinkSong(videoUrl, uid);

        if (result.isPresent()) {
            if (result.get().equals("exists")) {
                return ResponseEntity.status(HttpStatus.CONFLICT)
                        .body("✅ Song already exists for this user.");
            } else {
                return ResponseEntity.ok("✅ Song already existed. Added to your library.");
            }
        }

        try {
            // Step 1: Call Python backend to download the audio
            RestTemplate restTemplate = new RestTemplate();
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            String idToken = request.getHeader("Authorization");
            if (idToken != null) {
                headers.set("Authorization", idToken);  // 🔁 Forward the token
            }
            HttpEntity<Map<String, String>> pythonRequest = new HttpEntity<>(payload, headers);

            ResponseEntity<String> pythonResponse = restTemplate.postForEntity(PYTHON_API_URL, pythonRequest, String.class);

            if (!pythonResponse.getStatusCode().is2xxSuccessful()) {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body("Python server error: " + pythonResponse.getBody());
            }

            // Step 2: Parse response from Python server
            ObjectMapper mapper = new ObjectMapper();
            JsonNode jsonNode = mapper.readTree(pythonResponse.getBody());

            String driveFileId = jsonNode.has("url") ? jsonNode.get("url").asText() : null;
            String thumbnail = jsonNode.has("thumbnail") ? jsonNode.get("thumbnail").asText() : null;
            String title = jsonNode.has("file") ? jsonNode.get("file").asText().trim() : null;

            if (title != null && title.contains(".")) {
                title = title.substring(0, title.lastIndexOf('.'));
            }

            if (driveFileId == null || thumbnail == null || title == null) {
                throw new GeneralSecurityException("Missing required fields");
            }

            String formattedDriveUrl = "https://drive.google.com/file/d/" + driveFileId;
            String format = "webm";

            Song song = new Song(title, videoUrl, formattedDriveUrl, thumbnail, format);
            song.setUploadedAt(LocalDateTime.now());
            List<String> userIds = new ArrayList<>();
            userIds.add(uid);
            song.setUserIds(userIds);
            songService.save(song);

            return ResponseEntity.ok("Song processed and uploaded successfully");

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error: " + e.getMessage());
        }
    }
}
