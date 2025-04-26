package com.example.mobile_group_assignment;

import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import java.util.UUID;
public class ImageUploadHelper {
    private final StorageReference storageRef;

    public ImageUploadHelper() {
        this.storageRef = FirebaseStorage.getInstance().getReference();
    }

    public UploadTask uploadPlaceImage(byte[] imageData) {
        // Generate unique filename
        String filename = "places/" + UUID.randomUUID() + ".jpg";
        StorageReference imageRef = storageRef.child(filename);

        return imageRef.putBytes(imageData);
    }
}
