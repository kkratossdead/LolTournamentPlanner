-- phpMyAdmin SQL Dump
-- version 5.2.1
-- https://www.phpmyadmin.net/
--
-- Hôte : 127.0.0.1
-- Généré le : dim. 14 déc. 2025 à 13:26
-- Version du serveur : 11.7.2-MariaDB
-- Version de PHP : 8.2.12

SET SQL_MODE = "NO_AUTO_VALUE_ON_ZERO";
START TRANSACTION;
SET time_zone = "+00:00";


/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8mb4 */;

--
-- Base de données : `loltournamentplanner`
--

-- --------------------------------------------------------

--
-- Structure de la table `lol_accounts`
--

CREATE TABLE `lol_accounts` (
  `puuid` varchar(128) NOT NULL,
  `user_id` char(36) NOT NULL,
  `game_name` varchar(64) NOT NULL,
  `tag_line` varchar(32) NOT NULL,
  `summoner_level` int(11) NOT NULL,
  `tier` varchar(32) NOT NULL,
  `rank` varchar(16) NOT NULL,
  `league_points` int(11) NOT NULL,
  `last_updated` varchar(64) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Déchargement des données de la table `lol_accounts`
--

INSERT INTO `lol_accounts` (`puuid`, `user_id`, `game_name`, `tag_line`, `summoner_level`, `tier`, `rank`, `league_points`, `last_updated`) VALUES
('Euni0Ii9T2u2axPndKXwHl0BhrG6HWhcfkeg8YiOK_Kq0OWmSF2erEOMszC3SEc9QoZIUMqkiSiS4Q', '2db7345d-f170-4d0f-aa00-e2f04e790599', 'Pali Pali', '0001', 523, 'EMERALD', 'I', 52, ''),
('i06Ulv13adSC_PR-Dgt3OaL6i02vFQf97tmqoAsZQLgyuAT4nJoNzrPWa05XOJIZLrMuC2CbyRAoyg', '2db7345d-f170-4d0f-aa00-e2f04e790599', 'twitch r0od3x', '2005', 276, 'BRONZE', 'IV', 0, ''),
('_cJtMW4M74AACwYW4m0E-9GsmdC_RvEXSrtmAYiQmTOwaIN44xhqPLSn9CpvsSsVwtuPWqBh-T4fiA', '2db7345d-f170-4d0f-aa00-e2f04e790599', 'Azgar', 'IDK', 257, 'EMERALD', 'I', 52, '');

-- --------------------------------------------------------

--
-- Structure de la table `tournaments`
--

CREATE TABLE `tournaments` (
  `id` varchar(64) NOT NULL,
  `name` varchar(128) NOT NULL,
  `status` varchar(32) NOT NULL,
  `description` text NOT NULL,
  `start_date` varchar(64) NOT NULL,
  `max_participants` int(11) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Déchargement des données de la table `tournaments`
--

INSERT INTO `tournaments` (`id`, `name`, `status`, `description`, `start_date`, `max_participants`) VALUES
('1', 'Winter Cup 2025', 'OPEN', '5v5 community tournament (EU)', '2025-12-20', 64),
('2', 'SoloQ Challenge', 'OPEN', 'SoloQ style tournament (1v1)', '2025-12-22', 32),
('3', 'Rift Rivals: Community Edition', 'OPEN', 'Mixed teams, fun format', '2025-12-28', 50),
('4', 'ARAM Clash', 'CLOSED', 'ARAM-only clash', '2025-11-30', 16);

-- --------------------------------------------------------

--
-- Structure de la table `tournament_participants`
--

CREATE TABLE `tournament_participants` (
  `tournament_id` varchar(64) NOT NULL,
  `puuid` varchar(128) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Déchargement des données de la table `tournament_participants`
--

INSERT INTO `tournament_participants` (`tournament_id`, `puuid`) VALUES
('1', 'Euni0Ii9T2u2axPndKXwHl0BhrG6HWhcfkeg8YiOK_Kq0OWmSF2erEOMszC3SEc9QoZIUMqkiSiS4Q'),
('2', 'Euni0Ii9T2u2axPndKXwHl0BhrG6HWhcfkeg8YiOK_Kq0OWmSF2erEOMszC3SEc9QoZIUMqkiSiS4Q'),
('3', 'Euni0Ii9T2u2axPndKXwHl0BhrG6HWhcfkeg8YiOK_Kq0OWmSF2erEOMszC3SEc9QoZIUMqkiSiS4Q'),
('4', 'Euni0Ii9T2u2axPndKXwHl0BhrG6HWhcfkeg8YiOK_Kq0OWmSF2erEOMszC3SEc9QoZIUMqkiSiS4Q'),
('1', 'i06Ulv13adSC_PR-Dgt3OaL6i02vFQf97tmqoAsZQLgyuAT4nJoNzrPWa05XOJIZLrMuC2CbyRAoyg'),
('4', 'i06Ulv13adSC_PR-Dgt3OaL6i02vFQf97tmqoAsZQLgyuAT4nJoNzrPWa05XOJIZLrMuC2CbyRAoyg'),
('1', '_cJtMW4M74AACwYW4m0E-9GsmdC_RvEXSrtmAYiQmTOwaIN44xhqPLSn9CpvsSsVwtuPWqBh-T4fiA');

-- --------------------------------------------------------

--
-- Structure de la table `users`
--

CREATE TABLE `users` (
  `id` char(36) NOT NULL,
  `username` varchar(64) NOT NULL,
  `password` varchar(255) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Déchargement des données de la table `users`
--

INSERT INTO `users` (`id`, `username`, `password`) VALUES
('23efd97b-0e35-4d8e-9034-f030b9dc432b', 'Kkratoss', 'aymane'),
('2db7345d-f170-4d0f-aa00-e2f04e790599', 'Aymane', 'aymane'),
('d17749e4-39b7-4b55-bd46-bcec09a37efd', 'hamid', 'hamid');

--
-- Index pour les tables déchargées
--

--
-- Index pour la table `lol_accounts`
--
ALTER TABLE `lol_accounts`
  ADD PRIMARY KEY (`puuid`),
  ADD KEY `idx_lol_accounts_user_id` (`user_id`);

--
-- Index pour la table `tournaments`
--
ALTER TABLE `tournaments`
  ADD PRIMARY KEY (`id`),
  ADD KEY `idx_tournaments_status` (`status`);

--
-- Index pour la table `tournament_participants`
--
ALTER TABLE `tournament_participants`
  ADD PRIMARY KEY (`tournament_id`,`puuid`),
  ADD KEY `idx_tp_puuid` (`puuid`);

--
-- Index pour la table `users`
--
ALTER TABLE `users`
  ADD PRIMARY KEY (`id`),
  ADD UNIQUE KEY `username` (`username`);

--
-- Contraintes pour les tables déchargées
--

--
-- Contraintes pour la table `lol_accounts`
--
ALTER TABLE `lol_accounts`
  ADD CONSTRAINT `fk_lol_accounts_user` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE;

--
-- Contraintes pour la table `tournament_participants`
--
ALTER TABLE `tournament_participants`
  ADD CONSTRAINT `fk_tp_puuid` FOREIGN KEY (`puuid`) REFERENCES `lol_accounts` (`puuid`) ON DELETE CASCADE,
  ADD CONSTRAINT `fk_tp_tournament` FOREIGN KEY (`tournament_id`) REFERENCES `tournaments` (`id`) ON DELETE CASCADE;
COMMIT;

/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
