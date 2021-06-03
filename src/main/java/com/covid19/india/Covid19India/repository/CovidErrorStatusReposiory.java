package com.covid19.india.Covid19India.repository;

import com.covid19.india.Covid19India.model.ErrorStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CovidErrorStatusReposiory extends JpaRepository<ErrorStatus, Integer> {
}
