package com.example.Capstone.repository;

import java.util.List;

public interface RestaurantRankingRepository {

    List<RestaurantRankingRow> findRestaurantRankings(
            String regionName,
            String category,
            int limit,
            int smoothingConstant
    );
}
