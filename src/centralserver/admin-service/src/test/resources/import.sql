-- data populated for the integration tests
-- SQL needs to be defined in terms of autogenerated HSQL table structure, not the actual PostgreSQL tables.
INSERT INTO UI_USERS(ID, USERNAME, LOCALE, CREATED_AT, UPDATED_AT) values (1000001, 'testuser', null, now(), now());
-- noinspection SqlResolve
INSERT INTO APIKEY (ID, ENCODED_KEY) values (1, 'ad26a8235b3e847dc0b9ac34733d5acb39e2b6af634796e7eebe171165cdf2d1');
INSERT INTO APIKEY_ROLES (APIKEY_ID, ROLE) values (1, 'XROAD_SYSTEM_ADMINISTRATOR');
INSERT INTO APIKEY_ROLES (APIKEY_ID, ROLE) values (1, 'XROAD_SECURITY_OFFICER');
INSERT INTO APIKEY_ROLES (APIKEY_ID, ROLE) values (1, 'XROAD_REGISTRATION_OFFICER');

INSERT INTO MEMBER_CLASSES (ID, CODE, DESCRIPTION, CREATED_AT, UPDATED_AT) values (1000001, 'GOV', 'Government', now(), now());
INSERT INTO MEMBER_CLASSES (ID, CODE, DESCRIPTION, CREATED_AT, UPDATED_AT) values (1000002, 'MemberclassFoo', 'Member class with rare code string', now(), now());

INSERT INTO IDENTIFIERS (ID, OBJECT_TYPE, XROAD_INSTANCE, MEMBER_CLASS, MEMBER_CODE, SUBSYSTEM_CODE, TYPE) values (1000001, 'MEMBER', 'TEST', 'GOV', 'M1', null, 'ClientId');
INSERT INTO IDENTIFIERS (ID, OBJECT_TYPE, XROAD_INSTANCE, MEMBER_CLASS, MEMBER_CODE, SUBSYSTEM_CODE, TYPE) values (1000002, 'MEMBER', 'TEST', 'GOV', 'M2', null, 'ClientId');
INSERT INTO IDENTIFIERS (ID, OBJECT_TYPE, XROAD_INSTANCE, MEMBER_CLASS, MEMBER_CODE, SUBSYSTEM_CODE, TYPE) values (1000003, 'MEMBER', 'TEST', 'GOV', 'M3', null, 'ClientId');
INSERT INTO IDENTIFIERS (ID, OBJECT_TYPE, XROAD_INSTANCE, MEMBER_CLASS, MEMBER_CODE, SUBSYSTEM_CODE, TYPE) values (1000004, 'MEMBER', 'TEST', 'GOV', 'M4', null, 'ClientId');
INSERT INTO IDENTIFIERS (ID, OBJECT_TYPE, XROAD_INSTANCE, MEMBER_CLASS, MEMBER_CODE, SUBSYSTEM_CODE, TYPE) values (1000005, 'MEMBER', 'TEST', 'GOV', 'M5', null, 'ClientId');
INSERT INTO IDENTIFIERS (ID, OBJECT_TYPE, XROAD_INSTANCE, MEMBER_CLASS, MEMBER_CODE, SUBSYSTEM_CODE, TYPE) values (1000006, 'MEMBER', 'TEST', 'GOV', 'M6', null, 'ClientId');
INSERT INTO IDENTIFIERS (ID, OBJECT_TYPE, XROAD_INSTANCE, MEMBER_CLASS, MEMBER_CODE, SUBSYSTEM_CODE, TYPE) values (1000007, 'MEMBER', 'TEST', 'GOV', 'M7', null, 'ClientId');
INSERT INTO IDENTIFIERS (ID, OBJECT_TYPE, XROAD_INSTANCE, MEMBER_CLASS, MEMBER_CODE, SUBSYSTEM_CODE, TYPE) values (1000008, 'MEMBER', 'TEST', 'GOV', 'M8', null, 'ClientId');
INSERT INTO IDENTIFIERS (ID, OBJECT_TYPE, XROAD_INSTANCE, MEMBER_CLASS, MEMBER_CODE, SUBSYSTEM_CODE, TYPE) values (1000009, 'MEMBER', 'TEST', 'GOV', 'M9', null, 'ClientId');
INSERT INTO IDENTIFIERS (ID, OBJECT_TYPE, XROAD_INSTANCE, MEMBER_CLASS, MEMBER_CODE, SUBSYSTEM_CODE, TYPE) values (1000010, 'SUBSYSTEM', 'TEST', 'GOV', 'M1', 'SS1', 'ClientId');
INSERT INTO IDENTIFIERS (ID, OBJECT_TYPE, XROAD_INSTANCE, MEMBER_CLASS, MEMBER_CODE, SUBSYSTEM_CODE, TYPE) values (1000011, 'SUBSYSTEM', 'TEST', 'MemberclassFoo', 'M10', null, 'ClientId');
INSERT INTO IDENTIFIERS (ID, OBJECT_TYPE, XROAD_INSTANCE, MEMBER_CLASS, MEMBER_CODE, SUBSYSTEM_CODE, TYPE) values (1000012, 'MEMBER', 'Instance2', 'GOV', 'M11', null, 'ClientId');


INSERT INTO SECURITY_SERVER_CLIENTS (ID, MEMBER_CODE, SUBSYSTEM_CODE, NAME, XROAD_MEMBER_ID, MEMBER_CLASS_ID, SERVER_CLIENT_ID, TYPE, CREATED_AT, UPDATED_AT) values (1000001, 'M1', null, 'Member1', null, 1000001, 1000001, 'XRoadMember', now(), now());
INSERT INTO SECURITY_SERVER_CLIENTS (ID, MEMBER_CODE, SUBSYSTEM_CODE, NAME, XROAD_MEMBER_ID, MEMBER_CLASS_ID, SERVER_CLIENT_ID, TYPE, CREATED_AT, UPDATED_AT) values (1000002, 'M2', null, 'Member2', null, 1000001, 1000002, 'XRoadMember', now(), now());
INSERT INTO SECURITY_SERVER_CLIENTS (ID, MEMBER_CODE, SUBSYSTEM_CODE, NAME, XROAD_MEMBER_ID, MEMBER_CLASS_ID, SERVER_CLIENT_ID, TYPE, CREATED_AT, UPDATED_AT) values (1000003, 'M3', null, 'member3', null, 1000001, 1000003, 'XRoadMember', now(), now());
INSERT INTO SECURITY_SERVER_CLIENTS (ID, MEMBER_CODE, SUBSYSTEM_CODE, NAME, XROAD_MEMBER_ID, MEMBER_CLASS_ID, SERVER_CLIENT_ID, TYPE, CREATED_AT, UPDATED_AT) values (1000004, 'M4', null, 'mEmber4', null, 1000001, 1000004, 'XRoadMember', now(), now());
INSERT INTO SECURITY_SERVER_CLIENTS (ID, MEMBER_CODE, SUBSYSTEM_CODE, NAME, XROAD_MEMBER_ID, MEMBER_CLASS_ID, SERVER_CLIENT_ID, TYPE, CREATED_AT, UPDATED_AT) values (1000005, 'M5', null, 'Member5-ÅÖÄ', null, 1000001, 1000005, 'XRoadMember', now(), now());
INSERT INTO SECURITY_SERVER_CLIENTS (ID, MEMBER_CODE, SUBSYSTEM_CODE, NAME, XROAD_MEMBER_ID, MEMBER_CLASS_ID, SERVER_CLIENT_ID, TYPE, CREATED_AT, UPDATED_AT) values (1000006, 'M6', null, 'Member6\a', null, 1000001, 1000006, 'XRoadMember', now(), now());
INSERT INTO SECURITY_SERVER_CLIENTS (ID, MEMBER_CODE, SUBSYSTEM_CODE, NAME, XROAD_MEMBER_ID, MEMBER_CLASS_ID, SERVER_CLIENT_ID, TYPE, CREATED_AT, UPDATED_AT) values (1000007, 'M7', null, 'Member7_a', null, 1000001, 1000007, 'XRoadMember', now(), now());
INSERT INTO SECURITY_SERVER_CLIENTS (ID, MEMBER_CODE, SUBSYSTEM_CODE, NAME, XROAD_MEMBER_ID, MEMBER_CLASS_ID, SERVER_CLIENT_ID, TYPE, CREATED_AT, UPDATED_AT) values (1000008, 'M8', null, 'Member8%a', null, 1000001, 1000008, 'XRoadMember', now(), now());
INSERT INTO SECURITY_SERVER_CLIENTS (ID, MEMBER_CODE, SUBSYSTEM_CODE, NAME, XROAD_MEMBER_ID, MEMBER_CLASS_ID, SERVER_CLIENT_ID, TYPE, CREATED_AT, UPDATED_AT) values (1000009, 'M9', null, 'Member9__%%em%', null, 1000001, 1000009, 'XRoadMember', now(), now());
INSERT INTO SECURITY_SERVER_CLIENTS (ID, MEMBER_CODE, SUBSYSTEM_CODE, NAME, XROAD_MEMBER_ID, MEMBER_CLASS_ID, SERVER_CLIENT_ID, TYPE, CREATED_AT, UPDATED_AT) values (1000010, null, 'SS1', 'Member1-SS1', 1000001, 1000001, 1000010, 'Subsystem', now(), now());
INSERT INTO SECURITY_SERVER_CLIENTS (ID, MEMBER_CODE, SUBSYSTEM_CODE, NAME, XROAD_MEMBER_ID, MEMBER_CLASS_ID, SERVER_CLIENT_ID, TYPE, CREATED_AT, UPDATED_AT) values (1000011, 'M10', null, 'Member10', null, 1000002, 1000011, 'XRoadMember', now(), now());
INSERT INTO SECURITY_SERVER_CLIENTS (ID, MEMBER_CODE, SUBSYSTEM_CODE, NAME, XROAD_MEMBER_ID, MEMBER_CLASS_ID, SERVER_CLIENT_ID, TYPE, CREATED_AT, UPDATED_AT) values (1000012, 'M11', null, 'Member11', null, 1000001, 1000012, 'XRoadMember', now(), now());

INSERT INTO SECURITY_SERVERS (ID, SERVER_CODE, OWNER_ID, ADDRESS, CREATED_AT, UPDATED_AT) values (1000001, 'server1', 1000001, 'server1.test', now(), now());
INSERT INTO SECURITY_SERVERS (ID, SERVER_CODE, OWNER_ID, ADDRESS, CREATED_AT, UPDATED_AT) values (1000002, 'server2', 1000004, 'server2.test', now(), now());

INSERT INTO SERVER_CLIENTS (ID, SECURITY_SERVER_ID, SECURITY_SERVER_CLIENT_ID) values (1000001, 1000001, 1000001);
INSERT INTO SERVER_CLIENTS (ID, SECURITY_SERVER_ID, SECURITY_SERVER_CLIENT_ID) values (1000002, 1000001, 1000002);
INSERT INTO SERVER_CLIENTS (ID, SECURITY_SERVER_ID, SECURITY_SERVER_CLIENT_ID) values (1000003, 1000001, 1000010);
INSERT INTO SERVER_CLIENTS (ID, SECURITY_SERVER_ID, SECURITY_SERVER_CLIENT_ID) values (1000004, 1000002, 1000004);
INSERT INTO SERVER_CLIENTS (ID, SECURITY_SERVER_ID, SECURITY_SERVER_CLIENT_ID) values (1000005, 1000002, 1000010);
