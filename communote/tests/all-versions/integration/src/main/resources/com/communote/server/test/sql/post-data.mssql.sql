-- This SQL file will be applied after data.mssql.sql  
UPDATE configuration_app_setting SET setting_value = '@@UNIQUE_ID@@'                        WHERE SETTING_KEY = 'installation.unique.id';
UPDATE configuration_app_setting SET setting_value = '@@INSTALLATION_DATE@@'                WHERE SETTING_KEY = 'installation.date';
UPDATE configuration_app_setting SET setting_value = '@@INSTALLATION_DATE_UNENCRYPTED@@'    WHERE SETTING_KEY = 'installation.date.unencrypted';
UPDATE configuration_setting SET setting_value = '@@UNIQUE_ID@@'            WHERE SETTING_KEY = 'kenmei.unique.client.identifer';
UPDATE configuration_setting SET setting_value = '@@INSTALLATION_DATE@@'    WHERE SETTING_KEY = 'client.creation.date';
DELETE FROM configuration_app_setting WHERE SETTING_KEY = 'com.communote.core.keystore.password';
INSERT INTO configuration_app_setting (SETTING_KEY, setting_value) VALUES ('com.communote.core.keystore.password','changeit');
