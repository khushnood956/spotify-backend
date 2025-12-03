package com.spotify.backend.controller;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Indexes;
import com.mongodb.client.model.IndexOptions;
import org.bson.Document;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping("/api/indexing")
public class IndexingController {

    @Autowired
    private MongoTemplate mongoTemplate;

    /**
     * DEMO: Create indexes using modern MongoDB driver
     */
    @PostMapping("/create-indexes")
    public ResponseEntity<?> createPerformanceIndexes() {

        System.out.println("📈 CREATING INDEXES with modern MongoDB driver");

        // Get collections
        MongoCollection<Document> songsCollection =
                mongoTemplate.getCollection("songs");
        MongoCollection<Document> usersCollection =
                mongoTemplate.getCollection("users");
        MongoCollection<Document> playlistsCollection =
                mongoTemplate.getCollection("playlists");

        // 1. Create compound index on songs (genre ASC, playCount DESC)
        IndexOptions compoundOptions = new IndexOptions()
                .name("genre_plays_idx")
                .background(true); // Build in background

        songsCollection.createIndex(
                Indexes.compoundIndex(
                        Indexes.ascending("genre"),
                        Indexes.descending("playCount")
                ),
                compoundOptions
        );

        // 2. Create TTL index for session data (auto-delete after 24 hours)
        IndexOptions ttlOptions = new IndexOptions()
                .name("session_ttl_idx")
                .expireAfter(24L, TimeUnit.HOURS);

        // Example for sessions collection (if you have one)
        // mongoTemplate.getCollection("sessions").createIndex(
        //     Indexes.ascending("createdAt"),
        //     ttlOptions
        // );

        // 3. Create unique index on user email
        IndexOptions uniqueOptions = new IndexOptions()
                .name("email_unique_idx")
                .unique(true);

        usersCollection.createIndex(
                Indexes.ascending("email"),
                uniqueOptions
        );

        // 4. Create text index for song search (full-text search)
        IndexOptions textOptions = new IndexOptions()
                .name("song_text_search_idx")
                .weights(new Document("title", 10).append("genre", 5));

        songsCollection.createIndex(
                Indexes.text("title"),
                textOptions
        );

        // 5. Create geospatial index (if you have location data)
        // IndexOptions geoOptions = new IndexOptions().name("location_2dsphere_idx");
        // usersCollection.createIndex(Indexes.geo2dsphere("location"), geoOptions);

        // 6. Create partial index (only for active users)
        IndexOptions partialOptions = new IndexOptions()
                .name("active_users_idx")
                .partialFilterExpression(new Document("isActive", true));

        usersCollection.createIndex(
                Indexes.ascending("username"),
                partialOptions
        );

        System.out.println("✅ Indexes created successfully");

        return ResponseEntity.ok(Map.of(
                "message", "Indexes created using modern MongoDB driver",
                "indexesCreated", List.of(
                        Map.of("name", "genre_plays_idx", "type", "COMPOUND", "collection", "songs"),
                        Map.of("name", "email_unique_idx", "type", "UNIQUE", "collection", "users"),
                        Map.of("name", "song_text_search_idx", "type", "TEXT", "collection", "songs"),
                        Map.of("name", "active_users_idx", "type", "PARTIAL", "collection", "users")
                ),
                "mongodbVersion", "6.0+",
                "driverVersion", "4.0+",
                "note", "Using createIndex() method (ensureIndex is deprecated)"
        ));
    }

    /**
     * DEMO: Show index statistics and usage
     */
    @GetMapping("/index-stats")
    public ResponseEntity<?> getIndexStatistics() {

        // Execute MongoDB command to get index stats
        Document indexStats = mongoTemplate.executeCommand(
                new Document("aggregate", "system.profile")
                        .append("pipeline", List.of(
                                new Document("$indexStats", new Document()),
                                new Document("$project", new Document()
                                        .append("name", 1)
                                        .append("accesses.ops", 1)
                                        .append("accesses.since", 1)
                                        .append("host", 0)
                                )
                        ))
                        .append("cursor", new Document())
        );

        // List all indexes in collections
        List<Document> songIndexes = mongoTemplate.getCollection("songs")
                .listIndexes().into(new java.util.ArrayList<>());

        return ResponseEntity.ok(Map.of(
                "message", "Index statistics and usage",
                "indexStats", indexStats,
                "songIndexes", songIndexes.stream()
                        .map(doc -> doc.getString("name"))
                        .toList(),
                "recommendations", List.of(
                        "Drop unused indexes to save storage",
                        "Create covering indexes for frequent queries",
                        "Monitor index size vs. collection size"
                )
        ));
    }

    /**
     * DEMO: Compare query performance with/without index
     */
    @GetMapping("/query-performance")
    public ResponseEntity<?> compareQueryPerformance() {

        System.out.println("⚡ Testing query performance...");

        // Simulate query with explain()
        Document explainCommand = new Document("explain",
                new Document("find", "songs")
                        .append("filter", new Document("genre", "Pop"))
                        .append("sort", new Document("playCount", -1))
                        .append("limit", 10)
        );

        Document queryPlan = mongoTemplate.executeCommand(explainCommand);

        return ResponseEntity.ok(Map.of(
                "query", "Find top 10 Pop songs by playCount",
                "executionStats", queryPlan.get("executionStats", Document.class),
                "winningPlan", queryPlan.get("queryPlanner", Document.class)
                        .get("winningPlan", Document.class),
                "indexesUsed", extractUsedIndexes(queryPlan),
                "performanceNote", "Compound index on (genre, playCount) makes this query efficient"
        ));
    }

    private List<String> extractUsedIndexes(Document explainResult) {
        // Extract index names from explain plan
        try {
            Document winningPlan = explainResult.get("queryPlanner", Document.class)
                    .get("winningPlan", Document.class);

            List<String> indexes = new java.util.ArrayList<>();
            extractIndexesFromPlan(winningPlan, indexes);
            return indexes;
        } catch (Exception e) {
            return List.of("No index used (COLLSCAN)");
        }
    }

    private void extractIndexesFromPlan(Document plan, List<String> indexes) {
        if (plan.containsKey("indexName")) {
            indexes.add(plan.getString("indexName"));
        }
        if (plan.containsKey("inputStage")) {
            extractIndexesFromPlan(plan.get("inputStage", Document.class), indexes);
        }
        if (plan.containsKey("$or")) {
            List<Document> orStages = plan.getList("$or", Document.class);
            for (Document stage : orStages) {
                extractIndexesFromPlan(stage, indexes);
            }
        }
    }

    /**
     * DEMO: Show how to drop unused indexes
     */
    @DeleteMapping("/drop-unused-index/{indexName}")
    public ResponseEntity<?> dropUnusedIndex(@PathVariable String indexName) {

        try {
            // Don't actually drop in demo, just show the command
            System.out.println("Would drop index: " + indexName);
            // mongoTemplate.getCollection("songs").dropIndex(indexName);

            return ResponseEntity.ok(Map.of(
                    "message", "Index drop simulated",
                    "command", "db.songs.dropIndex('" + indexName + "')",
                    "warning", "Cannot drop _id index",
                    "bestPractice", "Monitor index usage for 30 days before dropping"
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "error", "Cannot drop index",
                    "reason", e.getMessage()
            ));
        }
    }
}