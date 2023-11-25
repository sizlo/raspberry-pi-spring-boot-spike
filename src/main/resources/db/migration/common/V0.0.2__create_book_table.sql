drop table if exists book;

create table book(
    id serial not null primary key,
    title text not null,
    category text not null,
    author_id int references author
);
