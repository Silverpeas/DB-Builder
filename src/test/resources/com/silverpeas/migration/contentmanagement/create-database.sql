
CREATE TABLE SB_ClassifyEngine_Classify 
(
	PositionId int NOT NULL ,
	ObjectId int NOT NULL ,
	Axis0	varchar (150),
	Axis1	varchar (150),
	Axis2	varchar (150),
	Axis3	varchar (150)
);

CREATE TABLE SB_ContentManager_Content 
(
	silverContentId			int		NOT NULL ,
	internalContentId		varchar(100)	NOT NULL ,
	contentInstanceId		int		NOT NULL, 
	authorId			int		NOT NULL,
	creationDate			date		NOT NULL,
	beginDate			varchar(10)	NULL,
	endDate				varchar(10)	NULL,
	isVisible			int		NULL
);

ALTER TABLE SB_ClassifyEngine_Classify ADD 
	 CONSTRAINT PK_ClassifyEngine_Classify PRIMARY KEY
	(
		PositionId
	)   
;
