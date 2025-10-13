# LRE Test YAML Creation Guide

This guide explains how to create YAML files for defining LRE tests. It covers top-level fields, validation rules, and
examples.

---

## Introduction

This guide explains how to create YAML configuration files for defining LoadRunner Enterprise (LRE) tests. It covers:

- Key configuration fields and their purposes
- Validation rules and constraints
- Practical examples and best practices
- Common pitfalls to avoid

---

## 1️⃣ Groups

**Field**: `groups`

**Type**: `Array` of `Group` Objects

**Required**: ✅ (mandatory — at least one group is required)

### 1.1 Description

The groups section defines the virtual user groups that participate in the test. Each group specifies:

* Name of the group - unique for each groups in the test
* Number of virtual users (vusers)
* Assigned script
* Load Generators (hostnames)
* Optional local RTS (runtime settings) or references to global RTS
* Optional group-level command line or reference to global command lines
* Optional scheduler settings (for BY_GROUP workload types)

> Note: At least one group must be defined. The test will fail validation if the `groups` array is empty.

### 1.2 YAML Structure

```yaml
groups:
  - name: "SampleGroup"                                   # mandatory, unique name for the group
    vusers: 50                                            # mandatory, number of virtual users in the group
    script: "e2e/Optional"                                # mandatory, script path or ID assigned to the group
    hostnames: "LG1, cloud1"                              # if lgAmount is not specified at root level, mandatory
    hostTemplate: "cloud1 : 1"                            # optional, host template for cloud LGs
    globalRTS: "RTS1"                                     # optional, reference to a global RTS by name
    globalCommandLine: "cmd1"                             # optional, reference to a global command line by name
    scheduler: # optional, required for BY_GROUP workload type
      - "Initialize: gradually:10u@30s"
      - "Start vusers: 10vu:gradually:2U@10s"
      - "Duration: 30 s"
      - "Duration: 30 s"
      - "Start vusers: 15vu:gradually:2U@10s"
      - "Stop vusers: 50vu:simultaneously"

  - name: "SampleGroup2"                                  # mandatory, unique name for the group
    vusers: 10                                            # mandatory, number of virtual users in the group
    script: "10"                                          # mandatory, script path or ID assigned to the group
    hostnames: "LG1, MyLocalLG"                           # hostnames can be specified as mixed automatch and manual
    globalRTS: "RTS1"                                     # optional, reference to a global RTS by name

    # Global RTS is not used in this group. and no local RTS as well. the test will use default RTS for this group. 
  - name: "SampleGroup3"
    vusers: 15
    script: "11"
    hostnames: "LG2"

    # Global RTS is not used in this group. defining RTS locally. 
    pacing: "random interval: 10 - 15 / 5"
    thinkTime: "random: 50 - 150 : 30"
    log: ignore
    selenium: "JREPath=C:\\java\\jdk,ClassPath=myclasspath.jar,TestNgFiles=testng.xml"

```

**Fields:**

| Field               | Type         | Required | Description                                                                                               |
|---------------------|--------------|----------|-----------------------------------------------------------------------------------------------------------|
| `name`              | String       | ✅        | Name of the group. Must be unique within the test.                                                        |
| `vusers`            | Integer      | ✅        | Number of virtual users in the group.                                                                     |
| `script`            | String       | ✅        | Script ID or name to execute for the group.                                                               |
| `hostnames`         | String       | ❌        | Comma-separated list of Load Generators (LGs) for this group. Ignored if `lgAmount` is set at root-level. |
| `globalRTS`         | String       | ❌        | Name of a globally defined RTS from `globalRts`. If provided, local RTS fields are ignored.               |
| `globalCommandLine` | String       | ❌        | Name of a globally defined Command Line.                                                                  |
| `pacing`            | String       | ❌        | Local pacing settings for the group (used only if `globalRTS` is not set).                                |
| `thinkTime`         | String       | ❌        | Local think time settings (used only if `globalRTS` is not set).                                          |
| `log`               | String       | ❌        | Log configuration for the group.                                                                          |
| `jmeter`            | String       | ❌        | JMeter configuration string.                                                                              |
| `selenium`          | String       | ❌        | Selenium configuration string.                                                                            |
| `javaVM`            | String       | ❌        | JavaVM configuration string.                                                                              |
| `scheduler`         | List<Object> | ❌        | Optional scheduler for vUser ramp-up. Each entry can include `rampUp` and `interval`.                     |

### 1.3 Validation Rules

**1. Mandatory Fields**

- `name` Must be non-empty and must be unique per test.
- `vusers` Must be a positive integer and should be greater than zero
- `script` Must exist in LRE. You can provide either the script id or its path.
  **2. Hostnames / LoadGenerators (LGs)**
- If `lgAmount` is not defined at root-level, `hostnames` is required.
- Multiple hostnames can be assigned using a comma-separated string
- Automatch LG names can be referred as `LG1`, `LG2` etc.
- On-prem LG names can be referred as its fully qualified name as available in LRE. `localLG1`
- cloud LG names (e.g., cloud1) can also be provided. If cloud LGs are used, `hostTemplate` is recommended to be
  defined.
- `hostTemplate` is optional and can be used to specify cloud LG template id or cloud template name.

**3. RTS (Runtime Settings)**

- **If `globalRTS` is defined:**
    - The group uses the referenced global RTS.
    - Local RTS settings (`pacing`, `thinkTime`, `log`, `jmeter`, `selenium`, `javaVM`) are not required. if defined,
      they will be ignored.
    - Validator throws an error if the referenced `globalRTS` does not exist.

- **If `globalRTS` is omitted:**
    - Local RTS can be defined using the `pacing`, `thinkTime`, `log`, `jmeter`, `selenium`, and `javaVM` fields.
    - Validator applies defaults for missing RTS fields.

**4. Command Lines**

- If `globalCommandLine` is defined, validator ensures the reference exists in global command lines.
- Local command lines (group-specific) can also be applied; otherwise, they are omitted.

**5. Scheduler**

- Required for `BY_GROUP` workload type.
- Optional for other workload types (`BY_TEST` or `GOAL_ORIENTED`).
- Scheduler configuration must match the number of Vusers in the group.

### Examples

**Group using Global RTS and Global Command Line:**

```yaml
groups:
  - name: "FrontendGroup"
    vusers: 20
    script: "frontend_script"
    hostnames: "LG1, LG2"
    globalRTS: "RTS1"
    globalCommandLine: "CmdLine1"

```

**Group with Local RTS and Scheduler (no global references):**

```yaml
groups:
  - name: "BackendGroup"
    vusers: 15
    script: "backend_script"
    hostnames: "LG3"
    pacing: "fixed delay:5/3"
    thinkTime: "modify:*1.5"
    log: "extended:on error:10:trace"
    jmeter: "StartMeasurements=true"
    javaVM: "UserSpecifiedJdk=true,JdkHome=/usr/lib/jvm/java-17"


```

> Using global RTS is recommended when multiple groups share identical runtime settings, simplifying YAML maintenance.
> Local RTS should only be used when per-group customization is required.

## 2️⃣ Controller

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
# controller is optional → LRE will allocate an available controller which is in idle status 
```

> Recommendation: Use this option only if specific controller required for your test. <br>
> Otherwise, omit this section to allow LRE to allocate an available controller.
---

## 3️⃣ Workload Type

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

| Code | Type          | SubType  | VuserDistributionMode | Scheduler Placement   |
|------|---------------|----------|-----------------------|-----------------------|
| 1    | BASIC         | BY_TEST  | BY_NUMBER             | Root-level allowed    |
| 2    | BASIC         | BY_TEST  | BY_PERCENTAGE         | Root-level allowed    |
| 3    | BASIC         | BY_GROUP | -                     | Group-level only      |
| 4    | REAL_WORLD    | BY_TEST  | BY_NUMBER             | Root-level allowed    |
| 5    | REAL_WORLD    | BY_TEST  | BY_PERCENTAGE         | Root-level allowed    |
| 6    | REAL_WORLD    | BY_GROUP | -                     | Group-level only      |
| 7    | GOAL_ORIENTED | -        | -                     | Controlled internally |

**Notes:**

- For `BY_GROUP` types (codes 3 & 6), root-level scheduler **should not be** used. Scheduler must be
  defined per group.
- For `BY_TEST` types (codes 1, 2, 4 & 5), root-level scheduler **should be** used.
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

## 4️⃣ LG assignment

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

## 5️⃣ Automatic Trending

**Field:** `automaticTrending`  
**Type:** `Object` (`AutomaticTrending`)  
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
lgAmount: 4
workloadTypeCode: 1
monitorProfileIds: [ 1001, 1002, 1003 ]
automaticTrending:
  reportId: 5
```

**Example (comprehensive):**

```yaml
automaticTrending:
  reportId: 42
  maxRuns: 50
  trendRange: "PartOfRun"
  onMaxRuns: "DeleteFirstSetNewBaseline"
  startTime: 10
  endTime: 60
```

---

## 6️⃣ Global RTS (Runtime Settings)

**Field:** `globalRts`  
**Type:** `Array` of `RTS Objects`
**Required:** ❌ (optional)

---

### Description

`globalRts` defines or updates **runtime settings (RTS)** that can be applied globally —  
to all groups or specific parts of the test.

It mirrors the `<GlobalRTS>` section in LRE’s XML test structure, providing a **YAML-friendly** abstraction.  
Each item in the list represents one `<RTS>` block from the LRE API, defined in a compact string-based format for
readability and authoring convenience.

If omitted, no global runtime settings will be created, and each group will rely on its own RTS.

---

### YAML Structure

```yaml
globalRts:
  - name: "RTS1"
    pacing: "fixed delay:5/3"
    thinkTime: "modify:*2.0"
    log: "extended:on error:15:trace"
    javaVM: "JavaEnvClassPaths=lib\\junit.jar;lib\\hamcrest.jar;user_binaries\\myapp.jar,UserSpecifiedJdk=true,JdkHome=/usr/lib/jvm/java-17,UseXboot=false,EnableClassLoaderPerVuser=true,JavaVmParameters=-Xms128m -Xmx512m -Dspring.profiles.active=test"

groups:
  - name: "SampleGroup1"
    vusers: 10
    script: "10"
    hostnames: "LG1, MyLocalLG"
    globalRTS: "RTS1"

  - name: "SampleGroup2"
    vusers: 10
    script: "10"
    hostnames: "LG1, MyLocalLG"
    globalRTS: "RTS1"

  - name: "SampleGroup3"
    vusers: 15
    script: "11"
    hostnames: "LG2"
    pacing: "random interval: 10 - 15 / 5"
    thinkTime: "random: 50 - 150 : 30"
    log: ignore

```

> With the above example, `SampleGroup1` and `SampleGroup2` uses the globally defined RTS named `RTS1`, while
`SampleGroup3` uses its own specific RTS settings locally.
> This allows for flexible configuration, applying common settings globally while customizing others at the group level.

### [Refer RTS documentation for details on each RTS setting.](ReadMe/RTS.md)

## 7️⃣ SLA Configuration

Field: `slaConfig`
Type: `Object` (`SLAConfig`)
Required: ❌ (optional)

### Description

The slaConfig section defines Service Level Agreement (SLA) thresholds for the test, enabling automated monitoring of
performance targets.
This section is optional — if omitted, no SLA monitoring will be applied.

### [Refer to SLA documentation for details on SLA configuration.](ReadMe/SLA.md)

**Example test with SLA configuration:**

```yaml
groups:
  - name: "SampleGroup"
    vusers: 50
    script: "e2e/Optional"
    hostnames: "LG1, cloud1"
    hostTemplate: "cloud1 : 1"
    pacing: "fixed delay:5/3"
    thinkTime: "modify:*2.0"
    log: "extended:on error:15:trace"

  - name: "SampleGroup2"
    vusers: 10
    script: "10"
    hostnames: "LG1, MyLocalLG"
    globalRTS: "RTS1"

sla:
  # Average Response Time Configuration
  avgResponseTimeLoadCriteria: "Hits per Second"
  avgResponseTimeLoadRanges: [ 5, 10, 15 , 20 ]
  avgResponseTimeThresholds:
    landing_page: [ 5, 10, 15, 20 , 25 ]
    login: [ 5, 10, 15, 20 , 25 ]
    logout: [ 2, 5, 12 , 25, 30 ]

  # Errors Per Second Configuration
  errorLoadCriteriaType: "Hits per Second"
  errorLoadRanges: [ 5,10,15,20 ]
  errorThreshold: [ 2, 3, 4, 5, 6 ]

```

---

## 8️⃣ Monitor Profiles

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


