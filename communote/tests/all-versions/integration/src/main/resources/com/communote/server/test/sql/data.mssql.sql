/****** Object:  Table [dbo].[user_image]    Script Date: 06/17/2016 17:45:08 ******/
SET ANSI_NULLS ON;
SET QUOTED_IDENTIFIER ON;
CREATE TABLE [dbo].[user_image](
    [ID] [bigint] IDENTITY(1,1) NOT NULL,
    [image] [varbinary](max) NOT NULL,
PRIMARY KEY CLUSTERED 
(
    [ID] ASC
)WITH (PAD_INDEX  = OFF, STATISTICS_NORECOMPUTE  = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS  = ON, ALLOW_PAGE_LOCKS  = ON) ON [PRIMARY]
) ON [PRIMARY] TEXTIMAGE_ON [PRIMARY];
/****** Object:  Table [dbo].[core_content]    Script Date: 06/17/2016 17:45:08 ******/
SET ANSI_NULLS ON;
SET QUOTED_IDENTIFIER ON;
CREATE TABLE [dbo].[core_content](
    [ID] [bigint] IDENTITY(1,1) NOT NULL,
    [content] [nvarchar](max) NOT NULL,
    [short_content] [nvarchar](max) NULL,
PRIMARY KEY CLUSTERED 
(
    [ID] ASC
)WITH (PAD_INDEX  = OFF, STATISTICS_NORECOMPUTE  = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS  = ON, ALLOW_PAGE_LOCKS  = ON) ON [PRIMARY]
) ON [PRIMARY] TEXTIMAGE_ON [PRIMARY];
/****** Object:  Table [dbo].[core_blog2blog_resolved]    Script Date: 06/17/2016 17:45:08 ******/
SET ANSI_NULLS ON;
SET QUOTED_IDENTIFIER ON;
CREATE TABLE [dbo].[core_blog2blog_resolved](
    [ID] [bigint] IDENTITY(1,1) NOT NULL,
    [parent_topic_id] [bigint] NOT NULL,
    [child_topic_id] [bigint] NOT NULL,
    [topic_path] [varchar](255) NOT NULL,
 CONSTRAINT [PK_CORE_BLOG2BLOG_RESOLVED] PRIMARY KEY CLUSTERED 
(
    [ID] ASC
)WITH (PAD_INDEX  = OFF, STATISTICS_NORECOMPUTE  = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS  = ON, ALLOW_PAGE_LOCKS  = ON) ON [PRIMARY],
 CONSTRAINT [kenmei6192cnstrnt] UNIQUE NONCLUSTERED 
(
    [parent_topic_id] ASC,
    [child_topic_id] ASC,
    [topic_path] ASC
)WITH (PAD_INDEX  = OFF, STATISTICS_NORECOMPUTE  = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS  = ON, ALLOW_PAGE_LOCKS  = ON) ON [PRIMARY]
) ON [PRIMARY];
/****** Object:  Table [dbo].[configuration_app_setting]    Script Date: 06/17/2016 17:45:08 ******/
SET ANSI_NULLS ON;
SET QUOTED_IDENTIFIER ON;
CREATE TABLE [dbo].[configuration_app_setting](
    [SETTING_KEY] [nvarchar](255) NOT NULL,
    [setting_value] [nvarchar](max) NULL,
 CONSTRAINT [pk_configuration_app_setting] PRIMARY KEY CLUSTERED 
(
    [SETTING_KEY] ASC
)WITH (PAD_INDEX  = OFF, STATISTICS_NORECOMPUTE  = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS  = ON, ALLOW_PAGE_LOCKS  = ON) ON [PRIMARY]
) ON [PRIMARY] TEXTIMAGE_ON [PRIMARY];
INSERT [dbo].[configuration_app_setting] ([SETTING_KEY], [setting_value]) VALUES (N'com.communote.core.keystore.password', N'a3e0797d-0fe9-4dc1-9425-15dc9751e6ff')
INSERT [dbo].[configuration_app_setting] ([SETTING_KEY], [setting_value]) VALUES (N'communote.standalone.installation', N'true')
INSERT [dbo].[configuration_app_setting] ([SETTING_KEY], [setting_value]) VALUES (N'installation.date', N'8MWbZi-aiim_ZuLadeuHRg:Zg7Y3cI9nerX6f4')
INSERT [dbo].[configuration_app_setting] ([SETTING_KEY], [setting_value]) VALUES (N'installation.unique.id', N'93fb4ef2-7b9b-4001-9f12-aa00fd90c0c6')
INSERT [dbo].[configuration_app_setting] ([SETTING_KEY], [setting_value]) VALUES (N'kenmei.attachment.max.upload.size', N'10485760')
INSERT [dbo].[configuration_app_setting] ([SETTING_KEY], [setting_value]) VALUES (N'kenmei.captcha.disable', N'true')
INSERT [dbo].[configuration_app_setting] ([SETTING_KEY], [setting_value]) VALUES (N'kenmei.crc.file.repository.storage.dir.root', N'/tmp/communote-test/data/filerepository')
INSERT [dbo].[configuration_app_setting] ([SETTING_KEY], [setting_value]) VALUES (N'kenmei.image.max.upload.size', N'1048576')
INSERT [dbo].[configuration_app_setting] ([SETTING_KEY], [setting_value]) VALUES (N'kenmei.trusted.ca.keystore.password', N'8738d33e-949e-4bbc-8e19-50bfc14bd7d0')
INSERT [dbo].[configuration_app_setting] ([SETTING_KEY], [setting_value]) VALUES (N'mailing.from.address', N'communote-installer-test@localhost')
INSERT [dbo].[configuration_app_setting] ([SETTING_KEY], [setting_value]) VALUES (N'mailing.from.address.name', N'[Local Test] Communote-Team')
INSERT [dbo].[configuration_app_setting] ([SETTING_KEY], [setting_value]) VALUES (N'mailing.host', N'localhost')
INSERT [dbo].[configuration_app_setting] ([SETTING_KEY], [setting_value]) VALUES (N'mailing.port', N'25')
INSERT [dbo].[configuration_app_setting] ([SETTING_KEY], [setting_value]) VALUES (N'virus.scanner.enabled', N'false')
/****** Object:  Table [dbo].[con_test]    Script Date: 06/17/2016 17:45:08 ******/
SET ANSI_NULLS ON;
SET QUOTED_IDENTIFIER ON;
CREATE TABLE [dbo].[con_test](
    [a] [char](1) NULL
) ON [PRIMARY];
/****** Object:  Table [dbo].[channel_configuration]    Script Date: 06/17/2016 17:45:08 ******/
SET ANSI_NULLS ON;
SET QUOTED_IDENTIFIER ON;
CREATE TABLE [dbo].[channel_configuration](
    [ID] [bigint] IDENTITY(1,1) NOT NULL,
    [force_ssl] [bit] NOT NULL,
    [channel_type] [nvarchar](1024) NOT NULL,
PRIMARY KEY CLUSTERED 
(
    [ID] ASC
)WITH (PAD_INDEX  = OFF, STATISTICS_NORECOMPUTE  = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS  = ON, ALLOW_PAGE_LOCKS  = ON) ON [PRIMARY]
) ON [PRIMARY];
/****** Object:  Table [dbo].[configuration_ldap_search]    Script Date: 06/17/2016 17:45:08 ******/
SET ANSI_NULLS ON;
SET QUOTED_IDENTIFIER ON;
CREATE TABLE [dbo].[configuration_ldap_search](
    [ID] [bigint] IDENTITY(1,1) NOT NULL,
    [search_filter] [nvarchar](1024) NULL,
    [property_mapping] [nvarchar](1024) NOT NULL,
PRIMARY KEY CLUSTERED 
(
    [ID] ASC
)WITH (PAD_INDEX  = OFF, STATISTICS_NORECOMPUTE  = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS  = ON, ALLOW_PAGE_LOCKS  = ON) ON [PRIMARY]
) ON [PRIMARY];
/****** Object:  Table [dbo].[core_global_string_property]    Script Date: 06/17/2016 17:45:08 ******/
SET ANSI_NULLS ON;
SET QUOTED_IDENTIFIER ON;
CREATE TABLE [dbo].[core_global_string_property](
    [ID] [bigint] IDENTITY(1,1) NOT NULL,
    [property_value] [nvarchar](max) NOT NULL,
    [key_group] [nvarchar](1024) NOT NULL,
    [property_key] [nvarchar](1024) NOT NULL,
    [last_modification_date] [datetime2](7) NULL,
PRIMARY KEY CLUSTERED 
(
    [ID] ASC
)WITH (PAD_INDEX  = OFF, STATISTICS_NORECOMPUTE  = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS  = ON, ALLOW_PAGE_LOCKS  = ON) ON [PRIMARY]
) ON [PRIMARY] TEXTIMAGE_ON [PRIMARY];
/****** Object:  Table [dbo].[core_global_id]    Script Date: 06/17/2016 17:45:08 ******/
SET ANSI_NULLS ON;
SET QUOTED_IDENTIFIER ON;
CREATE TABLE [dbo].[core_global_id](
    [ID] [bigint] IDENTITY(1,1) NOT NULL,
    [global_identifier] [nvarchar](255) NOT NULL,
PRIMARY KEY CLUSTERED 
(
    [ID] ASC
)WITH (PAD_INDEX  = OFF, STATISTICS_NORECOMPUTE  = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS  = ON, ALLOW_PAGE_LOCKS  = ON) ON [PRIMARY],
 CONSTRAINT [core_global_id_global_identifier_key] UNIQUE NONCLUSTERED 
(
    [global_identifier] ASC
)WITH (PAD_INDEX  = OFF, STATISTICS_NORECOMPUTE  = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS  = ON, ALLOW_PAGE_LOCKS  = ON) ON [PRIMARY]
) ON [PRIMARY];
SET IDENTITY_INSERT [dbo].[core_global_id] ON
INSERT [dbo].[core_global_id] ([ID], [global_identifier]) VALUES (3, N'/66116533-eb19-4239-a1a0-47c479c48d51/blog/1')
INSERT [dbo].[core_global_id] ([ID], [global_identifier]) VALUES (2, N'/66116533-eb19-4239-a1a0-47c479c48d51/tag/1')
INSERT [dbo].[core_global_id] ([ID], [global_identifier]) VALUES (1, N'/66116533-eb19-4239-a1a0-47c479c48d51/user/1')
SET IDENTITY_INSERT [dbo].[core_global_id] OFF
/****** Object:  Table [dbo].[core_global_binary_prop]    Script Date: 06/17/2016 17:45:08 ******/
SET ANSI_NULLS ON;
SET QUOTED_IDENTIFIER ON;
CREATE TABLE [dbo].[core_global_binary_prop](
    [ID] [bigint] IDENTITY(1,1) NOT NULL,
    [key_group] [nvarchar](128) NOT NULL,
    [property_key] [nvarchar](128) NOT NULL,
    [last_modification_date] [datetime2](7) NULL,
    [property_value] [varbinary](max) NOT NULL,
 CONSTRAINT [pk_core_global_binary_property] PRIMARY KEY CLUSTERED 
(
    [ID] ASC
)WITH (PAD_INDEX  = OFF, STATISTICS_NORECOMPUTE  = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS  = ON, ALLOW_PAGE_LOCKS  = ON) ON [PRIMARY]
) ON [PRIMARY] TEXTIMAGE_ON [PRIMARY];
/****** Object:  Table [dbo].[core_role2blog]    Script Date: 06/17/2016 17:45:08 ******/
SET ANSI_NULLS ON;
SET QUOTED_IDENTIFIER ON;
CREATE TABLE [dbo].[core_role2blog](
    [ID] [bigint] IDENTITY(1,1) NOT NULL,
    [blog_id] [bigint] NOT NULL,
    [user_id] [bigint] NOT NULL,
    [numeric_role] [int] NOT NULL,
    [external_system_id] [nvarchar](50) NULL,
    [granted_by_group] [bit] NOT NULL,
PRIMARY KEY CLUSTERED 
(
    [ID] ASC
)WITH (PAD_INDEX  = OFF, STATISTICS_NORECOMPUTE  = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS  = ON, ALLOW_PAGE_LOCKS  = ON) ON [PRIMARY]
) ON [PRIMARY];
CREATE NONCLUSTERED INDEX [core_role2blog_bidx] ON [dbo].[core_role2blog] 
(
    [blog_id] ASC
)WITH (PAD_INDEX  = OFF, STATISTICS_NORECOMPUTE  = OFF, SORT_IN_TEMPDB = OFF, IGNORE_DUP_KEY = OFF, DROP_EXISTING = OFF, ONLINE = OFF, ALLOW_ROW_LOCKS  = ON, ALLOW_PAGE_LOCKS  = ON) ON [PRIMARY];
CREATE NONCLUSTERED INDEX [core_role2blog_role_idx] ON [dbo].[core_role2blog] 
(
    [blog_id] ASC,
    [user_id] ASC,
    [numeric_role] ASC
)WITH (PAD_INDEX  = OFF, STATISTICS_NORECOMPUTE  = OFF, SORT_IN_TEMPDB = OFF, IGNORE_DUP_KEY = OFF, DROP_EXISTING = OFF, ONLINE = OFF, ALLOW_ROW_LOCKS  = ON, ALLOW_PAGE_LOCKS  = ON) ON [PRIMARY];
CREATE NONCLUSTERED INDEX [core_role2blog_uidx] ON [dbo].[core_role2blog] 
(
    [user_id] ASC
)WITH (PAD_INDEX  = OFF, STATISTICS_NORECOMPUTE  = OFF, SORT_IN_TEMPDB = OFF, IGNORE_DUP_KEY = OFF, DROP_EXISTING = OFF, ONLINE = OFF, ALLOW_ROW_LOCKS  = ON, ALLOW_PAGE_LOCKS  = ON) ON [PRIMARY];
SET IDENTITY_INSERT [dbo].[core_role2blog] ON
INSERT [dbo].[core_role2blog] ([ID], [blog_id], [user_id], [numeric_role], [external_system_id], [granted_by_group]) VALUES (1, 1, 1, 3, NULL, 0)
SET IDENTITY_INSERT [dbo].[core_role2blog] OFF
/****** Object:  Table [dbo].[core_processed_utp_mail]    Script Date: 06/17/2016 17:45:08 ******/
SET ANSI_NULLS ON;
SET QUOTED_IDENTIFIER ON;
CREATE TABLE [dbo].[core_processed_utp_mail](
    [ID] [bigint] IDENTITY(1,1) NOT NULL,
    [mail_message_id] [nvarchar](255) NOT NULL,
PRIMARY KEY CLUSTERED 
(
    [ID] ASC
)WITH (PAD_INDEX  = OFF, STATISTICS_NORECOMPUTE  = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS  = ON, ALLOW_PAGE_LOCKS  = ON) ON [PRIMARY],
 CONSTRAINT [core_processed_utp_mail_mail_message_id_key] UNIQUE NONCLUSTERED 
(
    [mail_message_id] ASC
)WITH (PAD_INDEX  = OFF, STATISTICS_NORECOMPUTE  = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS  = ON, ALLOW_PAGE_LOCKS  = ON) ON [PRIMARY]
) ON [PRIMARY];
/****** Object:  Table [dbo].[core_plugin_properties]    Script Date: 06/17/2016 17:45:08 ******/
SET ANSI_NULLS ON;
SET QUOTED_IDENTIFIER ON;
CREATE TABLE [dbo].[core_plugin_properties](
    [ID] [bigint] IDENTITY(1,1) NOT NULL,
    [key_group] [nvarchar](128) NULL,
    [property_key] [nvarchar](128) NULL,
    [property_value] [nvarchar](max) NULL,
    [last_modification_date] [datetime2](7) NULL,
    [application_property] [bit] NULL,
 CONSTRAINT [PK_CORE_PLUGIN_PROPERTIES] PRIMARY KEY CLUSTERED 
(
    [ID] ASC
)WITH (PAD_INDEX  = OFF, STATISTICS_NORECOMPUTE  = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS  = ON, ALLOW_PAGE_LOCKS  = ON) ON [PRIMARY],
 CONSTRAINT [plugin_prop_cnst] UNIQUE NONCLUSTERED 
(
    [property_key] ASC,
    [key_group] ASC
)WITH (PAD_INDEX  = OFF, STATISTICS_NORECOMPUTE  = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS  = ON, ALLOW_PAGE_LOCKS  = ON) ON [PRIMARY]
) ON [PRIMARY] TEXTIMAGE_ON [PRIMARY];
/****** Object:  Table [dbo].[core_tag_category]    Script Date: 06/17/2016 17:45:08 ******/
SET ANSI_NULLS ON;
SET QUOTED_IDENTIFIER ON;
CREATE TABLE [dbo].[core_tag_category](
    [ID] [bigint] IDENTITY(1,1) NOT NULL,
    [class] [nvarchar](255) NOT NULL,
    [name] [nvarchar](1024) NOT NULL,
    [prefix] [nvarchar](1024) NOT NULL,
    [description] [nvarchar](1024) NULL,
    [multiple_tags] [bit] NOT NULL,
PRIMARY KEY CLUSTERED 
(
    [ID] ASC
)WITH (PAD_INDEX  = OFF, STATISTICS_NORECOMPUTE  = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS  = ON, ALLOW_PAGE_LOCKS  = ON) ON [PRIMARY]
) ON [PRIMARY];
/****** Object:  Table [dbo].[core_task]    Script Date: 06/17/2016 17:45:08 ******/
SET ANSI_NULLS ON;
SET QUOTED_IDENTIFIER ON;
CREATE TABLE [dbo].[core_task](
    [ID] [bigint] IDENTITY(1,1) NOT NULL,
    [unique_name] [nvarchar](255) NOT NULL,
    [task_status] [nvarchar](255) NOT NULL,
    [task_interval] [bigint] NULL,
    [handler_class_name] [nvarchar](1024) NOT NULL,
    [active] [bit] NOT NULL,
    [next_execution] [datetime2](7) NULL,
    [last_execution] [datetime2](7) NULL,
PRIMARY KEY CLUSTERED 
(
    [ID] ASC
)WITH (PAD_INDEX  = OFF, STATISTICS_NORECOMPUTE  = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS  = ON, ALLOW_PAGE_LOCKS  = ON) ON [PRIMARY],
 CONSTRAINT [core_task_unique_name_key] UNIQUE NONCLUSTERED 
(
    [unique_name] ASC
)WITH (PAD_INDEX  = OFF, STATISTICS_NORECOMPUTE  = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS  = ON, ALLOW_PAGE_LOCKS  = ON) ON [PRIMARY]
) ON [PRIMARY];
SET IDENTITY_INSERT [dbo].[core_task] ON
INSERT [dbo].[core_task] ([ID], [unique_name], [task_status], [task_interval], [handler_class_name], [active], [next_execution], [last_execution]) VALUES (1, N'SynchronizeGroups', N'PENDING', 3600000, N'com.communote.server.core.user.groups.UserGroupSynchronizationTaskHandler', 1, CAST(0x0730D2674393833B0B AS DateTime2), NULL)
INSERT [dbo].[core_task] ([ID], [unique_name], [task_status], [task_interval], [handler_class_name], [active], [next_execution], [last_execution]) VALUES (2, N'RemindUsers', N'PENDING', 8640000, N'com.communote.server.core.user.RemindUserJob', 1, CAST(0x0730D2674393833B0B AS DateTime2), NULL)
INSERT [dbo].[core_task] ([ID], [unique_name], [task_status], [task_interval], [handler_class_name], [active], [next_execution], [last_execution]) VALUES (3, N'DeleteOrphanedAttachments', N'PENDING', 604800000, N'com.communote.server.core.tasks.DeleteOrphanedAttachmentsTaskHandler', 1, CAST(0x07D09CFEB5AB833B0B AS DateTime2), NULL)
SET IDENTITY_INSERT [dbo].[core_task] OFF
/****** Object:  Table [dbo].[iprange_filter]    Script Date: 06/17/2016 17:45:08 ******/
SET ANSI_NULLS ON;
SET QUOTED_IDENTIFIER ON;
CREATE TABLE [dbo].[iprange_filter](
    [ID] [bigint] IDENTITY(1,1) NOT NULL,
    [name] [nvarchar](1024) NOT NULL,
    [enabled] [bit] NOT NULL,
PRIMARY KEY CLUSTERED 
(
    [ID] ASC
)WITH (PAD_INDEX  = OFF, STATISTICS_NORECOMPUTE  = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS  = ON, ALLOW_PAGE_LOCKS  = ON) ON [PRIMARY]
) ON [PRIMARY];
/****** Object:  Table [dbo].[iprange_channel]    Script Date: 06/17/2016 17:45:08 ******/
SET ANSI_NULLS ON;
SET QUOTED_IDENTIFIER ON;
CREATE TABLE [dbo].[iprange_channel](
    [TYPE] [nvarchar](255) NOT NULL,
    [enabled] [bit] NOT NULL,
PRIMARY KEY CLUSTERED 
(
    [TYPE] ASC
)WITH (PAD_INDEX  = OFF, STATISTICS_NORECOMPUTE  = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS  = ON, ALLOW_PAGE_LOCKS  = ON) ON [PRIMARY]
) ON [PRIMARY];
/****** Object:  Table [dbo].[DATABASECHANGELOGLOCK]    Script Date: 06/17/2016 17:45:08 ******/
SET ANSI_NULLS ON;
SET QUOTED_IDENTIFIER ON;
CREATE TABLE [dbo].[DATABASECHANGELOGLOCK](
    [ID] [int] NOT NULL,
    [LOCKED] [bit] NOT NULL,
    [LOCKGRANTED] [datetime] NULL,
    [LOCKEDBY] [varchar](255) NULL,
 CONSTRAINT [PK_DATABASECHANGELOGLOCK] PRIMARY KEY CLUSTERED 
(
    [ID] ASC
)WITH (PAD_INDEX  = OFF, STATISTICS_NORECOMPUTE  = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS  = ON, ALLOW_PAGE_LOCKS  = ON) ON [PRIMARY]
) ON [PRIMARY];
INSERT [dbo].[DATABASECHANGELOGLOCK] ([ID], [LOCKED], [LOCKGRANTED], [LOCKEDBY]) VALUES (1, 0, NULL, NULL)
/****** Object:  Table [dbo].[DATABASECHANGELOG]    Script Date: 06/17/2016 17:45:08 ******/
SET ANSI_NULLS ON;
SET QUOTED_IDENTIFIER ON;
CREATE TABLE [dbo].[DATABASECHANGELOG](
    [ID] [varchar](63) NOT NULL,
    [AUTHOR] [varchar](63) NOT NULL,
    [FILENAME] [varchar](200) NOT NULL,
    [DATEEXECUTED] [datetime] NOT NULL,
    [MD5SUM] [varchar](32) NULL,
    [DESCRIPTION] [varchar](255) NULL,
    [COMMENTS] [varchar](255) NULL,
    [TAG] [varchar](255) NULL,
    [LIQUIBASE] [varchar](10) NULL,
 CONSTRAINT [PK_DATABASECHANGELOG] PRIMARY KEY CLUSTERED 
(
    [ID] ASC,
    [AUTHOR] ASC,
    [FILENAME] ASC
)WITH (PAD_INDEX  = OFF, STATISTICS_NORECOMPUTE  = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS  = ON, ALLOW_PAGE_LOCKS  = ON) ON [PRIMARY]
) ON [PRIMARY];
INSERT [dbo].[DATABASECHANGELOG] ([ID], [AUTHOR], [FILENAME], [DATEEXECUTED], [MD5SUM], [DESCRIPTION], [COMMENTS], [TAG], [LIQUIBASE]) VALUES (N'20091007_mysql', N'unknown', N'de/communardo/kenmei/database/update/v1.0.3/db.changelog.v1.0.3.xml', CAST(0x00009E8000A80228 AS DateTime), N'89652831b7f99da19387e93f72471b', N'Custom SQL', N'', NULL, N'1.9.2')
INSERT [dbo].[DATABASECHANGELOG] ([ID], [AUTHOR], [FILENAME], [DATEEXECUTED], [MD5SUM], [DESCRIPTION], [COMMENTS], [TAG], [LIQUIBASE]) VALUES (N'Add_binary_property_autoincrement', N'unknown', N'de/communardo/kenmei/database/update/v1.3/db.changelog.v1.3.xml', CAST(0x00009E8000A814E8 AS DateTime), N'75e9bb7923a12a020d14d3cda8a11', N'Set Column as Auto-Increment', N'', NULL, N'1.9.2')
INSERT [dbo].[DATABASECHANGELOG] ([ID], [AUTHOR], [FILENAME], [DATEEXECUTED], [MD5SUM], [DESCRIPTION], [COMMENTS], [TAG], [LIQUIBASE]) VALUES (N'ADD_INSTALLATION_ID', N'unknown', N'de/communardo/kenmei/database/update_2nd_pass/v1.1.4/db.changelog.v1.1.4.xml', CAST(0x0000A62801202819 AS DateTime), N'467db414d6b587dab40689753ecef1', N'Custom Change', N'Inserts an unique id to the application properties.', NULL, N'1.9.5')
INSERT [dbo].[DATABASECHANGELOG] ([ID], [AUTHOR], [FILENAME], [DATEEXECUTED], [MD5SUM], [DESCRIPTION], [COMMENTS], [TAG], [LIQUIBASE]) VALUES (N'Add_Last_Modification_Date_To_Properties', N'unknown', N'de/communardo/kenmei/database/update/v1.3/db.changelog.v1.3.xml', CAST(0x00009E8000A8186C AS DateTime), N'a452ff6128142840f7a082334267d02b', N'Add Column (x4)', N'', NULL, N'1.9.2')
INSERT [dbo].[DATABASECHANGELOG] ([ID], [AUTHOR], [FILENAME], [DATEEXECUTED], [MD5SUM], [DESCRIPTION], [COMMENTS], [TAG], [LIQUIBASE]) VALUES (N'confluence_set_basepath_mysql', N'unknown', N'de/communardo/kenmei/database/update/v1.1.3/db.changelog.v1.1.3.xml', CAST(0x00009E8000A80228 AS DateTime), N'8dab40b53251883889955704653be51', N'Custom SQL', N'', NULL, N'1.9.2')
INSERT [dbo].[DATABASECHANGELOG] ([ID], [AUTHOR], [FILENAME], [DATEEXECUTED], [MD5SUM], [DESCRIPTION], [COMMENTS], [TAG], [LIQUIBASE]) VALUES (N'CR_115_Add_Application_Settings_1', N'unknown', N'de/communardo/kenmei/database/update/v1.1.4/db.changelog.v1.1.4.xml', CAST(0x00009E8000A80930 AS DateTime), N'63fdc9f116fec14a18b3c3caaa2f6c26', N'Create Table', N'', NULL, N'1.9.2')
INSERT [dbo].[DATABASECHANGELOG] ([ID], [AUTHOR], [FILENAME], [DATEEXECUTED], [MD5SUM], [DESCRIPTION], [COMMENTS], [TAG], [LIQUIBASE]) VALUES (N'CR_115_Add_Application_Settings_Fix_Engine', N'unknown', N'de/communardo/kenmei/database/update/v1.1.4/db.changelog.v1.1.4.xml', CAST(0x00009E8000A80930 AS DateTime), N'3da6fc71eb27642e1c85fb6cc7856b1', N'Custom SQL', N'', NULL, N'1.9.2')
INSERT [dbo].[DATABASECHANGELOG] ([ID], [AUTHOR], [FILENAME], [DATEEXECUTED], [MD5SUM], [DESCRIPTION], [COMMENTS], [TAG], [LIQUIBASE]) VALUES (N'CR_115_Move_Application_Properties_Into_DB', N'unknown', N'de/communardo/kenmei/database/update_2nd_pass/v1.1.4/db.changelog.v1.1.4.xml', CAST(0x00009E8000A81D1C AS DateTime), N'57ff61206a4cf95e4a917e86e6bb0a8', N'Custom Change', N'Get rid of property files and store the properties in the DB.', NULL, N'1.9.2')
INSERT [dbo].[DATABASECHANGELOG] ([ID], [AUTHOR], [FILENAME], [DATEEXECUTED], [MD5SUM], [DESCRIPTION], [COMMENTS], [TAG], [LIQUIBASE]) VALUES (N'CR_115_Refactor_URL_Application_Properties', N'unknown', N'de/communardo/kenmei/database/update_2nd_pass/v1.1.4/db.changelog.v1.1.4.xml', CAST(0x00009E8000A81D1C AS DateTime), N'5ff5d0d59e42e57f8e949791cfad13e', N'Custom Change', N'Convert old URL related properties to the new properties.', NULL, N'1.9.2')
INSERT [dbo].[DATABASECHANGELOG] ([ID], [AUTHOR], [FILENAME], [DATEEXECUTED], [MD5SUM], [DESCRIPTION], [COMMENTS], [TAG], [LIQUIBASE]) VALUES (N'CR_115_Remove_Client_Proxy_Url', N'unknown', N'de/communardo/kenmei/database/update/v1.1.4/db.changelog.v1.1.4.xml', CAST(0x00009E8000A80930 AS DateTime), N'6ff27dc938b73dd590d32872eef4b068', N'Drop Column', N'', NULL, N'1.9.2')
INSERT [dbo].[DATABASECHANGELOG] ([ID], [AUTHOR], [FILENAME], [DATEEXECUTED], [MD5SUM], [DESCRIPTION], [COMMENTS], [TAG], [LIQUIBASE]) VALUES (N'cr_179_add_is_Html_column_v2', N'unknown', N'de/communardo/kenmei/database/update/v1.1.3/db.changelog.v1.1.3.xml', CAST(0x00009E8000A80228 AS DateTime), N'51c815f15bbb47cb1990c3f556b2fe', N'Add Column, Add Not-Null Constraint', N'', NULL, N'1.9.2')
INSERT [dbo].[DATABASECHANGELOG] ([ID], [AUTHOR], [FILENAME], [DATEEXECUTED], [MD5SUM], [DESCRIPTION], [COMMENTS], [TAG], [LIQUIBASE]) VALUES (N'cr_179_add_messages_table_fix_key_column', N'unknown', N'de/communardo/kenmei/database/update/v1.1.3/db.changelog.v1.1.3.xml', CAST(0x00009E8000A80228 AS DateTime), N'db6273f493f2c16fa8c63f1a43ea57', N'Modify Column', N'', NULL, N'1.9.2')
INSERT [dbo].[DATABASECHANGELOG] ([ID], [AUTHOR], [FILENAME], [DATEEXECUTED], [MD5SUM], [DESCRIPTION], [COMMENTS], [TAG], [LIQUIBASE]) VALUES (N'cr_179_add_messages_table_v2', N'unknown', N'de/communardo/kenmei/database/update/v1.1.3/db.changelog.v1.1.3.xml', CAST(0x00009E8000A80228 AS DateTime), N'51bf6b30763f19d84b1ba2aeac1fd14', N'Create Table, Add Foreign Key Constraint, Add Unique Constraint', N'', NULL, N'1.9.2')
INSERT [dbo].[DATABASECHANGELOG] ([ID], [AUTHOR], [FILENAME], [DATEEXECUTED], [MD5SUM], [DESCRIPTION], [COMMENTS], [TAG], [LIQUIBASE]) VALUES (N'cr_179_insert_default_values_for_imprint_terms_of_use_mysql', N'unknown', N'de/communardo/kenmei/database/update/v1.1.3/db.changelog.v1.1.3.xml', CAST(0x00009E8000A80228 AS DateTime), N'c7e584ebae6171b861bd5a37f4be42', N'Custom SQL', N'', NULL, N'1.9.2')
INSERT [dbo].[DATABASECHANGELOG] ([ID], [AUTHOR], [FILENAME], [DATEEXECUTED], [MD5SUM], [DESCRIPTION], [COMMENTS], [TAG], [LIQUIBASE]) VALUES (N'cr135', N'amo', N'de/communardo/kenmei/database/update/v1.1.3/db.changelog.v1.1.3.xml', CAST(0x00009E8000A80228 AS DateTime), N'a02a9368d39aadb2b9affeff80229c', N'Create Table, Add Foreign Key Constraint', N'CR 135 - User group synchronization', NULL, N'1.9.2')
INSERT [dbo].[DATABASECHANGELOG] ([ID], [AUTHOR], [FILENAME], [DATEEXECUTED], [MD5SUM], [DESCRIPTION], [COMMENTS], [TAG], [LIQUIBASE]) VALUES (N'cr135_1', N'unknown', N'de/communardo/kenmei/database/update/v1.1.3/db.changelog.v1.1.3.xml', CAST(0x00009E8000A80228 AS DateTime), N'318b839a3fff783a8f4a4a3e7f5866', N'Add Column', N'Add serviceUrl to confluence configuration', NULL, N'1.9.2')
INSERT [dbo].[DATABASECHANGELOG] ([ID], [AUTHOR], [FILENAME], [DATEEXECUTED], [MD5SUM], [DESCRIPTION], [COMMENTS], [TAG], [LIQUIBASE]) VALUES (N'cr69_user_groups_v2', N'unknown', N'de/communardo/kenmei/database/update/v1.1.3/db.changelog.v1.1.3.xml', CAST(0x00009E8000A80228 AS DateTime), N'41e53effcad9d4fe485b75dab0922bf1', N'Rename Column', N'', NULL, N'1.9.2')
INSERT [dbo].[DATABASECHANGELOG] ([ID], [AUTHOR], [FILENAME], [DATEEXECUTED], [MD5SUM], [DESCRIPTION], [COMMENTS], [TAG], [LIQUIBASE]) VALUES (N'Create_Default_Blog_Where_Missing', N'unknown', N'de/communardo/kenmei/database/update_2nd_pass/v1.1.4/db.changelog.v1.1.4.xml', CAST(0x00009E8000A81E48 AS DateTime), N'b5f386a532aacea1a461a7be18459a52', N'Custom Change', N'Create the default blog for clients where there is none yet.', NULL, N'1.9.2')
INSERT [dbo].[DATABASECHANGELOG] ([ID], [AUTHOR], [FILENAME], [DATEEXECUTED], [MD5SUM], [DESCRIPTION], [COMMENTS], [TAG], [LIQUIBASE]) VALUES (N'Create_encrypted_creation_date', N'unknown', N'de/communardo/kenmei/database/update_2nd_pass/v1.1.4/db.changelog.v1.1.4.xml', CAST(0x00009E8000A81E48 AS DateTime), N'b8279228f0818d3e3d42ba4a1984199', N'Custom Change', N'No comment.', NULL, N'1.9.2')
INSERT [dbo].[DATABASECHANGELOG] ([ID], [AUTHOR], [FILENAME], [DATEEXECUTED], [MD5SUM], [DESCRIPTION], [COMMENTS], [TAG], [LIQUIBASE]) VALUES (N'Fix_Column_Size_In_ROLE2BLOG', N'unknown', N'de/communardo/kenmei/database/update/v1.2/db.changelog.v1.2.xml', CAST(0x00009E8000A81164 AS DateTime), N'2a9f758ec4728aa16fd4bc3c8c22d19', N'Modify Column', N'Fix of bug in installer script', NULL, N'1.9.2')
INSERT [dbo].[DATABASECHANGELOG] ([ID], [AUTHOR], [FILENAME], [DATEEXECUTED], [MD5SUM], [DESCRIPTION], [COMMENTS], [TAG], [LIQUIBASE]) VALUES (N'initialize-database-schema_mssql', N'unknown', N'de/communardo/kenmei/database/install/mssql/db.changelog.init-db.mssql.xml', CAST(0x0000A6280120207C AS DateTime), N'8ae84bf41147cae3a58615d5f3dcdb', N'SQL From File', N'Initialize the Database Schema', NULL, N'1.9.5')
INSERT [dbo].[DATABASECHANGELOG] ([ID], [AUTHOR], [FILENAME], [DATEEXECUTED], [MD5SUM], [DESCRIPTION], [COMMENTS], [TAG], [LIQUIBASE]) VALUES (N'initialize-database-schema_mysql_v1', N'unknown', N'de/communardo/kenmei/database/install/mysql/db.changelog.init-db.mysql.xml', CAST(0x00009E8000A80228 AS DateTime), N'91754b8c92f026f912112d6e6fe849e0', N'SQL From File', N'Initialize the Database Schema', NULL, N'1.9.2')
INSERT [dbo].[DATABASECHANGELOG] ([ID], [AUTHOR], [FILENAME], [DATEEXECUTED], [MD5SUM], [DESCRIPTION], [COMMENTS], [TAG], [LIQUIBASE]) VALUES (N'installation_type_protection', N'Communote', N'com/communote/server/liquibase/update/v3.x/db.changelog.v3.4.xml', CAST(0x0000A628012023AC AS DateTime), N'ff39513379a4a6b048f2ad17774313a1', N'Insert Row', N'', NULL, N'1.9.5')
INSERT [dbo].[DATABASECHANGELOG] ([ID], [AUTHOR], [FILENAME], [DATEEXECUTED], [MD5SUM], [DESCRIPTION], [COMMENTS], [TAG], [LIQUIBASE]) VALUES (N'jt1528_convert_core_note_to_innoDB', N'unknown', N'de/communardo/kenmei/database/update/v1.1.3/db.changelog.v1.1.3.xml', CAST(0x00009E8000A80228 AS DateTime), N'5c59d4c1140af9231be2cdf42c265d', N'Custom SQL', N'Change engine of core_note to innoDB', NULL, N'1.9.2')
INSERT [dbo].[DATABASECHANGELOG] ([ID], [AUTHOR], [FILENAME], [DATEEXECUTED], [MD5SUM], [DESCRIPTION], [COMMENTS], [TAG], [LIQUIBASE]) VALUES (N'KENMEI_1555_Rename_reserved_keywords', N'unknown', N'de/communardo/kenmei/database/update/v1.1.4/db.changelog.v1.1.4.xml', CAST(0x00009E8000A80A5C AS DateTime), N'd8bb446dfba4c335fa7ecc4f5381a919', N'Rename Column (x2)', N'', NULL, N'1.9.2')
INSERT [dbo].[DATABASECHANGELOG] ([ID], [AUTHOR], [FILENAME], [DATEEXECUTED], [MD5SUM], [DESCRIPTION], [COMMENTS], [TAG], [LIQUIBASE]) VALUES (N'KENMEI_1577_Remove_crc_filesystem_config', N'unknown', N'de/communardo/kenmei/database/update/v1.1.4/db.changelog.v1.1.4.xml', CAST(0x00009E8000A80930 AS DateTime), N'f675d97e34cf9e8f6f42c08df83e4366', N'Drop Table', N'', NULL, N'1.9.2')
INSERT [dbo].[DATABASECHANGELOG] ([ID], [AUTHOR], [FILENAME], [DATEEXECUTED], [MD5SUM], [DESCRIPTION], [COMMENTS], [TAG], [LIQUIBASE]) VALUES (N'KENMEI_1941_Follow_BE_Add_Followable_Items', N'unknown', N'de/communardo/kenmei/database/update_runtime/v1.2/db.changelog.runtime.v1.2.xml', CAST(0x00009E8000A9C8D8 AS DateTime), N'f26f3ec897d29e80cb3aedbe98993545', N'Custom Change', N'add followable items to all existing notes', NULL, N'1.9.2')
INSERT [dbo].[DATABASECHANGELOG] ([ID], [AUTHOR], [FILENAME], [DATEEXECUTED], [MD5SUM], [DESCRIPTION], [COMMENTS], [TAG], [LIQUIBASE]) VALUES (N'KENMEI-1264_Offline-Autosave', N'Communote', N'de/communardo/kenmei/database/update/v3.x/db.changelog.v3.1.xml', CAST(0x0000A62801202386 AS DateTime), N'beb0cac28fc0a9df3b983d6cefefd9f0', N'Add Column', N'', NULL, N'1.9.5')
INSERT [dbo].[DATABASECHANGELOG] ([ID], [AUTHOR], [FILENAME], [DATEEXECUTED], [MD5SUM], [DESCRIPTION], [COMMENTS], [TAG], [LIQUIBASE]) VALUES (N'KENMEI-1264_Offline-Autosave_AddUploader_mssql', N'Communote', N'de/communardo/kenmei/database/update/v3.x/db.changelog.v3.1.xml', CAST(0x0000A6280120238A AS DateTime), N'3bb98c478b74d1a973aa1fc0d3cfd3', N'Custom SQL', N'', NULL, N'1.9.5')
INSERT [dbo].[DATABASECHANGELOG] ([ID], [AUTHOR], [FILENAME], [DATEEXECUTED], [MD5SUM], [DESCRIPTION], [COMMENTS], [TAG], [LIQUIBASE]) VALUES (N'KENMEI-1264_Offline-Autosave_Foreign_Keys', N'Communote', N'de/communardo/kenmei/database/update/v3.x/db.changelog.v3.1.xml', CAST(0x0000A62801202388 AS DateTime), N'f94dc4491622b20d6bd97b3e358698a', N'Add Foreign Key Constraint', N'', NULL, N'1.9.5')
INSERT [dbo].[DATABASECHANGELOG] ([ID], [AUTHOR], [FILENAME], [DATEEXECUTED], [MD5SUM], [DESCRIPTION], [COMMENTS], [TAG], [LIQUIBASE]) VALUES (N'KENMEI-1264_Offline-Autosave_TaskHandler_3', N'Communote', N'de/communardo/kenmei/database/update_2nd_pass/v3.x/db.changelog.v3.1.xml', CAST(0x0000A628012028AA AS DateTime), N'f343c72a10c5f04551c4344a8490a2e5', N'Custom Change', N'Add DeleteOrphanedAttachmentsTaskHandler to tasks', NULL, N'1.9.5')
INSERT [dbo].[DATABASECHANGELOG] ([ID], [AUTHOR], [FILENAME], [DATEEXECUTED], [MD5SUM], [DESCRIPTION], [COMMENTS], [TAG], [LIQUIBASE]) VALUES (N'KENMEI-1521_Purge_Disconnected_External_Auths', N'unknown', N'de/communardo/kenmei/database/update/v1.1.3/db.changelog.v1.1.3.xml', CAST(0x00009E8D008B93A4 AS DateTime), N'2f2d42d3b88a45b1857590a0f80dc75', N'Custom SQL', N'', NULL, N'1.9.2')
INSERT [dbo].[DATABASECHANGELOG] ([ID], [AUTHOR], [FILENAME], [DATEEXECUTED], [MD5SUM], [DESCRIPTION], [COMMENTS], [TAG], [LIQUIBASE]) VALUES (N'KENMEI-1533_Erzeugung_Global_ID_fehlerhaft-MySQL', N'unknown', N'de/communardo/kenmei/database/update/v1.1.3/db.changelog.v1.1.3.xml', CAST(0x00009E8000A80228 AS DateTime), N'5a199146dfd883297b1bfdb6ef6f517', N'SQL From File', N'', NULL, N'1.9.2')
INSERT [dbo].[DATABASECHANGELOG] ([ID], [AUTHOR], [FILENAME], [DATEEXECUTED], [MD5SUM], [DESCRIPTION], [COMMENTS], [TAG], [LIQUIBASE]) VALUES (N'KENMEI-1566-Remove_MySQL_Timestamp_Trigger', N'unknown', N'de/communardo/kenmei/database/update/v1.1.3/db.changelog.v1.1.3.xml', CAST(0x00009E8000A80228 AS DateTime), N'a4fb25b5e12f2ab2519c961d47707617', N'Custom SQL', N'', NULL, N'1.9.2')
INSERT [dbo].[DATABASECHANGELOG] ([ID], [AUTHOR], [FILENAME], [DATEEXECUTED], [MD5SUM], [DESCRIPTION], [COMMENTS], [TAG], [LIQUIBASE]) VALUES (N'KENMEI-1578_Felder_key_und_value_in_Modellen_aendern', N'unknown', N'de/communardo/kenmei/database/update/v1.2/db.changelog.v1.2.xml', CAST(0x00009E8000A80CB4 AS DateTime), N'4ff8a66180d5067f9a8337daa846', N'Rename Column (x4)', N'', NULL, N'1.9.2')
INSERT [dbo].[DATABASECHANGELOG] ([ID], [AUTHOR], [FILENAME], [DATEEXECUTED], [MD5SUM], [DESCRIPTION], [COMMENTS], [TAG], [LIQUIBASE]) VALUES (N'KENMEI-1578_Felder_key_und_value_in_Modellen_aendern_v2', N'unknown', N'de/communardo/kenmei/database/update/v1.2/db.changelog.v1.2.xml', CAST(0x0000A6280120214B AS DateTime), N'93d4a658b399a9dbd9f5499b3629cf1', N'Rename Column (x3)', N'', NULL, N'1.9.5')
INSERT [dbo].[DATABASECHANGELOG] ([ID], [AUTHOR], [FILENAME], [DATEEXECUTED], [MD5SUM], [DESCRIPTION], [COMMENTS], [TAG], [LIQUIBASE]) VALUES (N'KENMEI-1617_NullPointerException_bei_Zugriff_auf_Certificates', N'unknown', N'de/communardo/kenmei/database/update_2nd_pass/v1.1.4/db.changelog.v1.1.4.xml', CAST(0x00009E8000A81E48 AS DateTime), N'9af4a69dbe173a28caa8ea67321a9612', N'Custom Change', N'Add password for keystore if it doesnt exist.', NULL, N'1.9.2')
INSERT [dbo].[DATABASECHANGELOG] ([ID], [AUTHOR], [FILENAME], [DATEEXECUTED], [MD5SUM], [DESCRIPTION], [COMMENTS], [TAG], [LIQUIBASE]) VALUES (N'KENMEI-1672_Virus_scanner_in_admin_section', N'unknown', N'de/communardo/kenmei/database/update_2nd_pass/v1.1.4/db.changelog.v1.1.4.xml', CAST(0x00009E8000A81E48 AS DateTime), N'e99e6c9bad1fb4f2a4b5f15e30e2d681', N'Custom Change', N'Add enabled option for virus scanner', NULL, N'1.9.2')
INSERT [dbo].[DATABASECHANGELOG] ([ID], [AUTHOR], [FILENAME], [DATEEXECUTED], [MD5SUM], [DESCRIPTION], [COMMENTS], [TAG], [LIQUIBASE]) VALUES (N'KENMEI-1694_Follow_model_mysql', N'unknown', N'de/communardo/kenmei/database/update/v1.2/db.changelog.v1.2.xml', CAST(0x00009E8000A80DE0 AS DateTime), N'cf2d46294da178b3cec5db11a59cee', N'Custom SQL, Add Primary Key, Add Foreign Key Constraint (x2), Add Primary Key, Add Foreign Key Constraint (x2)', N'', NULL, N'1.9.2')
INSERT [dbo].[DATABASECHANGELOG] ([ID], [AUTHOR], [FILENAME], [DATEEXECUTED], [MD5SUM], [DESCRIPTION], [COMMENTS], [TAG], [LIQUIBASE]) VALUES (N'KENMEI-1694_Follow-add_blog_globalIds', N'unknown', N'de/communardo/kenmei/database/update_2nd_pass/v1.2/db.changelog.v1.2.xml', CAST(0x00009E8000A81E48 AS DateTime), N'c79f0fa2747d5864dcf55aa98b5100', N'Custom Change', N'Create the global Id for blogs where there is none yet.', NULL, N'1.9.2')
INSERT [dbo].[DATABASECHANGELOG] ([ID], [AUTHOR], [FILENAME], [DATEEXECUTED], [MD5SUM], [DESCRIPTION], [COMMENTS], [TAG], [LIQUIBASE]) VALUES (N'KENMEI-1694_Follow-add_tag_globalIds', N'unknown', N'de/communardo/kenmei/database/update_2nd_pass/v1.2/db.changelog.v1.2.xml', CAST(0x00009E8000A81E48 AS DateTime), N'3ee19392e521fda2a8629f23c9baba98', N'Custom Change', N'Create the default blog for clients where there is none yet.', NULL, N'1.9.2')
INSERT [dbo].[DATABASECHANGELOG] ([ID], [AUTHOR], [FILENAME], [DATEEXECUTED], [MD5SUM], [DESCRIPTION], [COMMENTS], [TAG], [LIQUIBASE]) VALUES (N'KENMEI-1694_Follow-remove_tag_globalIds', N'unknown', N'de/communardo/kenmei/database/update_2nd_pass/v1.2/db.changelog.v1.2.xml', CAST(0x00009E8000A81E48 AS DateTime), N'5db46f07a5f2b465bc80d13d44398', N'Custom SQL', N'Remove global IDs from tags', NULL, N'1.9.2')
INSERT [dbo].[DATABASECHANGELOG] ([ID], [AUTHOR], [FILENAME], [DATEEXECUTED], [MD5SUM], [DESCRIPTION], [COMMENTS], [TAG], [LIQUIBASE]) VALUES (N'KENMEI-1694_Follow-support_for_tags-MySQL_v3', N'unknown', N'de/communardo/kenmei/database/update/v1.2/db.changelog.v1.2.xml', CAST(0x00009E8000A80DE0 AS DateTime), N'eb3cd3e484968abfae07914643ba681', N'Drop Unique Constraint', N'', NULL, N'1.9.2')
INSERT [dbo].[DATABASECHANGELOG] ([ID], [AUTHOR], [FILENAME], [DATEEXECUTED], [MD5SUM], [DESCRIPTION], [COMMENTS], [TAG], [LIQUIBASE]) VALUES (N'KENMEI-1694_Follow-support_for_tags-MySQL_v4', N'unknown', N'de/communardo/kenmei/database/update/v1.2/db.changelog.v1.2.xml', CAST(0x00009E8000A80DE0 AS DateTime), N'794bcbcb61d474af2dd2a1b946bde68', N'Drop Foreign Key Constraint', N'', NULL, N'1.9.2')
INSERT [dbo].[DATABASECHANGELOG] ([ID], [AUTHOR], [FILENAME], [DATEEXECUTED], [MD5SUM], [DESCRIPTION], [COMMENTS], [TAG], [LIQUIBASE]) VALUES (N'KENMEI-1694_Follow-support_for_tags-MySQL_v5', N'unknown', N'de/communardo/kenmei/database/update/v1.2/db.changelog.v1.2.xml', CAST(0x00009E8000A80DE0 AS DateTime), N'6731ae54a2b9defa6af2780d3cff338', N'Drop Unique Constraint', N'', NULL, N'1.9.2')
INSERT [dbo].[DATABASECHANGELOG] ([ID], [AUTHOR], [FILENAME], [DATEEXECUTED], [MD5SUM], [DESCRIPTION], [COMMENTS], [TAG], [LIQUIBASE]) VALUES (N'KENMEI-1694_Follow-support_for_tags-MySQL_v6', N'unknown', N'de/communardo/kenmei/database/update/v1.2/db.changelog.v1.2.xml', CAST(0x00009E8000A80DE0 AS DateTime), N'd697217de5b4c55543b0446c5514dccb', N'Drop Foreign Key Constraint', N'', NULL, N'1.9.2')
INSERT [dbo].[DATABASECHANGELOG] ([ID], [AUTHOR], [FILENAME], [DATEEXECUTED], [MD5SUM], [DESCRIPTION], [COMMENTS], [TAG], [LIQUIBASE]) VALUES (N'KENMEI-1857_Option_for_system_notes_per_blog', N'unknown', N'de/communardo/kenmei/database/update/v1.1.4/db.changelog.v1.1.4.xml', CAST(0x00009E8000A80B88 AS DateTime), N'3710cfcc8722190afbabf4cdfbd1c50', N'Add Column, Add Not-Null Constraint', N'KENMEI-1857 allow disabling of system notes per blog', NULL, N'1.9.2')
INSERT [dbo].[DATABASECHANGELOG] ([ID], [AUTHOR], [FILENAME], [DATEEXECUTED], [MD5SUM], [DESCRIPTION], [COMMENTS], [TAG], [LIQUIBASE]) VALUES (N'KENMEI-1901_Drop_news_tables', N'unknown', N'de/communardo/kenmei/database/update/v1.2/db.changelog.v1.2.xml', CAST(0x00009E8000A80DE0 AS DateTime), N'e328a03e4b3621b07cd78ac5881b94dc', N'Drop Table (x3)', N'', NULL, N'1.9.2')
INSERT [dbo].[DATABASECHANGELOG] ([ID], [AUTHOR], [FILENAME], [DATEEXECUTED], [MD5SUM], [DESCRIPTION], [COMMENTS], [TAG], [LIQUIBASE]) VALUES (N'KENMEI-1979_Attachments_for_outgoing_mails_mysql', N'unknown', N'de/communardo/kenmei/database/update/v1.2/db.changelog.v1.2.xml', CAST(0x00009E8000A80DE0 AS DateTime), N'fcf55ea391ee0a078551c12c38c761', N'Custom SQL', N'', NULL, N'1.9.2')
INSERT [dbo].[DATABASECHANGELOG] ([ID], [AUTHOR], [FILENAME], [DATEEXECUTED], [MD5SUM], [DESCRIPTION], [COMMENTS], [TAG], [LIQUIBASE]) VALUES (N'kenmei-1988-ramconnector-entfernen', N'unknown', N'de/communardo/kenmei/database/update/v1.3.3/db.changelog.v1.3.3.xml', CAST(0x00009E8A00AB32F4 AS DateTime), N'9569702cd7d0b74787b6ab783602989', N'Drop Table', N'', NULL, N'1.9.2')
INSERT [dbo].[DATABASECHANGELOG] ([ID], [AUTHOR], [FILENAME], [DATEEXECUTED], [MD5SUM], [DESCRIPTION], [COMMENTS], [TAG], [LIQUIBASE]) VALUES (N'KENMEI-2008_Ldap_External_Group_Additional_Props', N'unknown', N'de/communardo/kenmei/database/update/v1.2/db.changelog.v1.2.xml', CAST(0x00009E8000A81038 AS DateTime), N'40578a21037635e7cd0434a3beaeac4', N'Add Column', N'', NULL, N'1.9.2')
INSERT [dbo].[DATABASECHANGELOG] ([ID], [AUTHOR], [FILENAME], [DATEEXECUTED], [MD5SUM], [DESCRIPTION], [COMMENTS], [TAG], [LIQUIBASE]) VALUES (N'KENMEI-2009_Finalize_changed_LDAP_Config', N'unknown', N'de/communardo/kenmei/database/update/v1.2/db.changelog.v1.2.xml', CAST(0x00009E8000A80F0C AS DateTime), N'45746a6edbf040d1eec5c559be13d4', N'Drop Column (x4), Add Not-Null Constraint', N'', NULL, N'1.9.2')
INSERT [dbo].[DATABASECHANGELOG] ([ID], [AUTHOR], [FILENAME], [DATEEXECUTED], [MD5SUM], [DESCRIPTION], [COMMENTS], [TAG], [LIQUIBASE]) VALUES (N'KENMEI-2009_Ldap_Config_SearchBase_list', N'unknown', N'de/communardo/kenmei/database/update/v1.2/db.changelog.v1.2.xml', CAST(0x00009E8000A81038 AS DateTime), N'3c6bcc15811c64f0494474d31c35104c', N'Add Column', N'', NULL, N'1.9.2')
INSERT [dbo].[DATABASECHANGELOG] ([ID], [AUTHOR], [FILENAME], [DATEEXECUTED], [MD5SUM], [DESCRIPTION], [COMMENTS], [TAG], [LIQUIBASE]) VALUES (N'KENMEI-2009_Ldap_Config_User_UID', N'unknown', N'de/communardo/kenmei/database/update/v1.2/db.changelog.v1.2.xml', CAST(0x00009E8000A80F0C AS DateTime), N'382cc4905529ba9151032204e58dcee', N'Add Column, Add Not-Null Constraint', N'', NULL, N'1.9.2')
INSERT [dbo].[DATABASECHANGELOG] ([ID], [AUTHOR], [FILENAME], [DATEEXECUTED], [MD5SUM], [DESCRIPTION], [COMMENTS], [TAG], [LIQUIBASE]) VALUES (N'KENMEI-2009_Ldap_config-add_uid_to_mapping', N'unknown', N'de/communardo/kenmei/database/update_2nd_pass/v1.2/db.changelog.v1.2.xml', CAST(0x00009E8000A81E48 AS DateTime), N'4c44b01203e35fc2473fdafac8c5cc', N'Custom Change', N'Add UID to userSearch property mapping', NULL, N'1.9.2')
INSERT [dbo].[DATABASECHANGELOG] ([ID], [AUTHOR], [FILENAME], [DATEEXECUTED], [MD5SUM], [DESCRIPTION], [COMMENTS], [TAG], [LIQUIBASE]) VALUES (N'KENMEI-2009_Ldap_External_User_Auth_Additional_Props', N'unknown', N'de/communardo/kenmei/database/update/v1.2/db.changelog.v1.2.xml', CAST(0x00009E8000A81038 AS DateTime), N'c92fd0704f4ac6551090f6d4ae43ada', N'Add Column, Custom SQL', N'', NULL, N'1.9.2')
INSERT [dbo].[DATABASECHANGELOG] ([ID], [AUTHOR], [FILENAME], [DATEEXECUTED], [MD5SUM], [DESCRIPTION], [COMMENTS], [TAG], [LIQUIBASE]) VALUES (N'KENMEI-2009_Migrate_LDAP_Config-MySQL', N'unknown', N'de/communardo/kenmei/database/update/v1.2/db.changelog.v1.2.xml', CAST(0x00009E8000A80F0C AS DateTime), N'96744914835013bcccd272c663ba2b8', N'Custom SQL', N'', NULL, N'1.9.2')
INSERT [dbo].[DATABASECHANGELOG] ([ID], [AUTHOR], [FILENAME], [DATEEXECUTED], [MD5SUM], [DESCRIPTION], [COMMENTS], [TAG], [LIQUIBASE]) VALUES (N'KENMEI-2009_Refactor_Group_Sync', N'unknown', N'de/communardo/kenmei/database/update/v1.2/db.changelog.v1.2.xml', CAST(0x00009E8000A80F0C AS DateTime), N'379ed5349921bcef239216c3d0b22920', N'Drop Column (x2)', N'', NULL, N'1.9.2')
INSERT [dbo].[DATABASECHANGELOG] ([ID], [AUTHOR], [FILENAME], [DATEEXECUTED], [MD5SUM], [DESCRIPTION], [COMMENTS], [TAG], [LIQUIBASE]) VALUES (N'KENMEI-2009_Tables_for_changed_LDAP_Config_mysql', N'unknown', N'de/communardo/kenmei/database/update/v1.2/db.changelog.v1.2.xml', CAST(0x00009E8000A80F0C AS DateTime), N'26c4a0b1e75df3286ed279cfc262998', N'Custom SQL, Add Foreign Key Constraint (x2), Add Column', N'', NULL, N'1.9.2')
INSERT [dbo].[DATABASECHANGELOG] ([ID], [AUTHOR], [FILENAME], [DATEEXECUTED], [MD5SUM], [DESCRIPTION], [COMMENTS], [TAG], [LIQUIBASE]) VALUES (N'KENMEI-2104_Ldap_Passwort_verschluesseln', N'unknown', N'de/communardo/kenmei/database/update_2nd_pass/v1.2/db.changelog.v1.2.xml', CAST(0x00009E8000A81E48 AS DateTime), N'f472fa9537c62bf44ffbd73b1ffb6ad', N'Custom Change', N'encrypts the LDAP manager password', NULL, N'1.9.2')
INSERT [dbo].[DATABASECHANGELOG] ([ID], [AUTHOR], [FILENAME], [DATEEXECUTED], [MD5SUM], [DESCRIPTION], [COMMENTS], [TAG], [LIQUIBASE]) VALUES (N'KENMEI-2113_Performanzoptimierung_Direct_Message_migrate', N'unknown', N'de/communardo/kenmei/database/update/v1.2/db.changelog.v1.2.xml', CAST(0x00009E8000A81164 AS DateTime), N'b35b34b640585fac11e4a5df10dfa', N'Custom SQL', N'', NULL, N'1.9.2')
INSERT [dbo].[DATABASECHANGELOG] ([ID], [AUTHOR], [FILENAME], [DATEEXECUTED], [MD5SUM], [DESCRIPTION], [COMMENTS], [TAG], [LIQUIBASE]) VALUES (N'KENMEI-2113_Performanzoptimierung_Direct_Message_mysql', N'unknown', N'de/communardo/kenmei/database/update/v1.2/db.changelog.v1.2.xml', CAST(0x00009E8000A81164 AS DateTime), N'd521a632fab7d957ad853854ea48694', N'Custom SQL, Add Primary Key, Add Foreign Key Constraint (x2)', N'', NULL, N'1.9.2')
INSERT [dbo].[DATABASECHANGELOG] ([ID], [AUTHOR], [FILENAME], [DATEEXECUTED], [MD5SUM], [DESCRIPTION], [COMMENTS], [TAG], [LIQUIBASE]) VALUES (N'KENMEI-2154_Jobs_Services_als_Tasks_in_Datenbank_mysql', N'unknown', N'de/communardo/kenmei/database/update/v1.3/db.changelog.v1.3.xml', CAST(0x00009E8000A81290 AS DateTime), N'd1db1d661338d345aab613ed1f94ec9a', N'Custom SQL (x3), Add Foreign Key Constraint (x2)', N'', NULL, N'1.9.2')
INSERT [dbo].[DATABASECHANGELOG] ([ID], [AUTHOR], [FILENAME], [DATEEXECUTED], [MD5SUM], [DESCRIPTION], [COMMENTS], [TAG], [LIQUIBASE]) VALUES (N'KENMEI-2154_Jobs_Services_als_Tasks_in_Datenbank-Rename-Columns', N'unknown', N'de/communardo/kenmei/database/update/v1.3/db.changelog.v1.3.xml', CAST(0x00009E8000A814E8 AS DateTime), N'26b5c92cd2dc3debe146334576775857', N'Rename Column (x2)', N'', NULL, N'1.9.2')
INSERT [dbo].[DATABASECHANGELOG] ([ID], [AUTHOR], [FILENAME], [DATEEXECUTED], [MD5SUM], [DESCRIPTION], [COMMENTS], [TAG], [LIQUIBASE]) VALUES (N'KENMEI-2154-Jobs_Services_als_Tasks_in_Datenbank_v2', N'unknown', N'de/communardo/kenmei/database/update_2nd_pass/v1.3/db.changelog.v1.3.xml', CAST(0x0000A62801202894 AS DateTime), N'627db31b9a285c1a388d525db74626', N'Custom Change', N'Updating existing jobs.', NULL, N'1.9.5')
INSERT [dbo].[DATABASECHANGELOG] ([ID], [AUTHOR], [FILENAME], [DATEEXECUTED], [MD5SUM], [DESCRIPTION], [COMMENTS], [TAG], [LIQUIBASE]) VALUES (N'KENMEI-2240_discussionId_befuellen-MySQL', N'unknown', N'de/communardo/kenmei/database/update/v1.3/db.changelog.v1.3.xml', CAST(0x00009E8000A81740 AS DateTime), N'89ee14e79e5c6e8054bff3e64e3d5c49', N'Custom SQL', N'', NULL, N'1.9.2')
INSERT [dbo].[DATABASECHANGELOG] ([ID], [AUTHOR], [FILENAME], [DATEEXECUTED], [MD5SUM], [DESCRIPTION], [COMMENTS], [TAG], [LIQUIBASE]) VALUES (N'KENMEI-2240_Spalte_discussionId_in_core_note', N'unknown', N'de/communardo/kenmei/database/update/v1.3/db.changelog.v1.3.xml', CAST(0x00009E8000A81740 AS DateTime), N'bedafeee1e858694c28879e61a2c5b26', N'Add Column', N'', NULL, N'1.9.2')
INSERT [dbo].[DATABASECHANGELOG] ([ID], [AUTHOR], [FILENAME], [DATEEXECUTED], [MD5SUM], [DESCRIPTION], [COMMENTS], [TAG], [LIQUIBASE]) VALUES (N'KENMEI-2261-LDAP_Authentication_SASL_Support', N'unknown', N'de/communardo/kenmei/database/update/v1.3/db.changelog.v1.3.xml', CAST(0x00009E8000A8186C AS DateTime), N'66db577a2f9c6ee89bdfa2c4848b117f', N'Add Column', N'', NULL, N'1.9.2')
INSERT [dbo].[DATABASECHANGELOG] ([ID], [AUTHOR], [FILENAME], [DATEEXECUTED], [MD5SUM], [DESCRIPTION], [COMMENTS], [TAG], [LIQUIBASE]) VALUES (N'KENMEI-2287_StartTLS_1', N'unknown', N'de/communardo/kenmei/database/update/v1.2/db.changelog.v1.2.xml', CAST(0x00009E8000A81164 AS DateTime), N'c629b7e26c3494b0c7ebf583ecce7fdd', N'Insert Row', N'', NULL, N'1.9.2')
INSERT [dbo].[DATABASECHANGELOG] ([ID], [AUTHOR], [FILENAME], [DATEEXECUTED], [MD5SUM], [DESCRIPTION], [COMMENTS], [TAG], [LIQUIBASE]) VALUES (N'KENMEI-2288_remove_old_caching_properties', N'unknown', N'de/communardo/kenmei/database/update/v1.3/db.changelog.v1.3.xml', CAST(0x00009E8000A8186C AS DateTime), N'93ac2c8652ac9ce97b5682a4a85cc0', N'Custom SQL', N'', NULL, N'1.9.2')
INSERT [dbo].[DATABASECHANGELOG] ([ID], [AUTHOR], [FILENAME], [DATEEXECUTED], [MD5SUM], [DESCRIPTION], [COMMENTS], [TAG], [LIQUIBASE]) VALUES (N'KENMEI-2295_Create_personal_blog_mysql', N'unknown', N'de/communardo/kenmei/database/update/v1.2/db.changelog.v1.2.xml', CAST(0x00009E8000A81290 AS DateTime), N'd60537d23ca7d5d226eb2eda3a2e18', N'Custom SQL', N'', NULL, N'1.9.2')
INSERT [dbo].[DATABASECHANGELOG] ([ID], [AUTHOR], [FILENAME], [DATEEXECUTED], [MD5SUM], [DESCRIPTION], [COMMENTS], [TAG], [LIQUIBASE]) VALUES (N'KENMEI-2302_Index_on_core_role2blog', N'unknown', N'de/communardo/kenmei/database/update/v1.2/db.changelog.v1.2.xml', CAST(0x00009E8000A81164 AS DateTime), N'83576031a02b3bc36cbc918a0c59d79', N'Create Index (x2)', N'', NULL, N'1.9.2')
INSERT [dbo].[DATABASECHANGELOG] ([ID], [AUTHOR], [FILENAME], [DATEEXECUTED], [MD5SUM], [DESCRIPTION], [COMMENTS], [TAG], [LIQUIBASE]) VALUES (N'KENMEI-2315_mediumblob_for_global_binary_property_2', N'unknown', N'de/communardo/kenmei/database/update/v1.3/db.changelog.v1.3.xml', CAST(0x00009E8000A81998 AS DateTime), N'8677bcb553814bf24c8fb3f41bb82ef7', N'Custom SQL', N'', NULL, N'1.9.2')
INSERT [dbo].[DATABASECHANGELOG] ([ID], [AUTHOR], [FILENAME], [DATEEXECUTED], [MD5SUM], [DESCRIPTION], [COMMENTS], [TAG], [LIQUIBASE]) VALUES (N'KENMEI-2335_UniqueJabberResourceIdPerCluster', N'unknown', N'de/communardo/kenmei/database/update/v1.2/db.changelog.v1.2.xml', CAST(0x00009E8000A81164 AS DateTime), N'c298eab7d736523bb2c86346674dabc', N'Custom SQL', N'', NULL, N'1.9.2')
INSERT [dbo].[DATABASECHANGELOG] ([ID], [AUTHOR], [FILENAME], [DATEEXECUTED], [MD5SUM], [DESCRIPTION], [COMMENTS], [TAG], [LIQUIBASE]) VALUES (N'KENMEI-2389_StartTLS_1', N'unknown', N'de/communardo/kenmei/database/update/v1.2/db.changelog.v1.2.xml', CAST(0x00009E8000A81164 AS DateTime), N'c8fe21bfa1ed5b39dc80e84e83a8aa67', N'Insert Row', N'', NULL, N'1.9.2')
INSERT [dbo].[DATABASECHANGELOG] ([ID], [AUTHOR], [FILENAME], [DATEEXECUTED], [MD5SUM], [DESCRIPTION], [COMMENTS], [TAG], [LIQUIBASE]) VALUES (N'KENMEI-2389_StartTLS_2', N'unknown', N'de/communardo/kenmei/database/update/v1.2/db.changelog.v1.2.xml', CAST(0x00009E8000A81164 AS DateTime), N'd5f814f6a5ec5bf9eb501bdbc41c6d41', N'Custom SQL', N'', NULL, N'1.9.2')
INSERT [dbo].[DATABASECHANGELOG] ([ID], [AUTHOR], [FILENAME], [DATEEXECUTED], [MD5SUM], [DESCRIPTION], [COMMENTS], [TAG], [LIQUIBASE]) VALUES (N'KENMEI-2465_Index_for_better_performance_mysql', N'unknown', N'de/communardo/kenmei/database/update/v1.3.1/db.changelog.v1.3.1.xml', CAST(0x00009E8000A81AC4 AS DateTime), N'64af9b8975bff0a14f4c95fa6c94715', N'Custom SQL', N'', NULL, N'1.9.2')
INSERT [dbo].[DATABASECHANGELOG] ([ID], [AUTHOR], [FILENAME], [DATEEXECUTED], [MD5SUM], [DESCRIPTION], [COMMENTS], [TAG], [LIQUIBASE]) VALUES (N'KENMEI-2510_confluence_passwort_verschluesseln', N'unknown', N'de/communardo/kenmei/database/update_2nd_pass/v1.2/db.changelog.v1.2.xml', CAST(0x00009E8000A81E48 AS DateTime), N'adf43a30d6bf756fefc6cfb243f428', N'Custom Change', N'encrypts the Confluence administrator password', NULL, N'1.9.2')
INSERT [dbo].[DATABASECHANGELOG] ([ID], [AUTHOR], [FILENAME], [DATEEXECUTED], [MD5SUM], [DESCRIPTION], [COMMENTS], [TAG], [LIQUIBASE]) VALUES (N'KENMEI-2510_sharepoint_passwort_verschluesseln', N'unknown', N'de/communardo/kenmei/database/update_2nd_pass/v1.2/db.changelog.v1.2.xml', CAST(0x00009E8000A81E48 AS DateTime), N'80ddc2197d1d616a48d012e6fb802452', N'Custom Change', N'encrypts the Sharepoint administrator password', NULL, N'1.9.2')
INSERT [dbo].[DATABASECHANGELOG] ([ID], [AUTHOR], [FILENAME], [DATEEXECUTED], [MD5SUM], [DESCRIPTION], [COMMENTS], [TAG], [LIQUIBASE]) VALUES (N'KENMEI-2601_Index-fuer-discussion_id-anlegen', N'UNKNOWN', N'de/communardo/kenmei/database/update/v1.3.2/db.changelog.v1.3.2.xml', CAST(0x00009E8000A81BF0 AS DateTime), N'a6483cd076bdafe25975ac34fcfc3384', N'Create Index', N'', NULL, N'1.9.2')
INSERT [dbo].[DATABASECHANGELOG] ([ID], [AUTHOR], [FILENAME], [DATEEXECUTED], [MD5SUM], [DESCRIPTION], [COMMENTS], [TAG], [LIQUIBASE]) VALUES (N'KENMEI-2601_Index-fuer-discussion_id-entfernen', N'UNKNOWN', N'de/communardo/kenmei/database/update/v1.3.2/db.changelog.v1.3.2.xml', CAST(0x00009E8000A81BF0 AS DateTime), N'4ae911eeae58366bebf8ae724282571a', N'Drop Index', N'', NULL, N'1.9.2')
INSERT [dbo].[DATABASECHANGELOG] ([ID], [AUTHOR], [FILENAME], [DATEEXECUTED], [MD5SUM], [DESCRIPTION], [COMMENTS], [TAG], [LIQUIBASE]) VALUES (N'KENMEI-2608-Refactor MostUsedBlogs', N'unknown', N'de/communardo/kenmei/database/update/v1.3.3/db.changelog.v1.3.3.xml', CAST(0x00009E8000A81D1C AS DateTime), N'f64386ffc88d3fef2b45eb268f6769b5', N'Drop Table', N'', NULL, N'1.9.2')
INSERT [dbo].[DATABASECHANGELOG] ([ID], [AUTHOR], [FILENAME], [DATEEXECUTED], [MD5SUM], [DESCRIPTION], [COMMENTS], [TAG], [LIQUIBASE]) VALUES (N'KENMEI-2616_Index_auf_Tag_lower_name_anlegen_mysql', N'UNKNOWN', N'de/communardo/kenmei/database/update/v1.3.2/db.changelog.v1.3.2.xml', CAST(0x00009E8400A51B30 AS DateTime), N'8911ae8ae2fdc6b26465e7fc86c3e756', N'Custom SQL', N'', NULL, N'1.9.2')
INSERT [dbo].[DATABASECHANGELOG] ([ID], [AUTHOR], [FILENAME], [DATEEXECUTED], [MD5SUM], [DESCRIPTION], [COMMENTS], [TAG], [LIQUIBASE]) VALUES (N'KENMEI-2628_Remove_Wrong_Unique_Constraint', N'unknown', N'de/communardo/kenmei/database/update/v1.2/db.changelog.v1.2.xml', CAST(0x00009E8D008B95FC AS DateTime), N'b02c2845ed23257c053a0c26cd8dcb4', N'Custom SQL, Add Unique Constraint', N'', NULL, N'1.9.2')
INSERT [dbo].[DATABASECHANGELOG] ([ID], [AUTHOR], [FILENAME], [DATEEXECUTED], [MD5SUM], [DESCRIPTION], [COMMENTS], [TAG], [LIQUIBASE]) VALUES (N'KENMEI-2629-Reset-Primary-Auth-on-deactivated-external-auth', N'unknown', N'de/communardo/kenmei/database/update/v1.3.3/db.changelog.v1.3.3.xml', CAST(0x00009EE100A163A0 AS DateTime), N'd9932ed049603f4d816789d0454a285a', N'Update Data (x2)', N'', NULL, N'1.9.2')
INSERT [dbo].[DATABASECHANGELOG] ([ID], [AUTHOR], [FILENAME], [DATEEXECUTED], [MD5SUM], [DESCRIPTION], [COMMENTS], [TAG], [LIQUIBASE]) VALUES (N'KENMEI-2629-Set-Internal-DB-Auth', N'unknown', N'de/communardo/kenmei/database/update/v1.3.3/db.changelog.v1.3.3.xml', CAST(0x00009EE201129CC8 AS DateTime), N'15316e7b824f8887b286053f38ac54d', N'Custom SQL', N'', NULL, N'1.9.2')
INSERT [dbo].[DATABASECHANGELOG] ([ID], [AUTHOR], [FILENAME], [DATEEXECUTED], [MD5SUM], [DESCRIPTION], [COMMENTS], [TAG], [LIQUIBASE]) VALUES (N'KENMEI-3005_change_property_value_types_mssql', N'UNKNOWN', N'de/communardo/kenmei/database/update/v2.1/db.changelog.v2.1.1.xml', CAST(0x0000A628012022B6 AS DateTime), N'9eda6920f3e772ceef288094312629e5', N'Custom SQL', N'', NULL, N'1.9.5')
INSERT [dbo].[DATABASECHANGELOG] ([ID], [AUTHOR], [FILENAME], [DATEEXECUTED], [MD5SUM], [DESCRIPTION], [COMMENTS], [TAG], [LIQUIBASE]) VALUES (N'KENMEI-3005_drop_not_null_constraints_mssql', N'UNKNOWN', N'de/communardo/kenmei/database/update/v2.1/db.changelog.v2.1.1.xml', CAST(0x0000A628012022C1 AS DateTime), N'c189d7b4beffad6163fa74a5ce562e6', N'Custom SQL', N'', NULL, N'1.9.5')
INSERT [dbo].[DATABASECHANGELOG] ([ID], [AUTHOR], [FILENAME], [DATEEXECUTED], [MD5SUM], [DESCRIPTION], [COMMENTS], [TAG], [LIQUIBASE]) VALUES (N'KENMEI-3005_fix_column_name', N'UNKNOWN', N'de/communardo/kenmei/database/update/v2.1/db.changelog.v2.1.1.xml', CAST(0x0000A628012021AE AS DateTime), N'141350baf72f232673fde54fa9a863aa', N'Rename Column', N'', NULL, N'1.9.5')
INSERT [dbo].[DATABASECHANGELOG] ([ID], [AUTHOR], [FILENAME], [DATEEXECUTED], [MD5SUM], [DESCRIPTION], [COMMENTS], [TAG], [LIQUIBASE]) VALUES (N'KENMEI-3005_fix_column_name_mssql', N'UNKNOWN', N'de/communardo/kenmei/database/update/v2.1/db.changelog.v2.1.1.xml', CAST(0x0000A628012022A1 AS DateTime), N'105a33b82385d8c2a3d8969d2714f5', N'Rename Column', N'', NULL, N'1.9.5')
INSERT [dbo].[DATABASECHANGELOG] ([ID], [AUTHOR], [FILENAME], [DATEEXECUTED], [MD5SUM], [DESCRIPTION], [COMMENTS], [TAG], [LIQUIBASE]) VALUES (N'KENMEI-3005_fix_large_table_name', N'Commuote', N'de/communardo/kenmei/database/update/v2.1/db.changelog.v2.1.1.xml', CAST(0x0000A628012022A9 AS DateTime), N'c8ae46c35b97d7c6ba91bd77458367', N'Rename Table', N'', NULL, N'1.9.5')
INSERT [dbo].[DATABASECHANGELOG] ([ID], [AUTHOR], [FILENAME], [DATEEXECUTED], [MD5SUM], [DESCRIPTION], [COMMENTS], [TAG], [LIQUIBASE]) VALUES (N'KENMEI-3005_rename_tables_new', N'UNKNOWN', N'de/communardo/kenmei/database/update/v2.1/db.changelog.v2.1.1.xml', CAST(0x0000A628012022BC AS DateTime), N'e6b3d3595e3c557cc17e79983a14515', N'Rename Table (x3)', N'', NULL, N'1.9.5')
INSERT [dbo].[DATABASECHANGELOG] ([ID], [AUTHOR], [FILENAME], [DATEEXECUTED], [MD5SUM], [DESCRIPTION], [COMMENTS], [TAG], [LIQUIBASE]) VALUES (N'KENMEI-3005_rename_tables_v2', N'UNKNOWN', N'de/communardo/kenmei/database/update/v2.1/db.changelog.v2.1.1.xml', CAST(0x0000A628012022D1 AS DateTime), N'95325bf04ee3b01dc3f9fdbf25f28f3a', N'Rename Table', N'', NULL, N'1.9.5')
INSERT [dbo].[DATABASECHANGELOG] ([ID], [AUTHOR], [FILENAME], [DATEEXECUTED], [MD5SUM], [DESCRIPTION], [COMMENTS], [TAG], [LIQUIBASE]) VALUES (N'KENMEI-3005_set_null_mssql', N'UNKNOWN', N'de/communardo/kenmei/database/update/v2.1/db.changelog.v2.1.1.xml', CAST(0x0000A628012022CE AS DateTime), N'5cbef42d04faf83a6f231522ed1e37', N'Custom SQL', N'', NULL, N'1.9.5')
INSERT [dbo].[DATABASECHANGELOG] ([ID], [AUTHOR], [FILENAME], [DATEEXECUTED], [MD5SUM], [DESCRIPTION], [COMMENTS], [TAG], [LIQUIBASE]) VALUES (N'KENMEI-3719_2', N'unknown', N'de/communardo/kenmei/database/update/v1.3.3/db.changelog.v1.3.3.xml', CAST(0x0000A62801202158 AS DateTime), N'f23f1e2a63e54359e668ff1c60de1e', N'Add Unique Constraint', N'', NULL, N'1.9.5')
INSERT [dbo].[DATABASECHANGELOG] ([ID], [AUTHOR], [FILENAME], [DATEEXECUTED], [MD5SUM], [DESCRIPTION], [COMMENTS], [TAG], [LIQUIBASE]) VALUES (N'KENMEI-3838_Domain_Controller_LDAP_Server_automatisch_ermitteln', N'Communote', N'de/communardo/kenmei/database/update/v2.2/db.changelog.v2.2.xml', CAST(0x0000A62801202318 AS DateTime), N'3e644577adfac5759ae154e4497737d', N'Add Column', N'', NULL, N'1.9.5')
INSERT [dbo].[DATABASECHANGELOG] ([ID], [AUTHOR], [FILENAME], [DATEEXECUTED], [MD5SUM], [DESCRIPTION], [COMMENTS], [TAG], [LIQUIBASE]) VALUES (N'KENMEI-3838_Domain_Controller_LDAP_Server_ermitteln_mssql', N'Communote', N'de/communardo/kenmei/database/update/v2.2/db.changelog.v2.2.xml', CAST(0x0000A6280120231B AS DateTime), N'990944238d074e075492d454754483', N'Drop Column (x2), Add Column', N'', NULL, N'1.9.5')
INSERT [dbo].[DATABASECHANGELOG] ([ID], [AUTHOR], [FILENAME], [DATEEXECUTED], [MD5SUM], [DESCRIPTION], [COMMENTS], [TAG], [LIQUIBASE]) VALUES (N'KENMEI-3838_Set_Dynamic_Mode_to_false', N'Communote', N'de/communardo/kenmei/database/update/v2.2/db.changelog.v2.2.xml', CAST(0x0000A6280120231D AS DateTime), N'ea36cdad1cb713fb847563ab187b665d', N'Update Data', N'', NULL, N'1.9.5')
INSERT [dbo].[DATABASECHANGELOG] ([ID], [AUTHOR], [FILENAME], [DATEEXECUTED], [MD5SUM], [DESCRIPTION], [COMMENTS], [TAG], [LIQUIBASE]) VALUES (N'KENMEI-3997_Ausschalten_des_Features_Selbstregistrierung', N'Commuote', N'de/communardo/kenmei/database/update/v2.1/db.changelog.v2.1.1.xml', CAST(0x0000A6280120219C AS DateTime), N'35adb7e64b6c407b9877a3215362c3ef', N'Add Column, Add Foreign Key Constraint', N'', NULL, N'1.9.5')
INSERT [dbo].[DATABASECHANGELOG] ([ID], [AUTHOR], [FILENAME], [DATEEXECUTED], [MD5SUM], [DESCRIPTION], [COMMENTS], [TAG], [LIQUIBASE]) VALUES (N'KENMEI-3997_Selbstregistrierung_2', N'Commuote', N'de/communardo/kenmei/database/update/v2.1/db.changelog.v2.1.1.xml', CAST(0x0000A628012021AC AS DateTime), N'12e56d5965b4e7f44e9a7bc7892bbf46', N'Add Column', N'', NULL, N'1.9.5')
INSERT [dbo].[DATABASECHANGELOG] ([ID], [AUTHOR], [FILENAME], [DATEEXECUTED], [MD5SUM], [DESCRIPTION], [COMMENTS], [TAG], [LIQUIBASE]) VALUES (N'KENMEI-3997_Selbstregistrierung_mssql', N'Commuote', N'de/communardo/kenmei/database/update/v2.1/db.changelog.v2.1.1.xml', CAST(0x0000A6280120219E AS DateTime), N'a2922424654fe2fbc761ef9997a26f7c', N'Add Column, Add Foreign Key Constraint', N'', NULL, N'1.9.5');
print 'Processed 100 total records'
INSERT [dbo].[DATABASECHANGELOG] ([ID], [AUTHOR], [FILENAME], [DATEEXECUTED], [MD5SUM], [DESCRIPTION], [COMMENTS], [TAG], [LIQUIBASE]) VALUES (N'KENMEI-3997_Selbstregistrierung_remove2', N'Commuote', N'de/communardo/kenmei/database/update/v2.1/db.changelog.v2.1.1.xml', CAST(0x0000A628012021AA AS DateTime), N'eabe999544ee2285adc23e6c59c247', N'Drop Column', N'', NULL, N'1.9.5')
INSERT [dbo].[DATABASECHANGELOG] ([ID], [AUTHOR], [FILENAME], [DATEEXECUTED], [MD5SUM], [DESCRIPTION], [COMMENTS], [TAG], [LIQUIBASE]) VALUES (N'KENMEI-3997_Selbstregistrierung_remove2_mssql', N'Commuote', N'de/communardo/kenmei/database/update/v2.1/db.changelog.v2.1.1.xml', CAST(0x0000A628012021A6 AS DateTime), N'7550398b33bed1ff48c411722c566ebf', N'Drop Foreign Key Constraint', N'', NULL, N'1.9.5')
INSERT [dbo].[DATABASECHANGELOG] ([ID], [AUTHOR], [FILENAME], [DATEEXECUTED], [MD5SUM], [DESCRIPTION], [COMMENTS], [TAG], [LIQUIBASE]) VALUES (N'KENMEI-4017-Add_Tag_Store_information', N'UNKNOWN', N'de/communardo/kenmei/database/update/v2.1/db.changelog.v2.1.1.xml', CAST(0x0000A62801202184 AS DateTime), N'ed6fd58dac11fe96a55b96cb2a12b83e', N'Modify Column, Rename Column, Add Column', N'', NULL, N'1.9.5')
INSERT [dbo].[DATABASECHANGELOG] ([ID], [AUTHOR], [FILENAME], [DATEEXECUTED], [MD5SUM], [DESCRIPTION], [COMMENTS], [TAG], [LIQUIBASE]) VALUES (N'KENMEI-4017-Add_Tag_Store_information_2_add_index', N'UNKNOWN', N'de/communardo/kenmei/database/update/v2.1/db.changelog.v2.1.1.xml', CAST(0x0000A6280120218C AS DateTime), N'7a1ce9db257c89aa7aacd4125416230', N'Add Unique Constraint', N'', NULL, N'1.9.5')
INSERT [dbo].[DATABASECHANGELOG] ([ID], [AUTHOR], [FILENAME], [DATEEXECUTED], [MD5SUM], [DESCRIPTION], [COMMENTS], [TAG], [LIQUIBASE]) VALUES (N'KENMEI-4017-Add_Tag_Store_information_2_not_mysql', N'UNKNOWN', N'de/communardo/kenmei/database/update/v2.1/db.changelog.v2.1.1.xml', CAST(0x0000A62801202189 AS DateTime), N'f13bc14d5a9d781ca41e2c3d0456e2', N'Drop Unique Constraint, Rename Column', N'', NULL, N'1.9.5')
INSERT [dbo].[DATABASECHANGELOG] ([ID], [AUTHOR], [FILENAME], [DATEEXECUTED], [MD5SUM], [DESCRIPTION], [COMMENTS], [TAG], [LIQUIBASE]) VALUES (N'KENMEI-4017-Add_Tag_Store_information_mssql_1', N'UNKNOWN', N'de/communardo/kenmei/database/update/v2.1/db.changelog.v2.1.1.xml', CAST(0x0000A6280120216C AS DateTime), N'6dcd582cbfc8d6f5ba6ae84e68c6749a', N'Drop Index', N'', NULL, N'1.9.5')
INSERT [dbo].[DATABASECHANGELOG] ([ID], [AUTHOR], [FILENAME], [DATEEXECUTED], [MD5SUM], [DESCRIPTION], [COMMENTS], [TAG], [LIQUIBASE]) VALUES (N'KENMEI-4017-Add_Tag_Store_information_mssql_2', N'UNKNOWN', N'de/communardo/kenmei/database/update/v2.1/db.changelog.v2.1.1.xml', CAST(0x0000A62801202186 AS DateTime), N'fe77e34cb34459c82ae20b0ac29c95f', N'Create Index', N'', NULL, N'1.9.5')
INSERT [dbo].[DATABASECHANGELOG] ([ID], [AUTHOR], [FILENAME], [DATEEXECUTED], [MD5SUM], [DESCRIPTION], [COMMENTS], [TAG], [LIQUIBASE]) VALUES (N'KENMEI-4017-Create_entity_to_tag_association', N'UNKNOWN', N'de/communardo/kenmei/database/update/v2.1/db.changelog.v2.1.1.xml', CAST(0x0000A6280120215D AS DateTime), N'93ecae16b48c48e51cbc4b317dffa7cf', N'Create Table', N'', NULL, N'1.9.5')
INSERT [dbo].[DATABASECHANGELOG] ([ID], [AUTHOR], [FILENAME], [DATEEXECUTED], [MD5SUM], [DESCRIPTION], [COMMENTS], [TAG], [LIQUIBASE]) VALUES (N'KENMEI-4017-Create_entity_to_tag_association_Constraints', N'UNKNOWN', N'de/communardo/kenmei/database/update/v2.1/db.changelog.v2.1.1.xml', CAST(0x0000A62801202160 AS DateTime), N'f5b4027ca2a47f2ad953c6b7b8aa0b3', N'Create Index, Add Foreign Key Constraint (x2)', N'', NULL, N'1.9.5')
INSERT [dbo].[DATABASECHANGELOG] ([ID], [AUTHOR], [FILENAME], [DATEEXECUTED], [MD5SUM], [DESCRIPTION], [COMMENTS], [TAG], [LIQUIBASE]) VALUES (N'KENMEI-4017-Create_tag2descriptions', N'UNKNOWN', N'de/communardo/kenmei/database/update/v2.1/db.changelog.v2.1.1.xml', CAST(0x0000A62801202168 AS DateTime), N'd555bf5d16beaec8a1224b7efece5e5', N'Create Table', N'', NULL, N'1.9.5')
INSERT [dbo].[DATABASECHANGELOG] ([ID], [AUTHOR], [FILENAME], [DATEEXECUTED], [MD5SUM], [DESCRIPTION], [COMMENTS], [TAG], [LIQUIBASE]) VALUES (N'KENMEI-4017-Create_tag2descriptions_Constraints', N'UNKNOWN', N'de/communardo/kenmei/database/update/v2.1/db.changelog.v2.1.1.xml', CAST(0x0000A6280120216A AS DateTime), N'c02defbee8416feb3bce5f405fdfe9', N'Add Foreign Key Constraint (x2), Add Unique Constraint', N'', NULL, N'1.9.5')
INSERT [dbo].[DATABASECHANGELOG] ([ID], [AUTHOR], [FILENAME], [DATEEXECUTED], [MD5SUM], [DESCRIPTION], [COMMENTS], [TAG], [LIQUIBASE]) VALUES (N'KENMEI-4017-Create_tag2names', N'UNKNOWN', N'de/communardo/kenmei/database/update/v2.1/db.changelog.v2.1.1.xml', CAST(0x0000A62801202162 AS DateTime), N'dde5e06d91cd71ddb96a3dbd5dfcbaaa', N'Create Table', N'', NULL, N'1.9.5')
INSERT [dbo].[DATABASECHANGELOG] ([ID], [AUTHOR], [FILENAME], [DATEEXECUTED], [MD5SUM], [DESCRIPTION], [COMMENTS], [TAG], [LIQUIBASE]) VALUES (N'KENMEI-4017-Create_tag2names_Constraints', N'UNKNOWN', N'de/communardo/kenmei/database/update/v2.1/db.changelog.v2.1.1.xml', CAST(0x0000A62801202165 AS DateTime), N'80ff97a3048ada5f861cce2e96c321a', N'Add Foreign Key Constraint (x2), Add Unique Constraint', N'', NULL, N'1.9.5')
INSERT [dbo].[DATABASECHANGELOG] ([ID], [AUTHOR], [FILENAME], [DATEEXECUTED], [MD5SUM], [DESCRIPTION], [COMMENTS], [TAG], [LIQUIBASE]) VALUES (N'KENMEI-4153_activate_default_blog_mssql', N'UNKNOWN', N'de/communardo/kenmei/database/update/v2.1/db.changelog.v2.1.1.xml', CAST(0x0000A62801202195 AS DateTime), N'9879efed9f9359f546cf5e23ec5cb599', N'Custom SQL', N'', NULL, N'1.9.5')
INSERT [dbo].[DATABASECHANGELOG] ([ID], [AUTHOR], [FILENAME], [DATEEXECUTED], [MD5SUM], [DESCRIPTION], [COMMENTS], [TAG], [LIQUIBASE]) VALUES (N'KENMEI-4153_mssql', N'UNKNOWN', N'de/communardo/kenmei/database/update/v2.1/db.changelog.v2.1.1.xml', CAST(0x0000A62801202191 AS DateTime), N'3695f380ffa94e5251ff5e953a81edf4', N'Custom SQL', N'', NULL, N'1.9.5')
INSERT [dbo].[DATABASECHANGELOG] ([ID], [AUTHOR], [FILENAME], [DATEEXECUTED], [MD5SUM], [DESCRIPTION], [COMMENTS], [TAG], [LIQUIBASE]) VALUES (N'KENMEI-4164-Dem Default Blog einen Blognamen zuweisen_all', N'Communote', N'de/communardo/kenmei/database/update/v2.1/db.changelog.v2.1.1.xml', CAST(0x0000A628012022C5 AS DateTime), N'b13eb5fd90ccf643748dbafdbffc9ac', N'Custom SQL', N'', NULL, N'1.9.5')
INSERT [dbo].[DATABASECHANGELOG] ([ID], [AUTHOR], [FILENAME], [DATEEXECUTED], [MD5SUM], [DESCRIPTION], [COMMENTS], [TAG], [LIQUIBASE]) VALUES (N'KENMEI-4164-Dem Default Blog einen Blognamen zuweisen_mssql', N'Communote', N'de/communardo/kenmei/database/update/v2.1/db.changelog.v2.1.1.xml', CAST(0x0000A628012022C3 AS DateTime), N'3e149df8b022fd3d1ae3cca6c38ffbd3', N'Custom SQL', N'', NULL, N'1.9.5')
INSERT [dbo].[DATABASECHANGELOG] ([ID], [AUTHOR], [FILENAME], [DATEEXECUTED], [MD5SUM], [DESCRIPTION], [COMMENTS], [TAG], [LIQUIBASE]) VALUES (N'KENMEI-4216_alter_anonymized_user_prefix_mssql_v2', N'Communote', N'de/communardo/kenmei/database/update/v2.1/db.changelog.v2.1.1.xml', CAST(0x0000A628012022F2 AS DateTime), N'73e09d7f2b701030e737e82aa44d53c', N'Custom SQL', N'', NULL, N'1.9.5')
INSERT [dbo].[DATABASECHANGELOG] ([ID], [AUTHOR], [FILENAME], [DATEEXECUTED], [MD5SUM], [DESCRIPTION], [COMMENTS], [TAG], [LIQUIBASE]) VALUES (N'KENMEI-4222_deadlock_delete_notes_index_global_id_mssql', N'Communote', N'de/communardo/kenmei/database/update/v2.1/db.changelog.v2.1.1.xml', CAST(0x0000A628012022EF AS DateTime), N'ca53deac6d29891b18d07bb4ea8f58d', N'Custom SQL', N'', NULL, N'1.9.5')
INSERT [dbo].[DATABASECHANGELOG] ([ID], [AUTHOR], [FILENAME], [DATEEXECUTED], [MD5SUM], [DESCRIPTION], [COMMENTS], [TAG], [LIQUIBASE]) VALUES (N'KENMEI-4222_deadlock_delete_notes_index_mssql', N'Communote', N'de/communardo/kenmei/database/update/v2.1/db.changelog.v2.1.1.xml', CAST(0x0000A628012022EC AS DateTime), N'28d7bbac268b54157e255d517b1f693c', N'Custom SQL', N'', NULL, N'1.9.5')
INSERT [dbo].[DATABASECHANGELOG] ([ID], [AUTHOR], [FILENAME], [DATEEXECUTED], [MD5SUM], [DESCRIPTION], [COMMENTS], [TAG], [LIQUIBASE]) VALUES (N'KENMEI-4236_deadlocks_taskmanagement_mssql', N'Communote', N'de/communardo/kenmei/database/update/v2.1/db.changelog.v2.1.1.xml', CAST(0x0000A628012022D7 AS DateTime), N'91b9c4d0125cfd7bea963fafa68452a', N'Custom SQL', N'', NULL, N'1.9.5')
INSERT [dbo].[DATABASECHANGELOG] ([ID], [AUTHOR], [FILENAME], [DATEEXECUTED], [MD5SUM], [DESCRIPTION], [COMMENTS], [TAG], [LIQUIBASE]) VALUES (N'KENMEI-4260_Exception_beim_Erstellen_einer_Note', N'Communote', N'de/communardo/kenmei/database/update/v2.1/db.changelog.v2.1.1.xml', CAST(0x0000A6280120219A AS DateTime), N'40d3f0382f6637ce6fb3749b20da', N'Custom SQL', N'', NULL, N'1.9.5')
INSERT [dbo].[DATABASECHANGELOG] ([ID], [AUTHOR], [FILENAME], [DATEEXECUTED], [MD5SUM], [DESCRIPTION], [COMMENTS], [TAG], [LIQUIBASE]) VALUES (N'KENMEI-4357-TagCloud-bei-Nutzerübersicht-geht-nicht_mssql1_2', N'Communote', N'de/communardo/kenmei/database/update/v2.1/db.changelog.v2.1.1.xml', CAST(0x0000A628012022AE AS DateTime), N'6ff5f852774da12e83622baee49c4c', N'Drop Unique Constraint, Drop Index, Custom SQL, Add Not-Null Constraint (x2)', N'', NULL, N'1.9.5')
INSERT [dbo].[DATABASECHANGELOG] ([ID], [AUTHOR], [FILENAME], [DATEEXECUTED], [MD5SUM], [DESCRIPTION], [COMMENTS], [TAG], [LIQUIBASE]) VALUES (N'KENMEI-4357-TagCloud-bei-Nutzerübersicht-geht-nicht_mssql2', N'UNKNOWN', N'de/communardo/kenmei/database/update/v2.1/db.changelog.v2.1.1.xml', CAST(0x0000A628012022B1 AS DateTime), N'f7d5875657933184b89b595ade47', N'Add Unique Constraint, Create Index', N'', NULL, N'1.9.5')
INSERT [dbo].[DATABASECHANGELOG] ([ID], [AUTHOR], [FILENAME], [DATEEXECUTED], [MD5SUM], [DESCRIPTION], [COMMENTS], [TAG], [LIQUIBASE]) VALUES (N'KENMEI-4504-SQL-Fulltextfunction-mssql-catalog', N'Unknown', N'de/communardo/kenmei/database/update/v2.1/db.changelog.fulltext.v2.1.1.xml', CAST(0x0000A628012022F4 AS DateTime), N'c1358598c85cdfd1e34a1781a51ad7df', N'Custom SQL', N'', NULL, N'1.9.5')
INSERT [dbo].[DATABASECHANGELOG] ([ID], [AUTHOR], [FILENAME], [DATEEXECUTED], [MD5SUM], [DESCRIPTION], [COMMENTS], [TAG], [LIQUIBASE]) VALUES (N'KENMEI-4504-SQL-Fulltextfunction-mssql-content', N'Unknown', N'de/communardo/kenmei/database/update/v2.1/db.changelog.fulltext.v2.1.1.xml', CAST(0x0000A628012022F8 AS DateTime), N'b8d26f9a4d72cdff690657bfcbaace2', N'Custom SQL', N'', NULL, N'1.9.5')
INSERT [dbo].[DATABASECHANGELOG] ([ID], [AUTHOR], [FILENAME], [DATEEXECUTED], [MD5SUM], [DESCRIPTION], [COMMENTS], [TAG], [LIQUIBASE]) VALUES (N'KENMEI-4504-SQL-Fulltextfunction-mssql-Custom-Message-drop', N'Communote', N'de/communardo/kenmei/database/update/v2.1/db.changelog.fulltext.v2.1.1.xml', CAST(0x0000A62801202305 AS DateTime), N'87ad593d829ab25ecb8142353b0fbd2', N'Custom SQL', N'', NULL, N'1.9.5')
INSERT [dbo].[DATABASECHANGELOG] ([ID], [AUTHOR], [FILENAME], [DATEEXECUTED], [MD5SUM], [DESCRIPTION], [COMMENTS], [TAG], [LIQUIBASE]) VALUES (N'KENMEI-4504-SQL-Fulltextfunction-mssql-tag-name-drop', N'Unknown', N'de/communardo/kenmei/database/update/v2.1/db.changelog.fulltext.v2.1.1.xml', CAST(0x0000A628012022FD AS DateTime), N'1845ddf7fbe5aaabcf2e0be3ed7fd4', N'Custom SQL', N'', NULL, N'1.9.5')
INSERT [dbo].[DATABASECHANGELOG] ([ID], [AUTHOR], [FILENAME], [DATEEXECUTED], [MD5SUM], [DESCRIPTION], [COMMENTS], [TAG], [LIQUIBASE]) VALUES (N'KENMEI-4504-SQL-Fulltextfunction-mssql-topic-drop', N'Unknown', N'de/communardo/kenmei/database/update/v2.1/db.changelog.fulltext.v2.1.1.xml', CAST(0x0000A628012022FB AS DateTime), N'e0415ab44bc8defc5e26d1ff89a97b', N'Custom SQL', N'', NULL, N'1.9.5')
INSERT [dbo].[DATABASECHANGELOG] ([ID], [AUTHOR], [FILENAME], [DATEEXECUTED], [MD5SUM], [DESCRIPTION], [COMMENTS], [TAG], [LIQUIBASE]) VALUES (N'KENMEI-4504-SQL-Fulltextfunction-mssql-user-drop', N'Unknown', N'de/communardo/kenmei/database/update/v2.1/db.changelog.fulltext.v2.1.1.xml', CAST(0x0000A62801202300 AS DateTime), N'7399010127f36416a81069b25b6e7e', N'Custom SQL', N'', NULL, N'1.9.5')
INSERT [dbo].[DATABASECHANGELOG] ([ID], [AUTHOR], [FILENAME], [DATEEXECUTED], [MD5SUM], [DESCRIPTION], [COMMENTS], [TAG], [LIQUIBASE]) VALUES (N'KENMEI-4504-SQL-Fulltextfunction-mssql-User-Group-drop', N'Communote', N'de/communardo/kenmei/database/update/v2.1/db.changelog.fulltext.v2.1.1.xml', CAST(0x0000A62801202307 AS DateTime), N'bdcbe2c907f6fd4407acd88b706ea6', N'Custom SQL', N'', NULL, N'1.9.5')
INSERT [dbo].[DATABASECHANGELOG] ([ID], [AUTHOR], [FILENAME], [DATEEXECUTED], [MD5SUM], [DESCRIPTION], [COMMENTS], [TAG], [LIQUIBASE]) VALUES (N'KENMEI-4504-SQL-Fulltextfunction-mssql-user-profile-drop', N'Unknown', N'de/communardo/kenmei/database/update/v2.1/db.changelog.fulltext.v2.1.1.xml', CAST(0x0000A62801202302 AS DateTime), N'6b8ae9acbc9de519c88d18e468c81526', N'Custom SQL', N'', NULL, N'1.9.5')
INSERT [dbo].[DATABASECHANGELOG] ([ID], [AUTHOR], [FILENAME], [DATEEXECUTED], [MD5SUM], [DESCRIPTION], [COMMENTS], [TAG], [LIQUIBASE]) VALUES (N'KENMEI-4573_Change_Datetime_To_Datetime2_v2', N'Communote', N'de/communardo/kenmei/database/update/v2.1/db.changelog.v2.1.1.xml', CAST(0x0000A628012022E7 AS DateTime), N'11ec26322e274abb39072bddbd929e', N'Custom SQL', N'', NULL, N'1.9.5')
INSERT [dbo].[DATABASECHANGELOG] ([ID], [AUTHOR], [FILENAME], [DATEEXECUTED], [MD5SUM], [DESCRIPTION], [COMMENTS], [TAG], [LIQUIBASE]) VALUES (N'KENMEI-4582_optimize_create_tagged_note_mssql', N'Communote', N'de/communardo/kenmei/database/update/v2.1/db.changelog.v2.1.1.xml', CAST(0x0000A628012022D9 AS DateTime), N'85da97eb3a3ea8f3ed182c838dfa', N'Custom SQL', N'', NULL, N'1.9.5')
INSERT [dbo].[DATABASECHANGELOG] ([ID], [AUTHOR], [FILENAME], [DATEEXECUTED], [MD5SUM], [DESCRIPTION], [COMMENTS], [TAG], [LIQUIBASE]) VALUES (N'KENMEI-4583_optimize_note_tag_sort_mssql', N'Communote', N'de/communardo/kenmei/database/update/v2.1/db.changelog.v2.1.1.xml', CAST(0x0000A628012022E9 AS DateTime), N'70b926f3e41dee815cd6e8785f79d09b', N'Custom SQL', N'', NULL, N'1.9.5')
INSERT [dbo].[DATABASECHANGELOG] ([ID], [AUTHOR], [FILENAME], [DATEEXECUTED], [MD5SUM], [DESCRIPTION], [COMMENTS], [TAG], [LIQUIBASE]) VALUES (N'KENMEI-4774_Column_for_application_property', N'Communote', N'de/communardo/kenmei/database/update/v2.2/db.changelog.v2.2.xml', CAST(0x0000A6280120230C AS DateTime), N'a7db319957f9d9988b0156db5ebb937', N'Add Column', N'', NULL, N'1.9.5')
INSERT [dbo].[DATABASECHANGELOG] ([ID], [AUTHOR], [FILENAME], [DATEEXECUTED], [MD5SUM], [DESCRIPTION], [COMMENTS], [TAG], [LIQUIBASE]) VALUES (N'KENMEI-4774_Eigenschaften_fuer_Plugins', N'Communote', N'de/communardo/kenmei/database/update/v2.2/db.changelog.v2.2.xml', CAST(0x0000A62801202309 AS DateTime), N'60d6e794152c2bc11a3ce93698f988', N'Create Table', N'', NULL, N'1.9.5')
INSERT [dbo].[DATABASECHANGELOG] ([ID], [AUTHOR], [FILENAME], [DATEEXECUTED], [MD5SUM], [DESCRIPTION], [COMMENTS], [TAG], [LIQUIBASE]) VALUES (N'KENMEI-4774_Eigenschaften_fuer_Plugins_unique', N'Communote', N'de/communardo/kenmei/database/update/v2.2/db.changelog.v2.2.xml', CAST(0x0000A6280120230A AS DateTime), N'4d6666ef5d19164f3b101864397bccef', N'Add Unique Constraint', N'', NULL, N'1.9.5')
INSERT [dbo].[DATABASECHANGELOG] ([ID], [AUTHOR], [FILENAME], [DATEEXECUTED], [MD5SUM], [DESCRIPTION], [COMMENTS], [TAG], [LIQUIBASE]) VALUES (N'KENMEI-4781_MSSQL_Datatypes', N'Communote', N'de/communardo/kenmei/database/update/v2.2/db.changelog.v2.2.xml', CAST(0x0000A62801202315 AS DateTime), N'64d26f64c842651cc86ded80247490', N'Custom SQL', N'', NULL, N'1.9.5')
INSERT [dbo].[DATABASECHANGELOG] ([ID], [AUTHOR], [FILENAME], [DATEEXECUTED], [MD5SUM], [DESCRIPTION], [COMMENTS], [TAG], [LIQUIBASE]) VALUES (N'KENMEI-4781_Refactoring_von_ExternalObjectManagement_v2', N'Communote', N'de/communardo/kenmei/database/update/v2.2/db.changelog.v2.2.xml', CAST(0x0000A62801202312 AS DateTime), N'ff956eeef12bf22171df6ebd4168865', N'Modify Column (x2), Add Column (x2), Update Data (x2), Add Not-Null Constraint (x2)', N'', NULL, N'1.9.5')
INSERT [dbo].[DATABASECHANGELOG] ([ID], [AUTHOR], [FILENAME], [DATEEXECUTED], [MD5SUM], [DESCRIPTION], [COMMENTS], [TAG], [LIQUIBASE]) VALUES (N'KENMEI-4845-Typen_Spalten_in_MSSQL_nicht_korrekt-v3', N'Communote', N'de/communardo/kenmei/database/update/v2.2/db.changelog.v2.2.xml', CAST(0x0000A62801202335 AS DateTime), N'f3fdcade36e539a6b9bb3101559f139', N'Custom SQL', N'', NULL, N'1.9.5')
INSERT [dbo].[DATABASECHANGELOG] ([ID], [AUTHOR], [FILENAME], [DATEEXECUTED], [MD5SUM], [DESCRIPTION], [COMMENTS], [TAG], [LIQUIBASE]) VALUES (N'KENMEI-4931_Thema einsehbar_obwohl_keine_Berechtigung_mssql_2', N'Communote', N'de/communardo/kenmei/database/update/v2.2/db.changelog.v2.2.xml', CAST(0x0000A6280120233A AS DateTime), N'c621673dc95591b87cebef2149b120', N'SQL From File', N'', NULL, N'1.9.5')
INSERT [dbo].[DATABASECHANGELOG] ([ID], [AUTHOR], [FILENAME], [DATEEXECUTED], [MD5SUM], [DESCRIPTION], [COMMENTS], [TAG], [LIQUIBASE]) VALUES (N'KENMEI-4xxx_MQ', N'Communote', N'de/communardo/kenmei/database/update_2nd_pass/v2.2/db.changelog.v2.2.xml', CAST(0x0000A62801202899 AS DateTime), N'fed0868ba054bcdcb65459b0e92f5f2', N'Custom Change', N'Add password for keystore if it doesn''t exist.', NULL, N'1.9.5')
INSERT [dbo].[DATABASECHANGELOG] ([ID], [AUTHOR], [FILENAME], [DATEEXECUTED], [MD5SUM], [DESCRIPTION], [COMMENTS], [TAG], [LIQUIBASE]) VALUES (N'KENMEI-5019_UniqueConstraint_ExternalGroups_mssql', N'Communote', N'de/communardo/kenmei/database/update/v2.2/db.changelog.v2.2.xml', CAST(0x0000A62801202339 AS DateTime), N'8a3195aadc220483b21d64e1358eeb', N'Custom SQL', N'', NULL, N'1.9.5')
INSERT [dbo].[DATABASECHANGELOG] ([ID], [AUTHOR], [FILENAME], [DATEEXECUTED], [MD5SUM], [DESCRIPTION], [COMMENTS], [TAG], [LIQUIBASE]) VALUES (N'KENMEI-5034-Angemeldet_bleiben_LDAP', N'Communote', N'de/communardo/kenmei/database/update/v2.3/db.changelog.v2.3.2.xml', CAST(0x0000A62801202355 AS DateTime), N'62806cf1a8b837291c472ff30fd7798', N'Add Column', N'', NULL, N'1.9.5')
INSERT [dbo].[DATABASECHANGELOG] ([ID], [AUTHOR], [FILENAME], [DATEEXECUTED], [MD5SUM], [DESCRIPTION], [COMMENTS], [TAG], [LIQUIBASE]) VALUES (N'KENMEI-5083_MSSQL_schlechte_Performance_bei_TagCloud-Aufruf_1', N'Communote', N'de/communardo/kenmei/database/update/v2.2/db.changelog.v2.2.xml', CAST(0x0000A6280120233C AS DateTime), N'ee3a604f3e27fcf62ec7bb9dfc96a5d', N'Custom SQL', N'', NULL, N'1.9.5')
INSERT [dbo].[DATABASECHANGELOG] ([ID], [AUTHOR], [FILENAME], [DATEEXECUTED], [MD5SUM], [DESCRIPTION], [COMMENTS], [TAG], [LIQUIBASE]) VALUES (N'KENMEI-5395-LastDiscussionNoteCreationDate-einfuegen', N'Communote', N'de/communardo/kenmei/database/update/v2.3/db.changelog.v2.3.xml', CAST(0x0000A6280120233E AS DateTime), N'7db328c1f9ba177cab22cef6b335a3f8', N'Add Column', N'', NULL, N'1.9.5')
INSERT [dbo].[DATABASECHANGELOG] ([ID], [AUTHOR], [FILENAME], [DATEEXECUTED], [MD5SUM], [DESCRIPTION], [COMMENTS], [TAG], [LIQUIBASE]) VALUES (N'KENMEI-5416_Setzen_LastDiscussionNoteCreationDate_mssql_4', N'Communote', N'de/communardo/kenmei/database/update/v2.3/db.changelog.v2.3.xml', CAST(0x0000A62801202344 AS DateTime), N'e1235f166437b59e6c44eb7ed5bfb8dc', N'Custom SQL', N'', NULL, N'1.9.5')
INSERT [dbo].[DATABASECHANGELOG] ([ID], [AUTHOR], [FILENAME], [DATEEXECUTED], [MD5SUM], [DESCRIPTION], [COMMENTS], [TAG], [LIQUIBASE]) VALUES (N'KENMEI-5416_Setzen_LastDiscussionNoteCreationDate_upd_col', N'Communote', N'de/communardo/kenmei/database/update/v2.3/db.changelog.v2.3.xml', CAST(0x0000A62801202341 AS DateTime), N'945d8c565c755d489ffe70aced337c6d', N'Modify Column', N'', NULL, N'1.9.5')
INSERT [dbo].[DATABASECHANGELOG] ([ID], [AUTHOR], [FILENAME], [DATEEXECUTED], [MD5SUM], [DESCRIPTION], [COMMENTS], [TAG], [LIQUIBASE]) VALUES (N'KENMEI-5455_Alternative_Methode_fuer_Nachladender_Notes', N'Communote', N'de/communardo/kenmei/database/update/v2.3/db.changelog.v2.3.1.xml', CAST(0x0000A62801202346 AS DateTime), N'65d83c24ca415f4d6b533d05163c022', N'Custom SQL', N'', NULL, N'1.9.5')
INSERT [dbo].[DATABASECHANGELOG] ([ID], [AUTHOR], [FILENAME], [DATEEXECUTED], [MD5SUM], [DESCRIPTION], [COMMENTS], [TAG], [LIQUIBASE]) VALUES (N'KENMEI-5524_1_1', N'Communote', N'de/communardo/kenmei/database/update/v2.3/db.changelog.v2.3.1.xml', CAST(0x0000A62801202348 AS DateTime), N'78b6368bd5b2ed7ce6adc0992b709586', N'Create Table', N'', NULL, N'1.9.5')
INSERT [dbo].[DATABASECHANGELOG] ([ID], [AUTHOR], [FILENAME], [DATEEXECUTED], [MD5SUM], [DESCRIPTION], [COMMENTS], [TAG], [LIQUIBASE]) VALUES (N'KENMEI-5524_1_1-fix_column_name', N'Communote', N'de/communardo/kenmei/database/update/v2.3/db.changelog.v2.3.1.xml', CAST(0x0000A62801202353 AS DateTime), N'318a811a93e01147b1ea39be1f3f395', N'Custom SQL', N'', NULL, N'1.9.5')
INSERT [dbo].[DATABASECHANGELOG] ([ID], [AUTHOR], [FILENAME], [DATEEXECUTED], [MD5SUM], [DESCRIPTION], [COMMENTS], [TAG], [LIQUIBASE]) VALUES (N'KENMEI-5524_1_2', N'Communote', N'de/communardo/kenmei/database/update/v2.3/db.changelog.v2.3.1.xml', CAST(0x0000A6280120234B AS DateTime), N'b3f6fd8aa13f6553ae28b334d5a50f7', N'Add Foreign Key Constraint (x2)', N'', NULL, N'1.9.5')
INSERT [dbo].[DATABASECHANGELOG] ([ID], [AUTHOR], [FILENAME], [DATEEXECUTED], [MD5SUM], [DESCRIPTION], [COMMENTS], [TAG], [LIQUIBASE]) VALUES (N'KENMEI-5524_5', N'Communote', N'de/communardo/kenmei/database/update/v2.3/db.changelog.v2.3.1.xml', CAST(0x0000A6280120234D AS DateTime), N'd29af978c3eaf6beb1b5d925dab827', N'Add Unique Constraint', N'', NULL, N'1.9.5')
INSERT [dbo].[DATABASECHANGELOG] ([ID], [AUTHOR], [FILENAME], [DATEEXECUTED], [MD5SUM], [DESCRIPTION], [COMMENTS], [TAG], [LIQUIBASE]) VALUES (N'KENMEI-5524_6', N'Communote', N'de/communardo/kenmei/database/update/v2.3/db.changelog.v2.3.1.xml', CAST(0x0000A6280120234F AS DateTime), N'7cedf3fcefd676807524cce4973bf329', N'Add Column, Add Foreign Key Constraint', N'', NULL, N'1.9.5')
INSERT [dbo].[DATABASECHANGELOG] ([ID], [AUTHOR], [FILENAME], [DATEEXECUTED], [MD5SUM], [DESCRIPTION], [COMMENTS], [TAG], [LIQUIBASE]) VALUES (N'KENMEI-5555_Anonymisieren_Nutzer_mit_vielen_Notes_sehr_lange', N'Communote', N'de/communardo/kenmei/database/update/v2.3/db.changelog.v2.3.2.xml', CAST(0x0000A62801202358 AS DateTime), N'1230ad57dbf563db0a680e9f779d8', N'Create Index (x2)', N'', NULL, N'1.9.5')
INSERT [dbo].[DATABASECHANGELOG] ([ID], [AUTHOR], [FILENAME], [DATEEXECUTED], [MD5SUM], [DESCRIPTION], [COMMENTS], [TAG], [LIQUIBASE]) VALUES (N'KENMEI-5562_Index_last_discussion_creation_date', N'Communote', N'de/communardo/kenmei/database/update/v2.3/db.changelog.v2.3.1.xml', CAST(0x0000A62801202351 AS DateTime), N'b0299e1c45133f98f5bb4f6235a78a', N'Custom SQL', N'', NULL, N'1.9.5')
INSERT [dbo].[DATABASECHANGELOG] ([ID], [AUTHOR], [FILENAME], [DATEEXECUTED], [MD5SUM], [DESCRIPTION], [COMMENTS], [TAG], [LIQUIBASE]) VALUES (N'KENMEI-5742_Packagestruktur_aufraeumen_Tasks_v2', N'Communote', N'de/communardo/kenmei/database/update/v2.4/db.changelog.v2.4.xml', CAST(0x0000A6280120235E AS DateTime), N'16bfbfd1c0f47030b4da5777c3f672', N'Custom SQL', N'', NULL, N'1.9.5')
INSERT [dbo].[DATABASECHANGELOG] ([ID], [AUTHOR], [FILENAME], [DATEEXECUTED], [MD5SUM], [DESCRIPTION], [COMMENTS], [TAG], [LIQUIBASE]) VALUES (N'KENMEI-5828-Localized-Email-imprint_terms', N'Communote', N'de/communardo/kenmei/database/update/v2.4/db.changelog.v2.4.xml', CAST(0x0000A62801202364 AS DateTime), N'a787203f141bd68c1913ddaffeb1b6b', N'Custom SQL', N'', NULL, N'1.9.5')
INSERT [dbo].[DATABASECHANGELOG] ([ID], [AUTHOR], [FILENAME], [DATEEXECUTED], [MD5SUM], [DESCRIPTION], [COMMENTS], [TAG], [LIQUIBASE]) VALUES (N'KENMEI-5828-Localized-Email-Signature_delete_old', N'Communote', N'de/communardo/kenmei/database/update/v2.4/db.changelog.v2.4.xml', CAST(0x0000A62801202362 AS DateTime), N'8bcc4071f291a6ca8f748e932debc26', N'Custom SQL', N'', NULL, N'1.9.5')
INSERT [dbo].[DATABASECHANGELOG] ([ID], [AUTHOR], [FILENAME], [DATEEXECUTED], [MD5SUM], [DESCRIPTION], [COMMENTS], [TAG], [LIQUIBASE]) VALUES (N'KENMEI-5828-Localized-Email-Signature_mssql_mysql', N'Communote', N'de/communardo/kenmei/database/update/v2.4/db.changelog.v2.4.xml', CAST(0x0000A62801202360 AS DateTime), N'b7c7d43fe8b1ab22907d5b8ece8499bd', N'Custom SQL', N'', NULL, N'1.9.5')
INSERT [dbo].[DATABASECHANGELOG] ([ID], [AUTHOR], [FILENAME], [DATEEXECUTED], [MD5SUM], [DESCRIPTION], [COMMENTS], [TAG], [LIQUIBASE]) VALUES (N'KENMEI-5859_Neuer_Inhaltstyp_Anhaenge_mssql_mysql', N'Communote', N'de/communardo/kenmei/database/update/v2.4/db.changelog.v2.4.xml', CAST(0x0000A62801202366 AS DateTime), N'c57d8f192b1c8e2fe2dfd0dc9930619', N'Custom SQL', N'', NULL, N'1.9.5')
INSERT [dbo].[DATABASECHANGELOG] ([ID], [AUTHOR], [FILENAME], [DATEEXECUTED], [MD5SUM], [DESCRIPTION], [COMMENTS], [TAG], [LIQUIBASE]) VALUES (N'KENMEI-5923_Benachrichtung_Themen_und_Diskussionen', N'Communote', N'de/communardo/kenmei/database/update/v2.4/db.changelog.v2.4.xml', CAST(0x0000A62801202369 AS DateTime), N'4189499ff788adfaac769c0d75e5f', N'Add Column', N'', NULL, N'1.9.5')
INSERT [dbo].[DATABASECHANGELOG] ([ID], [AUTHOR], [FILENAME], [DATEEXECUTED], [MD5SUM], [DESCRIPTION], [COMMENTS], [TAG], [LIQUIBASE]) VALUES (N'KENMEI-5923_Benachrichtung_Themen_und_Diskussionen_default_v2', N'Communote', N'de/communardo/kenmei/database/update/v2.4/db.changelog.v2.4.xml', CAST(0x0000A62801202370 AS DateTime), N'ae34de50903a953ce6a580302c2dac', N'Update Data', N'', NULL, N'1.9.5')
INSERT [dbo].[DATABASECHANGELOG] ([ID], [AUTHOR], [FILENAME], [DATEEXECUTED], [MD5SUM], [DESCRIPTION], [COMMENTS], [TAG], [LIQUIBASE]) VALUES (N'KENMEI-6098-Bild-Filter-zeigt-Nachrichten-ohne-Bild', N'Communote', N'de/communardo/kenmei/database/update/v2.4/db.changelog.v2.4.xml', CAST(0x0000A6280120236E AS DateTime), N'7ecafa23555e8df5323e84979bbb184', N'Custom SQL', N'', NULL, N'1.9.5')
INSERT [dbo].[DATABASECHANGELOG] ([ID], [AUTHOR], [FILENAME], [DATEEXECUTED], [MD5SUM], [DESCRIPTION], [COMMENTS], [TAG], [LIQUIBASE]) VALUES (N'KENMEI-6122-Add_missing_index', N'Communote', N'de/communardo/kenmei/database/update/v2.4/db.changelog.v2.4.xml', CAST(0x0000A62801202371 AS DateTime), N'4f7bc98c7c74ba4ab5cae9873ab19', N'Create Index', N'', NULL, N'1.9.5')
INSERT [dbo].[DATABASECHANGELOG] ([ID], [AUTHOR], [FILENAME], [DATEEXECUTED], [MD5SUM], [DESCRIPTION], [COMMENTS], [TAG], [LIQUIBASE]) VALUES (N'KENMEI-6192_Themenhierarchien_Umsetzung_blog2blog', N'Communote', N'de/communardo/kenmei/database/update/v3.x/db.changelog.v3.0.xml', CAST(0x0000A62801202373 AS DateTime), N'ffe2ff18c648d3e68139433d1fa8da8f', N'Create Table', N'', NULL, N'1.9.5')
INSERT [dbo].[DATABASECHANGELOG] ([ID], [AUTHOR], [FILENAME], [DATEEXECUTED], [MD5SUM], [DESCRIPTION], [COMMENTS], [TAG], [LIQUIBASE]) VALUES (N'KENMEI-6192_Themenhierarchien_Umsetzung_blog2blog_resolved', N'Communote', N'de/communardo/kenmei/database/update/v3.x/db.changelog.v3.0.xml', CAST(0x0000A62801202374 AS DateTime), N'79cb9a52016696d76b8219e79d1874f', N'Create Table', N'', NULL, N'1.9.5')
INSERT [dbo].[DATABASECHANGELOG] ([ID], [AUTHOR], [FILENAME], [DATEEXECUTED], [MD5SUM], [DESCRIPTION], [COMMENTS], [TAG], [LIQUIBASE]) VALUES (N'KENMEI-6192_Themenhierarchien_Umsetzung_constraint_2', N'Communote', N'de/communardo/kenmei/database/update/v3.x/db.changelog.v3.0.xml', CAST(0x0000A62801202378 AS DateTime), N'42f59fa11a3443dc208225977a1ed8', N'Add Unique Constraint', N'', NULL, N'1.9.5')
INSERT [dbo].[DATABASECHANGELOG] ([ID], [AUTHOR], [FILENAME], [DATEEXECUTED], [MD5SUM], [DESCRIPTION], [COMMENTS], [TAG], [LIQUIBASE]) VALUES (N'KENMEI-6192_Themenhierarchien_Umsetzung_foreign_keys', N'Communote', N'de/communardo/kenmei/database/update/v3.x/db.changelog.v3.0.xml', CAST(0x0000A62801202376 AS DateTime), N'52e253dadf622f36c1a81be4dc46fe', N'Add Foreign Key Constraint (x2)', N'', NULL, N'1.9.5')
INSERT [dbo].[DATABASECHANGELOG] ([ID], [AUTHOR], [FILENAME], [DATEEXECUTED], [MD5SUM], [DESCRIPTION], [COMMENTS], [TAG], [LIQUIBASE]) VALUES (N'KENMEI-6192_Themenhierarchien_Umsetzung_toplevel_flag', N'Communote', N'de/communardo/kenmei/database/update/v3.x/db.changelog.v3.0.xml', CAST(0x0000A6280120237A AS DateTime), N'acaeb509982a5828c99841b1ccaa546', N'Add Column', N'', NULL, N'1.9.5')
INSERT [dbo].[DATABASECHANGELOG] ([ID], [AUTHOR], [FILENAME], [DATEEXECUTED], [MD5SUM], [DESCRIPTION], [COMMENTS], [TAG], [LIQUIBASE]) VALUES (N'KENMEI-6256-NavigationItems', N'Communote', N'de/communardo/kenmei/database/update/v3.x/db.changelog.v3.0.xml', CAST(0x0000A6280120237B AS DateTime), N'f36ce97067d97f5cf0892041b9bf15f7', N'Create Table', N'', NULL, N'1.9.5')
INSERT [dbo].[DATABASECHANGELOG] ([ID], [AUTHOR], [FILENAME], [DATEEXECUTED], [MD5SUM], [DESCRIPTION], [COMMENTS], [TAG], [LIQUIBASE]) VALUES (N'KENMEI-6256-NavigationItems_add_name', N'Communote', N'de/communardo/kenmei/database/update/v3.x/db.changelog.v3.0.xml', CAST(0x0000A62801202380 AS DateTime), N'd884929523cd4d419f67af45537892a', N'Add Column', N'', NULL, N'1.9.5')
INSERT [dbo].[DATABASECHANGELOG] ([ID], [AUTHOR], [FILENAME], [DATEEXECUTED], [MD5SUM], [DESCRIPTION], [COMMENTS], [TAG], [LIQUIBASE]) VALUES (N'KENMEI-6256-NavigationItems_foreign_keys', N'Communote', N'de/communardo/kenmei/database/update/v3.x/db.changelog.v3.0.xml', CAST(0x0000A6280120237D AS DateTime), N'7fa1bdfeec2113f5f29f448b918f20b6', N'Add Foreign Key Constraint', N'', NULL, N'1.9.5')
INSERT [dbo].[DATABASECHANGELOG] ([ID], [AUTHOR], [FILENAME], [DATEEXECUTED], [MD5SUM], [DESCRIPTION], [COMMENTS], [TAG], [LIQUIBASE]) VALUES (N'KENMEI-6256-NavigationItems_rename_index', N'Communote', N'de/communardo/kenmei/database/update/v3.x/db.changelog.v3.0.xml', CAST(0x0000A6280120237F AS DateTime), N'33d323521e9aa43155c9504ccd4134', N'Rename Column', N'', NULL, N'1.9.5')
INSERT [dbo].[DATABASECHANGELOG] ([ID], [AUTHOR], [FILENAME], [DATEEXECUTED], [MD5SUM], [DESCRIPTION], [COMMENTS], [TAG], [LIQUIBASE]) VALUES (N'KENMEI-6270-ClientAktivierung-Make-The-Git-Hash-Work', N'Communote', N'de/communardo/kenmei/database/update/v3.x/db.changelog.v3.0.xml', CAST(0x0000A62801202384 AS DateTime), N'1e5cd6666e7728841c3453ebd29043b9', N'Modify Column', N'', NULL, N'1.9.5')
INSERT [dbo].[DATABASECHANGELOG] ([ID], [AUTHOR], [FILENAME], [DATEEXECUTED], [MD5SUM], [DESCRIPTION], [COMMENTS], [TAG], [LIQUIBASE]) VALUES (N'KENMEI-6543-Create_BuiltIn_NaviItems', N'Communote', N'de/communardo/kenmei/database/update_2nd_pass/v3.x/db.changelog.v3.0.xml', CAST(0x0000A628012028A3 AS DateTime), N'ff6b36f04575f9aa72335b2310e36087', N'Custom Change', N'Add built-in navigation items', NULL, N'1.9.5')
INSERT [dbo].[DATABASECHANGELOG] ([ID], [AUTHOR], [FILENAME], [DATEEXECUTED], [MD5SUM], [DESCRIPTION], [COMMENTS], [TAG], [LIQUIBASE]) VALUES (N'KENMEI-7154-Terms-Of_Use_1-mssql', N'Communote', N'de/communardo/kenmei/database/update/v3.x/db.changelog.v3.2.xml', CAST(0x0000A6280120238F AS DateTime), N'81c7bfc2e874aaf8b6e54bdda15548', N'Custom SQL', N'', NULL, N'1.9.5')
INSERT [dbo].[DATABASECHANGELOG] ([ID], [AUTHOR], [FILENAME], [DATEEXECUTED], [MD5SUM], [DESCRIPTION], [COMMENTS], [TAG], [LIQUIBASE]) VALUES (N'KENMEI-7154-Terms-Of_Use_2-mssql', N'Communote', N'de/communardo/kenmei/database/update/v3.x/db.changelog.v3.2.xml', CAST(0x0000A62801202392 AS DateTime), N'40b1a9a75921616d54a0efdab723d8f2', N'Custom SQL', N'', NULL, N'1.9.5')
INSERT [dbo].[DATABASECHANGELOG] ([ID], [AUTHOR], [FILENAME], [DATEEXECUTED], [MD5SUM], [DESCRIPTION], [COMMENTS], [TAG], [LIQUIBASE]) VALUES (N'KENMEI-7199-Drop-SharePoint-Configuration', N'Communote', N'de/communardo/kenmei/database/update/v3.x/db.changelog.v3.2.xml', CAST(0x0000A6280120238C AS DateTime), N'dc9630efe277bfd22645bcac93646ec', N'Drop Table, Custom SQL', N'', NULL, N'1.9.5')
INSERT [dbo].[DATABASECHANGELOG] ([ID], [AUTHOR], [FILENAME], [DATEEXECUTED], [MD5SUM], [DESCRIPTION], [COMMENTS], [TAG], [LIQUIBASE]) VALUES (N'KENMEI-7369-Drop-CRC-Configuration', N'Communote', N'de/communardo/kenmei/database/update/v3.x/db.changelog.v3.3.xml', CAST(0x0000A62801202395 AS DateTime), N'be4e654ee8abde97480e229775678a7', N'Drop Table (x2)', N'', NULL, N'1.9.5')
INSERT [dbo].[DATABASECHANGELOG] ([ID], [AUTHOR], [FILENAME], [DATEEXECUTED], [MD5SUM], [DESCRIPTION], [COMMENTS], [TAG], [LIQUIBASE]) VALUES (N'KENMEI-7383-Index-for-Attachment-ContentId_mssql', N'Communote', N'de/communardo/kenmei/database/update/v3.x/db.changelog.v3.3.xml', CAST(0x0000A6280120239D AS DateTime), N'98bcc7f663a8180888c83fb566828e', N'Custom SQL', N'', NULL, N'1.9.5')
INSERT [dbo].[DATABASECHANGELOG] ([ID], [AUTHOR], [FILENAME], [DATEEXECUTED], [MD5SUM], [DESCRIPTION], [COMMENTS], [TAG], [LIQUIBASE]) VALUES (N'KENMEI-7385-Crawl-Last-Modification-Date', N'Communote', N'de/communardo/kenmei/database/update/v3.x/db.changelog.v3.3.xml', CAST(0x0000A628012023A0 AS DateTime), N'5dbf4f53deff5ccee7175ce559252c7', N'Add Column', N'', NULL, N'1.9.5')
INSERT [dbo].[DATABASECHANGELOG] ([ID], [AUTHOR], [FILENAME], [DATEEXECUTED], [MD5SUM], [DESCRIPTION], [COMMENTS], [TAG], [LIQUIBASE]) VALUES (N'KENMEI-7385-Crawl-Last-Modification-Date-Not-Null', N'Communote', N'de/communardo/kenmei/database/update/v3.x/db.changelog.v3.3.xml', CAST(0x0000A628012023A1 AS DateTime), N'81be19ddddc7fa6b719d8a3d6f0889f', N'Modify Column', N'', NULL, N'1.9.5')
INSERT [dbo].[DATABASECHANGELOG] ([ID], [AUTHOR], [FILENAME], [DATEEXECUTED], [MD5SUM], [DESCRIPTION], [COMMENTS], [TAG], [LIQUIBASE]) VALUES (N'KENMEI-7386-Crawl-Last-Modification-Date-Note', N'Communote', N'de/communardo/kenmei/database/update/v3.x/db.changelog.v3.3.xml', CAST(0x0000A628012023A3 AS DateTime), N'ed93ec5d51918c57aaf0415a1228ee88', N'Add Column', N'', NULL, N'1.9.5')
INSERT [dbo].[DATABASECHANGELOG] ([ID], [AUTHOR], [FILENAME], [DATEEXECUTED], [MD5SUM], [DESCRIPTION], [COMMENTS], [TAG], [LIQUIBASE]) VALUES (N'KENMEI-7386-Crawl-Last-Modification-Date-Note-Not-Null', N'Communote', N'de/communardo/kenmei/database/update/v3.x/db.changelog.v3.3.xml', CAST(0x0000A628012023A5 AS DateTime), N'eaddef8b311ed12935ccfdb45b866f3', N'Modify Column', N'', NULL, N'1.9.5')
INSERT [dbo].[DATABASECHANGELOG] ([ID], [AUTHOR], [FILENAME], [DATEEXECUTED], [MD5SUM], [DESCRIPTION], [COMMENTS], [TAG], [LIQUIBASE]) VALUES (N'KENMEI-7392-Properties-for-Attachments_fkc_2-v2', N'Communote', N'de/communardo/kenmei/database/update/v3.x/db.changelog.v3.3.xml', CAST(0x0000A6280120239B AS DateTime), N'6553f6171bbb6e5d74ccd51a723fdc40', N'Add Foreign Key Constraint', N'', NULL, N'1.9.5')
INSERT [dbo].[DATABASECHANGELOG] ([ID], [AUTHOR], [FILENAME], [DATEEXECUTED], [MD5SUM], [DESCRIPTION], [COMMENTS], [TAG], [LIQUIBASE]) VALUES (N'KENMEI-7392-Properties-for-Attachments-IDENTITY_mssql', N'Communote', N'de/communardo/kenmei/database/update/v3.x/db.changelog.v3.3.xml', CAST(0x0000A62801202399 AS DateTime), N'fa7e87771469436ab1f769ce7ddca4c6', N'Custom SQL, Drop Column, Custom SQL', N'', NULL, N'1.9.5')
INSERT [dbo].[DATABASECHANGELOG] ([ID], [AUTHOR], [FILENAME], [DATEEXECUTED], [MD5SUM], [DESCRIPTION], [COMMENTS], [TAG], [LIQUIBASE]) VALUES (N'KENMEI-7392-Properties-for-Attachments-v2', N'Communote', N'de/communardo/kenmei/database/update/v3.x/db.changelog.v3.3.xml', CAST(0x0000A62801202397 AS DateTime), N'cd90d6cdfddc6fd3132373d6ea1d1ea9', N'Create Table', N'', NULL, N'1.9.5')
INSERT [dbo].[DATABASECHANGELOG] ([ID], [AUTHOR], [FILENAME], [DATEEXECUTED], [MD5SUM], [DESCRIPTION], [COMMENTS], [TAG], [LIQUIBASE]) VALUES (N'KENMEI-7477-Correct-Datetime-Datatype_v2', N'Communote', N'de/communardo/kenmei/database/update/v3.x/db.changelog.v3.3.xml', CAST(0x0000A628012023A9 AS DateTime), N'69915965652e9e70879f1164b3ff6167', N'Custom SQL', N'', NULL, N'1.9.5')
INSERT [dbo].[DATABASECHANGELOG] ([ID], [AUTHOR], [FILENAME], [DATEEXECUTED], [MD5SUM], [DESCRIPTION], [COMMENTS], [TAG], [LIQUIBASE]) VALUES (N'KENMEI-7523_remove_old_jobs', N'Communote', N'com/communote/server/liquibase/update/v3.x/db.changelog.v3.4.xml', CAST(0x0000A62801202581 AS DateTime), N'd38b603a8ea8cde9543cb7ebba9ea0fc', N'Delete Data', N'', NULL, N'1.9.5')
INSERT [dbo].[DATABASECHANGELOG] ([ID], [AUTHOR], [FILENAME], [DATEEXECUTED], [MD5SUM], [DESCRIPTION], [COMMENTS], [TAG], [LIQUIBASE]) VALUES (N'KENMEI-7524_remove_license_mode_subscription', N'Communote', N'com/communote/server/liquibase/update/v3.x/db.changelog.v3.4.xml', CAST(0x0000A628012023AA AS DateTime), N'716e958a59e65ba079b9ab4b2c4cb5c', N'Drop Table', N'', NULL, N'1.9.5')
INSERT [dbo].[DATABASECHANGELOG] ([ID], [AUTHOR], [FILENAME], [DATEEXECUTED], [MD5SUM], [DESCRIPTION], [COMMENTS], [TAG], [LIQUIBASE]) VALUES (N'KENMEI-7524_remove_license_table_v2', N'Communote', N'com/communote/server/liquibase/update/v3.x/db.changelog.v3.4.xml', CAST(0x0000A6280120257D AS DateTime), N'aa32adb832b1e3df63aa193cced44f9', N'Drop Table', N'', NULL, N'1.9.5')
INSERT [dbo].[DATABASECHANGELOG] ([ID], [AUTHOR], [FILENAME], [DATEEXECUTED], [MD5SUM], [DESCRIPTION], [COMMENTS], [TAG], [LIQUIBASE]) VALUES (N'KENMEI-7524_remove_new_credits_tables_v2', N'Communote', N'com/communote/server/liquibase/update/v3.x/db.changelog.v3.4.xml', CAST(0x0000A628012024F4 AS DateTime), N'b6db57b305ec7f0669cbead915423f', N'Drop Table (x2)', N'', NULL, N'1.9.5')
INSERT [dbo].[DATABASECHANGELOG] ([ID], [AUTHOR], [FILENAME], [DATEEXECUTED], [MD5SUM], [DESCRIPTION], [COMMENTS], [TAG], [LIQUIBASE]) VALUES (N'KENMEI-7524_remove_old_credits_tables_v2', N'Communote', N'com/communote/server/liquibase/update/v3.x/db.changelog.v3.4.xml', CAST(0x0000A62801202473 AS DateTime), N'a9f2d3d5ffa903cbbacb1ab77292098', N'Drop Table (x2)', N'', NULL, N'1.9.5')
INSERT [dbo].[DATABASECHANGELOG] ([ID], [AUTHOR], [FILENAME], [DATEEXECUTED], [MD5SUM], [DESCRIPTION], [COMMENTS], [TAG], [LIQUIBASE]) VALUES (N'KENMEI-7537_act_code_new_ms', N'Communote', N'com/communote/server/liquibase/update/v3.x/db.changelog.v3.4.xml', CAST(0x0000A62801202783 AS DateTime), N'b88c5594c1d6ac907be02ea421f533', N'Drop Table', N'', NULL, N'1.9.5')
INSERT [dbo].[DATABASECHANGELOG] ([ID], [AUTHOR], [FILENAME], [DATEEXECUTED], [MD5SUM], [DESCRIPTION], [COMMENTS], [TAG], [LIQUIBASE]) VALUES (N'KENMEI-7537_act_code_old_ms', N'Communote', N'com/communote/server/liquibase/update/v3.x/db.changelog.v3.4.xml', CAST(0x0000A62801202709 AS DateTime), N'712164e5ece412f668975c46267abb7', N'Drop Table', N'', NULL, N'1.9.5')
INSERT [dbo].[DATABASECHANGELOG] ([ID], [AUTHOR], [FILENAME], [DATEEXECUTED], [MD5SUM], [DESCRIPTION], [COMMENTS], [TAG], [LIQUIBASE]) VALUES (N'KENMEI-7537_cleanup_client_table', N'Communote', N'com/communote/server/liquibase/update/v3.x/db.changelog.v3.4.xml', CAST(0x0000A62801202583 AS DateTime), N'd7a8f6e553ad6b021bf0f361f2bf', N'Drop Column', N'', NULL, N'1.9.5')
INSERT [dbo].[DATABASECHANGELOG] ([ID], [AUTHOR], [FILENAME], [DATEEXECUTED], [MD5SUM], [DESCRIPTION], [COMMENTS], [TAG], [LIQUIBASE]) VALUES (N'KENMEI-7537_cleanup_client_tables_ms', N'Communote', N'com/communote/server/liquibase/update/v3.x/db.changelog.v3.4.xml', CAST(0x0000A6280120265D AS DateTime), N'f4967e216769683555bd48993f906a', N'Drop Index (x3), Drop Foreign Key Constraint (x3), Drop Column (x4), Drop Table (x6)', N'', NULL, N'1.9.5')
INSERT [dbo].[DATABASECHANGELOG] ([ID], [AUTHOR], [FILENAME], [DATEEXECUTED], [MD5SUM], [DESCRIPTION], [COMMENTS], [TAG], [LIQUIBASE]) VALUES (N'KENMEI-7547_modification_time_of_setting', N'Communote', N'com/communote/server/liquibase/update/v3.x/db.changelog.v3.4.xml', CAST(0x0000A62801202786 AS DateTime), N'78c1f960fb942e58e57baf7ebcf9a41', N'Add Column', N'', NULL, N'1.9.5')
INSERT [dbo].[DATABASECHANGELOG] ([ID], [AUTHOR], [FILENAME], [DATEEXECUTED], [MD5SUM], [DESCRIPTION], [COMMENTS], [TAG], [LIQUIBASE]) VALUES (N'KENMEI-Binary-Properties', N'unknown', N'de/communardo/kenmei/database/update/v1.3/db.changelog.v1.3.xml', CAST(0x00009E8000A813BC AS DateTime), N'901bbfc8af68f655b2185ac26d7b126', N'Create Table', N'', NULL, N'1.9.2');
print 'Processed 200 total records'
INSERT [dbo].[DATABASECHANGELOG] ([ID], [AUTHOR], [FILENAME], [DATEEXECUTED], [MD5SUM], [DESCRIPTION], [COMMENTS], [TAG], [LIQUIBASE]) VALUES (N'KENMEI-Blog-Properties', N'unknown', N'de/communardo/kenmei/database/update/v1.3.2/db.changelog.v1.3.2.xml', CAST(0x00009E8000A81BF0 AS DateTime), N'6f5bc99c34318034fda2208600b773', N'Create Table', N'', NULL, N'1.9.2')
INSERT [dbo].[DATABASECHANGELOG] ([ID], [AUTHOR], [FILENAME], [DATEEXECUTED], [MD5SUM], [DESCRIPTION], [COMMENTS], [TAG], [LIQUIBASE]) VALUES (N'KENMEI-Blog-Properties_autoincrement', N'unknown', N'de/communardo/kenmei/database/update/v1.3.2/db.changelog.v1.3.2.xml', CAST(0x00009E8000A81D1C AS DateTime), N'8c8dfcead1ae690398bd7706d2851d', N'Set Column as Auto-Increment', N'', NULL, N'1.9.2')
INSERT [dbo].[DATABASECHANGELOG] ([ID], [AUTHOR], [FILENAME], [DATEEXECUTED], [MD5SUM], [DESCRIPTION], [COMMENTS], [TAG], [LIQUIBASE]) VALUES (N'KENMEI-Blog-Properties_fkc', N'unknown', N'de/communardo/kenmei/database/update/v1.3.2/db.changelog.v1.3.2.xml', CAST(0x00009E8000A81D1C AS DateTime), N'e8ba9d17768c1e979aec77c75996bf1a', N'Add Foreign Key Constraint', N'', NULL, N'1.9.2')
INSERT [dbo].[DATABASECHANGELOG] ([ID], [AUTHOR], [FILENAME], [DATEEXECUTED], [MD5SUM], [DESCRIPTION], [COMMENTS], [TAG], [LIQUIBASE]) VALUES (N'KENMEI-Drop_snc_tables', N'unknown', N'de/communardo/kenmei/database/update/v1.3/db.changelog.v1.3.xml', CAST(0x00009E8000A81290 AS DateTime), N'1996781dbc24996e36864b7ece8f61d', N'Drop Table (x2)', N'', NULL, N'1.9.2')
INSERT [dbo].[DATABASECHANGELOG] ([ID], [AUTHOR], [FILENAME], [DATEEXECUTED], [MD5SUM], [DESCRIPTION], [COMMENTS], [TAG], [LIQUIBASE]) VALUES (N'KENMEI-Note-Properties_clob', N'unknown', N'de/communardo/kenmei/database/update/v1.3/db.changelog.v1.3.xml', CAST(0x00009E8000A8186C AS DateTime), N'61daee3a8c3850f6402bdb11ab5a49bb', N'Modify Column', N'', NULL, N'1.9.2')
INSERT [dbo].[DATABASECHANGELOG] ([ID], [AUTHOR], [FILENAME], [DATEEXECUTED], [MD5SUM], [DESCRIPTION], [COMMENTS], [TAG], [LIQUIBASE]) VALUES (N'KENMEI-Note-properties_mysql', N'unknown', N'de/communardo/kenmei/database/update/v1.3/db.changelog.v1.3.xml', CAST(0x00009E8000A81740 AS DateTime), N'3996e5ba99563e196a658722edc496', N'Custom SQL, Add Foreign Key Constraint', N'', NULL, N'1.9.2')
INSERT [dbo].[DATABASECHANGELOG] ([ID], [AUTHOR], [FILENAME], [DATEEXECUTED], [MD5SUM], [DESCRIPTION], [COMMENTS], [TAG], [LIQUIBASE]) VALUES (N'KENMEI-User-Grop-Properties_mysql', N'unknown', N'de/communardo/kenmei/database/update/v1.3/db.changelog.v1.3.xml', CAST(0x00009E8000A813BC AS DateTime), N'669a95d06bcb29abe32926762f76d3', N'Custom SQL, Add Foreign Key Constraint, Custom SQL, Add Foreign Key Constraint', N'', NULL, N'1.9.2')
INSERT [dbo].[DATABASECHANGELOG] ([ID], [AUTHOR], [FILENAME], [DATEEXECUTED], [MD5SUM], [DESCRIPTION], [COMMENTS], [TAG], [LIQUIBASE]) VALUES (N'KENMEI-User-Group-Properties_clob', N'unknown', N'de/communardo/kenmei/database/update/v1.3/db.changelog.v1.3.xml', CAST(0x00009E8000A813BC AS DateTime), N'b51dcb936e4e658ea6d26c6fd7f0903b', N'Modify Column (x2)', N'', NULL, N'1.9.2')
INSERT [dbo].[DATABASECHANGELOG] ([ID], [AUTHOR], [FILENAME], [DATEEXECUTED], [MD5SUM], [DESCRIPTION], [COMMENTS], [TAG], [LIQUIBASE]) VALUES (N'Like-Funktion', N'UNKNOWN', N'de/communardo/kenmei/database/update/v1.3.2/db.changelog.v1.3.2.xml', CAST(0x00009E8000A81AC4 AS DateTime), N'3bcadf67c0e431aad437221dbfbe6cf5', N'Create Table', N'', NULL, N'1.9.2')
INSERT [dbo].[DATABASECHANGELOG] ([ID], [AUTHOR], [FILENAME], [DATEEXECUTED], [MD5SUM], [DESCRIPTION], [COMMENTS], [TAG], [LIQUIBASE]) VALUES (N'Like-Funktion_MySQL', N'UNKNOWN', N'de/communardo/kenmei/database/update/v1.3.2/db.changelog.v1.3.2.xml', CAST(0x00009E8000A81BF0 AS DateTime), N'67a7975fddf52af67fae5416ae067', N'Add Foreign Key Constraint (x2), Set Column as Auto-Increment', N'', NULL, N'1.9.2')
INSERT [dbo].[DATABASECHANGELOG] ([ID], [AUTHOR], [FILENAME], [DATEEXECUTED], [MD5SUM], [DESCRIPTION], [COMMENTS], [TAG], [LIQUIBASE]) VALUES (N'mt_3272_1_mysql', N'unknown', N'de/communardo/kenmei/database/update/v1.1.3/db.changelog.v1.1.3.xml', CAST(0x00009E8000A80228 AS DateTime), N'f421adb0107c9087be3d4be78a9086f3', N'Custom SQL', N'MT 3272: UserProfile saves space instead of NULL if no time zone is selected', NULL, N'1.9.2')
INSERT [dbo].[DATABASECHANGELOG] ([ID], [AUTHOR], [FILENAME], [DATEEXECUTED], [MD5SUM], [DESCRIPTION], [COMMENTS], [TAG], [LIQUIBASE]) VALUES (N'mt_3314_refactor_confluence_page', N'unknown', N'de/communardo/kenmei/database/update/v1.1.3/db.changelog.v1.1.3.xml', CAST(0x00009E8000A80228 AS DateTime), N'86b2b96eee76c0d6bdaad4e1c24b5b25', N'Add Column', N'', NULL, N'1.9.2')
INSERT [dbo].[DATABASECHANGELOG] ([ID], [AUTHOR], [FILENAME], [DATEEXECUTED], [MD5SUM], [DESCRIPTION], [COMMENTS], [TAG], [LIQUIBASE]) VALUES (N'MT_3395_drop_source_column_from_blog_members', N'unknown', N'de/communardo/kenmei/database/update/v1.1.4/db.changelog.v1.1.4.xml', CAST(0x00009E8000A805AC AS DateTime), N'12af7efa26d7a9eefaedfcd1ad9e2d24', N'Drop Column', N'', NULL, N'1.9.2')
INSERT [dbo].[DATABASECHANGELOG] ([ID], [AUTHOR], [FILENAME], [DATEEXECUTED], [MD5SUM], [DESCRIPTION], [COMMENTS], [TAG], [LIQUIBASE]) VALUES (N'mt2846_favorite_users', N'unknown', N'de/communardo/kenmei/database/update/v1.1/db.changelog.v1.1.refactor.xml', CAST(0x00009E8000A80228 AS DateTime), N'9f4a84f23eb8c55a4730335d35c3ab7f', N'Rename Table', N'MT 2846: CR 119 - Refactoring Post Backend - Favorite users', NULL, N'1.9.2')
INSERT [dbo].[DATABASECHANGELOG] ([ID], [AUTHOR], [FILENAME], [DATEEXECUTED], [MD5SUM], [DESCRIPTION], [COMMENTS], [TAG], [LIQUIBASE]) VALUES (N'mt2846_global_id_checksum_v2', N'unknown', N'de/communardo/kenmei/database/update/v1.1/db.changelog.v1.1.refactor.xml', CAST(0x00009E8000A80228 AS DateTime), N'304f2354db312562949c37255a17bf', N'Custom SQL', N'MT 2846: CR 119 - Refactoring Post Backend: Global Id, Configuration, Checksum', NULL, N'1.9.2')
INSERT [dbo].[DATABASECHANGELOG] ([ID], [AUTHOR], [FILENAME], [DATEEXECUTED], [MD5SUM], [DESCRIPTION], [COMMENTS], [TAG], [LIQUIBASE]) VALUES (N'mt2846_global_id_v2_mysql', N'unknown', N'de/communardo/kenmei/database/update/v1.1/db.changelog.v1.1.refactor.xml', CAST(0x00009E8000A80228 AS DateTime), N'c0cbabe0cea6e8c89d81b7169f92e414', N'Insert Row', N'MT 2846: CR 119 - Refactoring Post Backend: Global Id', NULL, N'1.9.2')
INSERT [dbo].[DATABASECHANGELOG] ([ID], [AUTHOR], [FILENAME], [DATEEXECUTED], [MD5SUM], [DESCRIPTION], [COMMENTS], [TAG], [LIQUIBASE]) VALUES (N'mt2846_global_id_v3_mysql', N'unknown', N'de/communardo/kenmei/database/update/v1.1/db.changelog.v1.1.refactor.xml', CAST(0x00009F0300EFE7A0 AS DateTime), N'9a4a70e5bfa505daa43fb2ffbc162b', N'Insert Row', N'MT 2846: CR 119 - Refactoring Post Backend: Global Id', NULL, N'1.9.2')
INSERT [dbo].[DATABASECHANGELOG] ([ID], [AUTHOR], [FILENAME], [DATEEXECUTED], [MD5SUM], [DESCRIPTION], [COMMENTS], [TAG], [LIQUIBASE]) VALUES (N'mt2846_mysql_1', N'unknown', N'de/communardo/kenmei/database/update/v1.1/mysql/db.changelog.v1.1.refactor.mysql.xml', CAST(0x00009E8000A80228 AS DateTime), N'30d723e996f2fe7b25fddc235b2dcfcb', N'Drop Table (x6)', N'MT 2846: CR 119 - Refactoring Post Backend - MySql', NULL, N'1.9.2')
INSERT [dbo].[DATABASECHANGELOG] ([ID], [AUTHOR], [FILENAME], [DATEEXECUTED], [MD5SUM], [DESCRIPTION], [COMMENTS], [TAG], [LIQUIBASE]) VALUES (N'mt2846_mysql_10', N'unknown', N'de/communardo/kenmei/database/update/v1.1/mysql/db.changelog.v1.1.refactor.mysql.xml', CAST(0x00009E8000A80228 AS DateTime), N'cf28a7b028c31c85bc9ddeec98c856fc', N'Add Column (x7), Rename Column', N'MT 2846: CR 119 - Refactoring Post Backend - MySql', NULL, N'1.9.2')
INSERT [dbo].[DATABASECHANGELOG] ([ID], [AUTHOR], [FILENAME], [DATEEXECUTED], [MD5SUM], [DESCRIPTION], [COMMENTS], [TAG], [LIQUIBASE]) VALUES (N'mt2846_mysql_11', N'unknown', N'de/communardo/kenmei/database/update/v1.1/mysql/db.changelog.v1.1.refactor.mysql.xml', CAST(0x00009E8000A80228 AS DateTime), N'e3397a39ece50c8ba1ae089f831ac4b', N'SQL From File', N'MT 2846: CR 119 - Refactoring Post Backend - MySql', NULL, N'1.9.2')
INSERT [dbo].[DATABASECHANGELOG] ([ID], [AUTHOR], [FILENAME], [DATEEXECUTED], [MD5SUM], [DESCRIPTION], [COMMENTS], [TAG], [LIQUIBASE]) VALUES (N'mt2846_mysql_12', N'unknown', N'de/communardo/kenmei/database/update/v1.1/mysql/db.changelog.v1.1.refactor.mysql.xml', CAST(0x00009E8000A80228 AS DateTime), N'8f569acbd4f4184a409f268fc9cb46be', N'Add Not-Null Constraint (x3), Drop Table (x2)', N'MT 2846: CR 119 - Refactoring Post Backend - MySql', NULL, N'1.9.2')
INSERT [dbo].[DATABASECHANGELOG] ([ID], [AUTHOR], [FILENAME], [DATEEXECUTED], [MD5SUM], [DESCRIPTION], [COMMENTS], [TAG], [LIQUIBASE]) VALUES (N'mt2846_mysql_13', N'unknown', N'de/communardo/kenmei/database/update/v1.1/mysql/db.changelog.v1.1.refactor.mysql.xml', CAST(0x00009E8000A80228 AS DateTime), N'172934a379fa3cf0a5cf9ee1116b1', N'Rename Table', N'MT 2846: CR 119 - Refactoring Post Backend - MySql', NULL, N'1.9.2')
INSERT [dbo].[DATABASECHANGELOG] ([ID], [AUTHOR], [FILENAME], [DATEEXECUTED], [MD5SUM], [DESCRIPTION], [COMMENTS], [TAG], [LIQUIBASE]) VALUES (N'mt2846_mysql_14', N'unknown', N'de/communardo/kenmei/database/update/v1.1/mysql/db.changelog.v1.1.refactor.mysql.xml', CAST(0x00009E8000A80228 AS DateTime), N'53b64a5a1bb0c3235c619ce40ac1e5', N'Rename Column, Add Column (x3), Drop Table, Drop Column', N'MT 2846: CR 119 - Refactoring Post Backend - MySql', NULL, N'1.9.2')
INSERT [dbo].[DATABASECHANGELOG] ([ID], [AUTHOR], [FILENAME], [DATEEXECUTED], [MD5SUM], [DESCRIPTION], [COMMENTS], [TAG], [LIQUIBASE]) VALUES (N'mt2846_mysql_15', N'unknown', N'de/communardo/kenmei/database/update/v1.1/mysql/db.changelog.v1.1.refactor.mysql.xml', CAST(0x00009E8000A80228 AS DateTime), N'a15f2a38d192f1ea28297d763382923', N'Custom SQL', N'MT 2846: CR 119 - Refactoring Post Backend - MySql', NULL, N'1.9.2')
INSERT [dbo].[DATABASECHANGELOG] ([ID], [AUTHOR], [FILENAME], [DATEEXECUTED], [MD5SUM], [DESCRIPTION], [COMMENTS], [TAG], [LIQUIBASE]) VALUES (N'mt2846_mysql_16', N'unknown', N'de/communardo/kenmei/database/update/v1.1/mysql/db.changelog.v1.1.refactor.mysql.xml', CAST(0x00009E8000A80228 AS DateTime), N'f4f35c79c8f9cbdfa6fe278a2d1b0e', N'Rename Table', N'MT 2846: CR 119 - Refactoring Post Backend - MySql', NULL, N'1.9.2')
INSERT [dbo].[DATABASECHANGELOG] ([ID], [AUTHOR], [FILENAME], [DATEEXECUTED], [MD5SUM], [DESCRIPTION], [COMMENTS], [TAG], [LIQUIBASE]) VALUES (N'mt2846_mysql_17', N'unknown', N'de/communardo/kenmei/database/update/v1.1/mysql/db.changelog.v1.1.refactor.mysql.xml', CAST(0x00009E8000A80228 AS DateTime), N'2c781dee109292b6fd35f10b820c9e0', N'Rename Column (x5), Drop Column (x4)', N'MT 2846: CR 119 - Refactoring Post Backend - MySql', NULL, N'1.9.2')
INSERT [dbo].[DATABASECHANGELOG] ([ID], [AUTHOR], [FILENAME], [DATEEXECUTED], [MD5SUM], [DESCRIPTION], [COMMENTS], [TAG], [LIQUIBASE]) VALUES (N'mt2846_mysql_18', N'unknown', N'de/communardo/kenmei/database/update/v1.1/mysql/db.changelog.v1.1.refactor.mysql.xml', CAST(0x00009E8000A80228 AS DateTime), N'd41d8cd98f0b24e980998ecf8427e', N'Empty', N'MT 2846: CR 119 - Refactoring Post Backend - MySql', NULL, N'1.9.2')
INSERT [dbo].[DATABASECHANGELOG] ([ID], [AUTHOR], [FILENAME], [DATEEXECUTED], [MD5SUM], [DESCRIPTION], [COMMENTS], [TAG], [LIQUIBASE]) VALUES (N'mt2846_mysql_19', N'unknown', N'de/communardo/kenmei/database/update/v1.1/mysql/db.changelog.v1.1.refactor.mysql.xml', CAST(0x00009E8000A80228 AS DateTime), N'9d3c6d7f42f3efccf7973fc8135bde5', N'Add Foreign Key Constraint (x3), Add Unique Constraint (x3), Add Foreign Key Constraint (x11), Add Unique Constraint, Drop Foreign Key Constraint, Add Foreign Key Constraint (x3), Add Unique Constraint, Add Foreign Key Constraint', N'MT 2846: CR 119 - Refactoring Post Backend - MySql', NULL, N'1.9.2')
INSERT [dbo].[DATABASECHANGELOG] ([ID], [AUTHOR], [FILENAME], [DATEEXECUTED], [MD5SUM], [DESCRIPTION], [COMMENTS], [TAG], [LIQUIBASE]) VALUES (N'mt2846_mysql_2', N'unknown', N'de/communardo/kenmei/database/update/v1.1/mysql/db.changelog.v1.1.refactor.mysql.xml', CAST(0x00009E8000A80228 AS DateTime), N'a66cd5a4ddf1ad5b2ee81c0574673bd', N'Drop Foreign Key Constraint (x9)', N'MT 2846: CR 119 - Refactoring Post Backend - MySql', NULL, N'1.9.2')
INSERT [dbo].[DATABASECHANGELOG] ([ID], [AUTHOR], [FILENAME], [DATEEXECUTED], [MD5SUM], [DESCRIPTION], [COMMENTS], [TAG], [LIQUIBASE]) VALUES (N'mt2846_mysql_20', N'unknown', N'de/communardo/kenmei/database/update/v1.1/mysql/db.changelog.v1.1.refactor.mysql.xml', CAST(0x00009E8000A80228 AS DateTime), N'c59f2b28cf96874373cfeacffc881b4', N'SQL From File', N'MT 2846: CR 119 - Refactoring Post Backend - MySql', NULL, N'1.9.2')
INSERT [dbo].[DATABASECHANGELOG] ([ID], [AUTHOR], [FILENAME], [DATEEXECUTED], [MD5SUM], [DESCRIPTION], [COMMENTS], [TAG], [LIQUIBASE]) VALUES (N'mt2846_mysql_3', N'unknown', N'de/communardo/kenmei/database/update/v1.1/mysql/db.changelog.v1.1.refactor.mysql.xml', CAST(0x00009E8000A80228 AS DateTime), N'86c7a598426a561fc898d3bd5930a3e6', N'Create Table', N'MT 2846: CR 119 - Refactoring Post Backend - MySql', NULL, N'1.9.2')
INSERT [dbo].[DATABASECHANGELOG] ([ID], [AUTHOR], [FILENAME], [DATEEXECUTED], [MD5SUM], [DESCRIPTION], [COMMENTS], [TAG], [LIQUIBASE]) VALUES (N'mt2846_mysql_4', N'unknown', N'de/communardo/kenmei/database/update/v1.1/mysql/db.changelog.v1.1.refactor.mysql.xml', CAST(0x00009E8000A80228 AS DateTime), N'59f5a1279fabc9ed2a3c7f97edad36b8', N'Create Table', N'MT 2846: CR 119 - Refactoring Post Backend - MySql', NULL, N'1.9.2')
INSERT [dbo].[DATABASECHANGELOG] ([ID], [AUTHOR], [FILENAME], [DATEEXECUTED], [MD5SUM], [DESCRIPTION], [COMMENTS], [TAG], [LIQUIBASE]) VALUES (N'mt2846_mysql_4a', N'unknown', N'de/communardo/kenmei/database/update/v1.1/mysql/db.changelog.v1.1.refactor.mysql.xml', CAST(0x00009E8000A80228 AS DateTime), N'774a36dba4aff22ffbdf3bcf16ed6f', N'SQL From File', N'MT 2846: CR 119 - Refactoring Post Backend - MySql', NULL, N'1.9.2')
INSERT [dbo].[DATABASECHANGELOG] ([ID], [AUTHOR], [FILENAME], [DATEEXECUTED], [MD5SUM], [DESCRIPTION], [COMMENTS], [TAG], [LIQUIBASE]) VALUES (N'mt2846_mysql_5', N'unknown', N'de/communardo/kenmei/database/update/v1.1/mysql/db.changelog.v1.1.refactor.mysql.xml', CAST(0x00009E8000A80228 AS DateTime), N'937b3863b7ea9f194231bd2dafff6832', N'Drop Column (x7)', N'MT 2846: CR 119 - Refactoring Post Backend - MySql', NULL, N'1.9.2')
INSERT [dbo].[DATABASECHANGELOG] ([ID], [AUTHOR], [FILENAME], [DATEEXECUTED], [MD5SUM], [DESCRIPTION], [COMMENTS], [TAG], [LIQUIBASE]) VALUES (N'mt2846_mysql_6', N'unknown', N'de/communardo/kenmei/database/update/v1.1/mysql/db.changelog.v1.1.refactor.mysql.xml', CAST(0x00009E8000A80228 AS DateTime), N'a669428a7b42a371372f4e08556acb', N'SQL From File', N'MT 2846: CR 119 - Refactoring Post Backend - MySql', NULL, N'1.9.2')
INSERT [dbo].[DATABASECHANGELOG] ([ID], [AUTHOR], [FILENAME], [DATEEXECUTED], [MD5SUM], [DESCRIPTION], [COMMENTS], [TAG], [LIQUIBASE]) VALUES (N'mt2846_mysql_7', N'unknown', N'de/communardo/kenmei/database/update/v1.1/mysql/db.changelog.v1.1.refactor.mysql.xml', CAST(0x00009E8000A80228 AS DateTime), N'f431f661f875edb7a4c7122b691768', N'Drop Column, Rename Table, Add Not-Null Constraint', N'MT 2846: CR 119 - Refactoring Post Backend - MySql', NULL, N'1.9.2')
INSERT [dbo].[DATABASECHANGELOG] ([ID], [AUTHOR], [FILENAME], [DATEEXECUTED], [MD5SUM], [DESCRIPTION], [COMMENTS], [TAG], [LIQUIBASE]) VALUES (N'mt2846_mysql_8', N'unknown', N'de/communardo/kenmei/database/update/v1.1/mysql/db.changelog.v1.1.refactor.mysql.xml', CAST(0x00009E8000A80228 AS DateTime), N'44a6dfe46a8feb68eaece96d1ffd7ddf', N'SQL From File', N'MT 2846: CR 119 - Refactoring Post Backend - MySql', NULL, N'1.9.2')
INSERT [dbo].[DATABASECHANGELOG] ([ID], [AUTHOR], [FILENAME], [DATEEXECUTED], [MD5SUM], [DESCRIPTION], [COMMENTS], [TAG], [LIQUIBASE]) VALUES (N'mt2846_mysql_9', N'unknown', N'de/communardo/kenmei/database/update/v1.1/mysql/db.changelog.v1.1.refactor.mysql.xml', CAST(0x00009E8000A80228 AS DateTime), N'a8626d4dafc03b63fc2a64ba9cacfc9c', N'Rename Table', N'MT 2846: CR 119 - Refactoring Post Backend - MySql', NULL, N'1.9.2')
INSERT [dbo].[DATABASECHANGELOG] ([ID], [AUTHOR], [FILENAME], [DATEEXECUTED], [MD5SUM], [DESCRIPTION], [COMMENTS], [TAG], [LIQUIBASE]) VALUES (N'mt2846_remove_module_status', N'unknown', N'de/communardo/kenmei/database/update/v1.1/db.changelog.v1.1.refactor.xml', CAST(0x00009E8000A80228 AS DateTime), N'c616cb81c99fea1e6427599e6cec5187', N'Drop Column (x4)', N'MT 2846: CR 119 - Refactoring Post Backend - Remove module status', NULL, N'1.9.2')
INSERT [dbo].[DATABASECHANGELOG] ([ID], [AUTHOR], [FILENAME], [DATEEXECUTED], [MD5SUM], [DESCRIPTION], [COMMENTS], [TAG], [LIQUIBASE]) VALUES (N'mt2846_user_external_auth', N'unknown', N'de/communardo/kenmei/database/update/v1.1/db.changelog.v1.1.refactor.xml', CAST(0x00009E8000A80228 AS DateTime), N'6c8c9284a09b847b41d87b11499cf4b', N'Rename Table, Rename Column, Drop Foreign Key Constraint, Add Foreign Key Constraint', N'MT 2846: CR 119 - Refactoring Post Backend - Rename external user auth', NULL, N'1.9.2')
INSERT [dbo].[DATABASECHANGELOG] ([ID], [AUTHOR], [FILENAME], [DATEEXECUTED], [MD5SUM], [DESCRIPTION], [COMMENTS], [TAG], [LIQUIBASE]) VALUES (N'mt2859_2', N'unknown', N'de/communardo/kenmei/database/update/v1.0.3/db.changelog.v1.0.3.xml', CAST(0x00009E8000A80228 AS DateTime), N'b2e0e51b2fdd768127d43d458e21d939', N'Add Unique Constraint', N'', NULL, N'1.9.2')
INSERT [dbo].[DATABASECHANGELOG] ([ID], [AUTHOR], [FILENAME], [DATEEXECUTED], [MD5SUM], [DESCRIPTION], [COMMENTS], [TAG], [LIQUIBASE]) VALUES (N'mt2899_mysql_v3', N'unknown', N'de/communardo/kenmei/database/update/v1.0.3/db.changelog.v1.0.3.xml', CAST(0x00009E8000A80228 AS DateTime), N'2386128fd9dcb86b8fa469f8ff2ec81', N'Custom SQL', N'MT 2899: Image caching problems', NULL, N'1.9.2')
INSERT [dbo].[DATABASECHANGELOG] ([ID], [AUTHOR], [FILENAME], [DATEEXECUTED], [MD5SUM], [DESCRIPTION], [COMMENTS], [TAG], [LIQUIBASE]) VALUES (N'mt2940_1', N'unknown', N'de/communardo/kenmei/database/update/v1.1/db.changelog.v1.1.xml', CAST(0x00009E8000A80228 AS DateTime), N'b962fa5b9ddcecbc21d62b4999d8764', N'Update Data, Create Table, Add Foreign Key Constraint (x2)', N'CR 131: autosave refactoring', NULL, N'1.9.2')
INSERT [dbo].[DATABASECHANGELOG] ([ID], [AUTHOR], [FILENAME], [DATEEXECUTED], [MD5SUM], [DESCRIPTION], [COMMENTS], [TAG], [LIQUIBASE]) VALUES (N'mt2945_1', N'unknown', N'de/communardo/kenmei/database/update/v1.1/db.changelog.v1.1.xml', CAST(0x00009E8000A80228 AS DateTime), N'22b680833818a22cf3f5e695a7b185d', N'Add Column, Add Foreign Key Constraint', N'CR 109: Post without selecting a blog', NULL, N'1.9.2')
INSERT [dbo].[DATABASECHANGELOG] ([ID], [AUTHOR], [FILENAME], [DATEEXECUTED], [MD5SUM], [DESCRIPTION], [COMMENTS], [TAG], [LIQUIBASE]) VALUES (N'mt2957_1', N'unknown', N'de/communardo/kenmei/database/update/v1.1/db.changelog.v1.1.xml', CAST(0x00009E8000A80228 AS DateTime), N'2a75e566753c4ffb3f42c5fb9896cd', N'Add Column (x2)', N'CR 122 - Threaded answers', NULL, N'1.9.2')
INSERT [dbo].[DATABASECHANGELOG] ([ID], [AUTHOR], [FILENAME], [DATEEXECUTED], [MD5SUM], [DESCRIPTION], [COMMENTS], [TAG], [LIQUIBASE]) VALUES (N'mt2957_2_mysql', N'unknown', N'de/communardo/kenmei/database/update/v1.1/db.changelog.v1.1.xml', CAST(0x00009E8000A80228 AS DateTime), N'39dd942ccd10338659ba1bfdb94f9183', N'Custom SQL', N'CR 122 - Threaded answers', NULL, N'1.9.2')
INSERT [dbo].[DATABASECHANGELOG] ([ID], [AUTHOR], [FILENAME], [DATEEXECUTED], [MD5SUM], [DESCRIPTION], [COMMENTS], [TAG], [LIQUIBASE]) VALUES (N'mt2976_1', N'unknown', N'de/communardo/kenmei/database/update/v1.1/db.changelog.v1.1.xml', CAST(0x00009E8000A80228 AS DateTime), N'dcd4c199fd6bf5cd3ca66a34a84afa7', N'Add Column (x3)', N'Content Type', NULL, N'1.9.2')
INSERT [dbo].[DATABASECHANGELOG] ([ID], [AUTHOR], [FILENAME], [DATEEXECUTED], [MD5SUM], [DESCRIPTION], [COMMENTS], [TAG], [LIQUIBASE]) VALUES (N'mt2976_2', N'unknown', N'de/communardo/kenmei/database/update/v1.1/db.changelog.v1.1.xml', CAST(0x00009E8000A80228 AS DateTime), N'6b7314e763c81f1f96c67deb8d806d69', N'Custom SQL', N'Attachment Status', NULL, N'1.9.2')
INSERT [dbo].[DATABASECHANGELOG] ([ID], [AUTHOR], [FILENAME], [DATEEXECUTED], [MD5SUM], [DESCRIPTION], [COMMENTS], [TAG], [LIQUIBASE]) VALUES (N'mt3022_mysql', N'unknown', N'de/communardo/kenmei/database/update/v1.1/db.changelog.v1.1.xml', CAST(0x00009E8000A80228 AS DateTime), N'a4c8beb7d79b2eb7c18c2d883f2eb3c', N'Custom SQL', N'Increase Repository Limit', NULL, N'1.9.2')
INSERT [dbo].[DATABASECHANGELOG] ([ID], [AUTHOR], [FILENAME], [DATEEXECUTED], [MD5SUM], [DESCRIPTION], [COMMENTS], [TAG], [LIQUIBASE]) VALUES (N'mt3096_1', N'unknown', N'de/communardo/kenmei/database/update/v1.1/db.changelog.v1.1.xml', CAST(0x00009E8000A80228 AS DateTime), N'abcc6ad563565e4050381273ef3fcc5d', N'Custom SQL', N'deletes unconnected large user images', NULL, N'1.9.2')
INSERT [dbo].[DATABASECHANGELOG] ([ID], [AUTHOR], [FILENAME], [DATEEXECUTED], [MD5SUM], [DESCRIPTION], [COMMENTS], [TAG], [LIQUIBASE]) VALUES (N'mt3178_1_mysql', N'unknown', N'de/communardo/kenmei/database/update/v1.1/db.changelog.v1.1.xml', CAST(0x00009E8000A80228 AS DateTime), N'dd97e4a211a78df2d24fbda9552922b', N'Insert Row', N'Automated Client Approval', NULL, N'1.9.2')
INSERT [dbo].[DATABASECHANGELOG] ([ID], [AUTHOR], [FILENAME], [DATEEXECUTED], [MD5SUM], [DESCRIPTION], [COMMENTS], [TAG], [LIQUIBASE]) VALUES (N'mt3178_2_mysql', N'unknown', N'de/communardo/kenmei/database/update/v1.1/db.changelog.v1.1.xml', CAST(0x00009E8000A80228 AS DateTime), N'd1808dcb3e616cfff590fbbdf8494f', N'Insert Row', N'Automated Client Approval', NULL, N'1.9.2')
INSERT [dbo].[DATABASECHANGELOG] ([ID], [AUTHOR], [FILENAME], [DATEEXECUTED], [MD5SUM], [DESCRIPTION], [COMMENTS], [TAG], [LIQUIBASE]) VALUES (N'mt3187_1', N'unknown', N'de/communardo/kenmei/database/update/v1.1/db.changelog.v1.1.xml', CAST(0x00009E8000A80228 AS DateTime), N'77fa5d42e90366abddbbb7dbb94e61d', N'Drop Foreign Key Constraint', N'Adjust the forein key constraint for kenmei_users_fk.', NULL, N'1.9.2')
INSERT [dbo].[DATABASECHANGELOG] ([ID], [AUTHOR], [FILENAME], [DATEEXECUTED], [MD5SUM], [DESCRIPTION], [COMMENTS], [TAG], [LIQUIBASE]) VALUES (N'mt3187_2', N'unknown', N'de/communardo/kenmei/database/update/v1.1/db.changelog.v1.1.xml', CAST(0x00009E8000A80228 AS DateTime), N'1b315757661762a2659fde38c03259', N'Add Foreign Key Constraint', N'Adjust the forein key constraint for kenmei_users_fk.', NULL, N'1.9.2')
INSERT [dbo].[DATABASECHANGELOG] ([ID], [AUTHOR], [FILENAME], [DATEEXECUTED], [MD5SUM], [DESCRIPTION], [COMMENTS], [TAG], [LIQUIBASE]) VALUES (N'mt3187_3', N'unknown', N'de/communardo/kenmei/database/update/v1.1/db.changelog.v1.1.xml', CAST(0x00009E8000A80228 AS DateTime), N'77fa5d42e90366abddbbb7dbb94e61d', N'Drop Foreign Key Constraint', N'Adjust the forein key constraint for kenmei_users_fk.', NULL, N'1.9.2')
INSERT [dbo].[DATABASECHANGELOG] ([ID], [AUTHOR], [FILENAME], [DATEEXECUTED], [MD5SUM], [DESCRIPTION], [COMMENTS], [TAG], [LIQUIBASE]) VALUES (N'mt3187_4_mysql', N'unknown', N'de/communardo/kenmei/database/update/v1.1/db.changelog.v1.1.xml', CAST(0x00009E8000A80228 AS DateTime), N'925ad9965ef8812edba0b6ba81d9629', N'Custom SQL, Add Foreign Key Constraint', N'Adjust the forein key constraint for kenmei_users_fk.', NULL, N'1.9.2')
INSERT [dbo].[DATABASECHANGELOG] ([ID], [AUTHOR], [FILENAME], [DATEEXECUTED], [MD5SUM], [DESCRIPTION], [COMMENTS], [TAG], [LIQUIBASE]) VALUES (N'mt3196_1', N'unknown', N'de/communardo/kenmei/database/update/v1.1.2/db.changelog.v1.1.2.xml', CAST(0x00009E8000A80228 AS DateTime), N'3f4f52f46b355cec745a24706cca46d4', N'Add Column', N'CR 100 - Support Time Zones: add new column to user_profile', NULL, N'1.9.2')
INSERT [dbo].[DATABASECHANGELOG] ([ID], [AUTHOR], [FILENAME], [DATEEXECUTED], [MD5SUM], [DESCRIPTION], [COMMENTS], [TAG], [LIQUIBASE]) VALUES (N'mt3196_2', N'unknown', N'de/communardo/kenmei/database/update/v1.1.2/db.changelog.v1.1.2.xml', CAST(0x00009E8000A80228 AS DateTime), N'dca47279adc3bbd1ad7e1d7174e10b1', N'Add Column (x2)', N'CR 100 - Support Time Zones: add new column to user_client_creation, configuration_client', NULL, N'1.9.2')
INSERT [dbo].[DATABASECHANGELOG] ([ID], [AUTHOR], [FILENAME], [DATEEXECUTED], [MD5SUM], [DESCRIPTION], [COMMENTS], [TAG], [LIQUIBASE]) VALUES (N'mt3208', N'unknown', N'de/communardo/kenmei/database/update/v1.1.2/db.changelog.v1.1.2.xml', CAST(0x00009E8000A80228 AS DateTime), N'a5e3666944ac3f968080ff852bd777d3', N'Add Column', N'CR 68 Read-More functionality', NULL, N'1.9.2')
INSERT [dbo].[DATABASECHANGELOG] ([ID], [AUTHOR], [FILENAME], [DATEEXECUTED], [MD5SUM], [DESCRIPTION], [COMMENTS], [TAG], [LIQUIBASE]) VALUES (N'mt3277_1_mysql', N'unknown', N'de/communardo/kenmei/database/update/v1.1.3/db.changelog.v1.1.3.xml', CAST(0x00009E8000A80228 AS DateTime), N'2ea8e41788851b5f4a58846e05bcc82', N'Create Table, Custom SQL, Drop Column, Add Foreign Key Constraint (x2)', N'CR 96 - support for groups: add user_entity and copy users', NULL, N'1.9.2')
INSERT [dbo].[DATABASECHANGELOG] ([ID], [AUTHOR], [FILENAME], [DATEEXECUTED], [MD5SUM], [DESCRIPTION], [COMMENTS], [TAG], [LIQUIBASE]) VALUES (N'mt3277_10_mysql', N'unknown', N'de/communardo/kenmei/database/update/v1.1.3/db.changelog.v1.1.3.xml', CAST(0x00009E8000A80228 AS DateTime), N'fa346348fef8d167f2390c9b247f053', N'Create Table, Add Primary Key, Add Foreign Key Constraint (x2)', N'CR 96 - support for groups: allow several granting groups', NULL, N'1.9.2')
INSERT [dbo].[DATABASECHANGELOG] ([ID], [AUTHOR], [FILENAME], [DATEEXECUTED], [MD5SUM], [DESCRIPTION], [COMMENTS], [TAG], [LIQUIBASE]) VALUES (N'mt3277_11_mysql', N'unknown', N'de/communardo/kenmei/database/update/v1.1.3/db.changelog.v1.1.3.xml', CAST(0x00009E8000A80228 AS DateTime), N'c83fd6a7d42279a72d55ededfcfa4d86', N'Create Table, Add Primary Key, Add Foreign Key Constraint (x2)', N'CR 96 - support for groups: connect entity with group', NULL, N'1.9.2')
INSERT [dbo].[DATABASECHANGELOG] ([ID], [AUTHOR], [FILENAME], [DATEEXECUTED], [MD5SUM], [DESCRIPTION], [COMMENTS], [TAG], [LIQUIBASE]) VALUES (N'mt3277_13_mysql', N'unknown', N'de/communardo/kenmei/database/update/v1.1.3/db.changelog.v1.1.3.xml', CAST(0x00009E8000A80228 AS DateTime), N'4691cf11e2e377d7897b8abdecc6d88d', N'Add Not-Null Constraint (x2)', N'CR 96 - support for groups: not null constraints for all_can_x rights', NULL, N'1.9.2')
INSERT [dbo].[DATABASECHANGELOG] ([ID], [AUTHOR], [FILENAME], [DATEEXECUTED], [MD5SUM], [DESCRIPTION], [COMMENTS], [TAG], [LIQUIBASE]) VALUES (N'mt3277_14', N'unknown', N'de/communardo/kenmei/database/update/v1.1.3/db.changelog.v1.1.3.xml', CAST(0x00009E8000A80228 AS DateTime), N'e694eb5228924a36a25357646af454e4', N'Add Column, Custom SQL, Add Not-Null Constraint', N'CR 96 - support for groups: grantedByGroup flag for mapping table', NULL, N'1.9.2')
INSERT [dbo].[DATABASECHANGELOG] ([ID], [AUTHOR], [FILENAME], [DATEEXECUTED], [MD5SUM], [DESCRIPTION], [COMMENTS], [TAG], [LIQUIBASE]) VALUES (N'mt3277_2_mysql', N'unknown', N'de/communardo/kenmei/database/update/v1.1.3/db.changelog.v1.1.3.xml', CAST(0x00009E8000A80228 AS DateTime), N'88fa38c4a5f8ae66e1cbae3414dfccf', N'Drop Table (x2)', N'CR 96 - support for groups: remove obsolete security codes', NULL, N'1.9.2')
INSERT [dbo].[DATABASECHANGELOG] ([ID], [AUTHOR], [FILENAME], [DATEEXECUTED], [MD5SUM], [DESCRIPTION], [COMMENTS], [TAG], [LIQUIBASE]) VALUES (N'mt3277_3_mysql', N'unknown', N'de/communardo/kenmei/database/update/v1.1.3/db.changelog.v1.1.3.xml', CAST(0x00009E8000A80228 AS DateTime), N'f595d3d3bb28aa16d7567246a3a4585', N'Add Column, Custom SQL', N'CR 96 - support for groups: copy all_can_x rights from group to blog', NULL, N'1.9.2')
INSERT [dbo].[DATABASECHANGELOG] ([ID], [AUTHOR], [FILENAME], [DATEEXECUTED], [MD5SUM], [DESCRIPTION], [COMMENTS], [TAG], [LIQUIBASE]) VALUES (N'mt3277_4_mysql', N'unknown', N'de/communardo/kenmei/database/update/v1.1.3/db.changelog.v1.1.3.xml', CAST(0x00009E8000A80228 AS DateTime), N'25c695869c8f851fd412ca90db5b2677', N'Add Column, Custom SQL', N'CR 96 - support for groups: add blogs to group member', NULL, N'1.9.2')
INSERT [dbo].[DATABASECHANGELOG] ([ID], [AUTHOR], [FILENAME], [DATEEXECUTED], [MD5SUM], [DESCRIPTION], [COMMENTS], [TAG], [LIQUIBASE]) VALUES (N'mt3277_5_mysql', N'unknown', N'de/communardo/kenmei/database/update/v1.1.3/db.changelog.v1.1.3.xml', CAST(0x00009E8000A80228 AS DateTime), N'ce8baa4707c58ddfba1523c015deb0', N'Drop Foreign Key Constraint (x2), Drop Column (x2), Custom SQL, Rename Table, Rename Column (x2), Add Column, Add Foreign Key Constraint (x2), Add Not-Null Constraint (x3)', N'CR 96 - support for groups: group member to blog member', NULL, N'1.9.2')
INSERT [dbo].[DATABASECHANGELOG] ([ID], [AUTHOR], [FILENAME], [DATEEXECUTED], [MD5SUM], [DESCRIPTION], [COMMENTS], [TAG], [LIQUIBASE]) VALUES (N'mt3277_6_mysql', N'unknown', N'de/communardo/kenmei/database/update/v1.1.3/db.changelog.v1.1.3.xml', CAST(0x00009E8000A80228 AS DateTime), N'da4346ff7575e6ebf6b3cd1266164', N'Drop Foreign Key Constraint, Drop Column', N'CR 96 - support for groups: cleanup core_blog', NULL, N'1.9.2')
INSERT [dbo].[DATABASECHANGELOG] ([ID], [AUTHOR], [FILENAME], [DATEEXECUTED], [MD5SUM], [DESCRIPTION], [COMMENTS], [TAG], [LIQUIBASE]) VALUES (N'mt3277_7_mysql', N'unknown', N'de/communardo/kenmei/database/update/v1.1.3/db.changelog.v1.1.3.xml', CAST(0x00009E8000A80228 AS DateTime), N'3e47ee3a105c684a582e423518decca', N'Drop Column (x3), Delete Data, Add Column (x3), Add Foreign Key Constraint', N'CR 96 - support for groups: fix user_group', NULL, N'1.9.2')
INSERT [dbo].[DATABASECHANGELOG] ([ID], [AUTHOR], [FILENAME], [DATEEXECUTED], [MD5SUM], [DESCRIPTION], [COMMENTS], [TAG], [LIQUIBASE]) VALUES (N'mt3277_8_mysql', N'unknown', N'de/communardo/kenmei/database/update/v1.1.3/db.changelog.v1.1.3.xml', CAST(0x00009E8000A80228 AS DateTime), N'9a394f311e01bce782f7d562045c46b', N'Create Table', N'CR 96 - support for groups: add helper table for fast blog role lookup', NULL, N'1.9.2')
INSERT [dbo].[DATABASECHANGELOG] ([ID], [AUTHOR], [FILENAME], [DATEEXECUTED], [MD5SUM], [DESCRIPTION], [COMMENTS], [TAG], [LIQUIBASE]) VALUES (N'mt3277_9_mysql', N'unknown', N'de/communardo/kenmei/database/update/v1.1.3/db.changelog.v1.1.3.xml', CAST(0x00009E8000A80228 AS DateTime), N'ae6438d32dde3f4c6124ca8330b5ec6', N'Custom SQL', N'CR 96 - support for groups: fill core_role2blog', NULL, N'1.9.2')
INSERT [dbo].[DATABASECHANGELOG] ([ID], [AUTHOR], [FILENAME], [DATEEXECUTED], [MD5SUM], [DESCRIPTION], [COMMENTS], [TAG], [LIQUIBASE]) VALUES (N'mt3277_fix_null_constraint', N'unknown', N'de/communardo/kenmei/database/update/v1.1.3/db.changelog.v1.1.3.xml', CAST(0x00009E8000A80228 AS DateTime), N'97f2d0c8945bae1c69f8f827c57e73', N'Drop Not-Null Constraint', N'Drop wrong \"not null\" constraint from user entities.', NULL, N'1.9.2')
INSERT [dbo].[DATABASECHANGELOG] ([ID], [AUTHOR], [FILENAME], [DATEEXECUTED], [MD5SUM], [DESCRIPTION], [COMMENTS], [TAG], [LIQUIBASE]) VALUES (N'mt3281_1_mysql', N'unknown', N'de/communardo/kenmei/database/update/v1.1.3/db.changelog.v1.1.3.xml', CAST(0x00009E8000A80228 AS DateTime), N'3ebca4821f7296e11d97c5913fe8707c', N'Insert Row', N'CR 134 - Anonymous Access, Anonymous User', NULL, N'1.9.2')
INSERT [dbo].[DATABASECHANGELOG] ([ID], [AUTHOR], [FILENAME], [DATEEXECUTED], [MD5SUM], [DESCRIPTION], [COMMENTS], [TAG], [LIQUIBASE]) VALUES (N'mt3281_2_mysql', N'unknown', N'de/communardo/kenmei/database/update/v1.1.3/db.changelog.v1.1.3.xml', CAST(0x00009E8000A80228 AS DateTime), N'f4972baac8a2f4504d77fd85c7d15e9', N'Add Column, Add Not-Null Constraint', N'CR 134 - Anonymous Access, Anonymous User', NULL, N'1.9.2')
INSERT [dbo].[DATABASECHANGELOG] ([ID], [AUTHOR], [FILENAME], [DATEEXECUTED], [MD5SUM], [DESCRIPTION], [COMMENTS], [TAG], [LIQUIBASE]) VALUES (N'mt3283_2_mysql', N'unknown', N'de/communardo/kenmei/database/update/v1.1.3/db.changelog.v1.1.3.xml', CAST(0x00009E8000A80228 AS DateTime), N'22711a3246654a434873c0e1fbf78119', N'Set Column as Auto-Increment, Add Not-Null Constraint', N'Refactor External Authentication', NULL, N'1.9.2')
INSERT [dbo].[DATABASECHANGELOG] ([ID], [AUTHOR], [FILENAME], [DATEEXECUTED], [MD5SUM], [DESCRIPTION], [COMMENTS], [TAG], [LIQUIBASE]) VALUES (N'mt3283_confluence_configuration', N'unknown', N'de/communardo/kenmei/database/update/v1.1.3/db.changelog.v1.1.3.xml', CAST(0x00009E8000A80228 AS DateTime), N'5029a38aca9b7c4c25a4f41f619caa1c', N'Add Column (x2)', N'', NULL, N'1.9.2')
INSERT [dbo].[DATABASECHANGELOG] ([ID], [AUTHOR], [FILENAME], [DATEEXECUTED], [MD5SUM], [DESCRIPTION], [COMMENTS], [TAG], [LIQUIBASE]) VALUES (N'mt3283_external_configuration', N'unknown', N'de/communardo/kenmei/database/update/v1.1.3/db.changelog.v1.1.3.xml', CAST(0x00009E8000A80228 AS DateTime), N'c2c4eb85364225670b85647601134e0', N'Rename Table (x3), Rename Column, Add Column, Drop Foreign Key Constraint, Add Foreign Key Constraint', N'Refactor External Authentication', NULL, N'1.9.2')
INSERT [dbo].[DATABASECHANGELOG] ([ID], [AUTHOR], [FILENAME], [DATEEXECUTED], [MD5SUM], [DESCRIPTION], [COMMENTS], [TAG], [LIQUIBASE]) VALUES (N'mt3283_external_objects', N'unknown', N'de/communardo/kenmei/database/update/v1.1.3/db.changelog.v1.1.3.xml', CAST(0x00009E8000A80228 AS DateTime), N'75db72d38b92ade15a93f1b854b9048', N'Create Table, Add Unique Constraint, Create Table, Add Foreign Key Constraint (x2), Add Column', N'', NULL, N'1.9.2')
INSERT [dbo].[DATABASECHANGELOG] ([ID], [AUTHOR], [FILENAME], [DATEEXECUTED], [MD5SUM], [DESCRIPTION], [COMMENTS], [TAG], [LIQUIBASE]) VALUES (N'mt3283_external_objects_2', N'unknown', N'de/communardo/kenmei/database/update/v1.1.3/db.changelog.v1.1.3.xml', CAST(0x00009E8000A80228 AS DateTime), N'ff7ed34175992ead7a175351c0d4e9', N'Add Column, Add Not-Null Constraint', N'Add class to blog_member', NULL, N'1.9.2')
INSERT [dbo].[DATABASECHANGELOG] ([ID], [AUTHOR], [FILENAME], [DATEEXECUTED], [MD5SUM], [DESCRIPTION], [COMMENTS], [TAG], [LIQUIBASE]) VALUES (N'mt3283_external_objects_3', N'unknown', N'de/communardo/kenmei/database/update/v1.1.3/db.changelog.v1.1.3.xml', CAST(0x00009E8000A80228 AS DateTime), N'd3cc6233f8abccd7d6c2eb5017cf77', N'Add Column', N'', NULL, N'1.9.2')
INSERT [dbo].[DATABASECHANGELOG] ([ID], [AUTHOR], [FILENAME], [DATEEXECUTED], [MD5SUM], [DESCRIPTION], [COMMENTS], [TAG], [LIQUIBASE]) VALUES (N'mt3283_external_objects_fix_auto_increment_mysql', N'unknown', N'de/communardo/kenmei/database/update/v1.1.3/db.changelog.v1.1.3.xml', CAST(0x00009E8000A80228 AS DateTime), N'c7f24c22f84e8eff695ba917c8fd6d7', N'Custom SQL', N'CR 135 - Support Synchronization of blog rights with external systems', NULL, N'1.9.2')
INSERT [dbo].[DATABASECHANGELOG] ([ID], [AUTHOR], [FILENAME], [DATEEXECUTED], [MD5SUM], [DESCRIPTION], [COMMENTS], [TAG], [LIQUIBASE]) VALUES (N'mt3283_external_objects_fix_key_unique_mysql', N'unknown', N'de/communardo/kenmei/database/update/v1.1.3/db.changelog.v1.1.3.xml', CAST(0x00009E8000A80228 AS DateTime), N'c2294436992eb88562f385fe26135b0', N'Drop Primary Key, Add Column', N'CR 135 - Support Synchronization of blog rights with external systems', NULL, N'1.9.2')
INSERT [dbo].[DATABASECHANGELOG] ([ID], [AUTHOR], [FILENAME], [DATEEXECUTED], [MD5SUM], [DESCRIPTION], [COMMENTS], [TAG], [LIQUIBASE]) VALUES (N'mt3292_confluence_permission_url', N'unknown', N'de/communardo/kenmei/database/update/v1.1.3/db.changelog.v1.1.3.xml', CAST(0x00009E8000A80228 AS DateTime), N'21a7986f567bd1c385abbdfead1e8c3', N'Add Column', N'CR 135 - Support Synchronization of blog rights with external systems', NULL, N'1.9.2')
INSERT [dbo].[DATABASECHANGELOG] ([ID], [AUTHOR], [FILENAME], [DATEEXECUTED], [MD5SUM], [DESCRIPTION], [COMMENTS], [TAG], [LIQUIBASE]) VALUES (N'mt3329_1_mysql', N'unknown', N'de/communardo/kenmei/database/update/v1.1.3/db.changelog.v1.1.3.xml', CAST(0x00009E8000A80228 AS DateTime), N'836a30af94fb53cfe359ea11fe4f62fe', N'Create Table, Custom SQL, Add Foreign Key Constraint (x2)', N'CR 96 - support for hierarchical groups: add user_of_group and copy users', NULL, N'1.9.2')
INSERT [dbo].[DATABASECHANGELOG] ([ID], [AUTHOR], [FILENAME], [DATEEXECUTED], [MD5SUM], [DESCRIPTION], [COMMENTS], [TAG], [LIQUIBASE]) VALUES (N'mt3350_configuration_column_ldap', N'unknown', N'de/communardo/kenmei/database/update/v1.1.2/db.changelog.v1.1.2.xml', CAST(0x00009E8000A80228 AS DateTime), N'dd10f32f1dfab6778b0c3eb60acd4ba', N'Drop Column', N'MT 3350', NULL, N'1.9.2')
INSERT [dbo].[DATABASECHANGELOG] ([ID], [AUTHOR], [FILENAME], [DATEEXECUTED], [MD5SUM], [DESCRIPTION], [COMMENTS], [TAG], [LIQUIBASE]) VALUES (N'mt3350_configuration_column_source', N'unknown', N'de/communardo/kenmei/database/update/v1.1.3/db.changelog.v1.1.3.xml', CAST(0x00009E8000A80228 AS DateTime), N'12af7efa26d7a9eefaedfcd1ad9e2d24', N'Drop Column', N'MT 3350', NULL, N'1.9.2')
INSERT [dbo].[DATABASECHANGELOG] ([ID], [AUTHOR], [FILENAME], [DATEEXECUTED], [MD5SUM], [DESCRIPTION], [COMMENTS], [TAG], [LIQUIBASE]) VALUES (N'phone_and_fax_country_code_fix_mysql', N'unknown', N'de/communardo/kenmei/database/update/v1.1.3/db.changelog.v1.1.3.xml', CAST(0x00009E8000A80228 AS DateTime), N'89e69368ec3f98f683218ccbb47340', N'Custom SQL', N'', NULL, N'1.9.2')
INSERT [dbo].[DATABASECHANGELOG] ([ID], [AUTHOR], [FILENAME], [DATEEXECUTED], [MD5SUM], [DESCRIPTION], [COMMENTS], [TAG], [LIQUIBASE]) VALUES (N'phone_country_code_fix__client_mysql', N'unknown', N'de/communardo/kenmei/database/update/v1.1.3/db.changelog.v1.1.3.xml', CAST(0x00009E8000A80228 AS DateTime), N'84e1be9cc8d1aa2f974526d73f3cf9e7', N'Custom SQL', N'', NULL, N'1.9.2')
INSERT [dbo].[DATABASECHANGELOG] ([ID], [AUTHOR], [FILENAME], [DATEEXECUTED], [MD5SUM], [DESCRIPTION], [COMMENTS], [TAG], [LIQUIBASE]) VALUES (N'Remove_QueryHelper', N'Communote', N'de/communardo/kenmei/database/update/v2.4/db.changelog.v2.4.xml', CAST(0x0000A6280120235B AS DateTime), N'c9b4be9fd66455b18d2563688146a26', N'Drop Table', N'', NULL, N'1.9.5')
INSERT [dbo].[DATABASECHANGELOG] ([ID], [AUTHOR], [FILENAME], [DATEEXECUTED], [MD5SUM], [DESCRIPTION], [COMMENTS], [TAG], [LIQUIBASE]) VALUES (N'remove_uk_country_code', N'unknown', N'de/communardo/kenmei/database/update/v1.1.3/db.changelog.v1.1.3.xml', CAST(0x00009E8000A80228 AS DateTime), N'a8a46d136dea83ec4bdfffd11c9b065', N'Custom SQL', N'', NULL, N'1.9.2')
INSERT [dbo].[DATABASECHANGELOG] ([ID], [AUTHOR], [FILENAME], [DATEEXECUTED], [MD5SUM], [DESCRIPTION], [COMMENTS], [TAG], [LIQUIBASE]) VALUES (N'rename_default_blog_alias_1_mysql', N'unknown', N'de/communardo/kenmei/database/update/v1.1/db.changelog.v1.1.xml', CAST(0x00009E8000A80228 AS DateTime), N'aab6b129c4acfe2705da4d8d127dd7b', N'Custom SQL', N'Renames the blog alias of the default blog, if an alias public not exists.', NULL, N'1.9.2')
INSERT [dbo].[DATABASECHANGELOG] ([ID], [AUTHOR], [FILENAME], [DATEEXECUTED], [MD5SUM], [DESCRIPTION], [COMMENTS], [TAG], [LIQUIBASE]) VALUES (N'rename_default_blog_alias_2_mysql', N'unknown', N'de/communardo/kenmei/database/update/v1.1/db.changelog.v1.1.xml', CAST(0x00009E8000A80228 AS DateTime), N'b38717f58c847a3c1896dd7425ef50', N'Custom SQL', N'Renames the blog alias of the default blog, if it is still the message key.', NULL, N'1.9.2')
INSERT [dbo].[DATABASECHANGELOG] ([ID], [AUTHOR], [FILENAME], [DATEEXECUTED], [MD5SUM], [DESCRIPTION], [COMMENTS], [TAG], [LIQUIBASE]) VALUES (N'reset_checksum_001', N'unknown', N'de/communardo/kenmei/database/update/db.changelog.default.xml', CAST(0x00009E8000A80228 AS DateTime), N'6c50cd6ee695161d8f569de01f98956', N'Custom SQL', N'Reset Checksums', NULL, N'1.9.2')
INSERT [dbo].[DATABASECHANGELOG] ([ID], [AUTHOR], [FILENAME], [DATEEXECUTED], [MD5SUM], [DESCRIPTION], [COMMENTS], [TAG], [LIQUIBASE]) VALUES (N'set_configuration', N'unknown', N'de/communardo/kenmei/database/update/db.changelog.final.xml', CAST(0x00009F0300EFE7A0 AS DateTime), N'6148c09f2e75f3239da193221c5cedca', N'Custom SQL', N'Assign the configuration FK', NULL, N'1.9.2')
INSERT [dbo].[DATABASECHANGELOG] ([ID], [AUTHOR], [FILENAME], [DATEEXECUTED], [MD5SUM], [DESCRIPTION], [COMMENTS], [TAG], [LIQUIBASE]) VALUES (N'set_configuration_v2', N'unknown', N'de/communardo/kenmei/database/update/db.changelog.final.xml', CAST(0x0000A62801202789 AS DateTime), N'77ed99ff17bb8b6b7dff62f6eaa738', N'Custom SQL', N'Assign the configuration FK', NULL, N'1.9.5')
INSERT [dbo].[DATABASECHANGELOG] ([ID], [AUTHOR], [FILENAME], [DATEEXECUTED], [MD5SUM], [DESCRIPTION], [COMMENTS], [TAG], [LIQUIBASE]) VALUES (N'Unknown', N'KENMEI-4109-TagClearance_entfernen', N'de/communardo/kenmei/database/update/v2.1/db.changelog.v2.1.1.xml', CAST(0x0000A6280120215C AS DateTime), N'1e29d9d524219933d8377380bdc495', N'Drop Table (x3)', N'', NULL, N'1.9.5')
INSERT [dbo].[DATABASECHANGELOG] ([ID], [AUTHOR], [FILENAME], [DATEEXECUTED], [MD5SUM], [DESCRIPTION], [COMMENTS], [TAG], [LIQUIBASE]) VALUES (N'Update global ids for tags_mssql_v2', N'Communote', N'de/communardo/kenmei/database/update/v2.1/db.changelog.v2.1.1.xml', CAST(0x0000A628012022D4 AS DateTime), N'723379834b288078b9d77bdca02c4b38', N'Custom SQL', N'', NULL, N'1.9.5')
INSERT [dbo].[DATABASECHANGELOG] ([ID], [AUTHOR], [FILENAME], [DATEEXECUTED], [MD5SUM], [DESCRIPTION], [COMMENTS], [TAG], [LIQUIBASE]) VALUES (N'Update_Master_Data_From_Property_Files', N'unknown', N'de/communardo/kenmei/database/update_2nd_pass/v1.1.4/db.changelog.v1.1.4.xml', CAST(0x0000A62801202872 AS DateTime), N'38a37931a03d7a8d2b156f8813123074', N'Custom Change', N'Update the master data stored in DB with entries from property files.', NULL, N'1.9.5')
/****** Object:  Table [dbo].[notification_config]    Script Date: 06/17/2016 17:45:08 ******/
SET ANSI_NULLS ON;
SET QUOTED_IDENTIFIER ON;
CREATE TABLE [dbo].[notification_config](
    [ID] [bigint] IDENTITY(1,1) NOT NULL,
    [fallback] [nvarchar](1024) NULL,
PRIMARY KEY CLUSTERED 
(
    [ID] ASC
)WITH (PAD_INDEX  = OFF, STATISTICS_NORECOMPUTE  = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS  = ON, ALLOW_PAGE_LOCKS  = ON) ON [PRIMARY]
) ON [PRIMARY];
SET IDENTITY_INSERT [dbo].[notification_config] ON
INSERT [dbo].[notification_config] ([ID], [fallback]) VALUES (1, N'mail')
SET IDENTITY_INSERT [dbo].[notification_config] OFF
/****** Object:  Table [dbo].[md_language]    Script Date: 06/17/2016 17:45:08 ******/
SET ANSI_NULLS ON;
SET QUOTED_IDENTIFIER ON;
CREATE TABLE [dbo].[md_language](
    [ID] [bigint] IDENTITY(1,1) NOT NULL,
    [language_code] [nvarchar](255) NOT NULL,
    [name] [nvarchar](1024) NOT NULL,
PRIMARY KEY CLUSTERED 
(
    [ID] ASC
)WITH (PAD_INDEX  = OFF, STATISTICS_NORECOMPUTE  = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS  = ON, ALLOW_PAGE_LOCKS  = ON) ON [PRIMARY],
 CONSTRAINT [md_language_language_code_key] UNIQUE NONCLUSTERED 
(
    [language_code] ASC
)WITH (PAD_INDEX  = OFF, STATISTICS_NORECOMPUTE  = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS  = ON, ALLOW_PAGE_LOCKS  = ON) ON [PRIMARY]
) ON [PRIMARY];
SET IDENTITY_INSERT [dbo].[md_language] ON
INSERT [dbo].[md_language] ([ID], [language_code], [name]) VALUES (1, N'en', N'English')
INSERT [dbo].[md_language] ([ID], [language_code], [name]) VALUES (2, N'de', N'Deutsch')
SET IDENTITY_INSERT [dbo].[md_language] OFF
/****** Object:  Table [dbo].[md_country]    Script Date: 06/17/2016 17:45:08 ******/
SET ANSI_NULLS ON;
SET QUOTED_IDENTIFIER ON;
CREATE TABLE [dbo].[md_country](
    [ID] [bigint] IDENTITY(1,1) NOT NULL,
    [country_code] [nvarchar](255) NOT NULL,
    [name] [nvarchar](1024) NOT NULL,
PRIMARY KEY CLUSTERED 
(
    [ID] ASC
)WITH (PAD_INDEX  = OFF, STATISTICS_NORECOMPUTE  = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS  = ON, ALLOW_PAGE_LOCKS  = ON) ON [PRIMARY],
 CONSTRAINT [md_country_country_code_key] UNIQUE NONCLUSTERED 
(
    [country_code] ASC
)WITH (PAD_INDEX  = OFF, STATISTICS_NORECOMPUTE  = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS  = ON, ALLOW_PAGE_LOCKS  = ON) ON [PRIMARY]
) ON [PRIMARY];
SET IDENTITY_INSERT [dbo].[md_country] ON
INSERT [dbo].[md_country] ([ID], [country_code], [name]) VALUES (1, N'lb', N'Lebanon')
INSERT [dbo].[md_country] ([ID], [country_code], [name]) VALUES (2, N'la', N'Lao People''s Democratic Republic')
INSERT [dbo].[md_country] ([ID], [country_code], [name]) VALUES (3, N'kz', N'Kazakhstan')
INSERT [dbo].[md_country] ([ID], [country_code], [name]) VALUES (4, N'ky', N'Cayman Islands')
INSERT [dbo].[md_country] ([ID], [country_code], [name]) VALUES (5, N'kw', N'Kuwait')
INSERT [dbo].[md_country] ([ID], [country_code], [name]) VALUES (6, N'kr', N'Korea, Republic of')
INSERT [dbo].[md_country] ([ID], [country_code], [name]) VALUES (7, N'kp', N'Korea, Democratic People''s Republic of')
INSERT [dbo].[md_country] ([ID], [country_code], [name]) VALUES (8, N'kn', N'Saint Kitts and Nevis')
INSERT [dbo].[md_country] ([ID], [country_code], [name]) VALUES (9, N'km', N'Comoros')
INSERT [dbo].[md_country] ([ID], [country_code], [name]) VALUES (10, N'ki', N'Kiribati')
INSERT [dbo].[md_country] ([ID], [country_code], [name]) VALUES (11, N'kh', N'Cambodia')
INSERT [dbo].[md_country] ([ID], [country_code], [name]) VALUES (12, N'ws', N'Samoa')
INSERT [dbo].[md_country] ([ID], [country_code], [name]) VALUES (13, N'kg', N'Kyrgyzstan')
INSERT [dbo].[md_country] ([ID], [country_code], [name]) VALUES (14, N'ke', N'Kenya')
INSERT [dbo].[md_country] ([ID], [country_code], [name]) VALUES (15, N'wf', N'Wallis and Futuna')
INSERT [dbo].[md_country] ([ID], [country_code], [name]) VALUES (16, N'jp', N'Japan')
INSERT [dbo].[md_country] ([ID], [country_code], [name]) VALUES (17, N'jo', N'Jordan')
INSERT [dbo].[md_country] ([ID], [country_code], [name]) VALUES (18, N'jm', N'Jamaica')
INSERT [dbo].[md_country] ([ID], [country_code], [name]) VALUES (19, N'vu', N'Vanuatu')
INSERT [dbo].[md_country] ([ID], [country_code], [name]) VALUES (20, N'je', N'Jersey')
INSERT [dbo].[md_country] ([ID], [country_code], [name]) VALUES (21, N'vn', N'Vietnam')
INSERT [dbo].[md_country] ([ID], [country_code], [name]) VALUES (22, N'vi', N'Virgin Islands, U.S.')
INSERT [dbo].[md_country] ([ID], [country_code], [name]) VALUES (23, N'vg', N'Virgin Islands, British')
INSERT [dbo].[md_country] ([ID], [country_code], [name]) VALUES (24, N've', N'Venezuela')
INSERT [dbo].[md_country] ([ID], [country_code], [name]) VALUES (25, N'vc', N'Saint Vincent and the Grenadines')
INSERT [dbo].[md_country] ([ID], [country_code], [name]) VALUES (26, N'va', N'Holy See (Vatican City State)')
INSERT [dbo].[md_country] ([ID], [country_code], [name]) VALUES (27, N'it', N'Italy')
INSERT [dbo].[md_country] ([ID], [country_code], [name]) VALUES (28, N'is', N'Iceland')
INSERT [dbo].[md_country] ([ID], [country_code], [name]) VALUES (29, N'ir', N'Iran, Islamic Republic of')
INSERT [dbo].[md_country] ([ID], [country_code], [name]) VALUES (30, N'iq', N'Iraq')
INSERT [dbo].[md_country] ([ID], [country_code], [name]) VALUES (31, N'io', N'British Indian Ocean Territory')
INSERT [dbo].[md_country] ([ID], [country_code], [name]) VALUES (32, N'uz', N'Uzbekistan')
INSERT [dbo].[md_country] ([ID], [country_code], [name]) VALUES (33, N'in', N'India')
INSERT [dbo].[md_country] ([ID], [country_code], [name]) VALUES (34, N'uy', N'Uruguay')
INSERT [dbo].[md_country] ([ID], [country_code], [name]) VALUES (35, N'im', N'Isle of Man')
INSERT [dbo].[md_country] ([ID], [country_code], [name]) VALUES (36, N'il', N'Israel')
INSERT [dbo].[md_country] ([ID], [country_code], [name]) VALUES (37, N'us', N'United States')
INSERT [dbo].[md_country] ([ID], [country_code], [name]) VALUES (38, N'ie', N'Ireland')
INSERT [dbo].[md_country] ([ID], [country_code], [name]) VALUES (39, N'id', N'Indonesia')
INSERT [dbo].[md_country] ([ID], [country_code], [name]) VALUES (40, N'um', N'United States Minor Outlying Islands')
INSERT [dbo].[md_country] ([ID], [country_code], [name]) VALUES (41, N'ug', N'Uganda')
INSERT [dbo].[md_country] ([ID], [country_code], [name]) VALUES (42, N'hu', N'Hungary')
INSERT [dbo].[md_country] ([ID], [country_code], [name]) VALUES (43, N'ua', N'Ukraine')
INSERT [dbo].[md_country] ([ID], [country_code], [name]) VALUES (44, N'ht', N'Haiti')
INSERT [dbo].[md_country] ([ID], [country_code], [name]) VALUES (45, N'hr', N'Croatia')
INSERT [dbo].[md_country] ([ID], [country_code], [name]) VALUES (46, N'tz', N'Tanzania, United Republic of')
INSERT [dbo].[md_country] ([ID], [country_code], [name]) VALUES (47, N'hn', N'Honduras')
INSERT [dbo].[md_country] ([ID], [country_code], [name]) VALUES (48, N'hm', N'Heard Island and McDonald Islands')
INSERT [dbo].[md_country] ([ID], [country_code], [name]) VALUES (49, N'tw', N'Taiwan, Province of China')
INSERT [dbo].[md_country] ([ID], [country_code], [name]) VALUES (50, N'hk', N'Hong Kong')
INSERT [dbo].[md_country] ([ID], [country_code], [name]) VALUES (51, N'tv', N'Tuvalu')
INSERT [dbo].[md_country] ([ID], [country_code], [name]) VALUES (52, N'tt', N'Trinidad and Tobago')
INSERT [dbo].[md_country] ([ID], [country_code], [name]) VALUES (53, N'tr', N'Turkey')
INSERT [dbo].[md_country] ([ID], [country_code], [name]) VALUES (54, N'to', N'Tonga')
INSERT [dbo].[md_country] ([ID], [country_code], [name]) VALUES (55, N'tn', N'Tunisia')
INSERT [dbo].[md_country] ([ID], [country_code], [name]) VALUES (56, N'tm', N'Turkmenistan')
INSERT [dbo].[md_country] ([ID], [country_code], [name]) VALUES (57, N'tl', N'Timor-Leste')
INSERT [dbo].[md_country] ([ID], [country_code], [name]) VALUES (58, N'tk', N'Tokelau')
INSERT [dbo].[md_country] ([ID], [country_code], [name]) VALUES (59, N'tj', N'Tajikistan')
INSERT [dbo].[md_country] ([ID], [country_code], [name]) VALUES (60, N'th', N'Thailand')
INSERT [dbo].[md_country] ([ID], [country_code], [name]) VALUES (61, N'tg', N'Togo')
INSERT [dbo].[md_country] ([ID], [country_code], [name]) VALUES (62, N'tf', N'French Southern Territories')
INSERT [dbo].[md_country] ([ID], [country_code], [name]) VALUES (63, N'gy', N'Guyana')
INSERT [dbo].[md_country] ([ID], [country_code], [name]) VALUES (64, N'td', N'Chad')
INSERT [dbo].[md_country] ([ID], [country_code], [name]) VALUES (65, N'gw', N'Guinea-bissau')
INSERT [dbo].[md_country] ([ID], [country_code], [name]) VALUES (66, N'tc', N'Turks and Caicos Islands')
INSERT [dbo].[md_country] ([ID], [country_code], [name]) VALUES (67, N'gu', N'Guam')
INSERT [dbo].[md_country] ([ID], [country_code], [name]) VALUES (68, N'gt', N'Guatemala')
INSERT [dbo].[md_country] ([ID], [country_code], [name]) VALUES (69, N'gs', N'South Georgia and the South Sandwich Islands')
INSERT [dbo].[md_country] ([ID], [country_code], [name]) VALUES (70, N'gr', N'Greece')
INSERT [dbo].[md_country] ([ID], [country_code], [name]) VALUES (71, N'gq', N'Equatorial Guinea')
INSERT [dbo].[md_country] ([ID], [country_code], [name]) VALUES (72, N'gp', N'Guadeloupe')
INSERT [dbo].[md_country] ([ID], [country_code], [name]) VALUES (73, N'sz', N'Swaziland')
INSERT [dbo].[md_country] ([ID], [country_code], [name]) VALUES (74, N'gn', N'Guinea')
INSERT [dbo].[md_country] ([ID], [country_code], [name]) VALUES (75, N'sy', N'Syrian Arab Republic')
INSERT [dbo].[md_country] ([ID], [country_code], [name]) VALUES (76, N'gm', N'Gambia')
INSERT [dbo].[md_country] ([ID], [country_code], [name]) VALUES (77, N'gl', N'Greenland')
INSERT [dbo].[md_country] ([ID], [country_code], [name]) VALUES (78, N'sv', N'El Salvador')
INSERT [dbo].[md_country] ([ID], [country_code], [name]) VALUES (79, N'gi', N'Gibraltar')
INSERT [dbo].[md_country] ([ID], [country_code], [name]) VALUES (80, N'st', N'Sao Tome and Principe')
INSERT [dbo].[md_country] ([ID], [country_code], [name]) VALUES (81, N'gh', N'Ghana')
INSERT [dbo].[md_country] ([ID], [country_code], [name]) VALUES (82, N'gg', N'Guernsey')
INSERT [dbo].[md_country] ([ID], [country_code], [name]) VALUES (83, N'sr', N'Suriname')
INSERT [dbo].[md_country] ([ID], [country_code], [name]) VALUES (84, N'gf', N'French Guiana')
INSERT [dbo].[md_country] ([ID], [country_code], [name]) VALUES (85, N'ge', N'Georgia')
INSERT [dbo].[md_country] ([ID], [country_code], [name]) VALUES (86, N'gd', N'Grenada')
INSERT [dbo].[md_country] ([ID], [country_code], [name]) VALUES (87, N'so', N'Somalia')
INSERT [dbo].[md_country] ([ID], [country_code], [name]) VALUES (88, N'sn', N'Senegal')
INSERT [dbo].[md_country] ([ID], [country_code], [name]) VALUES (89, N'gb', N'United Kingdom')
INSERT [dbo].[md_country] ([ID], [country_code], [name]) VALUES (90, N'sm', N'San Marino')
INSERT [dbo].[md_country] ([ID], [country_code], [name]) VALUES (91, N'ga', N'Gabon')
INSERT [dbo].[md_country] ([ID], [country_code], [name]) VALUES (92, N'sl', N'Sierra Leone')
INSERT [dbo].[md_country] ([ID], [country_code], [name]) VALUES (93, N'sk', N'Slovakia')
INSERT [dbo].[md_country] ([ID], [country_code], [name]) VALUES (94, N'sj', N'Svalbard and Jan Mayen')
INSERT [dbo].[md_country] ([ID], [country_code], [name]) VALUES (95, N'si', N'Slovenia')
INSERT [dbo].[md_country] ([ID], [country_code], [name]) VALUES (96, N'sh', N'Saint Helena')
INSERT [dbo].[md_country] ([ID], [country_code], [name]) VALUES (97, N'sg', N'Singapore')
INSERT [dbo].[md_country] ([ID], [country_code], [name]) VALUES (98, N'se', N'Sweden')
INSERT [dbo].[md_country] ([ID], [country_code], [name]) VALUES (99, N'sd', N'Sudan')
INSERT [dbo].[md_country] ([ID], [country_code], [name]) VALUES (100, N'sc', N'Seychelles');
print 'Processed 100 total records'
INSERT [dbo].[md_country] ([ID], [country_code], [name]) VALUES (101, N'sb', N'Solomon Islands')
INSERT [dbo].[md_country] ([ID], [country_code], [name]) VALUES (102, N'sa', N'Saudi Arabia')
INSERT [dbo].[md_country] ([ID], [country_code], [name]) VALUES (103, N'fr', N'France')
INSERT [dbo].[md_country] ([ID], [country_code], [name]) VALUES (104, N'fo', N'Faroe Islands')
INSERT [dbo].[md_country] ([ID], [country_code], [name]) VALUES (105, N'fm', N'Micronesia, Federated States of')
INSERT [dbo].[md_country] ([ID], [country_code], [name]) VALUES (106, N'rw', N'Rwanda')
INSERT [dbo].[md_country] ([ID], [country_code], [name]) VALUES (107, N'fk', N'Falkland Islands (Malvinas)')
INSERT [dbo].[md_country] ([ID], [country_code], [name]) VALUES (108, N'fj', N'Fiji')
INSERT [dbo].[md_country] ([ID], [country_code], [name]) VALUES (109, N'ru', N'Russian Federation')
INSERT [dbo].[md_country] ([ID], [country_code], [name]) VALUES (110, N'fi', N'Finland')
INSERT [dbo].[md_country] ([ID], [country_code], [name]) VALUES (111, N'rs', N'Serbia')
INSERT [dbo].[md_country] ([ID], [country_code], [name]) VALUES (112, N'ro', N'Romania')
INSERT [dbo].[md_country] ([ID], [country_code], [name]) VALUES (113, N're', N'Reunion')
INSERT [dbo].[md_country] ([ID], [country_code], [name]) VALUES (114, N'et', N'Ethiopia')
INSERT [dbo].[md_country] ([ID], [country_code], [name]) VALUES (115, N'es', N'Spain')
INSERT [dbo].[md_country] ([ID], [country_code], [name]) VALUES (116, N'er', N'Eritrea')
INSERT [dbo].[md_country] ([ID], [country_code], [name]) VALUES (117, N'eh', N'Western Sahara')
INSERT [dbo].[md_country] ([ID], [country_code], [name]) VALUES (118, N'eg', N'Egypt')
INSERT [dbo].[md_country] ([ID], [country_code], [name]) VALUES (119, N'ee', N'Estonia')
INSERT [dbo].[md_country] ([ID], [country_code], [name]) VALUES (120, N'ec', N'Ecuador')
INSERT [dbo].[md_country] ([ID], [country_code], [name]) VALUES (121, N'dz', N'Algeria')
INSERT [dbo].[md_country] ([ID], [country_code], [name]) VALUES (122, N'qa', N'Qatar')
INSERT [dbo].[md_country] ([ID], [country_code], [name]) VALUES (123, N'do', N'Dominican Republic')
INSERT [dbo].[md_country] ([ID], [country_code], [name]) VALUES (124, N'py', N'Paraguay')
INSERT [dbo].[md_country] ([ID], [country_code], [name]) VALUES (125, N'dm', N'Dominica')
INSERT [dbo].[md_country] ([ID], [country_code], [name]) VALUES (126, N'pw', N'Palau')
INSERT [dbo].[md_country] ([ID], [country_code], [name]) VALUES (127, N'dk', N'Denmark')
INSERT [dbo].[md_country] ([ID], [country_code], [name]) VALUES (128, N'dj', N'Djibouti')
INSERT [dbo].[md_country] ([ID], [country_code], [name]) VALUES (129, N'pt', N'Portugal')
INSERT [dbo].[md_country] ([ID], [country_code], [name]) VALUES (130, N'ps', N'Palestinian Territory, occupied')
INSERT [dbo].[md_country] ([ID], [country_code], [name]) VALUES (131, N'pr', N'Puerto Rico')
INSERT [dbo].[md_country] ([ID], [country_code], [name]) VALUES (132, N'de', N'Germany')
INSERT [dbo].[md_country] ([ID], [country_code], [name]) VALUES (133, N'pn', N'Pitcairn')
INSERT [dbo].[md_country] ([ID], [country_code], [name]) VALUES (134, N'pm', N'Saint Pierre and Miquelon')
INSERT [dbo].[md_country] ([ID], [country_code], [name]) VALUES (135, N'pl', N'Poland')
INSERT [dbo].[md_country] ([ID], [country_code], [name]) VALUES (136, N'pk', N'Pakistan')
INSERT [dbo].[md_country] ([ID], [country_code], [name]) VALUES (137, N'ph', N'Philippines')
INSERT [dbo].[md_country] ([ID], [country_code], [name]) VALUES (138, N'pg', N'Papua New Guinea')
INSERT [dbo].[md_country] ([ID], [country_code], [name]) VALUES (139, N'cz', N'Czech Republic')
INSERT [dbo].[md_country] ([ID], [country_code], [name]) VALUES (140, N'pf', N'French Polynesia')
INSERT [dbo].[md_country] ([ID], [country_code], [name]) VALUES (141, N'cy', N'Cyprus')
INSERT [dbo].[md_country] ([ID], [country_code], [name]) VALUES (142, N'pe', N'Peru')
INSERT [dbo].[md_country] ([ID], [country_code], [name]) VALUES (143, N'cx', N'Christmas Island')
INSERT [dbo].[md_country] ([ID], [country_code], [name]) VALUES (144, N'cv', N'Cape Verde')
INSERT [dbo].[md_country] ([ID], [country_code], [name]) VALUES (145, N'cu', N'Cuba')
INSERT [dbo].[md_country] ([ID], [country_code], [name]) VALUES (146, N'pa', N'Panama')
INSERT [dbo].[md_country] ([ID], [country_code], [name]) VALUES (147, N'cr', N'Costa Rica')
INSERT [dbo].[md_country] ([ID], [country_code], [name]) VALUES (148, N'co', N'Colombia')
INSERT [dbo].[md_country] ([ID], [country_code], [name]) VALUES (149, N'cn', N'China')
INSERT [dbo].[md_country] ([ID], [country_code], [name]) VALUES (150, N'cm', N'Cameroon')
INSERT [dbo].[md_country] ([ID], [country_code], [name]) VALUES (151, N'cl', N'Chile')
INSERT [dbo].[md_country] ([ID], [country_code], [name]) VALUES (152, N'ck', N'Cook Islands')
INSERT [dbo].[md_country] ([ID], [country_code], [name]) VALUES (153, N'ci', N'Côte D''Ivoire')
INSERT [dbo].[md_country] ([ID], [country_code], [name]) VALUES (154, N'ch', N'Switzerland')
INSERT [dbo].[md_country] ([ID], [country_code], [name]) VALUES (155, N'cg', N'Congo')
INSERT [dbo].[md_country] ([ID], [country_code], [name]) VALUES (156, N'cf', N'Central African Republic')
INSERT [dbo].[md_country] ([ID], [country_code], [name]) VALUES (157, N'cd', N'Congo, the Democratic Republic of the')
INSERT [dbo].[md_country] ([ID], [country_code], [name]) VALUES (158, N'cc', N'Cocos (Keeling) Islands')
INSERT [dbo].[md_country] ([ID], [country_code], [name]) VALUES (159, N'om', N'Oman')
INSERT [dbo].[md_country] ([ID], [country_code], [name]) VALUES (160, N'ca', N'Canada')
INSERT [dbo].[md_country] ([ID], [country_code], [name]) VALUES (161, N'bz', N'Belize')
INSERT [dbo].[md_country] ([ID], [country_code], [name]) VALUES (162, N'by', N'Belarus')
INSERT [dbo].[md_country] ([ID], [country_code], [name]) VALUES (163, N'bw', N'Botswana')
INSERT [dbo].[md_country] ([ID], [country_code], [name]) VALUES (164, N'bv', N'Bouvet Island')
INSERT [dbo].[md_country] ([ID], [country_code], [name]) VALUES (165, N'bt', N'Bhutan')
INSERT [dbo].[md_country] ([ID], [country_code], [name]) VALUES (166, N'bs', N'Bahamas')
INSERT [dbo].[md_country] ([ID], [country_code], [name]) VALUES (167, N'br', N'Brazil')
INSERT [dbo].[md_country] ([ID], [country_code], [name]) VALUES (168, N'bo', N'Bolivia')
INSERT [dbo].[md_country] ([ID], [country_code], [name]) VALUES (169, N'nz', N'New Zealand')
INSERT [dbo].[md_country] ([ID], [country_code], [name]) VALUES (170, N'bn', N'Brunei Darussalam')
INSERT [dbo].[md_country] ([ID], [country_code], [name]) VALUES (171, N'bm', N'Bermuda')
INSERT [dbo].[md_country] ([ID], [country_code], [name]) VALUES (172, N'bl', N'Saint Barthélemy')
INSERT [dbo].[md_country] ([ID], [country_code], [name]) VALUES (173, N'bj', N'Benin')
INSERT [dbo].[md_country] ([ID], [country_code], [name]) VALUES (174, N'nu', N'Niue')
INSERT [dbo].[md_country] ([ID], [country_code], [name]) VALUES (175, N'bi', N'Burundi')
INSERT [dbo].[md_country] ([ID], [country_code], [name]) VALUES (176, N'bh', N'Bahrain')
INSERT [dbo].[md_country] ([ID], [country_code], [name]) VALUES (177, N'bg', N'Bulgaria')
INSERT [dbo].[md_country] ([ID], [country_code], [name]) VALUES (178, N'nr', N'Nauru')
INSERT [dbo].[md_country] ([ID], [country_code], [name]) VALUES (179, N'bf', N'Burkina Faso')
INSERT [dbo].[md_country] ([ID], [country_code], [name]) VALUES (180, N'be', N'Belgium')
INSERT [dbo].[md_country] ([ID], [country_code], [name]) VALUES (181, N'np', N'Nepal')
INSERT [dbo].[md_country] ([ID], [country_code], [name]) VALUES (182, N'bd', N'Bangladesh')
INSERT [dbo].[md_country] ([ID], [country_code], [name]) VALUES (183, N'no', N'Norway')
INSERT [dbo].[md_country] ([ID], [country_code], [name]) VALUES (184, N'bb', N'Barbados')
INSERT [dbo].[md_country] ([ID], [country_code], [name]) VALUES (185, N'ba', N'Bosnia and Herzegovina')
INSERT [dbo].[md_country] ([ID], [country_code], [name]) VALUES (186, N'nl', N'Netherlands')
INSERT [dbo].[md_country] ([ID], [country_code], [name]) VALUES (187, N'zw', N'Zimbabwe')
INSERT [dbo].[md_country] ([ID], [country_code], [name]) VALUES (188, N'ni', N'Nicaragua')
INSERT [dbo].[md_country] ([ID], [country_code], [name]) VALUES (189, N'ng', N'Nigeria')
INSERT [dbo].[md_country] ([ID], [country_code], [name]) VALUES (190, N'az', N'Azerbaijan')
INSERT [dbo].[md_country] ([ID], [country_code], [name]) VALUES (191, N'nf', N'Norfolk Island')
INSERT [dbo].[md_country] ([ID], [country_code], [name]) VALUES (192, N'ne', N'Niger')
INSERT [dbo].[md_country] ([ID], [country_code], [name]) VALUES (193, N'ax', N'Åland Islands')
INSERT [dbo].[md_country] ([ID], [country_code], [name]) VALUES (194, N'aw', N'Aruba')
INSERT [dbo].[md_country] ([ID], [country_code], [name]) VALUES (195, N'nc', N'New Caledonia')
INSERT [dbo].[md_country] ([ID], [country_code], [name]) VALUES (196, N'zm', N'Zambia')
INSERT [dbo].[md_country] ([ID], [country_code], [name]) VALUES (197, N'au', N'Australia')
INSERT [dbo].[md_country] ([ID], [country_code], [name]) VALUES (198, N'na', N'Namibia')
INSERT [dbo].[md_country] ([ID], [country_code], [name]) VALUES (199, N'at', N'Austria')
INSERT [dbo].[md_country] ([ID], [country_code], [name]) VALUES (200, N'as', N'American Samoa')
INSERT [dbo].[md_country] ([ID], [country_code], [name]) VALUES (201, N'ar', N'Argentina');
print 'Processed 200 total records'
INSERT [dbo].[md_country] ([ID], [country_code], [name]) VALUES (202, N'aq', N'Antarctica')
INSERT [dbo].[md_country] ([ID], [country_code], [name]) VALUES (203, N'ao', N'Angola')
INSERT [dbo].[md_country] ([ID], [country_code], [name]) VALUES (204, N'mz', N'Mozambique')
INSERT [dbo].[md_country] ([ID], [country_code], [name]) VALUES (205, N'an', N'Netherlands Antilles')
INSERT [dbo].[md_country] ([ID], [country_code], [name]) VALUES (206, N'my', N'Malaysia')
INSERT [dbo].[md_country] ([ID], [country_code], [name]) VALUES (207, N'am', N'Armenia')
INSERT [dbo].[md_country] ([ID], [country_code], [name]) VALUES (208, N'mx', N'Mexico')
INSERT [dbo].[md_country] ([ID], [country_code], [name]) VALUES (209, N'al', N'Albania')
INSERT [dbo].[md_country] ([ID], [country_code], [name]) VALUES (210, N'mw', N'Malawi')
INSERT [dbo].[md_country] ([ID], [country_code], [name]) VALUES (211, N'mv', N'Maldives')
INSERT [dbo].[md_country] ([ID], [country_code], [name]) VALUES (212, N'mu', N'Mauritius')
INSERT [dbo].[md_country] ([ID], [country_code], [name]) VALUES (213, N'za', N'South Africa')
INSERT [dbo].[md_country] ([ID], [country_code], [name]) VALUES (214, N'ai', N'Anguilla')
INSERT [dbo].[md_country] ([ID], [country_code], [name]) VALUES (215, N'mt', N'Malta')
INSERT [dbo].[md_country] ([ID], [country_code], [name]) VALUES (216, N'ms', N'Montserrat')
INSERT [dbo].[md_country] ([ID], [country_code], [name]) VALUES (217, N'ag', N'Antigua and Barbuda')
INSERT [dbo].[md_country] ([ID], [country_code], [name]) VALUES (218, N'mr', N'Mauritania')
INSERT [dbo].[md_country] ([ID], [country_code], [name]) VALUES (219, N'af', N'Afghanistan')
INSERT [dbo].[md_country] ([ID], [country_code], [name]) VALUES (220, N'mq', N'Martinique')
INSERT [dbo].[md_country] ([ID], [country_code], [name]) VALUES (221, N'ae', N'United Arab Emirates')
INSERT [dbo].[md_country] ([ID], [country_code], [name]) VALUES (222, N'mp', N'Northern Mariana Islands')
INSERT [dbo].[md_country] ([ID], [country_code], [name]) VALUES (223, N'ad', N'Andorra')
INSERT [dbo].[md_country] ([ID], [country_code], [name]) VALUES (224, N'mo', N'Macao')
INSERT [dbo].[md_country] ([ID], [country_code], [name]) VALUES (225, N'mn', N'Mongolia')
INSERT [dbo].[md_country] ([ID], [country_code], [name]) VALUES (226, N'mm', N'Myanmar')
INSERT [dbo].[md_country] ([ID], [country_code], [name]) VALUES (227, N'ml', N'Mali')
INSERT [dbo].[md_country] ([ID], [country_code], [name]) VALUES (228, N'mk', N'Macedonia, the Former Yugoslav Republic of')
INSERT [dbo].[md_country] ([ID], [country_code], [name]) VALUES (229, N'yt', N'Mayotte')
INSERT [dbo].[md_country] ([ID], [country_code], [name]) VALUES (230, N'mh', N'Marshall Islands')
INSERT [dbo].[md_country] ([ID], [country_code], [name]) VALUES (231, N'mg', N'Madagascar')
INSERT [dbo].[md_country] ([ID], [country_code], [name]) VALUES (232, N'mf', N'Saint Martin')
INSERT [dbo].[md_country] ([ID], [country_code], [name]) VALUES (233, N'me', N'Montenegro')
INSERT [dbo].[md_country] ([ID], [country_code], [name]) VALUES (234, N'md', N'Moldova, Republic of')
INSERT [dbo].[md_country] ([ID], [country_code], [name]) VALUES (235, N'mc', N'Monaco')
INSERT [dbo].[md_country] ([ID], [country_code], [name]) VALUES (236, N'ma', N'Morocco')
INSERT [dbo].[md_country] ([ID], [country_code], [name]) VALUES (237, N'ly', N'Libyan Arab Jamahiriya')
INSERT [dbo].[md_country] ([ID], [country_code], [name]) VALUES (238, N'ye', N'Yemen')
INSERT [dbo].[md_country] ([ID], [country_code], [name]) VALUES (239, N'lv', N'Latvia')
INSERT [dbo].[md_country] ([ID], [country_code], [name]) VALUES (240, N'lu', N'Luxembourg')
INSERT [dbo].[md_country] ([ID], [country_code], [name]) VALUES (241, N'lt', N'Lithuania')
INSERT [dbo].[md_country] ([ID], [country_code], [name]) VALUES (242, N'ls', N'Lesotho')
INSERT [dbo].[md_country] ([ID], [country_code], [name]) VALUES (243, N'lr', N'Liberia')
INSERT [dbo].[md_country] ([ID], [country_code], [name]) VALUES (244, N'lk', N'Sri Lanka')
INSERT [dbo].[md_country] ([ID], [country_code], [name]) VALUES (245, N'li', N'Liechtenstein')
INSERT [dbo].[md_country] ([ID], [country_code], [name]) VALUES (246, N'lc', N'Saint Lucia')
SET IDENTITY_INSERT [dbo].[md_country] OFF
/****** Object:  Table [dbo].[user_client_statistic]    Script Date: 06/17/2016 17:45:08 ******/
SET ANSI_NULLS ON;
SET QUOTED_IDENTIFIER ON;
CREATE TABLE [dbo].[user_client_statistic](
    [ID] [bigint] IDENTITY(1,1) NOT NULL,
    [repository_size] [bigint] NOT NULL,
PRIMARY KEY CLUSTERED 
(
    [ID] ASC
)WITH (PAD_INDEX  = OFF, STATISTICS_NORECOMPUTE  = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS  = ON, ALLOW_PAGE_LOCKS  = ON) ON [PRIMARY]
) ON [PRIMARY];
SET IDENTITY_INSERT [dbo].[user_client_statistic] ON
INSERT [dbo].[user_client_statistic] ([ID], [repository_size]) VALUES (1, 0)
SET IDENTITY_INSERT [dbo].[user_client_statistic] OFF
/****** Object:  Table [dbo].[user_client]    Script Date: 06/17/2016 17:45:08 ******/
SET ANSI_NULLS ON;
SET QUOTED_IDENTIFIER ON;
CREATE TABLE [dbo].[user_client](
    [ID] [bigint] IDENTITY(1,1) NOT NULL,
    [client_id] [nvarchar](255) NOT NULL,
    [name] [nvarchar](1024) NOT NULL,
    [client_status] [nvarchar](1024) NOT NULL,
    [creation_version] [nvarchar](1024) NULL,
    [creation_time] [datetime2](7) NULL,
    [creation_revision] [varchar](1024) NULL,
PRIMARY KEY CLUSTERED 
(
    [ID] ASC
)WITH (PAD_INDEX  = OFF, STATISTICS_NORECOMPUTE  = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS  = ON, ALLOW_PAGE_LOCKS  = ON) ON [PRIMARY]
) ON [PRIMARY];
CREATE UNIQUE NONCLUSTERED INDEX [user_client_client_id_key] ON [dbo].[user_client] 
(
    [client_id] ASC
)
WHERE ([client_id] IS NOT NULL)
WITH (PAD_INDEX  = OFF, STATISTICS_NORECOMPUTE  = OFF, SORT_IN_TEMPDB = OFF, IGNORE_DUP_KEY = OFF, DROP_EXISTING = OFF, ONLINE = OFF, ALLOW_ROW_LOCKS  = ON, ALLOW_PAGE_LOCKS  = ON) ON [PRIMARY];
SET IDENTITY_INSERT [dbo].[user_client] ON
INSERT [dbo].[user_client] ([ID], [client_id], [name], [client_status], [creation_version], [creation_time], [creation_revision]) VALUES (1, N'global', N'Global Test Client', N'ACTIVE', N'3.4.5250bf0', CAST(0x078020879092833B0B AS DateTime2), N'5250bf0')
SET IDENTITY_INSERT [dbo].[user_client] OFF
/****** Object:  Table [dbo].[user_entity]    Script Date: 06/17/2016 17:45:08 ******/
SET ANSI_NULLS ON;
SET QUOTED_IDENTIFIER ON;
CREATE TABLE [dbo].[user_entity](
    [ID] [bigint] IDENTITY(1,1) NOT NULL,
    [GLOBAL_ID_FK] [bigint] NULL,
PRIMARY KEY CLUSTERED 
(
    [ID] ASC
)WITH (PAD_INDEX  = OFF, STATISTICS_NORECOMPUTE  = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS  = ON, ALLOW_PAGE_LOCKS  = ON) ON [PRIMARY]
) ON [PRIMARY];
CREATE UNIQUE NONCLUSTERED INDEX [user_entity_global_id_fk_key] ON [dbo].[user_entity] 
(
    [GLOBAL_ID_FK] ASC
)
WHERE ([GLOBAL_ID_FK] IS NOT NULL)
WITH (PAD_INDEX  = OFF, STATISTICS_NORECOMPUTE  = OFF, SORT_IN_TEMPDB = OFF, IGNORE_DUP_KEY = OFF, DROP_EXISTING = OFF, ONLINE = OFF, ALLOW_ROW_LOCKS  = ON, ALLOW_PAGE_LOCKS  = ON) ON [PRIMARY];
SET IDENTITY_INSERT [dbo].[user_entity] ON
INSERT [dbo].[user_entity] ([ID], [GLOBAL_ID_FK]) VALUES (1, 1)
SET IDENTITY_INSERT [dbo].[user_entity] OFF
/****** Object:  Table [dbo].[user_contact]    Script Date: 06/17/2016 17:45:08 ******/
SET ANSI_NULLS ON;
SET QUOTED_IDENTIFIER ON;
CREATE TABLE [dbo].[user_contact](
    [ID] [bigint] IDENTITY(1,1) NOT NULL,
    [street] [nvarchar](1024) NULL,
    [zip] [nvarchar](1024) NULL,
    [city] [nvarchar](1024) NULL,
    [phone_country_code] [nvarchar](1024) NULL,
    [phone_area_code] [nvarchar](1024) NULL,
    [phone_phone_number] [nvarchar](1024) NULL,
    [fax_country_code] [nvarchar](1024) NULL,
    [fax_area_code] [nvarchar](1024) NULL,
    [fax_phone_number] [nvarchar](1024) NULL,
    [COUNTRY_FK] [bigint] NULL,
PRIMARY KEY CLUSTERED 
(
    [ID] ASC
)WITH (PAD_INDEX  = OFF, STATISTICS_NORECOMPUTE  = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS  = ON, ALLOW_PAGE_LOCKS  = ON) ON [PRIMARY]
) ON [PRIMARY];
/****** Object:  Table [dbo].[mc_config]    Script Date: 06/17/2016 17:45:08 ******/
SET ANSI_NULLS ON;
SET QUOTED_IDENTIFIER ON;
CREATE TABLE [dbo].[mc_config](
    [ID] [bigint] IDENTITY(1,1) NOT NULL,
    [type] [nvarchar](1024) NOT NULL,
    [properties] [nvarchar](1024) NULL,
    [only_if_available] [bit] NOT NULL,
    [priority] [int] NOT NULL,
    [NOTIFICATION_CONFIG_FK] [bigint] NULL,
PRIMARY KEY CLUSTERED 
(
    [ID] ASC
)WITH (PAD_INDEX  = OFF, STATISTICS_NORECOMPUTE  = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS  = ON, ALLOW_PAGE_LOCKS  = ON) ON [PRIMARY]
) ON [PRIMARY];
SET IDENTITY_INSERT [dbo].[mc_config] ON
INSERT [dbo].[mc_config] ([ID], [type], [properties], [only_if_available], [priority], [NOTIFICATION_CONFIG_FK]) VALUES (1, N'mail', NULL, 0, 1, 1)
SET IDENTITY_INSERT [dbo].[mc_config] OFF
/****** Object:  Table [dbo].[iprange_range]    Script Date: 06/17/2016 17:45:08 ******/
SET ANSI_NULLS ON;
SET QUOTED_IDENTIFIER ON;
CREATE TABLE [dbo].[iprange_range](
    [ID] [bigint] IDENTITY(1,1) NOT NULL,
    [start_value] [varbinary](32) NOT NULL,
    [end_value] [varbinary](32) NOT NULL,
    [string_representation] [nvarchar](1024) NULL,
    [IP_RANGE_FILTER_IN_FK] [bigint] NULL,
    [IP_RANGE_FILTER_EX_FK] [bigint] NULL,
PRIMARY KEY CLUSTERED 
(
    [ID] ASC
)WITH (PAD_INDEX  = OFF, STATISTICS_NORECOMPUTE  = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS  = ON, ALLOW_PAGE_LOCKS  = ON) ON [PRIMARY]
) ON [PRIMARY];
/****** Object:  Table [dbo].[iprange_filter_channel]    Script Date: 06/17/2016 17:45:08 ******/
SET ANSI_NULLS ON;
SET QUOTED_IDENTIFIER ON;
CREATE TABLE [dbo].[iprange_filter_channel](
    [IP_RANGE_FILTERS_FK] [bigint] NOT NULL,
    [CHANNELS_FK] [nvarchar](255) NOT NULL,
 CONSTRAINT [range_filters_channels] PRIMARY KEY CLUSTERED 
(
    [IP_RANGE_FILTERS_FK] ASC,
    [CHANNELS_FK] ASC
)WITH (PAD_INDEX  = OFF, STATISTICS_NORECOMPUTE  = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS  = ON, ALLOW_PAGE_LOCKS  = ON) ON [PRIMARY]
) ON [PRIMARY];
/****** Object:  Table [dbo].[custom_messages]    Script Date: 06/17/2016 17:45:08 ******/
SET ANSI_NULLS ON;
SET QUOTED_IDENTIFIER ON;
CREATE TABLE [dbo].[custom_messages](
    [ID] [bigint] IDENTITY(1,1) NOT NULL,
    [message_key] [nvarchar](255) NOT NULL,
    [message] [nvarchar](max) NOT NULL,
    [is_html] [bit] NOT NULL,
    [LANGUAGE_FK] [bigint] NULL,
PRIMARY KEY CLUSTERED 
(
    [ID] ASC
)WITH (PAD_INDEX  = OFF, STATISTICS_NORECOMPUTE  = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS  = ON, ALLOW_PAGE_LOCKS  = ON) ON [PRIMARY],
 CONSTRAINT [key_language_constraint] UNIQUE NONCLUSTERED 
(
    [message_key] ASC,
    [LANGUAGE_FK] ASC
)WITH (PAD_INDEX  = OFF, STATISTICS_NORECOMPUTE  = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS  = ON, ALLOW_PAGE_LOCKS  = ON) ON [PRIMARY]
) ON [PRIMARY] TEXTIMAGE_ON [PRIMARY];
/****** Object:  Table [dbo].[core_task_props]    Script Date: 06/17/2016 17:45:08 ******/
SET ANSI_NULLS ON;
SET QUOTED_IDENTIFIER ON;
CREATE TABLE [dbo].[core_task_props](
    [ID] [bigint] IDENTITY(1,1) NOT NULL,
    [property_key] [nvarchar](1024) NOT NULL,
    [property_value] [nvarchar](1024) NOT NULL,
    [TASK_FK] [bigint] NULL,
 CONSTRAINT [pk_core_task_props] PRIMARY KEY CLUSTERED 
(
    [ID] ASC
)WITH (PAD_INDEX  = OFF, STATISTICS_NORECOMPUTE  = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS  = ON, ALLOW_PAGE_LOCKS  = ON) ON [PRIMARY]
) ON [PRIMARY];
CREATE NONCLUSTERED INDEX [core_task_props_task_fk_idx] ON [dbo].[core_task_props] 
(
    [TASK_FK] ASC
)WITH (PAD_INDEX  = OFF, STATISTICS_NORECOMPUTE  = OFF, SORT_IN_TEMPDB = OFF, IGNORE_DUP_KEY = OFF, DROP_EXISTING = OFF, ONLINE = OFF, ALLOW_ROW_LOCKS  = ON, ALLOW_PAGE_LOCKS  = ON) ON [PRIMARY];
/****** Object:  Table [dbo].[core_task_execs]    Script Date: 06/17/2016 17:45:08 ******/
SET ANSI_NULLS ON;
SET QUOTED_IDENTIFIER ON;
CREATE TABLE [dbo].[core_task_execs](
    [ID] [bigint] IDENTITY(1,1) NOT NULL,
    [instance_name] [nvarchar](1024) NOT NULL,
    [TASK_FK] [bigint] NOT NULL,
 CONSTRAINT [pk_core_task_execs] PRIMARY KEY CLUSTERED 
(
    [ID] ASC
)WITH (PAD_INDEX  = OFF, STATISTICS_NORECOMPUTE  = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS  = ON, ALLOW_PAGE_LOCKS  = ON) ON [PRIMARY],
 CONSTRAINT [core_task_execs_task_fk_key] UNIQUE NONCLUSTERED 
(
    [TASK_FK] ASC
)WITH (PAD_INDEX  = OFF, STATISTICS_NORECOMPUTE  = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS  = ON, ALLOW_PAGE_LOCKS  = ON) ON [PRIMARY]
) ON [PRIMARY];
/****** Object:  Table [dbo].[core_tag]    Script Date: 06/17/2016 17:45:08 ******/
SET ANSI_NULLS ON;
SET QUOTED_IDENTIFIER ON;
CREATE TABLE [dbo].[core_tag](
    [ID] [bigint] IDENTITY(1,1) NOT NULL,
    [class] [nvarchar](255) NOT NULL,
    [tag_store_tag_id] [nvarchar](255) NOT NULL,
    [default_name] [nvarchar](255) NOT NULL,
    [GLOBAL_ID_FK] [bigint] NULL,
    [CATEGORY_FK] [bigint] NULL,
    [ABSTRACT_TAG_CATEGORY_TAGS_IDX] [int] NULL,
    [tag_store_alias] [nvarchar](255) NULL,
PRIMARY KEY CLUSTERED 
(
    [ID] ASC
)WITH (PAD_INDEX  = OFF, STATISTICS_NORECOMPUTE  = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS  = ON, ALLOW_PAGE_LOCKS  = ON) ON [PRIMARY],
 CONSTRAINT [core_tag_store_idx] UNIQUE NONCLUSTERED 
(
    [tag_store_tag_id] ASC,
    [tag_store_alias] ASC
)WITH (PAD_INDEX  = OFF, STATISTICS_NORECOMPUTE  = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS  = ON, ALLOW_PAGE_LOCKS  = ON) ON [PRIMARY]
) ON [PRIMARY];
CREATE UNIQUE NONCLUSTERED INDEX [core_tag_global_id_fk_key] ON [dbo].[core_tag] 
(
    [GLOBAL_ID_FK] ASC
)
WHERE ([GLOBAL_ID_FK] IS NOT NULL)
WITH (PAD_INDEX  = OFF, STATISTICS_NORECOMPUTE  = OFF, SORT_IN_TEMPDB = OFF, IGNORE_DUP_KEY = OFF, DROP_EXISTING = OFF, ONLINE = OFF, ALLOW_ROW_LOCKS  = ON, ALLOW_PAGE_LOCKS  = ON) ON [PRIMARY];
CREATE NONCLUSTERED INDEX [core_tag_id_asc_idx] ON [dbo].[core_tag] 
(
    [ID] ASC
)WITH (PAD_INDEX  = OFF, STATISTICS_NORECOMPUTE  = OFF, SORT_IN_TEMPDB = OFF, IGNORE_DUP_KEY = OFF, DROP_EXISTING = OFF, ONLINE = OFF, ALLOW_ROW_LOCKS  = ON, ALLOW_PAGE_LOCKS  = ON) ON [PRIMARY];
CREATE NONCLUSTERED INDEX [core_tag_lower_name_idx] ON [dbo].[core_tag] 
(
    [tag_store_tag_id] ASC
)WITH (PAD_INDEX  = OFF, STATISTICS_NORECOMPUTE  = OFF, SORT_IN_TEMPDB = OFF, IGNORE_DUP_KEY = OFF, DROP_EXISTING = OFF, ONLINE = OFF, ALLOW_ROW_LOCKS  = ON, ALLOW_PAGE_LOCKS  = ON) ON [PRIMARY];
CREATE NONCLUSTERED INDEX [tag_store_asc_idx] ON [dbo].[core_tag] 
(
    [tag_store_tag_id] ASC,
    [tag_store_alias] ASC
)WITH (PAD_INDEX  = OFF, STATISTICS_NORECOMPUTE  = OFF, SORT_IN_TEMPDB = OFF, IGNORE_DUP_KEY = OFF, DROP_EXISTING = OFF, ONLINE = OFF, ALLOW_ROW_LOCKS  = ON, ALLOW_PAGE_LOCKS  = ON) ON [PRIMARY];
SET IDENTITY_INSERT [dbo].[core_tag] ON
INSERT [dbo].[core_tag] ([ID], [class], [tag_store_tag_id], [default_name], [GLOBAL_ID_FK], [CATEGORY_FK], [ABSTRACT_TAG_CATEGORY_TAGS_IDX], [tag_store_alias]) VALUES (1, N'TagImpl', N'default', N'default', 2, NULL, NULL, N'DefaultBlogTagStore')
SET IDENTITY_INSERT [dbo].[core_tag] OFF
/****** Object:  Table [dbo].[configuration_ldap_sbase]    Script Date: 06/17/2016 17:45:08 ******/
SET ANSI_NULLS ON;
SET QUOTED_IDENTIFIER ON;
CREATE TABLE [dbo].[configuration_ldap_sbase](
    [ID] [bigint] IDENTITY(1,1) NOT NULL,
    [search_base] [nvarchar](1024) NOT NULL,
    [search_subtree] [bit] NOT NULL,
    [LDAP_SEARCH_CONFIGURATION_FK] [bigint] NULL,
    [sbase_idx] [int] NULL,
PRIMARY KEY CLUSTERED 
(
    [ID] ASC
)WITH (PAD_INDEX  = OFF, STATISTICS_NORECOMPUTE  = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS  = ON, ALLOW_PAGE_LOCKS  = ON) ON [PRIMARY]
) ON [PRIMARY];
/****** Object:  Table [dbo].[configuration_ldap_group]    Script Date: 06/17/2016 17:45:08 ******/
SET ANSI_NULLS ON;
SET QUOTED_IDENTIFIER ON;
CREATE TABLE [dbo].[configuration_ldap_group](
    [ID] [bigint] IDENTITY(1,1) NOT NULL,
    [member_mode] [bit] NOT NULL,
    [group_identifier_is_binary] [bit] NOT NULL,
    [GROUP_SEARCH_FK] [bigint] NOT NULL,
PRIMARY KEY CLUSTERED 
(
    [ID] ASC
)WITH (PAD_INDEX  = OFF, STATISTICS_NORECOMPUTE  = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS  = ON, ALLOW_PAGE_LOCKS  = ON) ON [PRIMARY],
 CONSTRAINT [configuration_ldap_group_group_search_fk_key] UNIQUE NONCLUSTERED 
(
    [GROUP_SEARCH_FK] ASC
)WITH (PAD_INDEX  = OFF, STATISTICS_NORECOMPUTE  = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS  = ON, ALLOW_PAGE_LOCKS  = ON) ON [PRIMARY]
) ON [PRIMARY];
/****** Object:  Table [dbo].[core_blog]    Script Date: 06/17/2016 17:45:08 ******/
SET ANSI_NULLS ON;
SET QUOTED_IDENTIFIER ON;
CREATE TABLE [dbo].[core_blog](
    [ID] [bigint] IDENTITY(1,1) NOT NULL,
    [title] [nvarchar](450) NOT NULL,
    [description] [nvarchar](max) NULL,
    [creation_date] [datetime2](7) NULL,
    [name_identifier] [nvarchar](255) NOT NULL,
    [last_modification_date] [datetime2](7) NULL,
    [all_can_read] [bit] NOT NULL,
    [all_can_write] [bit] NOT NULL,
    [public_access] [bit] NOT NULL,
    [create_system_notes] [bit] NOT NULL,
    [GLOBAL_ID_FK] [bigint] NULL,
    [toplevel_topic] [bit] NULL,
    [crawl_last_modification_date] [datetime2](7) NULL,
PRIMARY KEY CLUSTERED 
(
    [ID] ASC
)WITH (PAD_INDEX  = OFF, STATISTICS_NORECOMPUTE  = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS  = ON, ALLOW_PAGE_LOCKS  = ON) ON [PRIMARY],
 CONSTRAINT [core_blog_name_identifier_key] UNIQUE NONCLUSTERED 
(
    [name_identifier] ASC
)WITH (PAD_INDEX  = OFF, STATISTICS_NORECOMPUTE  = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS  = ON, ALLOW_PAGE_LOCKS  = ON) ON [PRIMARY]
) ON [PRIMARY] TEXTIMAGE_ON [PRIMARY];
CREATE UNIQUE NONCLUSTERED INDEX [core_blog_global_id_fk_key] ON [dbo].[core_blog] 
(
    [GLOBAL_ID_FK] ASC
)
WHERE ([GLOBAL_ID_FK] IS NOT NULL)
WITH (PAD_INDEX  = OFF, STATISTICS_NORECOMPUTE  = OFF, SORT_IN_TEMPDB = OFF, IGNORE_DUP_KEY = OFF, DROP_EXISTING = OFF, ONLINE = OFF, ALLOW_ROW_LOCKS  = ON, ALLOW_PAGE_LOCKS  = ON) ON [PRIMARY];
CREATE NONCLUSTERED INDEX [core_blog_name_identifier_index] ON [dbo].[core_blog] 
(
    [name_identifier] ASC
)WITH (PAD_INDEX  = OFF, STATISTICS_NORECOMPUTE  = OFF, SORT_IN_TEMPDB = OFF, IGNORE_DUP_KEY = OFF, DROP_EXISTING = OFF, ONLINE = OFF, ALLOW_ROW_LOCKS  = ON, ALLOW_PAGE_LOCKS  = ON) ON [PRIMARY];
CREATE NONCLUSTERED INDEX [core_blog_title_index] ON [dbo].[core_blog] 
(
    [title] ASC
)WITH (PAD_INDEX  = OFF, STATISTICS_NORECOMPUTE  = OFF, SORT_IN_TEMPDB = OFF, IGNORE_DUP_KEY = OFF, DROP_EXISTING = OFF, ONLINE = OFF, ALLOW_ROW_LOCKS  = ON, ALLOW_PAGE_LOCKS  = ON) ON [PRIMARY];
SET IDENTITY_INSERT [dbo].[core_blog] ON
INSERT [dbo].[core_blog] ([ID], [title], [description], [creation_date], [name_identifier], [last_modification_date], [all_can_read], [all_can_write], [public_access], [create_system_notes], [GLOBAL_ID_FK], [toplevel_topic], [crawl_last_modification_date]) VALUES (1, N'Global Test Client', NULL, CAST(0x07F0780E9192833B0B AS DateTime2), N'default', CAST(0x07F0780E9192833B0B AS DateTime2), 1, 1, 0, 0, 3, 0, CAST(0x0750981B9192833B0B AS DateTime2))
SET IDENTITY_INSERT [dbo].[core_blog] OFF
/****** Object:  Table [dbo].[user_profile]    Script Date: 06/17/2016 17:45:08 ******/
SET ANSI_NULLS ON;
SET QUOTED_IDENTIFIER ON;
CREATE TABLE [dbo].[user_profile](
    [ID] [bigint] IDENTITY(1,1) NOT NULL,
    [last_name] [nvarchar](450) NULL,
    [salutation] [nvarchar](1024) NULL,
    [position] [nvarchar](1024) NULL,
    [company] [nvarchar](1024) NULL,
    [first_name] [nvarchar](450) NULL,
    [last_modification_date] [datetime2](7) NULL,
    [last_photo_modification_date] [datetime2](7) NULL,
    [time_zone_id] [nvarchar](1024) NULL,
    [SMALL_IMAGE_FK] [bigint] NULL,
    [CONTACT_FK] [bigint] NULL,
    [MEDIUM_IMAGE_FK] [bigint] NULL,
    [LARGE_IMAGE_FK] [bigint] NULL,
    [NOTIFICATION_CONFIG_FK] [bigint] NOT NULL,
PRIMARY KEY CLUSTERED 
(
    [ID] ASC
)WITH (PAD_INDEX  = OFF, STATISTICS_NORECOMPUTE  = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS  = ON, ALLOW_PAGE_LOCKS  = ON) ON [PRIMARY]
) ON [PRIMARY];
CREATE UNIQUE NONCLUSTERED INDEX [user_profile_contact_fk_key] ON [dbo].[user_profile] 
(
    [CONTACT_FK] ASC
)
WHERE ([CONTACT_FK] IS NOT NULL)
WITH (PAD_INDEX  = OFF, STATISTICS_NORECOMPUTE  = OFF, SORT_IN_TEMPDB = OFF, IGNORE_DUP_KEY = OFF, DROP_EXISTING = OFF, ONLINE = OFF, ALLOW_ROW_LOCKS  = ON, ALLOW_PAGE_LOCKS  = ON) ON [PRIMARY];
CREATE NONCLUSTERED INDEX [user_profile_first_name_index] ON [dbo].[user_profile] 
(
    [first_name] ASC
)WITH (PAD_INDEX  = OFF, STATISTICS_NORECOMPUTE  = OFF, SORT_IN_TEMPDB = OFF, IGNORE_DUP_KEY = OFF, DROP_EXISTING = OFF, ONLINE = OFF, ALLOW_ROW_LOCKS  = ON, ALLOW_PAGE_LOCKS  = ON) ON [PRIMARY];
CREATE UNIQUE NONCLUSTERED INDEX [user_profile_large_image_fk_key] ON [dbo].[user_profile] 
(
    [LARGE_IMAGE_FK] ASC
)
WHERE ([LARGE_IMAGE_FK] IS NOT NULL)
WITH (PAD_INDEX  = OFF, STATISTICS_NORECOMPUTE  = OFF, SORT_IN_TEMPDB = OFF, IGNORE_DUP_KEY = OFF, DROP_EXISTING = OFF, ONLINE = OFF, ALLOW_ROW_LOCKS  = ON, ALLOW_PAGE_LOCKS  = ON) ON [PRIMARY];
CREATE NONCLUSTERED INDEX [user_profile_last_name_index] ON [dbo].[user_profile] 
(
    [last_name] ASC
)WITH (PAD_INDEX  = OFF, STATISTICS_NORECOMPUTE  = OFF, SORT_IN_TEMPDB = OFF, IGNORE_DUP_KEY = OFF, DROP_EXISTING = OFF, ONLINE = OFF, ALLOW_ROW_LOCKS  = ON, ALLOW_PAGE_LOCKS  = ON) ON [PRIMARY];
CREATE UNIQUE NONCLUSTERED INDEX [user_profile_medium_image_fk_key] ON [dbo].[user_profile] 
(
    [MEDIUM_IMAGE_FK] ASC
)
WHERE ([MEDIUM_IMAGE_FK] IS NOT NULL)
WITH (PAD_INDEX  = OFF, STATISTICS_NORECOMPUTE  = OFF, SORT_IN_TEMPDB = OFF, IGNORE_DUP_KEY = OFF, DROP_EXISTING = OFF, ONLINE = OFF, ALLOW_ROW_LOCKS  = ON, ALLOW_PAGE_LOCKS  = ON) ON [PRIMARY];
CREATE UNIQUE NONCLUSTERED INDEX [user_profile_notification_config_fk_key] ON [dbo].[user_profile] 
(
    [NOTIFICATION_CONFIG_FK] ASC
)
WHERE ([NOTIFICATION_CONFIG_FK] IS NOT NULL)
WITH (PAD_INDEX  = OFF, STATISTICS_NORECOMPUTE  = OFF, SORT_IN_TEMPDB = OFF, IGNORE_DUP_KEY = OFF, DROP_EXISTING = OFF, ONLINE = OFF, ALLOW_ROW_LOCKS  = ON, ALLOW_PAGE_LOCKS  = ON) ON [PRIMARY];
CREATE UNIQUE NONCLUSTERED INDEX [user_profile_small_image_fk_key] ON [dbo].[user_profile] 
(
    [SMALL_IMAGE_FK] ASC
)
WHERE ([SMALL_IMAGE_FK] IS NOT NULL)
WITH (PAD_INDEX  = OFF, STATISTICS_NORECOMPUTE  = OFF, SORT_IN_TEMPDB = OFF, IGNORE_DUP_KEY = OFF, DROP_EXISTING = OFF, ONLINE = OFF, ALLOW_ROW_LOCKS  = ON, ALLOW_PAGE_LOCKS  = ON) ON [PRIMARY];
SET IDENTITY_INSERT [dbo].[user_profile] ON
INSERT [dbo].[user_profile] ([ID], [last_name], [salutation], [position], [company], [first_name], [last_modification_date], [last_photo_modification_date], [time_zone_id], [SMALL_IMAGE_FK], [CONTACT_FK], [MEDIUM_IMAGE_FK], [LARGE_IMAGE_FK], [NOTIFICATION_CONFIG_FK]) VALUES (1, N'Admin', NULL, NULL, NULL, N'Peter', CAST(0x075088F49092833B0B AS DateTime2), NULL, N'time.zones.gmt.Europe/Amsterdam', NULL, NULL, NULL, NULL, 1)
SET IDENTITY_INSERT [dbo].[user_profile] OFF
/****** Object:  Table [dbo].[core_blog2blog]    Script Date: 06/17/2016 17:45:08 ******/
SET ANSI_NULLS ON;
SET QUOTED_IDENTIFIER ON;
CREATE TABLE [dbo].[core_blog2blog](
    [CHILDREN_FK] [bigint] NOT NULL,
    [PARENTS_FK] [bigint] NOT NULL,
 CONSTRAINT [PK_CORE_BLOG2BLOG] PRIMARY KEY CLUSTERED 
(
    [CHILDREN_FK] ASC,
    [PARENTS_FK] ASC
)WITH (PAD_INDEX  = OFF, STATISTICS_NORECOMPUTE  = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS  = ON, ALLOW_PAGE_LOCKS  = ON) ON [PRIMARY]
) ON [PRIMARY];
/****** Object:  Table [dbo].[core_blog_property]    Script Date: 06/17/2016 17:45:08 ******/
SET ANSI_NULLS ON;
SET QUOTED_IDENTIFIER ON;
CREATE TABLE [dbo].[core_blog_property](
    [ID] [bigint] IDENTITY(1,1) NOT NULL,
    [property_value] [nvarchar](4000) NOT NULL,
    [key_group] [nvarchar](128) NOT NULL,
    [property_key] [nvarchar](128) NOT NULL,
    [last_modification_date] [datetime2](7) NULL,
    [BLOG_FK] [bigint] NULL,
PRIMARY KEY CLUSTERED 
(
    [ID] ASC
)WITH (PAD_INDEX  = OFF, STATISTICS_NORECOMPUTE  = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS  = ON, ALLOW_PAGE_LOCKS  = ON) ON [PRIMARY]
) ON [PRIMARY];
/****** Object:  Table [dbo].[core_blog_member]    Script Date: 06/17/2016 17:45:08 ******/
SET ANSI_NULLS ON;
SET QUOTED_IDENTIFIER ON;
CREATE TABLE [dbo].[core_blog_member](
    [ID] [bigint] IDENTITY(1,1) NOT NULL,
    [class] [nvarchar](255) NOT NULL,
    [role] [nvarchar](1024) NOT NULL,
    [BLOG_FK] [bigint] NOT NULL,
    [KENMEI_ENTITY_FK] [bigint] NOT NULL,
    [external_system_id] [nvarchar](50) NULL,
PRIMARY KEY CLUSTERED 
(
    [ID] ASC
)WITH (PAD_INDEX  = OFF, STATISTICS_NORECOMPUTE  = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS  = ON, ALLOW_PAGE_LOCKS  = ON) ON [PRIMARY]
) ON [PRIMARY];
SET IDENTITY_INSERT [dbo].[core_blog_member] ON
INSERT [dbo].[core_blog_member] ([ID], [class], [role], [BLOG_FK], [KENMEI_ENTITY_FK], [external_system_id]) VALUES (1, N'BlogMemberImpl', N'MANAGER', 1, 1, NULL)
SET IDENTITY_INSERT [dbo].[core_blog_member] OFF
/****** Object:  Table [dbo].[core_blog2tag]    Script Date: 06/17/2016 17:45:08 ******/
SET ANSI_NULLS ON;
SET QUOTED_IDENTIFIER ON;
CREATE TABLE [dbo].[core_blog2tag](
    [BLOGS_FK] [bigint] NOT NULL,
    [TAGS_FK] [bigint] NOT NULL,
 CONSTRAINT [blogs_tag_constraint] UNIQUE NONCLUSTERED 
(
    [BLOGS_FK] ASC,
    [TAGS_FK] ASC
)WITH (PAD_INDEX  = OFF, STATISTICS_NORECOMPUTE  = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS  = ON, ALLOW_PAGE_LOCKS  = ON) ON [PRIMARY]
) ON [PRIMARY];
INSERT [dbo].[core_blog2tag] ([BLOGS_FK], [TAGS_FK]) VALUES (1, 1)
/****** Object:  Table [dbo].[configuration_client]    Script Date: 06/17/2016 17:45:08 ******/
SET ANSI_NULLS ON;
SET QUOTED_IDENTIFIER ON;
CREATE TABLE [dbo].[configuration_client](
    [ID] [bigint] IDENTITY(1,1) NOT NULL,
    [logo_image] [varbinary](max) NULL,
    [last_logo_image_modification_d] [datetime2](7) NULL,
    [time_zone_id] [nvarchar](1024) NULL,
    [DEFAULT_BLOG_FK] [bigint] NULL,
PRIMARY KEY CLUSTERED 
(
    [ID] ASC
)WITH (PAD_INDEX  = OFF, STATISTICS_NORECOMPUTE  = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS  = ON, ALLOW_PAGE_LOCKS  = ON) ON [PRIMARY]
) ON [PRIMARY] TEXTIMAGE_ON [PRIMARY];
CREATE UNIQUE NONCLUSTERED INDEX [configuration_client_default_blog_fk_key] ON [dbo].[configuration_client] 
(
    [DEFAULT_BLOG_FK] ASC
)
WHERE ([DEFAULT_BLOG_FK] IS NOT NULL)
WITH (PAD_INDEX  = OFF, STATISTICS_NORECOMPUTE  = OFF, SORT_IN_TEMPDB = OFF, IGNORE_DUP_KEY = OFF, DROP_EXISTING = OFF, ONLINE = OFF, ALLOW_ROW_LOCKS  = ON, ALLOW_PAGE_LOCKS  = ON) ON [PRIMARY];
SET IDENTITY_INSERT [dbo].[configuration_client] ON
INSERT [dbo].[configuration_client] ([ID], [logo_image], [last_logo_image_modification_d], [time_zone_id], [DEFAULT_BLOG_FK]) VALUES (1, NULL, NULL, N'time.zones.gmt.Europe/Amsterdam', 1)
SET IDENTITY_INSERT [dbo].[configuration_client] OFF
/****** Object:  Table [dbo].[core_external_object]    Script Date: 06/17/2016 17:45:08 ******/
SET ANSI_NULLS ON;
SET QUOTED_IDENTIFIER ON;
CREATE TABLE [dbo].[core_external_object](
    [ID] [bigint] IDENTITY(1,1) NOT NULL,
    [external_system_id] [nvarchar](50) NOT NULL,
    [external_id] [nvarchar](200) NOT NULL,
    [external_name] [nvarchar](1024) NULL,
    [BLOG_FK] [bigint] NULL,
PRIMARY KEY CLUSTERED 
(
    [ID] ASC
)WITH (PAD_INDEX  = OFF, STATISTICS_NORECOMPUTE  = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS  = ON, ALLOW_PAGE_LOCKS  = ON) ON [PRIMARY],
 CONSTRAINT [core_external_object_external_system_id_key] UNIQUE NONCLUSTERED 
(
    [external_system_id] ASC,
    [external_id] ASC
)WITH (PAD_INDEX  = OFF, STATISTICS_NORECOMPUTE  = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS  = ON, ALLOW_PAGE_LOCKS  = ON) ON [PRIMARY]
) ON [PRIMARY];
/****** Object:  Table [dbo].[core_entity2tags]    Script Date: 06/17/2016 17:45:08 ******/
SET ANSI_NULLS ON;
SET QUOTED_IDENTIFIER ON;
CREATE TABLE [dbo].[core_entity2tags](
    [KENMEI_ENTITIES_FK] [bigint] NOT NULL,
    [TAGS_FK] [bigint] NOT NULL
) ON [PRIMARY];
CREATE NONCLUSTERED INDEX [core_entity2tags_idx] ON [dbo].[core_entity2tags] 
(
    [KENMEI_ENTITIES_FK] ASC,
    [TAGS_FK] ASC
)WITH (PAD_INDEX  = OFF, STATISTICS_NORECOMPUTE  = OFF, SORT_IN_TEMPDB = OFF, IGNORE_DUP_KEY = OFF, DROP_EXISTING = OFF, ONLINE = OFF, ALLOW_ROW_LOCKS  = ON, ALLOW_PAGE_LOCKS  = ON) ON [PRIMARY];
/****** Object:  Table [dbo].[core_tag2names]    Script Date: 06/17/2016 17:45:08 ******/
SET ANSI_NULLS ON;
SET QUOTED_IDENTIFIER ON;
CREATE TABLE [dbo].[core_tag2names](
    [TAGS_FK] [bigint] NOT NULL,
    [NAMES_FK] [bigint] NOT NULL,
 CONSTRAINT [core_tag2names_idx] UNIQUE NONCLUSTERED 
(
    [TAGS_FK] ASC,
    [NAMES_FK] ASC
)WITH (PAD_INDEX  = OFF, STATISTICS_NORECOMPUTE  = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS  = ON, ALLOW_PAGE_LOCKS  = ON) ON [PRIMARY]
) ON [PRIMARY];
/****** Object:  Table [dbo].[core_tag2descriptions]    Script Date: 06/17/2016 17:45:08 ******/
SET ANSI_NULLS ON;
SET QUOTED_IDENTIFIER ON;
CREATE TABLE [dbo].[core_tag2descriptions](
    [TAGS_FK] [bigint] NOT NULL,
    [DESCRIPTIONS_FK] [bigint] NOT NULL,
 CONSTRAINT [core_tag2descriptions_idx] UNIQUE NONCLUSTERED 
(
    [TAGS_FK] ASC,
    [DESCRIPTIONS_FK] ASC
)WITH (PAD_INDEX  = OFF, STATISTICS_NORECOMPUTE  = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS  = ON, ALLOW_PAGE_LOCKS  = ON) ON [PRIMARY]
) ON [PRIMARY];
/****** Object:  Table [dbo].[user_group]    Script Date: 06/17/2016 17:45:08 ******/
SET ANSI_NULLS ON;
SET QUOTED_IDENTIFIER ON;
CREATE TABLE [dbo].[user_group](
    [ID] [bigint] NOT NULL,
    [alias] [nvarchar](300) NOT NULL,
    [name] [nvarchar](1024) NOT NULL,
    [description] [nvarchar](max) NULL,
PRIMARY KEY CLUSTERED 
(
    [ID] ASC
)WITH (PAD_INDEX  = OFF, STATISTICS_NORECOMPUTE  = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS  = ON, ALLOW_PAGE_LOCKS  = ON) ON [PRIMARY],
 CONSTRAINT [user_group_alias_key] UNIQUE NONCLUSTERED 
(
    [alias] ASC
)WITH (PAD_INDEX  = OFF, STATISTICS_NORECOMPUTE  = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS  = ON, ALLOW_PAGE_LOCKS  = ON) ON [PRIMARY]
) ON [PRIMARY] TEXTIMAGE_ON [PRIMARY];
/****** Object:  Table [dbo].[user_user]    Script Date: 06/17/2016 17:45:08 ******/
SET ANSI_NULLS ON;
SET QUOTED_IDENTIFIER ON;
CREATE TABLE [dbo].[user_user](
    [ID] [bigint] NOT NULL,
    [password] [nvarchar](1024) NULL,
    [email] [nvarchar](255) NOT NULL,
    [language_code] [nvarchar](1024) NOT NULL,
    [last_login] [datetime2](7) NULL,
    [status] [nvarchar](1024) NOT NULL,
    [alias] [nvarchar](255) NULL,
    [terms_accepted] [bit] NOT NULL,
    [reminder_mail_sent] [bit] NOT NULL,
    [status_changed] [datetime2](7) NULL,
    [PROFILE_FK] [bigint] NOT NULL,
    [authentication_token] [nvarchar](1024) NULL,
PRIMARY KEY CLUSTERED 
(
    [ID] ASC
)WITH (PAD_INDEX  = OFF, STATISTICS_NORECOMPUTE  = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS  = ON, ALLOW_PAGE_LOCKS  = ON) ON [PRIMARY],
 CONSTRAINT [user_user_email_key] UNIQUE NONCLUSTERED 
(
    [email] ASC
)WITH (PAD_INDEX  = OFF, STATISTICS_NORECOMPUTE  = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS  = ON, ALLOW_PAGE_LOCKS  = ON) ON [PRIMARY],
 CONSTRAINT [user_user_profile_fk_key] UNIQUE NONCLUSTERED 
(
    [PROFILE_FK] ASC
)WITH (PAD_INDEX  = OFF, STATISTICS_NORECOMPUTE  = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS  = ON, ALLOW_PAGE_LOCKS  = ON) ON [PRIMARY]
) ON [PRIMARY];
CREATE UNIQUE NONCLUSTERED INDEX [user_user_alias_key] ON [dbo].[user_user] 
(
    [alias] ASC
)
WHERE ([alias] IS NOT NULL)
WITH (PAD_INDEX  = OFF, STATISTICS_NORECOMPUTE  = OFF, SORT_IN_TEMPDB = OFF, IGNORE_DUP_KEY = OFF, DROP_EXISTING = OFF, ONLINE = OFF, ALLOW_ROW_LOCKS  = ON, ALLOW_PAGE_LOCKS  = ON) ON [PRIMARY];
CREATE NONCLUSTERED INDEX [user_user_email_index] ON [dbo].[user_user] 
(
    [email] ASC
)WITH (PAD_INDEX  = OFF, STATISTICS_NORECOMPUTE  = OFF, SORT_IN_TEMPDB = OFF, IGNORE_DUP_KEY = OFF, DROP_EXISTING = OFF, ONLINE = OFF, ALLOW_ROW_LOCKS  = ON, ALLOW_PAGE_LOCKS  = ON) ON [PRIMARY];
INSERT [dbo].[user_user] ([ID], [password], [email], [language_code], [last_login], [status], [alias], [terms_accepted], [reminder_mail_sent], [status_changed], [PROFILE_FK], [authentication_token]) VALUES (1, N'e10adc3949ba59abbe56e057f20f883e', N'communote@localhost', N'en', NULL, N'ACTIVE', N'communote', 1, 0, CAST(0x0780FDF49092833B0B AS DateTime2), 1, NULL)
/****** Object:  Table [dbo].[core_role2blog_granting_group]    Script Date: 06/17/2016 17:45:08 ******/
SET ANSI_NULLS ON;
SET QUOTED_IDENTIFIER ON;
CREATE TABLE [dbo].[core_role2blog_granting_group](
    [USER_TO_BLOG_ROLE_MAPPINGS_FK] [bigint] NOT NULL,
    [GRANTING_GROUPS_FK] [bigint] NOT NULL,
 CONSTRAINT [brm_gg_key] PRIMARY KEY CLUSTERED 
(
    [USER_TO_BLOG_ROLE_MAPPINGS_FK] ASC,
    [GRANTING_GROUPS_FK] ASC
)WITH (PAD_INDEX  = OFF, STATISTICS_NORECOMPUTE  = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS  = ON, ALLOW_PAGE_LOCKS  = ON) ON [PRIMARY]
) ON [PRIMARY];
/****** Object:  Table [dbo].[core_external_object_prop]    Script Date: 06/17/2016 17:45:08 ******/
SET ANSI_NULLS ON;
SET QUOTED_IDENTIFIER ON;
CREATE TABLE [dbo].[core_external_object_prop](
    [ID] [bigint] IDENTITY(1,1) NOT NULL,
    [property_key] [nvarchar](128) NULL,
    [property_value] [nvarchar](4000) NULL,
    [EXTERNAL_OBJECT_FK] [bigint] NULL,
    [key_group] [nvarchar](128) NULL,
    [last_modification_date] [datetime2](7) NULL,
PRIMARY KEY CLUSTERED 
(
    [ID] ASC
)WITH (PAD_INDEX  = OFF, STATISTICS_NORECOMPUTE  = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS  = ON, ALLOW_PAGE_LOCKS  = ON) ON [PRIMARY]
) ON [PRIMARY];
/****** Object:  Table [dbo].[configuration]    Script Date: 06/17/2016 17:45:08 ******/
SET ANSI_NULLS ON;
SET QUOTED_IDENTIFIER ON;
CREATE TABLE [dbo].[configuration](
    [ID] [bigint] IDENTITY(1,1) NOT NULL,
    [CLIENT_CONFIG_FK] [bigint] NULL,
PRIMARY KEY CLUSTERED 
(
    [ID] ASC
)WITH (PAD_INDEX  = OFF, STATISTICS_NORECOMPUTE  = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS  = ON, ALLOW_PAGE_LOCKS  = ON) ON [PRIMARY]
) ON [PRIMARY];
CREATE UNIQUE NONCLUSTERED INDEX [configuration_client_config_fk_key] ON [dbo].[configuration] 
(
    [CLIENT_CONFIG_FK] ASC
)
WHERE ([CLIENT_CONFIG_FK] IS NOT NULL)
WITH (PAD_INDEX  = OFF, STATISTICS_NORECOMPUTE  = OFF, SORT_IN_TEMPDB = OFF, IGNORE_DUP_KEY = OFF, DROP_EXISTING = OFF, ONLINE = OFF, ALLOW_ROW_LOCKS  = ON, ALLOW_PAGE_LOCKS  = ON) ON [PRIMARY];
SET IDENTITY_INSERT [dbo].[configuration] ON
INSERT [dbo].[configuration] ([ID], [CLIENT_CONFIG_FK]) VALUES (1, 1)
SET IDENTITY_INSERT [dbo].[configuration] OFF
/****** Object:  Table [dbo].[user_group_property]    Script Date: 06/17/2016 17:45:08 ******/
SET ANSI_NULLS ON;
SET QUOTED_IDENTIFIER ON;
CREATE TABLE [dbo].[user_group_property](
    [ID] [bigint] IDENTITY(1,1) NOT NULL,
    [property_value] [nvarchar](4000) NOT NULL,
    [key_group] [nvarchar](128) NOT NULL,
    [property_key] [nvarchar](128) NOT NULL,
    [last_modification_date] [datetime2](7) NULL,
    [KENMEI_ENTITY_GROUP_FK] [bigint] NULL,
PRIMARY KEY CLUSTERED 
(
    [ID] ASC
)WITH (PAD_INDEX  = OFF, STATISTICS_NORECOMPUTE  = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS  = ON, ALLOW_PAGE_LOCKS  = ON) ON [PRIMARY]
) ON [PRIMARY];
/****** Object:  Table [dbo].[user_group_member]    Script Date: 06/17/2016 17:45:08 ******/
SET ANSI_NULLS ON;
SET QUOTED_IDENTIFIER ON;
CREATE TABLE [dbo].[user_group_member](
    [GROUP_MEMBERS_FK] [bigint] NOT NULL,
    [GROUPS_FK] [bigint] NOT NULL,
 CONSTRAINT [user_group_member_pkey] PRIMARY KEY CLUSTERED 
(
    [GROUP_MEMBERS_FK] ASC,
    [GROUPS_FK] ASC
)WITH (PAD_INDEX  = OFF, STATISTICS_NORECOMPUTE  = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS  = ON, ALLOW_PAGE_LOCKS  = ON) ON [PRIMARY]
) ON [PRIMARY];
/****** Object:  Table [dbo].[user_group_external]    Script Date: 06/17/2016 17:45:08 ******/
SET ANSI_NULLS ON;
SET QUOTED_IDENTIFIER ON;
CREATE TABLE [dbo].[user_group_external](
    [ID] [bigint] NOT NULL,
    [external_system_id] [nvarchar](50) NOT NULL,
    [external_id] [nvarchar](250) NOT NULL,
    [additional_property] [nvarchar](max) NULL,
PRIMARY KEY CLUSTERED 
(
    [ID] ASC
)WITH (PAD_INDEX  = OFF, STATISTICS_NORECOMPUTE  = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS  = ON, ALLOW_PAGE_LOCKS  = ON) ON [PRIMARY],
 CONSTRAINT [user_group_external_unique_key] UNIQUE NONCLUSTERED 
(
    [external_id] ASC,
    [external_system_id] ASC
)WITH (PAD_INDEX  = OFF, STATISTICS_NORECOMPUTE  = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS  = ON, ALLOW_PAGE_LOCKS  = ON) ON [PRIMARY]
) ON [PRIMARY] TEXTIMAGE_ON [PRIMARY];
/****** Object:  Table [dbo].[user_user_property]    Script Date: 06/17/2016 17:45:08 ******/
SET ANSI_NULLS ON;
SET QUOTED_IDENTIFIER ON;
CREATE TABLE [dbo].[user_user_property](
    [ID] [bigint] IDENTITY(1,1) NOT NULL,
    [property_value] [nvarchar](4000) NOT NULL,
    [key_group] [nvarchar](128) NOT NULL,
    [property_key] [nvarchar](128) NOT NULL,
    [last_modification_date] [datetime2](7) NULL,
    [KENMEI_USER_FK] [bigint] NULL,
PRIMARY KEY CLUSTERED 
(
    [ID] ASC
)WITH (PAD_INDEX  = OFF, STATISTICS_NORECOMPUTE  = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS  = ON, ALLOW_PAGE_LOCKS  = ON) ON [PRIMARY]
) ON [PRIMARY];
/****** Object:  Table [dbo].[user_navigation_item]    Script Date: 06/17/2016 17:45:08 ******/
SET ANSI_NULLS ON;
SET QUOTED_IDENTIFIER ON;
CREATE TABLE [dbo].[user_navigation_item](
    [ID] [bigint] IDENTITY(1,1) NOT NULL,
    [item_index] [int] NOT NULL,
    [data] [text] NOT NULL,
    [last_access_date] [datetime2](7) NULL,
    [OWNER_FK] [bigint] NOT NULL,
    [name] [varchar](255) NOT NULL,
 CONSTRAINT [PK_USER_NAVIGATION_ITEM] PRIMARY KEY CLUSTERED 
(
    [ID] ASC
)WITH (PAD_INDEX  = OFF, STATISTICS_NORECOMPUTE  = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS  = ON, ALLOW_PAGE_LOCKS  = ON) ON [PRIMARY]
) ON [PRIMARY] TEXTIMAGE_ON [PRIMARY];
SET IDENTITY_INSERT [dbo].[user_navigation_item] ON
INSERT [dbo].[user_navigation_item] ([ID], [item_index], [data], [last_access_date], [OWNER_FK], [name]) VALUES (1, -1, N'{"contextType":"notesOverview","contextId":null,"filters":{"showFollowedItems":true}}', CAST(0x07D0D7069192833B0B AS DateTime2), 1, N'following')
INSERT [dbo].[user_navigation_item] ([ID], [item_index], [data], [last_access_date], [OWNER_FK], [name]) VALUES (2, -1, N'{"contextType":"notesOverview","contextId":null,"filters":{"showPostsForMe":true}}', CAST(0x07E0E00B9192833B0B AS DateTime2), 1, N'mentions')
SET IDENTITY_INSERT [dbo].[user_navigation_item] OFF
/****** Object:  Table [dbo].[user_of_group]    Script Date: 06/17/2016 17:45:08 ******/
SET ANSI_NULLS ON;
SET QUOTED_IDENTIFIER ON;
CREATE TABLE [dbo].[user_of_group](
    [ID] [bigint] IDENTITY(1,1) NOT NULL,
    [modification_type] [nvarchar](1024) NULL,
    [GROUP_FK] [bigint] NOT NULL,
    [USER_FK] [bigint] NOT NULL,
PRIMARY KEY CLUSTERED 
(
    [ID] ASC
)WITH (PAD_INDEX  = OFF, STATISTICS_NORECOMPUTE  = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS  = ON, ALLOW_PAGE_LOCKS  = ON) ON [PRIMARY]
) ON [PRIMARY];
/****** Object:  Table [dbo].[core_user2follows]    Script Date: 06/17/2016 17:45:08 ******/
SET ANSI_NULLS ON;
SET QUOTED_IDENTIFIER ON;
CREATE TABLE [dbo].[core_user2follows](
    [FOLLOWED_ITEMS_FK] [bigint] NOT NULL,
    [kenmei_users_fk] [bigint] NOT NULL,
 CONSTRAINT [followed_items_users_key] PRIMARY KEY CLUSTERED 
(
    [kenmei_users_fk] ASC,
    [FOLLOWED_ITEMS_FK] ASC
)WITH (PAD_INDEX  = OFF, STATISTICS_NORECOMPUTE  = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS  = ON, ALLOW_PAGE_LOCKS  = ON) ON [PRIMARY]
) ON [PRIMARY];
/****** Object:  Table [dbo].[configuration_setting]    Script Date: 06/17/2016 17:45:08 ******/
SET ANSI_NULLS ON;
SET QUOTED_IDENTIFIER ON;
CREATE TABLE [dbo].[configuration_setting](
    [SETTING_KEY] [nvarchar](255) NOT NULL,
    [setting_value] [nvarchar](max) NULL,
    [CONFIGURATION_FK] [bigint] NULL,
    [last_modification_timestamp] [bigint] NULL,
 CONSTRAINT [pk_configuration_setting] PRIMARY KEY CLUSTERED 
(
    [SETTING_KEY] ASC
)WITH (PAD_INDEX  = OFF, STATISTICS_NORECOMPUTE  = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS  = ON, ALLOW_PAGE_LOCKS  = ON) ON [PRIMARY]
) ON [PRIMARY] TEXTIMAGE_ON [PRIMARY];
INSERT [dbo].[configuration_setting] ([SETTING_KEY], [setting_value], [CONFIGURATION_FK], [last_modification_timestamp]) VALUES (N'client.creation.date', N'kgIxnw8ZFoIWg_Fp3Ryppg:dhAC9b_gspzbm3Zdiva-38to', 1, 1466177349533)
INSERT [dbo].[configuration_setting] ([SETTING_KEY], [setting_value], [CONFIGURATION_FK], [last_modification_timestamp]) VALUES (N'kenmei.automatic.user.activation', N'false', 1, 1466177349505)
INSERT [dbo].[configuration_setting] ([SETTING_KEY], [setting_value], [CONFIGURATION_FK], [last_modification_timestamp]) VALUES (N'kenmei.blog.allow.public.access', N'false', 1, 1466177349513)
INSERT [dbo].[configuration_setting] ([SETTING_KEY], [setting_value], [CONFIGURATION_FK], [last_modification_timestamp]) VALUES (N'kenmei.client.allow.all.can.read.write.for.all.users', N'true', 1, 1466177349516)
INSERT [dbo].[configuration_setting] ([SETTING_KEY], [setting_value], [CONFIGURATION_FK], [last_modification_timestamp]) VALUES (N'kenmei.client.blog.count.100.mail', N'', 1, 1466177349543)
INSERT [dbo].[configuration_setting] ([SETTING_KEY], [setting_value], [CONFIGURATION_FK], [last_modification_timestamp]) VALUES (N'kenmei.client.blog.count.90.mail', N'', 1, 1466177349555)
INSERT [dbo].[configuration_setting] ([SETTING_KEY], [setting_value], [CONFIGURATION_FK], [last_modification_timestamp]) VALUES (N'kenmei.client.blog.count.limit', N'0', 1, 1466177349528)
INSERT [dbo].[configuration_setting] ([SETTING_KEY], [setting_value], [CONFIGURATION_FK], [last_modification_timestamp]) VALUES (N'kenmei.client.delete.user.by.anonymize.enabled', N'false', 1, 1466177349553)
INSERT [dbo].[configuration_setting] ([SETTING_KEY], [setting_value], [CONFIGURATION_FK], [last_modification_timestamp]) VALUES (N'kenmei.client.delete.user.by.disable.enabled', N'false', 1, 1466177349494)
INSERT [dbo].[configuration_setting] ([SETTING_KEY], [setting_value], [CONFIGURATION_FK], [last_modification_timestamp]) VALUES (N'kenmei.client.reply.to.address', N'', 1, 1466177349535)
INSERT [dbo].[configuration_setting] ([SETTING_KEY], [setting_value], [CONFIGURATION_FK], [last_modification_timestamp]) VALUES (N'kenmei.client.reply.to.address.name', N'', 1, 1466177349549)
INSERT [dbo].[configuration_setting] ([SETTING_KEY], [setting_value], [CONFIGURATION_FK], [last_modification_timestamp]) VALUES (N'kenmei.client.support.email.address', N'', 1, 1466177349529)
INSERT [dbo].[configuration_setting] ([SETTING_KEY], [setting_value], [CONFIGURATION_FK], [last_modification_timestamp]) VALUES (N'kenmei.client.user.tagged.count.100.mail', N'', 1, 1466177349511)
INSERT [dbo].[configuration_setting] ([SETTING_KEY], [setting_value], [CONFIGURATION_FK], [last_modification_timestamp]) VALUES (N'kenmei.client.user.tagged.count.90.mail', N'', 1, 1466177349514)
INSERT [dbo].[configuration_setting] ([SETTING_KEY], [setting_value], [CONFIGURATION_FK], [last_modification_timestamp]) VALUES (N'kenmei.client.user.tagged.count.limit', N'0', 1, 1466177349557)
INSERT [dbo].[configuration_setting] ([SETTING_KEY], [setting_value], [CONFIGURATION_FK], [last_modification_timestamp]) VALUES (N'kenmei.crc.file.repository.size.100.mail', N'2016-06-16', 1, 1466177349539)
INSERT [dbo].[configuration_setting] ([SETTING_KEY], [setting_value], [CONFIGURATION_FK], [last_modification_timestamp]) VALUES (N'kenmei.crc.file.repository.size.90.mail', N'false', 1, 1466177349508)
INSERT [dbo].[configuration_setting] ([SETTING_KEY], [setting_value], [CONFIGURATION_FK], [last_modification_timestamp]) VALUES (N'kenmei.crc.file.repository.size.limit', N'0', 1, 1466177349525)
INSERT [dbo].[configuration_setting] ([SETTING_KEY], [setting_value], [CONFIGURATION_FK], [last_modification_timestamp]) VALUES (N'kenmei.notification.render.permalinks', N'true', 1, 1466177349507)
INSERT [dbo].[configuration_setting] ([SETTING_KEY], [setting_value], [CONFIGURATION_FK], [last_modification_timestamp]) VALUES (N'kenmei.unique.client.identifer', N'66116533-eb19-4239-a1a0-47c479c48d51', 1, 1466177349519)
/****** Object:  Table [dbo].[configuration_external_sys]    Script Date: 06/17/2016 17:45:08 ******/
SET ANSI_NULLS ON;
SET QUOTED_IDENTIFIER ON;
CREATE TABLE [dbo].[configuration_external_sys](
    [ID] [bigint] IDENTITY(1,1) NOT NULL,
    [allow_external_authentication] [bit] NOT NULL,
    [system_id] [nvarchar](50) NOT NULL,
    [primary_authentication] [bit] NOT NULL,
    [synchronize_user_groups] [bit] NOT NULL,
    [CONFIGURATION_FK] [bigint] NULL,
PRIMARY KEY CLUSTERED 
(
    [ID] ASC
)WITH (PAD_INDEX  = OFF, STATISTICS_NORECOMPUTE  = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS  = ON, ALLOW_PAGE_LOCKS  = ON) ON [PRIMARY],
 CONSTRAINT [configuration_external_system_system_id_key] UNIQUE NONCLUSTERED 
(
    [system_id] ASC
)WITH (PAD_INDEX  = OFF, STATISTICS_NORECOMPUTE  = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS  = ON, ALLOW_PAGE_LOCKS  = ON) ON [PRIMARY]
) ON [PRIMARY];
/****** Object:  Table [dbo].[core_note]    Script Date: 06/17/2016 17:45:08 ******/
SET ANSI_NULLS ON;
SET QUOTED_IDENTIFIER ON;
CREATE TABLE [dbo].[core_note](
    [ID] [bigint] IDENTITY(1,1) NOT NULL,
    [creation_date] [datetime2](7) NULL,
    [last_modification_date] [datetime2](7) NULL,
    [creation_source] [nvarchar](1024) NOT NULL,
    [direct] [bit] NOT NULL,
    [status] [nvarchar](1024) NOT NULL,
    [version] [bigint] NOT NULL,
    [discussion_path] [nvarchar](1024) NULL,
    [USER_FK] [bigint] NOT NULL,
    [CONTENT_FK] [bigint] NOT NULL,
    [GLOBAL_ID_FK] [bigint] NULL,
    [BLOG_FK] [bigint] NOT NULL,
    [PARENT_FK] [bigint] NULL,
    [ORIGIN_FK] [bigint] NULL,
    [discussion_id] [bigint] NULL,
    [last_discussion_creation_date] [datetime2](7) NULL,
    [mention_topic_readers] [bit] NULL,
    [mention_topic_authors] [bit] NULL,
    [mention_topic_managers] [bit] NULL,
    [mention_discussion_authors] [bit] NULL,
    [crawl_last_modification_date] [datetime2](7) NULL,
PRIMARY KEY CLUSTERED 
(
    [ID] ASC
)WITH (PAD_INDEX  = OFF, STATISTICS_NORECOMPUTE  = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS  = ON, ALLOW_PAGE_LOCKS  = ON) ON [PRIMARY],
 CONSTRAINT [core_note_content_fk_key] UNIQUE NONCLUSTERED 
(
    [CONTENT_FK] ASC
)WITH (PAD_INDEX  = OFF, STATISTICS_NORECOMPUTE  = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS  = ON, ALLOW_PAGE_LOCKS  = ON) ON [PRIMARY]
) ON [PRIMARY];
CREATE NONCLUSTERED INDEX [core_note_creation_date_index] ON [dbo].[core_note] 
(
    [creation_date] DESC
)WITH (PAD_INDEX  = OFF, STATISTICS_NORECOMPUTE  = OFF, SORT_IN_TEMPDB = OFF, IGNORE_DUP_KEY = OFF, DROP_EXISTING = OFF, ONLINE = OFF, ALLOW_ROW_LOCKS  = ON, ALLOW_PAGE_LOCKS  = ON) ON [PRIMARY];
CREATE NONCLUSTERED INDEX [core_note_discussion_id] ON [dbo].[core_note] 
(
    [discussion_id] ASC
)WITH (PAD_INDEX  = OFF, STATISTICS_NORECOMPUTE  = OFF, SORT_IN_TEMPDB = OFF, IGNORE_DUP_KEY = OFF, DROP_EXISTING = OFF, ONLINE = OFF, ALLOW_ROW_LOCKS  = ON, ALLOW_PAGE_LOCKS  = ON) ON [PRIMARY];
CREATE UNIQUE NONCLUSTERED INDEX [core_note_global_id_fk_key] ON [dbo].[core_note] 
(
    [GLOBAL_ID_FK] ASC
)
WHERE ([GLOBAL_ID_FK] IS NOT NULL)
WITH (PAD_INDEX  = OFF, STATISTICS_NORECOMPUTE  = OFF, SORT_IN_TEMPDB = OFF, IGNORE_DUP_KEY = OFF, DROP_EXISTING = OFF, ONLINE = OFF, ALLOW_ROW_LOCKS  = ON, ALLOW_PAGE_LOCKS  = ON) ON [PRIMARY];
CREATE NONCLUSTERED INDEX [core_note_id_asc_idx] ON [dbo].[core_note] 
(
    [direct] ASC
)
INCLUDE ( [ID],
[creation_date],
[status],
[BLOG_FK]) WITH (PAD_INDEX  = OFF, STATISTICS_NORECOMPUTE  = OFF, SORT_IN_TEMPDB = OFF, IGNORE_DUP_KEY = OFF, DROP_EXISTING = OFF, ONLINE = OFF, ALLOW_ROW_LOCKS  = ON, ALLOW_PAGE_LOCKS  = ON) ON [PRIMARY];
CREATE NONCLUSTERED INDEX [core_note_orgin_fk_idx] ON [dbo].[core_note] 
(
    [ORIGIN_FK] ASC
)
INCLUDE ( [ID],
[creation_date],
[last_modification_date],
[creation_source],
[direct],
[status],
[version],
[discussion_path],
[USER_FK],
[CONTENT_FK],
[GLOBAL_ID_FK],
[BLOG_FK],
[PARENT_FK],
[discussion_id]) WITH (PAD_INDEX  = OFF, STATISTICS_NORECOMPUTE  = OFF, SORT_IN_TEMPDB = OFF, IGNORE_DUP_KEY = OFF, DROP_EXISTING = OFF, ONLINE = OFF, ALLOW_ROW_LOCKS  = ON, ALLOW_PAGE_LOCKS  = ON) ON [PRIMARY];
CREATE NONCLUSTERED INDEX [core_note_parent_fk_idx] ON [dbo].[core_note] 
(
    [PARENT_FK] ASC
)
INCLUDE ( [ID],
[creation_date],
[last_modification_date],
[creation_source],
[direct],
[status],
[version],
[discussion_path],
[USER_FK],
[CONTENT_FK],
[GLOBAL_ID_FK],
[BLOG_FK],
[ORIGIN_FK],
[discussion_id]) WITH (PAD_INDEX  = OFF, STATISTICS_NORECOMPUTE  = OFF, SORT_IN_TEMPDB = OFF, IGNORE_DUP_KEY = OFF, DROP_EXISTING = OFF, ONLINE = OFF, ALLOW_ROW_LOCKS  = ON, ALLOW_PAGE_LOCKS  = ON) ON [PRIMARY];
CREATE NONCLUSTERED INDEX [core_note_user_fk_idx] ON [dbo].[core_note] 
(
    [USER_FK] ASC
)WITH (PAD_INDEX  = OFF, STATISTICS_NORECOMPUTE  = OFF, SORT_IN_TEMPDB = OFF, IGNORE_DUP_KEY = OFF, DROP_EXISTING = OFF, ONLINE = OFF, ALLOW_ROW_LOCKS  = ON, ALLOW_PAGE_LOCKS  = ON) ON [PRIMARY];
CREATE NONCLUSTERED INDEX [kenmei5455_idx] ON [dbo].[core_note] 
(
    [creation_date] DESC,
    [ID] DESC
)WITH (PAD_INDEX  = OFF, STATISTICS_NORECOMPUTE  = OFF, SORT_IN_TEMPDB = OFF, IGNORE_DUP_KEY = OFF, DROP_EXISTING = OFF, ONLINE = OFF, ALLOW_ROW_LOCKS  = ON, ALLOW_PAGE_LOCKS  = ON) ON [PRIMARY];
CREATE NONCLUSTERED INDEX [kenmei5562_idx] ON [dbo].[core_note] 
(
    [last_discussion_creation_date] DESC,
    [ID] DESC
)WITH (PAD_INDEX  = OFF, STATISTICS_NORECOMPUTE  = OFF, SORT_IN_TEMPDB = OFF, IGNORE_DUP_KEY = OFF, DROP_EXISTING = OFF, ONLINE = OFF, ALLOW_ROW_LOCKS  = ON, ALLOW_PAGE_LOCKS  = ON) ON [PRIMARY];
CREATE NONCLUSTERED INDEX [kenmei6122_idx] ON [dbo].[core_note] 
(
    [BLOG_FK] ASC,
    [USER_FK] ASC
)WITH (PAD_INDEX  = OFF, STATISTICS_NORECOMPUTE  = OFF, SORT_IN_TEMPDB = OFF, IGNORE_DUP_KEY = OFF, DROP_EXISTING = OFF, ONLINE = OFF, ALLOW_ROW_LOCKS  = ON, ALLOW_PAGE_LOCKS  = ON) ON [PRIMARY];
/****** Object:  Table [dbo].[security_code]    Script Date: 06/17/2016 17:45:08 ******/
SET ANSI_NULLS ON;
SET QUOTED_IDENTIFIER ON;
CREATE TABLE [dbo].[security_code](
    [ID] [bigint] IDENTITY(1,1) NOT NULL,
    [code] [nvarchar](255) NOT NULL,
    [action] [nvarchar](1024) NOT NULL,
    [creating_date] [datetime2](7) NULL,
    [KENMEI_USER_FK] [bigint] NULL,
PRIMARY KEY CLUSTERED 
(
    [ID] ASC
)WITH (PAD_INDEX  = OFF, STATISTICS_NORECOMPUTE  = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS  = ON, ALLOW_PAGE_LOCKS  = ON) ON [PRIMARY],
 CONSTRAINT [security_code_code_key] UNIQUE NONCLUSTERED 
(
    [code] ASC
)WITH (PAD_INDEX  = OFF, STATISTICS_NORECOMPUTE  = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS  = ON, ALLOW_PAGE_LOCKS  = ON) ON [PRIMARY]
) ON [PRIMARY];
/****** Object:  Table [dbo].[security_user_status]    Script Date: 06/17/2016 17:45:08 ******/
SET ANSI_NULLS ON;
SET QUOTED_IDENTIFIER ON;
CREATE TABLE [dbo].[security_user_status](
    [ID] [bigint] IDENTITY(1,1) NOT NULL,
    [locked_timeout] [datetime2](7) NULL,
    [failed_auth_counter] [int] NOT NULL,
    [channel_type] [nvarchar](1024) NOT NULL,
    [KENMEI_USER_FK] [bigint] NULL,
PRIMARY KEY CLUSTERED 
(
    [ID] ASC
)WITH (PAD_INDEX  = OFF, STATISTICS_NORECOMPUTE  = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS  = ON, ALLOW_PAGE_LOCKS  = ON) ON [PRIMARY]
) ON [PRIMARY];
/****** Object:  Table [dbo].[user_external_auth]    Script Date: 06/17/2016 17:45:08 ******/
SET ANSI_NULLS ON;
SET QUOTED_IDENTIFIER ON;
CREATE TABLE [dbo].[user_external_auth](
    [ID] [bigint] IDENTITY(1,1) NOT NULL,
    [external_user_id] [nvarchar](250) NOT NULL,
    [system_id] [nvarchar](50) NOT NULL,
    [permanent_id] [nvarchar](1024) NULL,
    [additional_property] [nvarchar](max) NULL,
    [KENMEI_USER_FK] [bigint] NULL,
PRIMARY KEY CLUSTERED 
(
    [ID] ASC
)WITH (PAD_INDEX  = OFF, STATISTICS_NORECOMPUTE  = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS  = ON, ALLOW_PAGE_LOCKS  = ON) ON [PRIMARY],
 CONSTRAINT [user_external_auth_external_user_id_key] UNIQUE NONCLUSTERED 
(
    [external_user_id] ASC,
    [system_id] ASC
)WITH (PAD_INDEX  = OFF, STATISTICS_NORECOMPUTE  = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS  = ON, ALLOW_PAGE_LOCKS  = ON) ON [PRIMARY]
) ON [PRIMARY] TEXTIMAGE_ON [PRIMARY];
/****** Object:  Table [dbo].[user_authorities]    Script Date: 06/17/2016 17:45:08 ******/
SET ANSI_NULLS ON;
SET QUOTED_IDENTIFIER ON;
CREATE TABLE [dbo].[user_authorities](
    [ID] [bigint] IDENTITY(1,1) NOT NULL,
    [role] [nvarchar](1024) NOT NULL,
    [KENMEI_USER_FK] [bigint] NULL,
PRIMARY KEY CLUSTERED 
(
    [ID] ASC
)WITH (PAD_INDEX  = OFF, STATISTICS_NORECOMPUTE  = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS  = ON, ALLOW_PAGE_LOCKS  = ON) ON [PRIMARY]
) ON [PRIMARY];
SET IDENTITY_INSERT [dbo].[user_authorities] ON
INSERT [dbo].[user_authorities] ([ID], [role], [KENMEI_USER_FK]) VALUES (1, N'ROLE_KENMEI_CLIENT_MANAGER', 1)
INSERT [dbo].[user_authorities] ([ID], [role], [KENMEI_USER_FK]) VALUES (2, N'ROLE_KENMEI_USER', 1)
SET IDENTITY_INSERT [dbo].[user_authorities] OFF
/****** Object:  Table [dbo].[security_user_unlock_code]    Script Date: 06/17/2016 17:45:08 ******/
SET ANSI_NULLS ON;
SET QUOTED_IDENTIFIER ON;
CREATE TABLE [dbo].[security_user_unlock_code](
    [ID] [bigint] NOT NULL,
    [channel] [nvarchar](1024) NOT NULL,
PRIMARY KEY CLUSTERED 
(
    [ID] ASC
)WITH (PAD_INDEX  = OFF, STATISTICS_NORECOMPUTE  = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS  = ON, ALLOW_PAGE_LOCKS  = ON) ON [PRIMARY]
) ON [PRIMARY];
/****** Object:  Table [dbo].[security_user_code]    Script Date: 06/17/2016 17:45:08 ******/
SET ANSI_NULLS ON;
SET QUOTED_IDENTIFIER ON;
CREATE TABLE [dbo].[security_user_code](
    [ID] [bigint] NOT NULL,
PRIMARY KEY CLUSTERED 
(
    [ID] ASC
)WITH (PAD_INDEX  = OFF, STATISTICS_NORECOMPUTE  = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS  = ON, ALLOW_PAGE_LOCKS  = ON) ON [PRIMARY]
) ON [PRIMARY];
/****** Object:  Table [dbo].[security_invite_client]    Script Date: 06/17/2016 17:45:08 ******/
SET ANSI_NULLS ON;
SET QUOTED_IDENTIFIER ON;
CREATE TABLE [dbo].[security_invite_client](
    [ID] [bigint] NOT NULL,
PRIMARY KEY CLUSTERED 
(
    [ID] ASC
)WITH (PAD_INDEX  = OFF, STATISTICS_NORECOMPUTE  = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS  = ON, ALLOW_PAGE_LOCKS  = ON) ON [PRIMARY]
) ON [PRIMARY];
/****** Object:  Table [dbo].[security_invite_blog]    Script Date: 06/17/2016 17:45:08 ******/
SET ANSI_NULLS ON;
SET QUOTED_IDENTIFIER ON;
CREATE TABLE [dbo].[security_invite_blog](
    [ID] [bigint] NOT NULL,
    [invitor_id] [bigint] NULL,
PRIMARY KEY CLUSTERED 
(
    [ID] ASC
)WITH (PAD_INDEX  = OFF, STATISTICS_NORECOMPUTE  = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS  = ON, ALLOW_PAGE_LOCKS  = ON) ON [PRIMARY]
) ON [PRIMARY];
/****** Object:  Table [dbo].[security_forgotten_pw_code]    Script Date: 06/17/2016 17:45:08 ******/
SET ANSI_NULLS ON;
SET QUOTED_IDENTIFIER ON;
CREATE TABLE [dbo].[security_forgotten_pw_code](
    [ID] [bigint] NOT NULL,
PRIMARY KEY CLUSTERED 
(
    [ID] ASC
)WITH (PAD_INDEX  = OFF, STATISTICS_NORECOMPUTE  = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS  = ON, ALLOW_PAGE_LOCKS  = ON) ON [PRIMARY]
) ON [PRIMARY];
/****** Object:  Table [dbo].[security_email_code]    Script Date: 06/17/2016 17:45:08 ******/
SET ANSI_NULLS ON;
SET QUOTED_IDENTIFIER ON;
CREATE TABLE [dbo].[security_email_code](
    [ID] [bigint] NOT NULL,
    [new_email_address] [nvarchar](1024) NOT NULL,
PRIMARY KEY CLUSTERED 
(
    [ID] ASC
)WITH (PAD_INDEX  = OFF, STATISTICS_NORECOMPUTE  = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS  = ON, ALLOW_PAGE_LOCKS  = ON) ON [PRIMARY]
) ON [PRIMARY];
/****** Object:  Table [dbo].[core_users2favorite_notes]    Script Date: 06/17/2016 17:45:08 ******/
SET ANSI_NULLS ON;
SET QUOTED_IDENTIFIER ON;
CREATE TABLE [dbo].[core_users2favorite_notes](
    [FAVORITE_NOTES_FK] [bigint] NOT NULL,
    [FAVORITE_USERS_FK] [bigint] NOT NULL,
 CONSTRAINT [fav_notes_fav_users] PRIMARY KEY CLUSTERED 
(
    [FAVORITE_NOTES_FK] ASC,
    [FAVORITE_USERS_FK] ASC
)WITH (PAD_INDEX  = OFF, STATISTICS_NORECOMPUTE  = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS  = ON, ALLOW_PAGE_LOCKS  = ON) ON [PRIMARY]
) ON [PRIMARY];
/****** Object:  Table [dbo].[core_notes2user_to_notify]    Script Date: 06/17/2016 17:45:08 ******/
SET ANSI_NULLS ON;
SET QUOTED_IDENTIFIER ON;
CREATE TABLE [dbo].[core_notes2user_to_notify](
    [NOTES_FK] [bigint] NOT NULL,
    [USERS_TO_BE_NOTIFIED_FK] [bigint] NOT NULL,
 CONSTRAINT [notes_userstobenotified_key] PRIMARY KEY CLUSTERED 
(
    [NOTES_FK] ASC,
    [USERS_TO_BE_NOTIFIED_FK] ASC
)WITH (PAD_INDEX  = OFF, STATISTICS_NORECOMPUTE  = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS  = ON, ALLOW_PAGE_LOCKS  = ON) ON [PRIMARY]
) ON [PRIMARY];
/****** Object:  Table [dbo].[core_notes2tag]    Script Date: 06/17/2016 17:45:08 ******/
SET ANSI_NULLS ON;
SET QUOTED_IDENTIFIER ON;
CREATE TABLE [dbo].[core_notes2tag](
    [NOTES_FK] [bigint] NOT NULL,
    [TAGS_FK] [bigint] NOT NULL,
 CONSTRAINT [notes_tags_key] PRIMARY KEY CLUSTERED 
(
    [NOTES_FK] ASC,
    [TAGS_FK] ASC
)WITH (PAD_INDEX  = OFF, STATISTICS_NORECOMPUTE  = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS  = ON, ALLOW_PAGE_LOCKS  = ON) ON [PRIMARY]
) ON [PRIMARY];
CREATE NONCLUSTERED INDEX [core_notes2tag_asc_idx] ON [dbo].[core_notes2tag] 
(
    [TAGS_FK] ASC,
    [NOTES_FK] ASC
)WITH (PAD_INDEX  = OFF, STATISTICS_NORECOMPUTE  = OFF, SORT_IN_TEMPDB = OFF, IGNORE_DUP_KEY = OFF, DROP_EXISTING = OFF, ONLINE = OFF, ALLOW_ROW_LOCKS  = ON, ALLOW_PAGE_LOCKS  = ON) ON [PRIMARY];
/****** Object:  Table [dbo].[core_notes2crossblogs]    Script Date: 06/17/2016 17:45:08 ******/
SET ANSI_NULLS ON;
SET QUOTED_IDENTIFIER ON;
CREATE TABLE [dbo].[core_notes2crossblogs](
    [NOTES_FK] [bigint] NOT NULL,
    [CROSSPOST_BLOGS_FK] [bigint] NOT NULL,
 CONSTRAINT [notes_crosspost_blogs_key] PRIMARY KEY CLUSTERED 
(
    [NOTES_FK] ASC,
    [CROSSPOST_BLOGS_FK] ASC
)WITH (PAD_INDEX  = OFF, STATISTICS_NORECOMPUTE  = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS  = ON, ALLOW_PAGE_LOCKS  = ON) ON [PRIMARY]
) ON [PRIMARY];
/****** Object:  Table [dbo].[core_note2followable]    Script Date: 06/17/2016 17:45:08 ******/
SET ANSI_NULLS ON;
SET QUOTED_IDENTIFIER ON;
CREATE TABLE [dbo].[core_note2followable](
    [NOTES_FK] [bigint] NOT NULL,
    [FOLLOWABLE_ITEMS_FK] [bigint] NOT NULL,
 CONSTRAINT [notes_followable_items_key] PRIMARY KEY CLUSTERED 
(
    [NOTES_FK] ASC,
    [FOLLOWABLE_ITEMS_FK] ASC
)WITH (PAD_INDEX  = OFF, STATISTICS_NORECOMPUTE  = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS  = ON, ALLOW_PAGE_LOCKS  = ON) ON [PRIMARY]
) ON [PRIMARY];
CREATE NONCLUSTERED INDEX [core_note_follow_items_fk_idx] ON [dbo].[core_note2followable] 
(
    [FOLLOWABLE_ITEMS_FK] ASC
)WITH (PAD_INDEX  = OFF, STATISTICS_NORECOMPUTE  = OFF, SORT_IN_TEMPDB = OFF, IGNORE_DUP_KEY = OFF, DROP_EXISTING = OFF, ONLINE = OFF, ALLOW_ROW_LOCKS  = ON, ALLOW_PAGE_LOCKS  = ON) ON [PRIMARY];
CREATE NONCLUSTERED INDEX [core_note_follow_note_fk_idx] ON [dbo].[core_note2followable] 
(
    [NOTES_FK] ASC
)WITH (PAD_INDEX  = OFF, STATISTICS_NORECOMPUTE  = OFF, SORT_IN_TEMPDB = OFF, IGNORE_DUP_KEY = OFF, DROP_EXISTING = OFF, ONLINE = OFF, ALLOW_ROW_LOCKS  = ON, ALLOW_PAGE_LOCKS  = ON) ON [PRIMARY];
/****** Object:  Table [dbo].[core_note2direct_user]    Script Date: 06/17/2016 17:45:08 ******/
SET ANSI_NULLS ON;
SET QUOTED_IDENTIFIER ON;
CREATE TABLE [dbo].[core_note2direct_user](
    [DIRECT_NOTES_FK] [bigint] NOT NULL,
    [DIRECT_USERS_FK] [bigint] NOT NULL,
 CONSTRAINT [direct_notes_users_key] PRIMARY KEY CLUSTERED 
(
    [DIRECT_NOTES_FK] ASC,
    [DIRECT_USERS_FK] ASC
)WITH (PAD_INDEX  = OFF, STATISTICS_NORECOMPUTE  = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS  = ON, ALLOW_PAGE_LOCKS  = ON) ON [PRIMARY]
) ON [PRIMARY];
/****** Object:  Table [dbo].[core_note_property]    Script Date: 06/17/2016 17:45:08 ******/
SET ANSI_NULLS ON;
SET QUOTED_IDENTIFIER ON;
CREATE TABLE [dbo].[core_note_property](
    [ID] [bigint] IDENTITY(1,1) NOT NULL,
    [key_group] [nvarchar](128) NOT NULL,
    [property_key] [nvarchar](128) NOT NULL,
    [property_value] [nvarchar](4000) NOT NULL,
    [NOTE_FK] [bigint] NULL,
    [last_modification_date] [datetime2](7) NULL,
PRIMARY KEY CLUSTERED 
(
    [ID] ASC
)WITH (PAD_INDEX  = OFF, STATISTICS_NORECOMPUTE  = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS  = ON, ALLOW_PAGE_LOCKS  = ON) ON [PRIMARY]
) ON [PRIMARY];
/****** Object:  Table [dbo].[configuration_ldap]    Script Date: 06/17/2016 17:45:08 ******/
SET ANSI_NULLS ON;
SET QUOTED_IDENTIFIER ON;
CREATE TABLE [dbo].[configuration_ldap](
    [ID] [bigint] NOT NULL,
    [url] [nvarchar](1024) NOT NULL,
    [manager_password] [nvarchar](1024) NOT NULL,
    [manager_d_n] [nvarchar](1024) NOT NULL,
    [GROUP_SYNC_CONFIG_FK] [bigint] NULL,
    [USER_SEARCH_FK] [bigint] NOT NULL,
    [user_identifier_is_binary] [bit] NOT NULL,
    [sasl_mode] [nvarchar](1024) NULL,
    [dynamic_mode] [bit] NULL,
    [server_domain] [nvarchar](255) NULL,
    [query_prefix] [nvarchar](255) NULL,
PRIMARY KEY CLUSTERED 
(
    [ID] ASC
)WITH (PAD_INDEX  = OFF, STATISTICS_NORECOMPUTE  = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS  = ON, ALLOW_PAGE_LOCKS  = ON) ON [PRIMARY]
) ON [PRIMARY];
CREATE UNIQUE NONCLUSTERED INDEX [configuration_ldap_group_sync_config_fk_key] ON [dbo].[configuration_ldap] 
(
    [GROUP_SYNC_CONFIG_FK] ASC
)
WHERE ([GROUP_SYNC_CONFIG_FK] IS NOT NULL)
WITH (PAD_INDEX  = OFF, STATISTICS_NORECOMPUTE  = OFF, SORT_IN_TEMPDB = OFF, IGNORE_DUP_KEY = OFF, DROP_EXISTING = OFF, ONLINE = OFF, ALLOW_ROW_LOCKS  = ON, ALLOW_PAGE_LOCKS  = ON) ON [PRIMARY];
/****** Object:  Table [dbo].[configuration_confluence]    Script Date: 06/17/2016 17:45:08 ******/
SET ANSI_NULLS ON;
SET QUOTED_IDENTIFIER ON;
CREATE TABLE [dbo].[configuration_confluence](
    [ID] [bigint] NOT NULL,
    [authentication_api_url] [nvarchar](1024) NOT NULL,
    [image_api_url] [nvarchar](1024) NULL,
    [admin_login] [nvarchar](1024) NULL,
    [admin_password] [nvarchar](1024) NULL,
    [service_url] [nvarchar](1024) NULL,
    [permissions_url] [nvarchar](1024) NULL,
    [base_path] [nvarchar](1024) NULL,
PRIMARY KEY CLUSTERED 
(
    [ID] ASC
)WITH (PAD_INDEX  = OFF, STATISTICS_NORECOMPUTE  = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS  = ON, ALLOW_PAGE_LOCKS  = ON) ON [PRIMARY]
) ON [PRIMARY];
/****** Object:  Table [dbo].[core_attachment]    Script Date: 06/17/2016 17:45:08 ******/
SET ANSI_NULLS ON;
SET QUOTED_IDENTIFIER ON;
CREATE TABLE [dbo].[core_attachment](
    [ID] [bigint] IDENTITY(1,1) NOT NULL,
    [content_identifier] [nvarchar](250) NOT NULL,
    [repository_identifier] [nvarchar](1024) NOT NULL,
    [name] [nvarchar](1024) NOT NULL,
    [content_type] [nvarchar](1024) NULL,
    [attachment_size] [bigint] NULL,
    [status] [nvarchar](1024) NOT NULL,
    [GLOBAL_ID_FK] [bigint] NULL,
    [NOTE_FK] [bigint] NULL,
    [UPLOADER_FK] [bigint] NULL,
    [upload_date] [datetime2](7) NULL,
PRIMARY KEY CLUSTERED 
(
    [ID] ASC
)WITH (PAD_INDEX  = OFF, STATISTICS_NORECOMPUTE  = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS  = ON, ALLOW_PAGE_LOCKS  = ON) ON [PRIMARY],
 CONSTRAINT [content_identifier_unique_key] UNIQUE NONCLUSTERED 
(
    [content_identifier] ASC
)WITH (PAD_INDEX  = OFF, STATISTICS_NORECOMPUTE  = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS  = ON, ALLOW_PAGE_LOCKS  = ON) ON [PRIMARY]
) ON [PRIMARY];
CREATE UNIQUE NONCLUSTERED INDEX [core_attachment_global_id_fk_key] ON [dbo].[core_attachment] 
(
    [GLOBAL_ID_FK] ASC
)
WHERE ([GLOBAL_ID_FK] IS NOT NULL)
WITH (PAD_INDEX  = OFF, STATISTICS_NORECOMPUTE  = OFF, SORT_IN_TEMPDB = OFF, IGNORE_DUP_KEY = OFF, DROP_EXISTING = OFF, ONLINE = OFF, ALLOW_ROW_LOCKS  = ON, ALLOW_PAGE_LOCKS  = ON) ON [PRIMARY];
/****** Object:  Table [dbo].[user_note_entity]    Script Date: 06/17/2016 17:45:08 ******/
SET ANSI_NULLS ON;
SET QUOTED_IDENTIFIER ON;
CREATE TABLE [dbo].[user_note_entity](
    [ID] [bigint] IDENTITY(1,1) NOT NULL,
    [rank] [bigint] NULL,
    [NOTE_FK] [bigint] NOT NULL,
    [USER_FK] [bigint] NOT NULL,
 CONSTRAINT [PK_USER_NOTE_ENTITY] PRIMARY KEY CLUSTERED 
(
    [ID] ASC
)WITH (PAD_INDEX  = OFF, STATISTICS_NORECOMPUTE  = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS  = ON, ALLOW_PAGE_LOCKS  = ON) ON [PRIMARY],
 CONSTRAINT [user_note_entity_fk_idx] UNIQUE NONCLUSTERED 
(
    [NOTE_FK] ASC,
    [USER_FK] ASC
)WITH (PAD_INDEX  = OFF, STATISTICS_NORECOMPUTE  = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS  = ON, ALLOW_PAGE_LOCKS  = ON) ON [PRIMARY]
) ON [PRIMARY];
/****** Object:  Table [dbo].[user_note_property]    Script Date: 06/17/2016 17:45:08 ******/
SET ANSI_NULLS ON;
SET QUOTED_IDENTIFIER ON;
CREATE TABLE [dbo].[user_note_property](
    [ID] [bigint] IDENTITY(1,1) NOT NULL,
    [property_value] [nvarchar](1024) NOT NULL,
    [property_key] [nvarchar](128) NOT NULL,
    [key_group] [nvarchar](128) NOT NULL,
    [last_modification_date] [datetime2](7) NULL,
    [USER_FK] [bigint] NOT NULL,
    [NOTE_FK] [bigint] NOT NULL,
    [user_note_entity_fk] [bigint] NULL,
PRIMARY KEY CLUSTERED 
(
    [ID] ASC
)WITH (PAD_INDEX  = OFF, STATISTICS_NORECOMPUTE  = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS  = ON, ALLOW_PAGE_LOCKS  = ON) ON [PRIMARY],
 CONSTRAINT [us_no_prop_constr] UNIQUE NONCLUSTERED 
(
    [property_key] ASC,
    [key_group] ASC,
    [USER_FK] ASC,
    [NOTE_FK] ASC
)WITH (PAD_INDEX  = OFF, STATISTICS_NORECOMPUTE  = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS  = ON, ALLOW_PAGE_LOCKS  = ON) ON [PRIMARY]
) ON [PRIMARY];
CREATE NONCLUSTERED INDEX [note_fk_idx] ON [dbo].[user_note_property] 
(
    [NOTE_FK] ASC
)WITH (PAD_INDEX  = OFF, STATISTICS_NORECOMPUTE  = OFF, SORT_IN_TEMPDB = OFF, IGNORE_DUP_KEY = OFF, DROP_EXISTING = OFF, ONLINE = OFF, ALLOW_ROW_LOCKS  = ON, ALLOW_PAGE_LOCKS  = ON) ON [PRIMARY];
CREATE NONCLUSTERED INDEX [user_fk_idx] ON [dbo].[user_note_property] 
(
    [USER_FK] ASC
)WITH (PAD_INDEX  = OFF, STATISTICS_NORECOMPUTE  = OFF, SORT_IN_TEMPDB = OFF, IGNORE_DUP_KEY = OFF, DROP_EXISTING = OFF, ONLINE = OFF, ALLOW_ROW_LOCKS  = ON, ALLOW_PAGE_LOCKS  = ON) ON [PRIMARY];
CREATE NONCLUSTERED INDEX [user_note_prop_note_fk_idx] ON [dbo].[user_note_property] 
(
    [NOTE_FK] ASC
)WITH (PAD_INDEX  = OFF, STATISTICS_NORECOMPUTE  = OFF, SORT_IN_TEMPDB = OFF, IGNORE_DUP_KEY = OFF, DROP_EXISTING = OFF, ONLINE = OFF, ALLOW_ROW_LOCKS  = ON, ALLOW_PAGE_LOCKS  = ON) ON [PRIMARY];
/****** Object:  Table [dbo].[core_attachment_property]    Script Date: 06/17/2016 17:45:08 ******/
SET ANSI_NULLS ON;
SET QUOTED_IDENTIFIER ON;
CREATE TABLE [dbo].[core_attachment_property](
    [key_group] [nvarchar](128) NOT NULL,
    [property_key] [nvarchar](128) NOT NULL,
    [property_value] [nvarchar](4000) NOT NULL,
    [ATTACHMENT_FK] [bigint] NULL,
    [last_modification_date] [datetime2](7) NULL,
    [ID] [bigint] IDENTITY(1,1) NOT NULL,
PRIMARY KEY CLUSTERED 
(
    [ID] ASC
)WITH (PAD_INDEX  = OFF, STATISTICS_NORECOMPUTE  = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS  = ON, ALLOW_PAGE_LOCKS  = ON) ON [PRIMARY]
) ON [PRIMARY];
/****** Object:  Default [DF_core_plugin_properties_application_property]    Script Date: 06/17/2016 17:45:08 ******/
ALTER TABLE [dbo].[core_plugin_properties] ADD  CONSTRAINT [DF_core_plugin_properties_application_property]  DEFAULT ((0)) FOR [application_property];
/****** Object:  Default [DF_core_tag_tag_store_alias]    Script Date: 06/17/2016 17:45:08 ******/
ALTER TABLE [dbo].[core_tag] ADD  CONSTRAINT [DF_core_tag_tag_store_alias]  DEFAULT ('DefaultNoteTagStore') FOR [tag_store_alias];
/****** Object:  Default [DF_core_blog_toplevel_topic]    Script Date: 06/17/2016 17:45:08 ******/
ALTER TABLE [dbo].[core_blog] ADD  CONSTRAINT [DF_core_blog_toplevel_topic]  DEFAULT ((0)) FOR [toplevel_topic];
/****** Object:  Default [DF_core_note_mention_topic_readers]    Script Date: 06/17/2016 17:45:08 ******/
ALTER TABLE [dbo].[core_note] ADD  CONSTRAINT [DF_core_note_mention_topic_readers]  DEFAULT ((0)) FOR [mention_topic_readers];
/****** Object:  Default [DF_core_note_mention_topic_authors]    Script Date: 06/17/2016 17:45:08 ******/
ALTER TABLE [dbo].[core_note] ADD  CONSTRAINT [DF_core_note_mention_topic_authors]  DEFAULT ((0)) FOR [mention_topic_authors];
/****** Object:  Default [DF_core_note_mention_topic_managers]    Script Date: 06/17/2016 17:45:08 ******/
ALTER TABLE [dbo].[core_note] ADD  CONSTRAINT [DF_core_note_mention_topic_managers]  DEFAULT ((0)) FOR [mention_topic_managers];
/****** Object:  Default [DF_core_note_mention_discussion_authors]    Script Date: 06/17/2016 17:45:08 ******/
ALTER TABLE [dbo].[core_note] ADD  CONSTRAINT [DF_core_note_mention_discussion_authors]  DEFAULT ((0)) FOR [mention_discussion_authors];
/****** Object:  Default [DF_configuration_ldap_dynamic_mode]    Script Date: 06/17/2016 17:45:08 ******/
ALTER TABLE [dbo].[configuration_ldap] ADD  CONSTRAINT [DF_configuration_ldap_dynamic_mode]  DEFAULT ((0)) FOR [dynamic_mode];
/****** Object:  ForeignKey [user_entity_GLOBAL_ID_FKC]    Script Date: 06/17/2016 17:45:08 ******/
ALTER TABLE [dbo].[user_entity]  WITH CHECK ADD  CONSTRAINT [user_entity_GLOBAL_ID_FKC] FOREIGN KEY([GLOBAL_ID_FK])
REFERENCES [dbo].[core_global_id] ([ID]);
ALTER TABLE [dbo].[user_entity] CHECK CONSTRAINT [user_entity_GLOBAL_ID_FKC];
/****** Object:  ForeignKey [user_contact_COUNTRY_FKC]    Script Date: 06/17/2016 17:45:08 ******/
ALTER TABLE [dbo].[user_contact]  WITH CHECK ADD  CONSTRAINT [user_contact_COUNTRY_FKC] FOREIGN KEY([COUNTRY_FK])
REFERENCES [dbo].[md_country] ([ID]);
ALTER TABLE [dbo].[user_contact] CHECK CONSTRAINT [user_contact_COUNTRY_FKC];
/****** Object:  ForeignKey [mc_config_NOTIFICATION_CONFIGC]    Script Date: 06/17/2016 17:45:08 ******/
ALTER TABLE [dbo].[mc_config]  WITH CHECK ADD  CONSTRAINT [mc_config_NOTIFICATION_CONFIGC] FOREIGN KEY([NOTIFICATION_CONFIG_FK])
REFERENCES [dbo].[notification_config] ([ID]);
ALTER TABLE [dbo].[mc_config] CHECK CONSTRAINT [mc_config_NOTIFICATION_CONFIGC];
/****** Object:  ForeignKey [ip_range_filter_C_ex]    Script Date: 06/17/2016 17:45:08 ******/
ALTER TABLE [dbo].[iprange_range]  WITH CHECK ADD  CONSTRAINT [ip_range_filter_C_ex] FOREIGN KEY([IP_RANGE_FILTER_EX_FK])
REFERENCES [dbo].[iprange_filter] ([ID]);
ALTER TABLE [dbo].[iprange_range] CHECK CONSTRAINT [ip_range_filter_C_ex];
/****** Object:  ForeignKey [ip_range_filter_C_in]    Script Date: 06/17/2016 17:45:08 ******/
ALTER TABLE [dbo].[iprange_range]  WITH CHECK ADD  CONSTRAINT [ip_range_filter_C_in] FOREIGN KEY([IP_RANGE_FILTER_IN_FK])
REFERENCES [dbo].[iprange_filter] ([ID]);
ALTER TABLE [dbo].[iprange_range] CHECK CONSTRAINT [ip_range_filter_C_in];
/****** Object:  ForeignKey [iprange_channel_IP_RANGE_FILTC]    Script Date: 06/17/2016 17:45:08 ******/
ALTER TABLE [dbo].[iprange_filter_channel]  WITH CHECK ADD  CONSTRAINT [iprange_channel_IP_RANGE_FILTC] FOREIGN KEY([IP_RANGE_FILTERS_FK])
REFERENCES [dbo].[iprange_filter] ([ID]);
ALTER TABLE [dbo].[iprange_filter_channel] CHECK CONSTRAINT [iprange_channel_IP_RANGE_FILTC];
/****** Object:  ForeignKey [iprange_filter_CHANNELS_FKC]    Script Date: 06/17/2016 17:45:08 ******/
ALTER TABLE [dbo].[iprange_filter_channel]  WITH CHECK ADD  CONSTRAINT [iprange_filter_CHANNELS_FKC] FOREIGN KEY([CHANNELS_FK])
REFERENCES [dbo].[iprange_channel] ([TYPE]);
ALTER TABLE [dbo].[iprange_filter_channel] CHECK CONSTRAINT [iprange_filter_CHANNELS_FKC];
/****** Object:  ForeignKey [custom_messages_language_fkc]    Script Date: 06/17/2016 17:45:08 ******/
ALTER TABLE [dbo].[custom_messages]  WITH CHECK ADD  CONSTRAINT [custom_messages_language_fkc] FOREIGN KEY([LANGUAGE_FK])
REFERENCES [dbo].[md_language] ([ID]);
ALTER TABLE [dbo].[custom_messages] CHECK CONSTRAINT [custom_messages_language_fkc];
/****** Object:  ForeignKey [core_task_props_TASK_FKC]    Script Date: 06/17/2016 17:45:08 ******/
ALTER TABLE [dbo].[core_task_props]  WITH CHECK ADD  CONSTRAINT [core_task_props_TASK_FKC] FOREIGN KEY([TASK_FK])
REFERENCES [dbo].[core_task] ([ID]);
ALTER TABLE [dbo].[core_task_props] CHECK CONSTRAINT [core_task_props_TASK_FKC];
/****** Object:  ForeignKey [core_task_execs_TASK_FKC]    Script Date: 06/17/2016 17:45:08 ******/
ALTER TABLE [dbo].[core_task_execs]  WITH CHECK ADD  CONSTRAINT [core_task_execs_TASK_FKC] FOREIGN KEY([TASK_FK])
REFERENCES [dbo].[core_task] ([ID]);
ALTER TABLE [dbo].[core_task_execs] CHECK CONSTRAINT [core_task_execs_TASK_FKC];
/****** Object:  ForeignKey [CATEGORIZED_TAG_CATEGORY_FKC]    Script Date: 06/17/2016 17:45:08 ******/
ALTER TABLE [dbo].[core_tag]  WITH CHECK ADD  CONSTRAINT [CATEGORIZED_TAG_CATEGORY_FKC] FOREIGN KEY([CATEGORY_FK])
REFERENCES [dbo].[core_tag_category] ([ID]);
ALTER TABLE [dbo].[core_tag] CHECK CONSTRAINT [CATEGORIZED_TAG_CATEGORY_FKC];
/****** Object:  ForeignKey [core_tag_GLOBAL_ID_FKC]    Script Date: 06/17/2016 17:45:08 ******/
ALTER TABLE [dbo].[core_tag]  WITH CHECK ADD  CONSTRAINT [core_tag_GLOBAL_ID_FKC] FOREIGN KEY([GLOBAL_ID_FK])
REFERENCES [dbo].[core_global_id] ([ID]);
ALTER TABLE [dbo].[core_tag] CHECK CONSTRAINT [core_tag_GLOBAL_ID_FKC];
/****** Object:  ForeignKey [configuration_ldap_sbase_LDAPC]    Script Date: 06/17/2016 17:45:08 ******/
ALTER TABLE [dbo].[configuration_ldap_sbase]  WITH CHECK ADD  CONSTRAINT [configuration_ldap_sbase_LDAPC] FOREIGN KEY([LDAP_SEARCH_CONFIGURATION_FK])
REFERENCES [dbo].[configuration_ldap_search] ([ID]);
ALTER TABLE [dbo].[configuration_ldap_sbase] CHECK CONSTRAINT [configuration_ldap_sbase_LDAPC];
/****** Object:  ForeignKey [configuration_ldap_group_GROUC]    Script Date: 06/17/2016 17:45:08 ******/
ALTER TABLE [dbo].[configuration_ldap_group]  WITH CHECK ADD  CONSTRAINT [configuration_ldap_group_GROUC] FOREIGN KEY([GROUP_SEARCH_FK])
REFERENCES [dbo].[configuration_ldap_search] ([ID]);
ALTER TABLE [dbo].[configuration_ldap_group] CHECK CONSTRAINT [configuration_ldap_group_GROUC];
/****** Object:  ForeignKey [core_blog_GLOBAL_ID_FKC]    Script Date: 06/17/2016 17:45:08 ******/
ALTER TABLE [dbo].[core_blog]  WITH CHECK ADD  CONSTRAINT [core_blog_GLOBAL_ID_FKC] FOREIGN KEY([GLOBAL_ID_FK])
REFERENCES [dbo].[core_global_id] ([ID]);
ALTER TABLE [dbo].[core_blog] CHECK CONSTRAINT [core_blog_GLOBAL_ID_FKC];
/****** Object:  ForeignKey [user_profile_CONTACT_FKC]    Script Date: 06/17/2016 17:45:08 ******/
ALTER TABLE [dbo].[user_profile]  WITH CHECK ADD  CONSTRAINT [user_profile_CONTACT_FKC] FOREIGN KEY([CONTACT_FK])
REFERENCES [dbo].[user_contact] ([ID]);
ALTER TABLE [dbo].[user_profile] CHECK CONSTRAINT [user_profile_CONTACT_FKC];
/****** Object:  ForeignKey [user_profile_LARGE_IMAGE_FKC]    Script Date: 06/17/2016 17:45:08 ******/
ALTER TABLE [dbo].[user_profile]  WITH CHECK ADD  CONSTRAINT [user_profile_LARGE_IMAGE_FKC] FOREIGN KEY([LARGE_IMAGE_FK])
REFERENCES [dbo].[user_image] ([ID]);
ALTER TABLE [dbo].[user_profile] CHECK CONSTRAINT [user_profile_LARGE_IMAGE_FKC];
/****** Object:  ForeignKey [user_profile_MEDIUM_IMAGE_FKC]    Script Date: 06/17/2016 17:45:08 ******/
ALTER TABLE [dbo].[user_profile]  WITH CHECK ADD  CONSTRAINT [user_profile_MEDIUM_IMAGE_FKC] FOREIGN KEY([MEDIUM_IMAGE_FK])
REFERENCES [dbo].[user_image] ([ID]);
ALTER TABLE [dbo].[user_profile] CHECK CONSTRAINT [user_profile_MEDIUM_IMAGE_FKC];
/****** Object:  ForeignKey [user_profile_NOTIFICATION_CONC]    Script Date: 06/17/2016 17:45:08 ******/
ALTER TABLE [dbo].[user_profile]  WITH CHECK ADD  CONSTRAINT [user_profile_NOTIFICATION_CONC] FOREIGN KEY([NOTIFICATION_CONFIG_FK])
REFERENCES [dbo].[notification_config] ([ID]);
ALTER TABLE [dbo].[user_profile] CHECK CONSTRAINT [user_profile_NOTIFICATION_CONC];
/****** Object:  ForeignKey [user_profile_SMALL_IMAGE_FKC]    Script Date: 06/17/2016 17:45:08 ******/
ALTER TABLE [dbo].[user_profile]  WITH CHECK ADD  CONSTRAINT [user_profile_SMALL_IMAGE_FKC] FOREIGN KEY([SMALL_IMAGE_FK])
REFERENCES [dbo].[user_image] ([ID]);
ALTER TABLE [dbo].[user_profile] CHECK CONSTRAINT [user_profile_SMALL_IMAGE_FKC];
/****** Object:  ForeignKey [core_blog_CHILDREN_FKC]    Script Date: 06/17/2016 17:45:08 ******/
ALTER TABLE [dbo].[core_blog2blog]  WITH CHECK ADD  CONSTRAINT [core_blog_CHILDREN_FKC] FOREIGN KEY([CHILDREN_FK])
REFERENCES [dbo].[core_blog] ([ID]);
ALTER TABLE [dbo].[core_blog2blog] CHECK CONSTRAINT [core_blog_CHILDREN_FKC];
/****** Object:  ForeignKey [core_blog_PARENTS_FKC]    Script Date: 06/17/2016 17:45:08 ******/
ALTER TABLE [dbo].[core_blog2blog]  WITH CHECK ADD  CONSTRAINT [core_blog_PARENTS_FKC] FOREIGN KEY([PARENTS_FK])
REFERENCES [dbo].[core_blog] ([ID]);
ALTER TABLE [dbo].[core_blog2blog] CHECK CONSTRAINT [core_blog_PARENTS_FKC];
/****** Object:  ForeignKey [core_blog_property_BLOG_FKC]    Script Date: 06/17/2016 17:45:08 ******/
ALTER TABLE [dbo].[core_blog_property]  WITH CHECK ADD  CONSTRAINT [core_blog_property_BLOG_FKC] FOREIGN KEY([BLOG_FK])
REFERENCES [dbo].[core_blog] ([ID]);
ALTER TABLE [dbo].[core_blog_property] CHECK CONSTRAINT [core_blog_property_BLOG_FKC];
/****** Object:  ForeignKey [core_blog_member_BLOG_FKC]    Script Date: 06/17/2016 17:45:08 ******/
ALTER TABLE [dbo].[core_blog_member]  WITH CHECK ADD  CONSTRAINT [core_blog_member_BLOG_FKC] FOREIGN KEY([BLOG_FK])
REFERENCES [dbo].[core_blog] ([ID]);
ALTER TABLE [dbo].[core_blog_member] CHECK CONSTRAINT [core_blog_member_BLOG_FKC];
/****** Object:  ForeignKey [core_blog_member_KENMEI_ENTITC]    Script Date: 06/17/2016 17:45:08 ******/
ALTER TABLE [dbo].[core_blog_member]  WITH CHECK ADD  CONSTRAINT [core_blog_member_KENMEI_ENTITC] FOREIGN KEY([KENMEI_ENTITY_FK])
REFERENCES [dbo].[user_entity] ([ID]);
ALTER TABLE [dbo].[core_blog_member] CHECK CONSTRAINT [core_blog_member_KENMEI_ENTITC];
/****** Object:  ForeignKey [core_blog_TAGS_FKC]    Script Date: 06/17/2016 17:45:08 ******/
ALTER TABLE [dbo].[core_blog2tag]  WITH CHECK ADD  CONSTRAINT [core_blog_TAGS_FKC] FOREIGN KEY([TAGS_FK])
REFERENCES [dbo].[core_tag] ([ID]);
ALTER TABLE [dbo].[core_blog2tag] CHECK CONSTRAINT [core_blog_TAGS_FKC];
/****** Object:  ForeignKey [core_tag_BLOGS_FKC]    Script Date: 06/17/2016 17:45:08 ******/
ALTER TABLE [dbo].[core_blog2tag]  WITH CHECK ADD  CONSTRAINT [core_tag_BLOGS_FKC] FOREIGN KEY([BLOGS_FK])
REFERENCES [dbo].[core_blog] ([ID]);
ALTER TABLE [dbo].[core_blog2tag] CHECK CONSTRAINT [core_tag_BLOGS_FKC];
/****** Object:  ForeignKey [configuration_client_DEFAULT_C]    Script Date: 06/17/2016 17:45:08 ******/
ALTER TABLE [dbo].[configuration_client]  WITH CHECK ADD  CONSTRAINT [configuration_client_DEFAULT_C] FOREIGN KEY([DEFAULT_BLOG_FK])
REFERENCES [dbo].[core_blog] ([ID]);
ALTER TABLE [dbo].[configuration_client] CHECK CONSTRAINT [configuration_client_DEFAULT_C];
/****** Object:  ForeignKey [core_external_object_BLOG_FKC]    Script Date: 06/17/2016 17:45:08 ******/
ALTER TABLE [dbo].[core_external_object]  WITH CHECK ADD  CONSTRAINT [core_external_object_BLOG_FKC] FOREIGN KEY([BLOG_FK])
REFERENCES [dbo].[core_blog] ([ID]);
ALTER TABLE [dbo].[core_external_object] CHECK CONSTRAINT [core_external_object_BLOG_FKC];
/****** Object:  ForeignKey [core_tag_KENMEI_ENTITIES_FKC]    Script Date: 06/17/2016 17:45:08 ******/
ALTER TABLE [dbo].[core_entity2tags]  WITH CHECK ADD  CONSTRAINT [core_tag_KENMEI_ENTITIES_FKC] FOREIGN KEY([KENMEI_ENTITIES_FK])
REFERENCES [dbo].[user_entity] ([ID]);
ALTER TABLE [dbo].[core_entity2tags] CHECK CONSTRAINT [core_tag_KENMEI_ENTITIES_FKC];
/****** Object:  ForeignKey [user_entity_TAGS_FKC]    Script Date: 06/17/2016 17:45:08 ******/
ALTER TABLE [dbo].[core_entity2tags]  WITH CHECK ADD  CONSTRAINT [user_entity_TAGS_FKC] FOREIGN KEY([TAGS_FK])
REFERENCES [dbo].[core_tag] ([ID]);
ALTER TABLE [dbo].[core_entity2tags] CHECK CONSTRAINT [user_entity_TAGS_FKC];
/****** Object:  ForeignKey [core_tag_NAMES_FKC]    Script Date: 06/17/2016 17:45:08 ******/
ALTER TABLE [dbo].[core_tag2names]  WITH CHECK ADD  CONSTRAINT [core_tag_NAMES_FKC] FOREIGN KEY([NAMES_FK])
REFERENCES [dbo].[custom_messages] ([ID]);
ALTER TABLE [dbo].[core_tag2names] CHECK CONSTRAINT [core_tag_NAMES_FKC];
/****** Object:  ForeignKey [custom_messages_TAGS_FKC]    Script Date: 06/17/2016 17:45:08 ******/
ALTER TABLE [dbo].[core_tag2names]  WITH CHECK ADD  CONSTRAINT [custom_messages_TAGS_FKC] FOREIGN KEY([TAGS_FK])
REFERENCES [dbo].[core_tag] ([ID]);
ALTER TABLE [dbo].[core_tag2names] CHECK CONSTRAINT [custom_messages_TAGS_FKC];
/****** Object:  ForeignKey [core_tag_DESCRIPTIONS_FKC]    Script Date: 06/17/2016 17:45:08 ******/
ALTER TABLE [dbo].[core_tag2descriptions]  WITH CHECK ADD  CONSTRAINT [core_tag_DESCRIPTIONS_FKC] FOREIGN KEY([DESCRIPTIONS_FK])
REFERENCES [dbo].[custom_messages] ([ID]);
ALTER TABLE [dbo].[core_tag2descriptions] CHECK CONSTRAINT [core_tag_DESCRIPTIONS_FKC];
/****** Object:  ForeignKey [custom_messages_TAGS_DESC_FKC]    Script Date: 06/17/2016 17:45:08 ******/
ALTER TABLE [dbo].[core_tag2descriptions]  WITH CHECK ADD  CONSTRAINT [custom_messages_TAGS_DESC_FKC] FOREIGN KEY([TAGS_FK])
REFERENCES [dbo].[core_tag] ([ID]);
ALTER TABLE [dbo].[core_tag2descriptions] CHECK CONSTRAINT [custom_messages_TAGS_DESC_FKC];
/****** Object:  ForeignKey [user_groupIFKC]    Script Date: 06/17/2016 17:45:08 ******/
ALTER TABLE [dbo].[user_group]  WITH CHECK ADD  CONSTRAINT [user_groupIFKC] FOREIGN KEY([ID])
REFERENCES [dbo].[user_entity] ([ID]);
ALTER TABLE [dbo].[user_group] CHECK CONSTRAINT [user_groupIFKC];
/****** Object:  ForeignKey [user_user_PROFILE_FKC]    Script Date: 06/17/2016 17:45:08 ******/
ALTER TABLE [dbo].[user_user]  WITH CHECK ADD  CONSTRAINT [user_user_PROFILE_FKC] FOREIGN KEY([PROFILE_FK])
REFERENCES [dbo].[user_profile] ([ID]);
ALTER TABLE [dbo].[user_user] CHECK CONSTRAINT [user_user_PROFILE_FKC];
/****** Object:  ForeignKey [user_userIFKC]    Script Date: 06/17/2016 17:45:08 ******/
ALTER TABLE [dbo].[user_user]  WITH CHECK ADD  CONSTRAINT [user_userIFKC] FOREIGN KEY([ID])
REFERENCES [dbo].[user_entity] ([ID]);
ALTER TABLE [dbo].[user_user] CHECK CONSTRAINT [user_userIFKC];
/****** Object:  ForeignKey [core_role2blog_GRANTING_GROUPC]    Script Date: 06/17/2016 17:45:08 ******/
ALTER TABLE [dbo].[core_role2blog_granting_group]  WITH CHECK ADD  CONSTRAINT [core_role2blog_GRANTING_GROUPC] FOREIGN KEY([GRANTING_GROUPS_FK])
REFERENCES [dbo].[user_group] ([ID]);
ALTER TABLE [dbo].[core_role2blog_granting_group] CHECK CONSTRAINT [core_role2blog_GRANTING_GROUPC];
/****** Object:  ForeignKey [user_group_USER_TO_BLOG_ROLE_C]    Script Date: 06/17/2016 17:45:08 ******/
ALTER TABLE [dbo].[core_role2blog_granting_group]  WITH CHECK ADD  CONSTRAINT [user_group_USER_TO_BLOG_ROLE_C] FOREIGN KEY([USER_TO_BLOG_ROLE_MAPPINGS_FK])
REFERENCES [dbo].[core_role2blog] ([ID]);
ALTER TABLE [dbo].[core_role2blog_granting_group] CHECK CONSTRAINT [user_group_USER_TO_BLOG_ROLE_C];
/****** Object:  ForeignKey [core_external_object_propertiC]    Script Date: 06/17/2016 17:45:08 ******/
ALTER TABLE [dbo].[core_external_object_prop]  WITH CHECK ADD  CONSTRAINT [core_external_object_propertiC] FOREIGN KEY([EXTERNAL_OBJECT_FK])
REFERENCES [dbo].[core_external_object] ([ID]);
ALTER TABLE [dbo].[core_external_object_prop] CHECK CONSTRAINT [core_external_object_propertiC];
/****** Object:  ForeignKey [configuration_CLIENT_CONFIG_FC]    Script Date: 06/17/2016 17:45:08 ******/
ALTER TABLE [dbo].[configuration]  WITH CHECK ADD  CONSTRAINT [configuration_CLIENT_CONFIG_FC] FOREIGN KEY([CLIENT_CONFIG_FK])
REFERENCES [dbo].[configuration_client] ([ID]);
ALTER TABLE [dbo].[configuration] CHECK CONSTRAINT [configuration_CLIENT_CONFIG_FC];
/****** Object:  ForeignKey [user_group_property_KENMEI_ENC]    Script Date: 06/17/2016 17:45:08 ******/
ALTER TABLE [dbo].[user_group_property]  WITH CHECK ADD  CONSTRAINT [user_group_property_KENMEI_ENC] FOREIGN KEY([KENMEI_ENTITY_GROUP_FK])
REFERENCES [dbo].[user_group] ([ID]);
ALTER TABLE [dbo].[user_group_property] CHECK CONSTRAINT [user_group_property_KENMEI_ENC];
/****** Object:  ForeignKey [user_entity_GROUPS_FKC]    Script Date: 06/17/2016 17:45:08 ******/
ALTER TABLE [dbo].[user_group_member]  WITH CHECK ADD  CONSTRAINT [user_entity_GROUPS_FKC] FOREIGN KEY([GROUPS_FK])
REFERENCES [dbo].[user_group] ([ID]);
ALTER TABLE [dbo].[user_group_member] CHECK CONSTRAINT [user_entity_GROUPS_FKC];
/****** Object:  ForeignKey [user_group_GROUP_MEMBERS_FKC]    Script Date: 06/17/2016 17:45:08 ******/
ALTER TABLE [dbo].[user_group_member]  WITH CHECK ADD  CONSTRAINT [user_group_GROUP_MEMBERS_FKC] FOREIGN KEY([GROUP_MEMBERS_FK])
REFERENCES [dbo].[user_entity] ([ID]);
ALTER TABLE [dbo].[user_group_member] CHECK CONSTRAINT [user_group_GROUP_MEMBERS_FKC];
/****** Object:  ForeignKey [user_group_externalIFKC]    Script Date: 06/17/2016 17:45:08 ******/
ALTER TABLE [dbo].[user_group_external]  WITH CHECK ADD  CONSTRAINT [user_group_externalIFKC] FOREIGN KEY([ID])
REFERENCES [dbo].[user_group] ([ID]);
ALTER TABLE [dbo].[user_group_external] CHECK CONSTRAINT [user_group_externalIFKC];
/****** Object:  ForeignKey [user_user_property_KENMEI_USEC]    Script Date: 06/17/2016 17:45:08 ******/
ALTER TABLE [dbo].[user_user_property]  WITH CHECK ADD  CONSTRAINT [user_user_property_KENMEI_USEC] FOREIGN KEY([KENMEI_USER_FK])
REFERENCES [dbo].[user_user] ([ID]);
ALTER TABLE [dbo].[user_user_property] CHECK CONSTRAINT [user_user_property_KENMEI_USEC];
/****** Object:  ForeignKey [user_navigation_item_OWNER_FKC]    Script Date: 06/17/2016 17:45:08 ******/
ALTER TABLE [dbo].[user_navigation_item]  WITH CHECK ADD  CONSTRAINT [user_navigation_item_OWNER_FKC] FOREIGN KEY([OWNER_FK])
REFERENCES [dbo].[user_user] ([ID]);
ALTER TABLE [dbo].[user_navigation_item] CHECK CONSTRAINT [user_navigation_item_OWNER_FKC];
/****** Object:  ForeignKey [user_of_group_GROUP_FKC]    Script Date: 06/17/2016 17:45:08 ******/
ALTER TABLE [dbo].[user_of_group]  WITH CHECK ADD  CONSTRAINT [user_of_group_GROUP_FKC] FOREIGN KEY([GROUP_FK])
REFERENCES [dbo].[user_group] ([ID]);
ALTER TABLE [dbo].[user_of_group] CHECK CONSTRAINT [user_of_group_GROUP_FKC];
/****** Object:  ForeignKey [user_of_group_USER_FKC]    Script Date: 06/17/2016 17:45:08 ******/
ALTER TABLE [dbo].[user_of_group]  WITH CHECK ADD  CONSTRAINT [user_of_group_USER_FKC] FOREIGN KEY([USER_FK])
REFERENCES [dbo].[user_user] ([ID]);
ALTER TABLE [dbo].[user_of_group] CHECK CONSTRAINT [user_of_group_USER_FKC];
/****** Object:  ForeignKey [core_global_id_kenmei_users_fC]    Script Date: 06/17/2016 17:45:08 ******/
ALTER TABLE [dbo].[core_user2follows]  WITH CHECK ADD  CONSTRAINT [core_global_id_kenmei_users_fC] FOREIGN KEY([kenmei_users_fk])
REFERENCES [dbo].[user_user] ([ID]);
ALTER TABLE [dbo].[core_user2follows] CHECK CONSTRAINT [core_global_id_kenmei_users_fC];
/****** Object:  ForeignKey [user_user_FOLLOWED_ITEMS_FKC]    Script Date: 06/17/2016 17:45:08 ******/
ALTER TABLE [dbo].[core_user2follows]  WITH CHECK ADD  CONSTRAINT [user_user_FOLLOWED_ITEMS_FKC] FOREIGN KEY([FOLLOWED_ITEMS_FK])
REFERENCES [dbo].[core_global_id] ([ID]);
ALTER TABLE [dbo].[core_user2follows] CHECK CONSTRAINT [user_user_FOLLOWED_ITEMS_FKC];
/****** Object:  ForeignKey [configuration_setting_CONFIGUC]    Script Date: 06/17/2016 17:45:08 ******/
ALTER TABLE [dbo].[configuration_setting]  WITH CHECK ADD  CONSTRAINT [configuration_setting_CONFIGUC] FOREIGN KEY([CONFIGURATION_FK])
REFERENCES [dbo].[configuration] ([ID]);
ALTER TABLE [dbo].[configuration_setting] CHECK CONSTRAINT [configuration_setting_CONFIGUC];
/****** Object:  ForeignKey [configuration_external_systemC]    Script Date: 06/17/2016 17:45:08 ******/
ALTER TABLE [dbo].[configuration_external_sys]  WITH CHECK ADD  CONSTRAINT [configuration_external_systemC] FOREIGN KEY([CONFIGURATION_FK])
REFERENCES [dbo].[configuration] ([ID]);
ALTER TABLE [dbo].[configuration_external_sys] CHECK CONSTRAINT [configuration_external_systemC];
/****** Object:  ForeignKey [core_note_BLOG_FKC]    Script Date: 06/17/2016 17:45:08 ******/
ALTER TABLE [dbo].[core_note]  WITH CHECK ADD  CONSTRAINT [core_note_BLOG_FKC] FOREIGN KEY([BLOG_FK])
REFERENCES [dbo].[core_blog] ([ID]);
ALTER TABLE [dbo].[core_note] CHECK CONSTRAINT [core_note_BLOG_FKC];
/****** Object:  ForeignKey [core_note_CONTENT_FKC]    Script Date: 06/17/2016 17:45:08 ******/
ALTER TABLE [dbo].[core_note]  WITH CHECK ADD  CONSTRAINT [core_note_CONTENT_FKC] FOREIGN KEY([CONTENT_FK])
REFERENCES [dbo].[core_content] ([ID]);
ALTER TABLE [dbo].[core_note] CHECK CONSTRAINT [core_note_CONTENT_FKC];
/****** Object:  ForeignKey [core_note_GLOBAL_ID_FKC]    Script Date: 06/17/2016 17:45:08 ******/
ALTER TABLE [dbo].[core_note]  WITH CHECK ADD  CONSTRAINT [core_note_GLOBAL_ID_FKC] FOREIGN KEY([GLOBAL_ID_FK])
REFERENCES [dbo].[core_global_id] ([ID]);
ALTER TABLE [dbo].[core_note] CHECK CONSTRAINT [core_note_GLOBAL_ID_FKC];
/****** Object:  ForeignKey [core_note_ORIGIN_FKC]    Script Date: 06/17/2016 17:45:08 ******/
ALTER TABLE [dbo].[core_note]  WITH CHECK ADD  CONSTRAINT [core_note_ORIGIN_FKC] FOREIGN KEY([ORIGIN_FK])
REFERENCES [dbo].[core_note] ([ID]);
ALTER TABLE [dbo].[core_note] CHECK CONSTRAINT [core_note_ORIGIN_FKC];
/****** Object:  ForeignKey [core_note_PARENT_FKC]    Script Date: 06/17/2016 17:45:08 ******/
ALTER TABLE [dbo].[core_note]  WITH CHECK ADD  CONSTRAINT [core_note_PARENT_FKC] FOREIGN KEY([PARENT_FK])
REFERENCES [dbo].[core_note] ([ID]);
ALTER TABLE [dbo].[core_note] CHECK CONSTRAINT [core_note_PARENT_FKC];
/****** Object:  ForeignKey [core_note_USER_FKC]    Script Date: 06/17/2016 17:45:08 ******/
ALTER TABLE [dbo].[core_note]  WITH CHECK ADD  CONSTRAINT [core_note_USER_FKC] FOREIGN KEY([USER_FK])
REFERENCES [dbo].[user_user] ([ID]);
ALTER TABLE [dbo].[core_note] CHECK CONSTRAINT [core_note_USER_FKC];
/****** Object:  ForeignKey [security_code_KENMEI_USER_FKC]    Script Date: 06/17/2016 17:45:08 ******/
ALTER TABLE [dbo].[security_code]  WITH CHECK ADD  CONSTRAINT [security_code_KENMEI_USER_FKC] FOREIGN KEY([KENMEI_USER_FK])
REFERENCES [dbo].[user_user] ([ID]);
ALTER TABLE [dbo].[security_code] CHECK CONSTRAINT [security_code_KENMEI_USER_FKC];
/****** Object:  ForeignKey [security_user_auth_failed_staC]    Script Date: 06/17/2016 17:45:08 ******/
ALTER TABLE [dbo].[security_user_status]  WITH CHECK ADD  CONSTRAINT [security_user_auth_failed_staC] FOREIGN KEY([KENMEI_USER_FK])
REFERENCES [dbo].[user_user] ([ID]);
ALTER TABLE [dbo].[security_user_status] CHECK CONSTRAINT [security_user_auth_failed_staC];
/****** Object:  ForeignKey [user_external_auth_KENMEI_USEC]    Script Date: 06/17/2016 17:45:08 ******/
ALTER TABLE [dbo].[user_external_auth]  WITH CHECK ADD  CONSTRAINT [user_external_auth_KENMEI_USEC] FOREIGN KEY([KENMEI_USER_FK])
REFERENCES [dbo].[user_user] ([ID]);
ALTER TABLE [dbo].[user_external_auth] CHECK CONSTRAINT [user_external_auth_KENMEI_USEC];
/****** Object:  ForeignKey [user_authorities_KENMEI_USER_C]    Script Date: 06/17/2016 17:45:08 ******/
ALTER TABLE [dbo].[user_authorities]  WITH CHECK ADD  CONSTRAINT [user_authorities_KENMEI_USER_C] FOREIGN KEY([KENMEI_USER_FK])
REFERENCES [dbo].[user_user] ([ID]);
ALTER TABLE [dbo].[user_authorities] CHECK CONSTRAINT [user_authorities_KENMEI_USER_C];
/****** Object:  ForeignKey [security_user_unlock_codeIFKC]    Script Date: 06/17/2016 17:45:08 ******/
ALTER TABLE [dbo].[security_user_unlock_code]  WITH CHECK ADD  CONSTRAINT [security_user_unlock_codeIFKC] FOREIGN KEY([ID])
REFERENCES [dbo].[security_code] ([ID]);
ALTER TABLE [dbo].[security_user_unlock_code] CHECK CONSTRAINT [security_user_unlock_codeIFKC];
/****** Object:  ForeignKey [security_user_codeIFKC]    Script Date: 06/17/2016 17:45:08 ******/
ALTER TABLE [dbo].[security_user_code]  WITH CHECK ADD  CONSTRAINT [security_user_codeIFKC] FOREIGN KEY([ID])
REFERENCES [dbo].[security_code] ([ID]);
ALTER TABLE [dbo].[security_user_code] CHECK CONSTRAINT [security_user_codeIFKC];
/****** Object:  ForeignKey [security_invite_clientIFKC]    Script Date: 06/17/2016 17:45:08 ******/
ALTER TABLE [dbo].[security_invite_client]  WITH CHECK ADD  CONSTRAINT [security_invite_clientIFKC] FOREIGN KEY([ID])
REFERENCES [dbo].[security_code] ([ID]);
ALTER TABLE [dbo].[security_invite_client] CHECK CONSTRAINT [security_invite_clientIFKC];
/****** Object:  ForeignKey [security_invite_blogIFKC]    Script Date: 06/17/2016 17:45:08 ******/
ALTER TABLE [dbo].[security_invite_blog]  WITH CHECK ADD  CONSTRAINT [security_invite_blogIFKC] FOREIGN KEY([ID])
REFERENCES [dbo].[security_code] ([ID]);
ALTER TABLE [dbo].[security_invite_blog] CHECK CONSTRAINT [security_invite_blogIFKC];
/****** Object:  ForeignKey [security_forgotten_pw_codeIFKC]    Script Date: 06/17/2016 17:45:08 ******/
ALTER TABLE [dbo].[security_forgotten_pw_code]  WITH CHECK ADD  CONSTRAINT [security_forgotten_pw_codeIFKC] FOREIGN KEY([ID])
REFERENCES [dbo].[security_code] ([ID]);
ALTER TABLE [dbo].[security_forgotten_pw_code] CHECK CONSTRAINT [security_forgotten_pw_codeIFKC];
/****** Object:  ForeignKey [security_email_codeIFKC]    Script Date: 06/17/2016 17:45:08 ******/
ALTER TABLE [dbo].[security_email_code]  WITH CHECK ADD  CONSTRAINT [security_email_codeIFKC] FOREIGN KEY([ID])
REFERENCES [dbo].[security_code] ([ID]);
ALTER TABLE [dbo].[security_email_code] CHECK CONSTRAINT [security_email_codeIFKC];
/****** Object:  ForeignKey [core_note_FAVORITE_USERS_FKC]    Script Date: 06/17/2016 17:45:08 ******/
ALTER TABLE [dbo].[core_users2favorite_notes]  WITH CHECK ADD  CONSTRAINT [core_note_FAVORITE_USERS_FKC] FOREIGN KEY([FAVORITE_USERS_FK])
REFERENCES [dbo].[user_user] ([ID]);
ALTER TABLE [dbo].[core_users2favorite_notes] CHECK CONSTRAINT [core_note_FAVORITE_USERS_FKC];
/****** Object:  ForeignKey [user_user_FAVORITE_NOTES_FKC]    Script Date: 06/17/2016 17:45:08 ******/
ALTER TABLE [dbo].[core_users2favorite_notes]  WITH CHECK ADD  CONSTRAINT [user_user_FAVORITE_NOTES_FKC] FOREIGN KEY([FAVORITE_NOTES_FK])
REFERENCES [dbo].[core_note] ([ID]);
ALTER TABLE [dbo].[core_users2favorite_notes] CHECK CONSTRAINT [user_user_FAVORITE_NOTES_FKC];
/****** Object:  ForeignKey [core_note_USERS_TO_BE_NOTIFIEC]    Script Date: 06/17/2016 17:45:08 ******/
ALTER TABLE [dbo].[core_notes2user_to_notify]  WITH CHECK ADD  CONSTRAINT [core_note_USERS_TO_BE_NOTIFIEC] FOREIGN KEY([USERS_TO_BE_NOTIFIED_FK])
REFERENCES [dbo].[user_user] ([ID]);
ALTER TABLE [dbo].[core_notes2user_to_notify] CHECK CONSTRAINT [core_note_USERS_TO_BE_NOTIFIEC];
/****** Object:  ForeignKey [user_user_NOTES_FKC]    Script Date: 06/17/2016 17:45:08 ******/
ALTER TABLE [dbo].[core_notes2user_to_notify]  WITH CHECK ADD  CONSTRAINT [user_user_NOTES_FKC] FOREIGN KEY([NOTES_FK])
REFERENCES [dbo].[core_note] ([ID]);
ALTER TABLE [dbo].[core_notes2user_to_notify] CHECK CONSTRAINT [user_user_NOTES_FKC];
/****** Object:  ForeignKey [core_note_TAGS_FKC]    Script Date: 06/17/2016 17:45:08 ******/
ALTER TABLE [dbo].[core_notes2tag]  WITH CHECK ADD  CONSTRAINT [core_note_TAGS_FKC] FOREIGN KEY([TAGS_FK])
REFERENCES [dbo].[core_tag] ([ID]);
ALTER TABLE [dbo].[core_notes2tag] CHECK CONSTRAINT [core_note_TAGS_FKC];
/****** Object:  ForeignKey [core_tag_NOTES_FKC]    Script Date: 06/17/2016 17:45:08 ******/
ALTER TABLE [dbo].[core_notes2tag]  WITH CHECK ADD  CONSTRAINT [core_tag_NOTES_FKC] FOREIGN KEY([NOTES_FK])
REFERENCES [dbo].[core_note] ([ID]);
ALTER TABLE [dbo].[core_notes2tag] CHECK CONSTRAINT [core_tag_NOTES_FKC];
/****** Object:  ForeignKey [core_blog_NOTES_FKC]    Script Date: 06/17/2016 17:45:08 ******/
ALTER TABLE [dbo].[core_notes2crossblogs]  WITH CHECK ADD  CONSTRAINT [core_blog_NOTES_FKC] FOREIGN KEY([NOTES_FK])
REFERENCES [dbo].[core_note] ([ID]);
ALTER TABLE [dbo].[core_notes2crossblogs] CHECK CONSTRAINT [core_blog_NOTES_FKC];
/****** Object:  ForeignKey [core_note_CROSSPOST_BLOGS_FKC]    Script Date: 06/17/2016 17:45:08 ******/
ALTER TABLE [dbo].[core_notes2crossblogs]  WITH CHECK ADD  CONSTRAINT [core_note_CROSSPOST_BLOGS_FKC] FOREIGN KEY([CROSSPOST_BLOGS_FK])
REFERENCES [dbo].[core_blog] ([ID]);
ALTER TABLE [dbo].[core_notes2crossblogs] CHECK CONSTRAINT [core_note_CROSSPOST_BLOGS_FKC];
/****** Object:  ForeignKey [core_global_id_NOTES_FKC]    Script Date: 06/17/2016 17:45:08 ******/
ALTER TABLE [dbo].[core_note2followable]  WITH CHECK ADD  CONSTRAINT [core_global_id_NOTES_FKC] FOREIGN KEY([NOTES_FK])
REFERENCES [dbo].[core_note] ([ID]);
ALTER TABLE [dbo].[core_note2followable] CHECK CONSTRAINT [core_global_id_NOTES_FKC];
/****** Object:  ForeignKey [core_note_FOLLOWABLE_ITEMS_FKC]    Script Date: 06/17/2016 17:45:08 ******/
ALTER TABLE [dbo].[core_note2followable]  WITH CHECK ADD  CONSTRAINT [core_note_FOLLOWABLE_ITEMS_FKC] FOREIGN KEY([FOLLOWABLE_ITEMS_FK])
REFERENCES [dbo].[core_global_id] ([ID]);
ALTER TABLE [dbo].[core_note2followable] CHECK CONSTRAINT [core_note_FOLLOWABLE_ITEMS_FKC];
/****** Object:  ForeignKey [core_note_DIRECT_USERS_FKC]    Script Date: 06/17/2016 17:45:08 ******/
ALTER TABLE [dbo].[core_note2direct_user]  WITH CHECK ADD  CONSTRAINT [core_note_DIRECT_USERS_FKC] FOREIGN KEY([DIRECT_USERS_FK])
REFERENCES [dbo].[user_user] ([ID]);
ALTER TABLE [dbo].[core_note2direct_user] CHECK CONSTRAINT [core_note_DIRECT_USERS_FKC];
/****** Object:  ForeignKey [user_user_DIRECT_NOTES_FKC]    Script Date: 06/17/2016 17:45:08 ******/
ALTER TABLE [dbo].[core_note2direct_user]  WITH CHECK ADD  CONSTRAINT [user_user_DIRECT_NOTES_FKC] FOREIGN KEY([DIRECT_NOTES_FK])
REFERENCES [dbo].[core_note] ([ID]);
ALTER TABLE [dbo].[core_note2direct_user] CHECK CONSTRAINT [user_user_DIRECT_NOTES_FKC];
/****** Object:  ForeignKey [core_note_properties_NOTE_FKC]    Script Date: 06/17/2016 17:45:08 ******/
ALTER TABLE [dbo].[core_note_property]  WITH CHECK ADD  CONSTRAINT [core_note_properties_NOTE_FKC] FOREIGN KEY([NOTE_FK])
REFERENCES [dbo].[core_note] ([ID]);
ALTER TABLE [dbo].[core_note_property] CHECK CONSTRAINT [core_note_properties_NOTE_FKC];
/****** Object:  ForeignKey [configuration_ldap_GROUP_SYNCC]    Script Date: 06/17/2016 17:45:08 ******/
ALTER TABLE [dbo].[configuration_ldap]  WITH CHECK ADD  CONSTRAINT [configuration_ldap_GROUP_SYNCC] FOREIGN KEY([GROUP_SYNC_CONFIG_FK])
REFERENCES [dbo].[configuration_ldap_group] ([ID]);
ALTER TABLE [dbo].[configuration_ldap] CHECK CONSTRAINT [configuration_ldap_GROUP_SYNCC];
/****** Object:  ForeignKey [configuration_ldapIFKC]    Script Date: 06/17/2016 17:45:08 ******/
ALTER TABLE [dbo].[configuration_ldap]  WITH CHECK ADD  CONSTRAINT [configuration_ldapIFKC] FOREIGN KEY([ID])
REFERENCES [dbo].[configuration_external_sys] ([ID]);
ALTER TABLE [dbo].[configuration_ldap] CHECK CONSTRAINT [configuration_ldapIFKC];
/****** Object:  ForeignKey [configuration_confluenceIFKC]    Script Date: 06/17/2016 17:45:08 ******/
ALTER TABLE [dbo].[configuration_confluence]  WITH CHECK ADD  CONSTRAINT [configuration_confluenceIFKC] FOREIGN KEY([ID])
REFERENCES [dbo].[configuration_external_sys] ([ID]);
ALTER TABLE [dbo].[configuration_confluence] CHECK CONSTRAINT [configuration_confluenceIFKC];
/****** Object:  ForeignKey [core_attachment_GLOBAL_ID_FKC]    Script Date: 06/17/2016 17:45:08 ******/
ALTER TABLE [dbo].[core_attachment]  WITH CHECK ADD  CONSTRAINT [core_attachment_GLOBAL_ID_FKC] FOREIGN KEY([GLOBAL_ID_FK])
REFERENCES [dbo].[core_global_id] ([ID]);
ALTER TABLE [dbo].[core_attachment] CHECK CONSTRAINT [core_attachment_GLOBAL_ID_FKC];
/****** Object:  ForeignKey [core_attachment_NOTE_FKC]    Script Date: 06/17/2016 17:45:08 ******/
ALTER TABLE [dbo].[core_attachment]  WITH CHECK ADD  CONSTRAINT [core_attachment_NOTE_FKC] FOREIGN KEY([NOTE_FK])
REFERENCES [dbo].[core_note] ([ID]);
ALTER TABLE [dbo].[core_attachment] CHECK CONSTRAINT [core_attachment_NOTE_FKC];
/****** Object:  ForeignKey [core_attachment_UPLOADER_FKC]    Script Date: 06/17/2016 17:45:08 ******/
ALTER TABLE [dbo].[core_attachment]  WITH CHECK ADD  CONSTRAINT [core_attachment_UPLOADER_FKC] FOREIGN KEY([UPLOADER_FK])
REFERENCES [dbo].[user_user] ([ID]);
ALTER TABLE [dbo].[core_attachment] CHECK CONSTRAINT [core_attachment_UPLOADER_FKC];
/****** Object:  ForeignKey [user_note_entity_note_fkc]    Script Date: 06/17/2016 17:45:08 ******/
ALTER TABLE [dbo].[user_note_entity]  WITH CHECK ADD  CONSTRAINT [user_note_entity_note_fkc] FOREIGN KEY([NOTE_FK])
REFERENCES [dbo].[core_note] ([ID]);
ALTER TABLE [dbo].[user_note_entity] CHECK CONSTRAINT [user_note_entity_note_fkc];
/****** Object:  ForeignKey [user_note_entity_user_fkc]    Script Date: 06/17/2016 17:45:08 ******/
ALTER TABLE [dbo].[user_note_entity]  WITH CHECK ADD  CONSTRAINT [user_note_entity_user_fkc] FOREIGN KEY([USER_FK])
REFERENCES [dbo].[user_user] ([ID]);
ALTER TABLE [dbo].[user_note_entity] CHECK CONSTRAINT [user_note_entity_user_fkc];
/****** Object:  ForeignKey [user_note_property_NOTE_FKC]    Script Date: 06/17/2016 17:45:08 ******/
ALTER TABLE [dbo].[user_note_property]  WITH CHECK ADD  CONSTRAINT [user_note_property_NOTE_FKC] FOREIGN KEY([NOTE_FK])
REFERENCES [dbo].[core_note] ([ID]);
ALTER TABLE [dbo].[user_note_property] CHECK CONSTRAINT [user_note_property_NOTE_FKC];
/****** Object:  ForeignKey [user_note_property_USER_FKC]    Script Date: 06/17/2016 17:45:08 ******/
ALTER TABLE [dbo].[user_note_property]  WITH CHECK ADD  CONSTRAINT [user_note_property_USER_FKC] FOREIGN KEY([USER_FK])
REFERENCES [dbo].[user_user] ([ID]);
ALTER TABLE [dbo].[user_note_property] CHECK CONSTRAINT [user_note_property_USER_FKC];
/****** Object:  ForeignKey [user_note_property_user_note_c]    Script Date: 06/17/2016 17:45:08 ******/
ALTER TABLE [dbo].[user_note_property]  WITH CHECK ADD  CONSTRAINT [user_note_property_user_note_c] FOREIGN KEY([user_note_entity_fk])
REFERENCES [dbo].[user_note_entity] ([ID]);
ALTER TABLE [dbo].[user_note_property] CHECK CONSTRAINT [user_note_property_user_note_c];
/****** Object:  ForeignKey [core_attachment_property_ATTAC]    Script Date: 06/17/2016 17:45:08 ******/
ALTER TABLE [dbo].[core_attachment_property]  WITH CHECK ADD  CONSTRAINT [core_attachment_property_ATTAC] FOREIGN KEY([ATTACHMENT_FK])
REFERENCES [dbo].[core_attachment] ([ID]);
ALTER TABLE [dbo].[core_attachment_property] CHECK CONSTRAINT [core_attachment_property_ATTAC];
