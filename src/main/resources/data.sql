insert into users(id, username, password)
values (1, 'bruce', 'wayne'),
       (2, 'peter', 'security_rules'),
       (3, 'tom', 'guessmeifyoucan'),
       (4, 'book', 'work');

insert into persons(id, firstName, lastName, email)
values (1, 'bruce', 'wayne', 'notBatman@gmail.com'),
       (2, 'Peter', 'Petigrew', 'oneFingernailFewerToClean@gmail.com'),
       (3, 'Tom', 'Riddle', 'theyGotMyNose@gmail.com'),
       (4, 'book', 'work', 'bw@insecurebook.com');

insert into hashedUsers(id, username, passwordHash, salt)
values (1, 'bruce', 'qw8Uxa2fXimKruS9wYEm4qm3ZaIGw/hJNvOG3PemhoA=', 'MEI4PU5hcHhaRHZz'),
       (2, 'peter', 'qPWryBEWiWdHsC+67dmO+y5ugGrMVI2w4MSz0+CpDm4=', 'MnY1am14c2d1ZlBf'),
       (3, 'tom', 'FLmYMYmwSRxcy0n2uwysy39ax0TRWvKHswSCPMo+PiI=', 'OChoOitAKWE0TWlD');

insert into book(id, name, author, description, price)
values (1, 'Le Petit Prince',
        'Antoine de Saint-Exupéry',
        'The Little Prince comes from a planet barely bigger than he is, on which there are baobabs and a very precious flower, a rose, which is doing its coquette and for which he feels responsible. The Little Prince loves the sunset. One day, he saw it forty-four times! He also visited other planets and met some very important people, but they didn’t know how to answer his questions. On Earth, he tamed the fox, who became his friend.',
        639.07),
       (2, 'The Brothers Karamazov',
        'Fyodor Dostoevsky',
        'The Brothers Karamazov is a murder mystery, a courtroom drama, and an exploration of erotic rivalry in a series of triangular love affairs involving the “wicked and sentimental” Fyodor Pavlovich Karamazov and his three sons―the impulsive and sensual Dmitri; the coldly rational Ivan; and the healthy, red-cheeked young novice Alyosha. Through the gripping events of their story, Dostoevsky portrays the whole of Russian life, is social and spiritual striving, in what was both the golden age and a tragic turning point in Russian culture.',
        413.71),
       (3, 'Na Drini ćuprija',
        'Ivo Andrić',
        'Najpoznatiji roman Ive Andrića, "Na Drini ćuprija" (1945), hronološki prati četiri velika zbivanja oko mosta preko rijeke Drine u Višegradu, koji je izgradio veliki vezir Mehmed Paša Sokolovic, poreklom iz tih krajeva.',
        1120.05),
       (4, 'The Lord of the Rings',
        'J.R.R. Tolkien',
        'The Lord of the Rings tells of the great quest undertaken by Frodo and the Fellowship of the Ring: Gandalf the Wizard; the hobbits Merry, Pippin, and Sam; Gimli the Dwarf; Legolas the Elf; Boromir of Gondor; and a tall, mysterious stranger called Strider.',
        2465.46);

insert into tags(id, name)
values (1, 'epic'),
       (2, 'fantasy'),
       (3, 'wholesome'),
       (4, 'historic'),
       (5, 'classic'),
       (6, 'biography');

insert into book_to_tag(bookId, tagId)
values (1, 1),
       (1, 3),
       (2, 5),
       (2, 3),
       (3, 3),
       (3, 4),
       (4, 1),
       (4, 2);

insert into ratings(bookId, userId, rating)
values (1, 3, 5),
       (3, 2, 1),
       (3, 1, 3),
       (1, 1, 5),
       (1, 2, 4);

insert into comments(bookId, userId, comment)
values (1, 1, 'Good read.');

insert into roles(id, name)
values (1, 'ADMIN'),
       (2, 'MANAGER'),
       (3, 'BUYER');

insert into permissions(id, name)
values (1, 'ADD_COMMENT'),
       (2, 'VIEW_BOOK_LIST'),
       (3, 'CREATE_BOOK'),
       (4, 'VIEW_PERSONS_LIST'),
       (5, 'VIEW_PERSON'),
       (6, 'UPDATE_PERSON'),
       (7, 'VIEW_MY_PROFILE'),
       (8, 'RATE_BOOK'),
       (9, 'BUY_BOOK'),
       (10, 'NEW_VOUCHER');

-- Dodeljujem sve permisije administratoru
insert into role_to_permissions(roleId, permissionId)
values (1, 1), (1, 2), (1, 3), (1, 4), (1, 5), (1, 6), (1, 7), (1, 8), (1, 9), (1, 10);

-- Dodelujem permisije menadzeru
insert into role_to_permissions(roleId, permissionId)
values (2, 1), (2, 2), (2, 3), (2, 4), (2, 6), (2, 7), (2, 9), (2, 10);

-- Dodeljujem permisije kupcu
insert into role_to_permissions(roleId, permissionId)
values (3, 1), (3, 2), (3, 6), (3, 7), (3, 8), (3, 9);

insert into user_to_roles(userId, roleId)
values (4, 1),  
       (3, 2),  
       (1, 3),  
       (2, 3);  