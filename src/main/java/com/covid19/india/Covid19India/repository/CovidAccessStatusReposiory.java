package com.covid19.india.Covid19India.repository;

import com.covid19.india.Covid19India.model.TrackUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CovidAccessStatusReposiory extends JpaRepository<TrackUser, Integer> {
}
