DROP TABLE IF EXISTS role_to_permissions;
DROP TABLE IF EXISTS user_to_roles;
DROP TABLE IF EXISTS permissions;
DROP TABLE IF EXISTS roles;
DROP TABLE IF EXISTS comments;
DROP TABLE IF EXISTS ratings;
DROP TABLE IF EXISTS book_to_tag;
DROP TABLE IF EXISTS voucher;
DROP TABLE IF EXISTS tags;
DROP TABLE IF EXISTS book;
DROP TABLE IF EXISTS persons;
DROP TABLE IF EXISTS users;

CREATE TABLE users
(
    id       INT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(255) NOT NULL,
    password VARCHAR(255) NOT NULL,
    CONSTRAINT uq_users_username UNIQUE (username)
);

-- One profile per user account, sharing the same id
CREATE TABLE persons
(
    id        INT PRIMARY KEY,
    firstName VARCHAR(255) NOT NULL,
    lastName  VARCHAR(255) NOT NULL,
    email     VARCHAR(255) NOT NULL,
    CONSTRAINT fk_persons_user FOREIGN KEY (id) REFERENCES users (id) ON DELETE CASCADE
);

CREATE TABLE book
(
    id          INT AUTO_INCREMENT PRIMARY KEY,
    name        VARCHAR(255)  NOT NULL,
    author      VARCHAR(255)  NOT NULL,
    description VARCHAR(1500) NOT NULL,
    price       DOUBLE        NOT NULL
);

CREATE TABLE tags
(
    id   INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255) NOT NULL
);

CREATE TABLE voucher
(
    id       INT AUTO_INCREMENT PRIMARY KEY,
    code     VARCHAR(255) NOT NULL,
    discount INT          NOT NULL,
    CONSTRAINT uq_voucher_code UNIQUE (code),
    CONSTRAINT ck_voucher_discount CHECK (discount BETWEEN 1 AND 95)
);

CREATE TABLE book_to_tag
(
    bookId INT NOT NULL,
    tagId  INT NOT NULL,
    PRIMARY KEY (bookId, tagId),
    CONSTRAINT fk_book_to_tag_book FOREIGN KEY (bookId) REFERENCES book (id) ON DELETE CASCADE,
    CONSTRAINT fk_book_to_tag_tag FOREIGN KEY (tagId) REFERENCES tags (id) ON DELETE CASCADE
);

CREATE TABLE ratings
(
    bookId INT NOT NULL,
    userId INT NOT NULL,
    rating INT NOT NULL,
    PRIMARY KEY (bookId, userId),
    CONSTRAINT ck_ratings_range CHECK (rating BETWEEN 1 AND 5),
    CONSTRAINT fk_ratings_book FOREIGN KEY (bookId) REFERENCES book (id) ON DELETE CASCADE,
    CONSTRAINT fk_ratings_user FOREIGN KEY (userId) REFERENCES users (id) ON DELETE CASCADE
);

CREATE TABLE comments
(
    id      INT AUTO_INCREMENT PRIMARY KEY,
    bookId  INT          NOT NULL,
    userId  INT          NOT NULL,
    comment VARCHAR(500) NOT NULL,
    CONSTRAINT fk_comments_book FOREIGN KEY (bookId) REFERENCES book (id) ON DELETE CASCADE,
    CONSTRAINT fk_comments_user FOREIGN KEY (userId) REFERENCES users (id) ON DELETE CASCADE
);

CREATE TABLE roles
(
    id   INT PRIMARY KEY,
    name VARCHAR(255) NOT NULL
);

CREATE TABLE permissions
(
    id   INT PRIMARY KEY,
    name VARCHAR(255) NOT NULL
);

CREATE TABLE user_to_roles
(
    userId INT NOT NULL,
    roleId INT NOT NULL,
    PRIMARY KEY (userId, roleId),
    CONSTRAINT fk_user_to_roles_user FOREIGN KEY (userId) REFERENCES users (id) ON DELETE CASCADE,
    CONSTRAINT fk_user_to_roles_role FOREIGN KEY (roleId) REFERENCES roles (id) ON DELETE CASCADE
);

CREATE TABLE role_to_permissions
(
    roleId       INT NOT NULL,
    permissionId INT NOT NULL,
    PRIMARY KEY (roleId, permissionId),
    CONSTRAINT fk_role_to_permissions_role FOREIGN KEY (roleId) REFERENCES roles (id) ON DELETE CASCADE,
    CONSTRAINT fk_role_to_permissions_permission FOREIGN KEY (permissionId) REFERENCES permissions (id) ON DELETE CASCADE
);
