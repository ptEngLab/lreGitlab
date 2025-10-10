## Sample YAML file

```yaml
groups:
  - name: "SampleGroup"                 # Logical name of the Vuser group
    vusers: 35                          # Total number of virtual users in this group
    script: "e2e/Optional"              # Path or script id of the test script to run
    hostnames: "LG1"                    # 1 Automatch LG
    pacing: "fixed delay:5"             # e.g., after previous iteration ends, new iteration will be started after fixed delay of 5 seconds
    thinkTime: "modify:*2.0"            # Multiply recorded think time by 2.0x

  - name: "SampleGroup2"
    vusers: 10
    script: "10"                        # Reference to another script or ID
    hostnames: "LG1, MyLocalLG"

  - name: "SampleGroup3"
    vusers: 15
    script: "11"
    hostnames: "LG2"
    pacing: "random interval: 10 - 15"  # Random pacing between 10–15 seconds
    thinkTime: "random: 50 - 150 : 30"  # Use the random percentage of recorded thinkTime between 50–150%, but limit the thinktime to max 30secs
    log: ignore                         # Disable logging

scheduler:
  - initialize: "gradually:10u@30s"
  - startVusers: "10vu:gradually:2U@10s"
  - duration: "30 s"
  - stopVusers: "60vu:simultaneously"

sla:

  # --- Errors Per Second Criteria ---
  errorLoadCriteriaType: "Hits per Second"
  errorLoadRanges: [ 5, 10, 15, 20 ]
  errorThreshold: [ 2, 3, 4, 5, 6 ]

  # Simple SLA criteria example
  totalHits: 20
  avgHitsPerSecond: 50
  totalThroughput: 80000
  avgThroughput: 5000
```



```yaml
lgAmount: 3                             # 3 automatch LGs for all the groups. No need to specify hostnames in groups section now.
groups:
  - name: "SampleGroup"                 # Logical name of the Vuser group
    vusers: 35                          # Total number of virtual users in this group
    script: "e2e/Optional"              # Path or script id of the test script to run
    pacing: "fixed delay:5"             # e.g., after previous iteration ends, new iteration will be started after fixed delay of 5 seconds
    thinkTime: "modify:*2.0"            # Multiply recorded think time by 2.0x
    
    # this group uses default values for pacing as start immediately after previous iteration ends, 
  - name: "SampleGroup2"
    vusers: 10
    script: "10"                        # Reference to another script or ID

  - name: "SampleGroup3"
    vusers: 15
    script: "11"
    pacing: "random interval: 10 - 15"  # Random pacing between 10–15 seconds
    thinkTime: "random: 50 - 150 : 30"  # Use the random percentage of recorded thinkTime between 50–150%, but limit the thinktime to max 30secs
    log: ignore                         # Disable logging

scheduler:
  - initialize: "gradually:10u@30s"
  - startVusers: "10vu:gradually:2U@10s"
  - duration: "30 s"
  - stopVusers: "60vu:simultaneously"

sla:

  # --- Errors Per Second Criteria ---
  errorLoadCriteriaType: "Hits per Second"
  errorLoadRanges: [ 5, 10, 15, 20 ]
  errorThreshold: [ 2, 3, 4, 5, 6 ]

  # Simple SLA criteria example
  totalHits: 20
  avgHitsPerSecond: 50
  totalThroughput: 80000
  avgThroughput: 5000
```
