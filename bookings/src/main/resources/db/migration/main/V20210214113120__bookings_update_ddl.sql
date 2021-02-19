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








-- public.bk_commercial_ground_availability_lock definition

-- Drop table

-- DROP TABLE public.bk_commercial_ground_availability_lock;

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