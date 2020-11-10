package ee.pnb.cgitest.archive;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.times;

import ee.pnb.cgitest.CgitestConfiguration;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.task.TaskExecutor;

@ExtendWith(MockitoExtension.class)
class ArchiveServiceTest {

  @Mock private ZipService zipService;
  @Mock private UnzipService unzipService;
  @Mock private FilePool filePool;
  @Mock private TaskExecutor taskExecutor;

  @Captor private ArgumentCaptor<Runnable> taskCaptor;

  @Captor private ArgumentCaptor<String> nameCaptor;

  private ArchiveService archiveService;

  @BeforeEach
  void init() {
    CgitestConfiguration config = new CgitestConfiguration();
    archiveService = new ArchiveService(zipService, unzipService, filePool, taskExecutor, config);
  }

  @Test
  @DisplayName("Given file count " +
               "when zip files are created " +
               "then create files with expected names")
  void zip() {
    // given
    int givenCount = 3;

    // when
    archiveService.zip(givenCount);

    // then
    then(zipService).should(times(givenCount)).createArchive(any(), nameCaptor.capture());

    List<String> actualNames = nameCaptor.getAllValues();
    assertThat(actualNames).containsExactly("file_0", "file_1", "file_2");
  }

  @Test
  @DisplayName("When unzipAll is called " +
               "then execute five instances of UnzipTask")
  void unzipAll() {
    // when
    archiveService.unzipAll();

    // then
    then(taskExecutor).should(times(5)).execute(taskCaptor.capture());
    assertThat(taskCaptor.getAllValues())
        .allSatisfy(task -> assertThat(task).isInstanceOf(UnzipTask.class));
  }

}