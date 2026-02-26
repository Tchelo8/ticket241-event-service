package com.tchelo.event_service_ticket241.repository;

import com.tchelo.event_service_ticket241.entity.City;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CityRepository extends JpaRepository<City, Long> {


    Optional<City> findByUuid(String uuid);


    Optional<City> findByNameIgnoreCase(String name);


    Optional<City> findByNameIgnoreCaseAndCountryIgnoreCase(String name, String country);


    List<City> findByIsActiveTrueOrderByNameAsc();

    List<City> findByCountryIgnoreCaseAndIsActiveTrueOrderByNameAsc(String country);


    @Query("SELECT c FROM City c WHERE " +
            "(LOWER(c.name) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
            "OR LOWER(c.country) LIKE LOWER(CONCAT('%', :keyword, '%'))) " +
            "AND c.isActive = true")
    List<City> searchCities(@Param("keyword") String keyword);

    boolean existsByNameIgnoreCaseAndCountryIgnoreCase(String name, String country);
}
