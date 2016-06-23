/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `channel_configuration` (
  `ID` bigint(20) NOT NULL AUTO_INCREMENT,
  `FORCE_SSL` tinyint(4) NOT NULL,
  `CHANNEL_TYPE` varchar(1024) COLLATE utf8_unicode_ci NOT NULL,
  PRIMARY KEY (`ID`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `con_test` (
  `a` char(1) COLLATE utf8_unicode_ci DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `configuration` (
  `ID` bigint(20) NOT NULL AUTO_INCREMENT,
  `CLIENT_CONFIG_FK` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`ID`),
  UNIQUE KEY `CLIENT_CONFIG_FK` (`CLIENT_CONFIG_FK`),
  KEY `configuration_CLIENT_CONFIG_FC` (`CLIENT_CONFIG_FK`),
  CONSTRAINT `configuration_CLIENT_CONFIG_FC` FOREIGN KEY (`CLIENT_CONFIG_FK`) REFERENCES `configuration_client` (`ID`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

INSERT INTO `configuration` VALUES (1,1);
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `configuration_app_setting` (
  `SETTING_KEY` varchar(255) COLLATE utf8_unicode_ci NOT NULL,
  `SETTING_VALUE` text COLLATE utf8_unicode_ci,
  PRIMARY KEY (`SETTING_KEY`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

INSERT INTO `configuration_app_setting` VALUES ('com.communote.core.keystore.password','71d5a6c6-81f1-4f9b-b07f-8acecbafadb1'),('communote.standalone.installation','true'),('installation.date','kM1UqFoZXv7CwdKkbmtUbw:PYcwbctFB6yFk2z9'),('installation.unique.id','ba30ec62-4e65-41a0-89aa-a75658725bf9'),('kenmei.attachment.max.upload.size','10485760'),('kenmei.captcha.disable','true'),('kenmei.crc.file.repository.storage.dir.root','/tmp/communote-test/data/filerepository'),('kenmei.image.max.upload.size','1048576'),('kenmei.trusted.ca.keystore.password','52e8b142-a48a-4d62-af96-85bf4896e675'),('mailfetching.starttls','true'),('mailing.from.address','communote-installer-test@localhost'),('mailing.from.address.name','[Local Test] Communote-Team'),('mailing.host','localhost'),('mailing.port','25'),('mailing.starttls','true'),('virus.scanner.enabled','false');
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `configuration_client` (
  `ID` bigint(20) NOT NULL AUTO_INCREMENT,
  `LOGO_IMAGE` longblob,
  `LAST_LOGO_IMAGE_MODIFICATION_D` timestamp NOT NULL DEFAULT '0000-00-00 00:00:00',
  `TIME_ZONE_ID` varchar(1024) COLLATE utf8_unicode_ci DEFAULT NULL,
  `DEFAULT_BLOG_FK` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`ID`),
  UNIQUE KEY `DEFAULT_BLOG_FK` (`DEFAULT_BLOG_FK`),
  KEY `configuration_client_DEFAULT_C` (`DEFAULT_BLOG_FK`),
  CONSTRAINT `configuration_client_DEFAULT_C` FOREIGN KEY (`DEFAULT_BLOG_FK`) REFERENCES `core_blog` (`ID`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

INSERT INTO `configuration_client` VALUES (1,NULL,'2016-06-15 19:19:55','time.zones.gmt.Europe/Amsterdam',1);
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `configuration_confluence` (
  `ID` bigint(20) NOT NULL,
  `AUTHENTICATION_API_URL` varchar(1024) COLLATE utf8_unicode_ci NOT NULL,
  `IMAGE_API_URL` varchar(1024) COLLATE utf8_unicode_ci DEFAULT NULL,
  `ADMIN_LOGIN` varchar(1024) COLLATE utf8_unicode_ci DEFAULT NULL,
  `ADMIN_PASSWORD` varchar(1024) COLLATE utf8_unicode_ci DEFAULT NULL,
  `SERVICE_URL` varchar(1024) COLLATE utf8_unicode_ci DEFAULT NULL,
  `PERMISSIONS_URL` varchar(1024) COLLATE utf8_unicode_ci DEFAULT NULL,
  `BASE_PATH` varchar(1024) COLLATE utf8_unicode_ci DEFAULT NULL,
  PRIMARY KEY (`ID`),
  KEY `configuration_confluenceIFKC` (`ID`),
  CONSTRAINT `configuration_confluenceIFKC` FOREIGN KEY (`ID`) REFERENCES `configuration_external_sys` (`ID`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `configuration_external_sys` (
  `ID` bigint(20) NOT NULL AUTO_INCREMENT,
  `ALLOW_EXTERNAL_AUTHENTICATION` tinyint(4) NOT NULL,
  `SYSTEM_ID` varchar(50) COLLATE utf8_unicode_ci NOT NULL,
  `PRIMARY_AUTHENTICATION` tinyint(4) NOT NULL,
  `SYNCHRONIZE_USER_GROUPS` tinyint(4) NOT NULL,
  `CONFIGURATION_FK` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`ID`),
  UNIQUE KEY `SYSTEM_ID` (`SYSTEM_ID`),
  KEY `configuration_external_systemC` (`CONFIGURATION_FK`),
  CONSTRAINT `configuration_external_systemC` FOREIGN KEY (`CONFIGURATION_FK`) REFERENCES `configuration` (`ID`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `configuration_ldap` (
  `ID` bigint(20) NOT NULL,
  `URL` varchar(1024) COLLATE utf8_unicode_ci NOT NULL,
  `MANAGER_PASSWORD` varchar(1024) COLLATE utf8_unicode_ci NOT NULL,
  `MANAGER_D_N` varchar(1024) COLLATE utf8_unicode_ci NOT NULL,
  `GROUP_SYNC_CONFIG_FK` bigint(20) DEFAULT NULL,
  `USER_SEARCH_FK` bigint(20) NOT NULL,
  `user_identifier_is_binary` tinyint(1) NOT NULL,
  `sasl_mode` varchar(1024) COLLATE utf8_unicode_ci DEFAULT NULL,
  `server_domain` varchar(255) COLLATE utf8_unicode_ci DEFAULT NULL,
  `query_prefix` varchar(255) COLLATE utf8_unicode_ci DEFAULT NULL,
  `dynamic_mode` tinyint(1) DEFAULT '0',
  PRIMARY KEY (`ID`),
  KEY `configuration_ldapIFKC` (`ID`),
  CONSTRAINT `configuration_ldapIFKC` FOREIGN KEY (`ID`) REFERENCES `configuration_external_sys` (`ID`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `configuration_ldap_group` (
  `ID` bigint(20) NOT NULL AUTO_INCREMENT,
  `member_mode` tinyint(1) NOT NULL,
  `group_identifier_is_binary` tinyint(1) NOT NULL,
  `GROUP_SEARCH_FK` bigint(20) NOT NULL,
  PRIMARY KEY (`ID`),
  UNIQUE KEY `GROUP_SEARCH_FK` (`GROUP_SEARCH_FK`),
  CONSTRAINT `configuration_ldap_group_GROUC` FOREIGN KEY (`GROUP_SEARCH_FK`) REFERENCES `configuration_ldap_search` (`ID`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `configuration_ldap_sbase` (
  `ID` bigint(20) NOT NULL AUTO_INCREMENT,
  `search_base` varchar(1024) COLLATE utf8_unicode_ci NOT NULL,
  `search_subtree` tinyint(1) NOT NULL,
  `LDAP_SEARCH_CONFIGURATION_FK` bigint(20) NOT NULL,
  `sbase_idx` int(11) DEFAULT NULL,
  PRIMARY KEY (`ID`),
  KEY `configuration_ldap_sbase_LDAPC` (`LDAP_SEARCH_CONFIGURATION_FK`),
  CONSTRAINT `configuration_ldap_sbase_LDAPC` FOREIGN KEY (`LDAP_SEARCH_CONFIGURATION_FK`) REFERENCES `configuration_ldap_search` (`ID`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `configuration_ldap_search` (
  `ID` bigint(20) NOT NULL AUTO_INCREMENT,
  `search_filter` varchar(1024) COLLATE utf8_unicode_ci DEFAULT NULL,
  `property_mapping` varchar(1024) COLLATE utf8_unicode_ci NOT NULL,
  PRIMARY KEY (`ID`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `configuration_setting` (
  `setting_key` varchar(255) COLLATE utf8_unicode_ci NOT NULL DEFAULT '',
  `setting_value` text COLLATE utf8_unicode_ci,
  `CONFIGURATION_FK` bigint(20) DEFAULT NULL,
  `last_modification_timestamp` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`setting_key`),
  KEY `configuration_setting_CONFIGUC` (`CONFIGURATION_FK`),
  CONSTRAINT `configuration_setting_CONFIGUC` FOREIGN KEY (`CONFIGURATION_FK`) REFERENCES `configuration` (`ID`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

INSERT INTO `configuration_setting` VALUES ('client.creation.date','lKfbq7611K3xXVkmHBVRmw:SqXyRMlabi7y31s',1,1466018396754),('kenmei.automatic.user.activation','false',1,1466018396711),('kenmei.blog.allow.public.access','false',1,1466018396729),('kenmei.client.allow.all.can.read.write.for.all.users','true',1,1466018396743),('kenmei.client.blog.count.100.mail','',1,1466018396751),('kenmei.client.blog.count.90.mail','',1,1466018396757),('kenmei.client.blog.count.limit','0',1,1466018396738),('kenmei.client.delete.user.by.anonymize.enabled','false',1,1466018396714),('kenmei.client.delete.user.by.disable.enabled','false',1,1466018396708),('kenmei.client.reply.to.address','',1,1466018396725),('kenmei.client.reply.to.address.name','',1,1466018396697),('kenmei.client.support.email.address','',1,1466018396690),('kenmei.client.user.tagged.count.100.mail','',1,1466018396747),('kenmei.client.user.tagged.count.90.mail','',1,1466018396760),('kenmei.client.user.tagged.count.limit','0',1,1466018396700),('kenmei.crc.file.repository.size.100.mail','2016-06-14',1,1466018396704),('kenmei.crc.file.repository.size.90.mail','false',1,1466018396721),('kenmei.crc.file.repository.size.limit','0',1,1466018396733),('kenmei.notification.render.permalinks','true',1,1466018396694),('kenmei.unique.client.identifer','ac78f0a9-8101-45aa-8e37-550895cfbd39',1,1466018396717);
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `core_attachment` (
  `ID` bigint(20) NOT NULL AUTO_INCREMENT,
  `content_identifier` varchar(250) COLLATE utf8_unicode_ci NOT NULL,
  `REPOSITORY_IDENTIFIER` varchar(1024) COLLATE utf8_unicode_ci NOT NULL,
  `NAME` varchar(1024) COLLATE utf8_unicode_ci NOT NULL,
  `CONTENT_TYPE` varchar(1024) COLLATE utf8_unicode_ci DEFAULT NULL,
  `attachment_size` bigint(20) DEFAULT NULL,
  `STATUS` varchar(1024) COLLATE utf8_unicode_ci NOT NULL,
  `GLOBAL_ID_FK` bigint(20) DEFAULT NULL,
  `NOTE_FK` bigint(20) DEFAULT NULL,
  `UPLOADER_FK` bigint(20) DEFAULT NULL,
  `upload_date` datetime DEFAULT NULL,
  PRIMARY KEY (`ID`),
  UNIQUE KEY `content_identifier_unique_key` (`content_identifier`),
  UNIQUE KEY `GLOBAL_ID_FK` (`GLOBAL_ID_FK`),
  KEY `core_attachment_NOTE_FKC` (`NOTE_FK`),
  KEY `core_attachment_GLOBAL_ID_FKC` (`GLOBAL_ID_FK`),
  KEY `core_attachment_UPLOADER_FKC` (`UPLOADER_FK`),
  CONSTRAINT `core_attachment_GLOBAL_ID_FKC` FOREIGN KEY (`GLOBAL_ID_FK`) REFERENCES `core_global_id` (`ID`),
  CONSTRAINT `core_attachment_NOTE_FKC` FOREIGN KEY (`NOTE_FK`) REFERENCES `core_note` (`ID`),
  CONSTRAINT `core_attachment_UPLOADER_FKC` FOREIGN KEY (`UPLOADER_FK`) REFERENCES `user_user` (`ID`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `core_attachment_property` (
  `ID` bigint(20) NOT NULL AUTO_INCREMENT,
  `key_group` varchar(128) COLLATE utf8_unicode_ci NOT NULL,
  `property_key` varchar(128) COLLATE utf8_unicode_ci NOT NULL,
  `property_value` varchar(4000) COLLATE utf8_unicode_ci NOT NULL,
  `ATTACHMENT_FK` bigint(20) DEFAULT NULL,
  `last_modification_date` datetime NOT NULL DEFAULT '1983-06-19 04:09:23',
  PRIMARY KEY (`ID`),
  KEY `core_attachment_property_ATTAC` (`ATTACHMENT_FK`),
  CONSTRAINT `core_attachment_property_ATTAC` FOREIGN KEY (`ATTACHMENT_FK`) REFERENCES `core_attachment` (`ID`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `core_blog` (
  `ID` bigint(20) NOT NULL AUTO_INCREMENT,
  `TITLE` varchar(1024) COLLATE utf8_unicode_ci NOT NULL,
  `DESCRIPTION` text COLLATE utf8_unicode_ci,
  `CREATION_DATE` timestamp NOT NULL DEFAULT '0000-00-00 00:00:00',
  `NAME_IDENTIFIER` varchar(255) COLLATE utf8_unicode_ci NOT NULL,
  `LAST_MODIFICATION_DATE` timestamp NOT NULL DEFAULT '0000-00-00 00:00:00',
  `ALL_CAN_READ` tinyint(4) NOT NULL,
  `ALL_CAN_WRITE` tinyint(4) NOT NULL,
  `PUBLIC_ACCESS` tinyint(4) NOT NULL,
  `GLOBAL_ID_FK` bigint(20) DEFAULT NULL,
  `create_system_notes` tinyint(1) NOT NULL,
  `toplevel_topic` tinyint(1) DEFAULT '0',
  `crawl_last_modification_date` datetime DEFAULT NULL,
  PRIMARY KEY (`ID`),
  UNIQUE KEY `NAME_IDENTIFIER` (`NAME_IDENTIFIER`),
  UNIQUE KEY `GLOBAL_ID_FK` (`GLOBAL_ID_FK`),
  KEY `core_blog_GLOBAL_ID_FKC` (`GLOBAL_ID_FK`),
  KEY `core_blog_title_index` (`TITLE`(100)),
  KEY `core_blog_name_identifier_index` (`NAME_IDENTIFIER`(100)),
  CONSTRAINT `core_blog_GLOBAL_ID_FKC` FOREIGN KEY (`GLOBAL_ID_FK`) REFERENCES `core_global_id` (`ID`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

INSERT INTO `core_blog` VALUES (1,'Global Test Client',NULL,'2016-06-15 19:19:57','default','2016-06-15 19:19:57',1,1,0,3,0,0,'2016-06-15 21:19:58');
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `core_blog2blog` (
  `CHILDREN_FK` bigint(20) NOT NULL,
  `PARENTS_FK` bigint(20) NOT NULL,
  PRIMARY KEY (`CHILDREN_FK`,`PARENTS_FK`),
  KEY `core_blog_PARENTS_FKC` (`PARENTS_FK`),
  CONSTRAINT `core_blog_CHILDREN_FKC` FOREIGN KEY (`CHILDREN_FK`) REFERENCES `core_blog` (`ID`),
  CONSTRAINT `core_blog_PARENTS_FKC` FOREIGN KEY (`PARENTS_FK`) REFERENCES `core_blog` (`ID`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `core_blog2blog_resolved` (
  `ID` bigint(20) NOT NULL AUTO_INCREMENT,
  `parent_topic_id` bigint(20) NOT NULL,
  `child_topic_id` bigint(20) NOT NULL,
  `topic_path` varchar(255) COLLATE utf8_unicode_ci NOT NULL,
  PRIMARY KEY (`ID`),
  UNIQUE KEY `kenmei6192cnstrnt` (`parent_topic_id`,`child_topic_id`,`topic_path`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `core_blog2tag` (
  `BLOGS_FK` bigint(20) NOT NULL,
  `TAGS_FK` bigint(20) NOT NULL,
  PRIMARY KEY (`BLOGS_FK`,`TAGS_FK`),
  KEY `core_tag_BLOGS_FKC` (`BLOGS_FK`),
  KEY `core_blog_TAGS_FKC` (`TAGS_FK`),
  CONSTRAINT `core_blog_TAGS_FKC` FOREIGN KEY (`TAGS_FK`) REFERENCES `core_tag` (`ID`),
  CONSTRAINT `core_tag_BLOGS_FKC` FOREIGN KEY (`BLOGS_FK`) REFERENCES `core_blog` (`ID`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

INSERT INTO `core_blog2tag` VALUES (1,1);
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `core_blog_member` (
  `ID` bigint(20) NOT NULL AUTO_INCREMENT,
  `class` varchar(255) COLLATE utf8_unicode_ci NOT NULL,
  `ROLE` varchar(1024) COLLATE utf8_unicode_ci NOT NULL,
  `BLOG_FK` bigint(20) NOT NULL,
  `KENMEI_ENTITY_FK` bigint(20) NOT NULL,
  `EXTERNAL_SYSTEM_ID` varchar(50) COLLATE utf8_unicode_ci DEFAULT NULL,
  PRIMARY KEY (`ID`),
  KEY `core_blog_member_BLOG_FKC` (`BLOG_FK`),
  KEY `core_blog_member_KENMEI_ENTITC` (`KENMEI_ENTITY_FK`),
  CONSTRAINT `core_blog_member_BLOG_FKC` FOREIGN KEY (`BLOG_FK`) REFERENCES `core_blog` (`ID`),
  CONSTRAINT `core_blog_member_KENMEI_ENTITC` FOREIGN KEY (`KENMEI_ENTITY_FK`) REFERENCES `user_entity` (`ID`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

INSERT INTO `core_blog_member` VALUES (1,'BlogMemberImpl','MANAGER',1,1,NULL);
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `core_blog_property` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `key_group` varchar(128) COLLATE utf8_unicode_ci NOT NULL,
  `property_key` varchar(128) COLLATE utf8_unicode_ci NOT NULL,
  `property_value` varchar(4000) COLLATE utf8_unicode_ci NOT NULL,
  `blog_fk` bigint(20) DEFAULT NULL,
  `last_modification_date` datetime NOT NULL DEFAULT '1983-06-19 04:09:23',
  PRIMARY KEY (`id`),
  KEY `core_blog_property_BLOG_FKC` (`blog_fk`),
  CONSTRAINT `core_blog_property_BLOG_FKC` FOREIGN KEY (`blog_fk`) REFERENCES `core_blog` (`ID`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `core_content` (
  `ID` bigint(20) NOT NULL AUTO_INCREMENT,
  `CONTENT` text COLLATE utf8_unicode_ci NOT NULL,
  `SHORT_CONTENT` text COLLATE utf8_unicode_ci,
  PRIMARY KEY (`ID`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `core_entity2tags` (
  `KENMEI_ENTITIES_FK` bigint(20) NOT NULL,
  `TAGS_FK` bigint(20) NOT NULL,
  KEY `core_entity2tags_idx` (`KENMEI_ENTITIES_FK`,`TAGS_FK`),
  KEY `user_entity_TAGS_FKC` (`TAGS_FK`),
  CONSTRAINT `core_tag_KENMEI_ENTITIES_FKC` FOREIGN KEY (`KENMEI_ENTITIES_FK`) REFERENCES `user_entity` (`ID`),
  CONSTRAINT `user_entity_TAGS_FKC` FOREIGN KEY (`TAGS_FK`) REFERENCES `core_tag` (`ID`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `core_external_object` (
  `ID` bigint(20) NOT NULL AUTO_INCREMENT,
  `EXTERNAL_SYSTEM_ID` varchar(50) COLLATE utf8_unicode_ci NOT NULL,
  `EXTERNAL_ID` varchar(200) COLLATE utf8_unicode_ci NOT NULL,
  `EXTERNAL_NAME` varchar(1024) COLLATE utf8_unicode_ci DEFAULT NULL,
  `BLOG_FK` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`ID`),
  UNIQUE KEY `EXTERNAL_SYSTEM_ID` (`EXTERNAL_SYSTEM_ID`,`EXTERNAL_ID`),
  KEY `core_external_object_BLOG_FKC` (`BLOG_FK`),
  CONSTRAINT `core_external_object_BLOG_FKC` FOREIGN KEY (`BLOG_FK`) REFERENCES `core_blog` (`ID`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `core_external_object_prop` (
  `ID` bigint(20) NOT NULL AUTO_INCREMENT,
  `property_key` varchar(128) COLLATE utf8_unicode_ci DEFAULT NULL,
  `property_value` varchar(4000) COLLATE utf8_unicode_ci DEFAULT NULL,
  `EXTERNAL_OBJECT_FK` bigint(20) DEFAULT NULL,
  `key_group` varchar(128) COLLATE utf8_unicode_ci NOT NULL,
  `last_modification_date` timestamp NOT NULL DEFAULT '1993-06-19 14:16:16',
  PRIMARY KEY (`ID`),
  KEY `core_external_object_propertiC` (`EXTERNAL_OBJECT_FK`),
  CONSTRAINT `core_external_object_propertiC` FOREIGN KEY (`EXTERNAL_OBJECT_FK`) REFERENCES `core_external_object` (`ID`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `core_global_binary_prop` (
  `ID` bigint(20) NOT NULL AUTO_INCREMENT,
  `key_group` varchar(128) COLLATE utf8_unicode_ci NOT NULL,
  `property_key` varchar(128) COLLATE utf8_unicode_ci NOT NULL,
  `property_value` mediumblob NOT NULL,
  `KENMEI_ENTITY_GROUP_FK` bigint(20) DEFAULT NULL,
  `last_modification_date` datetime NOT NULL DEFAULT '1983-06-19 04:09:23',
  PRIMARY KEY (`ID`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `core_global_id` (
  `ID` bigint(20) NOT NULL AUTO_INCREMENT,
  `GLOBAL_IDENTIFIER` varchar(255) COLLATE utf8_unicode_ci NOT NULL,
  PRIMARY KEY (`ID`),
  UNIQUE KEY `GLOBAL_IDENTIFIER` (`GLOBAL_IDENTIFIER`)
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

INSERT INTO `core_global_id` VALUES (3,'/ac78f0a9-8101-45aa-8e37-550895cfbd39/blog/1'),(2,'/ac78f0a9-8101-45aa-8e37-550895cfbd39/tag/1'),(1,'/ac78f0a9-8101-45aa-8e37-550895cfbd39/user/1');
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `core_note` (
  `ID` bigint(20) NOT NULL AUTO_INCREMENT,
  `CREATION_DATE` timestamp NOT NULL DEFAULT '0000-00-00 00:00:00',
  `LAST_MODIFICATION_DATE` timestamp NOT NULL DEFAULT '0000-00-00 00:00:00',
  `CREATION_SOURCE` varchar(1024) COLLATE utf8_unicode_ci NOT NULL,
  `DIRECT` tinyint(4) NOT NULL,
  `STATUS` varchar(1024) COLLATE utf8_unicode_ci NOT NULL,
  `VERSION` bigint(20) NOT NULL,
  `DISCUSSION_PATH` varchar(1024) COLLATE utf8_unicode_ci DEFAULT NULL,
  `USER_FK` bigint(20) NOT NULL,
  `CONTENT_FK` bigint(20) NOT NULL,
  `GLOBAL_ID_FK` bigint(20) DEFAULT NULL,
  `BLOG_FK` bigint(20) NOT NULL,
  `PARENT_FK` bigint(20) DEFAULT NULL,
  `ORIGIN_FK` bigint(20) DEFAULT NULL,
  `discussion_id` bigint(20) DEFAULT NULL,
  `last_discussion_creation_date` timestamp NOT NULL DEFAULT '0000-00-00 00:00:00',
  `mention_topic_readers` tinyint(1) DEFAULT '0',
  `mention_topic_authors` tinyint(1) DEFAULT '0',
  `mention_topic_managers` tinyint(1) DEFAULT '0',
  `mention_discussion_authors` tinyint(1) DEFAULT '0',
  `crawl_last_modification_date` datetime DEFAULT NULL,
  PRIMARY KEY (`ID`),
  UNIQUE KEY `CONTENT_FK` (`CONTENT_FK`),
  UNIQUE KEY `GLOBAL_ID_FK` (`GLOBAL_ID_FK`),
  KEY `core_note_PARENT_FKC` (`PARENT_FK`),
  KEY `core_note_CONTENT_FKC` (`CONTENT_FK`),
  KEY `core_note_USER_FKC` (`USER_FK`),
  KEY `core_note_ORIGIN_FKC` (`ORIGIN_FK`),
  KEY `core_note_BLOG_FKC` (`BLOG_FK`),
  KEY `core_note_GLOBAL_ID_FKC` (`GLOBAL_ID_FK`),
  KEY `core_note_creation_date_index` (`CREATION_DATE`),
  KEY `core_note_discussion_id` (`discussion_id`),
  KEY `kenmei5455_idx` (`CREATION_DATE`,`ID`),
  KEY `kenmei5562_idx` (`last_discussion_creation_date`,`ID`),
  KEY `kenmei6122_idx` (`BLOG_FK`,`USER_FK`),
  CONSTRAINT `core_note_BLOG_FKC` FOREIGN KEY (`BLOG_FK`) REFERENCES `core_blog` (`ID`),
  CONSTRAINT `core_note_CONTENT_FKC` FOREIGN KEY (`CONTENT_FK`) REFERENCES `core_content` (`ID`),
  CONSTRAINT `core_note_GLOBAL_ID_FKC` FOREIGN KEY (`GLOBAL_ID_FK`) REFERENCES `core_global_id` (`ID`),
  CONSTRAINT `core_note_ORIGIN_FKC` FOREIGN KEY (`ORIGIN_FK`) REFERENCES `core_note` (`ID`),
  CONSTRAINT `core_note_PARENT_FKC` FOREIGN KEY (`PARENT_FK`) REFERENCES `core_note` (`ID`),
  CONSTRAINT `core_note_USER_FKC` FOREIGN KEY (`USER_FK`) REFERENCES `user_user` (`ID`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `core_note2direct_user` (
  `DIRECT_USERS_FK` bigint(20) NOT NULL,
  `DIRECT_NOTES_FK` bigint(20) NOT NULL,
  PRIMARY KEY (`DIRECT_USERS_FK`,`DIRECT_NOTES_FK`),
  KEY `user_user_DIRECT_NOTES_FKC` (`DIRECT_NOTES_FK`),
  CONSTRAINT `core_note_DIRECT_USERS_FKC` FOREIGN KEY (`DIRECT_USERS_FK`) REFERENCES `user_user` (`ID`),
  CONSTRAINT `user_user_DIRECT_NOTES_FKC` FOREIGN KEY (`DIRECT_NOTES_FK`) REFERENCES `core_note` (`ID`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `core_note2followable` (
  `NOTES_FK` bigint(20) NOT NULL,
  `FOLLOWABLE_ITEMS_FK` bigint(20) NOT NULL,
  PRIMARY KEY (`NOTES_FK`,`FOLLOWABLE_ITEMS_FK`),
  KEY `core_note_follow_items_fk_idx` (`FOLLOWABLE_ITEMS_FK`),
  KEY `core_note_follow_note_fk_idx` (`NOTES_FK`),
  CONSTRAINT `core_global_id_NOTES_FKC` FOREIGN KEY (`NOTES_FK`) REFERENCES `core_note` (`ID`),
  CONSTRAINT `core_note_FOLLOWABLE_ITEMS_FKC` FOREIGN KEY (`FOLLOWABLE_ITEMS_FK`) REFERENCES `core_global_id` (`ID`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `core_note_property` (
  `ID` bigint(20) NOT NULL AUTO_INCREMENT,
  `key_group` varchar(128) COLLATE utf8_unicode_ci NOT NULL,
  `property_key` varchar(128) COLLATE utf8_unicode_ci NOT NULL,
  `property_value` varchar(4000) COLLATE utf8_unicode_ci NOT NULL,
  `NOTE_FK` bigint(20) DEFAULT NULL,
  `last_modification_date` datetime NOT NULL DEFAULT '1983-06-19 04:09:23',
  PRIMARY KEY (`ID`),
  KEY `core_note_properties_NOTE_FKC` (`NOTE_FK`),
  CONSTRAINT `core_note_properties_NOTE_FKC` FOREIGN KEY (`NOTE_FK`) REFERENCES `core_note` (`ID`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `core_notes2crossblogs` (
  `NOTES_FK` bigint(20) NOT NULL,
  `CROSSPOST_BLOGS_FK` bigint(20) NOT NULL,
  PRIMARY KEY (`NOTES_FK`,`CROSSPOST_BLOGS_FK`),
  KEY `core_note_CROSSPOST_BLOGS_FKC` (`CROSSPOST_BLOGS_FK`),
  KEY `core_blog_NOTES_FKC` (`NOTES_FK`),
  CONSTRAINT `core_blog_NOTES_FKC` FOREIGN KEY (`NOTES_FK`) REFERENCES `core_note` (`ID`),
  CONSTRAINT `core_note_CROSSPOST_BLOGS_FKC` FOREIGN KEY (`CROSSPOST_BLOGS_FK`) REFERENCES `core_blog` (`ID`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `core_notes2tag` (
  `NOTES_FK` bigint(20) NOT NULL,
  `TAGS_FK` bigint(20) NOT NULL,
  PRIMARY KEY (`NOTES_FK`,`TAGS_FK`),
  KEY `core_note_TAGS_FKC` (`TAGS_FK`),
  KEY `core_tag_NOTES_FKC` (`NOTES_FK`),
  CONSTRAINT `core_note_TAGS_FKC` FOREIGN KEY (`TAGS_FK`) REFERENCES `core_tag` (`ID`),
  CONSTRAINT `core_tag_NOTES_FKC` FOREIGN KEY (`NOTES_FK`) REFERENCES `core_note` (`ID`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `core_notes2user_to_notify` (
  `NOTES_FK` bigint(20) NOT NULL,
  `USERS_TO_BE_NOTIFIED_FK` bigint(20) NOT NULL,
  PRIMARY KEY (`NOTES_FK`,`USERS_TO_BE_NOTIFIED_FK`),
  KEY `core_note_USERS_TO_BE_NOTIFIEC` (`USERS_TO_BE_NOTIFIED_FK`),
  KEY `user_user_NOTES_FKC` (`NOTES_FK`),
  CONSTRAINT `core_note_USERS_TO_BE_NOTIFIEC` FOREIGN KEY (`USERS_TO_BE_NOTIFIED_FK`) REFERENCES `user_user` (`ID`),
  CONSTRAINT `user_user_NOTES_FKC` FOREIGN KEY (`NOTES_FK`) REFERENCES `core_note` (`ID`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `core_plugin_properties` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `key_group` varchar(128) COLLATE utf8_unicode_ci NOT NULL,
  `property_key` varchar(128) COLLATE utf8_unicode_ci NOT NULL,
  `property_value` text COLLATE utf8_unicode_ci NOT NULL,
  `last_modification_date` datetime NOT NULL DEFAULT '1983-06-19 04:08:23',
  `application_property` tinyint(1) DEFAULT '0',
  PRIMARY KEY (`id`),
  UNIQUE KEY `plugin_prop_cnst` (`property_key`,`key_group`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `core_processed_utp_mail` (
  `ID` bigint(20) NOT NULL AUTO_INCREMENT,
  `MAIL_MESSAGE_ID` varchar(255) COLLATE utf8_unicode_ci NOT NULL,
  PRIMARY KEY (`ID`),
  UNIQUE KEY `MAIL_MESSAGE_ID` (`MAIL_MESSAGE_ID`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `core_role2blog` (
  `ID` bigint(20) NOT NULL AUTO_INCREMENT,
  `BLOG_ID` bigint(20) NOT NULL,
  `USER_ID` bigint(20) NOT NULL,
  `NUMERIC_ROLE` int(11) NOT NULL,
  `EXTERNAL_SYSTEM_ID` varchar(50) COLLATE utf8_unicode_ci DEFAULT NULL,
  `GRANTED_BY_GROUP` tinyint(4) NOT NULL,
  PRIMARY KEY (`ID`),
  KEY `core_role2blog_bidx` (`BLOG_ID`),
  KEY `core_role2blog_uidx` (`USER_ID`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

INSERT INTO `core_role2blog` VALUES (1,1,1,3,NULL,0);
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `core_role2blog_granting_group` (
  `USER_TO_BLOG_ROLE_MAPPINGS_FK` bigint(20) NOT NULL,
  `GRANTING_GROUPS_FK` bigint(20) NOT NULL,
  PRIMARY KEY (`USER_TO_BLOG_ROLE_MAPPINGS_FK`,`GRANTING_GROUPS_FK`),
  KEY `core_role2blog_GRANTING_GROUPC` (`GRANTING_GROUPS_FK`),
  KEY `user_group_USER_TO_BLOG_ROLE_C` (`USER_TO_BLOG_ROLE_MAPPINGS_FK`),
  CONSTRAINT `core_role2blog_GRANTING_GROUPC` FOREIGN KEY (`GRANTING_GROUPS_FK`) REFERENCES `user_group` (`ID`),
  CONSTRAINT `user_group_USER_TO_BLOG_ROLE_C` FOREIGN KEY (`USER_TO_BLOG_ROLE_MAPPINGS_FK`) REFERENCES `core_role2blog` (`ID`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `core_tag` (
  `ID` bigint(20) NOT NULL AUTO_INCREMENT,
  `class` varchar(255) COLLATE utf8_unicode_ci NOT NULL,
  `tag_store_tag_id` varchar(255) COLLATE utf8_unicode_ci NOT NULL,
  `default_name` varchar(255) COLLATE utf8_unicode_ci NOT NULL,
  `GLOBAL_ID_FK` bigint(20) DEFAULT NULL,
  `CATEGORY_FK` bigint(20) DEFAULT NULL,
  `ABSTRACT_TAG_CATEGORY_TAGS_IDX` int(11) DEFAULT NULL,
  `tag_store_alias` varchar(255) COLLATE utf8_unicode_ci NOT NULL DEFAULT 'DefaultNoteTagStore',
  PRIMARY KEY (`ID`),
  UNIQUE KEY `core_tag_store_idx` (`tag_store_tag_id`,`tag_store_alias`),
  UNIQUE KEY `GLOBAL_ID_FK` (`GLOBAL_ID_FK`),
  KEY `CATEGORIZED_TAG_CATEGORY_FKC` (`CATEGORY_FK`),
  KEY `core_tag_GLOBAL_ID_FKC` (`GLOBAL_ID_FK`),
  KEY `core_tag_lower_name_idx` (`tag_store_tag_id`(100)),
  CONSTRAINT `CATEGORIZED_TAG_CATEGORY_FKC` FOREIGN KEY (`CATEGORY_FK`) REFERENCES `core_tag_category` (`ID`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

INSERT INTO `core_tag` VALUES (1,'TagImpl','default','default',2,NULL,NULL,'DefaultBlogTagStore');
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `core_tag2descriptions` (
  `TAGS_FK` bigint(20) NOT NULL,
  `DESCRIPTIONS_FK` bigint(20) NOT NULL,
  UNIQUE KEY `core_tag2descriptions_idx` (`TAGS_FK`,`DESCRIPTIONS_FK`),
  KEY `core_tag_DESCRIPTIONS_FKC` (`DESCRIPTIONS_FK`),
  CONSTRAINT `core_tag_DESCRIPTIONS_FKC` FOREIGN KEY (`DESCRIPTIONS_FK`) REFERENCES `custom_messages` (`ID`),
  CONSTRAINT `custom_messages_TAGS_DESC_FKC` FOREIGN KEY (`TAGS_FK`) REFERENCES `core_tag` (`ID`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `core_tag2names` (
  `TAGS_FK` bigint(20) NOT NULL,
  `NAMES_FK` bigint(20) NOT NULL,
  UNIQUE KEY `core_tag2names_idx` (`TAGS_FK`,`NAMES_FK`),
  KEY `core_tag_NAMES_FKC` (`NAMES_FK`),
  CONSTRAINT `core_tag_NAMES_FKC` FOREIGN KEY (`NAMES_FK`) REFERENCES `custom_messages` (`ID`),
  CONSTRAINT `custom_messages_TAGS_FKC` FOREIGN KEY (`TAGS_FK`) REFERENCES `core_tag` (`ID`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `core_tag_category` (
  `ID` bigint(20) NOT NULL AUTO_INCREMENT,
  `class` varchar(255) COLLATE utf8_unicode_ci NOT NULL,
  `NAME` varchar(1024) COLLATE utf8_unicode_ci NOT NULL,
  `PREFIX` varchar(1024) COLLATE utf8_unicode_ci NOT NULL,
  `DESCRIPTION` varchar(1024) COLLATE utf8_unicode_ci DEFAULT NULL,
  `MULTIPLE_TAGS` tinyint(4) NOT NULL,
  PRIMARY KEY (`ID`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `core_task` (
  `ID` bigint(20) NOT NULL AUTO_INCREMENT,
  `unique_name` varchar(255) COLLATE utf8_unicode_ci NOT NULL,
  `task_status` varchar(255) COLLATE utf8_unicode_ci DEFAULT NULL,
  `task_interval` bigint(20) DEFAULT NULL,
  `handler_class_name` varchar(1024) COLLATE utf8_unicode_ci NOT NULL,
  `active` tinyint(1) NOT NULL,
  `next_execution` timestamp NOT NULL DEFAULT '0000-00-00 00:00:00',
  `last_execution` timestamp NULL DEFAULT '0000-00-00 00:00:00',
  PRIMARY KEY (`ID`),
  UNIQUE KEY `unique_name` (`unique_name`)
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

INSERT INTO `core_task` VALUES (1,'SynchronizeGroups','PENDING',3600000,'com.communote.server.core.user.groups.UserGroupSynchronizationTaskHandler',1,'2016-06-15 19:24:56',NULL),(2,'RemindUsers','PENDING',8640000,'com.communote.server.core.user.RemindUserJob',1,'2016-06-15 19:24:56',NULL),(3,'DeleteOrphanedAttachments','PENDING',604800000,'com.communote.server.core.tasks.DeleteOrphanedAttachmentsTaskHandler',1,'2016-06-15 22:19:56',NULL);
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `core_task_execs` (
  `ID` bigint(20) NOT NULL AUTO_INCREMENT,
  `instance_name` varchar(1024) COLLATE utf8_unicode_ci NOT NULL,
  `TASK_FK` bigint(20) NOT NULL,
  PRIMARY KEY (`ID`),
  UNIQUE KEY `TASK_FK` (`TASK_FK`),
  CONSTRAINT `core_task_execs_TASK_FKC` FOREIGN KEY (`TASK_FK`) REFERENCES `core_task` (`ID`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `core_task_props` (
  `ID` bigint(20) NOT NULL AUTO_INCREMENT,
  `property_key` varchar(1024) COLLATE utf8_unicode_ci NOT NULL,
  `property_value` varchar(1024) COLLATE utf8_unicode_ci DEFAULT NULL,
  `TASK_FK` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`ID`),
  KEY `core_task_props_TASK_FKC` (`TASK_FK`),
  CONSTRAINT `core_task_props_TASK_FKC` FOREIGN KEY (`TASK_FK`) REFERENCES `core_task` (`ID`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `core_user2follows` (
  `KENMEI_USERS_FK` bigint(20) NOT NULL,
  `FOLLOWED_ITEMS_FK` bigint(20) NOT NULL,
  PRIMARY KEY (`KENMEI_USERS_FK`,`FOLLOWED_ITEMS_FK`),
  KEY `user_user_FOLLOWED_ITEMS_FKC` (`FOLLOWED_ITEMS_FK`),
  CONSTRAINT `core_global_id_KENMEI_USERS_FC` FOREIGN KEY (`KENMEI_USERS_FK`) REFERENCES `user_user` (`ID`),
  CONSTRAINT `user_user_FOLLOWED_ITEMS_FKC` FOREIGN KEY (`FOLLOWED_ITEMS_FK`) REFERENCES `core_global_id` (`ID`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `core_users2favorite_notes` (
  `FAVORITE_NOTES_FK` bigint(20) NOT NULL,
  `FAVORITE_USERS_FK` bigint(20) NOT NULL,
  PRIMARY KEY (`FAVORITE_NOTES_FK`,`FAVORITE_USERS_FK`),
  KEY `user_user_FAVORITE_NOTES_FKC` (`FAVORITE_NOTES_FK`),
  KEY `core_note_FAVORITE_USERS_FKC` (`FAVORITE_USERS_FK`),
  CONSTRAINT `core_note_FAVORITE_USERS_FKC` FOREIGN KEY (`FAVORITE_USERS_FK`) REFERENCES `user_user` (`ID`),
  CONSTRAINT `user_user_FAVORITE_NOTES_FKC` FOREIGN KEY (`FAVORITE_NOTES_FK`) REFERENCES `core_note` (`ID`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `custom_messages` (
  `ID` bigint(20) NOT NULL AUTO_INCREMENT,
  `message_key` varchar(255) COLLATE utf8_unicode_ci DEFAULT NULL,
  `MESSAGE` text COLLATE utf8_unicode_ci NOT NULL,
  `IS_HTML` tinyint(4) NOT NULL,
  `LANGUAGE_FK` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`ID`),
  UNIQUE KEY `KEY` (`message_key`,`LANGUAGE_FK`),
  KEY `custom_messages_LANGUAGE_FKC` (`LANGUAGE_FK`),
  CONSTRAINT `custom_messages_LANGUAGE_FKC` FOREIGN KEY (`LANGUAGE_FK`) REFERENCES `md_language` (`ID`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `databasechangelog` (
  `ID` varchar(63) COLLATE utf8_unicode_ci NOT NULL,
  `AUTHOR` varchar(63) COLLATE utf8_unicode_ci NOT NULL,
  `FILENAME` varchar(200) COLLATE utf8_unicode_ci NOT NULL,
  `DATEEXECUTED` datetime NOT NULL,
  `MD5SUM` varchar(32) COLLATE utf8_unicode_ci DEFAULT NULL,
  `DESCRIPTION` varchar(255) COLLATE utf8_unicode_ci DEFAULT NULL,
  `COMMENTS` varchar(255) COLLATE utf8_unicode_ci DEFAULT NULL,
  `TAG` varchar(255) COLLATE utf8_unicode_ci DEFAULT NULL,
  `LIQUIBASE` varchar(10) COLLATE utf8_unicode_ci DEFAULT NULL,
  PRIMARY KEY (`ID`,`AUTHOR`,`FILENAME`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

INSERT INTO `databasechangelog` VALUES ('20091007_mysql','unknown','de/communardo/kenmei/database/update/v1.0.3/db.changelog.v1.0.3.xml','2016-06-15 21:17:53','89652831b7f99da19387e93f72471b','Custom SQL','',NULL,'1.9.2'),('Add_binary_property_autoincrement','unknown','de/communardo/kenmei/database/update/v1.3/db.changelog.v1.3.xml','2016-06-15 21:18:28','75e9bb7923a12a020d14d3cda8a11','Set Column as Auto-Increment','',NULL,'1.9.5'),('ADD_INSTALLATION_ID','unknown','de/communardo/kenmei/database/update_2nd_pass/v1.1.4/db.changelog.v1.1.4.xml','2016-06-15 21:19:55','467db414d6b587dab40689753ecef1','Custom Change','Inserts an unique id to the application properties.',NULL,'1.9.5'),('Add_Last_Modification_Date_To_Properties','unknown','de/communardo/kenmei/database/update/v1.3/db.changelog.v1.3.xml','2016-06-15 21:18:36','a452ff6128142840f7a082334267d02b','Add Column (x4)','',NULL,'1.9.5'),('confluence_set_basepath_mysql','unknown','de/communardo/kenmei/database/update/v1.1.3/db.changelog.v1.1.3.xml','2016-06-15 21:17:53','8dab40b53251883889955704653be51','Custom SQL','',NULL,'1.9.2'),('CR_115_Add_Application_Settings_1','unknown','de/communardo/kenmei/database/update/v1.1.4/db.changelog.v1.1.4.xml','2016-06-15 21:17:56','63fdc9f116fec14a18b3c3caaa2f6c26','Create Table','',NULL,'1.9.5'),('CR_115_Add_Application_Settings_Fix_Engine','unknown','de/communardo/kenmei/database/update/v1.1.4/db.changelog.v1.1.4.xml','2016-06-15 21:17:56','3da6fc71eb27642e1c85fb6cc7856b1','Custom SQL','',NULL,'1.9.5'),('CR_115_Move_Application_Properties_Into_DB','unknown','de/communardo/kenmei/database/update_2nd_pass/v1.1.4/db.changelog.v1.1.4.xml','2016-06-15 21:19:55','57ff61206a4cf95e4a917e86e6bb0a8','Custom Change','Get rid of property files and store the properties in the DB.',NULL,'1.9.5'),('CR_115_Refactor_URL_Application_Properties','unknown','de/communardo/kenmei/database/update_2nd_pass/v1.1.4/db.changelog.v1.1.4.xml','2016-06-15 21:19:55','5ff5d0d59e42e57f8e949791cfad13e','Custom Change','Convert old URL related properties to the new properties.',NULL,'1.9.5'),('CR_115_Remove_Client_Proxy_Url','unknown','de/communardo/kenmei/database/update/v1.1.4/db.changelog.v1.1.4.xml','2016-06-15 21:17:57','6ff27dc938b73dd590d32872eef4b068','Drop Column','',NULL,'1.9.5'),('cr_179_add_is_Html_column_v2','unknown','de/communardo/kenmei/database/update/v1.1.3/db.changelog.v1.1.3.xml','2016-06-15 21:17:53','51c815f15bbb47cb1990c3f556b2fe','Add Column, Add Not-Null Constraint','',NULL,'1.9.2'),('cr_179_add_messages_table_fix_key_column','unknown','de/communardo/kenmei/database/update/v1.1.3/db.changelog.v1.1.3.xml','2016-06-15 21:17:53','db6273f493f2c16fa8c63f1a43ea57','Modify Column','',NULL,'1.9.2'),('cr_179_add_messages_table_v2','unknown','de/communardo/kenmei/database/update/v1.1.3/db.changelog.v1.1.3.xml','2016-06-15 21:17:53','51bf6b30763f19d84b1ba2aeac1fd14','Create Table, Add Foreign Key Constraint, Add Unique Constraint','',NULL,'1.9.2'),('cr_179_insert_default_values_for_imprint_terms_of_use_mysql','unknown','de/communardo/kenmei/database/update/v1.1.3/db.changelog.v1.1.3.xml','2016-06-15 21:17:53','c7e584ebae6171b861bd5a37f4be42','Custom SQL','',NULL,'1.9.2'),('cr135','amo','de/communardo/kenmei/database/update/v1.1.3/db.changelog.v1.1.3.xml','2016-06-15 21:17:53','a02a9368d39aadb2b9affeff80229c','Create Table, Add Foreign Key Constraint','CR 135 - User group synchronization',NULL,'1.9.2'),('cr135_1','unknown','de/communardo/kenmei/database/update/v1.1.3/db.changelog.v1.1.3.xml','2016-06-15 21:17:53','318b839a3fff783a8f4a4a3e7f5866','Add Column','Add serviceUrl to confluence configuration',NULL,'1.9.2'),('cr69_user_groups_v2','unknown','de/communardo/kenmei/database/update/v1.1.3/db.changelog.v1.1.3.xml','2016-06-15 21:17:53','41e53effcad9d4fe485b75dab0922bf1','Rename Column','',NULL,'1.9.2'),('Create_Default_Blog_Where_Missing','unknown','de/communardo/kenmei/database/update_2nd_pass/v1.1.4/db.changelog.v1.1.4.xml','2016-06-15 21:19:55','b5f386a532aacea1a461a7be18459a52','Custom Change','Create the default blog for clients where there is none yet.',NULL,'1.9.5'),('Create_encrypted_creation_date','unknown','de/communardo/kenmei/database/update_2nd_pass/v1.1.4/db.changelog.v1.1.4.xml','2016-06-15 21:19:55','b8279228f0818d3e3d42ba4a1984199','Custom Change','No comment.',NULL,'1.9.5'),('Fix_Column_Size_In_ROLE2BLOG','unknown','de/communardo/kenmei/database/update/v1.2/db.changelog.v1.2.xml','2016-06-15 21:18:19','2a9f758ec4728aa16fd4bc3c8c22d19','Modify Column','Fix of bug in installer script',NULL,'1.9.5'),('initialize-database-schema_mysql_v1','unknown','de/communardo/kenmei/database/install/mysql/db.changelog.init-db.mysql.xml','2016-06-15 21:17:53','3673535e4428693110a8c0b89f1d69fb','SQL From File','Initialize the Database Schema',NULL,'1.9.5'),('installation_type_protection','Communote','com/communote/server/liquibase/update/v3.x/db.changelog.v3.4.xml','2016-06-15 21:19:53','ff39513379a4a6b048f2ad17774313a1','Insert Row','',NULL,'1.9.5'),('jt1528_convert_core_note_to_innoDB','unknown','de/communardo/kenmei/database/update/v1.1.3/db.changelog.v1.1.3.xml','2016-06-15 21:17:53','5c59d4c1140af9231be2cdf42c265d','Custom SQL','Change engine of core_note to innoDB',NULL,'1.9.2'),('KENMEI_1555_Rename_reserved_keywords','unknown','de/communardo/kenmei/database/update/v1.1.4/db.changelog.v1.1.4.xml','2016-06-15 21:17:57','d8bb446dfba4c335fa7ecc4f5381a919','Rename Column (x2)','',NULL,'1.9.5'),('KENMEI_1577_Remove_crc_filesystem_config','unknown','de/communardo/kenmei/database/update/v1.1.4/db.changelog.v1.1.4.xml','2016-06-15 21:17:57','f675d97e34cf9e8f6f42c08df83e4366','Drop Table','',NULL,'1.9.5'),('KENMEI-1264_Offline-Autosave','Communote','de/communardo/kenmei/database/update/v3.x/db.changelog.v3.1.xml','2016-06-15 21:19:46','beb0cac28fc0a9df3b983d6cefefd9f0','Add Column','',NULL,'1.9.5'),('KENMEI-1264_Offline-Autosave_AddUploader_mysql','Communote','de/communardo/kenmei/database/update/v3.x/db.changelog.v3.1.xml','2016-06-15 21:19:46','ba85eb5334fb28a69726d07597455efb','Custom SQL','',NULL,'1.9.5'),('KENMEI-1264_Offline-Autosave_Foreign_Keys','Communote','de/communardo/kenmei/database/update/v3.x/db.changelog.v3.1.xml','2016-06-15 21:19:46','f94dc4491622b20d6bd97b3e358698a','Add Foreign Key Constraint','',NULL,'1.9.5'),('KENMEI-1264_Offline-Autosave_TaskHandler_3','Communote','de/communardo/kenmei/database/update_2nd_pass/v3.x/db.changelog.v3.1.xml','2016-06-15 21:19:56','f343c72a10c5f04551c4344a8490a2e5','Custom Change','Add DeleteOrphanedAttachmentsTaskHandler to tasks',NULL,'1.9.5'),('KENMEI-1521_Purge_Disconnected_External_Auths','unknown','de/communardo/kenmei/database/update/v1.1.3/db.changelog.v1.1.3.xml','2016-06-15 21:17:54','2f2d42d3b88a45b1857590a0f80dc75','Custom SQL','',NULL,'1.9.5'),('KENMEI-1533_Erzeugung_Global_ID_fehlerhaft-MySQL','unknown','de/communardo/kenmei/database/update/v1.1.3/db.changelog.v1.1.3.xml','2016-06-15 21:17:53','5a199146dfd883297b1bfdb6ef6f517','SQL From File','',NULL,'1.9.2'),('KENMEI-1566-Remove_MySQL_Timestamp_Trigger','unknown','de/communardo/kenmei/database/update/v1.1.3/db.changelog.v1.1.3.xml','2016-06-15 21:17:53','a4fb25b5e12f2ab2519c961d47707617','Custom SQL','',NULL,'1.9.2'),('KENMEI-1578_Felder_key_und_value_in_Modellen_aendern_v2','unknown','de/communardo/kenmei/database/update/v1.2/db.changelog.v1.2.xml','2016-06-15 21:18:00','93d4a658b399a9dbd9f5499b3629cf1','Rename Column (x3)','',NULL,'1.9.5'),('KENMEI-1617_NullPointerException_bei_Zugriff_auf_Certificates','unknown','de/communardo/kenmei/database/update_2nd_pass/v1.1.4/db.changelog.v1.1.4.xml','2016-06-15 21:19:55','9af4a69dbe173a28caa8ea67321a9612','Custom Change','Add password for keystore if it doesn\'t exist.',NULL,'1.9.5'),('KENMEI-1672_Virus_scanner_in_admin_section','unknown','de/communardo/kenmei/database/update_2nd_pass/v1.1.4/db.changelog.v1.1.4.xml','2016-06-15 21:19:55','e99e6c9bad1fb4f2a4b5f15e30e2d681','Custom Change','Add enabled option for virus scanner',NULL,'1.9.5'),('KENMEI-1694_Follow_model_mysql','unknown','de/communardo/kenmei/database/update/v1.2/db.changelog.v1.2.xml','2016-06-15 21:18:05','cf2d46294da178b3cec5db11a59cee','Custom SQL, Add Primary Key, Add Foreign Key Constraint (x2), Add Primary Key, Add Foreign Key Constraint (x2)','',NULL,'1.9.5'),('KENMEI-1694_Follow-add_blog_globalIds','unknown','de/communardo/kenmei/database/update_2nd_pass/v1.2/db.changelog.v1.2.xml','2016-06-15 21:19:55','c79f0fa2747d5864dcf55aa98b5100','Custom Change','Create the global Id for blogs where there is none yet.',NULL,'1.9.5'),('KENMEI-1694_Follow-add_tag_globalIds','unknown','de/communardo/kenmei/database/update_2nd_pass/v1.2/db.changelog.v1.2.xml','2016-06-15 21:19:55','3ee19392e521fda2a8629f23c9baba98','Custom Change','Create the default blog for clients where there is none yet.',NULL,'1.9.5'),('KENMEI-1694_Follow-remove_tag_globalIds','unknown','de/communardo/kenmei/database/update_2nd_pass/v1.2/db.changelog.v1.2.xml','2016-06-15 21:19:55','5db46f07a5f2b465bc80d13d44398','Custom SQL','Remove global IDs from tags',NULL,'1.9.5'),('KENMEI-1694_Follow-support_for_tags-MySQL_v3','unknown','de/communardo/kenmei/database/update/v1.2/db.changelog.v1.2.xml','2016-06-15 21:18:05','eb3cd3e484968abfae07914643ba681','Drop Unique Constraint','',NULL,'1.9.5'),('KENMEI-1694_Follow-support_for_tags-MySQL_v4','unknown','de/communardo/kenmei/database/update/v1.2/db.changelog.v1.2.xml','2016-06-15 21:18:05','794bcbcb61d474af2dd2a1b946bde68','Drop Foreign Key Constraint','',NULL,'1.9.5'),('KENMEI-1694_Follow-support_for_tags-MySQL_v5','unknown','de/communardo/kenmei/database/update/v1.2/db.changelog.v1.2.xml','2016-06-15 21:18:05','6731ae54a2b9defa6af2780d3cff338','Drop Unique Constraint','',NULL,'1.9.5'),('KENMEI-1694_Follow-support_for_tags-MySQL_v6','unknown','de/communardo/kenmei/database/update/v1.2/db.changelog.v1.2.xml','2016-06-15 21:18:05','d697217de5b4c55543b0446c5514dccb','Drop Foreign Key Constraint','',NULL,'1.9.5'),('KENMEI-1857_Option_for_system_notes_per_blog','unknown','de/communardo/kenmei/database/update/v1.1.4/db.changelog.v1.1.4.xml','2016-06-15 21:17:58','3710cfcc8722190afbabf4cdfbd1c50','Add Column, Add Not-Null Constraint','KENMEI-1857 allow disabling of system notes per blog',NULL,'1.9.5'),('KENMEI-1901_Drop_news_tables','unknown','de/communardo/kenmei/database/update/v1.2/db.changelog.v1.2.xml','2016-06-15 21:18:06','e328a03e4b3621b07cd78ac5881b94dc','Drop Table (x3)','',NULL,'1.9.5'),('KENMEI-1979_Attachments_for_outgoing_mails_mysql','unknown','de/communardo/kenmei/database/update/v1.2/db.changelog.v1.2.xml','2016-06-15 21:18:05','fcf55ea391ee0a078551c12c38c761','Custom SQL','',NULL,'1.9.5'),('kenmei-1988-ramconnector-entfernen','unknown','de/communardo/kenmei/database/update/v1.3.3/db.changelog.v1.3.3.xml','2016-06-15 21:18:44','9569702cd7d0b74787b6ab783602989','Drop Table','',NULL,'1.9.5'),('KENMEI-2008_Ldap_External_Group_Additional_Props','unknown','de/communardo/kenmei/database/update/v1.2/db.changelog.v1.2.xml','2016-06-15 21:18:16','40578a21037635e7cd0434a3beaeac4','Add Column','',NULL,'1.9.5'),('KENMEI-2009_Finalize_changed_LDAP_Config','unknown','de/communardo/kenmei/database/update/v1.2/db.changelog.v1.2.xml','2016-06-15 21:18:12','45746a6edbf040d1eec5c559be13d4','Drop Column (x4), Add Not-Null Constraint','',NULL,'1.9.5'),('KENMEI-2009_Ldap_Config_SearchBase_list','unknown','de/communardo/kenmei/database/update/v1.2/db.changelog.v1.2.xml','2016-06-15 21:18:14','3c6bcc15811c64f0494474d31c35104c','Add Column','',NULL,'1.9.5'),('KENMEI-2009_Ldap_Config_User_UID','unknown','de/communardo/kenmei/database/update/v1.2/db.changelog.v1.2.xml','2016-06-15 21:18:13','382cc4905529ba9151032204e58dcee','Add Column, Add Not-Null Constraint','',NULL,'1.9.5'),('KENMEI-2009_Ldap_config-add_uid_to_mapping','unknown','de/communardo/kenmei/database/update_2nd_pass/v1.2/db.changelog.v1.2.xml','2016-06-15 21:19:55','4c44b01203e35fc2473fdafac8c5cc','Custom Change','Add UID to userSearch property mapping',NULL,'1.9.5'),('KENMEI-2009_Ldap_External_User_Auth_Additional_Props','unknown','de/communardo/kenmei/database/update/v1.2/db.changelog.v1.2.xml','2016-06-15 21:18:15','c92fd0704f4ac6551090f6d4ae43ada','Add Column, Custom SQL','',NULL,'1.9.5'),('KENMEI-2009_Migrate_LDAP_Config-MySQL','unknown','de/communardo/kenmei/database/update/v1.2/db.changelog.v1.2.xml','2016-06-15 21:18:09','96744914835013bcccd272c663ba2b8','Custom SQL','',NULL,'1.9.5'),('KENMEI-2009_Refactor_Group_Sync','unknown','de/communardo/kenmei/database/update/v1.2/db.changelog.v1.2.xml','2016-06-15 21:18:12','379ed5349921bcef239216c3d0b22920','Drop Column (x2)','',NULL,'1.9.5'),('KENMEI-2009_Tables_for_changed_LDAP_Config_mysql','unknown','de/communardo/kenmei/database/update/v1.2/db.changelog.v1.2.xml','2016-06-15 21:18:09','26c4a0b1e75df3286ed279cfc262998','Custom SQL, Add Foreign Key Constraint (x2), Add Column','',NULL,'1.9.5'),('KENMEI-2104_Ldap_Passwort_verschluesseln','unknown','de/communardo/kenmei/database/update_2nd_pass/v1.2/db.changelog.v1.2.xml','2016-06-15 21:19:55','f472fa9537c62bf44ffbd73b1ffb6ad','Custom Change','encrypts the LDAP manager password',NULL,'1.9.5'),('KENMEI-2113_Performanzoptimierung_Direct_Message_migrate','unknown','de/communardo/kenmei/database/update/v1.2/db.changelog.v1.2.xml','2016-06-15 21:18:18','b35b34b640585fac11e4a5df10dfa','Custom SQL','',NULL,'1.9.5'),('KENMEI-2113_Performanzoptimierung_Direct_Message_mysql','unknown','de/communardo/kenmei/database/update/v1.2/db.changelog.v1.2.xml','2016-06-15 21:18:18','d521a632fab7d957ad853854ea48694','Custom SQL, Add Primary Key, Add Foreign Key Constraint (x2)','',NULL,'1.9.5'),('KENMEI-2154_Jobs_Services_als_Tasks_in_Datenbank_mysql','unknown','de/communardo/kenmei/database/update/v1.3/db.changelog.v1.3.xml','2016-06-15 21:18:24','d1db1d661338d345aab613ed1f94ec9a','Custom SQL (x3), Add Foreign Key Constraint (x2)','',NULL,'1.9.5'),('KENMEI-2154_Jobs_Services_als_Tasks_in_Datenbank-Rename-Columns','unknown','de/communardo/kenmei/database/update/v1.3/db.changelog.v1.3.xml','2016-06-15 21:18:29','26b5c92cd2dc3debe146334576775857','Rename Column (x2)','',NULL,'1.9.5'),('KENMEI-2154-Jobs_Services_als_Tasks_in_Datenbank_v2','unknown','de/communardo/kenmei/database/update_2nd_pass/v1.3/db.changelog.v1.3.xml','2016-06-15 21:19:56','627db31b9a285c1a388d525db74626','Custom Change','Updating existing jobs.',NULL,'1.9.5'),('KENMEI-2240_discussionId_befuellen-MySQL','unknown','de/communardo/kenmei/database/update/v1.3/db.changelog.v1.3.xml','2016-06-15 21:18:32','89ee14e79e5c6e8054bff3e64e3d5c49','Custom SQL','',NULL,'1.9.5'),('KENMEI-2240_Spalte_discussionId_in_core_note','unknown','de/communardo/kenmei/database/update/v1.3/db.changelog.v1.3.xml','2016-06-15 21:18:31','bedafeee1e858694c28879e61a2c5b26','Add Column','',NULL,'1.9.5'),('KENMEI-2261-LDAP_Authentication_SASL_Support','unknown','de/communardo/kenmei/database/update/v1.3/db.changelog.v1.3.xml','2016-06-15 21:18:34','66db577a2f9c6ee89bdfa2c4848b117f','Add Column','',NULL,'1.9.5'),('KENMEI-2287_StartTLS_1','unknown','de/communardo/kenmei/database/update/v1.2/db.changelog.v1.2.xml','2016-06-15 21:18:20','c629b7e26c3494b0c7ebf583ecce7fdd','Insert Row','',NULL,'1.9.5'),('KENMEI-2288_remove_old_caching_properties','unknown','de/communardo/kenmei/database/update/v1.3/db.changelog.v1.3.xml','2016-06-15 21:18:36','93ac2c8652ac9ce97b5682a4a85cc0','Custom SQL','',NULL,'1.9.5'),('KENMEI-2295_Create_personal_blog_mysql','unknown','de/communardo/kenmei/database/update/v1.2/db.changelog.v1.2.xml','2016-06-15 21:18:20','d60537d23ca7d5d226eb2eda3a2e18','Custom SQL','',NULL,'1.9.5'),('KENMEI-2302_Index_on_core_role2blog','unknown','de/communardo/kenmei/database/update/v1.2/db.changelog.v1.2.xml','2016-06-15 21:18:19','83576031a02b3bc36cbc918a0c59d79','Create Index (x2)','',NULL,'1.9.5'),('KENMEI-2315_mediumblob_for_global_binary_property_2','unknown','de/communardo/kenmei/database/update/v1.3/db.changelog.v1.3.xml','2016-06-15 21:18:37','8677bcb553814bf24c8fb3f41bb82ef7','Custom SQL','',NULL,'1.9.5'),('KENMEI-2335_UniqueJabberResourceIdPerCluster','unknown','de/communardo/kenmei/database/update/v1.2/db.changelog.v1.2.xml','2016-06-15 21:18:19','c298eab7d736523bb2c86346674dabc','Custom SQL','',NULL,'1.9.5'),('KENMEI-2389_StartTLS_1','unknown','de/communardo/kenmei/database/update/v1.2/db.changelog.v1.2.xml','2016-06-15 21:18:19','c8fe21bfa1ed5b39dc80e84e83a8aa67','Insert Row','',NULL,'1.9.5'),('KENMEI-2389_StartTLS_2','unknown','de/communardo/kenmei/database/update/v1.2/db.changelog.v1.2.xml','2016-06-15 21:18:19','d5f814f6a5ec5bf9eb501bdbc41c6d41','Custom SQL','',NULL,'1.9.5'),('KENMEI-2465_Index_for_better_performance_mysql','unknown','de/communardo/kenmei/database/update/v1.3.1/db.changelog.v1.3.1.xml','2016-06-15 21:18:38','64af9b8975bff0a14f4c95fa6c94715','Custom SQL','',NULL,'1.9.5'),('KENMEI-2510_confluence_passwort_verschluesseln','unknown','de/communardo/kenmei/database/update_2nd_pass/v1.2/db.changelog.v1.2.xml','2016-06-15 21:19:56','adf43a30d6bf756fefc6cfb243f428','Custom Change','encrypts the Confluence administrator password',NULL,'1.9.5'),('KENMEI-2510_sharepoint_passwort_verschluesseln','unknown','de/communardo/kenmei/database/update_2nd_pass/v1.2/db.changelog.v1.2.xml','2016-06-15 21:19:56','80ddc2197d1d616a48d012e6fb802452','Custom Change','encrypts the Sharepoint administrator password',NULL,'1.9.5'),('KENMEI-2601_Index-fuer-discussion_id-anlegen','UNKNOWN','de/communardo/kenmei/database/update/v1.3.2/db.changelog.v1.3.2.xml','2016-06-15 21:18:41','a6483cd076bdafe25975ac34fcfc3384','Create Index','',NULL,'1.9.5'),('KENMEI-2601_Index-fuer-discussion_id-entfernen','UNKNOWN','de/communardo/kenmei/database/update/v1.3.2/db.changelog.v1.3.2.xml','2016-06-15 21:18:41','4ae911eeae58366bebf8ae724282571a','Drop Index','',NULL,'1.9.5'),('KENMEI-2608-Refactor MostUsedBlogs','unknown','de/communardo/kenmei/database/update/v1.3.3/db.changelog.v1.3.3.xml','2016-06-15 21:18:44','f64386ffc88d3fef2b45eb268f6769b5','Drop Table','',NULL,'1.9.5'),('KENMEI-2616_Index_auf_Tag_lower_name_anlegen_mysql','UNKNOWN','de/communardo/kenmei/database/update/v1.3.2/db.changelog.v1.3.2.xml','2016-06-15 21:18:44','8911ae8ae2fdc6b26465e7fc86c3e756','Custom SQL','',NULL,'1.9.5'),('KENMEI-2628_Remove_Wrong_Unique_Constraint','unknown','de/communardo/kenmei/database/update/v1.2/db.changelog.v1.2.xml','2016-06-15 21:18:21','b02c2845ed23257c053a0c26cd8dcb4','Custom SQL, Add Unique Constraint','',NULL,'1.9.5'),('KENMEI-2629-Reset-Primary-Auth-on-deactivated-external-auth','unknown','de/communardo/kenmei/database/update/v1.3.3/db.changelog.v1.3.3.xml','2016-06-15 21:18:44','d9932ed049603f4d816789d0454a285a','Update Data (x2)','',NULL,'1.9.5'),('KENMEI-3005_change_property_value_types_mysql','UNKNOWN','de/communardo/kenmei/database/update/v2.1/db.changelog.v2.1.1.xml','2016-06-15 21:19:07','387a584a9d608155cc939c734e2c692','Custom SQL','',NULL,'1.9.5'),('KENMEI-3005_drop_not_null_constraints_mysql','UNKNOWN','de/communardo/kenmei/database/update/v2.1/db.changelog.v2.1.1.xml','2016-06-15 21:19:13','c6a62aed1dfac74868713b6ff2e66553','Custom SQL','',NULL,'1.9.5'),('KENMEI-3005_fix_column_name','UNKNOWN','de/communardo/kenmei/database/update/v2.1/db.changelog.v2.1.1.xml','2016-06-15 21:19:00','141350baf72f232673fde54fa9a863aa','Rename Column','',NULL,'1.9.5'),('KENMEI-3005_fix_column_name_mysql','Communote','de/communardo/kenmei/database/update/v2.1/db.changelog.v2.1.1.xml','2016-06-15 21:19:02','105a33b82385d8c2a3d8969d2714f5','Rename Column','',NULL,'1.9.5'),('KENMEI-3005_fix_large_table_name','Commuote','de/communardo/kenmei/database/update/v2.1/db.changelog.v2.1.1.xml','2016-06-15 21:19:02','c8ae46c35b97d7c6ba91bd77458367','Rename Table','',NULL,'1.9.5'),('KENMEI-3005_rename_tables_new','UNKNOWN','de/communardo/kenmei/database/update/v2.1/db.changelog.v2.1.1.xml','2016-06-15 21:19:08','e6b3d3595e3c557cc17e79983a14515','Rename Table (x3)','',NULL,'1.9.5'),('KENMEI-3005_rename_tables_v2','UNKNOWN','de/communardo/kenmei/database/update/v2.1/db.changelog.v2.1.1.xml','2016-06-15 21:19:13','95325bf04ee3b01dc3f9fdbf25f28f3a','Rename Table','',NULL,'1.9.5'),('KENMEI-3005_set_null_mysql','UNKNOWN','de/communardo/kenmei/database/update/v2.1/db.changelog.v2.1.1.xml','2016-06-15 21:19:13','3825c215165854fcba8a2cbfb8e882fc','Custom SQL','',NULL,'1.9.5'),('KENMEI-3719_2','unknown','de/communardo/kenmei/database/update/v1.3.3/db.changelog.v1.3.3.xml','2016-06-15 21:18:45','f23f1e2a63e54359e668ff1c60de1e','Add Unique Constraint','',NULL,'1.9.5'),('KENMEI-3719_clean_mysql','unknown','de/communardo/kenmei/database/update/v1.3.3/db.changelog.v1.3.3.xml','2016-06-15 21:18:44','a6665afafbce9c511211339b5c13be','Custom SQL','',NULL,'1.9.5'),('KENMEI-3762-Drop-Unique-LANGUAGE_FK_2','unknown','de/communardo/kenmei/database/update/v1.3.3/db.changelog.v1.3.3.xml','2016-06-15 21:18:45','ba3c125efb69ec43ae145829fdab2ce','Drop Unique Constraint','',NULL,'1.9.5'),('KENMEI-3838_Domain_Controller_LDAP_Server_automatisch_ermitteln','Communote','de/communardo/kenmei/database/update/v2.2/db.changelog.v2.2.xml','2016-06-15 21:19:20','3e644577adfac5759ae154e4497737d','Add Column','',NULL,'1.9.5'),('KENMEI-3838_Set_Dynamic_Mode_to_false','Communote','de/communardo/kenmei/database/update/v2.2/db.changelog.v2.2.xml','2016-06-15 21:19:20','ea36cdad1cb713fb847563ab187b665d','Update Data','',NULL,'1.9.5'),('KENMEI-3997_Ausschalten_des_Features_Selbstregistrierung','Commuote','de/communardo/kenmei/database/update/v2.1/db.changelog.v2.1.1.xml','2016-06-15 21:18:57','35adb7e64b6c407b9877a3215362c3ef','Add Column, Add Foreign Key Constraint','',NULL,'1.9.5'),('KENMEI-3997_Selbstregistrierung_2','Commuote','de/communardo/kenmei/database/update/v2.1/db.changelog.v2.1.1.xml','2016-06-15 21:19:00','12e56d5965b4e7f44e9a7bc7892bbf46','Add Column','',NULL,'1.9.5'),('KENMEI-3997_Selbstregistrierung_mysql','Commuote','de/communardo/kenmei/database/update/v2.1/db.changelog.v2.1.1.xml','2016-06-15 21:18:58','a2922424654fe2fbc761ef9997a26f7c','Add Column, Add Foreign Key Constraint','',NULL,'1.9.5'),('KENMEI-3997_Selbstregistrierung_remove2','Commuote','de/communardo/kenmei/database/update/v2.1/db.changelog.v2.1.1.xml','2016-06-15 21:18:59','eabe999544ee2285adc23e6c59c247','Drop Column','',NULL,'1.9.5'),('KENMEI-3997_Selbstregistrierung_remove2_mysql','Commuote','de/communardo/kenmei/database/update/v2.1/db.changelog.v2.1.1.xml','2016-06-15 21:18:58','7550398b33bed1ff48c411722c566ebf','Drop Foreign Key Constraint','',NULL,'1.9.5'),('KENMEI-4017-Add_Tag_Store_information','UNKNOWN','de/communardo/kenmei/database/update/v2.1/db.changelog.v2.1.1.xml','2016-06-15 21:18:55','ed6fd58dac11fe96a55b96cb2a12b83e','Modify Column, Rename Column, Add Column','',NULL,'1.9.5'),('KENMEI-4017-Add_Tag_Store_information_2_add_index','UNKNOWN','de/communardo/kenmei/database/update/v2.1/db.changelog.v2.1.1.xml','2016-06-15 21:18:57','7a1ce9db257c89aa7aacd4125416230','Add Unique Constraint','',NULL,'1.9.5'),('KENMEI-4017-Add_Tag_Store_information_2_mysql','UNKNOWN','de/communardo/kenmei/database/update/v2.1/db.changelog.v2.1.1.xml','2016-06-15 21:18:56','f15aff2d90a3c80cab7e3d285b27034','Drop Unique Constraint, Rename Column','',NULL,'1.9.5'),('KENMEI-4017-Add_Tag_Store_information_2_not_mysql','UNKNOWN','de/communardo/kenmei/database/update/v2.1/db.changelog.v2.1.1.xml','2016-06-15 21:18:56','f13bc14d5a9d781ca41e2c3d0456e2','Drop Unique Constraint, Rename Column','',NULL,'1.9.5'),('KENMEI-4017-Create_entity_to_tag_association','UNKNOWN','de/communardo/kenmei/database/update/v2.1/db.changelog.v2.1.1.xml','2016-06-15 21:18:45','93ecae16b48c48e51cbc4b317dffa7cf','Create Table','',NULL,'1.9.5'),('KENMEI-4017-Create_entity_to_tag_association_Constraints','UNKNOWN','de/communardo/kenmei/database/update/v2.1/db.changelog.v2.1.1.xml','2016-06-15 21:18:47','f5b4027ca2a47f2ad953c6b7b8aa0b3','Create Index, Add Foreign Key Constraint (x2)','',NULL,'1.9.5'),('KENMEI-4017-Create_tag2descriptions','UNKNOWN','de/communardo/kenmei/database/update/v2.1/db.changelog.v2.1.1.xml','2016-06-15 21:18:51','d555bf5d16beaec8a1224b7efece5e5','Create Table','',NULL,'1.9.5'),('KENMEI-4017-Create_tag2descriptions_Constraints','UNKNOWN','de/communardo/kenmei/database/update/v2.1/db.changelog.v2.1.1.xml','2016-06-15 21:18:53','c02defbee8416feb3bce5f405fdfe9','Add Foreign Key Constraint (x2), Add Unique Constraint','',NULL,'1.9.5'),('KENMEI-4017-Create_tag2names','UNKNOWN','de/communardo/kenmei/database/update/v2.1/db.changelog.v2.1.1.xml','2016-06-15 21:18:48','dde5e06d91cd71ddb96a3dbd5dfcbaaa','Create Table','',NULL,'1.9.5'),('KENMEI-4017-Create_tag2names_Constraints','UNKNOWN','de/communardo/kenmei/database/update/v2.1/db.changelog.v2.1.1.xml','2016-06-15 21:18:50','80ff97a3048ada5f861cce2e96c321a','Add Foreign Key Constraint (x2), Add Unique Constraint','',NULL,'1.9.5'),('KENMEI-4153_activate_default_blog_mysql','UNKNOWN','de/communardo/kenmei/database/update/v2.1/db.changelog.v2.1.1.xml','2016-06-15 21:18:57','9879efed9f9359f546cf5e23ec5cb599','Custom SQL','',NULL,'1.9.5'),('KENMEI-4153_mysql','UNKNOWN','de/communardo/kenmei/database/update/v2.1/db.changelog.v2.1.1.xml','2016-06-15 21:18:57','cc193968718994cca997435da8e58d','Custom SQL','',NULL,'1.9.5'),('KENMEI-4164-Dem Default Blog einen Blognamen zuweisen_all','Communote','de/communardo/kenmei/database/update/v2.1/db.changelog.v2.1.1.xml','2016-06-15 21:19:13','b13eb5fd90ccf643748dbafdbffc9ac','Custom SQL','',NULL,'1.9.5'),('KENMEI-4164-Dem Default Blog einen Blognamen zuweisen_mysql','Communote','de/communardo/kenmei/database/update/v2.1/db.changelog.v2.1.1.xml','2016-06-15 21:19:13','6f629ef8ac4755728c48e846210194b','Custom SQL','',NULL,'1.9.5'),('KENMEI-4216_alter_anonymized_user_prefix_mysql','Communote','de/communardo/kenmei/database/update/v2.1/db.changelog.v2.1.1.xml','2016-06-15 21:19:13','75d65972287d339ed2ae48afe64280a1','Custom SQL','',NULL,'1.9.5'),('KENMEI-4357-TagCloud-bei-Nutzerübersicht-geht-nicht_mysql','Communote','de/communardo/kenmei/database/update/v2.1/db.changelog.v2.1.1.xml','2016-06-15 21:19:04','18f9a77c21bb211b85566f72fb1c4','Add Not-Null Constraint (x2)','',NULL,'1.9.5'),('KENMEI-4488-Sprache an ext Tags nicht aktualisiert_mysql_2','UNKNOWN','de/communardo/kenmei/database/update/v2.1/db.changelog.v2.1.1.xml','2016-06-15 21:19:13','b0c48232793c6e44441b3cb63aff2959','Custom SQL','',NULL,'1.9.5'),('KENMEI-4774_Column_for_application_property','Communote','de/communardo/kenmei/database/update/v2.2/db.changelog.v2.2.xml','2016-06-15 21:19:15','a7db319957f9d9988b0156db5ebb937','Add Column','',NULL,'1.9.5'),('KENMEI-4774_Eigenschaften_fuer_Plugins','Communote','de/communardo/kenmei/database/update/v2.2/db.changelog.v2.2.xml','2016-06-15 21:19:14','60d6e794152c2bc11a3ce93698f988','Create Table','',NULL,'1.9.5'),('KENMEI-4774_Eigenschaften_fuer_Plugins_autoincrement','Communote','de/communardo/kenmei/database/update/v2.2/db.changelog.v2.2.xml','2016-06-15 21:19:14','81f0b960abbbc59752766b1a9c27eaf','Set Column as Auto-Increment','',NULL,'1.9.5'),('KENMEI-4774_Eigenschaften_fuer_Plugins_unique','Communote','de/communardo/kenmei/database/update/v2.2/db.changelog.v2.2.xml','2016-06-15 21:19:14','4d6666ef5d19164f3b101864397bccef','Add Unique Constraint','',NULL,'1.9.5'),('KENMEI-4781_Refactoring_von_ExternalObjectManagement_v2','Communote','de/communardo/kenmei/database/update/v2.2/db.changelog.v2.2.xml','2016-06-15 21:19:19','ff956eeef12bf22171df6ebd4168865','Modify Column (x2), Add Column (x2), Update Data (x2), Add Not-Null Constraint (x2)','',NULL,'1.9.5'),('KENMEI-4817-Follow-optimieren-Create-Index-2','Communote','de/communardo/kenmei/database/update/v2.2/db.changelog.v2.2.xml','2016-06-15 21:19:23','bdedb6b88c186a7232aee1c0cef8220','Create Index (x2)','',NULL,'1.9.5'),('KENMEI-4846-Remove_with_time_zone_v3','Communote','de/communardo/kenmei/database/update/v2.3/db.changelog.v2.3.2.xml','2016-06-15 21:19:32','f44aaeee258561aca1e465a11322687','Custom SQL','',NULL,'1.9.5'),('KENMEI-4931_Thema einsehbar_obwohl_keine_Berechtigung_mysql_2','Communote','de/communardo/kenmei/database/update/v2.2/db.changelog.v2.2.xml','2016-06-15 21:19:22','c781a3052b31f73e6c4a1dfe26a9b3','SQL From File','',NULL,'1.9.5'),('KENMEI-4xxx_MQ','Communote','de/communardo/kenmei/database/update_2nd_pass/v2.2/db.changelog.v2.2.xml','2016-06-15 21:19:56','fed0868ba054bcdcb65459b0e92f5f2','Custom Change','Add password for keystore if it doesn\'t exist.',NULL,'1.9.5'),('KENMEI-5019_UniqueConstraint_ExternalGroups_mysql','Communote','de/communardo/kenmei/database/update/v2.2/db.changelog.v2.2.xml','2016-06-15 21:19:22','6f12557e2359a6c725171aa05ded6e','Modify Column, Add Unique Constraint','',NULL,'1.9.5'),('KENMEI-5034-Angemeldet_bleiben_LDAP','Communote','de/communardo/kenmei/database/update/v2.3/db.changelog.v2.3.2.xml','2016-06-15 21:19:31','62806cf1a8b837291c472ff30fd7798','Add Column','',NULL,'1.9.5'),('KENMEI-5243-Comparison of external id must be case sensitive','Communote','de/communardo/kenmei/database/update/v2.2/db.changelog.v2.2.xml','2016-06-15 21:19:24','6411b1a698135e7aad29d3816e2e4ff','Custom SQL','',NULL,'1.9.5'),('KENMEI-5395-LastDiscussionNoteCreationDate-einfuegen','Communote','de/communardo/kenmei/database/update/v2.3/db.changelog.v2.3.xml','2016-06-15 21:19:25','7db328c1f9ba177cab22cef6b335a3f8','Add Column','',NULL,'1.9.5'),('KENMEI-5416_Setzen_LastDiscussionNoteCreationDate_mysql_4','Communote','de/communardo/kenmei/database/update/v2.3/db.changelog.v2.3.xml','2016-06-15 21:19:25','ad415c7e539ea7d7e11370fabc9371d2','Custom SQL','',NULL,'1.9.5'),('KENMEI-5455_Alternative_Methode_fuer_Nachladender_Notes','Communote','de/communardo/kenmei/database/update/v2.3/db.changelog.v2.3.1.xml','2016-06-15 21:19:25','65d83c24ca415f4d6b533d05163c022','Custom SQL','',NULL,'1.9.5'),('KENMEI-5524_1_1','Communote','de/communardo/kenmei/database/update/v2.3/db.changelog.v2.3.1.xml','2016-06-15 21:19:25','78b6368bd5b2ed7ce6adc0992b709586','Create Table','',NULL,'1.9.5'),('KENMEI-5524_1_2','Communote','de/communardo/kenmei/database/update/v2.3/db.changelog.v2.3.1.xml','2016-06-15 21:19:27','b3f6fd8aa13f6553ae28b334d5a50f7','Add Foreign Key Constraint (x2)','',NULL,'1.9.5'),('KENMEI-5524_4','Communote','de/communardo/kenmei/database/update/v2.3/db.changelog.v2.3.1.xml','2016-06-15 21:19:27','3aa7788ac8fbc97db9157b9963bba96e','Set Column as Auto-Increment','',NULL,'1.9.5'),('KENMEI-5524_5','Communote','de/communardo/kenmei/database/update/v2.3/db.changelog.v2.3.1.xml','2016-06-15 21:19:28','d29af978c3eaf6beb1b5d925dab827','Add Unique Constraint','',NULL,'1.9.5'),('KENMEI-5524_6','Communote','de/communardo/kenmei/database/update/v2.3/db.changelog.v2.3.1.xml','2016-06-15 21:19:29','7cedf3fcefd676807524cce4973bf329','Add Column, Add Foreign Key Constraint','',NULL,'1.9.5'),('KENMEI-5555_Anonymisieren_Nutzer_mit_vielen_Notes_sehr_lange','Communote','de/communardo/kenmei/database/update/v2.3/db.changelog.v2.3.2.xml','2016-06-15 21:19:33','1230ad57dbf563db0a680e9f779d8','Create Index (x2)','',NULL,'1.9.5'),('KENMEI-5562_Index_last_discussion_creation_date','Communote','de/communardo/kenmei/database/update/v2.3/db.changelog.v2.3.1.xml','2016-06-15 21:19:29','b0299e1c45133f98f5bb4f6235a78a','Custom SQL','',NULL,'1.9.5'),('KENMEI-5563-TIMESTAMP-without-TimeZone_mysql','Communote','de/communardo/kenmei/database/update/v2.3/db.changelog.v2.3.1.xml','2016-06-15 21:19:30','5d884fb70b4d6c174cc95c7212fb8f6','Custom SQL','',NULL,'1.9.5'),('KENMEI-5657-Case_Sensitive_for_external_user_id','Communote','de/communardo/kenmei/database/update/v2.3/db.changelog.v2.3.2.xml','2016-06-15 21:19:32','2ebf3c02c558adbf1a95d1555737f27','Custom SQL','',NULL,'1.9.5'),('KENMEI-5742_Packagestruktur_aufraeumen_Tasks_v2','Communote','de/communardo/kenmei/database/update/v2.4/db.changelog.v2.4.xml','2016-06-15 21:19:33','16bfbfd1c0f47030b4da5777c3f672','Custom SQL','',NULL,'1.9.5'),('KENMEI-5828-Localized-Email-imprint_terms','Communote','de/communardo/kenmei/database/update/v2.4/db.changelog.v2.4.xml','2016-06-15 21:19:33','a787203f141bd68c1913ddaffeb1b6b','Custom SQL','',NULL,'1.9.5'),('KENMEI-5828-Localized-Email-Signature_delete_old','Communote','de/communardo/kenmei/database/update/v2.4/db.changelog.v2.4.xml','2016-06-15 21:19:33','8bcc4071f291a6ca8f748e932debc26','Custom SQL','',NULL,'1.9.5'),('KENMEI-5828-Localized-Email-Signature_mssql_mysql','Communote','de/communardo/kenmei/database/update/v2.4/db.changelog.v2.4.xml','2016-06-15 21:19:33','b7c7d43fe8b1ab22907d5b8ece8499bd','Custom SQL','',NULL,'1.9.5'),('KENMEI-5859_Neuer_Inhaltstyp_Anhaenge_mssql_mysql','Communote','de/communardo/kenmei/database/update/v2.4/db.changelog.v2.4.xml','2016-06-15 21:19:33','c57d8f192b1c8e2fe2dfd0dc9930619','Custom SQL','',NULL,'1.9.5'),('KENMEI-5923_Benachrichtung_Themen_und_Diskussionen','Communote','de/communardo/kenmei/database/update/v2.4/db.changelog.v2.4.xml','2016-06-15 21:19:37','4189499ff788adfaac769c0d75e5f','Add Column','',NULL,'1.9.5'),('KENMEI-5923_Benachrichtung_Themen_und_Diskussionen_default_v2','Communote','de/communardo/kenmei/database/update/v2.4/db.changelog.v2.4.xml','2016-06-15 21:19:37','ae34de50903a953ce6a580302c2dac','Update Data','',NULL,'1.9.5'),('KENMEI-6098-Bild-Filter-zeigt-Nachrichten-ohne-Bild_mysql','Communote','de/communardo/kenmei/database/update/v2.4/db.changelog.v2.4.xml','2016-06-15 21:19:37','9ec45dc6bf1285faac1a27e6790d2bc','Custom SQL','',NULL,'1.9.5'),('KENMEI-6122-Add_missing_index','Communote','de/communardo/kenmei/database/update/v2.4/db.changelog.v2.4.xml','2016-06-15 21:19:37','4f7bc98c7c74ba4ab5cae9873ab19','Create Index','',NULL,'1.9.5'),('KENMEI-6192_Themenhierarchien_Umsetzung_blog2blog','Communote','de/communardo/kenmei/database/update/v3.x/db.changelog.v3.0.xml','2016-06-15 21:19:38','ffe2ff18c648d3e68139433d1fa8da8f','Create Table','',NULL,'1.9.5'),('KENMEI-6192_Themenhierarchien_Umsetzung_blog2blog_resolved','Communote','de/communardo/kenmei/database/update/v3.x/db.changelog.v3.0.xml','2016-06-15 21:19:38','79cb9a52016696d76b8219e79d1874f','Create Table','',NULL,'1.9.5'),('KENMEI-6192_Themenhierarchien_Umsetzung_constraint_2','Communote','de/communardo/kenmei/database/update/v3.x/db.changelog.v3.0.xml','2016-06-15 21:19:39','42f59fa11a3443dc208225977a1ed8','Add Unique Constraint','',NULL,'1.9.5'),('KENMEI-6192_Themenhierarchien_Umsetzung_foreign_keys','Communote','de/communardo/kenmei/database/update/v3.x/db.changelog.v3.0.xml','2016-06-15 21:19:39','52e253dadf622f36c1a81be4dc46fe','Add Foreign Key Constraint (x2)','',NULL,'1.9.5'),('KENMEI-6192_Themenhierarchien_Umsetzung_toplevel_flag','Communote','de/communardo/kenmei/database/update/v3.x/db.changelog.v3.0.xml','2016-06-15 21:19:40','acaeb509982a5828c99841b1ccaa546','Add Column','',NULL,'1.9.5'),('KENMEI-6256-NavigationItems','Communote','de/communardo/kenmei/database/update/v3.x/db.changelog.v3.0.xml','2016-06-15 21:19:41','f36ce97067d97f5cf0892041b9bf15f7','Create Table','',NULL,'1.9.5'),('KENMEI-6256-NavigationItems_add_name','Communote','de/communardo/kenmei/database/update/v3.x/db.changelog.v3.0.xml','2016-06-15 21:19:43','d884929523cd4d419f67af45537892a','Add Column','',NULL,'1.9.5'),('KENMEI-6256-NavigationItems_foreign_keys','Communote','de/communardo/kenmei/database/update/v3.x/db.changelog.v3.0.xml','2016-06-15 21:19:41','7fa1bdfeec2113f5f29f448b918f20b6','Add Foreign Key Constraint','',NULL,'1.9.5'),('KENMEI-6256-NavigationItems_rename_index','Communote','de/communardo/kenmei/database/update/v3.x/db.changelog.v3.0.xml','2016-06-15 21:19:42','33d323521e9aa43155c9504ccd4134','Rename Column','',NULL,'1.9.5'),('KENMEI-6270-ClientAktivierung-Make-The-Git-Hash-Work','Communote','de/communardo/kenmei/database/update/v3.x/db.changelog.v3.0.xml','2016-06-15 21:19:44','1e5cd6666e7728841c3453ebd29043b9','Modify Column','',NULL,'1.9.5'),('KENMEI-6543-Create_BuiltIn_NaviItems','Communote','de/communardo/kenmei/database/update_2nd_pass/v3.x/db.changelog.v3.0.xml','2016-06-15 21:19:56','ff6b36f04575f9aa72335b2310e36087','Custom Change','Add built-in navigation items',NULL,'1.9.5'),('KENMEI-7154-Terms-Of_Use_1-mysql','Communote','de/communardo/kenmei/database/update/v3.x/db.changelog.v3.2.xml','2016-06-15 21:19:47','81c7bfc2e874aaf8b6e54bdda15548','Custom SQL','',NULL,'1.9.5'),('KENMEI-7154-Terms-Of_Use_2-mysql','Communote','de/communardo/kenmei/database/update/v3.x/db.changelog.v3.2.xml','2016-06-15 21:19:47','40b1a9a75921616d54a0efdab723d8f2','Custom SQL','',NULL,'1.9.5'),('KENMEI-7199-Drop-SharePoint-Configuration','Communote','de/communardo/kenmei/database/update/v3.x/db.changelog.v3.2.xml','2016-06-15 21:19:47','dc9630efe277bfd22645bcac93646ec','Drop Table, Custom SQL','',NULL,'1.9.5'),('KENMEI-7369-Drop-CRC-Configuration','Communote','de/communardo/kenmei/database/update/v3.x/db.changelog.v3.3.xml','2016-06-15 21:19:47','be4e654ee8abde97480e229775678a7','Drop Table (x2)','',NULL,'1.9.5'),('KENMEI-7383-Index-for-Attachment-ContentId_mysql','Communote','de/communardo/kenmei/database/update/v3.x/db.changelog.v3.3.xml','2016-06-15 21:19:50','754efb261d3ee3a9208bac41c581b4d1','Modify Column, Add Unique Constraint','',NULL,'1.9.5'),('KENMEI-7385-Crawl-Last-Modification-Date','Communote','de/communardo/kenmei/database/update/v3.x/db.changelog.v3.3.xml','2016-06-15 21:19:51','5dbf4f53deff5ccee7175ce559252c7','Add Column','',NULL,'1.9.5'),('KENMEI-7385-Crawl-Last-Modification-Date-Not-Null','Communote','de/communardo/kenmei/database/update/v3.x/db.changelog.v3.3.xml','2016-06-15 21:19:51','81be19ddddc7fa6b719d8a3d6f0889f','Modify Column','',NULL,'1.9.5'),('KENMEI-7386-Crawl-Last-Modification-Date-Note','Communote','de/communardo/kenmei/database/update/v3.x/db.changelog.v3.3.xml','2016-06-15 21:19:52','ed93ec5d51918c57aaf0415a1228ee88','Add Column','',NULL,'1.9.5'),('KENMEI-7386-Crawl-Last-Modification-Date-Note-Not-Null','Communote','de/communardo/kenmei/database/update/v3.x/db.changelog.v3.3.xml','2016-06-15 21:19:52','eaddef8b311ed12935ccfdb45b866f3','Modify Column','',NULL,'1.9.5'),('KENMEI-7392-Properties-for-Attachments_autoincrement-v2','Communote','de/communardo/kenmei/database/update/v3.x/db.changelog.v3.3.xml','2016-06-15 21:19:49','ab64a867e9322bcc61849e75fafa311b','Set Column as Auto-Increment','',NULL,'1.9.5'),('KENMEI-7392-Properties-for-Attachments_fkc_2-v2','Communote','de/communardo/kenmei/database/update/v3.x/db.changelog.v3.3.xml','2016-06-15 21:19:48','6553f6171bbb6e5d74ccd51a723fdc40','Add Foreign Key Constraint','',NULL,'1.9.5'),('KENMEI-7392-Properties-for-Attachments-v2','Communote','de/communardo/kenmei/database/update/v3.x/db.changelog.v3.3.xml','2016-06-15 21:19:47','cd90d6cdfddc6fd3132373d6ea1d1ea9','Create Table','',NULL,'1.9.5'),('KENMEI-7523_remove_old_jobs','Communote','com/communote/server/liquibase/update/v3.x/db.changelog.v3.4.xml','2016-06-15 21:19:53','d38b603a8ea8cde9543cb7ebba9ea0fc','Delete Data','',NULL,'1.9.5'),('KENMEI-7524_remove_license_mode_subscription','Communote','com/communote/server/liquibase/update/v3.x/db.changelog.v3.4.xml','2016-06-15 21:19:53','716e958a59e65ba079b9ab4b2c4cb5c','Drop Table','',NULL,'1.9.5'),('KENMEI-7524_remove_license_tables_mysql','Communote','com/communote/server/liquibase/update/v3.x/db.changelog.v3.4.xml','2016-06-15 21:19:53','43cafc64959af3a1fc10ea4aac5efc4','Custom SQL','',NULL,'1.9.5'),('KENMEI-7537_act_code_mysql','Communote','com/communote/server/liquibase/update/v3.x/db.changelog.v3.4.xml','2016-06-15 21:19:54','7e18c3458664cf7cf06a441c589637','Custom SQL','',NULL,'1.9.5'),('KENMEI-7537_cleanup_client_table','Communote','com/communote/server/liquibase/update/v3.x/db.changelog.v3.4.xml','2016-06-15 21:19:53','d7a8f6e553ad6b021bf0f361f2bf','Drop Column','',NULL,'1.9.5'),('KENMEI-7537_cleanup_client_tables_mysql','Communote','com/communote/server/liquibase/update/v3.x/db.changelog.v3.4.xml','2016-06-15 21:19:54','bbb9d2f55ab1cc667fe81d6ff7ba69','Drop Foreign Key Constraint (x3), Drop Column (x4), Drop Table (x6)','',NULL,'1.9.5'),('KENMEI-7547_modification_time_of_setting','Communote','com/communote/server/liquibase/update/v3.x/db.changelog.v3.4.xml','2016-06-15 21:19:54','78c1f960fb942e58e57baf7ebcf9a41','Add Column','',NULL,'1.9.5'),('KENMEI-Binary-Properties','unknown','de/communardo/kenmei/database/update/v1.3/db.changelog.v1.3.xml','2016-06-15 21:18:27','901bbfc8af68f655b2185ac26d7b126','Create Table','',NULL,'1.9.5'),('KENMEI-Blog-Properties','unknown','de/communardo/kenmei/database/update/v1.3.2/db.changelog.v1.3.2.xml','2016-06-15 21:18:41','6f5bc99c34318034fda2208600b773','Create Table','',NULL,'1.9.5'),('KENMEI-Blog-Properties_autoincrement','unknown','de/communardo/kenmei/database/update/v1.3.2/db.changelog.v1.3.2.xml','2016-06-15 21:18:43','8c8dfcead1ae690398bd7706d2851d','Set Column as Auto-Increment','',NULL,'1.9.5'),('KENMEI-Blog-Properties_fkc','unknown','de/communardo/kenmei/database/update/v1.3.2/db.changelog.v1.3.2.xml','2016-06-15 21:18:42','e8ba9d17768c1e979aec77c75996bf1a','Add Foreign Key Constraint','',NULL,'1.9.5'),('KENMEI-Drop_snc_tables','unknown','de/communardo/kenmei/database/update/v1.3/db.changelog.v1.3.xml','2016-06-15 21:18:21','1996781dbc24996e36864b7ece8f61d','Drop Table (x2)','',NULL,'1.9.5'),('KENMEI-Note-Properties_clob','unknown','de/communardo/kenmei/database/update/v1.3/db.changelog.v1.3.xml','2016-06-15 21:18:33','61daee3a8c3850f6402bdb11ab5a49bb','Modify Column','',NULL,'1.9.5'),('KENMEI-Note-properties_mysql','unknown','de/communardo/kenmei/database/update/v1.3/db.changelog.v1.3.xml','2016-06-15 21:18:33','3996e5ba99563e196a658722edc496','Custom SQL, Add Foreign Key Constraint','',NULL,'1.9.5'),('KENMEI-User-Grop-Properties_mysql','unknown','de/communardo/kenmei/database/update/v1.3/db.changelog.v1.3.xml','2016-06-15 21:18:25','669a95d06bcb29abe32926762f76d3','Custom SQL, Add Foreign Key Constraint, Custom SQL, Add Foreign Key Constraint','',NULL,'1.9.5'),('KENMEI-User-Group-Properties_clob','unknown','de/communardo/kenmei/database/update/v1.3/db.changelog.v1.3.xml','2016-06-15 21:18:27','b51dcb936e4e658ea6d26c6fd7f0903b','Modify Column (x2)','',NULL,'1.9.5'),('Like-Funktion','UNKNOWN','de/communardo/kenmei/database/update/v1.3.2/db.changelog.v1.3.2.xml','2016-06-15 21:18:39','3bcadf67c0e431aad437221dbfbe6cf5','Create Table','',NULL,'1.9.5'),('Like-Funktion_MySQL','UNKNOWN','de/communardo/kenmei/database/update/v1.3.2/db.changelog.v1.3.2.xml','2016-06-15 21:18:41','67a7975fddf52af67fae5416ae067','Add Foreign Key Constraint (x2), Set Column as Auto-Increment','',NULL,'1.9.5'),('mt_3272_1_mysql','unknown','de/communardo/kenmei/database/update/v1.1.3/db.changelog.v1.1.3.xml','2016-06-15 21:17:53','f421adb0107c9087be3d4be78a9086f3','Custom SQL','MT 3272: UserProfile saves space instead of NULL if no time zone is selected',NULL,'1.9.2'),('mt_3314_refactor_confluence_page','unknown','de/communardo/kenmei/database/update/v1.1.3/db.changelog.v1.1.3.xml','2016-06-15 21:17:53','86b2b96eee76c0d6bdaad4e1c24b5b25','Add Column','',NULL,'1.9.2'),('MT_3395_drop_source_column_from_blog_members','unknown','de/communardo/kenmei/database/update/v1.1.4/db.changelog.v1.1.4.xml','2016-06-15 21:17:55','12af7efa26d7a9eefaedfcd1ad9e2d24','Drop Column','',NULL,'1.9.5'),('mt2846_favorite_users','unknown','de/communardo/kenmei/database/update/v1.1/db.changelog.v1.1.refactor.xml','2016-06-15 21:17:53','9f4a84f23eb8c55a4730335d35c3ab7f','Rename Table','MT 2846: CR 119 - Refactoring Post Backend - Favorite users',NULL,'1.9.2'),('mt2846_global_id_checksum_v2','unknown','de/communardo/kenmei/database/update/v1.1/db.changelog.v1.1.refactor.xml','2016-06-15 21:17:53','304f2354db312562949c37255a17bf','Custom SQL','MT 2846: CR 119 - Refactoring Post Backend: Global Id, Configuration, Checksum',NULL,'1.9.2'),('mt2846_global_id_v2_mysql','unknown','de/communardo/kenmei/database/update/v1.1/db.changelog.v1.1.refactor.xml','2016-06-15 21:17:53','c0cbabe0cea6e8c89d81b7169f92e414','Insert Row','MT 2846: CR 119 - Refactoring Post Backend: Global Id',NULL,'1.9.2'),('mt2846_global_id_v3_mysql','unknown','de/communardo/kenmei/database/update/v1.1/db.changelog.v1.1.refactor.xml','2016-06-15 21:17:54','5a66e2fc6e9065379f648f9fc0fe1cb1','Insert Row','MT 2846: CR 119 - Refactoring Post Backend: Global Id',NULL,'1.9.5'),('mt2846_mysql_1','unknown','de/communardo/kenmei/database/update/v1.1/mysql/db.changelog.v1.1.refactor.mysql.xml','2016-06-15 21:17:53','30d723e996f2fe7b25fddc235b2dcfcb','Drop Table (x6)','MT 2846: CR 119 - Refactoring Post Backend - MySql',NULL,'1.9.2'),('mt2846_mysql_10','unknown','de/communardo/kenmei/database/update/v1.1/mysql/db.changelog.v1.1.refactor.mysql.xml','2016-06-15 21:17:53','cf28a7b028c31c85bc9ddeec98c856fc','Add Column (x7), Rename Column','MT 2846: CR 119 - Refactoring Post Backend - MySql',NULL,'1.9.2'),('mt2846_mysql_11','unknown','de/communardo/kenmei/database/update/v1.1/mysql/db.changelog.v1.1.refactor.mysql.xml','2016-06-15 21:17:53','e3397a39ece50c8ba1ae089f831ac4b','SQL From File','MT 2846: CR 119 - Refactoring Post Backend - MySql',NULL,'1.9.2'),('mt2846_mysql_12','unknown','de/communardo/kenmei/database/update/v1.1/mysql/db.changelog.v1.1.refactor.mysql.xml','2016-06-15 21:17:53','8f569acbd4f4184a409f268fc9cb46be','Add Not-Null Constraint (x3), Drop Table (x2)','MT 2846: CR 119 - Refactoring Post Backend - MySql',NULL,'1.9.2'),('mt2846_mysql_13','unknown','de/communardo/kenmei/database/update/v1.1/mysql/db.changelog.v1.1.refactor.mysql.xml','2016-06-15 21:17:53','172934a379fa3cf0a5cf9ee1116b1','Rename Table','MT 2846: CR 119 - Refactoring Post Backend - MySql',NULL,'1.9.2'),('mt2846_mysql_14','unknown','de/communardo/kenmei/database/update/v1.1/mysql/db.changelog.v1.1.refactor.mysql.xml','2016-06-15 21:17:53','53b64a5a1bb0c3235c619ce40ac1e5','Rename Column, Add Column (x3), Drop Table, Drop Column','MT 2846: CR 119 - Refactoring Post Backend - MySql',NULL,'1.9.2'),('mt2846_mysql_15','unknown','de/communardo/kenmei/database/update/v1.1/mysql/db.changelog.v1.1.refactor.mysql.xml','2016-06-15 21:17:53','a15f2a38d192f1ea28297d763382923','Custom SQL','MT 2846: CR 119 - Refactoring Post Backend - MySql',NULL,'1.9.2'),('mt2846_mysql_16','unknown','de/communardo/kenmei/database/update/v1.1/mysql/db.changelog.v1.1.refactor.mysql.xml','2016-06-15 21:17:53','f4f35c79c8f9cbdfa6fe278a2d1b0e','Rename Table','MT 2846: CR 119 - Refactoring Post Backend - MySql',NULL,'1.9.2'),('mt2846_mysql_17','unknown','de/communardo/kenmei/database/update/v1.1/mysql/db.changelog.v1.1.refactor.mysql.xml','2016-06-15 21:17:53','2c781dee109292b6fd35f10b820c9e0','Rename Column (x5), Drop Column (x4)','MT 2846: CR 119 - Refactoring Post Backend - MySql',NULL,'1.9.2'),('mt2846_mysql_18','unknown','de/communardo/kenmei/database/update/v1.1/mysql/db.changelog.v1.1.refactor.mysql.xml','2016-06-15 21:17:53','d41d8cd98f0b24e980998ecf8427e','Empty','MT 2846: CR 119 - Refactoring Post Backend - MySql',NULL,'1.9.2'),('mt2846_mysql_19','unknown','de/communardo/kenmei/database/update/v1.1/mysql/db.changelog.v1.1.refactor.mysql.xml','2016-06-15 21:17:53','9d3c6d7f42f3efccf7973fc8135bde5','Add Foreign Key Constraint (x3), Add Unique Constraint (x3), Add Foreign Key Constraint (x11), Add Unique Constraint, Drop Foreign Key Constraint, Add Foreign Key Constraint (x3), Add Unique Constraint, Add Foreign Key Constraint','MT 2846: CR 119 - Refactoring Post Backend - MySql',NULL,'1.9.2'),('mt2846_mysql_2','unknown','de/communardo/kenmei/database/update/v1.1/mysql/db.changelog.v1.1.refactor.mysql.xml','2016-06-15 21:17:53','a66cd5a4ddf1ad5b2ee81c0574673bd','Drop Foreign Key Constraint (x9)','MT 2846: CR 119 - Refactoring Post Backend - MySql',NULL,'1.9.2'),('mt2846_mysql_20','unknown','de/communardo/kenmei/database/update/v1.1/mysql/db.changelog.v1.1.refactor.mysql.xml','2016-06-15 21:17:53','c59f2b28cf96874373cfeacffc881b4','SQL From File','MT 2846: CR 119 - Refactoring Post Backend - MySql',NULL,'1.9.2'),('mt2846_mysql_3','unknown','de/communardo/kenmei/database/update/v1.1/mysql/db.changelog.v1.1.refactor.mysql.xml','2016-06-15 21:17:53','86c7a598426a561fc898d3bd5930a3e6','Create Table','MT 2846: CR 119 - Refactoring Post Backend - MySql',NULL,'1.9.2'),('mt2846_mysql_4','unknown','de/communardo/kenmei/database/update/v1.1/mysql/db.changelog.v1.1.refactor.mysql.xml','2016-06-15 21:17:53','59f5a1279fabc9ed2a3c7f97edad36b8','Create Table','MT 2846: CR 119 - Refactoring Post Backend - MySql',NULL,'1.9.2'),('mt2846_mysql_4a','unknown','de/communardo/kenmei/database/update/v1.1/mysql/db.changelog.v1.1.refactor.mysql.xml','2016-06-15 21:17:53','774a36dba4aff22ffbdf3bcf16ed6f','SQL From File','MT 2846: CR 119 - Refactoring Post Backend - MySql',NULL,'1.9.2'),('mt2846_mysql_5','unknown','de/communardo/kenmei/database/update/v1.1/mysql/db.changelog.v1.1.refactor.mysql.xml','2016-06-15 21:17:53','937b3863b7ea9f194231bd2dafff6832','Drop Column (x7)','MT 2846: CR 119 - Refactoring Post Backend - MySql',NULL,'1.9.2'),('mt2846_mysql_6','unknown','de/communardo/kenmei/database/update/v1.1/mysql/db.changelog.v1.1.refactor.mysql.xml','2016-06-15 21:17:53','a669428a7b42a371372f4e08556acb','SQL From File','MT 2846: CR 119 - Refactoring Post Backend - MySql',NULL,'1.9.2'),('mt2846_mysql_7','unknown','de/communardo/kenmei/database/update/v1.1/mysql/db.changelog.v1.1.refactor.mysql.xml','2016-06-15 21:17:53','f431f661f875edb7a4c7122b691768','Drop Column, Rename Table, Add Not-Null Constraint','MT 2846: CR 119 - Refactoring Post Backend - MySql',NULL,'1.9.2'),('mt2846_mysql_8','unknown','de/communardo/kenmei/database/update/v1.1/mysql/db.changelog.v1.1.refactor.mysql.xml','2016-06-15 21:17:53','44a6dfe46a8feb68eaece96d1ffd7ddf','SQL From File','MT 2846: CR 119 - Refactoring Post Backend - MySql',NULL,'1.9.2'),('mt2846_mysql_9','unknown','de/communardo/kenmei/database/update/v1.1/mysql/db.changelog.v1.1.refactor.mysql.xml','2016-06-15 21:17:53','a8626d4dafc03b63fc2a64ba9cacfc9c','Rename Table','MT 2846: CR 119 - Refactoring Post Backend - MySql',NULL,'1.9.2'),('mt2846_remove_module_status','unknown','de/communardo/kenmei/database/update/v1.1/db.changelog.v1.1.refactor.xml','2016-06-15 21:17:53','c616cb81c99fea1e6427599e6cec5187','Drop Column (x4)','MT 2846: CR 119 - Refactoring Post Backend - Remove module status',NULL,'1.9.2'),('mt2846_user_external_auth','unknown','de/communardo/kenmei/database/update/v1.1/db.changelog.v1.1.refactor.xml','2016-06-15 21:17:53','6c8c9284a09b847b41d87b11499cf4b','Rename Table, Rename Column, Drop Foreign Key Constraint, Add Foreign Key Constraint','MT 2846: CR 119 - Refactoring Post Backend - Rename external user auth',NULL,'1.9.2'),('mt2859_2','unknown','de/communardo/kenmei/database/update/v1.0.3/db.changelog.v1.0.3.xml','2016-06-15 21:17:53','b2e0e51b2fdd768127d43d458e21d939','Add Unique Constraint','',NULL,'1.9.2'),('mt2899_mysql_v3','unknown','de/communardo/kenmei/database/update/v1.0.3/db.changelog.v1.0.3.xml','2016-06-15 21:17:53','2386128fd9dcb86b8fa469f8ff2ec81','Custom SQL','MT 2899: Image caching problems',NULL,'1.9.2'),('mt2940_1','unknown','de/communardo/kenmei/database/update/v1.1/db.changelog.v1.1.xml','2016-06-15 21:17:53','b962fa5b9ddcecbc21d62b4999d8764','Update Data, Create Table, Add Foreign Key Constraint (x2)','CR 131: autosave refactoring',NULL,'1.9.2'),('mt2945_1','unknown','de/communardo/kenmei/database/update/v1.1/db.changelog.v1.1.xml','2016-06-15 21:17:53','22b680833818a22cf3f5e695a7b185d','Add Column, Add Foreign Key Constraint','CR 109: Post without selecting a blog',NULL,'1.9.2'),('mt2957_1','unknown','de/communardo/kenmei/database/update/v1.1/db.changelog.v1.1.xml','2016-06-15 21:17:53','2a75e566753c4ffb3f42c5fb9896cd','Add Column (x2)','CR 122 - Threaded answers',NULL,'1.9.2'),('mt2957_2_mysql','unknown','de/communardo/kenmei/database/update/v1.1/db.changelog.v1.1.xml','2016-06-15 21:17:53','39dd942ccd10338659ba1bfdb94f9183','Custom SQL','CR 122 - Threaded answers',NULL,'1.9.2'),('mt2976_1','unknown','de/communardo/kenmei/database/update/v1.1/db.changelog.v1.1.xml','2016-06-15 21:17:53','dcd4c199fd6bf5cd3ca66a34a84afa7','Add Column (x3)','Content Type',NULL,'1.9.2'),('mt2976_2','unknown','de/communardo/kenmei/database/update/v1.1/db.changelog.v1.1.xml','2016-06-15 21:17:53','6b7314e763c81f1f96c67deb8d806d69','Custom SQL','Attachment Status',NULL,'1.9.2'),('mt3022_mysql','unknown','de/communardo/kenmei/database/update/v1.1/db.changelog.v1.1.xml','2016-06-15 21:17:53','a4c8beb7d79b2eb7c18c2d883f2eb3c','Custom SQL','Increase Repository Limit',NULL,'1.9.2'),('mt3096_1','unknown','de/communardo/kenmei/database/update/v1.1/db.changelog.v1.1.xml','2016-06-15 21:17:53','abcc6ad563565e4050381273ef3fcc5d','Custom SQL','deletes unconnected large user images',NULL,'1.9.2'),('mt3178_1_mysql','unknown','de/communardo/kenmei/database/update/v1.1/db.changelog.v1.1.xml','2016-06-15 21:17:53','dd97e4a211a78df2d24fbda9552922b','Insert Row','Automated Client Approval',NULL,'1.9.2'),('mt3178_2_mysql','unknown','de/communardo/kenmei/database/update/v1.1/db.changelog.v1.1.xml','2016-06-15 21:17:53','d1808dcb3e616cfff590fbbdf8494f','Insert Row','Automated Client Approval',NULL,'1.9.2'),('mt3187_1','unknown','de/communardo/kenmei/database/update/v1.1/db.changelog.v1.1.xml','2016-06-15 21:17:53','77fa5d42e90366abddbbb7dbb94e61d','Drop Foreign Key Constraint','Adjust the forein key constraint for kenmei_users_fk.',NULL,'1.9.2'),('mt3187_2','unknown','de/communardo/kenmei/database/update/v1.1/db.changelog.v1.1.xml','2016-06-15 21:17:53','1b315757661762a2659fde38c03259','Add Foreign Key Constraint','Adjust the forein key constraint for kenmei_users_fk.',NULL,'1.9.2'),('mt3187_3','unknown','de/communardo/kenmei/database/update/v1.1/db.changelog.v1.1.xml','2016-06-15 21:17:53','77fa5d42e90366abddbbb7dbb94e61d','Drop Foreign Key Constraint','Adjust the forein key constraint for kenmei_users_fk.',NULL,'1.9.2'),('mt3187_4_mysql','unknown','de/communardo/kenmei/database/update/v1.1/db.changelog.v1.1.xml','2016-06-15 21:17:53','925ad9965ef8812edba0b6ba81d9629','Custom SQL, Add Foreign Key Constraint','Adjust the forein key constraint for kenmei_users_fk.',NULL,'1.9.2'),('mt3196_1','unknown','de/communardo/kenmei/database/update/v1.1.2/db.changelog.v1.1.2.xml','2016-06-15 21:17:53','3f4f52f46b355cec745a24706cca46d4','Add Column','CR 100 - Support Time Zones: add new column to user_profile',NULL,'1.9.2'),('mt3196_2','unknown','de/communardo/kenmei/database/update/v1.1.2/db.changelog.v1.1.2.xml','2016-06-15 21:17:53','dca47279adc3bbd1ad7e1d7174e10b1','Add Column (x2)','CR 100 - Support Time Zones: add new column to user_client_creation, configuration_client',NULL,'1.9.2'),('mt3208','unknown','de/communardo/kenmei/database/update/v1.1.2/db.changelog.v1.1.2.xml','2016-06-15 21:17:53','a5e3666944ac3f968080ff852bd777d3','Add Column','CR 68 Read-More functionality',NULL,'1.9.2'),('mt3277_1_mysql','unknown','de/communardo/kenmei/database/update/v1.1.3/db.changelog.v1.1.3.xml','2016-06-15 21:17:53','2ea8e41788851b5f4a58846e05bcc82','Create Table, Custom SQL, Drop Column, Add Foreign Key Constraint (x2)','CR 96 - support for groups: add user_entity and copy users',NULL,'1.9.2'),('mt3277_10_mysql','unknown','de/communardo/kenmei/database/update/v1.1.3/db.changelog.v1.1.3.xml','2016-06-15 21:17:53','fa346348fef8d167f2390c9b247f053','Create Table, Add Primary Key, Add Foreign Key Constraint (x2)','CR 96 - support for groups: allow several granting groups',NULL,'1.9.2'),('mt3277_11_mysql','unknown','de/communardo/kenmei/database/update/v1.1.3/db.changelog.v1.1.3.xml','2016-06-15 21:17:53','c83fd6a7d42279a72d55ededfcfa4d86','Create Table, Add Primary Key, Add Foreign Key Constraint (x2)','CR 96 - support for groups: connect entity with group',NULL,'1.9.2'),('mt3277_13_mysql','unknown','de/communardo/kenmei/database/update/v1.1.3/db.changelog.v1.1.3.xml','2016-06-15 21:17:53','4691cf11e2e377d7897b8abdecc6d88d','Add Not-Null Constraint (x2)','CR 96 - support for groups: not null constraints for all_can_x rights',NULL,'1.9.2'),('mt3277_14','unknown','de/communardo/kenmei/database/update/v1.1.3/db.changelog.v1.1.3.xml','2016-06-15 21:17:53','e694eb5228924a36a25357646af454e4','Add Column, Custom SQL, Add Not-Null Constraint','CR 96 - support for groups: grantedByGroup flag for mapping table',NULL,'1.9.2'),('mt3277_2_mysql','unknown','de/communardo/kenmei/database/update/v1.1.3/db.changelog.v1.1.3.xml','2016-06-15 21:17:53','88fa38c4a5f8ae66e1cbae3414dfccf','Drop Table (x2)','CR 96 - support for groups: remove obsolete security codes',NULL,'1.9.2'),('mt3277_3_mysql','unknown','de/communardo/kenmei/database/update/v1.1.3/db.changelog.v1.1.3.xml','2016-06-15 21:17:53','f595d3d3bb28aa16d7567246a3a4585','Add Column, Custom SQL','CR 96 - support for groups: copy all_can_x rights from group to blog',NULL,'1.9.2'),('mt3277_4_mysql','unknown','de/communardo/kenmei/database/update/v1.1.3/db.changelog.v1.1.3.xml','2016-06-15 21:17:53','25c695869c8f851fd412ca90db5b2677','Add Column, Custom SQL','CR 96 - support for groups: add blogs to group member',NULL,'1.9.2'),('mt3277_5_mysql','unknown','de/communardo/kenmei/database/update/v1.1.3/db.changelog.v1.1.3.xml','2016-06-15 21:17:53','ce8baa4707c58ddfba1523c015deb0','Drop Foreign Key Constraint (x2), Drop Column (x2), Custom SQL, Rename Table, Rename Column (x2), Add Column, Add Foreign Key Constraint (x2), Add Not-Null Constraint (x3)','CR 96 - support for groups: group member to blog member',NULL,'1.9.2'),('mt3277_6_mysql','unknown','de/communardo/kenmei/database/update/v1.1.3/db.changelog.v1.1.3.xml','2016-06-15 21:17:53','da4346ff7575e6ebf6b3cd1266164','Drop Foreign Key Constraint, Drop Column','CR 96 - support for groups: cleanup core_blog',NULL,'1.9.2'),('mt3277_7_mysql','unknown','de/communardo/kenmei/database/update/v1.1.3/db.changelog.v1.1.3.xml','2016-06-15 21:17:53','3e47ee3a105c684a582e423518decca','Drop Column (x3), Delete Data, Add Column (x3), Add Foreign Key Constraint','CR 96 - support for groups: fix user_group',NULL,'1.9.2'),('mt3277_8_mysql','unknown','de/communardo/kenmei/database/update/v1.1.3/db.changelog.v1.1.3.xml','2016-06-15 21:17:53','9a394f311e01bce782f7d562045c46b','Create Table','CR 96 - support for groups: add helper table for fast blog role lookup',NULL,'1.9.2'),('mt3277_9_mysql','unknown','de/communardo/kenmei/database/update/v1.1.3/db.changelog.v1.1.3.xml','2016-06-15 21:17:53','ae6438d32dde3f4c6124ca8330b5ec6','Custom SQL','CR 96 - support for groups: fill core_role2blog',NULL,'1.9.2'),('mt3277_fix_null_constraint','unknown','de/communardo/kenmei/database/update/v1.1.3/db.changelog.v1.1.3.xml','2016-06-15 21:17:53','97f2d0c8945bae1c69f8f827c57e73','Drop Not-Null Constraint','Drop wrong \"not null\" constraint from user entities.',NULL,'1.9.2'),('mt3281_1_mysql','unknown','de/communardo/kenmei/database/update/v1.1.3/db.changelog.v1.1.3.xml','2016-06-15 21:17:53','3ebca4821f7296e11d97c5913fe8707c','Insert Row','CR 134 - Anonymous Access, Anonymous User',NULL,'1.9.2'),('mt3281_2_mysql','unknown','de/communardo/kenmei/database/update/v1.1.3/db.changelog.v1.1.3.xml','2016-06-15 21:17:53','f4972baac8a2f4504d77fd85c7d15e9','Add Column, Add Not-Null Constraint','CR 134 - Anonymous Access, Anonymous User',NULL,'1.9.2'),('mt3283_2_mysql','unknown','de/communardo/kenmei/database/update/v1.1.3/db.changelog.v1.1.3.xml','2016-06-15 21:17:53','22711a3246654a434873c0e1fbf78119','Set Column as Auto-Increment, Add Not-Null Constraint','Refactor External Authentication',NULL,'1.9.2'),('mt3283_confluence_configuration','unknown','de/communardo/kenmei/database/update/v1.1.3/db.changelog.v1.1.3.xml','2016-06-15 21:17:53','5029a38aca9b7c4c25a4f41f619caa1c','Add Column (x2)','',NULL,'1.9.2'),('mt3283_external_configuration','unknown','de/communardo/kenmei/database/update/v1.1.3/db.changelog.v1.1.3.xml','2016-06-15 21:17:53','c2c4eb85364225670b85647601134e0','Rename Table (x3), Rename Column, Add Column, Drop Foreign Key Constraint, Add Foreign Key Constraint','Refactor External Authentication',NULL,'1.9.2'),('mt3283_external_objects','unknown','de/communardo/kenmei/database/update/v1.1.3/db.changelog.v1.1.3.xml','2016-06-15 21:17:53','75db72d38b92ade15a93f1b854b9048','Create Table, Add Unique Constraint, Create Table, Add Foreign Key Constraint (x2), Add Column','',NULL,'1.9.2'),('mt3283_external_objects_2','unknown','de/communardo/kenmei/database/update/v1.1.3/db.changelog.v1.1.3.xml','2016-06-15 21:17:53','ff7ed34175992ead7a175351c0d4e9','Add Column, Add Not-Null Constraint','Add class to blog_member',NULL,'1.9.2'),('mt3283_external_objects_3','unknown','de/communardo/kenmei/database/update/v1.1.3/db.changelog.v1.1.3.xml','2016-06-15 21:17:53','d3cc6233f8abccd7d6c2eb5017cf77','Add Column','',NULL,'1.9.2'),('mt3283_external_objects_fix_auto_increment_mysql','unknown','de/communardo/kenmei/database/update/v1.1.3/db.changelog.v1.1.3.xml','2016-06-15 21:17:53','c7f24c22f84e8eff695ba917c8fd6d7','Custom SQL','CR 135 - Support Synchronization of blog rights with external systems',NULL,'1.9.2'),('mt3283_external_objects_fix_key_unique_mysql','unknown','de/communardo/kenmei/database/update/v1.1.3/db.changelog.v1.1.3.xml','2016-06-15 21:17:53','c2294436992eb88562f385fe26135b0','Drop Primary Key, Add Column','CR 135 - Support Synchronization of blog rights with external systems',NULL,'1.9.2'),('mt3292_confluence_permission_url','unknown','de/communardo/kenmei/database/update/v1.1.3/db.changelog.v1.1.3.xml','2016-06-15 21:17:53','21a7986f567bd1c385abbdfead1e8c3','Add Column','CR 135 - Support Synchronization of blog rights with external systems',NULL,'1.9.2'),('mt3329_1_mysql','unknown','de/communardo/kenmei/database/update/v1.1.3/db.changelog.v1.1.3.xml','2016-06-15 21:17:53','836a30af94fb53cfe359ea11fe4f62fe','Create Table, Custom SQL, Add Foreign Key Constraint (x2)','CR 96 - support for hierarchical groups: add user_of_group and copy users',NULL,'1.9.2'),('mt3350_configuration_column_ldap','unknown','de/communardo/kenmei/database/update/v1.1.2/db.changelog.v1.1.2.xml','2016-06-15 21:17:53','dd10f32f1dfab6778b0c3eb60acd4ba','Drop Column','MT 3350',NULL,'1.9.2'),('mt3350_configuration_column_source','unknown','de/communardo/kenmei/database/update/v1.1.3/db.changelog.v1.1.3.xml','2016-06-15 21:17:53','12af7efa26d7a9eefaedfcd1ad9e2d24','Drop Column','MT 3350',NULL,'1.9.2'),('phone_and_fax_country_code_fix_mysql','unknown','de/communardo/kenmei/database/update/v1.1.3/db.changelog.v1.1.3.xml','2016-06-15 21:17:53','89e69368ec3f98f683218ccbb47340','Custom SQL','',NULL,'1.9.2'),('phone_country_code_fix__client_mysql','unknown','de/communardo/kenmei/database/update/v1.1.3/db.changelog.v1.1.3.xml','2016-06-15 21:17:53','84e1be9cc8d1aa2f974526d73f3cf9e7','Custom SQL','',NULL,'1.9.2'),('Remove_QueryHelper','Communote','de/communardo/kenmei/database/update/v2.4/db.changelog.v2.4.xml','2016-06-15 21:19:33','c9b4be9fd66455b18d2563688146a26','Drop Table','',NULL,'1.9.5'),('remove_uk_country_code','unknown','de/communardo/kenmei/database/update/v1.1.3/db.changelog.v1.1.3.xml','2016-06-15 21:17:53','a8a46d136dea83ec4bdfffd11c9b065','Custom SQL','',NULL,'1.9.2'),('rename_default_blog_alias_1_mysql','unknown','de/communardo/kenmei/database/update/v1.1/db.changelog.v1.1.xml','2016-06-15 21:17:53','aab6b129c4acfe2705da4d8d127dd7b','Custom SQL','Renames the blog alias of the default blog, if an alias \'public\' not exists.',NULL,'1.9.2'),('rename_default_blog_alias_2_mysql','unknown','de/communardo/kenmei/database/update/v1.1/db.changelog.v1.1.xml','2016-06-15 21:17:53','b38717f58c847a3c1896dd7425ef50','Custom SQL','Renames the blog alias of the default blog, if it is still the message key.',NULL,'1.9.2'),('reset_checksum_001','unknown','de/communardo/kenmei/database/update/db.changelog.default.xml','2016-06-15 21:17:53','6c50cd6ee695161d8f569de01f98956','Custom SQL','Reset Checksums',NULL,'1.9.2'),('set_configuration','unknown','de/communardo/kenmei/database/update/db.changelog.final.xml','2016-06-15 21:17:53','6148c09f2e75f3239da193221c5cedca','Custom SQL','Assign the configuration FK',NULL,'1.9.2'),('set_configuration_v2','unknown','de/communardo/kenmei/database/update/db.changelog.final.xml','2016-06-15 21:19:54','77ed99ff17bb8b6b7dff62f6eaa738','Custom SQL','Assign the configuration FK',NULL,'1.9.5'),('Unknown','KENMEI-4109-TagClearance_entfernen','de/communardo/kenmei/database/update/v2.1/db.changelog.v2.1.1.xml','2016-06-15 21:18:45','1e29d9d524219933d8377380bdc495','Drop Table (x3)','',NULL,'1.9.5'),('Update global ids for tags_mysql','Communote','de/communardo/kenmei/database/update/v2.1/db.changelog.v2.1.1.xml','2016-06-15 21:19:13','5d2ed0b43e18fb6e41d4cb43084acf8','Custom SQL','',NULL,'1.9.5'),('Update_Master_Data_From_Property_Files','unknown','de/communardo/kenmei/database/update_2nd_pass/v1.1.4/db.changelog.v1.1.4.xml','2016-06-15 21:19:55','38a37931a03d7a8d2b156f8813123074','Custom Change','Update the master data stored in DB with entries from property files.',NULL,'1.9.5');
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `databasechangeloglock` (
  `ID` int(11) NOT NULL,
  `LOCKED` tinyint(1) NOT NULL,
  `LOCKGRANTED` datetime DEFAULT NULL,
  `LOCKEDBY` varchar(255) COLLATE utf8_unicode_ci DEFAULT NULL,
  PRIMARY KEY (`ID`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

INSERT INTO `databasechangeloglock` VALUES (1,0,NULL,NULL);
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `iprange_channel` (
  `TYPE` varchar(255) COLLATE utf8_unicode_ci NOT NULL,
  `ENABLED` tinyint(4) NOT NULL,
  PRIMARY KEY (`TYPE`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `iprange_filter` (
  `ID` bigint(20) NOT NULL AUTO_INCREMENT,
  `NAME` varchar(1024) COLLATE utf8_unicode_ci NOT NULL,
  `ENABLED` tinyint(4) NOT NULL,
  PRIMARY KEY (`ID`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `iprange_filter_channel` (
  `IP_RANGE_FILTERS_FK` bigint(20) NOT NULL,
  `CHANNELS_FK` varchar(255) COLLATE utf8_unicode_ci NOT NULL,
  PRIMARY KEY (`IP_RANGE_FILTERS_FK`,`CHANNELS_FK`),
  KEY `iprange_channel_IP_RANGE_FILTC` (`IP_RANGE_FILTERS_FK`),
  KEY `iprange_filter_CHANNELS_FKC` (`CHANNELS_FK`),
  CONSTRAINT `iprange_channel_IP_RANGE_FILTC` FOREIGN KEY (`IP_RANGE_FILTERS_FK`) REFERENCES `iprange_filter` (`ID`),
  CONSTRAINT `iprange_filter_CHANNELS_FKC` FOREIGN KEY (`CHANNELS_FK`) REFERENCES `iprange_channel` (`TYPE`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `iprange_range` (
  `ID` bigint(20) NOT NULL AUTO_INCREMENT,
  `START_VALUE` longblob NOT NULL,
  `END_VALUE` longblob NOT NULL,
  `STRING_REPRESENTATION` varchar(1024) COLLATE utf8_unicode_ci DEFAULT NULL,
  `IP_RANGE_FILTER_IN_FK` bigint(20) DEFAULT NULL,
  `IP_RANGE_FILTER_EX_FK` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`ID`),
  KEY `ip_range_filter_C_ex` (`IP_RANGE_FILTER_EX_FK`),
  KEY `ip_range_filter_C_in` (`IP_RANGE_FILTER_IN_FK`),
  CONSTRAINT `ip_range_filter_C_ex` FOREIGN KEY (`IP_RANGE_FILTER_EX_FK`) REFERENCES `iprange_filter` (`ID`),
  CONSTRAINT `ip_range_filter_C_in` FOREIGN KEY (`IP_RANGE_FILTER_IN_FK`) REFERENCES `iprange_filter` (`ID`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `mc_config` (
  `ID` bigint(20) NOT NULL AUTO_INCREMENT,
  `TYPE` varchar(1024) COLLATE utf8_unicode_ci NOT NULL,
  `PROPERTIES` varchar(1024) COLLATE utf8_unicode_ci DEFAULT NULL,
  `ONLY_IF_AVAILABLE` tinyint(4) NOT NULL,
  `PRIORITY` int(11) NOT NULL,
  `NOTIFICATION_CONFIG_FK` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`ID`),
  KEY `mc_config_NOTIFICATION_CONFIGC` (`NOTIFICATION_CONFIG_FK`),
  CONSTRAINT `mc_config_NOTIFICATION_CONFIGC` FOREIGN KEY (`NOTIFICATION_CONFIG_FK`) REFERENCES `notification_config` (`ID`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

INSERT INTO `mc_config` VALUES (1,'mail',NULL,0,1,1);
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `md_country` (
  `ID` bigint(20) NOT NULL AUTO_INCREMENT,
  `COUNTRY_CODE` varchar(255) COLLATE utf8_unicode_ci NOT NULL,
  `NAME` varchar(1024) COLLATE utf8_unicode_ci NOT NULL,
  PRIMARY KEY (`ID`),
  UNIQUE KEY `COUNTRY_CODE` (`COUNTRY_CODE`)
) ENGINE=InnoDB AUTO_INCREMENT=247 DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

INSERT INTO `md_country` VALUES (1,'lb','Lebanon'),(2,'la','Lao People\'s Democratic Republic'),(3,'kz','Kazakhstan'),(4,'ky','Cayman Islands'),(5,'kw','Kuwait'),(6,'kr','Korea, Republic of'),(7,'kp','Korea, Democratic People\'s Republic of'),(8,'kn','Saint Kitts and Nevis'),(9,'km','Comoros'),(10,'ki','Kiribati'),(11,'kh','Cambodia'),(12,'ws','Samoa'),(13,'kg','Kyrgyzstan'),(14,'ke','Kenya'),(15,'wf','Wallis and Futuna'),(16,'jp','Japan'),(17,'jo','Jordan'),(18,'jm','Jamaica'),(19,'vu','Vanuatu'),(20,'je','Jersey'),(21,'vn','Vietnam'),(22,'vi','Virgin Islands, U.S.'),(23,'vg','Virgin Islands, British'),(24,'ve','Venezuela'),(25,'vc','Saint Vincent and the Grenadines'),(26,'va','Holy See (Vatican City State)'),(27,'it','Italy'),(28,'is','Iceland'),(29,'ir','Iran, Islamic Republic of'),(30,'iq','Iraq'),(31,'io','British Indian Ocean Territory'),(32,'uz','Uzbekistan'),(33,'in','India'),(34,'uy','Uruguay'),(35,'im','Isle of Man'),(36,'il','Israel'),(37,'us','United States'),(38,'ie','Ireland'),(39,'id','Indonesia'),(40,'um','United States Minor Outlying Islands'),(41,'ug','Uganda'),(42,'hu','Hungary'),(43,'ua','Ukraine'),(44,'ht','Haiti'),(45,'hr','Croatia'),(46,'tz','Tanzania, United Republic of'),(47,'hn','Honduras'),(48,'hm','Heard Island and McDonald Islands'),(49,'tw','Taiwan, Province of China'),(50,'hk','Hong Kong'),(51,'tv','Tuvalu'),(52,'tt','Trinidad and Tobago'),(53,'tr','Turkey'),(54,'to','Tonga'),(55,'tn','Tunisia'),(56,'tm','Turkmenistan'),(57,'tl','Timor-Leste'),(58,'tk','Tokelau'),(59,'tj','Tajikistan'),(60,'th','Thailand'),(61,'tg','Togo'),(62,'tf','French Southern Territories'),(63,'gy','Guyana'),(64,'td','Chad'),(65,'gw','Guinea-bissau'),(66,'tc','Turks and Caicos Islands'),(67,'gu','Guam'),(68,'gt','Guatemala'),(69,'gs','South Georgia and the South Sandwich Islands'),(70,'gr','Greece'),(71,'gq','Equatorial Guinea'),(72,'gp','Guadeloupe'),(73,'sz','Swaziland'),(74,'gn','Guinea'),(75,'sy','Syrian Arab Republic'),(76,'gm','Gambia'),(77,'gl','Greenland'),(78,'sv','El Salvador'),(79,'gi','Gibraltar'),(80,'st','Sao Tome and Principe'),(81,'gh','Ghana'),(82,'gg','Guernsey'),(83,'sr','Suriname'),(84,'gf','French Guiana'),(85,'ge','Georgia'),(86,'gd','Grenada'),(87,'so','Somalia'),(88,'sn','Senegal'),(89,'gb','United Kingdom'),(90,'sm','San Marino'),(91,'ga','Gabon'),(92,'sl','Sierra Leone'),(93,'sk','Slovakia'),(94,'sj','Svalbard and Jan Mayen'),(95,'si','Slovenia'),(96,'sh','Saint Helena'),(97,'sg','Singapore'),(98,'se','Sweden'),(99,'sd','Sudan'),(100,'sc','Seychelles'),(101,'sb','Solomon Islands'),(102,'sa','Saudi Arabia'),(103,'fr','France'),(104,'fo','Faroe Islands'),(105,'fm','Micronesia, Federated States of'),(106,'rw','Rwanda'),(107,'fk','Falkland Islands (Malvinas)'),(108,'fj','Fiji'),(109,'ru','Russian Federation'),(110,'fi','Finland'),(111,'rs','Serbia'),(112,'ro','Romania'),(113,'re','Reunion'),(114,'et','Ethiopia'),(115,'es','Spain'),(116,'er','Eritrea'),(117,'eh','Western Sahara'),(118,'eg','Egypt'),(119,'ee','Estonia'),(120,'ec','Ecuador'),(121,'dz','Algeria'),(122,'qa','Qatar'),(123,'do','Dominican Republic'),(124,'py','Paraguay'),(125,'dm','Dominica'),(126,'pw','Palau'),(127,'dk','Denmark'),(128,'dj','Djibouti'),(129,'pt','Portugal'),(130,'ps','Palestinian Territory, occupied'),(131,'pr','Puerto Rico'),(132,'de','Germany'),(133,'pn','Pitcairn'),(134,'pm','Saint Pierre and Miquelon'),(135,'pl','Poland'),(136,'pk','Pakistan'),(137,'ph','Philippines'),(138,'pg','Papua New Guinea'),(139,'cz','Czech Republic'),(140,'pf','French Polynesia'),(141,'cy','Cyprus'),(142,'pe','Peru'),(143,'cx','Christmas Island'),(144,'cv','Cape Verde'),(145,'cu','Cuba'),(146,'pa','Panama'),(147,'cr','Costa Rica'),(148,'co','Colombia'),(149,'cn','China'),(150,'cm','Cameroon'),(151,'cl','Chile'),(152,'ck','Cook Islands'),(153,'ci','Côte D\'Ivoire'),(154,'ch','Switzerland'),(155,'cg','Congo'),(156,'cf','Central African Republic'),(157,'cd','Congo, the Democratic Republic of the'),(158,'cc','Cocos (Keeling) Islands'),(159,'om','Oman'),(160,'ca','Canada'),(161,'bz','Belize'),(162,'by','Belarus'),(163,'bw','Botswana'),(164,'bv','Bouvet Island'),(165,'bt','Bhutan'),(166,'bs','Bahamas'),(167,'br','Brazil'),(168,'bo','Bolivia'),(169,'nz','New Zealand'),(170,'bn','Brunei Darussalam'),(171,'bm','Bermuda'),(172,'bl','Saint Barthélemy'),(173,'bj','Benin'),(174,'nu','Niue'),(175,'bi','Burundi'),(176,'bh','Bahrain'),(177,'bg','Bulgaria'),(178,'nr','Nauru'),(179,'bf','Burkina Faso'),(180,'be','Belgium'),(181,'np','Nepal'),(182,'bd','Bangladesh'),(183,'no','Norway'),(184,'bb','Barbados'),(185,'ba','Bosnia and Herzegovina'),(186,'nl','Netherlands'),(187,'zw','Zimbabwe'),(188,'ni','Nicaragua'),(189,'ng','Nigeria'),(190,'az','Azerbaijan'),(191,'nf','Norfolk Island'),(192,'ne','Niger'),(193,'ax','Åland Islands'),(194,'aw','Aruba'),(195,'nc','New Caledonia'),(196,'zm','Zambia'),(197,'au','Australia'),(198,'na','Namibia'),(199,'at','Austria'),(200,'as','American Samoa'),(201,'ar','Argentina'),(202,'aq','Antarctica'),(203,'ao','Angola'),(204,'mz','Mozambique'),(205,'an','Netherlands Antilles'),(206,'my','Malaysia'),(207,'am','Armenia'),(208,'mx','Mexico'),(209,'al','Albania'),(210,'mw','Malawi'),(211,'mv','Maldives'),(212,'mu','Mauritius'),(213,'za','South Africa'),(214,'ai','Anguilla'),(215,'mt','Malta'),(216,'ms','Montserrat'),(217,'ag','Antigua and Barbuda'),(218,'mr','Mauritania'),(219,'af','Afghanistan'),(220,'mq','Martinique'),(221,'ae','United Arab Emirates'),(222,'mp','Northern Mariana Islands'),(223,'ad','Andorra'),(224,'mo','Macao'),(225,'mn','Mongolia'),(226,'mm','Myanmar'),(227,'ml','Mali'),(228,'mk','Macedonia, the Former Yugoslav Republic of'),(229,'yt','Mayotte'),(230,'mh','Marshall Islands'),(231,'mg','Madagascar'),(232,'mf','Saint Martin'),(233,'me','Montenegro'),(234,'md','Moldova, Republic of'),(235,'mc','Monaco'),(236,'ma','Morocco'),(237,'ly','Libyan Arab Jamahiriya'),(238,'ye','Yemen'),(239,'lv','Latvia'),(240,'lu','Luxembourg'),(241,'lt','Lithuania'),(242,'ls','Lesotho'),(243,'lr','Liberia'),(244,'lk','Sri Lanka'),(245,'li','Liechtenstein'),(246,'lc','Saint Lucia');
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `md_language` (
  `ID` bigint(20) NOT NULL AUTO_INCREMENT,
  `LANGUAGE_CODE` varchar(255) COLLATE utf8_unicode_ci NOT NULL,
  `NAME` varchar(1024) COLLATE utf8_unicode_ci NOT NULL,
  PRIMARY KEY (`ID`),
  UNIQUE KEY `LANGUAGE_CODE` (`LANGUAGE_CODE`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

INSERT INTO `md_language` VALUES (1,'en','English'),(2,'de','Deutsch');
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `notification_config` (
  `ID` bigint(20) NOT NULL AUTO_INCREMENT,
  `FALLBACK` varchar(1024) COLLATE utf8_unicode_ci DEFAULT NULL,
  PRIMARY KEY (`ID`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

INSERT INTO `notification_config` VALUES (1,'mail');
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `security_code` (
  `ID` bigint(20) NOT NULL AUTO_INCREMENT,
  `CODE` varchar(255) COLLATE utf8_unicode_ci NOT NULL,
  `ACTION` varchar(1024) COLLATE utf8_unicode_ci NOT NULL,
  `CREATING_DATE` timestamp NOT NULL DEFAULT '0000-00-00 00:00:00',
  `KENMEI_USER_FK` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`ID`),
  UNIQUE KEY `CODE` (`CODE`),
  KEY `security_code_KENMEI_USER_FKC` (`KENMEI_USER_FK`),
  CONSTRAINT `security_code_KENMEI_USER_FKC` FOREIGN KEY (`KENMEI_USER_FK`) REFERENCES `user_user` (`ID`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `security_email_code` (
  `ID` bigint(20) NOT NULL,
  `NEW_EMAIL_ADDRESS` varchar(1024) COLLATE utf8_unicode_ci NOT NULL,
  PRIMARY KEY (`ID`),
  KEY `security_email_codeIFKC` (`ID`),
  CONSTRAINT `security_email_codeIFKC` FOREIGN KEY (`ID`) REFERENCES `security_code` (`ID`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `security_forgotten_pw_code` (
  `ID` bigint(20) NOT NULL,
  PRIMARY KEY (`ID`),
  KEY `security_forgotten_pw_codeIFKC` (`ID`),
  CONSTRAINT `security_forgotten_pw_codeIFKC` FOREIGN KEY (`ID`) REFERENCES `security_code` (`ID`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `security_invite_blog` (
  `ID` bigint(20) NOT NULL,
  `invitor_id` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`ID`),
  KEY `security_invite_blogIFKC` (`ID`),
  CONSTRAINT `security_invite_blogIFKC` FOREIGN KEY (`ID`) REFERENCES `security_code` (`ID`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `security_invite_client` (
  `ID` bigint(20) NOT NULL,
  PRIMARY KEY (`ID`),
  KEY `security_invite_clientIFKC` (`ID`),
  CONSTRAINT `security_invite_clientIFKC` FOREIGN KEY (`ID`) REFERENCES `security_code` (`ID`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `security_user_code` (
  `ID` bigint(20) NOT NULL,
  PRIMARY KEY (`ID`),
  KEY `security_user_codeIFKC` (`ID`),
  CONSTRAINT `security_user_codeIFKC` FOREIGN KEY (`ID`) REFERENCES `security_code` (`ID`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `security_user_status` (
  `ID` bigint(20) NOT NULL AUTO_INCREMENT,
  `LOCKED_TIMEOUT` timestamp NOT NULL DEFAULT '0000-00-00 00:00:00',
  `FAILED_AUTH_COUNTER` int(11) NOT NULL,
  `CHANNEL_TYPE` varchar(1024) COLLATE utf8_unicode_ci NOT NULL,
  `KENMEI_USER_FK` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`ID`),
  KEY `security_user_auth_failed_staC` (`KENMEI_USER_FK`),
  CONSTRAINT `security_user_auth_failed_staC` FOREIGN KEY (`KENMEI_USER_FK`) REFERENCES `user_user` (`ID`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `security_user_unlock_code` (
  `ID` bigint(20) NOT NULL,
  `CHANNEL` varchar(1024) COLLATE utf8_unicode_ci NOT NULL,
  PRIMARY KEY (`ID`),
  KEY `security_user_unlock_codeIFKC` (`ID`),
  CONSTRAINT `security_user_unlock_codeIFKC` FOREIGN KEY (`ID`) REFERENCES `security_code` (`ID`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `user_authorities` (
  `ID` bigint(20) NOT NULL AUTO_INCREMENT,
  `ROLE` varchar(1024) COLLATE utf8_unicode_ci NOT NULL,
  `KENMEI_USER_FK` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`ID`),
  KEY `user_authorities_KENMEI_USER_C` (`KENMEI_USER_FK`),
  CONSTRAINT `user_authorities_KENMEI_USER_C` FOREIGN KEY (`KENMEI_USER_FK`) REFERENCES `user_user` (`ID`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

INSERT INTO `user_authorities` VALUES (1,'ROLE_KENMEI_CLIENT_MANAGER',1),(2,'ROLE_KENMEI_USER',1);
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `user_client` (
  `ID` bigint(20) NOT NULL AUTO_INCREMENT,
  `CLIENT_ID` varchar(255) COLLATE utf8_unicode_ci NOT NULL,
  `NAME` varchar(1024) COLLATE utf8_unicode_ci NOT NULL,
  `CLIENT_STATUS` varchar(1024) COLLATE utf8_unicode_ci NOT NULL,
  `CREATION_VERSION` varchar(1024) COLLATE utf8_unicode_ci DEFAULT NULL,
  `CREATION_TIME` timestamp NOT NULL DEFAULT '0000-00-00 00:00:00',
  `creation_revision` varchar(1024) COLLATE utf8_unicode_ci DEFAULT NULL,
  PRIMARY KEY (`ID`),
  UNIQUE KEY `CLIENT_ID` (`CLIENT_ID`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

INSERT INTO `user_client` VALUES (1,'global','Global Test Client','ACTIVE','3.4.65f2b','2016-06-15 19:19:56','65f2b');
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `user_client_statistic` (
  `ID` bigint(20) NOT NULL AUTO_INCREMENT,
  `REPOSITORY_SIZE` bigint(20) NOT NULL,
  PRIMARY KEY (`ID`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

INSERT INTO `user_client_statistic` VALUES (1,0);
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `user_contact` (
  `ID` bigint(20) NOT NULL AUTO_INCREMENT,
  `STREET` varchar(1024) COLLATE utf8_unicode_ci DEFAULT NULL,
  `ZIP` varchar(1024) COLLATE utf8_unicode_ci DEFAULT NULL,
  `CITY` varchar(1024) COLLATE utf8_unicode_ci DEFAULT NULL,
  `PHONE_COUNTRY_CODE` varchar(1024) COLLATE utf8_unicode_ci DEFAULT NULL,
  `PHONE_AREA_CODE` varchar(1024) COLLATE utf8_unicode_ci DEFAULT NULL,
  `PHONE_PHONE_NUMBER` varchar(1024) COLLATE utf8_unicode_ci DEFAULT NULL,
  `FAX_COUNTRY_CODE` varchar(1024) COLLATE utf8_unicode_ci DEFAULT NULL,
  `FAX_AREA_CODE` varchar(1024) COLLATE utf8_unicode_ci DEFAULT NULL,
  `FAX_PHONE_NUMBER` varchar(1024) COLLATE utf8_unicode_ci DEFAULT NULL,
  `COUNTRY_FK` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`ID`),
  KEY `user_contact_COUNTRY_FKC` (`COUNTRY_FK`),
  CONSTRAINT `user_contact_COUNTRY_FKC` FOREIGN KEY (`COUNTRY_FK`) REFERENCES `md_country` (`ID`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `user_entity` (
  `ID` bigint(20) NOT NULL AUTO_INCREMENT,
  `GLOBAL_ID_FK` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`ID`),
  UNIQUE KEY `GLOBAL_ID_FK` (`GLOBAL_ID_FK`),
  KEY `user_entity_GLOBAL_ID_FKC` (`GLOBAL_ID_FK`),
  CONSTRAINT `user_entity_GLOBAL_ID_FKC` FOREIGN KEY (`GLOBAL_ID_FK`) REFERENCES `core_global_id` (`ID`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

INSERT INTO `user_entity` VALUES (1,1);
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `user_external_auth` (
  `ID` bigint(20) NOT NULL AUTO_INCREMENT,
  `external_user_id` varchar(250) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `SYSTEM_ID` varchar(50) COLLATE utf8_unicode_ci NOT NULL,
  `KENMEI_USER_FK` bigint(20) DEFAULT NULL,
  `permanent_id` varchar(1024) COLLATE utf8_unicode_ci DEFAULT NULL,
  `additional_property` text COLLATE utf8_unicode_ci,
  PRIMARY KEY (`ID`),
  UNIQUE KEY `EXTERNAL_USER_ID` (`external_user_id`,`SYSTEM_ID`),
  KEY `user_external_auth_KENMEI_USEC` (`KENMEI_USER_FK`),
  CONSTRAINT `user_external_auth_KENMEI_USEC` FOREIGN KEY (`KENMEI_USER_FK`) REFERENCES `user_user` (`ID`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `user_group` (
  `ID` bigint(20) NOT NULL,
  `ALIAS` varchar(255) COLLATE utf8_unicode_ci NOT NULL,
  `NAME` varchar(1024) COLLATE utf8_unicode_ci NOT NULL,
  `DESCRIPTION` text COLLATE utf8_unicode_ci,
  PRIMARY KEY (`ID`),
  UNIQUE KEY `ALIAS` (`ALIAS`),
  KEY `user_groupIFKC` (`ID`),
  CONSTRAINT `user_groupIFKC` FOREIGN KEY (`ID`) REFERENCES `user_entity` (`ID`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `user_group_external` (
  `ID` bigint(20) NOT NULL,
  `external_system_id` varchar(50) COLLATE utf8_unicode_ci NOT NULL,
  `external_id` varchar(250) CHARACTER SET utf8 COLLATE utf8_bin NOT NULL,
  `additional_property` text COLLATE utf8_unicode_ci,
  PRIMARY KEY (`ID`),
  UNIQUE KEY `user_group_external_unique_key` (`external_id`,`external_system_id`),
  KEY `user_group_externalIFKC` (`ID`),
  CONSTRAINT `user_group_externalIFKC` FOREIGN KEY (`ID`) REFERENCES `user_group` (`ID`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `user_group_member` (
  `GROUP_MEMBERS_FK` bigint(20) NOT NULL,
  `GROUPS_FK` bigint(20) NOT NULL,
  PRIMARY KEY (`GROUP_MEMBERS_FK`,`GROUPS_FK`),
  KEY `user_group_GROUP_MEMBERS_FKC` (`GROUP_MEMBERS_FK`),
  KEY `user_entity_GROUPS_FKC` (`GROUPS_FK`),
  CONSTRAINT `user_entity_GROUPS_FKC` FOREIGN KEY (`GROUPS_FK`) REFERENCES `user_group` (`ID`),
  CONSTRAINT `user_group_GROUP_MEMBERS_FKC` FOREIGN KEY (`GROUP_MEMBERS_FK`) REFERENCES `user_entity` (`ID`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `user_group_property` (
  `ID` bigint(20) NOT NULL AUTO_INCREMENT,
  `key_group` varchar(128) COLLATE utf8_unicode_ci NOT NULL,
  `property_key` varchar(128) COLLATE utf8_unicode_ci NOT NULL,
  `property_value` varchar(4000) COLLATE utf8_unicode_ci NOT NULL,
  `KENMEI_ENTITY_GROUP_FK` bigint(20) DEFAULT NULL,
  `last_modification_date` datetime NOT NULL DEFAULT '1983-06-19 04:09:23',
  PRIMARY KEY (`ID`),
  KEY `user_group_property_KENMEI_ENC` (`KENMEI_ENTITY_GROUP_FK`),
  CONSTRAINT `user_group_property_KENMEI_ENC` FOREIGN KEY (`KENMEI_ENTITY_GROUP_FK`) REFERENCES `user_group` (`ID`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `user_image` (
  `ID` bigint(20) NOT NULL AUTO_INCREMENT,
  `IMAGE` longblob NOT NULL,
  PRIMARY KEY (`ID`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `user_navigation_item` (
  `ID` bigint(20) NOT NULL AUTO_INCREMENT,
  `item_index` int(11) DEFAULT NULL,
  `data` text COLLATE utf8_unicode_ci NOT NULL,
  `last_access_date` datetime NOT NULL,
  `OWNER_FK` bigint(20) NOT NULL,
  `name` varchar(255) COLLATE utf8_unicode_ci NOT NULL,
  PRIMARY KEY (`ID`),
  KEY `user_navigation_item_OWNER_FKC` (`OWNER_FK`),
  CONSTRAINT `user_navigation_item_OWNER_FKC` FOREIGN KEY (`OWNER_FK`) REFERENCES `user_user` (`ID`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

INSERT INTO `user_navigation_item` VALUES (1,-1,'{\"contextType\":\"notesOverview\",\"contextId\":null,\"filters\":{\"showFollowedItems\":true}}','2016-06-15 21:19:57',1,'following'),(2,-1,'{\"contextType\":\"notesOverview\",\"contextId\":null,\"filters\":{\"showPostsForMe\":true}}','2016-06-15 21:19:57',1,'mentions');
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `user_note_entity` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `rank` bigint(20) DEFAULT NULL,
  `note_fk` bigint(20) NOT NULL,
  `user_fk` bigint(20) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `user_note_entity_fk_idx` (`note_fk`,`user_fk`),
  KEY `user_note_entity_user_fkc` (`user_fk`),
  CONSTRAINT `user_note_entity_note_fkc` FOREIGN KEY (`note_fk`) REFERENCES `core_note` (`ID`),
  CONSTRAINT `user_note_entity_user_fkc` FOREIGN KEY (`user_fk`) REFERENCES `user_user` (`ID`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `user_note_property` (
  `ID` bigint(20) NOT NULL AUTO_INCREMENT,
  `property_key` varchar(128) COLLATE utf8_unicode_ci NOT NULL,
  `property_value` varchar(1024) COLLATE utf8_unicode_ci NOT NULL,
  `key_group` varchar(128) COLLATE utf8_unicode_ci NOT NULL,
  `last_modification_date` datetime NOT NULL DEFAULT '1983-06-19 04:09:23',
  `USER_FK` bigint(20) NOT NULL,
  `NOTE_FK` bigint(20) NOT NULL,
  `user_note_entity_fk` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`ID`),
  UNIQUE KEY `us_no_prop_constr` (`property_key`,`key_group`,`USER_FK`,`NOTE_FK`),
  KEY `user_note_property_user_note_c` (`user_note_entity_fk`),
  KEY `note_fk_idx` (`NOTE_FK`),
  KEY `user_fk_idx` (`USER_FK`),
  CONSTRAINT `user_note_property_NOTE_FKC` FOREIGN KEY (`NOTE_FK`) REFERENCES `core_note` (`ID`),
  CONSTRAINT `user_note_property_USER_FKC` FOREIGN KEY (`USER_FK`) REFERENCES `user_user` (`ID`),
  CONSTRAINT `user_note_property_user_note_c` FOREIGN KEY (`user_note_entity_fk`) REFERENCES `user_note_entity` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `user_of_group` (
  `ID` bigint(20) NOT NULL AUTO_INCREMENT,
  `MODIFICATION_TYPE` varchar(1024) COLLATE utf8_unicode_ci DEFAULT NULL,
  `GROUP_FK` bigint(20) NOT NULL,
  `USER_FK` bigint(20) NOT NULL,
  PRIMARY KEY (`ID`),
  KEY `user_of_group_GROUP_FKC` (`GROUP_FK`),
  KEY `user_of_group_USER_FKC` (`USER_FK`),
  CONSTRAINT `user_of_group_GROUP_FKC` FOREIGN KEY (`GROUP_FK`) REFERENCES `user_group` (`ID`),
  CONSTRAINT `user_of_group_USER_FKC` FOREIGN KEY (`USER_FK`) REFERENCES `user_user` (`ID`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `user_profile` (
  `ID` bigint(20) NOT NULL AUTO_INCREMENT,
  `LAST_NAME` varchar(1024) COLLATE utf8_unicode_ci DEFAULT NULL,
  `SALUTATION` varchar(1024) COLLATE utf8_unicode_ci DEFAULT NULL,
  `POSITION` varchar(1024) COLLATE utf8_unicode_ci DEFAULT NULL,
  `COMPANY` varchar(1024) COLLATE utf8_unicode_ci DEFAULT NULL,
  `FIRST_NAME` varchar(1024) COLLATE utf8_unicode_ci DEFAULT NULL,
  `LAST_MODIFICATION_DATE` timestamp NOT NULL DEFAULT '0000-00-00 00:00:00',
  `LAST_PHOTO_MODIFICATION_DATE` timestamp NOT NULL DEFAULT '0000-00-00 00:00:00',
  `TIME_ZONE_ID` varchar(1024) COLLATE utf8_unicode_ci DEFAULT NULL,
  `SMALL_IMAGE_FK` bigint(20) DEFAULT NULL,
  `CONTACT_FK` bigint(20) DEFAULT NULL,
  `MEDIUM_IMAGE_FK` bigint(20) DEFAULT NULL,
  `LARGE_IMAGE_FK` bigint(20) DEFAULT NULL,
  `NOTIFICATION_CONFIG_FK` bigint(20) NOT NULL,
  PRIMARY KEY (`ID`),
  UNIQUE KEY `NOTIFICATION_CONFIG_FK` (`NOTIFICATION_CONFIG_FK`),
  UNIQUE KEY `SMALL_IMAGE_FK` (`SMALL_IMAGE_FK`),
  UNIQUE KEY `CONTACT_FK` (`CONTACT_FK`),
  UNIQUE KEY `MEDIUM_IMAGE_FK` (`MEDIUM_IMAGE_FK`),
  UNIQUE KEY `LARGE_IMAGE_FK` (`LARGE_IMAGE_FK`),
  KEY `user_profile_SMALL_IMAGE_FKC` (`SMALL_IMAGE_FK`),
  KEY `user_profile_CONTACT_FKC` (`CONTACT_FK`),
  KEY `user_profile_NOTIFICATION_CONC` (`NOTIFICATION_CONFIG_FK`),
  KEY `user_profile_MEDIUM_IMAGE_FKC` (`MEDIUM_IMAGE_FK`),
  KEY `user_profile_LARGE_IMAGE_FKC` (`LARGE_IMAGE_FK`),
  KEY `user_profile_first_name_index` (`FIRST_NAME`(100)),
  KEY `user_profile_last_name_index` (`LAST_NAME`(100)),
  CONSTRAINT `user_profile_CONTACT_FKC` FOREIGN KEY (`CONTACT_FK`) REFERENCES `user_contact` (`ID`),
  CONSTRAINT `user_profile_LARGE_IMAGE_FKC` FOREIGN KEY (`LARGE_IMAGE_FK`) REFERENCES `user_image` (`ID`),
  CONSTRAINT `user_profile_MEDIUM_IMAGE_FKC` FOREIGN KEY (`MEDIUM_IMAGE_FK`) REFERENCES `user_image` (`ID`),
  CONSTRAINT `user_profile_NOTIFICATION_CONC` FOREIGN KEY (`NOTIFICATION_CONFIG_FK`) REFERENCES `notification_config` (`ID`),
  CONSTRAINT `user_profile_SMALL_IMAGE_FKC` FOREIGN KEY (`SMALL_IMAGE_FK`) REFERENCES `user_image` (`ID`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

INSERT INTO `user_profile` VALUES (1,'Admin',NULL,NULL,NULL,'Peter','2016-06-15 19:19:57','2016-06-15 19:19:57','time.zones.gmt.Europe/Amsterdam',NULL,NULL,NULL,NULL,1);
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `user_user` (
  `ID` bigint(20) NOT NULL,
  `PASSWORD` varchar(1024) COLLATE utf8_unicode_ci DEFAULT NULL,
  `EMAIL` varchar(255) COLLATE utf8_unicode_ci NOT NULL,
  `LANGUAGE_CODE` varchar(1024) COLLATE utf8_unicode_ci NOT NULL,
  `LAST_LOGIN` timestamp NULL DEFAULT NULL,
  `STATUS` varchar(1024) COLLATE utf8_unicode_ci NOT NULL,
  `ALIAS` varchar(255) COLLATE utf8_unicode_ci DEFAULT NULL,
  `TERMS_ACCEPTED` tinyint(4) NOT NULL,
  `REMINDER_MAIL_SENT` tinyint(4) NOT NULL,
  `STATUS_CHANGED` timestamp NOT NULL DEFAULT '0000-00-00 00:00:00',
  `PROFILE_FK` bigint(20) NOT NULL,
  `authentication_token` varchar(1024) COLLATE utf8_unicode_ci DEFAULT NULL,
  PRIMARY KEY (`ID`),
  UNIQUE KEY `EMAIL` (`EMAIL`),
  UNIQUE KEY `PROFILE_FK` (`PROFILE_FK`),
  UNIQUE KEY `ALIAS` (`ALIAS`),
  KEY `user_userIFKC` (`ID`),
  KEY `user_user_PROFILE_FKC` (`PROFILE_FK`),
  KEY `user_user_email_index` (`EMAIL`(100)),
  CONSTRAINT `user_userIFKC` FOREIGN KEY (`ID`) REFERENCES `user_entity` (`ID`),
  CONSTRAINT `user_user_PROFILE_FKC` FOREIGN KEY (`PROFILE_FK`) REFERENCES `user_profile` (`ID`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

INSERT INTO `user_user` VALUES (1,'e10adc3949ba59abbe56e057f20f883e','communote@localhost','en',NULL,'ACTIVE','communote',1,0,'2016-06-15 19:19:57',1,NULL);
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `user_user_property` (
  `ID` bigint(20) NOT NULL AUTO_INCREMENT,
  `key_group` varchar(128) COLLATE utf8_unicode_ci NOT NULL,
  `property_key` varchar(128) COLLATE utf8_unicode_ci NOT NULL,
  `property_value` varchar(4000) COLLATE utf8_unicode_ci NOT NULL,
  `KENMEI_USER_FK` bigint(20) DEFAULT NULL,
  `last_modification_date` datetime NOT NULL DEFAULT '1983-06-19 04:09:23',
  PRIMARY KEY (`ID`),
  KEY `user_user_property_KENMEI_USEC` (`KENMEI_USER_FK`),
  CONSTRAINT `user_user_property_KENMEI_USEC` FOREIGN KEY (`KENMEI_USER_FK`) REFERENCES `user_user` (`ID`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

