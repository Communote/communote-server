-- This SQL file will be applied after data.mysql.sql  
UPDATE configuration_app_setting SET setting_value = '@@UNIQUE_ID@@'                        WHERE setting_key = 'installation.unique.id';
UPDATE configuration_app_setting SET setting_value = '@@INSTALLATION_DATE@@'                WHERE setting_key = 'installation.date';
UPDATE configuration_app_setting SET setting_value = '@@INSTALLATION_DATE_UNENCRYPTED@@'    WHERE setting_key = 'installation.date.unencrypted';
UPDATE configuration_setting SET setting_value = '@@UNIQUE_ID@@'            WHERE setting_key = 'kenmei.unique.client.identifer';
UPDATE configuration_setting SET setting_value = '@@INSTALLATION_DATE@@'    WHERE setting_key = 'client.creation.date';
DELETE FROM configuration_app_setting WHERE setting_key = 'com.communote.core.keystore.password';
INSERT INTO configuration_app_setting (setting_key, setting_value) VALUES ('com.communote.core.keystore.password','changeit');