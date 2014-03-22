

/* First created by JCasGen Tue Nov 06 11:09:24 CET 2012 */
package de.tudarmstadt.ukp.dkpro.tc.type;

import org.apache.uima.jcas.JCas; 
import org.apache.uima.jcas.JCasRegistry;
import org.apache.uima.jcas.cas.TOP_Type;

import org.apache.uima.jcas.tcas.Annotation;


/** 
 * Updated by JCasGen Sat Mar 22 12:19:05 CET 2014
 * XML source: /home/zesch/workspace_new/de.tudarmstadt.ukp.dkpro.tc/de.tudarmstadt.ukp.dkpro.tc.api-asl/src/main/resources/desc/type/TextClassification.xml
 * @generated */
public class TextClassificationOutcome extends Annotation {
  /** @generated
   * @ordered 
   */
  @SuppressWarnings ("hiding")
  public final static int typeIndexID = JCasRegistry.register(TextClassificationOutcome.class);
  /** @generated
   * @ordered 
   */
  @SuppressWarnings ("hiding")
  public final static int type = typeIndexID;
  /** @generated  */
  @Override
  public              int getTypeIndexID() {return typeIndexID;}
 
  /** Never called.  Disable default constructor
   * @generated */
  protected TextClassificationOutcome() {/* intentionally empty block */}
    
  /** Internal - constructor used by generator 
   * @generated */
  public TextClassificationOutcome(int addr, TOP_Type type) {
    super(addr, type);
    readObject();
  }
  
  /** @generated */
  public TextClassificationOutcome(JCas jcas) {
    super(jcas);
    readObject();   
  } 

  /** @generated */  
  public TextClassificationOutcome(JCas jcas, int begin, int end) {
    super(jcas);
    setBegin(begin);
    setEnd(end);
    readObject();
  }   

  /** <!-- begin-user-doc -->
    * Write your own initialization here
    * <!-- end-user-doc -->
  @generated modifiable */
  private void readObject() {/*default - does nothing empty block */}
     
 
    
  //*--------------*
  //* Feature: outcome

  /** getter for outcome - gets 
   * @generated */
  public String getOutcome() {
    if (TextClassificationOutcome_Type.featOkTst && ((TextClassificationOutcome_Type)jcasType).casFeat_outcome == null)
      jcasType.jcas.throwFeatMissing("outcome", "de.tudarmstadt.ukp.dkpro.tc.type.TextClassificationOutcome");
    return jcasType.ll_cas.ll_getStringValue(addr, ((TextClassificationOutcome_Type)jcasType).casFeatCode_outcome);}
    
  /** setter for outcome - sets  
   * @generated */
  public void setOutcome(String v) {
    if (TextClassificationOutcome_Type.featOkTst && ((TextClassificationOutcome_Type)jcasType).casFeat_outcome == null)
      jcasType.jcas.throwFeatMissing("outcome", "de.tudarmstadt.ukp.dkpro.tc.type.TextClassificationOutcome");
    jcasType.ll_cas.ll_setStringValue(addr, ((TextClassificationOutcome_Type)jcasType).casFeatCode_outcome, v);}    
   
    
  //*--------------*
  //* Feature: prediction

  /** getter for prediction - gets 
   * @generated */
  public String getPrediction() {
    if (TextClassificationOutcome_Type.featOkTst && ((TextClassificationOutcome_Type)jcasType).casFeat_prediction == null)
      jcasType.jcas.throwFeatMissing("prediction", "de.tudarmstadt.ukp.dkpro.tc.type.TextClassificationOutcome");
    return jcasType.ll_cas.ll_getStringValue(addr, ((TextClassificationOutcome_Type)jcasType).casFeatCode_prediction);}
    
  /** setter for prediction - sets  
   * @generated */
  public void setPrediction(String v) {
    if (TextClassificationOutcome_Type.featOkTst && ((TextClassificationOutcome_Type)jcasType).casFeat_prediction == null)
      jcasType.jcas.throwFeatMissing("prediction", "de.tudarmstadt.ukp.dkpro.tc.type.TextClassificationOutcome");
    jcasType.ll_cas.ll_setStringValue(addr, ((TextClassificationOutcome_Type)jcasType).casFeatCode_prediction, v);}    
   
    
  //*--------------*
  //* Feature: confidence

  /** getter for confidence - gets The confidence for the outcome
   * @generated */
  public double getConfidence() {
    if (TextClassificationOutcome_Type.featOkTst && ((TextClassificationOutcome_Type)jcasType).casFeat_confidence == null)
      jcasType.jcas.throwFeatMissing("confidence", "de.tudarmstadt.ukp.dkpro.tc.type.TextClassificationOutcome");
    return jcasType.ll_cas.ll_getDoubleValue(addr, ((TextClassificationOutcome_Type)jcasType).casFeatCode_confidence);}
    
  /** setter for confidence - sets The confidence for the outcome 
   * @generated */
  public void setConfidence(double v) {
    if (TextClassificationOutcome_Type.featOkTst && ((TextClassificationOutcome_Type)jcasType).casFeat_confidence == null)
      jcasType.jcas.throwFeatMissing("confidence", "de.tudarmstadt.ukp.dkpro.tc.type.TextClassificationOutcome");
    jcasType.ll_cas.ll_setDoubleValue(addr, ((TextClassificationOutcome_Type)jcasType).casFeatCode_confidence, v);}    
  }

    