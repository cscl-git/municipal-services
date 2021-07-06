alter table bk_bookings add bk_account_type varchar(255) NULL;
alter table bk_bookings add	bk_bank_account_holder varchar(255) NULL;
alter table bk_bookings add	bk_bank_account_number varchar(255) NULL;
alter table bk_bookings add	bk_bank_name varchar(255) NULL;
alter table bk_bookings add	bk_ifsc_code varchar(255) NULL;
alter table bk_bookings add discount numeric(19,2) NULL;




-- Drop table

-- DROP TABLE public.bk_rooms_model;

CREATE TABLE public.bk_rooms_model (
	id varchar(255) NOT NULL,
	"action" varchar(255) NULL,
	community_application_number varchar(255) NULL,
	created_date date NULL,
	discount numeric(19,2) NULL,
	facilation_charge numeric(19,2) NULL,
	from_date date NULL,
	last_modified_date date NULL,
	remarks varchar(255) NULL,
	room_application_number varchar(255) NULL,
	room_application_status varchar(255) NULL,
	room_business_service varchar(255) NULL,
	to_date date NULL,
	total_no_of_rooms varchar(255) NULL,
	type_of_room varchar(255) NULL,
	room_payment_status varchar(255) NULL,
	CONSTRAINT bk_rooms_model_pkey PRIMARY KEY (id),
	CONSTRAINT fk63oahghbgxsopq2prdi9sybu8 FOREIGN KEY (community_application_number) REFERENCES bk_bookings(bk_application_number)
);



CREATE TABLE public.bk_room_master (
	id varchar(255) NOT NULL,
	from_date timestamp NULL,
	rent_for_3_hrs varchar(255) NULL,
	rent_for_6_hrs varchar(255) NULL,
	rent_for_9_hrs varchar(255) NULL,
	rent_for_one_day varchar(255) NULL,
	sector_name varchar(255) NULL,
	to_date timestamp NULL,
	total_number_of_rooms varchar(255) NULL,
	type_of_room varchar(255) NULL,
	created_date timestamp NULL,
	last_modified_date timestamp NULL,
	community_center_name varchar(255) NULL,
	CONSTRAINT bk_room_master_pkey PRIMARY KEY (id)
);







-- public.bk_commercial_ground_availability_lock definition

-- Drop table

DROP TABLE public.bk_commercial_ground_availability_lock;

CREATE TABLE public.bk_commercial_ground_availability_lock (
	id varchar(255) NOT NULL,
	booking_venue varchar(255) NULL,
	from_date date NULL,
	is_locked bool NULL,
	to_date date NULL,
	venue_type varchar(255) NULL,
	created_date varchar(255) NULL,
	last_modified_date varchar(255) NULL,
	CONSTRAINT bk_commercial_ground_availability_lock_pkey PRIMARY KEY (id)
);



alter table bk_bookings add	created_date varchar(255) NULL;
alter table bk_bookings add	last_modified_date varchar(255) NULL;

alter table bk_osujm_new_location add created_date varchar(255) NULL;
alter table bk_osujm_new_location add last_modified_date varchar(255) NULL;
alter table bk_bookings add bk_nominee_name varchar(255) NULL;

alter table bk_commercial_ground_availability_lock add reason_for_hold varchar(255) NULL;
alter table bk_commercial_ground_availability_lock add sector varchar(255) NULL;
alter table bk_bookings add refundable_security_money numeric(19,2) NULL;
alter table bk_rooms_model add room_status varchar(255) NULL;

alter table bk_rooms_model add room_created_date varchar(255) NULL;
alter table bk_bookings add	card_number varchar(255) NULL;
alter table bk_bookings add	transaction_number varchar(255) NULL;

-- Drop table

-- DROP TABLE public.bk_booking_lock_dates;

CREATE TABLE public.bk_booking_lock_dates (
	id varchar(255) NOT NULL,
	application_number varchar(255) NULL,
	booking_type varchar(255) NULL,
	booking_venue varchar(255) NULL,
	created_date varchar(255) NULL,
	last_modified_date varchar(255) NULL,
	sector varchar(255) NULL,
	CONSTRAINT bk_booking_lock_dates_pkey PRIMARY KEY (id)
);


alter table bk_booking_lock_dates add from_date date NULL;
alter table bk_booking_lock_dates add to_date date NULL;

ALTER TABLE bk_bookings ADD COLUMN payment_collection_type varchar(255) NULL,
ADD COLUMN paid_by varchar(255) NULL,
ADD COLUMN payer_name varchar(255) NULL,
ADD COLUMN payer_mobile_number varchar(255) NULL,
ADD COLUMN cheque_number varchar(255) NULL,
ADD COLUMN payment_date varchar(255) NULL,
ADD COLUMN ifsc_code varchar(255) NULL,
ADD COLUMN bank_name varchar(255) NULL,
ADD COLUMN bank_branch varchar(255) NULL;

ALTER TABLE bk_rooms_model ADD COLUMN payment_collection_type varchar(255) NULL,
ADD COLUMN paid_by varchar(255) NULL,
ADD COLUMN payer_name varchar(255) NULL,
ADD COLUMN payer_mobile_number varchar(255) NULL,
ADD COLUMN cheque_number varchar(255) NULL,
ADD COLUMN payment_date varchar(255) NULL,
ADD COLUMN ifsc_code varchar(255) NULL,
ADD COLUMN bank_name varchar(255) NULL,
ADD COLUMN bank_branch varchar(255) NULL,
ADD COLUMN transaction_number varchar(255) NULL,
ADD COLUMN card_number varchar(255) NULL;