

/* First created by JCasGen Sat Mar 22 12:19:05 CET 2014 */
package de.tudarmstadt.ukp.dkpro.tc.type;

import org.apache.uima.jcas.JCas; 
import org.apache.uima.jcas.JCasRegistry;
import org.apache.uima.jcas.cas.TOP_Type;

import org.apache.uima.jcas.tcas.Annotation;


/** 
 * Updated by JCasGen Sat Mar 22 12:19:05 CET 2014
 * XML source: /home/zesch/workspace_new/de.tudarmstadt.ukp.dkpro.tc/de.tudarmstadt.ukp.dkpro.tc.api-asl/src/main/resources/desc/type/TextClassification.xml
 * @generated */
public class TextClassificationFocus extends Annotation {
  /** @generated
   * @ordered 
   */
  @SuppressWarnings ("hiding")
  public final static int typeIndexID = JCasRegistry.register(TextClassificationFocus.class);
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
  protected TextClassificationFocus() {/* intentionally empty block */}
    
  /** Internal - constructor used by generator 
   * @generated */
  public TextClassificationFocus(int addr, TOP_Type type) {
    super(addr, type);
    readObject();
  }
  
  /** @generated */
  public TextClassificationFocus(JCas jcas) {
    super(jcas);
    readObject();   
  } 

  /** @generated */  
  public TextClassificationFocus(JCas jcas, int begin, int end) {
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

    