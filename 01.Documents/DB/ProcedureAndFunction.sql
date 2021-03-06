-- DROP PROCEDURE IF EXISTS INSERT_STN_ORDER_ITEM;
DELIMITER $$
CREATE PROCEDURE INSERT_STN_ORDER_ITEM(_ORDER_SETLE_NO bigint, _ORDER_NO decimal(10,0), OUT RESULT INT)
BEGIN
	DECLARE exit handler for SQLEXCEPTION
	BEGIN
		ROLLBACK;
		SET RESULT = -1;
	END;

	INSERT INTO STN_ORDER_DLVY (
		ORDER_NO
		,ORDER_SETLE_NO
		,ORDER_KND_CODE
		,SBSCRPT_CYCLE_SE_CODE
		,SBSCRPT_WEEK_CYCLE
		,SBSCRPT_DLVY_WD
		,SBSCRPT_MT_CYCLE
		,SBSCRPT_DLVY_DAY
		,ORDER_ODR
		,ORDER_CO
		,SLE_AMOUNT
		,DLVY_AMOUNT
		,DLVY_USER_NM
		,TELNO
		,DLVY_ZIP
		,DLVY_ADRES
		,DLVY_ADRES_DETAIL
		,DLVY_MSSAGE
		,DLVY_STTUS_CODE
		,HDRY_ID
		,HDRY_DLVY_DE
		,INVC_NO
		,REGIST_PNTTM
	)
	SELECT
		_ORDER_NO
		,_ORDER_SETLE_NO
		,o.ORDER_KND_CODE
		,o.SBSCRPT_CYCLE_SE_CODE
		,o.SBSCRPT_WEEK_CYCLE
		,o.SBSCRPT_DLVY_WD
		,o.SBSCRPT_MT_CYCLE
		,o.SBSCRPT_DLVY_DAY
		,o.NOW_ODR
		,o.ORDER_CO
		,o.TOT_AMOUNT 
		,o.DLVY_AMOUNT
		,o.DLVY_USER_NM
		,o.TELNO
		,o.DLVY_ZIP
		,o.DLVY_ADRES
		,o.DLVY_ADRES_DETAIL
		,o.DLVY_MSSAGE
		,'DLVY01' -- 배송준비
		,h.HDRY_ID 
		,NULL
		,NULL 
		,SYSDATE()
	FROM	STN_ORDER o
	JOIN	STN_GOODS g ON g.GOODS_ID = o.GOODS_ID AND g.USE_AT = 'Y'
	JOIN	STN_PRTNR_CMPNY_MAPNG  p ON p.PCMAPNG_ID = g.PCMAPNG_ID AND p.USE_AT ='Y'
	JOIN	STN_CMPNY c ON c.CMPNY_ID = p.CMPNY_ID AND c.USE_AT = 'Y'
	JOIN	STN_CMPNY_HDRY_MAPNG hm ON hm.CMPNY_ID = c.CMPNY_ID 
	JOIN	STN_HDRY_CMPNY h ON h.HDRY_ID = hm.HDRY_ID AND h.USE_AT = 'Y'
	WHERE	o.ORDER_NO = _ORDER_NO
		AND	o.USE_AT = 'Y'
	;

	SET RESULT = 0;
END $$
DELIMITER ;