package com.scamshield.profile.service;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.UUID;

import org.bson.Document;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.gridfs.GridFsTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.mongodb.client.gridfs.model.GridFSFile;
import com.scamshield.profile.dto.ProfilePhotoResponse;
import com.scamshield.profile.model.UserProfile;
import com.scamshield.profile.repository.UserProfileRepository;
import com.scamshield.profile.util.ProfileException;
import com.scamshield.profile.util.ProfileValidationUtil;

@Service
public class ProfilePhotoService {
    private final GridFsTemplate gridFsTemplate;
    private final UserProfileRepository profileRepository;
    private final ProfileService profileService;

    public ProfilePhotoService(
            GridFsTemplate gridFsTemplate,
            UserProfileRepository profileRepository,
            ProfileService profileService) {
        this.gridFsTemplate = gridFsTemplate;
        this.profileRepository = profileRepository;
        this.profileService = profileService;
    }

    public ProfilePhotoResponse upload(String userId, MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw ProfileException.badRequest("Choose a non-empty JPEG, PNG, or WEBP image.");
        }
        byte[] content;
        try {
            content = file.getBytes();
        } catch (IOException exception) {
            throw ProfileException.badRequest("The uploaded profile photo could not be read.");
        }
        String contentType = ProfileValidationUtil.validatePhoto(content, file.getContentType());
        UserProfile profile = profileService.getOrCreateProfile(userId);
        String oldStorageId = profile.getPhotoStorageId();
        String extension = switch (contentType) {
            case "image/jpeg" -> "jpg";
            case "image/png" -> "png";
            case "image/webp" -> "webp";
            default -> throw ProfileException.badRequest("Unsupported profile photo format.");
        };
        ObjectId newStorageId = gridFsTemplate.store(
                new ByteArrayInputStream(content),
                UUID.randomUUID() + "." + extension,
                contentType,
                new Document("ownerId", userId));
        try {
            profile.updatePhoto(newStorageId.toHexString(), contentType);
            profileRepository.save(profile);
        } catch (RuntimeException exception) {
            gridFsTemplate.delete(Query.query(Criteria.where("_id").is(newStorageId)));
            throw exception;
        }
        deleteOwnedPhoto(oldStorageId, userId);
        return new ProfilePhotoResponse("/api/profile/photo", contentType);
    }

    public PhotoContent get(String userId) {
        UserProfile profile = profileService.getOrCreateProfile(userId);
        if (profile.getPhotoStorageId() == null) {
            throw ProfileException.notFound("No profile photo has been uploaded.");
        }
        ObjectId storageId = parseStorageId(profile.getPhotoStorageId());
        GridFSFile storedFile = gridFsTemplate.findOne(Query.query(
                Criteria.where("_id").is(storageId).and("metadata.ownerId").is(userId)));
        if (storedFile == null) {
            throw ProfileException.notFound("No profile photo has been uploaded.");
        }
        try (var input = gridFsTemplate.getResource(storedFile).getInputStream()) {
            byte[] content = input.readAllBytes();
            String contentType = ProfileValidationUtil.validatePhoto(content, profile.getPhotoContentType());
            return new PhotoContent(content, contentType);
        } catch (IOException exception) {
            throw new IllegalStateException("The private profile photo could not be read.", exception);
        }
    }

    public void delete(String userId) {
        UserProfile profile = profileService.getOrCreateProfile(userId);
        String oldStorageId = profile.getPhotoStorageId();
        if (oldStorageId == null) return;
        profile.updatePhoto(null, null);
        profileRepository.save(profile);
        deleteOwnedPhoto(oldStorageId, userId);
    }

    private void deleteOwnedPhoto(String id, String userId) {
        if (id == null) return;
        ObjectId storageId = parseStorageId(id);
        gridFsTemplate.delete(Query.query(
                Criteria.where("_id").is(storageId).and("metadata.ownerId").is(userId)));
    }

    private ObjectId parseStorageId(String id) {
        try {
            return new ObjectId(id);
        } catch (IllegalArgumentException exception) {
            throw new IllegalStateException("Profile photo storage reference is invalid.", exception);
        }
    }

    public record PhotoContent(byte[] bytes, String contentType) {
    }
}
