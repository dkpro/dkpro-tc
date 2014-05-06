

/* First created by JCasGen Tue May 06 17:51:57 CEST 2014 */
package de.tudarmstadt.ukp.dkpro.tc.api.type;

import org.apache.uima.jcas.JCas; 
import org.apache.uima.jcas.JCasRegistry;
import org.apache.uima.jcas.cas.TOP_Type;

import org.apache.uima.jcas.tcas.Annotation;


/** 
 * Updated by JCasGen Tue May 06 17:51:57 CEST 2014
 * XML source: /Users/zesch/Documents/workspace/de.tudarmstadt.ukp.dkpro.tc/de.tudarmstadt.ukp.dkpro.tc.api-asl/src/main/resources/desc/type/TextClassification.xml
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
  /** @generated
   * @return index of the type  
   */
  @Override
  public              int getTypeIndexID() {return typeIndexID;}
 
  /** Never called.  Disable default constructor
   * @generated */
  protected TextClassificationOutcome() {/* intentionally empty block */}
    
  /** Internal - constructor used by generator 
   * @generated
   * @param addr low level Feature Structure reference
   * @param type the type of this Feature Structure 
   */
  public TextClassificationOutcome(int addr, TOP_Type type) {
    super(addr, type);
    readObject();
  }
  
  /** @generated
   * @param jcas JCas to which this Feature Structure belongs 
   */
  public TextClassificationOutcome(JCas jcas) {
    super(jcas);
    readObject();   
  } 

  /** @generated
   * @param jcas JCas to which this Feature Structure belongs
   * @param begin offset to the begin spot in the SofA
   * @param end offset to the end spot in the SofA 
  */  
  public TextClassificationOutcome(JCas jcas, int begin, int end) {
    super(jcas);
    setBegin(begin);
    setEnd(end);
    readObject();
  }   

  /** 
   * <!-- begin-user-doc -->
   * Write your own initialization here
   * <!-- end-user-doc -->
   *
   * @generated modifiable 
   */
  private void readObject() {/*default - does nothing empty block */}
     
 
    
  //*--------------*
  //* Feature: outcome

  /** getter for outcome - gets 
   * @generated
   * @return value of the feature 
   */
  public String getOutcome() {
    if (TextClassificationOutcome_Type.featOkTst && ((TextClassificationOutcome_Type)jcasType).casFeat_outcome == null)
      jcasType.jcas.throwFeatMissing("outcome", "de.tudarmstadt.ukp.dkpro.tc.api.type.TextClassificationOutcome");
    return jcasType.ll_cas.ll_getStringValue(addr, ((TextClassificationOutcome_Type)jcasType).casFeatCode_outcome);}
    
  /** setter for outcome - sets  
   * @generated
   * @param v value to set into the feature 
   */
  public void setOutcome(String v) {
    if (TextClassificationOutcome_Type.featOkTst && ((TextClassificationOutcome_Type)jcasType).casFeat_outcome == null)
      jcasType.jcas.throwFeatMissing("outcome", "de.tudarmstadt.ukp.dkpro.tc.api.type.TextClassificationOutcome");
    jcasType.ll_cas.ll_setStringValue(addr, ((TextClassificationOutcome_Type)jcasType).casFeatCode_outcome, v);}    
   
    
  //*--------------*
  //* Feature: prediction

  /** getter for prediction - gets 
   * @generated
   * @return value of the feature 
   */
  public String getPrediction() {
    if (TextClassificationOutcome_Type.featOkTst && ((TextClassificationOutcome_Type)jcasType).casFeat_prediction == null)
      jcasType.jcas.throwFeatMissing("prediction", "de.tudarmstadt.ukp.dkpro.tc.api.type.TextClassificationOutcome");
    return jcasType.ll_cas.ll_getStringValue(addr, ((TextClassificationOutcome_Type)jcasType).casFeatCode_prediction);}
    
  /** setter for prediction - sets  
   * @generated
   * @param v value to set into the feature 
   */
  public void setPrediction(String v) {
    if (TextClassificationOutcome_Type.featOkTst && ((TextClassificationOutcome_Type)jcasType).casFeat_prediction == null)
      jcasType.jcas.throwFeatMissing("prediction", "de.tudarmstadt.ukp.dkpro.tc.api.type.TextClassificationOutcome");
    jcasType.ll_cas.ll_setStringValue(addr, ((TextClassificationOutcome_Type)jcasType).casFeatCode_prediction, v);}    
   
    
  //*--------------*
  //* Feature: confidence

  /** getter for confidence - gets The confidence for the prediction
   * @generated
   * @return value of the feature 
   */
  public double getConfidence() {
    if (TextClassificationOutcome_Type.featOkTst && ((TextClassificationOutcome_Type)jcasType).casFeat_confidence == null)
      jcasType.jcas.throwFeatMissing("confidence", "de.tudarmstadt.ukp.dkpro.tc.api.type.TextClassificationOutcome");
    return jcasType.ll_cas.ll_getDoubleValue(addr, ((TextClassificationOutcome_Type)jcasType).casFeatCode_confidence);}
    
  /** setter for confidence - sets The confidence for the prediction 
   * @generated
   * @param v value to set into the feature 
   */
  public void setConfidence(double v) {
    if (TextClassificationOutcome_Type.featOkTst && ((TextClassificationOutcome_Type)jcasType).casFeat_confidence == null)
      jcasType.jcas.throwFeatMissing("confidence", "de.tudarmstadt.ukp.dkpro.tc.api.type.TextClassificationOutcome");
    jcasType.ll_cas.ll_setDoubleValue(addr, ((TextClassificationOutcome_Type)jcasType).casFeatCode_confidence, v);}    
  }

    