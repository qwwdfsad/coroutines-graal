### Flow benchmark

`FlowBenchmark` is constructed to expose a non-standard pattern which Graal fails to compile.
`Flow` is a very simplified version of [`kotlinx.coroutines.Flow`](https://kotlin.github.io/kotlinx.coroutines/kotlinx-coroutines-core/kotlinx.coroutines.flow/-flow/index.html), a suspension-based primitive for operating with reactive streams.

### Benchmark results
How to run: `./gradlew --no-daemon cleanJmhJar jmhJar && java -jar benchmarks.jar` from the root folder.

Results:
```
// Java 1.8.0_162-b12
FlowBenchmark.flowBaseline  avgt  7  3.542 ± 0.026  us/op

// Graalvm-ce-19.0.0
FlowBenchmark.flowBaseline  avgt  7  54.129 ± 0.387  us/op
```

`dtraceasm` profiler shows that all the time spent in the interpreter, mostly in `fast_aputfield` (probably it is a coroutine state machine spilling).
 
Native call-stacks obtained via `async-profiler` are polluted with `InterpreterRuntime::frequency_counter_overflow` from the uppermost Java frame (`flow.FlowBenchmark$numbers$$inlined$flow$1::collect`), that is, by the way, compiled with C1.

### Compilation log

[Compilation log](#compilation-log) contains pretty suspicious statements about target method:
```
294  498  3  flow.FlowBenchmark$numbers$$inlined$flow$1::collect (255 bytes) COMPILE SKIPPED: live_in set of first block not empty (retry at different tier)
337  535  4  flow.FlowBenchmark$numbers$$inlined$flow$1::collect (255 bytes) COMPILE SKIPPED: Non-reducible loop (not retryable)
```
