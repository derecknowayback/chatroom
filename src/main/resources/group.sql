CREATE DATABASE IF NOT EXISTS chatroom;

USE chatroom;

create table IF NOT EXISTS tb_group (
    groupID     int auto_increment  primary key,
    `limit`     int          null,
    groupMember varchar(100) null,
    name        varchar(50)  null
);

INSERT INTO tb_group (`limit`, groupMember, name) VALUES
                                        (10, '1,2,3', 'Group1'),
                                        (50, '4,5,6,7', 'Group2');