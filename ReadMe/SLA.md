# SLA Configuration Guide

This document provides guidance for configuring SLA (Service Level Agreement) parameters for performance tests using a
YAML configuration file.

---

## SLAConfig Section

The `SLAConfig` section in your YAML file defines thresholds for various SLA metrics such as response time, errors, and
throughput.

### Example YAML

```yaml
SLAConfig:
  # Average Response Time Configuration
  AvgResponseTimeLoadCriteria: "Hits per Second"
  AvgResponseTimeLoadRanges: [ 5, 10, 15, 20 ]
  AvgResponseTimeThresholds:
    landing_page: [ 5, 10, 15, 20, 25 ]
    login: [ 5, 10, 15, 20, 25 ]
    logout: [ 2, 5, 12, 25, 30 ]

  # Errors Per Second Configuration
  ErrorLoadCriteriaType: "Hits per Second"
  ErrorLoadRanges: [ 5, 10, 15, 20 ]
  ErrorThreshold: [ 2, 3, 4, 5, 6 ]

  # Percentile Response Time Configuration
  PercentileResponseTimeThreshold: 95
  PercentileResponseTimeTransactions:
    landing_page: 5
    login: 8

  TotalHits: 20
  AverageHitsPerSecond: 50
  TotalThroughput: 80000
  AverageThroughput: 5000
```

> ⚠️ Important: 
> Provide either **Average Response Time SLA** or **Percentile Response Time SLA**. Both cannot be configured together. 

### Fields Description

#### Average Response Time SLA

* `AvgResponseTimeLoadCriteria`: Load criteria type (e.g., Hits per Second, Running VUsers).
* `AvgResponseTimeLoadRanges`: List of load values for which thresholds are defined.
* `AvgResponseTimeThresholds`: Map of transaction names to threshold values.

#### Errors Per Second SLA

* `ErrorLoadCriteriaType`: Load criteria type (e.g., Hits per Second, Running VUsers).
* `ErrorLoadRanges`: List of load values for which error thresholds are defined.
* `ErrorThreshold`: List of error thresholds corresponding to the load ranges.

#### Percentile Response Time SLA

* `PercentileResponseTimeThreshold`: Percentile (e.g., 95).
* `PercentileResponseTimeTransactions`: Map of transaction names to maximum allowed response time for the specified
  percentile.

#### Simple SLA Metrics

* `TotalHits`: Total number of hits expected.
* `AverageHitsPerSecond`: Average hits per second expected.
* `TotalThroughput`: Total throughput expected.
* `AverageThroughput`: Average throughput expected.

---

### Table: Supported Load Criteria Types

The following values are allowed:

| Criteria Type                    | Description                                  | Avg Response Time Criteria | Error Criteria Type |
|:---------------------------------|:---------------------------------------------|:--------------------------:|:-------------------:|
| `Hits per Second`                | Number of HTTP requests per second           |             ✅              |          ✅          |
| `Running VUsers`                 | Number of concurrent virtual users           |             ✅              |          ✅          |
| `Throughput`                     | Total throughput in bytes or KB              |             ✅              |          ✅          |
| `Transactions per Second`        | Number of business transactions per second   |             ✅              |          ❌          |
| `Transactions per Second Passed` | Number of successful transactions per second |             ✅              |          ❌          |


> ⚠️ Important: <br> 
> 
> **Average Response Time SLA** and **Percentile Response Time SLA** cannot be configured together. Only one response time SLA
> type can be provided.


---

## Rules & Validation

1. **Mutual Exclusivity**: You cannot use both Average Response Time SLA and Percentile Response Time SLA together.
2. **Thresholds**:
    * Number of thresholds must match load ranges plus two (for less than and greater than thresholds).
    * Maximum of 5 thresholds allowed.
3. **Load Ranges**: Must be provided in ascending order.
4. **Transaction names** must match those defined in the test script.

---

## Example


```yaml
SLAConfig:
  # Average Response Time Configuration
  AvgResponseTimeLoadCriteria: "Hits per Second"
  AvgResponseTimeLoadRanges: [100, 200, 300]
  AvgResponseTimeThresholds:
    home_page: [2, 3, 4, 5]
    search: [3, 4, 5, 6]

  # Errors Per Second Configuration
  ErrorLoadCriteriaType: "Hits per Second"
  ErrorLoadRanges: [100, 200, 300]
  ErrorThreshold: [1, 2, 3, 4]

  # Percentile Response Time Configuration
  PercentileResponseTimeThreshold: 95
  PercentileResponseTimeTransactions:
    home_page: 2
    search: 3

  # Simple SLA Metrics
  TotalHits: 50000
  AverageHitsPerSecond: 100
  TotalThroughput: 80000
  AverageThroughput: 5000
```


## Average Response Time SLA Explained

### Load Criteria & Ranges

```yaml
AvgResponseTimeLoadCriteria: "Hits per Second"
AvgResponseTimeLoadRanges: [100, 200, 300]
```

- **Zone 1:** Light traffic (< 100 hits/sec)
- **Zone 2:** Medium traffic (100–200 hits/sec)
- **Zone 3:** High traffic (200–300 hits/sec)
- **Zone 4:** Very high traffic (≥ 300 hits/sec)

### Response Time Thresholds

```yaml
AvgResponseTimeThresholds:
  home_page: [2, 3, 4, 5]
  search: [3, 4, 5, 6]
```

**Interpretation:**

- **For the Home Page:**

  * When traffic is light (< 100 hits/sec): Page must load in under 2 seconds

  * When traffic is medium (100-200 hits/sec): Page must load in under 3 seconds
 
  * When traffic is high (200-300 hits/sec): Page must load in under 4 seconds
 
  * When traffic is very high (≥ 300 hits/sec): Page must load in under 5 seconds
 
- **For the Search Page:**

  * When traffic is light: Search must complete in under 3 seconds
  
  * When traffic is medium: Search must complete in under 4 seconds
  
  * When traffic is high: Search must complete in under 5 seconds
  
  * When traffic is very high: Search must complete in under 6 seconds


---

## Errors Per Second SLA Explained

### Load Criteria & Ranges

```yaml
ErrorLoadCriteriaType: "Hits per Second"
ErrorLoadRanges: [100, 200, 300]
```

### Error Thresholds

```yaml
ErrorThreshold: [1, 2, 3, 4]
```

**Interpretation:**
Across all pages and transactions:

* When traffic is light (< 100 hits/sec): Maximum 1 error per second allowed

* When traffic is medium (100-200 hits/sec): Maximum 2 errors per second allowed

* When traffic is high (200-300 hits/sec): Maximum 3 errors per second allowed

* When traffic is very high (≥ 300 hits/sec): Maximum 4 errors per second allowed


## Simple SLA Metrics Explained

**What they measure:**  
"Overall performance goals for the entire test"

```yaml
TotalHits: 50000
AverageHitsPerSecond: 100
TotalThroughput: 80000
AverageThroughput: 5000
```

**Interpretation:**

- **Total Hits (50,000):** Handle at least 50,000 page views successfully
- **Average Hits Per Second (100):** Maintain 100 requests/sec on average
- **Total Throughput / Average Throughput:** Throughput goals in KB or bytes

---

