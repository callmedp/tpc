# Indexing — Deep Dive: Theory

---

## Q1. What is a Database Index?

An **index** is a separate data structure that the database maintains to speed up data retrieval. It works like a book's index: instead of scanning every page (full table scan), you jump directly to the relevant page(s).

### Cost-benefit
| | Without Index | With Index |
|---|---|---|
| SELECT by column | O(n) full scan | O(log n) or O(1) |
| INSERT / UPDATE / DELETE | Fast (just write row) | Slower (must update index too) |
| Storage | Smaller | Extra disk space for index structure |

**Core trade-off:** indexes speed up reads at the cost of slower writes and additional storage. Don't index everything — index selectively.

---

## Q2. B-Tree Index — The Default

The **B-Tree** (Balanced Tree) is the default index structure in PostgreSQL, MySQL (InnoDB), Oracle, SQL Server.

### Structure
```
                    [50]
                  /      \
            [20, 35]      [70, 85]
           /   |   \      /   |   \
         [10][25][40] [60][75][90]
```

- Each internal node holds **keys** and **pointers** to children
- All leaf nodes are at the **same depth** (balanced)
- Keys are sorted → binary search within each node
- Each node is sized to fit a disk page (typically 4KB–16KB)

### B+ Tree (what databases actually use)
The real implementation is **B+ Tree** where:
- **Internal nodes** hold only keys (no data pointers) — more keys fit per page
- **Leaf nodes** hold keys + data pointers + are **linked as a doubly-linked list**
- Range scans are efficient: find start key, then follow leaf pointers

```
Leaf nodes linked:  [1,2,3] ↔ [4,5,6] ↔ [7,8,9]
Range scan [3..7]: find 3, walk forward to 7
```

### Operations
| Operation | Complexity |
|---|---|
| Point lookup | O(log n) |
| Range scan | O(log n + k) where k = results |
| Insert/Delete | O(log n) + page splits/merges |

### B-Tree supports
- `=`, `<`, `>`, `<=`, `>=`, `BETWEEN`, `LIKE 'prefix%'`, `ORDER BY`, `GROUP BY`

### B-Tree does NOT help with
- `LIKE '%suffix'` or `LIKE '%middle%'` — can't use index (no left-anchored prefix)
- Functions on indexed column: `WHERE YEAR(created_at) = 2024` — wraps column, breaks index
- Low-cardinality columns (e.g., boolean) — often full scan is cheaper

---

## Q3. Clustered vs Non-Clustered Index

### Clustered Index
The **table data itself is physically ordered** by the index key. Only ONE clustered index per table.

- In **MySQL InnoDB**: the primary key IS the clustered index (table = B+ Tree with rows in leaves)
- In **PostgreSQL**: there is no "clustered index" concept by default; rows live in the heap regardless. You can `CLUSTER` a table once, but it doesn't stay ordered
- In **SQL Server**: explicitly designated as `CLUSTERED`

```
InnoDB table (clustered on PK = id):
B+ Tree leaf nodes contain the ACTUAL ROW DATA
→ PK lookup = read tree, data is right there (no second hop)
```

### Non-Clustered (Secondary) Index
A **separate structure** that maps indexed column(s) → primary key. Accessing the full row requires a **second lookup** into the clustered index.

```
Secondary index on email:
B+ Tree leaf nodes contain: email → PK (id)
                                         ↓
                              Clustered index lookup by id
                                         ↓
                              Actual row data
```

**InnoDB minutia:** secondary index leaves store the PK value, not physical row pointer. If the PK changes, only the clustered index leaf changes; secondary index is unaffected. This is why InnoDB discourages wide/random PKs.

---

## Q4. Covering Index

An index that contains **all columns needed** by a query, so the database never needs to access the actual table (heap/clustered index).

```sql
-- Query:
SELECT name, email FROM users WHERE department = 'eng';

-- Covering index:
CREATE INDEX idx_dept_name_email ON users(department, name, email);
-- All needed columns in the index → zero table I/O
```

This is called an **index-only scan** in PostgreSQL or a **covering index scan** in MySQL.

### Why it's powerful
- Eliminates the second lookup (the "bookmark lookup" or "heap fetch")
- Especially valuable when the table is very large (random I/O avoided)

### PostgreSQL caveat
Even with a covering index, PostgreSQL must check the **visibility map** (for MVCC) to confirm a row is visible to the current transaction. If the heap page hasn't been vacuumed, a heap fetch is still needed.

---

## Q5. Composite Index and the Left-Prefix Rule

A **composite index** (multi-column index) on `(a, b, c)` is equivalent to sorting rows first by `a`, then by `b` within same `a`, then by `c`.

### Left-prefix rule
An index on `(a, b, c)` can be used for queries that filter on:
- `a` alone ✓
- `a, b` ✓
- `a, b, c` ✓
- `b` alone ✗ (can't skip leading column)
- `a, c` — partial use (uses `a`, but `c` can't be range-scanned without `b`)

```sql
CREATE INDEX idx ON orders(customer_id, status, created_at);

-- Uses full index:
WHERE customer_id = 1 AND status = 'SHIPPED' AND created_at > '2024-01-01'

-- Uses 2 columns:
WHERE customer_id = 1 AND status = 'SHIPPED'

-- Uses 1 column:
WHERE customer_id = 1

-- Can't use index effectively:
WHERE status = 'SHIPPED'           -- skips customer_id
WHERE created_at > '2024-01-01'    -- skips both leading columns
```

### Range condition breaks further use
```sql
WHERE customer_id = 1 AND created_at > '2024-01-01' AND status = 'SHIPPED'
-- Uses: customer_id (equality), created_at (range)
-- CANNOT use: status (after range column, index is exhausted for filtering)
```

**Column order rule:** Put **equality columns first**, then **range columns last**.

---

## Q6. Index Cardinality and Selectivity

**Cardinality** = number of distinct values in a column.

**Selectivity** = cardinality / total rows. Ranges from 0 (useless) to 1 (unique).

| Column | Example | Cardinality | Selectivity | Index value |
|---|---|---|---|---|
| `id` | 1M unique values | 1,000,000 | 1.0 | Excellent |
| `email` | ~1M unique | High | ~1.0 | Excellent |
| `status` | 5 values | 5 | 0.000005 | Usually poor |
| `gender` | 2-3 values | 2 | ~0.5 | Very poor |
| `country` | 195 | 195 | Moderate | Context-dependent |

### When low-cardinality index can still help
If you have `WHERE status = 'FAILED'` and only 0.1% of rows are FAILED, a **partial index** on `status = 'FAILED'` is highly selective and valuable.

---

## Q7. Hash Index

### Structure
A **hash table**: `hash(value) → row pointer`.

| Operation | Complexity |
|---|---|
| Exact lookup `=` | O(1) |
| Range scan `<`, `>`, `BETWEEN` | NOT SUPPORTED |
| Ordering | NOT SUPPORTED |

### When to use
- Only for **exact equality lookups**
- Never for range queries or sorting

### Database support
- **PostgreSQL**: explicit `CREATE INDEX USING HASH`; rarely used (B-Tree is just as fast for equality in most cases due to page caching)
- **MySQL InnoDB**: adaptive hash index — automatically creates an in-memory hash index on B-Tree leaves for frequently accessed pages (transparent, not user-configurable)
- **MySQL MEMORY engine**: supports hash indexes explicitly
- **Redis**: all key lookups are hash-based

---

## Q8. Bitmap Index

### Structure
For each distinct value, maintain a **bit array** of 1/0 for each row.

```
status = 'ACTIVE':   [1, 0, 1, 1, 0, 1, 0, 0, 1]
status = 'INACTIVE': [0, 1, 0, 0, 1, 0, 1, 0, 0]
status = 'DELETED':  [0, 0, 0, 0, 0, 0, 0, 1, 0]
```

### Strengths
- **AND/OR/NOT** operations = bitwise ops → extremely fast for multi-condition queries
- Excellent for **low-cardinality** columns
- Compact storage (1 bit per row)
- Ideal for data warehouses / OLAP

### Weaknesses
- **Very expensive to update** — any row modification can require rewriting large parts of the bitmap
- **Not suitable for OLTP** (high write rate)

### Where it's used
- **Oracle**: explicit bitmap indexes
- **Parquet/column stores**: bitmap encoding for column filtering
- **Elasticsearch**: uses bitsets for filter caching
- **Roaring Bitmaps**: modern compressed bitmap used in analytics engines (Druid, Spark, ClickHouse)

---

## Q9. Full-Text Index and Inverted Index

### Full-text index
Used for **natural language search** within text columns. Regular B-Tree can't do `LIKE '%word%'`.

### Inverted Index
The data structure behind full-text search:

```
Document 1: "kafka is fast"
Document 2: "kafka streams are powerful"
Document 3: "fast stream processing"

Inverted index:
"kafka"      → [doc1, doc2]
"fast"       → [doc1, doc3]
"streams"    → [doc2, doc3]
"powerful"   → [doc2]
"processing" → [doc3]
```

**Query `"kafka fast"`** → intersect [doc1, doc2] ∩ [doc1, doc3] = [doc1]

### Databases
| DB | Full-Text Support |
|---|---|
| **PostgreSQL** | `tsvector` + `tsquery`; GIN index on `tsvector` |
| **MySQL** | `FULLTEXT` index on MyISAM / InnoDB (5.6+) |
| **Elasticsearch** | Built entirely on inverted index (Lucene) |
| **MongoDB** | Text index using inverted index |

### PostgreSQL example
```sql
ALTER TABLE articles ADD COLUMN tsv tsvector;
UPDATE articles SET tsv = to_tsvector('english', title || ' ' || body);
CREATE INDEX idx_articles_tsv ON articles USING GIN(tsv);
SELECT * FROM articles WHERE tsv @@ to_tsquery('kafka & stream');
```

---

## Q10. Partial Index

An index built on a **subset of rows** (those satisfying a WHERE condition).

```sql
-- Only index unshipped orders (99% of rows are shipped — ignore them)
CREATE INDEX idx_unshipped ON orders(customer_id)
WHERE status = 'UNSHIPPED';

-- Only index non-null values
CREATE INDEX idx_phone ON users(phone)
WHERE phone IS NOT NULL;
```

### Benefits
- Smaller index → faster maintenance, fits in memory
- Query planner uses it only when WHERE clause matches the partial condition
- Highly selective even on low-cardinality columns

### Use cases
- Soft-delete patterns: index only `WHERE deleted_at IS NULL`
- Queue-like tables: index only `WHERE processed = false`
- Exception handling: `WHERE error_code IS NOT NULL`

---

## Q11. Expression / Functional Index

Index the **result of a function** rather than the raw column value.

```sql
-- Case-insensitive email lookup:
CREATE INDEX idx_lower_email ON users(LOWER(email));
-- Query MUST use the same expression:
SELECT * FROM users WHERE LOWER(email) = 'john@example.com';

-- Date extraction:
CREATE INDEX idx_year ON events(EXTRACT(YEAR FROM created_at));
SELECT * FROM events WHERE EXTRACT(YEAR FROM created_at) = 2024;
```

### Why it matters
```sql
-- This CANNOT use an index on created_at:
WHERE YEAR(created_at) = 2024       -- function wraps the column

-- Options:
-- 1) Rewrite to range:
WHERE created_at >= '2024-01-01' AND created_at < '2025-01-01'
-- 2) Create functional index on YEAR(created_at)
```

---

## Q12. Index in PostgreSQL Internals (Heap + MVCC)

### Heap-based storage
PostgreSQL stores rows in **heap files** (unordered). The table and all its indexes are separate files. Every index entry contains a pointer to the physical row location: `(page_number, item_offset)` = **ctid**.

### HOT (Heap-Only Tuple) updates
When a row is updated and the updated columns are **not indexed**:
- PostgreSQL creates a new row version in the same heap page
- Links old → new via a HOT chain
- Index is NOT updated (the old ctid still works — the chain leads to the new version)
- Eliminates index bloat for updates that don't touch indexed columns

### MVCC and index-only scans
PostgreSQL's MVCC stores row visibility information in the heap. An index-only scan must verify that the returned row is visible to the current snapshot. It uses the **visibility map** — a bitmap tracking which heap pages have only live tuples. If a page is marked all-visible, the index-only scan skips the heap fetch.

```
VACUUM maintains the visibility map.
Tables with frequent UPDATEs need regular VACUUM for index-only scans to be effective.
```

### Index types in PostgreSQL
| Index type | Use case |
|---|---|
| `BTREE` | Default, range/equality |
| `HASH` | Equality only |
| `GIN` | Arrays, JSONB, full-text (many keys → one row) |
| `GiST` | Geometric types, range types, nearest-neighbor |
| `BRIN` | Block Range Index — huge sequential tables (timestamps) |
| `SP-GiST` | Space-partitioned GiST; IP addresses, phone numbers |

---

## Q13. MySQL InnoDB Indexing Internals

### InnoDB primary key = clustered index
The **table IS a B+ Tree** keyed by the primary key. Leaf nodes contain the entire row.

```
Clustered index leaf node:
| PK | col1 | col2 | col3 | ... all columns |
```

### Secondary indexes reference the PK
Secondary index leaf nodes contain: `indexed_column(s) → PK`.

**Double lookup:** secondary index lookup → finds PK → clustered index lookup → finds row.

### PK design implications
- **Small PK** (INT, BIGINT): secondary indexes are compact
- **UUID PK**: 16 bytes per secondary index entry vs 8 for BIGINT. Also, random UUID inserts cause **B+ Tree page splits** = fragmentation = poor write performance
- `AUTO_INCREMENT` PK: always appends to the rightmost leaf → minimal splits → fast inserts

### InnoDB Adaptive Hash Index (AHI)
InnoDB maintains an in-memory hash index automatically for frequently accessed B-Tree pages. If the same index lookup is seen repeatedly, InnoDB caches it as a hash lookup (O(1)). This is transparent and automatic.

---

## Q14. EXPLAIN / EXPLAIN ANALYZE

### PostgreSQL
```sql
EXPLAIN ANALYZE SELECT * FROM orders WHERE customer_id = 42;
```

Key output fields:
| Field | Meaning |
|---|---|
| `Seq Scan` | Full table scan — no index used |
| `Index Scan` | Index used; still fetches heap rows |
| `Index Only Scan` | Covering index — no heap fetch |
| `Bitmap Index Scan` | Multiple index lookups combined via bitmap then heap fetched in one pass |
| `cost=X..Y` | X=startup cost, Y=total cost (in arbitrary units) |
| `rows=N` | Planner's estimate of rows returned |
| `actual time=X..Y` | Real execution time (milliseconds) |
| `actual rows=N` | Actual rows returned |
| `Buffers: hit=N` | Pages read from memory (cache hit) |
| `Buffers: read=N` | Pages read from disk (cache miss) |

**Large estimate vs actual rows discrepancy = stale statistics. Run `ANALYZE` to update.**

### MySQL
```sql
EXPLAIN SELECT * FROM orders WHERE customer_id = 42;
```

Key fields:
| Field | Meaning |
|---|---|
| `type` | Join type: `const` > `eq_ref` > `ref` > `range` > `index` > `ALL` (bad) |
| `key` | Index used |
| `rows` | Estimated rows examined |
| `Extra` | `Using index` (covering), `Using filesort` (bad), `Using temporary` (bad) |

`ALL` in the `type` column means full table scan — almost always needs an index.

---

## Q15. LSM Tree (Log-Structured Merge Tree)

Used in **write-optimized** storage engines: Cassandra, HBase, RocksDB, LevelDB.

### Problem with B-Trees for writes
B-Tree random writes require seeking to the correct leaf page. On HDD this is expensive. On SSD, random writes cause write amplification and wear.

### LSM approach
Convert random writes → sequential writes:

1. **MemTable**: in-memory sorted structure (red-black tree / skip list). All writes go here first.
2. When MemTable fills up → flush to disk as an **SSTable** (Sorted String Table) — immutable, sorted, sequential write.
3. Periodic **compaction**: merge multiple SSTables into larger, sorted SSTables; discard deleted/overwritten keys.

```
Write path: MemTable → SSTable_L0 → SSTable_L1 → SSTable_L2 (compaction)
Read path: MemTable → SSTable_L0 → SSTable_L1 → SSTable_L2 (check each level)
```

### Read amplification
LSM must check multiple levels on read → use **Bloom filters** per SSTable to skip SSTables that definitely don't contain the key.

### Write amplification
Compaction rewrites data multiple times. Tuning: more levels = lower read amp but higher write amp.

### vs B-Tree
| | B-Tree | LSM |
|---|---|---|
| Write speed | Moderate (random I/O) | Very fast (sequential) |
| Read speed | Fast | Moderate (must check levels) |
| Space amplification | Low | Higher (duplicates until compaction) |
| Best for | Read-heavy OLTP | Write-heavy workloads |

---

## Q16. Index in NoSQL

### MongoDB
```javascript
db.orders.createIndex({ customer_id: 1 })        // ascending B-Tree
db.orders.createIndex({ customer_id: 1, status: 1 }) // compound
db.orders.createIndex({ location: "2dsphere" })   // geospatial
db.orders.createIndex({ name: "text" })           // full-text
db.orders.createIndex({ ttl_field: 1 }, { expireAfterSeconds: 3600 }) // TTL index
```

- Default `_id` index is unique
- Secondary indexes store `{ indexed_field → _id }` → document fetched by `_id`
- Covered query: when projected fields are all in the index (like PostgreSQL index-only scan)
- Compound index left-prefix rule applies same as SQL

### Elasticsearch
- Every field is indexed by default (inverted index)
- `keyword` type: exact match (not analyzed)
- `text` type: full-text analyzed (tokenized)
- Doc values: column-oriented storage for sorting/aggregation (like a separate index per field in column format)
- Shard-level indexing: each shard is an independent Lucene index

### Cassandra
- Primary index = partition key (consistent hash → node lookup)
- Clustering columns: B-Tree within partition (sorting)
- Secondary indexes: local indexes (per node) — avoid for high-cardinality columns
- Materialized views: denormalized tables with different partition key

---

## Q17. OLTP vs OLAP Indexing Strategies

| Aspect | OLTP | OLAP / Data Warehouse |
|---|---|---|
| Workload | Many small reads/writes | Few very large reads |
| Index type | B-Tree (selective columns) | Bitmap, column store |
| Column store | Rare | Default (Redshift, BigQuery, Snowflake, ClickHouse) |
| Partitioning | By tenant/date | By date range, region |
| Goal | Fast point queries | Fast full-column aggregations |
| Write penalty | Must minimize | Acceptable (bulk loads) |

### Column-store indexing
In columnar databases, each column is stored contiguously:
```
Traditional row store:  [id, name, age, dept] [id, name, age, dept] ...
Columnar store:         [id1, id2, id3...] | [name1, name2...] | [age1, age2...]
```
- Reading a single column = sequential scan of one file → excellent compression + vectorized execution
- No B-Tree needed — just scan the column
- Predicate pushdown: filter at column-read time (skip blocks using min/max metadata = **zone maps**)

---

## Q18. Query Optimizer and Index Selection

The query optimizer's job: given a query, pick the **lowest-cost execution plan**.

### How it decides which index to use
1. **Statistics**: `pg_statistics` (PostgreSQL) / `information_schema.TABLE_STATISTICS` (MySQL) — histogram of column value distribution
2. **Cost model**: estimates I/O pages + CPU operations for each plan
3. **Selectivity estimate**: based on statistics, how many rows will each predicate filter?

### When the optimizer chooses full scan over index
- Table is small (full scan costs fewer pages than index + heap lookups)
- Column has low selectivity (15-20%+ of rows → full scan often cheaper due to random I/O of index lookups)
- Statistics are stale → bad estimate → wrong plan
- Function wrapped around indexed column

### Forcing index use (hints)
```sql
-- MySQL
SELECT * FROM orders FORCE INDEX (idx_customer) WHERE customer_id = 1;

-- PostgreSQL (no hints — instead disable other options)
SET enable_seqscan = off;
EXPLAIN SELECT ...;
SET enable_seqscan = on;

-- Oracle
SELECT /*+ INDEX(orders idx_customer) */ * FROM orders WHERE ...;
```

---

## Q19. Index Maintenance Overhead

### Write penalty
Every INSERT/UPDATE/DELETE must update all indexes on the table. Table with 5 indexes → 6 writes per row change.

### Index bloat (PostgreSQL)
- MVCC: dead row versions accumulate in heap and index pages
- Old index entries pointing to dead tuples accumulate
- `VACUUM` reclaims space; `VACUUM FULL` rewrites table (locks table)
- Monitor with: `pgstattuple` extension

```sql
SELECT schemaname, tablename, n_dead_tup, n_live_tup
FROM pg_stat_user_tables ORDER BY n_dead_tup DESC;
```

### Index fragmentation (MySQL / SQL Server)
- Page splits on B-Tree insert → pages become partially filled
- `ANALYZE TABLE` (MySQL) / `REBUILD INDEX` (SQL Server) to defragment

### When to drop an index
- Index not used (check `pg_stat_user_indexes.idx_scan = 0`)
- Duplicate index (same columns in different order covered by another)
- Write-heavy table with low-selectivity column

---

## Q20. Common Indexing Mistakes and Gotchas

1. **Indexing every column** — index maintenance overhead slows writes more than the read benefit.

2. **Not indexing foreign keys** — every FK lookup from child → parent scans child table. Always index FK columns.

3. **Using `LIKE '%prefix'` expecting index** — `LIKE '%abc'` cannot use a B-Tree index (no left anchor). Use full-text search or reverse the string and use `LIKE 'cba%'` on a reverse index.

4. **Implicit type conversion breaks index** — `WHERE user_id = '123'` when `user_id` is INT. DB converts string to int OR casts column → index unusable. Always match types.

5. **NULL and indexes** — B-Tree indexes in most DBs DO include NULL values. But `IS NULL` / `IS NOT NULL` may not use indexes (DB-dependent). PostgreSQL includes NULLs in B-Tree; they appear at the "end" by default.

6. **`SELECT *` prevents index-only scan** — always select only needed columns when possible.

7. **High-write table with many indexes** — each additional index costs one extra write per DML. Profile before adding.

8. **Stale statistics mislead optimizer** — run `ANALYZE` (PostgreSQL) or `ANALYZE TABLE` (MySQL) after bulk loads.

9. **Wrong composite index column order** — putting range column before equality column wastes the index. Equality columns must come first.

10. **UUID primary keys in InnoDB** — random UUIDs cause hot-spot inserts at random positions in the B+ Tree → constant page splits → severe fragmentation. Use UUIDv7 (time-ordered) or ULID instead, or use BIGINT AUTO_INCREMENT.

11. **Over-relying on index hints** — hints prevent the optimizer from adapting. Fix underlying statistics or schema instead.

12. **No index on `WHERE` clause of large JOINs** — the join column on the larger table must be indexed, or you get a nested-loop full scan.
