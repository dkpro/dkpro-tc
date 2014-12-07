/*******************************************************************************
 * Copyright 2014
 * Ubiquitous Knowledge Processing (UKP) Lab
 * Technische Universit??t Darmstadt
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/

/* First created by JCasGen Tue May 06 17:51:57 CEST 2014 */
package de.tudarmstadt.ukp.dkpro.tc.api.type;

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
 * Updated by JCasGen Wed Nov 05 14:39:52 CET 2014
 * @generated */
public class TextClassificationUnit_Type extends Annotation_Type {
  /** @generated 
   * @return the generator for this type
   */
  @Override
  protected FSGenerator getFSGenerator() {return fsGenerator;}
  /** @generated */
  private final FSGenerator fsGenerator = 
    new FSGenerator() {
      public FeatureStructure createFS(int addr, CASImpl cas) {
  			 if (TextClassificationUnit_Type.this.useExistingInstance) {
  			   // Return eq fs instance if already created
  		     FeatureStructure fs = TextClassificationUnit_Type.this.jcas.getJfsFromCaddr(addr);
  		     if (null == fs) {
  		       fs = new TextClassificationUnit(addr, TextClassificationUnit_Type.this);
  			   TextClassificationUnit_Type.this.jcas.putJfsFromCaddr(addr, fs);
  			   return fs;
  		     }
  		     return fs;
        } else return new TextClassificationUnit(addr, TextClassificationUnit_Type.this);
  	  }
    };
  /** @generated */
  @SuppressWarnings ("hiding")
  public final static int typeIndexID = TextClassificationUnit.typeIndexID;
  /** @generated 
     @modifiable */
  @SuppressWarnings ("hiding")
  public final static boolean featOkTst = JCasRegistry.getFeatOkTst("de.tudarmstadt.ukp.dkpro.tc.api.type.TextClassificationUnit");



  /** @generated */
  final Feature casFeat_suffix;
  /** @generated */
  final int     casFeatCode_suffix;
  /** @generated
   * @param addr low level Feature Structure reference
   * @return the feature value 
   */ 
  public String getSuffix(int addr) {
        if (featOkTst && casFeat_suffix == null)
      jcas.throwFeatMissing("suffix", "de.tudarmstadt.ukp.dkpro.tc.api.type.TextClassificationUnit");
    return ll_cas.ll_getStringValue(addr, casFeatCode_suffix);
  }
  /** @generated
   * @param addr low level Feature Structure reference
   * @param v value to set 
   */    
  public void setSuffix(int addr, String v) {
        if (featOkTst && casFeat_suffix == null)
      jcas.throwFeatMissing("suffix", "de.tudarmstadt.ukp.dkpro.tc.api.type.TextClassificationUnit");
    ll_cas.ll_setStringValue(addr, casFeatCode_suffix, v);}
    
  
 
  /** @generated */
  final Feature casFeat_id;
  /** @generated */
  final int     casFeatCode_id;
  /** @generated
   * @param addr low level Feature Structure reference
   * @return the feature value 
   */ 
  public int getId(int addr) {
        if (featOkTst && casFeat_id == null)
      jcas.throwFeatMissing("id", "de.tudarmstadt.ukp.dkpro.tc.api.type.TextClassificationUnit");
    return ll_cas.ll_getIntValue(addr, casFeatCode_id);
  }
  /** @generated
   * @param addr low level Feature Structure reference
   * @param v value to set 
   */    
  public void setId(int addr, int v) {
        if (featOkTst && casFeat_id == null)
      jcas.throwFeatMissing("id", "de.tudarmstadt.ukp.dkpro.tc.api.type.TextClassificationUnit");
    ll_cas.ll_setIntValue(addr, casFeatCode_id, v);}
    
  



  /** initialize variables to correspond with Cas Type and Features
	 * @generated
	 * @param jcas JCas
	 * @param casType Type 
	 */
  public TextClassificationUnit_Type(JCas jcas, Type casType) {
    super(jcas, casType);
    casImpl.getFSClassRegistry().addGeneratorForType((TypeImpl)this.casType, getFSGenerator());

 
    casFeat_suffix = jcas.getRequiredFeatureDE(casType, "suffix", "uima.cas.String", featOkTst);
    casFeatCode_suffix  = (null == casFeat_suffix) ? JCas.INVALID_FEATURE_CODE : ((FeatureImpl)casFeat_suffix).getCode();

 
    casFeat_id = jcas.getRequiredFeatureDE(casType, "id", "uima.cas.Integer", featOkTst);
    casFeatCode_id  = (null == casFeat_id) ? JCas.INVALID_FEATURE_CODE : ((FeatureImpl)casFeat_id).getCode();

  }
}



    