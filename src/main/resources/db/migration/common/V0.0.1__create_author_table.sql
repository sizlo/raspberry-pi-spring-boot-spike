drop table if exists author;

create table author(
    id serial not null primary key,
    first_name text not null,
    last_name text not null
);
