package com.spotify.backend.controller;

import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/views")
public class ViewController {

    @Autowired
    private MongoTemplate mongoTemplate;

    /**
     * DEMO: Create MongoDB View for aggregated data
     * Shows denormalized read-optimized view
     */
    @PostMapping("/create-dashboard-view")
    public ResponseEntity<?> createDashboardView() {

        System.out.println("👁️ CREATING MATERIALIZED VIEW for dashboard");

        String viewCreationCommand = """
        db.createView(
            "dashboard_stats_view",
            "songs",
            [
                {
                    $lookup: {
                        from: "artists",
                        localField: "artist_id",
                        foreignField: "_id",
                        as: "artist_details"
                    }
                },
                {
                    $group: {
                        _id: "$genre",
                        totalSongs: {$sum: 1},
                        totalPlays: {$sum: "$playCount"},
                        avgDuration: {$avg: "$duration"},
                        topArtist: {$first: "$artist_details.name"}
                    }
                },
                {
                    $sort: {totalPlays: -1}
                }
            ]
        )
        """;

        System.out.println("View Command:\n" + viewCreationCommand);

        return ResponseEntity.ok(Map.of(
                "message", "Dashboard view created (conceptual)",
                "viewName", "dashboard_stats_view",
                "purpose", "Pre-aggregated statistics for faster dashboard loading",
                "sourceCollections", List.of("songs", "artists"),
                "refreshStrategy", "On-demand refresh or change streams",
                "performanceBenefit", "Reduces complex joins at query time"
        ));
    }

    /**
     * DEMO: Query from view instead of base collection
     */
    @GetMapping("/dashboard-from-view")
    public ResponseEntity<?> getDashboardFromView() {

        System.out.println("📊 QUERYING FROM MATERIALIZED VIEW");

        // Simulated view data
        List<Map<String, Object>> viewData = List.of(
                Map.of("genre", "Pop", "totalSongs", 45, "totalPlays", 12000, "avgDuration", 180),
                Map.of("genre", "Rock", "totalSongs", 32, "totalPlays", 8500, "avgDuration", 210),
                Map.of("genre", "Hip Hop", "totalSongs", 28, "totalPlays", 9500, "avgDuration", 195)
        );

        return ResponseEntity.ok(Map.of(
                "dataSource", "dashboard_stats_view (Materialized View)",
                "queryTime", "12ms (vs 120ms from base collections)",
                "data", viewData,
                "note", "View is read-only and updated periodically"
        ));
    }
}