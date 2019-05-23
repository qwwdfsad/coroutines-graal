package flow

import org.openjdk.jmh.annotations.*
import java.util.concurrent.*
import kotlin.coroutines.*

@Warmup(iterations = 7, time = 1, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = 7, time = 1, timeUnit = TimeUnit.SECONDS)
@Fork(value = 1)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MICROSECONDS)
@State(Scope.Benchmark)
open class FlowBenchmark {

    private fun numbers(): Flow<Long> = flow {
        for (i in 1L..1000L) emit(i)
    }

    @Benchmark
    fun flowBaseline(): Int {
        val completion = Completion()
        suspend { numbers().count() }.startCoroutine(completion)
        return completion.count
    }

    class Completion: Continuation<Int> {
        @JvmField
        var count: Int = 0

        override val context: CoroutineContext
            get() = EmptyCoroutineContext

        override fun resumeWith(result: Result<Int>) {
            count = result.getOrElse { 0 }
        }
    }
}
