-- MySQL dump 10.13  Distrib 8.0.32, for Win64 (x86_64)
--
-- Host: localhost    Database: ElectronicSystemDb
-- ------------------------------------------------------
-- Server version	8.0.32

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!50503 SET NAMES utf8mb4 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Current Database: `ElectronicSystemDb`
--

/*!40000 DROP DATABASE IF EXISTS `ElectronicSystemDb`*/;

CREATE DATABASE /*!32312 IF NOT EXISTS*/ `ElectronicSystemDb` /*!40100 DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci */ /*!80016 DEFAULT ENCRYPTION='N' */;

USE `ElectronicSystemDb`;

--
-- Table structure for table `customerstable`
--

DROP TABLE IF EXISTS `customerstable`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `customerstable` (
  `id` int NOT NULL AUTO_INCREMENT,
  `fullname` varchar(100) DEFAULT NULL,
  `email` varchar(100) DEFAULT NULL,
  `contact_number` varchar(20) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=7 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `customerstable`
--

LOCK TABLES `customerstable` WRITE;
/*!40000 ALTER TABLE `customerstable` DISABLE KEYS */;
INSERT INTO `customerstable` VALUES (1,'Test','test@gmail.com','12345678'),(2,'Test','testing@gmail.com','1234567822'),(4,'kaybee','kb@gmail.com','34567654'),(6,'rent','ffr','123456789');
/*!40000 ALTER TABLE `customerstable` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `productstable`
--

DROP TABLE IF EXISTS `productstable`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `productstable` (
  `prod_id` int NOT NULL AUTO_INCREMENT,
  `product_Name` varchar(100) DEFAULT NULL,
  `price` decimal(10,2) DEFAULT NULL,
  `stock` int DEFAULT NULL,
  PRIMARY KEY (`prod_id`)
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `productstable`
--

LOCK TABLES `productstable` WRITE;
/*!40000 ALTER TABLE `productstable` DISABLE KEYS */;
INSERT INTO `productstable` VALUES (1,'bible',360.00,20),(2,'wine',79.99,90);
/*!40000 ALTER TABLE `productstable` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `user_roles`
--

DROP TABLE IF EXISTS `user_roles`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `user_roles` (
  `id` int NOT NULL AUTO_INCREMENT,
  `roleName` varchar(50) NOT NULL,
  `description` text,
  PRIMARY KEY (`id`),
  UNIQUE KEY `roleName` (`roleName`)
) ENGINE=InnoDB AUTO_INCREMENT=6 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `user_roles`
--

LOCK TABLES `user_roles` WRITE;
/*!40000 ALTER TABLE `user_roles` DISABLE KEYS */;
INSERT INTO `user_roles` VALUES (1,'Administrator','Full system access'),(2,'Manager','Management level access'),(5,'sales clerk','Basic user access');
/*!40000 ALTER TABLE `user_roles` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `users`
--

DROP TABLE IF EXISTS `users`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `users` (
  `id` int NOT NULL AUTO_INCREMENT,
  `firstName` varchar(50) NOT NULL,
  `lastName` varchar(50) NOT NULL,
  `username` varchar(100) NOT NULL,
  `email` varchar(100) NOT NULL,
  `password` varchar(255) NOT NULL,
  `phoneNumber` varchar(20) DEFAULT NULL,
  `userRole` varchar(50) DEFAULT 'User',
  `inventoryManagement` tinyint(1) DEFAULT '0',
  `customerManagement` tinyint(1) DEFAULT '0',
  `salesManagement` tinyint(1) DEFAULT '0',
  `userManagement` tinyint(1) DEFAULT '0',
  `dataBackupRestore` tinyint(1) DEFAULT '0',
  `generateReport` tinyint(1) DEFAULT '0',
  `isActive` tinyint(1) DEFAULT '1',
  `createdAt` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `updatedAt` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `lastLogin` datetime DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `username` (`username`),
  UNIQUE KEY `email` (`email`)
) ENGINE=InnoDB AUTO_INCREMENT=9 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `users`
--

LOCK TABLES `users` WRITE;
/*!40000 ALTER TABLE `users` DISABLE KEYS */;
INSERT INTO `users` VALUES (1,'Admin','User','admin','admin@business.com','admin123','1234567890','Administrator',1,1,1,1,1,1,1,'2025-06-17 00:53:02','2025-06-19 14:09:04','2025-06-19 16:09:04'),(2,'John','Doe','johndoe','john@business.com','john123','0987654321','Manager',1,1,1,0,0,1,1,'2025-06-17 00:53:02','2025-06-17 00:53:02',NULL),(3,'Jane','Smith','janesmith','jane@business.com','jane123','1122334455','Employee',1,0,0,0,0,0,1,'2025-06-17 00:53:02','2025-06-17 00:53:02',NULL),(4,'ff','xx','ddd','we@gmail.com','a8f30363cdcdbc59862447a675538968981b79daac72a64ccc6782c55bd13480','1234567890','Administrator',0,1,1,0,0,0,1,'2025-06-17 01:38:21','2025-06-17 01:38:21',NULL),(5,'gen','mts','geng','stk@gmail.com','f3cd279ca1cb1b468e36ca55f15425035c18aad3de36472f49d01a9b246067ed','1234567894','Employee',1,1,0,0,0,0,0,'2025-06-17 19:53:46','2025-06-17 19:53:46',NULL),(6,'now','at','admin2','srr@gmail.com','f3cd279ca1cb1b468e36ca55f15425035c18aad3de36472f49d01a9b246067ed','1234567899','Administrator',1,0,0,1,0,0,1,'2025-06-17 21:39:41','2025-06-17 21:46:46',NULL),(7,'senzo','zungu','senzos','zungu@gmail.com','aca58723fffe0073e55e1436a7f5f222ad3b90286897339010bb6351dfd91a05','1234567899','Administrator',0,1,0,1,0,0,1,'2025-06-17 21:44:50','2025-06-17 21:49:03','2025-06-17 23:49:03'),(8,'tester','testers','klerk','kt@gmail.com','7a959ced278895253d76b5b5fd33dec4e7ed2988b8905f53ceea542e8c931664','1234567899','sales clerk',0,0,0,0,0,0,1,'2025-06-17 23:00:17','2025-06-19 14:35:58','2025-06-19 16:35:58');
/*!40000 ALTER TABLE `users` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2025-06-19 21:37:28
