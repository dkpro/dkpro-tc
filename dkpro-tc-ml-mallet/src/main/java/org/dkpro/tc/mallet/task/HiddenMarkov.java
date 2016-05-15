package org.dkpro.tc.mallet.task;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import cc.mallet.fst.HMM;
import cc.mallet.fst.HMMTrainerByLikelihood;
import cc.mallet.fst.NoopTransducerTrainer;
import cc.mallet.fst.Transducer;
import cc.mallet.fst.TransducerTrainer;
import cc.mallet.pipe.Pipe;
import cc.mallet.pipe.SerialPipes;
import cc.mallet.pipe.SimpleTaggerSentence2TokenSequence;
import cc.mallet.pipe.TokenSequence2FeatureSequence;
import cc.mallet.pipe.iterator.LineGroupIterator;
import cc.mallet.types.Instance;
import cc.mallet.types.InstanceList;
import cc.mallet.types.Sequence;

public class HiddenMarkov
{
    private File trainFile;
    private File testFile;
    private File modelFile;
    private File prediction;
    private Integer iterations;

    public HiddenMarkov(File fileTrain, File fileTest, File fileModel, File prediction,
            Integer iterations)
    {
        this.trainFile = fileTrain;
        this.testFile = fileTest;
        this.modelFile = fileModel;
        this.prediction = prediction;
        this.iterations = iterations;
    }

    public void run()
        throws Exception
    {
        runTrainHmm();
        runTestHmm();
    }

    private void runTrainHmm()
        throws Exception
    {
        Reader trainingFileReader = new InputStreamReader(new FileInputStream(trainFile));

        ArrayList<Pipe> pipes = new ArrayList<Pipe>();

        pipes.add(new SimpleTaggerSentence2TokenSequence());
        pipes.add(new TokenSequence2FeatureSequence());
        Pipe p = new SerialPipes(pipes);
        p.setTargetProcessing(true);
        InstanceList trainingData = new InstanceList(p);
        trainingData.addThruPipe(
                new LineGroupIterator(trainingFileReader, Pattern.compile("^\\s*$"), true));

        HMM hmm = new HMM(trainingData.getPipe(), null);
        hmm.addStatesForLabelsConnectedAsIn(trainingData);
        HMMTrainerByLikelihood trainer = new HMMTrainerByLikelihood(hmm);
        trainer.train(trainingData, iterations);

        hmm.write(modelFile);
    }

    public void runTestHmm()
        throws Exception
    {
        Reader testFileReader = new InputStreamReader(new FileInputStream(testFile), "UTF-8");

        ObjectInputStream s = new ObjectInputStream(new FileInputStream(modelFile));
        HMM hmm = (HMM) s.readObject();
        s.close();

        List<Pipe> pipes = new ArrayList<Pipe>();

        pipes.add(new SimpleTaggerSentence2TokenSequence());
        pipes.add(new TokenSequence2FeatureSequence());
        Pipe p = new SerialPipes(pipes);
        // Pipe p = hmm.getInputPipe();
        p.setTargetProcessing(true);

        InstanceList testData = new InstanceList(p);
        testData.addThruPipe(
                new LineGroupIterator(testFileReader, Pattern.compile("^\\s*$"), true));

        writePrediction(new NoopTransducerTrainer(hmm), testData, prediction);
    }

    @SuppressWarnings("rawtypes")
    public void writePrediction(TransducerTrainer tt, InstanceList testing, File filePredictions)
        throws Exception
    {

        BufferedWriter writer = new BufferedWriter(
                new OutputStreamWriter(new FileOutputStream(filePredictions), "utf-8"));
        writer.write("#prediction;gold" + "\n");

        Transducer model = tt.getTransducer();

        for (int i = 0; i < testing.size(); i++) {
            Instance instance = testing.get(i);
            Sequence input = (Sequence) instance.getData();
            Sequence gold = (Sequence) instance.getTarget();
            assert(input.size() == gold.size());
            Sequence pred = model.transduce(input);
            assert(pred.size() == gold.size());
            for (int j = 0; j < gold.size(); j++) {
                String g = gold.get(j).toString();
                String p = pred.get(j).toString();
                writer.write(p + ";" + g + "\n");
            }
        }
        writer.close();
    }
}
