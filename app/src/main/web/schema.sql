DROP TABLE IF EXISTS cmov_login CASCADE;
DROP FUNCTION IF EXISTS cmov_insert_login(e TEXT, n TEXT, p TEXT, k TEXT) CASCADE;
DROP FUNCTION IF EXISTS cmov_login(e TEXT, p TEXT) CASCADE;
DROP FUNCTION IF EXISTS cmov_active_account(e TEXT, k TEXT) CASCADE;



CREATE TABLE cmov_login
(
  email TEXT NOT NULL,
  nickname TEXT NOT NULL,
  password TEXT NOT NULL,
  key TEXT NOT NULL,
  not_active BOOLEAN NOT NULL,
  CONSTRAINT cmov_login_key PRIMARY KEY (email)
);



CREATE FUNCTION cmov_insert_login(e TEXT, n TEXT, p TEXT, k TEXT) RETURNS VOID AS
$$
BEGIN
  IF EXISTS(SELECT * FROM cmov_login WHERE email = e AND not_active = TRUE)
  THEN UPDATE cmov_login SET nickname = n, password = p, key = k WHERE email = e;
  ELSE INSERT INTO cmov_login values (e, n, p, k, TRUE);
  END IF;
END;
$$
LANGUAGE 'plpgsql';



CREATE FUNCTION cmov_login(e TEXT, p TEXT) RETURNS TABLE(n TEXT) AS
$$
BEGIN
  RETURN QUERY SELECT nickname FROM cmov_login WHERE email = e AND password = p AND not_active = FALSE;
END;
$$
LANGUAGE 'plpgsql';



CREATE FUNCTION cmov_active_account(e TEXT, k TEXT) RETURNS BOOLEAN AS
$$
BEGIN
	IF EXISTS(SELECT * FROM cmov_login WHERE email = e AND key = k)
	THEN 
		 UPDATE cmov_login SET not_active = FALSE WHERE email = e;
		 RETURN TRUE;
  	ELSE RETURN FALSE;
  	END IF;
END;
$$
LANGUAGE 'plpgsql';

