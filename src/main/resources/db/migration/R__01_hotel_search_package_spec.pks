CREATE OR REPLACE PACKAGE hotel_search_pkg AS
    PROCEDURE persist_search_batch (
        p_searches_json IN CLOB
    );

    PROCEDURE find_search_count (
        p_search_id IN VARCHAR2,
        p_result OUT SYS_REFCURSOR
    );
END hotel_search_pkg;
/
