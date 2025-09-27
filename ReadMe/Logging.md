# 📘 LRE Log Configuration – Decision Guide
Logging in LRE can be configured as **Ignore, Standard,** or **Extended**.

Use this guide to decide which one fits your needs:

---

## 1. Ignore Mode

1. [x] Use when: You want no logs at all
2. [x] **Example:**
 
    ```yaml
    Log: ignore
    ```

## 2. Standard Mode

1. [x] **Use when:** You need basic logs, usually for debugging or lightweight monitoring.
2. [x] **Options:**
 
   1. [x] **Always** → log everything.
   2. [x] **On Error** → log only if an error occurs. 
   3. [x] Optional **cache size** (limits KB of stored log). 
3. [x] **Examples:**

    ```yaml
    # Always log standard events
    Log: standard:always
    
    # Log only on error, with cache size 20 KB
    Log: standard:on error:20
    ```

## 3. Extended Mode

1. [x] **Use when:** You need detailed logs for deep troubleshooting.
2. [x] **Options**:
   1. [x] **Always** or **On Error** (like Standard).
   2. [x] **Cache size** (KB).
   3. [x] **Flags** to control log content:
      1. [x] `substitution` → log parameter substitutions
      2. [x] `server` → log server responses
      3. [x] `trace` → log advanced traces

3. [x] **Examples:**
    ```yaml
    # Always log everything (all flags)
    Log: extended:always:substitution,server,trace
    
    # On error, 50 KB cache, all flags
    Log: extended:on error:50:substitution,server,trace
    
    # On error, 25 KB cache, only substitutions and traces
    Log: extended:on error:25:substitution,trace
    ```

✅ Choose Extended if:

* You need parameter substitution logs for debugging dynamic values.
* You need server response logs for diagnosing API/backend issues.
* You need trace logs for in-depth debugging.
* You’re in debugging or development mode, not raw load-testing mode.

---
## Quick Rule of Thumb

1. [x] Performance testing only → `ignore` 
2. [x] Basic debugging → `standard`
3. [x] Detailed troubleshooting → `extended with flags`

---

# LRE Log Configuration Documentation
The `Log` configuration defines how logging is handled in an LRE (LoadRunner Enterprise) test.

It supports three primary modes:

1. [x] **IGNORE** → no logging
2. [x] **STANDARD** → basic logging, with optional error-based conditions and cache limits
3. [x] **EXTENDED** → detailed logging with advanced options and fine-grained flags
---

## 1. Ignore Log

Disable logging entirely.
```yaml
# No logging at all
Log: ignore
```

## 2. Standard Log
Provides standard logging with configurable options.

**Always log everything**
```yaml
Log: standard:always
```

**Log only on error, with cache size limit**

```yaml
# Log on error, limit cache to 20 KB
Log: standard:on error:20

# Log on error, limit cache to 1 KB
Log: standard:on error:1
```

✅ Choose Standard if:

1. [x] You just want basic logging.
2. [x] You don’t need parameter substitutions, server responses, or deep
   traces.



## 3. Extended Log

Provides detailed logging with optional flags and conditions.

Minimal configuration

```yaml    
# Always log with extended details
Log: extended:always

# Log extended details only on error, cache limit 50 KB
Log: extended:on error:50
```
With all flags enabled

Flags:

1. [x] `substitution` → log parameter substitutions
2. [x] `server` → log server responses
3. [x] `trace` → log advanced traces


```yaml
# Extended log, on error, cache limit 50 KB, with all flags
Log: extended:on error:50:substitution,server,trace

# Extended log, on error, cache limit 100 KB, with all flags
Log: extended:on error:100:substitution,server,trace

# Extended log, always, with all flags
Log: extended:always:substitution,server,trace

```  

With single flags
```yaml
# Only log parameter substitutions
Log: extended:always:substitution

# Only log server responses
Log: extended:always:server

# Only log traces
Log: extended:always:trace

# On error, cache 30 KB, only server logs
Log: extended:on error:30:server  

# On error, cache 15 KB, only trace logs
Log: extended:on error:15:trace

# On error, cache 20 KB, only server logs
Log: extended:on error:20:server
```

With flag combinations

```yaml
# On error, cache 10 KB, log substitutions and server responses
Log: extended:on error:10:substitution,server

# On error, cache 25 KB, log substitutions and traces
Log: extended:on error:25:substitution,trace

# On error, cache 30 KB, log server responses and traces
Log: extended:on error:30:server,trace

# Always log substitutions and traces
Log: extended:always:substitution,trace

# Always log server responses and traces
Log: extended:always:server,trace

```
| Input                                          | Mode     | Condition | Cache | Substitution | Server | Trace |
| ---------------------------------------------- | -------- | --------- | ----- | ------------ | ------ | ----- |
| ignore                                         | IGNORE   | –         | –     | ❌            | ❌      | ❌     |
| standard:always                                | STANDARD | Always    | –     | ❌            | ❌      | ❌     |
| standard:on error:1                            | STANDARD | On Error  | 1     | ❌            | ❌      | ❌     |
| standard:on error:20                           | STANDARD | On Error  | 20    | ❌            | ❌      | ❌     |
| extended:always                                | EXTENDED | Always    | –     | ❌            | ❌      | ❌     |
| extended:on error:10                           | EXTENDED | On Error  | 10    | ❌            | ❌      | ❌     |
| extended:on error:50                           | EXTENDED | On Error  | 50    | ❌            | ❌      | ❌     |
| extended:always:substitution                   | EXTENDED | Always    | –     | ✅            | ❌      | ❌     |
| extended:always:server                         | EXTENDED | Always    | –     | ❌            | ✅      | ❌     |
| extended:always:trace                          | EXTENDED | Always    | –     | ❌            | ❌      | ✅     |
| extended:always:substitution,server            | EXTENDED | Always    | –     | ✅            | ✅      | ❌     |
| extended:always:substitution,trace             | EXTENDED | Always    | –     | ✅            | ❌      | ✅     |
| extended:always:server,trace                   | EXTENDED | Always    | –     | ❌            | ✅      | ✅     |
| extended:always:substitution,server,trace      | EXTENDED | Always    | –     | ✅            | ✅      | ✅     |
| extended:on error:1:substitution               | EXTENDED | On Error  | 1     | ✅            | ❌      | ❌     |
| extended:on error:1:server                     | EXTENDED | On Error  | 1     | ❌            | ✅      | ❌     |
| extended:on error:1:trace                      | EXTENDED | On Error  | 1     | ❌            | ❌      | ✅     |
| extended:on error:1:substitution,server        | EXTENDED | On Error  | 1     | ✅            | ✅      | ❌     |
| extended:on error:1:substitution,trace         | EXTENDED | On Error  | 1     | ✅            | ❌      | ✅     |
| extended:on error:1:server,trace               | EXTENDED | On Error  | 1     | ❌            | ✅      | ✅     |
| extended:on error:1:substitution,server,trace  | EXTENDED | On Error  | 1     | ✅            | ✅      | ✅     |
| extended:on error:20:substitution              | EXTENDED | On Error  | 20    | ✅            | ❌      | ❌     |
| extended:on error:20:server                    | EXTENDED | On Error  | 20    | ❌            | ✅      | ❌     |
| extended:on error:20:trace                     | EXTENDED | On Error  | 20    | ❌            | ❌      | ✅     |
| extended:on error:20:substitution,server       | EXTENDED | On Error  | 20    | ✅            | ✅      | ❌     |
| extended:on error:20:substitution,trace        | EXTENDED | On Error  | 20    | ✅            | ❌      | ✅     |
| extended:on error:20:server,trace              | EXTENDED | On Error  | 20    | ❌            | ✅      | ✅     |
| extended:on error:20:substitution,server,trace | EXTENDED | On Error  | 20    | ✅            | ✅      | ✅     |
| extended:on error:50:substitution              | EXTENDED | On Error  | 50    | ✅            | ❌      | ❌     |
| extended:on error:50:server                    | EXTENDED | On Error  | 50    | ❌            | ✅      | ❌     |
| extended:on error:50:trace                     | EXTENDED | On Error  | 50    | ❌            | ❌      | ✅     |
| extended:on error:50:substitution,server       | EXTENDED | On Error  | 50    | ✅            | ✅      | ❌     |
| extended:on error:50:substitution,trace        | EXTENDED | On Error  | 50    | ✅            | ❌      | ✅     |
| extended:on error:50:server,trace              | EXTENDED | On Error  | 50    | ❌            | ✅      | ✅     |
| extended:on error:50:substitution,server,trace | EXTENDED | On Error  | 50    | ✅            | ✅      | ✅     |
