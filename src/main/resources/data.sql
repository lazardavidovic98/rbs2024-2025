-- Identity columns are filled in insertion order, so the ids below are 1..n.

INSERT INTO users(username, password)
VALUES ('bruce', 'wayne'),
       ('peter', 'security_rules'),
       ('tom', 'guessmeifyoucan'),
       ('book', 'work');

INSERT INTO persons(id, firstName, lastName, email)
VALUES (1, 'Bruce', 'Wayne', 'notBatman@gmail.com'),
       (2, 'Peter', 'Petigrew', 'oneFingernailFewerToClean@gmail.com'),
       (3, 'Tom', 'Riddle', 'theyGotMyNose@gmail.com'),
       (4, 'Book', 'Work', 'bw@insecurebook.com');

INSERT INTO book(name, author, description, price)
VALUES ('Le Petit Prince',
        'Antoine de Saint-Exupéry',
        'The Little Prince comes from a planet barely bigger than he is, on which there are baobabs and a very precious flower, a rose, which is doing its coquette and for which he feels responsible. The Little Prince loves the sunset. One day, he saw it forty-four times! He also visited other planets and met some very important people, but they didn’t know how to answer his questions. On Earth, he tamed the fox, who became his friend.',
        639.07),
       ('The Brothers Karamazov',
        'Fyodor Dostoevsky',
        'The Brothers Karamazov is a murder mystery, a courtroom drama, and an exploration of erotic rivalry in a series of triangular love affairs involving the “wicked and sentimental” Fyodor Pavlovich Karamazov and his three sons―the impulsive and sensual Dmitri; the coldly rational Ivan; and the healthy, red-cheeked young novice Alyosha. Through the gripping events of their story, Dostoevsky portrays the whole of Russian life, is social and spiritual striving, in what was both the golden age and a tragic turning point in Russian culture.',
        413.71),
       ('Na Drini ćuprija',
        'Ivo Andrić',
        'Najpoznatiji roman Ive Andrića, "Na Drini ćuprija" (1945), hronološki prati četiri velika zbivanja oko mosta preko rijeke Drine u Višegradu, koji je izgradio veliki vezir Mehmed Paša Sokolovic, poreklom iz tih krajeva.',
        1120.05),
       ('The Lord of the Rings',
        'J.R.R. Tolkien',
        'The Lord of the Rings tells of the great quest undertaken by Frodo and the Fellowship of the Ring: Gandalf the Wizard; the hobbits Merry, Pippin, and Sam; Gimli the Dwarf; Legolas the Elf; Boromir of Gondor; and a tall, mysterious stranger called Strider.',
        2465.46);

INSERT INTO tags(name)
VALUES ('epic'),
       ('fantasy'),
       ('wholesome'),
       ('historic'),
       ('classic'),
       ('biography');

INSERT INTO book_to_tag(bookId, tagId)
VALUES (1, 1),
       (1, 3),
       (2, 5),
       (2, 3),
       (3, 3),
       (3, 4),
       (4, 1),
       (4, 2);

INSERT INTO ratings(bookId, userId, rating)
VALUES (1, 3, 5),
       (3, 2, 1),
       (3, 1, 3),
       (1, 1, 5),
       (1, 2, 4);

INSERT INTO comments(bookId, userId, comment)
VALUES (1, 1, 'Good read.');

-- Role based access control: users -> roles -> permissions
INSERT INTO roles(id, name)
VALUES (1, 'ADMIN'),
       (2, 'MANAGER'),
       (3, 'BUYER');

INSERT INTO permissions(id, name)
VALUES (1, 'ADD_COMMENT'),
       (2, 'VIEW_BOOK_LIST'),
       (3, 'CREATE_BOOK'),
       (4, 'VIEW_PERSONS_LIST'),
       (5, 'VIEW_PERSON'),
       (6, 'UPDATE_PERSON'),
       (7, 'VIEW_MY_PROFILE'),
       (8, 'RATE_BOOK'),
       (9, 'BUY_BOOK'),
       (10, 'NEW_VOUCHER');

-- ADMIN: every permission
INSERT INTO role_to_permissions(roleId, permissionId)
VALUES (1, 1), (1, 2), (1, 3), (1, 4), (1, 5), (1, 6), (1, 7), (1, 8), (1, 9), (1, 10);

-- MANAGER: manages the catalogue, users and vouchers, cannot rate books
INSERT INTO role_to_permissions(roleId, permissionId)
VALUES (2, 1), (2, 2), (2, 3), (2, 4), (2, 6), (2, 7), (2, 9), (2, 10);

-- BUYER: browses, rates, comments and buys; may only edit their own profile
INSERT INTO role_to_permissions(roleId, permissionId)
VALUES (3, 1), (3, 2), (3, 6), (3, 7), (3, 8), (3, 9);

INSERT INTO user_to_roles(userId, roleId)
VALUES (4, 1), -- book  : ADMIN
       (3, 2), -- tom   : MANAGER
       (1, 3), -- bruce : BUYER
       (2, 3); -- peter : BUYER
