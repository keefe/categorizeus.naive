drop index if exists messagetag;
drop index if exists message_attachments_message;
drop table if exists tags;
drop table if exists messages;
drop table if exists users;
drop table if exists message_tags;
drop table if exists user_sessions;
drop table if exists message_attachments;


create table if not exists tags(
	id bigserial primary key,
	tag varchar(255) not null
);

create table if not exists messages(
	id bigserial primary key,
	body text,
	title text,
	posted_by bigint,
	replies_to bigint,
	root_replies_to bigint
);

create table if not exists message_attachments(
	id bigserial primary key,
	message_id bigint,
	filename text,
	extension text,
	length bigint
);

create table if not exists users(
	id bigserial primary key,
	username text,
	email text,
	name text,
	given_name text,
	family_name text,
	authorized boolean default false,
	passhash text
);

create table if not exists message_tags(
	message_id bigint,
	tag_id bigint,
	user_id bigint
);

create table if not exists user_sessions(
	session_uuid varchar(128) primary key,
	user_id bigint
);

create unique index messagetag on message_tags(message_id, tag_id);
create index user_messages on messages(posted_by);
create index message_replies on messages(replies_to);
create index message_attachments_message on message_attachments(message_id);
create index message_root_replies on messages(root_replies_to);
create unique index taglookup on tags(tag);
