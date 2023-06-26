CREATE DATABASE IF NOT EXISTS chatroom;

USE chatroom;

CREATE TABLE user (
                      id INT AUTO_INCREMENT PRIMARY KEY,
                      username VARCHAR(255),
                      password VARCHAR(255)
);