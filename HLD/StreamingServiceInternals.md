# How Modern Video Streaming Actually Works

# Step 1 - Admin Uploads Original Video

Example:

```text
movie.mp4
```

This is usually:
- very large
- highest quality
- source/master video

Flow:

```text
Admin Upload
      |
      v
Raw Video Storage (S3/Object Storage)
```

---

# Step 2 - Transcoding Pipeline Starts

System now converts ONE video into MANY versions.

Example:

```text
movie.mp4
```

becomes:

```text
360p
480p
720p
1080p
4K
```

Flow:

```text
Raw Video
    |
    v
Transcoding Workers
    |
    +--> 360p
    +--> 480p
    +--> 720p
    +--> 1080p
```

---

# Step 3 - Video Gets Split Into Chunks

Very important concept.

System does NOT keep giant movie file for streaming.

Instead:

```text
1080p_movie.mp4
```

becomes:

```text
1080p/
    seg1.ts
    seg2.ts
    seg3.ts
```

Each segment:
- 5 sec
OR
- 10 sec

---

# Example Timeline

Suppose:
- each segment = 10 sec

Then:

| Segment | Video Time |
|---|---|
| seg1.ts | 0 → 10 sec |
| seg2.ts | 10 → 20 sec |
| seg3.ts | 20 → 30 sec |

---

# Step 4 - Manifest File Created

Manifest is like:
# Table of Contents for Video

Example:

```text
master.m3u8
```

Contains:

```text
Available qualities:
- 360p
- 720p
- 1080p
```

And:

```text
Where chunks are located
```

---

# Actual Manifest Example

## Master Manifest

```text
#EXTM3U

#EXT-X-STREAM-INF:BANDWIDTH=800000
360p/index.m3u8

#EXT-X-STREAM-INF:BANDWIDTH=2000000
720p/index.m3u8

#EXT-X-STREAM-INF:BANDWIDTH=5000000
1080p/index.m3u8
```

---

# 720p Manifest

```text
#EXTM3U

#EXTINF:10,
seg1.ts

#EXTINF:10,
seg2.ts

#EXTINF:10,
seg3.ts
```

Meaning:
- each chunk = 10 sec
- these are chunk files

---

# Step 5 - User Opens Netflix/Hotstar

Frontend first calls:
# Metadata API

Example:

```http
GET /show/123
```

Returns:

```json
{
   "title": "The Flash",
   "seasons": [...],
   "episodes": [...]
}
```

This is:
- UI data
- episode lists
- thumbnails
- descriptions

NOT actual video.

---

# Step 6 - User Clicks Play

Now player requests:
# Playback API

Example:

```http
GET /playback/episode123
```

Backend returns:

```json
{
   "manifest_url":
   "https://cdn.xyz.com/master.m3u8"
}
```

---

# Step 7 - Player Downloads Manifest

Player now knows:
- available qualities
- chunk locations
- bitrate options

---

# Step 8 - Player Starts Streaming Chunks

Player requests:

```http
GET /720p/seg1.ts
```

then:

```http
GET /720p/seg2.ts
```

then:

```http
GET /720p/seg3.ts
```

continuously.

---

# Important Point

Video is NOT downloaded fully.

Chunks stream continuously behind the scenes.

Like:

```text
Play current chunk
↓
Meanwhile download next chunk
↓
Play next chunk
↓
Download another chunk
```

---

# Step 9 - Adaptive Bitrate Streaming

Suppose internet becomes slow.

Player switches from:

```text
1080p/seg10.ts
```

to:

```text
480p/seg11.ts
```

WITHOUT restarting movie.

Why possible?

Because:
- all qualities have same timestamps

Example:

| Quality | Segment | Time |
|---|---|---|
| 1080p seg10 | 90-100 sec |
| 720p seg10 | 90-100 sec |
| 480p seg10 | 90-100 sec |

So switching is seamless.

---

# Step 10 - Seeking Works

Suppose user jumps to:

```text
01:30:00
```

Player calculates:

```text
5400 sec / 10 sec chunk
= segment 540
```

Then directly requests:

```http
GET /720p/seg540.ts
```

This is why seeking is instant.

---

# What Your Hotstar Payload Was

You discovered:
# Internal Encoding Metadata

Example:

```json
"video_codec": "h264"
```

means:
- video compressed using H264 codec

---

# This

```json
"width": 1920,
"height": 1080
```

means:
- Full HD version exists

---

# This

```json
"video_br": 3228
```

means:
- bitrate

Higher bitrate:
- better quality
- more internet usage

---

# This

```json
"phone-h264"
```

means:
- optimized version for phones

---

# This

```json
"tv-hevc"
```

means:
- optimized version for TVs using HEVC codec

---

# This

```json
"VMAF_score": 99
```

means:
- video quality score

Created by Netflix.

Higher = better visual quality.

---

# Seek Thumbnail System

This part:

```json
"frameInterval": 10
```

means:
- generate preview image every 10 sec

So while dragging timeline:

```text
|----●------|
```

Player shows thumbnail preview instantly.

---

# Overall End-to-End Flow

```text
1. Admin uploads movie.mp4
            |
2. Transcoding pipeline starts
            |
3. Multiple qualities generated
            |
4. Videos split into chunks
            |
5. Manifest files created
            |
6. Stored in CDN/Object storage
            |
7. User opens app
            |
8. Metadata API returns show info
            |
9. User clicks Play
            |
10. Playback API returns manifest URL
            |
11. Player downloads manifest
            |
12. Player continuously requests chunks
            |
13. Player switches quality dynamically
            |
14. CDN serves chunks from nearest edge
```

---

# Final Mental Model

Think of streaming like:

```text
YouTube/Netflix is NOT sending one big movie file.

It is continuously sending
small video pieces dynamically
according to your internet speed.
```
