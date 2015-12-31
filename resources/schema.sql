create table revisions (id integer primary key, epochsecond integer, user text, page_id integer, title text);
create table revision_texts (id integer primary key, txt text);
