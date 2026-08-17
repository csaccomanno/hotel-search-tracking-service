package com.riu.hotelsearch.infrastructure.adapter.out.persistence.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Immutable;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "hotel_searches")
@Immutable
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class HotelSearchEntity {

    @Id
    @Column(name = "search_id", length = 36, nullable = false, updatable = false)
    private String searchId;

    @Column(name = "hotel_id", length = 100, nullable = false)
    private String hotelId;

    @Column(name = "check_in", nullable = false)
    private LocalDate checkIn;

    @Column(name = "check_out", nullable = false)
    private LocalDate checkOut;

    @Column(name = "ages_hash", length = 64, nullable = false)
    @Getter(AccessLevel.NONE)
    private String agesHash;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "hotel_search_ages", joinColumns = @JoinColumn(name = "search_id"))
    @OrderColumn(name = "age_order")
    @Column(name = "age", nullable = false)
    private List<Integer> ages = new ArrayList<>();

    public HotelSearchEntity(
            String searchId,
            String hotelId,
            LocalDate checkIn,
            LocalDate checkOut,
            String agesHash,
            List<Integer> ages) {
        this.searchId = searchId;
        this.hotelId = hotelId;
        this.checkIn = checkIn;
        this.checkOut = checkOut;
        this.agesHash = agesHash;
        this.ages = new ArrayList<>(ages);
    }

}
