package org.dkpro.tc.examples.deeplearning.keras;

import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;

import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.io.IOUtils;
import org.dkpro.tc.examples.single.sequence.LabFolderTrackerReport;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.spotify.docker.client.DefaultDockerClient;
import com.spotify.docker.client.DockerClient;
import com.spotify.docker.client.LogStream;
import com.spotify.docker.client.messages.ContainerConfig;
import com.spotify.docker.client.messages.ContainerCreation;
import com.spotify.docker.client.messages.ExecCreation;

public class KerasTest
{
    private static final String PREDICTION_FILE = "/root/prediction.txt";

    DockerClient docker;
    ContainerConfig containerConfig;
    ContainerCreation creation;
    String id;

    @Before
    public void setup()
        throws Exception
    {

        // try {
        // DeepLearningKerasSeq2SeqTrainTest.main(null);
        // }
        // catch (Exception e) {
        // // Ignore silently, this will crash for sure on systems where Keras is not installed at
        // // the expected location
        // }

        docker = DefaultDockerClient.fromEnv().build();

        containerConfig = ContainerConfig.builder().image("dkprotc").attachStdout(Boolean.TRUE)
                .attachStderr(Boolean.TRUE).attachStdin(Boolean.TRUE).tty(true).user("root")
                .build();

        creation = docker.createContainer(containerConfig);
        id = creation.id();

    }

    @After
    public void cleanUp()
        throws Exception
    {
        // Kill container
        docker.killContainer(id);

        // Remove container
        // docker.removeContainer(id);

        // Close the docker client
        docker.close();
    }

    @Test
    public void runKerasTrainTest()
        throws Exception
    {
        // verifyOutputFolders();

        LabFolderTrackerReport.vectorizationTaskTrain = new File(
                "/Users/toobee/Documents/Eclipse/dkpro-tc/dkpro-tc-examples/target/results/DeepLearningKerasSeq2SeqTrainTest/org.dkpro.lab/repository/VectorizationTask-Train-KerasSeq2Seq-20170607085853128")
                        .getAbsolutePath();
        LabFolderTrackerReport.vectorizationTaskTest = new File(
                "/Users/toobee/Documents/Eclipse/dkpro-tc/dkpro-tc-examples/target/results/DeepLearningKerasSeq2SeqTrainTest/org.dkpro.lab/repository/VectorizationTask-Test-KerasSeq2Seq-20170607085853679")
                        .getAbsolutePath();
        LabFolderTrackerReport.preparationTask = new File(
                "/Users/toobee/Documents/Eclipse/dkpro-tc/dkpro-tc-examples/target/results/DeepLearningKerasSeq2SeqTrainTest/org.dkpro.lab/repository/PreparationTask-KerasSeq2Seq-20170607085852347")
                        .getAbsolutePath();

        createFolderInContainer();

        copyFiles(LabFolderTrackerReport.vectorizationTaskTrain, "/root/train");
        copyFiles(LabFolderTrackerReport.vectorizationTaskTest, "/root/test");
        System.err.println("Copied folders");
        copyCode("src/main/resources/kerasCode/seq/", "/root");
        System.err.println("Copied code");

        runCode();
        System.err.println("Ran code");
        retrievePredictions();
        System.err.println("Retrieved prediction");

        // final String[] command = { "bash", "-c", "cd /root/train/; ls" };
        // final ExecCreation execCreation = docker.execCreate(id, command,
        // DockerClient.ExecCreateParam.attachStdout(),
        // DockerClient.ExecCreateParam.attachStderr());
        // final LogStream output = docker.execStart(execCreation.id());
        // final String execOutput = output.readFully();
        // System.out.println(execOutput);

    }

    private void retrievePredictions()
        throws Exception
    {
        TarArchiveInputStream tarStream = new TarArchiveInputStream(
                docker.archiveContainer(id, PREDICTION_FILE));

        TarArchiveEntry entry = null;
        while ((entry = (TarArchiveEntry) tarStream.getNextEntry()) != null) {
            OutputStream outputFileStream = new FileOutputStream(
                    new File("/Users/toobee/Desktop/" + entry.getName()));
            IOUtils.copy(tarStream, outputFileStream);
            outputFileStream.close();

            // FileUtils.copyFile(file, new File("/Users/toobee/Desktop/"+entry.getName()));
        }
        tarStream.close();
    }

    private void runCode()
        throws Exception
    {
        String[] command = { "bash", "-c",
                "python3 /root/posTaggingLstm.py /root/train/instanceVectors.txt /root/train/outcomeVectors.txt /root/test/instanceVectors.txt /root/test/outcomeVectors.txt '' 75 "
                        + PREDICTION_FILE };

        // String[] command = { "bash", "-c", "python3", "/root/posTaggingLstm.py",
        // "/root/train/instanceVectors.txt", "/root/train/outcomeVectors.txt",
        // "/root/test/instanceVectors.txt", "/root/test/outcomeVectors.txt", "", "75",
        // PREDICTION_FILE };

        ExecCreation execCreation = docker.execCreate(id, command,
                DockerClient.ExecCreateParam.attachStdout(),
                DockerClient.ExecCreateParam.attachStderr());
        LogStream output = docker.execStart(execCreation.id());
        String readFully = output.readFully();
        System.out.println("[" + readFully + "]");

    }

    private void copyCode(String source, String target)
        throws Exception
    {
        docker.copyToContainer(new File(source).toPath(), id, target);
    }

    private void createFolderInContainer()
        throws Exception
    {
        final ContainerConfig containerConfig = ContainerConfig.builder().image("dkprotc")
                .user("root").attachStdout(Boolean.TRUE).attachStderr(Boolean.TRUE)
                .attachStdin(Boolean.TRUE).tty(true).build();

        final ContainerCreation creation = docker.createContainer(containerConfig);
        id = creation.id();

        docker.startContainer(id);

        mkdir(docker, id, "/root/train");
        mkdir(docker, id, "/root/test");
    }

    private void mkdir(DockerClient docker, String id, String folder)
        throws Exception
    {
        String[] command = { "bash", "-c", "mkdir " + folder };
        ExecCreation execCreation = docker.execCreate(id, command,
                DockerClient.ExecCreateParam.attachStdout(),
                DockerClient.ExecCreateParam.attachStderr());
        LogStream output = docker.execStart(execCreation.id());
        output.readFully();
    }

    private void copyFiles(String folder, String out)
        throws Exception
    {
        docker.copyToContainer(new File(folder + "/output/").toPath(), id, out);
    }

    private void verifyOutputFolders()
    {
        assertTrue(new File(LabFolderTrackerReport.vectorizationTaskTrain).exists());
        assertTrue(new File(LabFolderTrackerReport.vectorizationTaskTest).exists());
        assertTrue(new File(LabFolderTrackerReport.preparationTask).exists());
    }
}
