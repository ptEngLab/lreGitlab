## Sample YAML file

```yaml
MonitorProfileId: 1001, 1002
Groups:
  - Name: "SampleGroup"
    Vusers: 50
    ScriptName: "e2e/Optional"
    HostNames: "LG1, LG2, cloud1, cloud2, devserver"
    HostTemplate: "cloud1 : 1, cloud2 : 2"
    Pacing: "immediately"
    ThinkTime: "modify:*2"
    Log: extended:on error:15:trace

  - Name: "SampleGroup2"
    Vusers: 10
    ScriptId: 10
    HostNames: "LG1"
    Pacing: "fixed delay:5/3"
    ThinkTime: "random:80-120"
    Log: standard:on error:20

  - Name: "SampleGroup3"
    Vusers: 15
    ScriptId: 11
    HostNames: "LG2"
    Pacing: "random interval: 10 - 15 / 5"
    ThinkTime: "random: 50 - 150 : 30"
    Log: ignore
```
