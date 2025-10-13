# Scheduler Configuration

## Overview

The scheduler defines how a test executes over time — controlling how virtual users (Vusers) or groups are *
*initialized, started, held, and stopped**.
Its structure depends on the **workload type** (`BASIC` or `REAL_WORLD`) and scope (`BY_TEST` or `BY_GROUP`).

Schedulers let you model everything from a simple fixed-duration run to a complex, phased load pattern.

## Workload Matrix

| Workload Type | Scope    | Scheduler Placement | Supported Actions                                         | Notes                              |
|---------------|----------|---------------------|-----------------------------------------------------------|------------------------------------|
| BASIC         | BY_TEST  | At test level       | initialize, startVusers, duration, stopVusers             | Single sequence for all groups     |
| BASIC         | BY_GROUP | Inside each group   | startGroup, initialize, startVusers, duration, stopVusers | Groups run independently           |
| REAL_WORLD    | BY_TEST  | At test level       | initialize, startVusers, duration, stopVusers             | Multiple actions allowed per phase |
| REAL_WORLD    | BY_GROUP | Inside each group   | startGroup, initialize, startVusers, duration, stopVusers | Multiple actions allowed per phase |

## Supported Actions

Each scheduler step defines a **phase** in the test timeline. Actions are processed in order, top to bottom.

| Action          | Description                                       | Examples                                             | Notes                                 |
|-----------------|---------------------------------------------------|------------------------------------------------------|---------------------------------------|
| **startGroup**  | (BY_GROUP only) Controls when a group starts.     | `immediately`, `delay:30s`, `GroupA`                 | Defaults to `immediately` if missing. |
| **initialize**  | Prepares Vusers before execution.                 | `simultaneously`, `gradually:10u@30s`, `just before` | Only one kept; duplicates removed.    |
| **startVusers** | Starts Vusers either gradually or simultaneously. | `10vu:gradually:2u@10s`, `simultaneously`            | Multiple allowed for REAL_WORLD.      |
| **duration**    | Specifies how long the phase runs.                | `30s`, `10m`, `1h30m`, `until complete`              | Mandatory for time-based runs.        |
| **stopVusers**  | Stops Vusers.                                     | `simultaneously`, `gradually:10u@30s`                | Added automatically if required.      |

## Workload Behavior

| Workload                  | Scope       | Multiple Steps | Behavior                                        |
|---------------------------|-------------|----------------|-------------------------------------------------|
| **BASIC (BY_TEST)**       | Test level  | ❌ Not allowed  | Single sequence; missing actions auto-inserted. |
| **BASIC (BY_GROUP)**      | Group level | ❌ Not allowed  | Each group runs independently.                  |
| **REAL_WORLD (BY_TEST)**  | Test level  | ✅ Allowed      | Multiple startVusers/duration phases allowed.   |
| **REAL_WORLD (BY_GROUP)** | Group level | ✅ Allowed      | Complex ramp patterns supported per group.      |

> 💡 For BY_GROUP schedulers, if startGroup is missing, a default `startGroup: "immediately"` is inserted.

## Default actions

The following defaults are applied if actions are missing:

| Action          | Default                                      | Behavior                                         |
|-----------------|----------------------------------------------|--------------------------------------------------|
| **startGroup**  | `immediately`                                | Added to each group if missing.                  |
| **initialize**  | `just before`                                | Inserted early (after startGroup).               |
| **startVusers** | `simultaneously`                             | For REAL_WORLD, uses group vuser count if known. |
| **duration**    | `until complete` (BASIC) / `5m` (REAL_WORLD) | Added if missing.                                |
| **stopVusers**  | `simultaneously`                             | Added if duration exists and no stop specified.  |

## Syntax Overview

Each entry follows this structure:

```yaml
scheduler:
  - actionType: "parameters"
```

## Examples

### BASIC Workload - BY_TEST

Single scheduler for all groups in the test.

```yaml
groups:
  - name: "GroupA"
    vusers: 50
    script: "e2e/script1"

  - name: "GroupB"
    vusers: 20
    script: "e2e/script2"

scheduler:
  - initialize: "gradually:10u@30s"
  - startVusers: "simultaneously"
  - duration: "1h"
  - stopVusers: "simultaneously"
```

### BASIC Workload - BY_GROUP

Each group has its own scheduler.

```yaml
groups:
  - name: "GroupA"
    vusers: 50
    script: "e2e/script1"    
    scheduler:
      - startGroup: "immediately"
      - initialize: "simultaneously"
      - startVusers: "gradually:5u@15s"
      - duration: "30m"
      - stopVusers: "simultaneously"
        
  - name: "GroupB"
    vusers: 20
    script: "e2e/script2"
    scheduler:
      - startGroup: "delay:10m"
      - initialize: "simultaneously"
      - startVusers: "simultaneously"
      - duration: "20m"
      - stopVusers: "simultaneously"
```

### REAL_WORLD Workload - BY_TEST
Multiple phases with different startVusers and durations.

```yaml
groups:
  - name: "GroupA"
    vusers: 50
    script: "e2e/script1"
  - name: "GroupB"
    vusers: 20
    script: "e2e/script2"

scheduler:
  - initialize: "gradually:20u@15s"
  - startVusers: "10vu:gradually:2u@5s"    # Phase 1 ramp-up
  - duration: "15m"
  - startVusers: "20vu:gradually:5u@10s"   # Phase 2 ramp-up
  - duration: "30m"
  - startVusers: "30vu:simultaneously"     # Phase 3 ramp-up
  - duration: "45m"
  - stopVusers: "gradually:10u@15s"        # Gradual ramp-down
```


### REAL_WORLD Workload - BY_TEST
Each group has its own scheduler with different startVusers and durations.

Group A does not have `startGroup` defined. so it is added as `immediately` by default.

Group B does not have `initialize` defined. so it will be added as `just before` by default.

```yaml
groups:
  - name: "GroupA"
    vusers: 50
    script: "e2e/script1"
    scheduler:
      - initialize: "gradually:20u@15s"
      - startVusers: "10vu:gradually:2u@5s"    # Phase 1 ramp-up
      - duration: "15m"
      - startVusers: "20vu:gradually:5u@10s"   # Phase 2 ramp-up
      - duration: "30m"
      - startVusers: "30vu:simultaneously"     # Phase 3 ramp-up
      - duration: "45m"
      - stopVusers: "gradually:10u@15s"        # Gradual ramp-down

  - name: "GroupB"
    vusers: 20
    script: "e2e/script2"
    scheduler:
      - startGroup: "after GroupA"
      - startVusers: "10vu:gradually:2u@5s"    # Phase 1 ramp-up
      - duration: "15m"
      - startVusers: "20vu:gradually:5u@10s"   # Phase 2 ramp-up
      - duration: "30m"
      - startVusers: "30vu:simultaneously"     # Phase 3 ramp-up
      - duration: "45m"
      - stopVusers: "gradually:10u@15s"        # Gradual ramp-down

```

