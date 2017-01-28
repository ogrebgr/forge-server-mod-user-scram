CREATE TABLE `user_scram` (
  `user` int(11) NOT NULL,
  `username` varchar(255) COLLATE utf8_unicode_ci NOT NULL,
  `salt` varchar(255) COLLATE utf8_unicode_ci NOT NULL,
  `server_key` varchar(255) COLLATE utf8_unicode_ci NOT NULL,
  `stored_key` varchar(255) COLLATE utf8_unicode_ci NOT NULL,
  `iterations` int(11) NOT NULL,
  `username_lc` varchar(255) COLLATE utf8_unicode_ci NOT NULL,
  PRIMARY KEY (`user`),
  UNIQUE KEY `i_username` (`username`),
  UNIQUE KEY `i_username_lc` (`username_lc`),
  CONSTRAINT `fk_user_scram_1` FOREIGN KEY (`user`) REFERENCES `users` (`id`) ON DELETE NO ACTION ON UPDATE NO ACTION
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci
