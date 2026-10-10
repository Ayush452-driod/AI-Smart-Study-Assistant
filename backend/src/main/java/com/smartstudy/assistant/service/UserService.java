package com.smartstudy.assistant.service;

import com.smartstudy.assistant.model.User;
import com.smartstudy.assistant.repository.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.*;
import java.util.Map;
import java.util.UUID;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final Path uploadDirectory;

    public UserService(
            UserRepository userRepository,
            @Value("${app.upload-dir}") String uploadDirectory) {

        this.userRepository = userRepository;
        this.uploadDirectory =
                Paths.get(uploadDirectory).toAbsolutePath().normalize();
    }

    public User getByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found."));
    }

    public Map<String, Object> getProfile(String email) {
        return safeUser(getByEmail(email));
    }

    public Map<String, Object> updateProfile(
            String email,
            String name) {

        User user = getByEmail(email);

        if (name != null && !name.trim().isBlank()) {
            user.setName(name.trim());
        }

        userRepository.save(user);

        return safeUser(user);
    }

    public Map<String, Object> uploadPicture(
            String email,
            MultipartFile file) throws Exception {

        if (file.isEmpty()) {
            throw new RuntimeException("Please choose an image.");
        }

        if (file.getSize() > 5 * 1024 * 1024) {
            throw new RuntimeException("Maximum image size is 5 MB.");
        }

        String type = file.getContentType();

        if (type == null || !type.startsWith("image/")) {
            throw new RuntimeException("Only image files are allowed.");
        }

        User user = getByEmail(email);

        Files.createDirectories(uploadDirectory);

        deleteOldPicture(user);

        String extension = getExtension(type);
        String filename = UUID.randomUUID() + extension;

        Files.write(
                uploadDirectory.resolve(filename),
                file.getBytes(),
                StandardOpenOption.CREATE_NEW
        );

        user.setProfilePicture("/uploads/profile/" + filename);

        userRepository.save(user);

        return safeUser(user);
    }

    public Map<String, Object> removePicture(String email)
            throws Exception {

        User user = getByEmail(email);

        deleteOldPicture(user);

        // Back to blank/default picture.
        user.setProfilePicture(null);

        userRepository.save(user);

        return safeUser(user);
    }

    private void deleteOldPicture(User user) throws Exception {

        if (user.getProfilePicture() != null) {

            String filename =
                    Paths.get(user.getProfilePicture())
                            .getFileName()
                            .toString();

            Files.deleteIfExists(
                    uploadDirectory.resolve(filename)
            );
        }
    }

    private String getExtension(String type) {

        return switch (type) {
            case "image/png" -> ".png";
            case "image/webp" -> ".webp";
            case "image/gif" -> ".gif";
            default -> ".jpg";
        };
    }

    private Map<String, Object> safeUser(User user) {

        return Map.of(
                "id", user.getId(),
                "name", user.getName(),
                "email", user.getEmail(),
                "profilePicture",
                user.getProfilePicture() == null
                        ? ""
                        : user.getProfilePicture()
        );
    }
}
