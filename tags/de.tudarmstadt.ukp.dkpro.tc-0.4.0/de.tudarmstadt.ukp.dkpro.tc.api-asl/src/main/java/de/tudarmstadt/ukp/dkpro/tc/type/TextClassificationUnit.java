

/* First created by JCasGen Wed Aug 28 16:30:21 CEST 2013 */
package de.tudarmstadt.ukp.dkpro.tc.type;

import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.JCasRegistry;
import org.apache.uima.jcas.cas.TOP_Type;
import org.apache.uima.jcas.tcas.Annotation;


/** 
 * Updated by JCasGen Fri Aug 30 15:54:36 CEST 2013
 * XML source: /Users/a_vovk/UKP/vovk/workspace/de.tudarmstadt.ukp.dkpro.tc/de.tudarmstadt.ukp.dkpro.tc.api-asl/src/main/resources/desc/type/TextClassification.xml
 * @generated */
public class TextClassificationUnit extends Annotation {
  /** @generated
   * @ordered 
   */
  @SuppressWarnings ("hiding")
  public final static int typeIndexID = JCasRegistry.register(TextClassificationUnit.class);
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
  protected TextClassificationUnit() {/* intentionally empty block */}
    
  /** Internal - constructor used by generator 
   * @generated */
  public TextClassificationUnit(int addr, TOP_Type type) {
    super(addr, type);
    readObject();
  }
  
  /** @generated */
  public TextClassificationUnit(JCas jcas) {
    super(jcas);
    readObject();   
  } 

  /** @generated */  
  public TextClassificationUnit(JCas jcas, int begin, int end) {
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
     
}

    