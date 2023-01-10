ALTER TABLE stonad
    DROP CONSTRAINT stonad_stonad_type_skyldner_id_kravhaver_id_key ;

ALTER TABLE stonad
    ADD UNIQUE (type, skyldner_id, kravhaver_id, sak_id) ;