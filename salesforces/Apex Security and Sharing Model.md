Here's a breakdown of this page — it's the chapter overview for Apex's security and sharing model, and it contains a genuinely important recent change worth flagging up front.

## ⚠️ Big change to know about (API v67.0+)

This is the most consequential thing on the page, and it reverses long-standing Apex defaults:

| Behavior | API v66.0 and earlier | API v67.0 and later |
|---|---|---|
| **Execution mode** | Runs in **system mode** by default (ignores user's object/field permissions unless you say otherwise) | Runs in **user mode** by default (current user's permissions and field-level security are enforced automatically) |
| **Sharing mode** (no explicit keyword) | Determined by a fallback chain (inheritance chain version, Aura/LWC controller status, calling class's mode, else `without sharing`) | Defaults to **`with sharing`** |
| **`WITH SECURITY_ENFORCED`** in SOQL | Supported | **Removed** — must use `WITH USER_MODE` instead |

**Why this matters:** if you're studying from older Trailhead modules, blog posts, or even code you wrote a year ago, you may have learned "Apex runs in system mode by default and ignores sharing/FLS unless you say `with sharing`." That's no longer true for anything compiled at API 67.0+. Existing orgs/classes aren't silently changed — this only kicks in based on the API version stamped on the class — but any *new* class you save today (Summer '26 org) inherits the safer, more restrictive defaults.

## The three core mechanisms this chapter organizes

1. **Record-level security** — controlled via the `with sharing`, `without sharing`, and `inherited sharing` class keywords. Covered in depth in the linked [Enforce Sharing Rules](https://developer.salesforce.com/docs/atlas.en-us.apexcode.meta/apexcode/apex_security_sharing_rules.htm) page.
2. **Object- and field-level security** — whether the running user can even see/edit a given object or field, independent of which specific records they can see. Covered in [Enforce Object and Field Permissions](https://developer.salesforce.com/docs/atlas.en-us.apexcode.meta/apexcode/apex_classes_perms_enforcing.htm), which also introduces `Security.stripInaccessible()` for filtering out fields/objects the user can't access before returning data.
3. **Class-level execution security** — restricting *who* can even invoke a given Apex class/method, based on profile or permission set. Covered in [Class Security](https://developer.salesforce.com/docs/atlas.en-us.apexcode.meta/apexcode/apex_classes_security.htm). Notably, this only applies to classes, not triggers.

Two more sub-pages round out the chapter:
- **Understanding Apex Managed Sharing** — programmatically granting record access (the Apex equivalent of manual sharing rules) via `Share` objects like `AccountShare`.
- **Security Tips for Apex and Visualforce Development** — general secure-coding practices (SOQL injection prevention, etc.).

## Practical takeaway for your Trailhead/exam work

Since the Batch, Queueable, and Schedulable classes we've been building (`LeadProcessor`, `AddPrimaryContact`, `DailyLeadProcessor`) didn't declare `with sharing` or `without sharing`, their behavior technically depends on the org's default API version. If you want predictable, portable behavior regardless of what version your org saves classes at, it's good practice to be explicit — e.g., `public with sharing class AddPrimaryContact implements Queueable`.

Want me to add `with sharing` explicitly to any of those earlier classes, or go deeper into `stripInaccessible()` / `WITH USER_MODE` syntax?
