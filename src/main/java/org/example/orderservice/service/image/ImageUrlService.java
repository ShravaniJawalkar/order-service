package org.example.orderservice.service.image;

import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.concurrent.CompletableFuture;

@Service
public class ImageUrlService {

    @Async
    public CompletableFuture<String> getImageUrl(String id) {
        // Simulating a delay to mimic a long-running operation
        return new CompletableFuture<String>().completeAsync(() -> "https://example.com/image/" + id);

        // For demonstration, returning a placeholder URL

    }

    public String getImageDescription(String id) {
        return "Description for image with ID: " + id;
    }

    @Transactional
    public String getImageType(String id) {
        // Logic to retrieve the image type by its ID
        // For demonstration, returning a placeholder string
        System.out.println(Thread.currentThread().getName());
        return "Type of image with ID: " + id;
    }
}
