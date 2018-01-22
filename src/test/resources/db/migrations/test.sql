CREATE TABLE if NOT EXISTS `user` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `username` varchar(255) NOT NULL unique,
  `name` varchar(255) NOT NULL,
  `email` varchar(255) DEFAULT NULL unique,
  `password` varchar(255) DEFAULT NULL,
  `status` tinyint(4) NOT NULL,
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

insert into user set id = 1, username = 'suadmin', name = '超级管理员', status = 0;
