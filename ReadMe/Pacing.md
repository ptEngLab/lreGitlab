# 📘 LRE Pacing – Full Examples

The LrePacingValidator validates pacing for groups in LRE tests. The following examples illustrate all supported
formats.

## 1. Immediate Start

* No delay.
* Number of iterations defaults to 1.

```yaml
# Start new iteration immediately after the previous
Pacing: immediately
```

## 2. Fixed Delay

* X → delay in seconds between iterations.

* /Y → number of iterations (optional, defaults to 1).

```yaml
# Fixed 5-second delay, 3 iterations
Pacing: fixed delay:5/3

# Fixed 10-second delay, 1 iteration (default)
Pacing: fixed delay:10

```

## 3. Random Delay

* min-max → range of delay in seconds.

* /Y → number of iterations (optional, defaults to 1).

```yaml
# Random delay between 10–20 seconds, 2 iterations
Pacing: random delay:10-20/2

# Random delay between 5–15 seconds, 1 iteration
Pacing: random delay:5-15
```

## 4. Fixed Interval

Interval controls the time between starting each iteration, regardless of iteration duration.

```yaml
# Fixed interval of 8 seconds, default 1 iteration
Pacing: fixed interval:8

# Fixed interval 12 seconds, 4 iterations
Pacing: fixed interval:12/4
```

## 5. Random Interval

* Interval is randomly selected from the specified range.

* /Y → number of iterations (optional, defaults to 1).

```yaml
# Random interval between 10–15 seconds, 5 iterations
Pacing: random interval:10-15/5

# Random interval 20–25 seconds, 1 iteration
Pacing: random interval:20-25
```

## 6. Summary Table of Examples

| Example                   | Type            | Format                | Meaning                        |
|---------------------------|-----------------|-----------------------|--------------------------------|
| `immediately`             | Immediate       | immediately           | Start immediately              |
| `fixed delay:5/3`         | Fixed Delay     | fixed delay:X/Y       | 5 sec delay, 3 iterations      |
| `random delay:10-20/2`    | Random Delay    | random delay:X-Y/Y    | Random 10–20 sec, 2 iterations |
| `fixed interval:12/4`     | Fixed Interval  | fixed interval:X/Y    | 12 sec interval, 4 iterations  |
| `random interval:10-15/5` | Random Interval | random interval:X-Y/Y | Random 10–15 sec, 5 iterations |

## ✅ Key Rules:

* Iterations are optional, defaulting to 1.
* Delays and intervals must be positive integers.
* Random ranges must satisfy min ≤ max.
* Invalid formats will throw a PacingException.