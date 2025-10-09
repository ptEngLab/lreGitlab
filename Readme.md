# LRE Test YAML Creation Guide

This guide explains how to create YAML files for defining LRE tests.  
It covers top-level fields, validation rules, and examples.

---

## 1️⃣ Controller

**Field:** `controller`  
**Type:** `String`  
**Required:** ❌ (optional)

**Description:**

- Specifies the LRE controller that will execute the test.
- If omitted, LRE may assign a default controller.

**Validation rules:**

- If provided, must not be empty.
- Must exist on LRE. If the name is invalid, the validator throws an error.
- Your test may fail if the specified controller is offline or busy.

**Example:**

```yaml
controller: "devserver"
```

**Example (omitted):**

```yaml
# controller is optional → LRE will allocate a available controller which is in idle status 
```

> Recommendation: Use this option only if specific controller required for your test. <br>
> Otherwise, omit this section to allow LRE to allocate an available controller.
---

## 2️⃣ Workload Type

**Field:** `workloadTypeCode`  
**Type:** `Integer`  
**Required:** ❌ (optional)

**Description:**

- Determines how the test distributes virtual users (vUsers) and where the scheduler should be defined.
- If omitted, defaults to:  
  Type: BASIC  
  SubType: BY_TEST  
  VusersDistributionMode: BY_NUMBER

**Valid values:**

| Code | Type          | SubType  | Scheduler Placement   | LG Assignment         |
|------|---------------|----------|-----------------------|-----------------------|
| 1    | BASIC         | BY_TEST  | Root-level allowed    | Test-level `lgAmount` |
| 2    | BASIC         | BY_TEST  | Root-level allowed    | Test-level `lgAmount` |
| 3    | BASIC         | BY_GROUP | Group-level only      | Hostnames per group   |
| 4    | REAL_WORLD    | BY_TEST  | Root-level allowed    | Test-level `lgAmount` |
| 5    | REAL_WORLD    | BY_TEST  | Root-level allowed    | Test-level `lgAmount` |
| 6    | REAL_WORLD    | BY_GROUP | Group-level only      | Hostnames per group   |
| 7    | GOAL_ORIENTED | -        | Controlled internally | Managed internally    |

**Notes:**

- For `BY_GROUP` types (codes 3 & 6), **root-level scheduler and `lgAmount` should not be used**. Scheduler must be
  defined per group.
- For `GOAL_ORIENTED` (code 7), LRE controls scheduling internally.

**Example (explicit):**

```yaml
workloadTypeCode: 2
```

**Example (omitted, defaults applied):**

```yaml
# workloadTypeCode omitted → defaults to BASIC/BY_TEST/BY_NUMBER
```

---

## 3️⃣ LG assignment

**Field:** `lgAmount`  
**Type:** `Integer`  
**Required:** ❌ (optional)

**Description:**

- Specifies the number of automatch Load Generators (LGs) used by the test.
- Only relevant for root-level LG assignment.
- If provided, **automatically assigns requested automatch on-premises LGs** to all groups in test.
- If omitted, LGs must be explicitly defined per group in the group's `hostnames` field.

**Validation rules:**

- If provided, it must be a positive integer (`>0`) if provided.
- If omitted, LG distribution is set to `MANUAL`, requiring per-group hostnames.
- Negative or zero values are invalid and will cause validation to fail.

**Examples:**

**Explicit root-level LG assignment:**

```yaml
lgAmount: 4
```

> LRE will auto-match 4 LGs across all groups.


**Manual, per-group LG assignment (omit root-level** `lgAmount`**)**:

```yaml
workloadTypeCode: 1
groups:
  - name: "SampleGroup"
    hostnames: "LG1, LG2"
  - name: "SampleGroup2"
    hostnames: "LG3"
```

> LGs are defined individually for each group.

---

## 4️⃣ Monitor Profiles

**Field:** `monitorProfileIds`  
**Type:** `List<Integer>`  
**Required:** ❌ (optional)

**Description:**

- Specifies a list of Monitor Profiles to attach to the test.
- Each entry corresponds to a valid **Monitor Profile ID** in LRE.
- Optional — if omitted, no monitor profiles will be attached.

**Validation rules:**

- Only non-null integers are considered.
- If the list is empty or null, no profiles are attached.
- Each ID must correspond to a valid Monitor Profile in LRE (validation is done server-side).

**Example:**

```yaml
lgAmount: 4
workloadTypeCode: 1
monitorProfileIds: [ 1001, 1002, 1003 ]
```

> The test will attach Monitor Profiles with IDs 1001, 1002, and 1003.

---

## 5️⃣ Automatic Trending

**Field:** `automaticTrending`  
**Type:** `Object` (`YamlAutomaticTrending`)  
**Required:** ❌ (optional)

**Description:**

- Automatically publishes trend results after test execution.
- Optional. If omitted, no automatic trending is configured.
- Default values are applied for certain fields if they are not provided in YAML.

**Sub-fields:**

| Sub-field    | Type    | Required                                | Description                                                                                                  | Default                     |
|--------------|---------|-----------------------------------------|--------------------------------------------------------------------------------------------------------------|-----------------------------|
| `reportId`   | Integer | ✔                                       | ID of the existing trend report. If the ID does not exist, LRE may create a new report.                      | None                        |
| `maxRuns`    | Integer | ❌                                       | Maximum number of runs allowed in the trend report.                                                          | 20                          |
| `trendRange` | Enum    | ❌                                       | Range of runs to consider. Possible values: `CompleteRun`, `PartOfRun`.                                      | `CompleteRun`               |
| `onMaxRuns`  | Enum    | ❌                                       | Action when max runs is reached. Possible values: `DoNotPublishAdditionalRuns`, `DeleteFirstSetNewBaseline`. | `DeleteFirstSetNewBaseline` |
| `startTime`  | Integer | Mandatory if `trendRange` = `PartOfRun` | Start time in minutes for partial run trending.                                                              | 20                          |
| `endTime`    | Integer | Mandatory if `trendRange` = `PartOfRun` | End time in minutes for partial run trending.                                                                | 80                          |

**Validation rules:**

- `reportId` must be a valid integer if provided.
- `maxRuns` must be a positive integer; defaults to `20` if omitted.
- `trendRange` must be either `CompleteRun` or `PartOfRun`; defaults to `CompleteRun`.
- `onMaxRuns` must be either `DeleteFirstSetNewBaseline` or `DoNotPublishAdditionalRuns`; defaults to
  `DeleteFirstSetNewBaseline`.
- If `trendRange` = `PartOfRun`, both `startTime` and `endTime` must be provided and `startTime` must be greater than
  `endTime`.

**Example (minimal):**

```yaml
automaticTrending:
  reportId: 5
```