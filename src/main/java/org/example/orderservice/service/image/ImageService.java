package org.example.orderservice.service.image;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.util.concurrent.CompletableFuture;

@Service
public class ImageService {

    @Autowired
    private ImageUrlService imageUrlService;

    // This service can be used to handle image-related operations
    // such as uploading, processing, or retrieving images for orders.
    @Async
    public CompletableFuture<String> uploadImage(byte[] imageData) throws InterruptedException {
        // Logic to upload image data
        //return new CompletableFuture<String>("Image uploaded successfully") ;
        CompletableFuture<String> future = new CompletableFuture<>();
        return future.toCompletableFuture().whenComplete((result, error) -> {
            if (error != null) {
                future.completeExceptionally(error);
            } else {
                // Simulating a long-running operation
                try {
                    Thread.sleep(Duration.ofSeconds(5).toMillis());
                    future.complete("Image uploaded successfully");
                } catch (InterruptedException e) {
                    future.completeExceptionally(e);
                }
            }
        });

    }

    // here  we are adding @Transactional annotation to calling method
// and @Async to caller method so caller method will run in new thread so if any rollback happens in caller method
// calling will not get rolled back not recommended approach
    @Transactional
    public String getImage(String id) {
        // Logic to retrieve an image by its ID
        // For demonstration, returning a placeholder string
        System.out.println(Thread.currentThread().getName());
        CompletableFuture<String> stringCompletableFuture = imageUrlService.getImageUrl(id);
        try {
            return stringCompletableFuture.get();
        } catch (Exception e) {
            throw new RuntimeException("Error retrieving image URL", e);
        }
    }

    // here we are adding both @Transactional and @Async annotations
    // but if the parent method is annotated with @Transactional then the Propagation that parent method has defined  will not supported by child method
    //need to use this with precaution
    @Transactional
    @Async
    public CompletableFuture<String> getImageDescription(String id) {
        // Logic to retrieve an image by its ID asynchronously
        // For demonstration, returning a placeholder string
        System.out.println(Thread.currentThread().getName());
        return new CompletableFuture<String>().completeAsync(() -> imageUrlService.getImageDescription(id));
    }

    // this method will execute in a new thread
    // and caller method inside this method will be a part of this thread and will be treated as a new transaction
    //recommended approach
    @Async
    public CompletableFuture<String> getImageType(String id) {
        // Logic to retrieve an image type by its ID asynchronously
        // For demonstration, returning a placeholder string
        System.out.println(Thread.currentThread().getName());
        return new CompletableFuture<String>().completeAsync(() -> imageUrlService.getImageType(id));
    }

    // here we are handling exception using try-catch block
    @Async
    public void createImageThumbnail(String imageId, byte[] imageData) {
        // Logic to create a thumbnail for the image
        // Simulating a delay to mimic a long-running operation
        try {
            int imageSize = imageData.length/0;
            Thread.sleep(Duration.ofSeconds(2).toMillis());
            System.out.println("Thumbnail created for image ID: " + imageId);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Error creating thumbnail", e);
        }
    }

    // here execption will be handled by SimpleAsyncUncaughtExceptionHandler
    // this handler is provided by Spring Boot
    @Async
    public void deleteImageThumbnail(String imageId) {
        // Logic to delete a thumbnail for the image
        Thread.currentThread().setName(Thread.currentThread().getName() + "-deleteImageThumbnail");
        System.out.println("Thumbnail deleted for image ID: " + imageId + " in thread: " + Thread.currentThread().getName());

    }
    //for this method we will create our own custom exception handler
    @Async
    public void setImageThumbnail(String imageId, String thumbnailId) {
        Thread.currentThread().setName(Thread.currentThread().getName() + "-setImageThumbnail");
        System.out.println("Thumbnail set for image ID: " + imageId + " in thread: " + Thread.currentThread().getName());
    }
}
