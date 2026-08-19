package com.example.cropdryer.repository;

import com.example.cropdryer.entity.Reading;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface ReadingRepository extends JpaRepository<Reading, Long> {
    Optional<Reading> findTopByOrderByCreatedAtDesc();
    Optional<Reading> findTopByDeviceIdOrderByCreatedAtDesc(String deviceId);

    Page<Reading> findAllByOrderByCreatedAtDesc(Pageable pageable);
    Page<Reading> findByDeviceIdOrderByCreatedAtDesc(String deviceId, Pageable pageable);

    List<Reading> findByAlarmInOrderByCreatedAtDesc(Collection<String> alarms);
    List<Reading> findByAlarmInAndDeviceIdOrderByCreatedAtDesc(Collection<String> alarms, String deviceId);
}
