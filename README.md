# feeder-crawler-index-builder

This is a command line app that implements feeder -> crawler -> index builder pipeline. RabbitMQ is used as message broker between three
components. The feeder reads raw queries from a file, send it to queue *q_feeds*. The crawler consumes *q_feeds* and craws ads data from
Amazon.com, then publish the data to *q_product*. The index builder consumes *q_product*, builds forward index and invert index, and saves
it to MySQL and Memcached, respectively.

1. clone the project
```
git clone https://github.com/zjuzhanxf/feeder-crawler-index-builder.git
```
2. open the project with Intellij
3. Use brew to install memcached and start on port 11211
```
brew install memcached
/usr/local/bin/memcached -d -p 11211
```
4. Install rabbitMQ 
https://www.rabbitmq.com/ <br>
Start rabbitMQ server
```
/usr/local/sbin/rabbitmq-server
```

5. Install MySQL and MySQLWorkbench, connect to MySQL, create schema search_ads_demo. Create table ads with below query commands.
```
CREATE SCHEMA `search_ads_demo`;

CREATE TABLE `search_ads_demo`.`ad` (
  `adId` INT(11) NOT NULL AUTO_INCREMENT,
  `campaignId` INT(11) NULL DEFAULT NULL,
  `keyWords` VARCHAR(1024) NULL DEFAULT NULL,
  `relevanceScore` DOUBLE NULL DEFAULT NULL,
  `pClick` DOUBLE NULL DEFAULT NULL,
  `bidPrice` DOUBLE NULL DEFAULT NULL,
  `rankScore` DOUBLE NULL DEFAULT NULL,
  `qualityScore` DOUBLE NULL DEFAULT NULL,
  `costPerClick` DOUBLE NULL DEFAULT NULL,
  `position` INT(11) NULL DEFAULT NULL,
  `title` VARCHAR(2048) NULL DEFAULT NULL,
  `price` DOUBLE NULL DEFAULT NULL,
  `thumbnail` MEDIUMTEXT NULL DEFAULT NULL,
  `description` MEDIUMTEXT NULL DEFAULT NULL,
  `brand` VARCHAR(100) NULL DEFAULT NULL,
  `detail_url` MEDIUMTEXT NULL DEFAULT NULL,
  `query` VARCHAR(1024) NULL DEFAULT NULL,
  `query_group_id` INT(11) NULL DEFAULT NULL,
  `category` VARCHAR(1024) NULL DEFAULT NULL,
  PRIMARY KEY (`adId`));
```
6. In Intellij, go to Run -> Edit Configurations -> Program Arguments,
configure the input arguments for FeederMain.java and CrawlerMain.java as rawQuery3.txt and proxylist_bittiger.csv, respectively.
The two files are in the src/resouces folder
7. Run the FeederMain.java, CrawlerMain.java and IndexBuilderMain.java. 
