package edu.ucsd.rsync;

import com.github.fracpete.processoutput4j.output.ConsoleOutputProcessOutput;
import com.github.fracpete.rsync4j.RSync;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class RsyncAdapter {

    private RSync rsync;

    public RsyncAdapter(String source, String destination) {
        log.warn("Setting up rsync from {} to {}", source, destination);

        rsync = new RSync()
                .source(source)
                .destination(destination)
                .archive(true)
                .delete(true);
    }

    public void sync() throws Exception {
        log.warn("Rsync starting...");
        ConsoleOutputProcessOutput output = new ConsoleOutputProcessOutput();
        output.monitor(rsync.builder());
        log.warn("Rsync finished");
    }

}
