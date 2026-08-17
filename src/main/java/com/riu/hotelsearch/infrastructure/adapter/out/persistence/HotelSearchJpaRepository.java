package com.riu.hotelsearch.infrastructure.adapter.out.persistence;

import com.riu.hotelsearch.infrastructure.adapter.out.persistence.entity.HotelSearchEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;

interface HotelSearchJpaRepository extends JpaRepository<HotelSearchEntity, String> {

    @Query("""
            select count(search)
            from HotelSearchEntity search
            where search.hotelId = :hotelId
              and search.checkIn = :checkIn
              and search.checkOut = :checkOut
              and search.agesHash = :agesHash
            """)
    long countMatching(
            @Param("hotelId") String hotelId,
            @Param("checkIn") LocalDate checkIn,
            @Param("checkOut") LocalDate checkOut,
            @Param("agesHash") String agesHash);
}
