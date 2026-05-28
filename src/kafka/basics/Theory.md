# Kafka — Basics & Deep Dive: Theory

---

## Q1. What is Apache Kafka and what problem does it solve?

Apache Kafka is a **distributed, fault-tolerant, high-throughput event streaming platform** originally built at LinkedIn and open-sourced in 2011.

### Problem it solves
Traditional systems used **point-to-point** integration (service A calls service B directly), creating tight coupling. As services grew, you needed N×M connections for N producers and M consumers. Kafka solves this with a **central log bus**:

```
[Service A]  ──┐
[Service B]  ──┤──▶  [Kafka]  ──▶  [Analytics], [DB Sync], [Alerts]
[Service C]  ──┘
```

- **Decoupling**: producers don't know who consumes
- **Buffering**: absorbs bursty traffic; consumers catch up at their own pace
- **Replay**: consumers can re-read old events (unlike queues that delete on ack)
- **Durability**: persists to disk; survives broker restarts

### Core use cases
| Use Case | Example |
|---|---|
| Event streaming | User clicks, page views |
| Log aggregation | Centralizing app/infra logs |
| Change data capture (CDC) | DB changes → downstream sync |
| Metrics pipeline | IoT sensor data → dashboards |
| Microservice communication | Order service → Inventory service |
| Stream processing | Real-time fraud detection |

---

## Q2. What are the core components of Kafka?

```
Producer → [Broker Cluster] → Consumer Group
                ↕
           ZooKeeper / KRaft
```

| Component | Role |
|---|---|
| **Producer** | Publishes records to a topic |
| **Consumer** | Reads records from a topic |
| **Broker** | A single Kafka server; stores logs |
| **Cluster** | Multiple brokers forming one Kafka system |
| **Topic** | Named category/feed where records are published |
| **Partition** | Ordered, append-only log; a topic is split into N partitions |
| **Offset** | Monotonically increasing integer identifying a record's position in a partition |
| **Consumer Group** | Group of consumers sharing reads; each partition → exactly one consumer in the group |
| **ZooKeeper** | Manages broker metadata, leader election (legacy; being replaced) |
| **KRaft** | Kafka's built-in consensus (Raft-based), replaces ZooKeeper from Kafka 3.3+ |

---

## Q3. What is a Topic and a Partition? How does ordering work?

### Topic
A **named, durable, append-only log**. Think of it like a table in a DB, but immutable (you only append).

### Partition
Each topic is split into **P partitions**. A partition is:
- An **ordered**, **immutable sequence** of records
- Stored as a segment file on disk
- Replicated across brokers for fault tolerance

```
Topic: "orders"  (3 partitions)

Partition 0: [0][1][2][3]...
Partition 1: [0][1][2][3]...
Partition 2: [0][1][2][3]...
```

### Ordering guarantee
- **Within a partition**: strictly ordered by offset
- **Across partitions**: NO ordering guarantee

**Interview trap:** Kafka does NOT guarantee global ordering across a topic. To guarantee order for related events (e.g., all events for order-id 123), always send them to the same partition using a **message key**. Records with the same key always go to the same partition (via `hash(key) % numPartitions`).

### Key → Partition assignment
```java
// Producer with key
producer.send(new ProducerRecord<>("orders", orderId.toString(), orderPayload));
// All events for same orderId → same partition → ordered
```

---

## Q4. What is an Offset?

An **offset** is a 64-bit integer, unique within a partition, that monotonically increases. It identifies the position of a record in the partition log.

```
Partition 0:
offset:  0    1    2    3    4    5
record: [A]  [B]  [C]  [D]  [E]  [F]
```

- Offsets are **immutable** — once written, a record's offset never changes
- Consumer tracks its **committed offset** — the last offset it successfully processed
- Kafka stores committed offsets in the internal topic `__consumer_offsets`
- On restart, consumer resumes from committed offset

### Two critical offset positions
| Position | Meaning |
|---|---|
| `auto.offset.reset=earliest` | Read from the very beginning of the partition |
| `auto.offset.reset=latest` | Read only new records published after subscription |

---

## Q5. What is a Consumer Group?

A **Consumer Group** is a set of consumers that collectively consume a topic. Kafka distributes partitions among group members so that:
- Each partition is consumed by **exactly one consumer** in the group at any time
- A single consumer can handle multiple partitions
- More consumers than partitions → some consumers are idle

```
Topic: 4 partitions

Consumer Group A (2 consumers):
  Consumer-1 → Partition 0, 1
  Consumer-2 → Partition 2, 3

Consumer Group B (1 consumer):
  Consumer-3 → Partition 0, 1, 2, 3  (reads ALL)
```

**Key insight:** Multiple consumer groups can consume the same topic independently. This is unlike traditional queues where each message is consumed by one consumer total.

### Parallelism rule
`max_parallelism = number_of_partitions`. Adding more consumers than partitions gives no throughput gain — the extra consumers sit idle.

---

## Q6. What is Replication and the ISR?

### Replication
Each partition has **1 leader + N-1 replicas** across different brokers. `replication.factor` controls N.

```
Partition 0 (replication.factor=3):
  Broker 1: LEADER   ← producer writes here
  Broker 2: Follower ← syncs from leader
  Broker 3: Follower ← syncs from leader
```

### ISR (In-Sync Replicas)
ISR is the set of replicas that are **caught up with the leader** within `replica.lag.time.max.ms` (default 10s).

- If a follower falls behind → removed from ISR
- When it catches up → added back to ISR
- Leader election: only ISR members can become the new leader

### `min.insync.replicas` (critical param)
With `acks=all`, the leader won't ack a write until this many ISR replicas have persisted it. If ISR < `min.insync.replicas`, broker returns `NotEnoughReplicasException`.

```
replication.factor=3, min.insync.replicas=2
→ tolerate 1 broker failure without data loss
```

---

## Q7. Producer Acknowledgments: acks=0, 1, all

| acks | Meaning | Durability | Latency |
|---|---|---|---|
| `0` | No ack — fire and forget | Lowest (data loss possible) | Lowest |
| `1` | Leader acks after writing locally | Medium (lost if leader crashes before follower syncs) | Medium |
| `all` / `-1` | Leader acks after **all ISR** replicas confirm | Highest (no data loss within ISR) | Highest |

**Interview answer:** In production for financial/critical data always use `acks=all` + `min.insync.replicas=2` + `replication.factor=3`.

---

## Q8. Delivery Semantics: At-Most-Once, At-Least-Once, Exactly-Once

### At-Most-Once
Producer sends → doesn't retry on failure. Consumer commits offset before processing.
- **Risk:** message lost (0 or 1 delivery)
- Config: `acks=0`, `retries=0`, commit offset before processing

### At-Least-Once (default)
Producer retries on failure. Consumer commits offset after processing.
- **Risk:** duplicates (if broker acked but response lost, producer retries)
- Config: `acks=1` or `all`, `retries>0`

### Exactly-Once (EOS)
Achieved via two mechanisms:

**1. Idempotent Producer** (`enable.idempotence=true`)
- Kafka assigns each producer a **PID (Producer ID)** and a **sequence number** per partition
- Broker deduplicates retries using (PID, partition, sequence)
- Prevents duplicates from producer retries
- Automatically sets: `acks=all`, `retries=MAX`, `max.in.flight.requests.per.connection=5`

**2. Transactions** (Producer + Consumer)
```java
producer.initTransactions();
try {
    producer.beginTransaction();
    producer.send(new ProducerRecord<>("output", key, value));
    producer.sendOffsetsToTransaction(offsets, consumerGroupMetadata);
    producer.commitTransaction();
} catch (Exception e) {
    producer.abortTransaction();
}
```
- Consumer must set `isolation.level=read_committed` to see only committed messages
- Kafka uses `__transaction_state` internal topic to track transaction state

**End-to-end EOS:** Kafka Streams provides exactly-once by default when `processing.guarantee=exactly_once_v2`.

---

## Q9. How Does Kafka Store Data on Disk?

### Log Segment Files
Each partition is stored as a sequence of **segment files** on disk:

```
/kafka-logs/orders-0/
  00000000000000000000.log   ← raw message bytes
  00000000000000000000.index ← offset → byte position mapping
  00000000000000000000.timeindex ← timestamp → offset mapping
  00000000000000000537.log   ← next segment (starts at offset 537)
```

- Active segment: open for appending (sequential writes → very fast)
- Older segments: immutable, can be read/deleted independently

### Sequential I/O
Kafka uses **sequential disk writes** (appending to log). Sequential I/O on HDD is comparable to random access SSD. This is a core reason Kafka is fast.

### Page Cache
Kafka relies heavily on the **OS page cache** rather than JVM heap. Messages written to disk are also in page cache; consumers often read from cache without touching disk. This keeps JVM GC minimal and throughput high.

---

## Q10. Retention Policies

Kafka retains messages independently of whether they've been consumed.

### Time-based retention
```
log.retention.hours=168  # 7 days (default)
log.retention.ms=604800000
```

### Size-based retention
```
log.retention.bytes=1073741824  # 1 GB per partition
```

### Compaction (instead of deletion)
For topics where you want to keep the **latest value per key** (like a materialized view):
```
log.cleanup.policy=compact
```

Log compaction keeps at least the most recent record per key. Old records with the same key get tombstoned.

```
Before compaction:
[key=A, v=1] [key=B, v=1] [key=A, v=2] [key=C, v=1] [key=A, v=3]

After compaction:
[key=B, v=1] [key=C, v=1] [key=A, v=3]
```

Use cases: user preferences, account balances, config state.

---

## Q11. What Happens During Consumer Rebalancing?

Rebalancing is the process of **reassigning partitions** among consumers in a group. Triggered by:
- Consumer joins the group
- Consumer leaves (crash/timeout) — detected via `session.timeout.ms`
- Topic partition count changes
- Consumer calls `unsubscribe()`

### Stop-the-world rebalance (old)
1. Group coordinator sends `JoinGroup` request to all consumers
2. All consumers stop consuming (poll loop pauses)
3. New assignment computed, sent back to all
4. Consumers resume with new assignment

This causes **unavailability** (consumer lag spikes) during rebalance.

### Incremental Cooperative Rebalancing (new — Kafka 2.4+)
- Only partitions that need to move are revoked
- Consumers keep their partitions that don't move
- Eliminates stop-the-world pauses
- Config: `partition.assignment.strategy=CooperativeStickyAssignor`

### Partition Assignment Strategies
| Strategy | Behavior |
|---|---|
| `RangeAssignor` (default) | Assigns ranges of partitions; can be uneven |
| `RoundRobinAssignor` | Distributes evenly across consumers |
| `StickyAssignor` | Minimizes partition movement on rebalance; reduces rebalance overhead |
| `CooperativeStickyAssignor` | Sticky + incremental cooperative (best for production) |

---

## Q12. Key Producer Configuration Parameters

| Param | Default | Tuning guidance |
|---|---|---|
| `acks` | `1` | Use `all` for durability |
| `retries` | `2147483647` | Keep high with idempotent |
| `batch.size` | `16384` (16 KB) | Increase for throughput (e.g., 64 KB) |
| `linger.ms` | `0` | Add 5-20ms to batch more messages together |
| `buffer.memory` | `33554432` (32 MB) | Total memory for buffering before blocking |
| `compression.type` | `none` | Use `snappy` or `lz4` for throughput |
| `max.in.flight.requests.per.connection` | `5` | Set to `1` for strict ordering without idempotence |
| `enable.idempotence` | `false` | Set `true` for exactly-once |
| `max.block.ms` | `60000` | How long `send()` blocks when buffer is full |
| `delivery.timeout.ms` | `120000` | Total time for a record to be sent (includes retries) |

### Throughput tuning recipe
```
batch.size=65536      # 64 KB
linger.ms=10          # wait 10ms to batch
compression.type=lz4  # fast compression
acks=1                # if durability allows
```

---

## Q13. Key Consumer Configuration Parameters

| Param | Default | Tuning guidance |
|---|---|---|
| `group.id` | — | Required for consumer groups |
| `auto.offset.reset` | `latest` | `earliest` to reprocess from start |
| `enable.auto.commit` | `true` | Set `false` for at-least-once control |
| `auto.commit.interval.ms` | `5000` | If auto commit is on |
| `max.poll.records` | `500` | Max records per poll(); reduce if processing is slow |
| `max.poll.interval.ms` | `300000` | Max time between poll() calls before kicked from group |
| `session.timeout.ms` | `45000` | Heartbeat failure detection window |
| `heartbeat.interval.ms` | `3000` | How often consumer sends heartbeats; must be < session.timeout.ms/3 |
| `fetch.min.bytes` | `1` | Min bytes broker waits before responding |
| `fetch.max.wait.ms` | `500` | Max wait time for fetch.min.bytes |
| `isolation.level` | `read_uncommitted` | Set `read_committed` for transactional topics |

### `max.poll.interval.ms` — the silent killer
If your consumer processing takes longer than `max.poll.interval.ms`, the broker assumes it died and triggers a rebalance. Fix: reduce `max.poll.records` or increase `max.poll.interval.ms`.

---

## Q14. What is Consumer Lag?

**Consumer lag** = (latest offset in partition) - (committed offset of consumer group)

A high lag means the consumer is falling behind the producer. This is the primary operational metric for Kafka consumers.

```
Partition 0: latest offset = 1000
Consumer Group A committed offset = 700
→ lag = 300
```

### Monitoring tools
- `kafka-consumer-groups.sh --describe`
- **Burrow** (LinkedIn's lag monitor)
- Confluent Control Center
- Prometheus + kafka_exporter

### Why lag grows
- Slow message processing
- Consumer crashes (no consumption during rebalance)
- Under-provisioned consumers (fewer than partitions)
- GC pauses, DB slowness in consumer logic

---

## Q15. Kafka vs Traditional Message Queues (RabbitMQ)

| Aspect | Kafka | RabbitMQ |
|---|---|---|
| Model | Distributed log (pull) | Message queue (push) |
| Message retention | Configurable (days/weeks) | Deleted after ack |
| Replay | Yes — re-read from any offset | No |
| Ordering | Per-partition | Per-queue (with caveats) |
| Throughput | Millions/sec | Tens of thousands/sec |
| Consumer model | Pull (consumer polls) | Push (broker pushes) |
| Routing | Via topic/partition key | Exchanges + routing keys |
| Consumer groups | Built-in (parallel consumption) | Competing consumers |
| Protocol | Kafka binary protocol | AMQP |
| Best for | Stream processing, event sourcing, audit logs | Task queues, RPC, complex routing |

---

## Q16. What are Kafka's Internal Topics?

| Topic | Purpose |
|---|---|
| `__consumer_offsets` | Stores committed offsets per (group, topic, partition) |
| `__transaction_state` | Tracks state of in-progress transactions |
| `__cluster_metadata` | (KRaft mode) Stores cluster metadata (replaces ZooKeeper) |
| `_schemas` | Confluent Schema Registry stores Avro schemas here |

`__consumer_offsets` is a compacted topic with 50 partitions by default. Committing an offset is just producing a message to this topic.

---

## Q17. ZooKeeper vs KRaft Mode

### ZooKeeper (legacy)
- Manages broker metadata, leader elections, topic configs
- External dependency — operational overhead
- ZooKeeper ensemble itself needs quorum (3 or 5 nodes)
- Metadata propagation is async → brief inconsistency windows

### KRaft (Kafka Raft Metadata Mode — Kafka 2.8+ preview, 3.3+ GA)
- Kafka brokers run a built-in **Raft consensus** for metadata
- Eliminates ZooKeeper dependency
- One less system to operate and monitor
- **Controller quorum**: 3 or 5 dedicated controller nodes (or combined broker+controller)
- Faster metadata operations (10x more partitions supported)

```
KRaft cluster roles:
  - broker: handles client requests, stores data
  - controller: runs Raft, manages metadata
  - combined: both (fine for dev)
```

---

## Q18. Kafka Compression

Kafka supports **end-to-end compression**: producer compresses a batch, broker stores it compressed, consumer decompresses.

| Codec | CPU cost | Ratio | Speed | Use case |
|---|---|---|---|---|
| `none` | None | 1x | — | Low-volume, debugging |
| `gzip` | High | Best | Slow | Archival, max size reduction |
| `snappy` | Low | Medium | Fast | General purpose (Google) |
| `lz4` | Very low | Medium | Fastest | Latency-sensitive pipelines |
| `zstd` | Low-medium | Best/speed balance | Fast | Modern best choice |

**Recommendation for production:** `lz4` or `zstd`. Compression is done per **batch**, not per message — larger batches = better compression ratio. Set `linger.ms > 0` to allow batching.

---

## Q19. Log Compaction Deep Dive

### Compaction process
A background **Log Cleaner** thread (configurable threads: `log.cleaner.threads`) scans the log and removes older records for keys that have newer records.

```
log.cleanup.policy=compact,delete   # Both: compact AND delete old segments
```

### Tombstone records
To delete a key entirely, produce a message with **null value** (tombstone). Log cleaner will eventually remove all records for that key. Tombstones are retained for `log.cleaner.delete.retention.ms` (default 24h).

### When to use compaction
- **Kafka as a changelog** for KTables in Kafka Streams
- **Source of truth** for materialized views
- **CDC topics**: one record per changed row

### Minutiae
- Compaction does NOT guarantee strict ordering is preserved across compacted keys
- Compaction runs in the background — there's always a lag before old records are cleaned
- `min.compaction.lag.ms`: minimum time a record can sit before being compacted (default 0)
- `max.compaction.lag.ms`: maximum time before a record must be compacted

---

## Q20. Kafka Streams

Kafka Streams is a **client-side library** (no separate cluster) for stream processing.

```java
StreamsBuilder builder = new StreamsBuilder();
KStream<String, String> stream = builder.stream("input-topic");
stream
    .filter((k, v) -> v.contains("error"))
    .mapValues(v -> v.toUpperCase())
    .to("error-topic");

KafkaStreams app = new KafkaStreams(builder.build(), props);
app.start();
```

### Key abstractions
| Abstraction | Description |
|---|---|
| `KStream` | Unbounded stream of records (each record = event) |
| `KTable` | Changelog stream (latest value per key = materialized table) |
| `GlobalKTable` | Like KTable but fully replicated on every instance |
| `KGroupedStream` | Grouped stream for aggregations |
| `SessionWindows` | Session-based windowing |

### Windowing types
| Type | Description |
|---|---|
| `TumblingWindow` | Fixed, non-overlapping time windows |
| `HoppingWindow` | Fixed-size, overlapping (advance < size) |
| `SessionWindow` | Activity-based, gap-triggered |
| `SlidingWindow` | Defined by time difference between records |

### Exactly-once in Kafka Streams
```properties
processing.guarantee=exactly_once_v2
```
This uses transactions under the hood. Every output write + offset commit is a single atomic transaction.

### State stores
Streams maintains local **RocksDB** state stores for aggregations. State is backed by a Kafka **changelog topic** for fault tolerance. On restart, state is restored from the changelog.

---

## Q21. Kafka Connect

Kafka Connect is a **framework for integrating Kafka with external systems** without writing custom code.

```
[Database] ←→ [Source Connector] → [Kafka] → [Sink Connector] → [Elasticsearch]
```

| Type | Direction | Example |
|---|---|---|
| Source Connector | External → Kafka | JDBC, Debezium (CDC), S3, MongoDB |
| Sink Connector | Kafka → External | Elasticsearch, HDFS, BigQuery, S3 |

### Key features
- **Distributed mode**: runs as a cluster, fault-tolerant
- **Standalone mode**: single process, for dev/testing
- **Converters**: serialize/deserialize (JSON, Avro, Protobuf)
- **Transformations (SMT)**: single-message transforms (filter, mask fields, rename)
- **Schema Registry integration**: auto-manages schema evolution

---

## Q22. Schema Evolution with Avro + Schema Registry

**Problem:** Kafka messages are just bytes. Without a schema contract, adding/removing fields breaks consumers.

**Solution:** Confluent Schema Registry
- Central repository for Avro/Protobuf/JSON schemas
- Producer registers schema → gets schema ID
- Message format: `[magic byte][4-byte schema ID][avro payload]`
- Consumer fetches schema by ID → deserializes correctly

### Compatibility modes
| Mode | What's allowed |
|---|---|
| `BACKWARD` | New schema can read data written with old schema (add optional fields) |
| `FORWARD` | Old schema can read data written with new schema |
| `FULL` | Both backward and forward |
| `NONE` | No compatibility check |

---

## Q23. Kafka High-Availability Architecture Patterns

### Partition count sizing
```
target_throughput / throughput_per_partition
```
More partitions = more parallelism but:
- More file handles, more ZooKeeper znodes
- Longer leader election time on broker failure
- More memory for producer/consumer buffers

### Multi-DC / MirrorMaker 2
For geo-replication across data centers:
- **MirrorMaker 2** (built on Kafka Connect): bi-directional replication
- Handles offset translation, consumer group checkpointing
- Active-active or active-passive topology

### Rack awareness
```properties
broker.rack=us-east-1a
```
Kafka places replicas across different racks (availability zones) to survive AZ failures.

---

## Q24. Exactly-Once End-to-End: How it Actually Works

Full exactly-once pipeline: Producer → Kafka → Kafka Consumer → Kafka Producer (Kafka Streams)

1. **Idempotent producer**: deduplicates retries at the broker using (PID, seq)
2. **Transactions**: atomically commits processed offsets + output messages
3. **`read_committed`**: consumers skip messages from aborted transactions

```
Timeline:
Producer: BEGIN TXN
Producer: send(output-topic, msg1)
Producer: send(output-topic, msg2)
Producer: sendOffsetsToTransaction(input-offsets, group)
Producer: COMMIT TXN
                ↓
Broker writes everything atomically or nothing
                ↓
Consumer (read_committed): sees msg1, msg2 only after commit
```

**Minutiae:**
- `transactional.id` must be set and unique per producer instance
- If producer crashes mid-transaction, on restart it calls `initTransactions()` which fences old zombie producers
- Epoch fencing: each `initTransactions()` call increments the epoch; broker rejects writes from old epochs

---

## Q25. Common Kafka Interview Gotchas

1. **"Kafka guarantees ordering"** — only within a partition, not across partitions. Use a key for related events.

2. **"More consumers = more throughput"** — only up to `num_partitions`. Extra consumers sit idle.

3. **`auto.commit` with at-least-once** — if you enable auto-commit and your consumer crashes between commit and processing, you lose messages (at-most-once). Disable auto-commit and commit after processing.

4. **`max.poll.interval.ms` timeout** — if processing takes too long between `poll()` calls, the consumer is kicked from the group. Increase the value or reduce `max.poll.records`.

5. **Rebalancing causes duplicate processing** — when using at-least-once, design your consumers to be idempotent (handle duplicates gracefully).

6. **Key = null → round-robin partitioning** — records with null key are distributed round-robin. If you need ordering, always set a key.

7. **`linger.ms=0` kills batching** — with default `linger.ms=0`, the producer sends a batch immediately when `batch.size` is reached or when the thread yields. Set `linger.ms=5-20` to batch more.

8. **`unclean.leader.election.enable=true`** — allows non-ISR replicas to become leader (risk of data loss). Default is now `false`. Don't set it `true` unless you explicitly want availability over durability.

9. **Compacted topics still have duplicates temporarily** — compaction is asynchronous. You may see multiple values for the same key in the log before the cleaner runs.

10. **Consumer group rebalance on any new consumer** — even if consumer B joins group, consumer A might lose partitions it was processing. Handle `onPartitionsRevoked()` properly (commit offsets, flush state).
