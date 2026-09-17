package uk.gov.hmcts.reform.ccd.util.log;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@SuppressWarnings({"PMD.AvoidInstantiatingObjectsInLoops", "PMD.CloseResource"})
class TaskDeleteRecordHolderTest {

    @Test
    void shouldHoldTasksDeletionResults() {
        final TasksDeletionRecordHolder taskDeletionRecordHolder = new TasksDeletionRecordHolder();

        final Integer caseTasksDeletionResult1 = 201;
        final Integer caseTasksDeletionResult2 = 403;

        taskDeletionRecordHolder.setCaseTasksDeletionResults("123",caseTasksDeletionResult1);
        taskDeletionRecordHolder.setCaseTasksDeletionResults("456", caseTasksDeletionResult2);

        final int tasksDeletionResults1 = taskDeletionRecordHolder.getTasksDeletionResults("123");
        final int tasksDeletionResults2 = taskDeletionRecordHolder.getTasksDeletionResults("456");

        assertThat(tasksDeletionResults1).isEqualTo(caseTasksDeletionResult1);
        assertThat(tasksDeletionResults2).isEqualTo(caseTasksDeletionResult2);

    }

    @SuppressWarnings("PMD.CognitiveComplexity")
    @Test
    void shouldHandleConcurrentReadsAndWritesUnderLoad() throws Exception {
        final int writerThreads = 8;
        final int readerThreads = 8;
        final int operationsPerThread = 3_000;
        final int totalThreads = writerThreads + readerThreads;
        final int rounds = 40;

        for (int round = 0; round < rounds; round++) {
            final TasksDeletionRecordHolder holder = new TasksDeletionRecordHolder();
            ExecutorService pool = Executors.newFixedThreadPool(totalThreads);
            CyclicBarrier barrier = new CyclicBarrier(totalThreads);
            List<Future<?>> futures = new ArrayList<>();

            for (int i = 0; i < writerThreads; i++) {
                final int writerId = i;
                futures.add(pool.submit(() -> {
                    barrier.await();
                    for (int j = 0; j < operationsPerThread; j++) {
                        holder.setCaseTasksDeletionResults(
                            "case-" + writerId + "-" + j,
                            200 + (j % 10)
                        );
                    }
                    return null;
                }));
            }

            for (int i = 0; i < readerThreads; i++) {
                final int readerId = i;
                futures.add(pool.submit(() -> {
                    barrier.await();
                    for (int j = 0; j < operationsPerThread; j++) {
                        holder.getTasksDeletionResults("missing-" + readerId + "-" + j);
                        holder.getTasksDeletionResults("case-" + (j % writerThreads) + "-" + (j % operationsPerThread));
                    }
                    return null;
                }));
            }

            for (Future<?> future : futures) {
                future.get();
            }

            pool.shutdown();
            assertThat(pool.awaitTermination(30, TimeUnit.SECONDS)).isTrue();

            final int expectedEntries = writerThreads * operationsPerThread;
            assertThat(expectedEntries).isEqualTo(holder.getTasksDeletionByCaseRef().size());

            for (int i = 0; i < writerThreads; i++) {
                for (int j = 0; j < operationsPerThread; j++) {
                    final String caseRef = "case-" + i + "-" + j;
                    final int expectedStatus = 200 + (j % 10);
                    assertThat(holder.getTasksDeletionResults(caseRef)).isEqualTo(expectedStatus);
                }
            }
        }
    }
}
