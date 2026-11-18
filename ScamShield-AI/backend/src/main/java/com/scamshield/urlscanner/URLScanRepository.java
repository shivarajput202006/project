package com.scamshield.urlscanner;

import java.util.List;
import java.util.Optional;

import org.springframework.data.mongodb.repository.MongoRepository;

public interface URLScanRepository extends MongoRepository<URLScan, String> {
    List<URLScan> findTop25ByUserIdOrderByScanTimestampDesc(String userId);

    List<URLScan> findTop5ByUserIdOrderByScanTimestampDesc(String userId);

    Optional<URLScan> findTop1ByUserIdOrderByScanTimestampDesc(String userId);

    List<URLScan> findByUserIdOrderByScanTimestampDesc(String userId);

    long countByUserId(String userId);

    Optional<URLScan> findByIdAndUserId(String id, String userId);

    long deleteByIdAndUserId(String id, String userId);

    long deleteByUserId(String userId);
}
