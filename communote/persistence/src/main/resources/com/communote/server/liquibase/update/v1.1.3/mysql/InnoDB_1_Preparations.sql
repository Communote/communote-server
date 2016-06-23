DELETE FROM core_notes2tag  WHERE not (tags_fk in (SELECT id FROM core_tag));
DELETE FROM core_blog2tag WHERE not (tags_fk in (SELECT id FROM core_tag));

ALTER TABLE configuration_setting       CHANGE `KEY` `KEY` VARCHAR(255);
ALTER TABLE security_code               CHANGE `code` `code` VARCHAR(255);
ALTER TABLE core_blog                   CHANGE `NAME_IDENTIFIER` `NAME_IDENTIFIER` VARCHAR(255);
ALTER TABLE core_global_id              CHANGE `GLOBAL_IDENTIFIER` `GLOBAL_IDENTIFIER` VARCHAR(255);
ALTER TABLE core_processed_utp_mail     CHANGE `MAIL_MESSAGE_ID` `MAIL_MESSAGE_ID` VARCHAR(255);
ALTER TABLE core_tag                    CHANGE `NAME` `NAME` VARCHAR(255);
ALTER TABLE custom_messages             CHANGE `KEY` `KEY` VARCHAR(255);
ALTER TABLE iprange_channel             CHANGE `TYPE` `TYPE` VARCHAR(255);
ALTER TABLE iprange_filter_channel      CHANGE `CHANNELS_FK` `CHANNELS_FK` VARCHAR(255);
ALTER TABLE md_country                  CHANGE `COUNTRY_CODE` `COUNTRY_CODE` VARCHAR(255);
ALTER TABLE md_language                 CHANGE `LANGUAGE_CODE` `LANGUAGE_CODE` VARCHAR(255);
ALTER TABLE user_client                 CHANGE `CLIENT_ID` `CLIENT_ID` VARCHAR(255);
ALTER TABLE user_user                   CHANGE `EMAIL` `EMAIL` VARCHAR(255);
ALTER TABLE user_user                   CHANGE `ALIAS` `ALIAS` VARCHAR(255);