package de.tudarmstadt.ukp.dkpro.tc.mallet.api;

import de.tudarmstadt.ukp.dkpro.tc.api.features.Instance;

/**
 * Internal representation of a Mallet instance. Extends TC Instance, and has an additional field for
 * storing the instance id since Mallet should differentiate between instances belonging to the same
 * instance sequence
 * 
 * @author zesch
 * @author perumal
 *
 */
public class MalletInstance
extends Instance
{
    private String instanceSequenceId;
    private int instancePosition;
    
    public void setInstanceSequenceId(String instanceId) {
    	this.instanceSequenceId = instanceId;
    }
    
    public String getInstanceSequenceId() {
    	return this.instanceSequenceId;
    }
    
    public void setInstancePosition(int instancePosition) {
    	this.instancePosition = instancePosition;
    }
    
    public int getInstancePosition() {
    	return this.instancePosition;
    }
}