CREATE SEQUENCE SEQ_NULM_ALF_ID_GEN;
CREATE TABLE nulm_smid_alf_details
(
  uuid character varying(64) NOT NULL,
  id character varying(64) NOT NULL,
  date_of_formation character varying(64),
  name character varying(255),
  registeration_date character varying(255),
  address character varying(255),
  account_number character varying(255),
  bank_name character varying(255),
  branch_name character varying(255),
  contact_number character varying(255),
  tenant_id character varying(256),
  is_active boolean,
  created_by character varying(64),
  created_time bigint,
  last_modified_by character varying(64),
  last_modified_time bigint,
  adhaar_number character varying(256),
  date_of_opening_account character varying(256),
  alf_formated_through character varying(256),
  CONSTRAINT nulm_alf_application_detail_pkey PRIMARY KEY (uuid),
  CONSTRAINT id UNIQUE (id)
);
CREATE TABLE nulm_alf_application_document
(
  document_uuid character varying(64) NOT NULL,
  filestore_id character varying NOT NULL,
  application_uuid character varying(64) NOT NULL,
  document_type character varying(256) NOT NULL,
  tenant_id character varying(256),
  is_active boolean NOT NULL,
  created_by character varying(64),
  created_time bigint,
  last_modified_by character varying(256),
  last_modified_time bigint,
  CONSTRAINT nulm_alf_application_document_pkey PRIMARY KEY (document_uuid),
  CONSTRAINT fk_nulm_alf_application_document_uuid FOREIGN KEY (application_uuid)
      REFERENCES nulm_smid_alf_details (uuid) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE NO ACTION,
  CONSTRAINT application_uuid_uq_document_type UNIQUE (application_uuid, document_type, tenant_id),
  CONSTRAINT id_uq UNIQUE (application_uuid)
);