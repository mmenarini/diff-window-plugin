package edu.ucsd.git;

import lombok.extern.slf4j.Slf4j;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;

import java.io.File;
import java.io.IOException;

@Slf4j
public class GitAdapter {

    private Git git;

    public GitAdapter(String projectDir) throws IOException {
        Repository repo = new FileRepositoryBuilder()
                .setGitDir(new File(projectDir + "/.git"))
                .build();
        git = new Git(repo);
    }

    public void addAllChangesToIndex() throws IllegalStateException {
        try {
            git.add()
                    .addFilepattern(".")
                    .call();
        } catch (GitAPIException e) {
            throw new IllegalStateException("Failed to stage files for commit", e);
        }
    }

    public void commit(String message) throws IllegalStateException {
        try {
            git.commit()
                    .setMessage(message)
                    .call();
        } catch (GitAPIException e) {
            throw new IllegalStateException("Failed to commit index", e);
        }
    }

    public void commitAllChanges(String message) {
        this.addAllChangesToIndex();
        this.commit(message);
    }

    public String getHashOfHead() throws IOException {
        return getHashOfAncestorCommit(0);
    }

    public String getHashOfFirstParent() throws IOException {
        return getHashOfAncestorCommit(1);
    }

    public String getHashOfAncestorCommit(int generationsBeforeHead) throws IOException {
        String command = "HEAD";
        if (generationsBeforeHead > 0) {
            command += "~" + generationsBeforeHead;
        }

        return git.getRepository().resolve(command).getName();
    }
}
