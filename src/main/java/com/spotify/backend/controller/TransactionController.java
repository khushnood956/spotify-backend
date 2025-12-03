package com.spotify.backend.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/transactions")
public class TransactionController {

    /**
     * DEMO: Atomic playlist creation with multiple songs
     * Shows ACID properties in MongoDB 4.0+
     */
    @PostMapping("/playlist/batch-create")
    @Transactional  // Spring Data MongoDB transactions (requires replica set)
    public ResponseEntity<?> createPlaylistWithSongs(@RequestBody Map<String, Object> request) {

        System.out.println("🚀 TRANSACTION STARTED: Multi-document atomic operation");

        // Simulate transaction steps:
        // 1. Create playlist document
        // 2. Add songs to playlist_songs collection
        // 3. Update user's playlist count
        // 4. Log the activity

        try {
            // This would be a real transaction in production
            // For demo: Show how it maintains consistency

            System.out.println("✅ TRANSACTION COMMITTED: All operations successful");

            return ResponseEntity.ok(Map.of(
                    "message", "Transaction completed atomically",
                    "transactionId", "txn_12345",
                    "rollbackPossible", true,
                    "isolationLevel", "READ_COMMITTED"
            ));

        } catch (Exception e) {
            System.out.println("❌ TRANSACTION ROLLED BACK: " + e.getMessage());
            throw e; // Spring will roll back automatically
        }
    }
    /**
     * DEMO: Index usage in transactions
     */
    @GetMapping("/transaction-with-index")
    public ResponseEntity<?> transactionWithIndex() {

        return ResponseEntity.ok(Map.of(
                "scenario", "Transaction with indexed query",
                "steps", List.of(
                        "1. Start transaction",
                        "2. Query songs by genre (uses genre_plays_idx)",
                        "3. Update playCount on matching documents",
                        "4. Update artist total plays",
                        "5. Commit transaction"
                ),
                "indexImpact", "Without index: Table lock on songs collection",
                "withIndex", "Document-level locking, better concurrency",
                "isolation", "Indexes provide snapshot isolation points",
                "mongodbCommand", """
                db.songs.find(
                    {genre: "Pop"},
                    {maxTimeMS: 5000}
                ).hint("genre_plays_idx")
                """
        ));
    }
    /**
     * DEMO: Pessimistic locking for concurrent song plays
     */
    @PostMapping("/song/concurrent-play")
    public ResponseEntity<?> concurrentSongPlay(@RequestParam String songId) {

        System.out.println("🔒 ACQUIRING LOCK for song: " + songId);

        // Simulate locking mechanism
        // In MongoDB: db.songs.update({_id: songId}, {$inc: {playCount: 1}})
        // This is atomic at document level

        // Show concurrency handling
        try {
            Thread.sleep(100); // Simulate processing time

            System.out.println("✅ LOCK RELEASED: Play count incremented atomically");

            return ResponseEntity.ok(Map.of(
                    "message", "Concurrent play handled with atomic increment",
                    "concurrencyControl", "Document-level atomic operations",
                    "lockType", "Optimistic with version field (_v in MongoDB)"
            ));

        } catch (InterruptedException e) {
            return ResponseEntity.badRequest().body("Concurrency issue detected");
        }
    }
}