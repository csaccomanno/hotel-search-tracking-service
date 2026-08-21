CREATE OR REPLACE PACKAGE BODY hotel_search_pkg AS
    PROCEDURE persist_search_batch (
        p_searches_json IN CLOB
    ) AS
    BEGIN
        INSERT /*+ IGNORE_ROW_ON_DUPKEY_INDEX(hotel_searches (search_id)) */
          INTO hotel_searches (
              search_id,
              hotel_id,
              check_in,
              check_out,
              ages_hash
          )
        SELECT search_id,
               hotel_id,
               TO_DATE(check_in, 'YYYY-MM-DD'),
               TO_DATE(check_out, 'YYYY-MM-DD'),
               ages_hash
          FROM JSON_TABLE(
                   p_searches_json,
                   '$[*]' ERROR ON ERROR
                   COLUMNS (
                       search_id VARCHAR2(36) PATH '$.searchId',
                       hotel_id VARCHAR2(100) PATH '$.hotelId',
                       check_in VARCHAR2(10) PATH '$.checkIn',
                       check_out VARCHAR2(10) PATH '$.checkOut',
                       ages_hash VARCHAR2(64) PATH '$.agesHash'
                   ));

        INSERT /*+ IGNORE_ROW_ON_DUPKEY_INDEX(hotel_search_ages (search_id, age_order)) */
          INTO hotel_search_ages (
              search_id,
              age_order,
              age
          )
        SELECT search_id,
               age_order - 1,
               age
          FROM JSON_TABLE(
                   p_searches_json,
                   '$[*]' ERROR ON ERROR
                   COLUMNS (
                       search_id VARCHAR2(36) PATH '$.searchId',
                       NESTED PATH '$.ages[*]'
                       COLUMNS (
                           age_order FOR ORDINALITY,
                           age NUMBER PATH '$'
                       )
                   ));
    END persist_search_batch;

    PROCEDURE find_search_count (
        p_search_id IN VARCHAR2,
        p_result OUT SYS_REFCURSOR
    ) AS
    BEGIN
        OPEN p_result FOR
            SELECT searched.search_id,
                   searched.hotel_id,
                   searched.check_in,
                   searched.check_out,
                   (
                       SELECT JSON_ARRAYAGG(
                                  age.age ORDER BY age.age_order RETURNING CLOB
                              )
                         FROM hotel_search_ages age
                        WHERE age.search_id = searched.search_id
                   ) AS ages_json,
                   (
                       SELECT COUNT(*)
                         FROM hotel_searches matching
                        WHERE matching.hotel_id = searched.hotel_id
                          AND matching.check_in = searched.check_in
                          AND matching.check_out = searched.check_out
                          AND matching.ages_hash = searched.ages_hash
                   ) AS matching_count
              FROM hotel_searches searched
             WHERE searched.search_id = p_search_id;
    END find_search_count;
END hotel_search_pkg;
/
