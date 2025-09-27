# LRE ThinkTime Configuration

The `LreThinkTimeValidator` class validates and parses **ThinkTime** configurations for LRE (LoadRunner Enterprise)
groups.  

ThinkTime defines delays between requests to simulate real user behavior.

---

## ThinkTime Types

| Type     | Description                                                                |
|----------|----------------------------------------------------------------------------|
| `IGNORE` | No think time is applied.                                                  |
| `REPLAY` | Replay recorded think time, optional limit in seconds.                     |
| `MODIFY` | Modify recorded think time by a limit and/or multiply factor.              |
| `RANDOM` | Random think time, between min/max percentages, optional limit in seconds. |

---

## Valid Examples

```yaml
# No think time
ThinkTime: ignore

# Replay recorded think time, limit to 10 seconds max
ThinkTime: replay:10

# Modify think time: limit to 20 seconds, multiply by 1.5
ThinkTime: modify:20*1.5

# Random think time: between 50% and 150% of recorded, limit to 30 seconds
ThinkTime: random:50-150:30

# Random think time: between 80% and 120% of recorded (no time limit)
ThinkTime: random:80-120

# Modify think time: only multiply by 2.0 (no time limit)
ThinkTime: modify:*2.0
```

## Parsing Rules

### 1. Type extraction

* The first part of the string before `:` defines the type.

* Valid types: `ignore`, `replay`, `modify`, `random`.

### 2. Configuration extraction

* Everything after the first `:` is the configuration string.

* Rules differ per type:

| Type     | Config Format                            | Notes                          | 
|----------|------------------------------------------|--------------------------------|
| `IGNORE` | Optional empty                           | Any config is ignored.         |
| `REPLAY` | `[limitSeconds]`                         | Limit think time in seconds.   |
| `MODIFY` | `[limitSeconds]*[factor]` or `*[factor]` | Multiply factor is optional.   |
| `RANDOM` | `minPercent-maxPercent[:limitSeconds]`   | Percentage range is mandatory. |

### 3. Validation

* Percentages must be integers.
* Min percentage ≤ Max percentage.
* Limits and multiply factors must be positive numbers.

# Complete Reference Table for ThinkTime

| Type   | YAML Syntax                 | Limit | Multiply | Min % | Max % | Description                                                                |
|--------|-----------------------------|-------|----------|-------|-------|----------------------------------------------------------------------------|
| ignore | ThinkTime: ignore           | -     | -        | -     | -     | No think time applied                                                      |
| replay | ThinkTime: replay           | -     | -        | -     | -     | Use recorded think time                                                    |
| replay | ThinkTime: replay:10        | 10    | -        | -     | -     | Use recorded think time but limit think time to max 10 sec                 |
| modify | ThinkTime: modify:*1.5      | -     | 1.5      | -     | -     | Multiply the recorded think time by 1.5                                    |
| modify | ThinkTime: modify:*2.0      | -     | 2.0      | -     | -     | Multiply the recorded think time by 2.0                                    |
| modify | ThinkTime: modify:10*1.5    | 10    | 1.5      | -     | -     | Multiply the recorded think time by 1.5, but limit thinktime to max 10 sec |
| modify | ThinkTime: modify:15        | 15    | 1.0      | -     | -     | Multiply the recorded think time by 1, but limit thinktime to max 15 sec   |
| random | ThinkTime: random:50-150    | -     | -        | 50    | 150   | Random 50-150% of recorded                                                 |
| random | ThinkTime: random:80-120    | -     | -        | 80    | 120   | Random 80-120% of recorded                                                 |
| random | ThinkTime: random:50-150:30 | 30    | -        | 50    | 150   | Random 50-150% of recorded but limit thinktime to max 30s                  |
| random | ThinkTime: random:80-120:15 | 15    | -        | 80    | 120   | Random 80-120% of recorded but limit thinktime to max 15s                  |


# [Click here for Sample YAML file](sample-config.yaml.md)


