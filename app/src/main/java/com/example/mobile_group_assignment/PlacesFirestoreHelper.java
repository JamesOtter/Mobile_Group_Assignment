package com.example.mobile_group_assignment;

import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

public class PlacesFirestoreHelper {
    private final FirebaseFirestore db;
    private final CollectionReference placesCollection;
    private final FirebaseAuth auth;

    public PlacesFirestoreHelper() {
        db = FirebaseFirestore.getInstance();
        placesCollection = db.collection("places");
        auth = FirebaseAuth.getInstance();
    }

    // Method to get all existing places
    public Query getAllPlaces() {
        return placesCollection.orderBy("name", Query.Direction.ASCENDING);
    }

    public FirestoreRecyclerOptions<Place> getAllPlacesOptions() {
        Query query = getAllPlaces();
        return new FirestoreRecyclerOptions.Builder<Place>()
                .setQuery(query, snapshot -> {
                    Place place = snapshot.toObject(Place.class);
                    place.setDocumentId(snapshot.getId());
                    return place;
                })
                .build();
    }

    // Method to get all places created by current user
    public Query getPlacesByUser() {
        FirebaseUser user = auth.getCurrentUser();
        if (user != null) {
            return placesCollection.whereEqualTo("createdBy", user.getUid());
        }
        return null;
    }

    // Method to add a new place
    public void addPlace(Place place, final FirestoreCallback callback) {
        placesCollection.add(place)
                .addOnSuccessListener(documentReference -> {
                    callback.onSuccess();
                })
                .addOnFailureListener(callback::onFailure);
    }

    // Method to get place data by id
    public void getPlaceById(String placeId, PlaceCallback callback) {
        placesCollection.document(placeId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        Place place = documentSnapshot.toObject(Place.class);
                        if (place != null) {
                            place.setDocumentId(documentSnapshot.getId());
                            callback.onSuccess(place);
                        } else {
                            callback.onFailure(new Exception("Failed to parse place data"));
                        }
                    } else {
                        callback.onFailure(new Exception("Place not found"));
                    }
                })
                .addOnFailureListener(callback::onFailure);
    }

    // Method to update a place
    public void updatePlace(Place place, final FirestoreCallback callback) {
        placesCollection.document(place.getDocumentId())
                .set(place)
                .addOnSuccessListener(unused -> callback.onSuccess())
                .addOnFailureListener(callback::onFailure);
    }

    // Method to delete a place
    public void deletePlace(String placeId, String imageUrl, final FirestoreCallback callback) {
        placesCollection.document(placeId)
                .delete()
                .addOnSuccessListener(unused -> {
                    // If successful and has image, delete from Storage
                    if (imageUrl != null && !imageUrl.isEmpty()) {
                        deleteImageFromStorage(imageUrl, callback);
                    } else {
                        callback.onSuccess();
                    }
                })
                .addOnFailureListener(callback::onFailure);
    }

    private void deleteImageFromStorage(String imageUrl, FirestoreCallback callback) {
        StorageReference photoRef = FirebaseStorage.getInstance().getReferenceFromUrl(imageUrl);
        photoRef.delete()
                .addOnSuccessListener(unused -> callback.onSuccess())
                .addOnFailureListener(callback::onFailure);
    }

    public FirestoreRecyclerOptions<Place> getPlacesOptions() {
        Query query = getPlacesByUser();
        if (query == null) return null;

        return new FirestoreRecyclerOptions.Builder<Place>()
                .setQuery(query, snapshot -> {
                    Place place = snapshot.toObject(Place.class);
                    place.setDocumentId(snapshot.getId()); // Set document ID
                    return place;
                })
                .build();
    }

    // Interface for callback
    public interface FirestoreCallback {
        void onSuccess();
        void onFailure(Exception e);
    }

    public interface PlaceCallback {
        void onSuccess(Place place);
        void onFailure(Exception e);
    }
}
