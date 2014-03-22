
/* First created by JCasGen Tue Nov 06 11:09:24 CET 2012 */
package de.tudarmstadt.ukp.dkpro.tc.type;

import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.JCasRegistry;
import org.apache.uima.cas.impl.CASImpl;
import org.apache.uima.cas.impl.FSGenerator;
import org.apache.uima.cas.FeatureStructure;
import org.apache.uima.cas.impl.TypeImpl;
import org.apache.uima.cas.Type;
import org.apache.uima.cas.impl.FeatureImpl;
import org.apache.uima.cas.Feature;
import org.apache.uima.jcas.tcas.Annotation_Type;

/** 
 * Updated by JCasGen Sat Mar 22 12:19:05 CET 2014
 * @generated */
public class TextClassificationOutcome_Type extends Annotation_Type {
  /** @generated */
  @Override
  protected FSGenerator getFSGenerator() {return fsGenerator;}
  /** @generated */
  private final FSGenerator fsGenerator = 
    new FSGenerator() {
      public FeatureStructure createFS(int addr, CASImpl cas) {
  			 if (TextClassificationOutcome_Type.this.useExistingInstance) {
  			   // Return eq fs instance if already created
  		     FeatureStructure fs = TextClassificationOutcome_Type.this.jcas.getJfsFromCaddr(addr);
  		     if (null == fs) {
  		       fs = new TextClassificationOutcome(addr, TextClassificationOutcome_Type.this);
  			   TextClassificationOutcome_Type.this.jcas.putJfsFromCaddr(addr, fs);
  			   return fs;
  		     }
  		     return fs;
        } else return new TextClassificationOutcome(addr, TextClassificationOutcome_Type.this);
  	  }
    };
  /** @generated */
  @SuppressWarnings ("hiding")
  public final static int typeIndexID = TextClassificationOutcome.typeIndexID;
  /** @generated 
     @modifiable */
  @SuppressWarnings ("hiding")
  public final static boolean featOkTst = JCasRegistry.getFeatOkTst("de.tudarmstadt.ukp.dkpro.tc.type.TextClassificationOutcome");
 
  /** @generated */
  final Feature casFeat_outcome;
  /** @generated */
  final int     casFeatCode_outcome;
  /** @generated */ 
  public String getOutcome(int addr) {
        if (featOkTst && casFeat_outcome == null)
      jcas.throwFeatMissing("outcome", "de.tudarmstadt.ukp.dkpro.tc.type.TextClassificationOutcome");
    return ll_cas.ll_getStringValue(addr, casFeatCode_outcome);
  }
  /** @generated */    
  public void setOutcome(int addr, String v) {
        if (featOkTst && casFeat_outcome == null)
      jcas.throwFeatMissing("outcome", "de.tudarmstadt.ukp.dkpro.tc.type.TextClassificationOutcome");
    ll_cas.ll_setStringValue(addr, casFeatCode_outcome, v);}
    
  
 
  /** @generated */
  final Feature casFeat_prediction;
  /** @generated */
  final int     casFeatCode_prediction;
  /** @generated */ 
  public String getPrediction(int addr) {
        if (featOkTst && casFeat_prediction == null)
      jcas.throwFeatMissing("prediction", "de.tudarmstadt.ukp.dkpro.tc.type.TextClassificationOutcome");
    return ll_cas.ll_getStringValue(addr, casFeatCode_prediction);
  }
  /** @generated */    
  public void setPrediction(int addr, String v) {
        if (featOkTst && casFeat_prediction == null)
      jcas.throwFeatMissing("prediction", "de.tudarmstadt.ukp.dkpro.tc.type.TextClassificationOutcome");
    ll_cas.ll_setStringValue(addr, casFeatCode_prediction, v);}
    
  
 
  /** @generated */
  final Feature casFeat_confidence;
  /** @generated */
  final int     casFeatCode_confidence;
  /** @generated */ 
  public double getConfidence(int addr) {
        if (featOkTst && casFeat_confidence == null)
      jcas.throwFeatMissing("confidence", "de.tudarmstadt.ukp.dkpro.tc.type.TextClassificationOutcome");
    return ll_cas.ll_getDoubleValue(addr, casFeatCode_confidence);
  }
  /** @generated */    
  public void setConfidence(int addr, double v) {
        if (featOkTst && casFeat_confidence == null)
      jcas.throwFeatMissing("confidence", "de.tudarmstadt.ukp.dkpro.tc.type.TextClassificationOutcome");
    ll_cas.ll_setDoubleValue(addr, casFeatCode_confidence, v);}
    
  



  /** initialize variables to correspond with Cas Type and Features
	* @generated */
  public TextClassificationOutcome_Type(JCas jcas, Type casType) {
    super(jcas, casType);
    casImpl.getFSClassRegistry().addGeneratorForType((TypeImpl)this.casType, getFSGenerator());

 
    casFeat_outcome = jcas.getRequiredFeatureDE(casType, "outcome", "uima.cas.String", featOkTst);
    casFeatCode_outcome  = (null == casFeat_outcome) ? JCas.INVALID_FEATURE_CODE : ((FeatureImpl)casFeat_outcome).getCode();

 
    casFeat_prediction = jcas.getRequiredFeatureDE(casType, "prediction", "uima.cas.String", featOkTst);
    casFeatCode_prediction  = (null == casFeat_prediction) ? JCas.INVALID_FEATURE_CODE : ((FeatureImpl)casFeat_prediction).getCode();

 
    casFeat_confidence = jcas.getRequiredFeatureDE(casType, "confidence", "uima.cas.Double", featOkTst);
    casFeatCode_confidence  = (null == casFeat_confidence) ? JCas.INVALID_FEATURE_CODE : ((FeatureImpl)casFeat_confidence).getCode();

  }
}



    