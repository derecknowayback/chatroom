CREATE DATABASE IF NOT EXISTS chatroom;

USE chatroom;

CREATE TABLE user (
                      id INT AUTO_INCREMENT PRIMARY KEY,
                      username VARCHAR(255),
                      password VARCHAR(255)
);


INSERT INTO user (username, password) VALUES
                                          ('user1', 'password1'),
                                          ('user2', 'password2');