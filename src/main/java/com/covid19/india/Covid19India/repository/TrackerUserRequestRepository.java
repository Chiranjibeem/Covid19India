package com.covid19.india.Covid19India.repository;

import com.covid19.india.Covid19India.model.TrackUserRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TrackerUserRequestRepository extends JpaRepository<TrackUserRequest, Integer> {
}
