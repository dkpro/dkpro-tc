package org.dkpro.tc.examples.deeplearning.keras;

import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;

import org.dkpro.tc.core.DeepLearningConstants;
import org.dkpro.tc.examples.single.sequence.LabFolderTrackerReport;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.spotify.docker.client.DefaultDockerClient;
import com.spotify.docker.client.DockerClient;
import com.spotify.docker.client.LogStream;
import com.spotify.docker.client.exceptions.DockerException;
import com.spotify.docker.client.messages.ContainerConfig;
import com.spotify.docker.client.messages.ContainerCreation;
import com.spotify.docker.client.messages.ExecCreation;

public class KerasTest
{
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
                .attachStderr(Boolean.TRUE).attachStdin(Boolean.TRUE).tty(true).user("root").build();

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
        docker.removeContainer(id);

        // Close the docker client
        docker.close();
    }

    @Test
    public void runKerasTrainTest()
        throws Exception
    {
//        verifyOutputFolders();

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
        
        copyFiles(LabFolderTrackerReport.vectorizationTaskTrain, "train");
        copyFiles(LabFolderTrackerReport.vectorizationTaskTest, "test");
        
        final String[] command = { "bash", "-c", "cd /root/train/; ls" };
        final ExecCreation execCreation = docker.execCreate(id, command,
                DockerClient.ExecCreateParam.attachStdout(),
                DockerClient.ExecCreateParam.attachStderr());
        final LogStream output = docker.execStart(execCreation.id());
        final String execOutput = output.readFully();
        System.out.println(execOutput);

    }

    private void createFolderInContainer() throws Exception
    {
        final ContainerConfig containerConfig = ContainerConfig.builder().image("dkprotc").user("root")
                .attachStdout(Boolean.TRUE).attachStderr(Boolean.TRUE)
                .attachStdin(Boolean.TRUE).tty(true)
                .build();
        

        final ContainerCreation creation = docker.createContainer(containerConfig);
        id = creation.id();

        docker.startContainer(id);
        
        String[] command = { "bash", "-c", "mkdir /root/train" };
        ExecCreation execCreation = docker.execCreate(id, command
                ,
                DockerClient.ExecCreateParam.attachStdout(),
                DockerClient.ExecCreateParam.attachStderr()
                );
        LogStream output = docker.execStart(execCreation.id());
        output.readFully();
        
        
        command = new String [] { "bash", "-c", "mkdir /root/test" };
        execCreation = docker.execCreate(id, command
                ,
                DockerClient.ExecCreateParam.attachStdout(),
                DockerClient.ExecCreateParam.attachStderr()
                );
        output = docker.execStart(execCreation.id());
        output.readFully();
    }

    private void copyFiles(String folder, String out) throws Exception
    {
        docker.copyToContainer(new File(folder+"/output/").toPath(), id, "/root/"+out);
    }

    private void verifyOutputFolders()
    {
        assertTrue(new File(LabFolderTrackerReport.vectorizationTaskTrain).exists());
        assertTrue(new File(LabFolderTrackerReport.vectorizationTaskTest).exists());
        assertTrue(new File(LabFolderTrackerReport.preparationTask).exists());
    }
}
