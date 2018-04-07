package org.dkpro.tc.ml.svmhmm.core;

import java.util.List;

public abstract class SvmHmm
{
    public static void runCommand(List<String> command) throws Exception
    {
        ProcessBuilder processBuilder = new ProcessBuilder(command).inheritIO();

        // run the process
        Process process = processBuilder.start();
        process.waitFor();
    }
}
