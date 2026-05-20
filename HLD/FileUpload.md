# System Design: Upload File / Large Files

# 1. Problem Statement

Design a scalable system that allows users to:

* Upload files (small + large)
* Resume interrupted uploads
* Upload files in parallel chunks
* Track upload progress
* Store files reliably
* Download files efficiently
* Handle millions of uploads

Examples:

* Google Drive
* Dropbox
* OneDrive
* S3 Multipart Upload

---

# 2. Functional Requirements

## Core Features

* Upload files
* Upload very large files (GBs)
* Pause/resume upload
* Retry failed chunks
* Download files
* Delete files
* View upload progress
* Share file link

## Non Functional Requirements

* Highly scalable
* Fault tolerant
* Durable storage
* Low latency
* High throughput
* Secure
* Efficient bandwidth usage

---

# 3. High Level Architecture

```text
                +----------------+
                |    Client App  |
                +--------+-------+
                         |
                         v
                +----------------+
                | API Gateway    |
                +--------+-------+
                         |
        +-------------------------------+
        |                               |
        v                               v
+---------------+              +----------------+
| Upload Service|              | Auth Service   |
+-------+-------+              +----------------+
        |
        |
        v
+---------------------+
| Metadata DB         |
| (Postgres/MySQL)    |
+---------------------+
        |
        v
+---------------------+
| Object Storage      |
| (S3/GCS/HDFS)       |
+---------------------+
        |
        v
+---------------------+
| CDN                 |
+---------------------+
```

---

# 4. Why Object Storage?

Large files should NOT be stored inside relational DB.

Use:

* Amazon S3
* Google Cloud Storage
* Azure Blob
* HDFS

Because object storage provides:

* High durability
* Infinite scalability
* Cheap storage
* Multipart upload support
* CDN integration

---

# 5. Simple Upload Flow

## Step-by-step

```text
Client -> Upload API -> Object Storage
                       -> Metadata DB
```

### Flow

1. User selects file
2. Client sends upload request
3. Server authenticates user
4. Server generates upload ID
5. File uploaded to storage
6. Metadata stored in DB
7. Upload completed response returned

---

# 6. Problem With Large Files

Uploading huge files directly causes:

* Network failures
* Restart from beginning
* Memory pressure
* Timeout issues
* Slow uploads

Solution:

# Multipart / Chunk Upload

---

# 7. Chunk Upload Design

Instead of uploading one huge file:

```text
5 GB file
-> split into 5 MB chunks
-> upload chunks independently
```

Example:

```text
Chunk 1
Chunk 2
Chunk 3
Chunk 4
...
```

Benefits:

* Retry only failed chunk
* Parallel uploads
* Resume support
* Faster upload

---

# 8. Chunk Upload Workflow

## Step 1: Initiate Upload

Client calls:

```http
POST /upload/initiate
```

Response:

```json
{
  "uploadId": "u123",
  "chunkSize": "5MB"
}
```

DB entry:

```text
uploadId | fileName | totalChunks | status
```

---

## Step 2: Upload Chunks

Client uploads chunks independently.

```http
PUT /upload/chunk
```

Payload:

```text
uploadId
chunkNumber
chunkData
```

Storage path:

```text
/uploads/u123/chunk-1
/uploads/u123/chunk-2
```

---

## Step 3: Track Uploaded Chunks

Use Redis or DB.

Example:

```text
uploadId -> [1,2,3,5]
```

Meaning:
Chunk 4 missing.

---

## Step 4: Complete Upload

Client calls:

```http
POST /upload/complete
```

Server:

* Verifies all chunks exist
* Merges chunks
* Creates final object
* Updates metadata DB

---

# 9. Parallel Uploads

Huge optimization.

Instead of:

```text
Chunk1 -> Chunk2 -> Chunk3
```

Use:

```text
Chunk1
Chunk2
Chunk3
Chunk4
(all parallel)
```

Benefits:

* Better bandwidth usage
* Faster uploads
* Better UX

Usually client uploads:

* 4–10 chunks concurrently

---

# 10. Resume Upload

If internet disconnects:

Client asks:

```http
GET /upload/status/{uploadId}
```

Response:

```json
{
  "uploadedChunks": [1,2,3,4,7]
}
```

Client uploads only missing chunks.

This is extremely common interview topic.

---

# 11. Presigned URL Optimization

Important real-world optimization.

Without optimization:

```text
Client -> Backend -> Storage
```

Backend becomes bottleneck.

Better:

```text
Client -> Backend (get signed URL)
Client -> S3 directly
```

Flow:

```text
1. Client requests upload URL
2. Backend generates signed URL
3. Client uploads directly to S3
4. Backend stores metadata
```

Benefits:

* Backend CPU reduced
* Backend bandwidth reduced
* Massive scalability improvement

---

# 12. Metadata Database Design

## File Table

```sql
File(
  file_id,
  user_id,
  file_name,
  size,
  content_type,
  storage_path,
  created_at,
  status
)
```

## Upload Table

```sql
Upload(
  upload_id,
  file_id,
  total_chunks,
  uploaded_chunks,
  status
)
```

---

# 13. Why Redis?

Redis used for:

* Upload progress
* Chunk tracking
* Temporary metadata
* Rate limiting

Reason:

* Very fast
* In-memory
* Auto expiration

Example:

```text
upload:u123 -> bitmap of uploaded chunks
```

---

# 14. File Merge Strategy

Two approaches:

## Approach 1: Server Merge

```text
Server downloads chunks
-> merges
-> uploads final file
```

Problem:

* Heavy CPU
* Heavy network

---

## Approach 2: Storage Native Multipart Merge

S3 supports:

```text
CompleteMultipartUpload
```

Storage combines chunks internally.

Best approach.

---

# 15. Upload Progress Tracking

Frontend calculates:

```text
uploadedBytes / totalBytes
```

Example:

```text
250MB / 1GB = 25%
```

Progress update mechanisms:

* Polling
* WebSocket
* SSE

Usually polling sufficient.

---

# 16. Scalability

## Horizontal Scaling

Upload service should be stateless.

```text
        Load Balancer
             |
   ---------------------
   |         |         |
Upload1   Upload2   Upload3
```

Because uploads stored in:

* S3
* Redis
* DB

Any instance can continue upload.

---

# 17. CDN For Downloads

Downloads served via CDN.

```text
User -> CDN -> Storage
```

Benefits:

* Faster downloads
* Reduced origin load
* Global caching

Examples:

* CloudFront
* Cloudflare
* Akamai

---

# 18. Security

## Authentication

Use:

* JWT
* OAuth

## Authorization

Verify:

```text
user owns file?
```

## Virus Scanning

After upload:

```text
Upload -> Queue -> Virus Scanner
```

## Encryption

* HTTPS in transit
* S3 encryption at rest

---

# 19. Rate Limiting

Prevent abuse.

Use:

* API Gateway
* Redis token bucket

Example:

```text
100 uploads/min/user
```

---

# 20. Handling Failures

## Chunk Upload Failure

Retry only failed chunk.

## Service Crash

State persisted in Redis/DB.

## Duplicate Chunk

Use:

```text
(uploadId, chunkNumber)
```

as unique key.

---

# 21. File Deduplication

Optimization asked in interviews.

Client computes:

```text
SHA-256 hash
```

If file already exists:

```text
Don't upload again.
Reuse existing storage object.
```

Used by:

* Dropbox
* Google Drive

---

# 22. Compression

Optional optimization.

Compress:

* Images
* Logs
* Text files

Do NOT compress:

* MP4
* ZIP
* JPEG

(already compressed)

---

# 23. Async Processing

After upload:

```text
Upload Complete
      |
      v
 Kafka / SQS Queue
      |
      +--> Virus Scan
      +--> Thumbnail Generation
      +--> Metadata Extraction
      +--> OCR
```

---

# 24. APIs

## Initiate Upload

```http
POST /upload/initiate
```

## Upload Chunk

```http
PUT /upload/chunk
```

## Upload Status

```http
GET /upload/status/{id}
```

## Complete Upload

```http
POST /upload/complete
```

## Download File

```http
GET /files/{id}
```

---

# 25. Bottlenecks

## Backend Bandwidth

Solution:

* Direct S3 upload

## DB Writes

Solution:

* Redis buffering
* Batch writes

## Large Merge Operations

Solution:

* Storage-native multipart merge

---

# 26. Interview Deep Dive Questions

## Why Chunk Upload?

* Resume support
* Retry support
* Parallelism
* Better reliability

## Why Presigned URLs?

* Backend scalability
* Lower bandwidth cost
* Reduced CPU usage

## Why Redis?

* Fast progress tracking
* Ephemeral state

## SQL or NoSQL?

* SQL for metadata consistency
* Object storage for binary data

---

# 27. Tradeoffs

| Choice             | Pros            | Cons                   |
| ------------------ | --------------- | ---------------------- |
| Store in DB        | Simple          | Very expensive         |
| Store in S3        | Scalable        | Extra complexity       |
| Server-side upload | Easy validation | Backend bottleneck     |
| Direct upload      | Scalable        | More client complexity |

---

# 28. End-to-End Final Flow

```text
1. User selects file
2. Client splits into chunks
3. Backend creates upload session
4. Backend returns presigned URLs
5. Client uploads chunks directly to S3
6. Client retries failed chunks
7. Client requests completion
8. S3 merges chunks
9. Metadata stored in DB
10. CDN serves downloads
```

---

# 29. Real-World Technologies

## Storage

* Amazon S3
* GCS
* Azure Blob

## Queue

* Kafka
* RabbitMQ
* SQS

## DB

* PostgreSQL
* MySQL

## Cache

* Redis

## CDN

* CloudFront
* Cloudflare

---

# 30. Most Important Interview Points

If interviewer asks "design file upload system", ALWAYS discuss:

1. Chunk upload
2. Resume support
3. Parallel uploads
4. Presigned URLs
5. Object storage
6. Metadata DB
7. Redis progress tracking
8. CDN download optimization
9. Virus scanning
10. Scalability

These are the core interview expectations.

---

# 31. 2-Minute Interview Summary

"For large file upload systems, we split files into chunks and upload them independently, usually directly to object storage like S3 using presigned URLs. Upload progress and chunk metadata are tracked in Redis/DB. Failed chunks are retried individually, enabling resume functionality. The upload service remains stateless and horizontally scalable. Final files are merged using storage-native multipart APIs, and downloads are served through CDN for low latency."
