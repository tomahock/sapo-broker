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
			WHERE predicate=$1 AND event_time<=$2 GROUP BY agent_name
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
			WHERE subject=$1 AND predicate=$2 AND event_time<=$3 GROUP BY agent_name
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



