/* Need to switch of FK check for MySQL since there are crosswise FK references */
SET FOREIGN_KEY_CHECKS = 0;;

CREATE TABLE IF NOT EXISTS Game (
  gameID int NOT NULL UNIQUE AUTO_INCREMENT,
  
  name varchar(255),

  phase tinyint,
  step tinyint,
  currentPlayer tinyint NULL,

  boardName varchar(255),
  
  PRIMARY KEY (gameID),
  FOREIGN KEY (gameID, currentPlayer) REFERENCES Player(gameID, playerID)
);;
  
CREATE TABLE IF NOT EXISTS Player (
  gameID int NOT NULL,
  playerID tinyint NOT NULL,

  name varchar(255),
  colour varchar(31),
  
  positionX int,
  positionY int,
  heading tinyint,
  score int,
  health int,
  checkpointsReached int,

  proKort1 varchar(255),
  proKort2 varchar(255),
  proKort3 varchar(255),
  proKort4 varchar(255),
  proKort5 varchar(255),

  comKort1 varchar(255),
  comKort2 varchar(255),
  comKort3 varchar(255),
  comKort4 varchar(255),
  comKort5 varchar(255),
  comKort6 varchar(255),
  comKort7 varchar(255),
  comKort8 varchar(255),

  PRIMARY KEY (gameID, playerID),
  FOREIGN KEY (gameID) REFERENCES Game(gameID)
);;

SET FOREIGN_KEY_CHECKS = 1;;

