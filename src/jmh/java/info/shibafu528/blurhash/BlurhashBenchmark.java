package info.shibafu528.blurhash;

import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

@Fork(2)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
public class BlurhashBenchmark {

    private ExecutorService executor = Executors.newCachedThreadPool();

    @Benchmark
    @BenchmarkMode(Mode.AverageTime)
    public int[] decode() {
        return new Blurhash().decode("LNAdApj[00aymkj[TKay9}ay-Sj[", 128, 128, 1);
    }

    @Benchmark
    @BenchmarkMode(Mode.AverageTime)
    public int[] decodeMT() {
        return new MTBlurhash(executor, 8).decode("LNAdApj[00aymkj[TKay9}ay-Sj[", 128, 128, 1);
    }
}
