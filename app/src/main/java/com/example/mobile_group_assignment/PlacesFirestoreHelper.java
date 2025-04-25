package com.example.mobile_group_assignment;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

public class PlacesFirestoreHelper {
    private final FirebaseFirestore db;
    private final CollectionReference placesCollection;
    private final FirebaseAuth auth;

    public PlacesFirestoreHelper() {
        db = FirebaseFirestore.getInstance();
        placesCollection = db.collection("places");
        auth = FirebaseAuth.getInstance();
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
                    place.setId(documentReference.getId());
                    callback.onSuccess();
                })
                .addOnFailureListener(callback::onFailure);
    }

    // Method to update a place
    public void updatePlace(Place place, final FirestoreCallback callback) {
        placesCollection.document(place.getId())
                .set(place)
                .addOnSuccessListener(unused -> callback.onSuccess())
                .addOnFailureListener(callback::onFailure);
    }

    // Method to delete a place
    public void deletePlace(String placeId, final FirestoreCallback callback) {
        placesCollection.document(placeId)
                .delete()
                .addOnSuccessListener(unused -> callback.onSuccess())
                .addOnFailureListener(callback::onFailure);
    }

    // Interface for callback
    public interface FirestoreCallback {
        void onSuccess();
        void onFailure(Exception e);
    }
}
