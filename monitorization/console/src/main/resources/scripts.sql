-- Table: fault_data

-- DROP TABLE fault_data;

CREATE TABLE fault_data
(
  id serial NOT NULL,
  agent_name text NOT NULL,
  event_time timestamp without time zone NOT NULL,
  message character varying(8192),
  short_message text,
  CONSTRAINT fault_data_pkey PRIMARY KEY (id)
)
WITH (
  OIDS=FALSE
);
ALTER TABLE fault_data OWNER TO lneves;


-- Table: raw_data

-- DROP TABLE raw_data;

CREATE TABLE raw_data
(
  agent_name text NOT NULL,
  event_time timestamp with time zone NOT NULL,
  subject text NOT NULL,
  predicate text NOT NULL,
  object_value double precision NOT NULL,
  properties hstore,
  CONSTRAINT raw_data_pkey PRIMARY KEY (agent_name, event_time, subject, predicate)
)
WITH (
  OIDS=FALSE
);
ALTER TABLE raw_data OWNER TO lneves;

-- Function: last_event_for_predicate(text, timestamp with time zone)

-- DROP FUNCTION last_event_for_predicate(text, timestamp with time zone);

CREATE OR REPLACE FUNCTION last_event_for_predicate(text, timestamp with time zone)
  RETURNS double precision AS
$BODY$
	SELECT COALESCE(SUM(raw_data.object_value), 0) FROM
		raw_data
		,(
			SELECT max(event_time) as m_time, agent_name
			FROM raw_data
			WHERE predicate=$1 AND event_time<=$2 AND event_time > $2 - time '00:05' GROUP BY agent_name
		) last_events
	WHERE
		raw_data.event_time = last_events.m_time
		AND raw_data.agent_name = last_events.agent_name
		AND predicate=$1
$BODY$
  LANGUAGE 'sql' VOLATILE
  COST 100;
ALTER FUNCTION last_event_for_predicate(text, timestamp with time zone) OWNER TO postgres;


-- Function: last_event_for_subject_and_predicate(text, text, timestamp with time zone)

-- DROP FUNCTION last_event_for_subject_and_predicate(text, text, timestamp with time zone);

CREATE OR REPLACE FUNCTION last_event_for_subject_and_predicate(text, text, timestamp with time zone)
  RETURNS double precision AS
$BODY$
	SELECT COALESCE(SUM(raw_data.object_value), 0) FROM
		raw_data
		,(
			SELECT max(event_time) as m_time, agent_name
			FROM raw_data
			WHERE subject=$1 AND predicate=$2 AND event_time<=$3 AND event_time > $3 - time '00:05' GROUP BY agent_name
		) last_events
	WHERE
		raw_data.event_time = last_events.m_time
		AND raw_data.agent_name = last_events.agent_name
		AND subject=$1
		AND predicate=$2
$BODY$
  LANGUAGE 'sql' VOLATILE
  COST 100;
ALTER FUNCTION last_event_for_subject_and_predicate(text, text, timestamp with time zone) OWNER TO postgres;

-- Function: last_event_for_subject_predicate_agent(text, text, text, timestamp with time zone)

-- DROP FUNCTION last_event_for_subject_predicate_agent(text, text, text, timestamp with time zone);

CREATE OR REPLACE FUNCTION last_event_for_subject_predicate_agent(text, text, text, timestamp with time zone)
  RETURNS double precision AS
$BODY$

	SELECT COALESCE(object_value, 0)
	FROM raw_data
	WHERE
		subject=$1
		AND predicate=$2
		AND event_time<=$4
		AND event_time > ($4 - time '00:35')
		AND agent_name = $3
	ORDER BY event_time DESC LIMIT 1;
			
$BODY$
  LANGUAGE 'sql' VOLATILE
  COST 100;
ALTER FUNCTION last_event_for_subject_predicate_agent(text, text, text, timestamp with time zone) OWNER TO broker_console;

-- Function: last_event_input_message(timestamp with time zone)

-- DROP FUNCTION last_event_input_message(timestamp with time zone);

CREATE OR REPLACE FUNCTION last_event_input_message(timestamp with time zone)
  RETURNS double precision AS
$BODY$
	SELECT COALESCE(SUM(raw_data.object_value), 0) FROM
		raw_data
		,(
			SELECT max(event_time) as m_time, agent_name
			FROM raw_data
			WHERE predicate = 'input-rate' AND (subject='topic://.*' OR subject='http' OR subject='dropbox' OR subject ~'^queue://' ) AND event_time<=$1 GROUP BY agent_name
		) last_events
	WHERE
		raw_data.event_time = last_events.m_time
		AND raw_data.agent_name = last_events.agent_name
		AND predicate = 'input-rate'
		AND (subject='topic://.*' OR subject='http' OR subject='dropbox' OR subject ~'^queue://' )
$BODY$
  LANGUAGE 'sql' VOLATILE
  COST 100;
ALTER FUNCTION last_event_input_message(timestamp with time zone) OWNER TO postgres;

-- Function: last_event_input_message_for_agent(text, timestamp with time zone)

-- DROP FUNCTION last_event_input_message_for_agent(text, timestamp with time zone);

CREATE OR REPLACE FUNCTION last_event_input_message_for_agent(text, timestamp with time zone)
  RETURNS double precision AS
$BODY$
	SELECT COALESCE(SUM(raw_data.object_value), 0) FROM
		raw_data
		,(
			SELECT max(event_time) as m_time
			FROM raw_data
			WHERE predicate = 'input-rate' AND (subject='topic://.*' OR subject='http' OR subject='dropbox' OR subject ~'^queue://' ) AND event_time<=$2 AND agent_name = $1
		) last_events
	WHERE
		raw_data.event_time = last_events.m_time
		AND predicate = 'input-rate'
		AND agent_name = $1
		AND (subject='topic://.*' OR subject='http' OR subject='dropbox' OR subject ~'^queue://' )
$BODY$
  LANGUAGE 'sql' VOLATILE
  COST 100;
ALTER FUNCTION last_event_input_message_for_agent(text, timestamp with time zone) OWNER TO postgres;

-- Function: last_event_ouput_message(timestamp with time zone)

-- DROP FUNCTION last_event_ouput_message(timestamp with time zone);

CREATE OR REPLACE FUNCTION last_event_ouput_message(timestamp with time zone)
  RETURNS double precision AS
$BODY$
	SELECT COALESCE(SUM(raw_data.object_value), 0) FROM
		raw_data
		,(
			SELECT max(event_time) as m_time, agent_name
			FROM raw_data
			WHERE predicate = 'output-rate' AND (subject ~'^topic://' OR subject ~'^queue://' ) AND event_time<=$1 GROUP BY agent_name
		) last_events
	WHERE
		raw_data.event_time = last_events.m_time
		AND raw_data.agent_name = last_events.agent_name
		AND predicate = 'output-rate'
		AND (subject ~'^topic://' OR subject ~'^queue://' )
$BODY$
  LANGUAGE 'sql' VOLATILE
  COST 100;
ALTER FUNCTION last_event_ouput_message(timestamp with time zone) OWNER TO postgres;



-- Function: f_truncate_raw_data()

-- DROP FUNCTION f_truncate_raw_data();

CREATE OR REPLACE FUNCTION f_truncate_raw_data()
  RETURNS integer AS
$BODY$
DECLARE
chour integer;
BEGIN
    SET TIME ZONE 'UTC';
    chour:= EXTRACT(hour FROM now() + interval '1 hour')::integer % 4;

    EXECUTE 'TRUNCATE raw_data_' || chour; 

    RETURN 0;
END
$BODY$
  LANGUAGE plpgsql VOLATILE
  COST 100;
ALTER FUNCTION f_truncate_raw_data() OWNER TO broker_collector;



-- Function: last_event_for_subject_and_predicate(text, text, timestamp with time zone, text)

-- DROP FUNCTION last_event_for_subject_and_predicate(text, text, timestamp with time zone, text);

CREATE OR REPLACE FUNCTION last_event_for_subject_and_predicate(text, text, timestamp with time zone, text)
  RETURNS double precision AS
$BODY$
	SELECT SUM(object_value) FROM 
	(
		SELECT DISTINCT ON (agent_name, subject) object_value
		FROM raw_data 
		WHERE
		subject=$1
		AND predicate=$2
		AND event_time<=$3
		AND event_time > ($3 - $4::time)
		AND (object_value > 0)
		ORDER BY agent_name, subject, event_time DESC
	) last_events
$BODY$
  LANGUAGE sql VOLATILE
  COST 100;
ALTER FUNCTION last_event_for_subject_and_predicate(text, text, timestamp with time zone, text) OWNER TO broker_collector;


-- Function: last_event_predicate_for_agent(text, text, timestamp with time zone, text)

-- DROP FUNCTION last_event_predicate_for_agent(text, text, timestamp with time zone, text);

CREATE OR REPLACE FUNCTION last_event_predicate_for_agent(text, text, timestamp with time zone, text)
  RETURNS double precision AS
$BODY$
	SELECT SUM(object_value) FROM 
	(
		SELECT DISTINCT ON (subject) object_value
		FROM raw_data 
		WHERE
		predicate=$1
		AND event_time<=$3 AND event_time > ($3 - $4::time)
		AND agent_name = $2
		AND (object_value > 0)
		ORDER BY subject, event_time DESC
	) last_events
$BODY$
  LANGUAGE sql VOLATILE
  COST 100;
ALTER FUNCTION last_event_predicate_for_agent(text, text, timestamp with time zone, text) OWNER TO broker_collector;


-- Function: last_event_ouput_message(timestamp with time zone, text)

-- DROP FUNCTION last_event_ouput_message(timestamp with time zone, text);

CREATE OR REPLACE FUNCTION last_event_ouput_message(timestamp with time zone, text)
  RETURNS double precision AS
$BODY$
	SELECT SUM(object_value) FROM
		(
			SELECT DISTINCT ON(subject, agent_name) object_value
			FROM raw_data
			WHERE predicate = 'output-rate'
			AND (subject ~ '^topic://.*' OR subject ~ '^queue://.*' )
			AND event_time<=$1 AND event_time > ($1 - $2::time)
			AND object_value > 0
			ORDER BY subject, agent_name, event_time DESC
		) last_or
$BODY$
  LANGUAGE sql VOLATILE
  COST 100;
ALTER FUNCTION last_event_ouput_message(timestamp with time zone, text) OWNER TO broker_collector;


-- Function: last_event_for_subject_predicate_agent(text, text, text, timestamp with time zone, text)

-- DROP FUNCTION last_event_for_subject_predicate_agent(text, text, text, timestamp with time zone, text);

CREATE OR REPLACE FUNCTION last_event_for_subject_predicate_agent(text, text, text, timestamp with time zone, text)
  RETURNS double precision AS
$BODY$

	SELECT COALESCE(object_value, 0)
	FROM raw_data
	WHERE
		subject=$1
		AND predicate=$2
		AND event_time<=$4
		AND event_time > ($4 - $5::time)
		AND agent_name = $3
	ORDER BY event_time DESC LIMIT 1;
$BODY$
  LANGUAGE sql VOLATILE
  COST 100;
ALTER FUNCTION last_event_for_subject_predicate_agent(text, text, text, timestamp with time zone, text) OWNER TO broker_collector;


-- Function: last_event_input_message(timestamp with time zone, text)

-- DROP FUNCTION last_event_input_message(timestamp with time zone, text);

CREATE OR REPLACE FUNCTION last_event_input_message(timestamp with time zone, text)
  RETURNS double precision AS
$BODY$
	SELECT SUM(object_value) FROM
		(
			SELECT DISTINCT ON(subject, agent_name) object_value
			FROM raw_data
			WHERE predicate = 'input-rate'
			AND (subject ~ '^topic://.*' OR subject ~ '^queue://.*' )
			AND event_time<=$1 AND event_time > ($1 - $2::time)
			AND object_value > 0
			ORDER BY subject, agent_name, event_time DESC
		) last_ir
$BODY$
  LANGUAGE sql VOLATILE
  COST 100;
ALTER FUNCTION last_event_input_message(timestamp with time zone, text) OWNER TO broker_collector;


-- Function: last_event_for_predicate(text, timestamp with time zone, text)

-- DROP FUNCTION last_event_for_predicate(text, timestamp with time zone, text);

CREATE OR REPLACE FUNCTION last_event_for_predicate(text, timestamp with time zone, text)
  RETURNS double precision AS
$BODY$
	SELECT SUM(object_value) FROM 
	(
		SELECT DISTINCT ON (agent_name, subject) object_value
		FROM raw_data 
		WHERE
		predicate = $1
		AND (event_time<=$2) AND (event_time > ($2 -$3::time))
		AND (object_value > 0)
		ORDER BY agent_name, subject, event_time DESC
	) last_events
$BODY$
  LANGUAGE sql VOLATILE
  COST 100;
ALTER FUNCTION last_event_for_predicate(text, timestamp with time zone, text) OWNER TO broker_console;


-- Function: last_event_input_message_for_agent(text, timestamp with time zone, text)

-- DROP FUNCTION last_event_input_message_for_agent(text, timestamp with time zone, text);

CREATE OR REPLACE FUNCTION last_event_input_message_for_agent(text, timestamp with time zone, text)
  RETURNS double precision AS
$BODY$
	SELECT SUM(object_value) FROM
		(
			SELECT DISTINCT ON(subject, agent_name) object_value
			FROM raw_data
			WHERE predicate = 'input-rate'
			AND (subject ~ '^topic://.*' OR subject ~ '^queue://.*' )
			AND agent_name = $1
			AND event_time <= $2 AND event_time > ($2 - $3::time)
			AND object_value > 0
			ORDER BY subject, agent_name, event_time DESC
		) last_ir
$BODY$
  LANGUAGE sql VOLATILE
  COST 100;
ALTER FUNCTION last_event_input_message_for_agent(text, timestamp with time zone, text) OWNER TO broker_collector;

-- Function: last_event_ouput_message_for_agent(text, timestamp with time zone, text)

-- DROP FUNCTION last_event_ouput_message_for_agent(text, timestamp with time zone, text);

CREATE OR REPLACE FUNCTION last_event_ouput_message_for_agent(text, timestamp with time zone, text)
  RETURNS double precision AS
$BODY$
	SELECT SUM(object_value) FROM
		(
			SELECT DISTINCT ON(subject, agent_name) object_value
			FROM raw_data
			WHERE predicate = 'output-rate'
			AND (subject ~ '^topic://.*' OR subject ~ '^queue://.*' )
			AND agent_name = $1
			AND event_time <= $2 AND event_time > ($2 - $3::time)
			AND object_value > 0
			ORDER BY subject, agent_name, event_time DESC
		) last_or
$BODY$
  LANGUAGE sql VOLATILE
  COST 100;
ALTER FUNCTION last_event_ouput_message_for_agent(text, timestamp with time zone, text) OWNER TO broker_collector;

