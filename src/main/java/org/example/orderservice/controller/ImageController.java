package org.example.orderservice.controller;

import org.example.orderservice.service.image.ImageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

@RestController
@RequestMapping("/api/images")
public class ImageController {

    @Autowired
    private ImageService imageService;

    @PostMapping
    public String uploadImage(@RequestBody byte[] image) {
        // Logic to handle image upload
        String result = "";
        try {

            result = imageService.uploadImage(image).get();
            // Simulating a long-running operation
        } catch (ExecutionException | InterruptedException e) {
            throw new RuntimeException(e);
        }

        return result;
    }

    @GetMapping("/{id}")
    public String getImage(@PathVariable String id) {
        // Logic to retrieve an image
        // For demonstration, returning a placeholder string
        return imageService.getImage(id);
    }

    @GetMapping("/{id}/description")
    public String getImageDescription(@PathVariable String id) {
        // Logic to retrieve an image description
        // For demonstration, returning a placeholder string
       CompletableFuture<String> completableFuture=imageService.getImageDescription(id);
        try {
            return completableFuture.get();
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException("Error retrieving image description", e);
        }
    }

    @GetMapping("/{id}/type")
    public String getImageType(@PathVariable String id) {
        // Logic to retrieve an image type
        // For demonstration, returning a placeholder string
        CompletableFuture<String> completableFuture= imageService.getImageType(id);
        try {
            return completableFuture.get();
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException("Error retrieving image type", e);
        }
    }

    @PostMapping("{id}/thumbnail")
    public void createThumbnail(@PathVariable String id, @RequestBody byte[] image) {
        // Logic to create a thumbnail for the image
        // For demonstration, returning a placeholder string
        try {
            imageService.createImageThumbnail(id,image);
        } catch (Exception e) {
            throw new RuntimeException("Error creating thumbnail", e);
        }
    }

    @DeleteMapping("/{id}/thumbnail")
    public void deleteThumbnail(@PathVariable String id) {
        // Logic to delete a thumbnail for the image
        // For demonstration, returning a placeholder string
        try {
            imageService.deleteImageThumbnail(id);
        } catch (Exception e) {
            throw new RuntimeException("Error deleting thumbnail", e);
        }
    }

    @PutMapping("{id}/thumbnail/{thumbnailId}")
    public void updateImageThumbnail(@PathVariable String id, @PathVariable String thumbnailId) {
        // Logic to update a thumbnail for the image
        // For demonstration, returning a placeholder string
        try {
            imageService.setImageThumbnail(id, thumbnailId);
        } catch (Exception e) {
            throw new RuntimeException("Error updating thumbnail", e);
        }
    }

}
