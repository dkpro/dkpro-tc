package org.dkpro.tc.ml.vowpalwabbit;

import java.io.File;

import de.tudarmstadt.ukp.dkpro.core.api.resources.RuntimeProvider;

public class TestDummy {
	
	private static final String classpath = "classpath:/org/dkpro/tc/ml/vowpalwabbit/";
	static RuntimeProvider runtimeProvider = null;
	public static void main(String[] args) throws Exception {
		runtimeProvider = new RuntimeProvider(classpath);
		File file = runtimeProvider.getFile("vw");
		
		System.out.println(file.getAbsolutePath());
	}

}
