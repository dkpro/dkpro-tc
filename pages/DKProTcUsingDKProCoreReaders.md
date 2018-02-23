---
layout: page-fullwidth
title: "DKPro TC Using DKPro Core Readers"
permalink: "/DKProTcUsingDKProCoreReaders/"
---

DKProTC works with two data types that are expected to be present when an experiment is executed. `TextClassificationTarget` and the corresponding actual value of this target, the `TextClassificationOutcome`. For sequence classification examples an additional `TextClassificationSequence` is necessary that marks explicitly the span of the sequence. The data readers provided by DKProTC set these values in the reader. When one of the many data format readers in DKPro Core are used, these information are not set, yet. The two required data types exist only in DKPro TC and are not used by DKPro Core.

Consequently, an additional step is needed that adds the required information when readers from DKPro Core (or elsewhere) are used. This is most easily done by adding a `Preprocessing` step to a DKPro TC experiment. Below is an example how it could be used for Part-of-Speech Tagging for instance.
In order to be used in the `Preprocessing`, the class have to inherit from `JCasAnnotator_ImplBase`:

{% highlight java %} 
public class SequenceOutcomeAnnotator
    extends JCasAnnotator_ImplBase
{
    int tcId = 0;

    @Override
    public void process(JCas aJCas)
        throws AnalysisEngineProcessException
    {
        // Iterate all sentences
        for (Sentence sent : JCasUtil.select(aJCas, Sentence.class)) {
        
            // Each sentence is a classification sequence
            TextClassificationSequence sequence = new TextClassificationSequence(aJCas,
                    sent.getBegin(), sent.getEnd());
            sequence.addToIndexes();

            // Iterate all tokens in the span of the sentence
            List<Token> tokens = JCasUtil.selectCovered(aJCas, Token.class, sent);
            for (Token token : tokens) {
                // Each token is a classification target, i.e. we want to predict a label for each word in the sentence/sequence
                TextClassificationTarget target = new TextClassificationTarget(aJCas,
                        token.getBegin(), token.getEnd());
                unit.setId(tcId++);
                unit.setSuffix(token.getCoveredText());
                unit.addToIndexes();

                // The outcome annotation defines the `true` value that shall be predicted
                // The outcome shares the same span as the token above to keep annotations aligned.
                TextClassificationOutcome outcome = new TextClassificationOutcome(aJCas,
                        token.getBegin(), token.getEnd());
                outcome.setOutcome(getTextClassificationOutcome(aJCas, target));
                outcome.addToIndexes();
            }

        }
    }

    public String getTextClassificationOutcome(JCas jcas, TextClassificationTarget target)
    {
        // Select the POS annotation in range of the target (the token in this case)
        List<POS> posList = JCasUtil.selectCovered(jcas, POS.class, target);
        return posList.get(0).getPosValue().replaceAll(" ", "_"); // Return this value as expected outcome
    }

}
{% endhighlight %}

To add the `Preprocessing` to your experiment, only a minor modification to your code is necessary:

{% highlight java %} 
ExperimentTrainTest experiment = new ExperimentTrainTest("CrfExperiment");
experiment.setPreprocessing(createEngineDescription(SequenceOutcomeAnnotator.class)); //Here, the preprocessing is added
experiment.setParameterSpace(pSpace);
{% endhighlight %}

`Preprocessing` is not limited to a single component, assuming we would read plain text
