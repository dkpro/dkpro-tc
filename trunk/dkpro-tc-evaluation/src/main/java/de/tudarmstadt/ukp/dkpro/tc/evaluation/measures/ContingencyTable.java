package de.tudarmstadt.ukp.dkpro.tc.evaluation.measures;

public class ContingencyTable {

	private double[][][] table;
	
	public ContingencyTable(int numberOfClasses) {
		this.table = new double[numberOfClasses][2][2];
	}
	
	public int getSize() {
		return table.length;
	}
	
	public void addTruePositives(int classId, double count) {
		table[classId][0][0] += count;		
	}

	public void addTrueNegatives(int classId, double count) {
		table[classId][1][1] += count;
	}

	public void addFalsePositives(int classId, double count) {
		table[classId][0][1] += count;
	}

	public void addFalseNegatives(int classId, double count) {
		table[classId][1][0] += count;		
	}
	
	public double getTruePositives(int classId) {
		return table[classId][0][0];
	}

	public double getTrueNegatives(int classId) {
		return table[classId][1][1];
	}

	public double getFalsePositives(int classId) {
		return table[classId][1][0];		
	}

	public double getFalseNegatives(int classId) {
		return table[classId][0][1];
	}
}