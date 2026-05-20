# Low Level Design: Large File Upload System

# 1. Goal

Design LLD for a scalable large file upload system supporting:

* Multipart uploads
* Parallel chunk upload
* Resume upload
* Retry failed chunks
* Upload tracking
* Final file merge
* Download support

Focus:

* Classes
* APIs
* DB schema
* Object models
* Internal workflow
* Concurrency
* Failure handling

---

# 2. Core Requirements

## Functional

* Upload large files
* Pause/resume upload
* Retry failed chunks
* Parallel upload
* Complete upload
* Download file
* Delete file

## Non Functional

* Scalable
* Reliable
* Fault tolerant
* Durable
* Stateless services

---

# 3. High Level Component View

```text
+----------------------+
| Client               |
+----------+-----------+
           |
           v
+----------------------+
| API Gateway          |
+----------+-----------+
           |
           v
+----------------------+
| Upload Controller    |
+----------+-----------+
           |
           v
+----------------------+
| Upload Service       |
+----------+-----------+
     |            |
     v            v
+---------+   +-------------+
| Redis   |   | Metadata DB |
+---------+   +-------------+
     |
     v
+----------------------+
| Object Storage (S3)  |
+----------------------+
```

---

# 4. Main Entities

# File Entity

```java
class FileMetadata {
    Long fileId;
    Long userId;
    String fileName;
    Long fileSize;
    String contentType;
    String storagePath;
    FileStatus status;
    LocalDateTime createdAt;
}
```

---

# Upload Session Entity

```java
class UploadSession {
    String uploadId;
    Long fileId;
    int totalChunks;
    long uploadedBytes;
    UploadStatus status;
    LocalDateTime createdAt;
}
```

---

# Chunk Metadata Entity

```java
class ChunkMetadata {
    Long id;
    String uploadId;
    Integer chunkNumber;
    Long chunkSize;
    String etag;
    ChunkStatus status;
}
```

---

# 5. Enums

```java
enum FileStatus {
    UPLOADING,
    ACTIVE,
    FAILED,
    DELETED
}
```

```java
enum UploadStatus {
    INITIATED,
    IN_PROGRESS,
    COMPLETED,
    FAILED
}
```

```java
enum ChunkStatus {
    PENDING,
    UPLOADED,
    FAILED
}
```

---

# 6. Database Schema

# FILE_METADATA

```sql
CREATE TABLE file_metadata (
    file_id BIGINT PRIMARY KEY,
    user_id BIGINT,
    file_name VARCHAR(255),
    file_size BIGINT,
    content_type VARCHAR(100),
    storage_path VARCHAR(500),
    status VARCHAR(20),
    created_at TIMESTAMP
);
```

---

# UPLOAD_SESSION

```sql
CREATE TABLE upload_session (
    upload_id VARCHAR(100) PRIMARY KEY,
    file_id BIGINT,
    total_chunks INT,
    uploaded_bytes BIGINT,
    status VARCHAR(20),
    created_at TIMESTAMP
);
```

---

# CHUNK_METADATA

```sql
CREATE TABLE chunk_metadata (
    id BIGINT PRIMARY KEY,
    upload_id VARCHAR(100),
    chunk_number INT,
    chunk_size BIGINT,
    etag VARCHAR(255),
    status VARCHAR(20),

    UNIQUE(upload_id, chunk_number)
);
```

---

# 7. API Design

# Initiate Upload

```http
POST /uploads/initiate
```

Request:

```json
{
  "fileName": "movie.mp4",
  "fileSize": 1000000000,
  "contentType": "video/mp4"
}
```

Response:

```json
{
  "uploadId": "u123",
  "chunkSize": 5242880,
  "totalChunks": 200
}
```

---

# Upload Chunk

```http
PUT /uploads/{uploadId}/chunks/{chunkNumber}
```

Headers:

```text
Content-Type: application/octet-stream
```

Response:

```json
{
  "etag": "etag123"
}
```

---

# Upload Status

```http
GET /uploads/{uploadId}/status
```

Response:

```json
{
  "uploadedChunks": [1,2,3,4],
  "remainingChunks": [5,6]
}
```

---

# Complete Upload

```http
POST /uploads/{uploadId}/complete
```

Response:

```json
{
  "fileId": 101,
  "status": "ACTIVE"
}
```

---

# Download File

```http
GET /files/{fileId}
```

---

# 8. Controller Layer

```java
@RestController
class UploadController {

    private UploadService uploadService;

    @PostMapping("/uploads/initiate")
    UploadInitiateResponse initiateUpload(
            @RequestBody UploadInitiateRequest request) {

        return uploadService.initiateUpload(request);
    }

    @PutMapping("/uploads/{uploadId}/chunks/{chunkNumber}")
    UploadChunkResponse uploadChunk(
            @PathVariable String uploadId,
            @PathVariable Integer chunkNumber,
            InputStream chunkData) {

        return uploadService.uploadChunk(uploadId,
                                         chunkNumber,
                                         chunkData);
    }
}
```

---

# 9. Service Layer

```java
interface UploadService {

    UploadInitiateResponse initiateUpload(
            UploadInitiateRequest request);

    UploadChunkResponse uploadChunk(
            String uploadId,
            Integer chunkNumber,
            InputStream chunkData);

    UploadStatusResponse getStatus(String uploadId);

    CompleteUploadResponse completeUpload(String uploadId);
}
```

---

# 10. Upload Service Implementation

```java
class UploadServiceImpl implements UploadService {

    private StorageService storageService;
    private UploadRepository uploadRepository;
    private ChunkRepository chunkRepository;
    private RedisTemplate redisTemplate;

    @Override
    public UploadInitiateResponse initiateUpload(
            UploadInitiateRequest request) {

        String uploadId = UUID.randomUUID().toString();

        int totalChunks = calculateChunks(request.getFileSize());

        UploadSession session = new UploadSession();
        session.setUploadId(uploadId);
        session.setTotalChunks(totalChunks);
        session.setStatus(UploadStatus.INITIATED);

        uploadRepository.save(session);

        return new UploadInitiateResponse(uploadId,
                                          CHUNK_SIZE,
                                          totalChunks);
    }
}
```

---

# 11. Chunk Upload Logic

```java
@Override
public UploadChunkResponse uploadChunk(
        String uploadId,
        Integer chunkNumber,
        InputStream chunkData) {

    validateUpload(uploadId);

    String etag = storageService.uploadChunk(
            uploadId,
            chunkNumber,
            chunkData);

    ChunkMetadata chunk = new ChunkMetadata();
    chunk.setUploadId(uploadId);
    chunk.setChunkNumber(chunkNumber);
    chunk.setEtag(etag);
    chunk.setStatus(ChunkStatus.UPLOADED);

    chunkRepository.save(chunk);

    redisTemplate.opsForSet()
                 .add(uploadId,
                      chunkNumber.toString());

    return new UploadChunkResponse(etag);
}
```

---

# 12. Storage Service

```java
interface StorageService {

    String uploadChunk(
            String uploadId,
            Integer chunkNumber,
            InputStream data);

    void completeMultipartUpload(
            String uploadId);

    URL generatePresignedUrl(
            String uploadId,
            Integer chunkNumber);
}
```

---

# 13. S3 Storage Implementation

```java
class S3StorageService implements StorageService {

    private AmazonS3 s3Client;

    @Override
    public String uploadChunk(
            String uploadId,
            Integer chunkNumber,
            InputStream data) {

        String key = uploadId + "/chunk-" + chunkNumber;

        PutObjectResult result = s3Client.putObject(
                BUCKET_NAME,
                key,
                data,
                new ObjectMetadata());

        return result.getETag();
    }
}
```

---

# 14. Resume Upload Logic

Client reconnects.

```http
GET /uploads/{uploadId}/status
```

Server checks:

```text
Redis
or
ChunkMetadata table
```

Response:

```json
{
  "uploadedChunks": [1,2,3]
}
```

Client uploads only missing chunks.

---

# 15. Parallel Upload Design

Frontend uploads chunks concurrently.

Example:

```text
Thread1 -> Chunk1
Thread2 -> Chunk2
Thread3 -> Chunk3
```

Backend remains stateless.

Each chunk request independent.

---

# 16. Redis Design

# Track Uploaded Chunks

```text
KEY:
upload:u123

VALUE:
SET(1,2,3,4)
```

---

# Track Upload Progress

```text
KEY:
progress:u123

VALUE:
80%
```

---

# 17. Complete Upload Flow

```java
@Override
public CompleteUploadResponse completeUpload(
        String uploadId) {

    UploadSession session = uploadRepository
            .findById(uploadId)
            .orElseThrow();

    int uploadedChunks = chunkRepository
            .countUploadedChunks(uploadId);

    if (uploadedChunks != session.getTotalChunks()) {
        throw new RuntimeException("Missing chunks");
    }

    storageService.completeMultipartUpload(uploadId);

    session.setStatus(UploadStatus.COMPLETED);

    uploadRepository.save(session);

    return new CompleteUploadResponse();
}
```

---

# 18. Retry Mechanism

If chunk upload fails:

```text
Retry only failed chunk.
```

Frontend retry policy:

```text
Retry 3 times
Exponential backoff
```

Example:

```text
1s
2s
4s
```

---

# 19. Idempotency

Very important.

Same chunk may upload twice.

Prevent duplicates using:

```sql
UNIQUE(upload_id, chunk_number)
```

If duplicate:

* Ignore
  or
* Replace existing chunk

---

# 20. Presigned URL Optimization

Instead of:

```text
Client -> Backend -> S3
```

Use:

```text
Client -> S3 directly
```

Backend only generates signed URL.

---

# Generate Presigned URL

```java
URL generatePresignedUrl(
        String uploadId,
        Integer chunkNumber) {

    Date expiration = new Date(
            System.currentTimeMillis() + 600000);

    return s3Client.generatePresignedUrl(
            BUCKET_NAME,
            uploadId + "/chunk-" + chunkNumber,
            expiration,
            HttpMethod.PUT);
}
```

---

# 21. Concurrency Handling

Multiple chunks uploaded simultaneously.

Need thread-safe updates.

Approaches:

## Optimistic Locking

```text
version column
```

## Atomic Redis Operations

```text
SADD upload:u123 5
```

---

# 22. File Merge Strategies

# Bad Approach

```text
Download chunks
Merge locally
Upload final file
```

Problem:

* Huge network cost
* Huge CPU usage

---

# Good Approach

Use S3 multipart completion.

```text
S3 internally merges chunks.
```

---

# 23. Download Flow

## Download API

```http
GET /files/{id}
```

Server returns:

```text
Presigned download URL
```

or

```text
CDN URL
```

---

# 24. Security

## Authentication

JWT token.

## Authorization

Verify:

```text
Does file belong to user?
```

## Virus Scan

After upload:

```text
Kafka Event
 -> Virus Scanner
 -> Update status
```

---

# 25. Validation

## Validate File Size

```text
Reject > max size
```

## Validate Content Type

```text
Only allowed MIME types
```

## Validate Chunk Order

```text
chunkNumber <= totalChunks
```

---

# 26. Cleanup Job

Scheduled cleanup.

Delete:

```text
Expired uploads
Unused chunks
Failed sessions
```

Example:

```text
Uploads inactive for 24 hours
```

---

# 27. Async Event Flow

```text
Upload Complete
      |
      v
 Kafka Topic
      |
      +--> Thumbnail Service
      +--> OCR Service
      +--> Virus Scanner
      +--> Metadata Extractor
```

---

# 28. Repository Layer

```java
interface UploadRepository {

    void save(UploadSession session);

    Optional<UploadSession> findById(String uploadId);
}
```

---

```java
interface ChunkRepository {

    void save(ChunkMetadata metadata);

    int countUploadedChunks(String uploadId);

    List<Integer> getUploadedChunks(String uploadId);
}
```

---

# 29. Failure Handling

## Server Crash

No problem.

State stored in:

* Redis
* DB
* S3

Service stateless.

---

## Network Failure

Retry failed chunks.

---

## Duplicate Requests

Handled using:

```text
(uploadId, chunkNumber)
```

---

# 30. Scalability

Upload service horizontally scalable.

```text
             Load Balancer
                    |
      --------------------------------
      |              |              |
 Upload-1       Upload-2       Upload-3
```

No sticky sessions required.

Because state externalized.

---

# 31. Sequence Diagram

```text
Client -> API : initiate upload
API -> DB : create upload session
API -> Client : uploadId

Client -> S3 : upload chunk1
Client -> S3 : upload chunk2
Client -> S3 : upload chunk3

Client -> API : complete upload
API -> S3 : complete multipart upload
API -> DB : mark completed
API -> Client : success
```

---

# 32. Important Interview Topics

Always discuss:

* Chunk upload
* Resume upload
* Presigned URLs
* Retry mechanism
* Idempotency
* Parallel uploads
* Redis tracking
* Stateless services
* Multipart merge
* CDN downloads

---

# 33. Common Interview Follow-ups

## Why not store file in DB?

* Expensive
* Slow
* Bad scalability

---

## Why chunk upload?

* Retry support
* Resume support
* Faster upload

---

## Why presigned URLs?

* Backend scalability
* Lower bandwidth cost

---

## Why Redis?

* Fast upload tracking
* Ephemeral state

---

# 34. End-to-End Internal Workflow

```text
1. Client initiates upload
2. Upload session created
3. File split into chunks
4. Chunks uploaded in parallel
5. Redis tracks uploaded chunks
6. Failed chunks retried
7. Client requests completion
8. S3 merges chunks
9. Metadata updated
10. CDN serves downloads
```

---

# 35. Final Interview Summary

"The LLD contains UploadController, UploadService, StorageService, Redis tracking, SQL metadata tables, and S3 multipart storage. Files are split into chunks and uploaded independently, enabling retries, resume support, and parallel uploads. Services remain stateless while Redis and DB maintain upload state. Final assembly is delegated to S3 multipart completion APIs for scalability and efficiency."
