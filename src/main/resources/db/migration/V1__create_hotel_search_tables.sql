CREATE TABLE hotel_searches (
    search_id VARCHAR2(36) NOT NULL,
    hotel_id VARCHAR2(100) NOT NULL,
    check_in DATE NOT NULL,
    check_out DATE NOT NULL,
    ages_hash VARCHAR2(64) NOT NULL,
    CONSTRAINT pk_hotel_searches PRIMARY KEY (search_id),
    CONSTRAINT chk_search_dates CHECK (check_in < check_out)
);

CREATE TABLE hotel_search_ages (
    search_id VARCHAR2(36) NOT NULL,
    age_order NUMBER(10) NOT NULL,
    age NUMBER(10) NOT NULL,
    CONSTRAINT pk_hotel_search_ages PRIMARY KEY (search_id, age_order),
    CONSTRAINT fk_search_ages_search FOREIGN KEY (search_id)
        REFERENCES hotel_searches (search_id) ON DELETE CASCADE,
    CONSTRAINT chk_search_age CHECK (age >= 0)
);

CREATE INDEX idx_hotel_search_count
    ON hotel_searches (hotel_id, check_in, check_out, ages_hash);
