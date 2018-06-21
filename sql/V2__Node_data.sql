CREATE TABLE node_headers (
  id VARCHAR(64) NOT NULL PRIMARY KEY,
  parent_id VARCHAR(64) NOT NULL,
  version SMALLINT NOT NULL,
  height BIGINT NOT NULL,
  n_bits BIGINT NOT NULL,
  difficulty BIGINT NOT NULL,
  timestamp BIGINT NOT NULL,
  state_root VARCHAR(66) NOT NULL,
  ad_proofs_root VARCHAR(64) NOT NULL,
  transactions_root VARCHAR(64) NOT NULL,
  extension_hash VARCHAR(64) NOT NULL,
  equihash_solutions VARCHAR NOT NULL,
  interlinks VARCHAR ARRAY NOT NULL
);

CREATE INDEX "node_headers__parent_id" ON node_headers (parent_id);
CREATE INDEX "node_headers__height" ON node_headers (height);
CREATE INDEX "node_headers__ts" ON node_headers (timestamp);

ALTER TABLE node_headers OWNER TO ergo;

CREATE TABLE node_transactions (
  id VARCHAR(64) NOT NULL PRIMARY KEY,
  header_id VARCHAR(64) NOT NULL,
  coinbase BOOLEAN NOT NULL,
  timestamp BIGINT NOT NULL
);

ALTER TABLE node_transactions OWNER TO ergo;

CREATE INDEX "node_transactions__header_id" on node_transactions (header_id);
CREATE INDEX "node_transactions__timestamp" on node_transactions (timestamp);

CREATE TABLE node_inputs (
  box_id VARCHAR(64) NOT NULL PRIMARY KEY,
  tx_id VARCHAR(64) NOT NULL,
  proof_bytes VARCHAR NOT NULL,
  extension JSON NOT NULL
);

ALTER TABLE node_inputs OWNER TO ergo;

CREATE INDEX "node_inputs__tx_id" on node_inputs (tx_id);

CREATE TABLE node_outputs (
  box_id VARCHAR(64) NOT NULL PRIMARY KEY,
  tx_id VARCHAR(64) NOT NULL,
  value BIGINT NOT NULL,
  index INTEGER NOT NULL,
  proposition VARCHAR NOT NULL,
  hash VARCHAR NOT NULL,
  additional_registers JSON NOT NULL
);

ALTER TABLE node_outputs OWNER to ergo;

CREATE INDEX "node_outputs__tx_id" on node_outputs (tx_id);

CREATE TABLE node_ad_proofs (
  header_id VARCHAR(64) NOT NULL PRIMARY KEY,
  proof_bytes VARCHAR NOT NULL,
  digest VARCHAR NOT NULL
);

ALTER TABLE node_ad_proofs OWNER to ergo;
