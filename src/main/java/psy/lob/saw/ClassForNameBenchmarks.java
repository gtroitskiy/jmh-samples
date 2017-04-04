package psy.lob.saw;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Param;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.infra.Blackhole;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@State(Scope.Thread)
public class ClassForNameBenchmarks {
  private static final LoadingCache<String, Class<?>> CLASS_CACHE = CacheBuilder
          .newBuilder()
          .concurrencyLevel(Runtime.getRuntime().availableProcessors())
          .build(new CacheLoader<String, Class<?>>() {
            @Override
            public Class<?> load(String input) throws RuntimeException {
              try {
                return Class.forName(input);
              } catch (ClassNotFoundException e) {
                throw new RuntimeException(e);
              }
            }
          });

  @Param({"32", "1024", "32768"})
  int size;

  Class[] classes = {Object.class, Integer.class, String.class, Short.class, Long.class, Double.class,
          Float.class, Boolean.class, Character.class, Byte.class};
  String[] classNames;

  @Setup
  public void prepare() {
    classNames = new String[size];
    for (int i = 0; i < size; i++) {
      classNames[i] = classes[i % classes.length].getName();
    }
  }

  @Benchmark
  public void generalClassLoading(Blackhole fox) throws ExecutionException, ClassNotFoundException {
    for (int y = 0; y < classes.length; y++) {
      fox.consume(Class.forName(classNames[y], false, sun.misc.VM.latestUserDefinedLoader()));
    }
  }

  @Benchmark
  public void cachedClassLoading(Blackhole fox) throws ExecutionException {
    for (int y = 0; y < classes.length; y++) {
      fox.consume(CLASS_CACHE.get(classNames[y]));
    }
  }
}
