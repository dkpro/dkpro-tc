package org.dkpro.tc.ml.uima;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.io.FileUtils;
import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.fit.factory.ConfigurationParameterFactory;
import org.apache.uima.resource.ExternalResourceDescription;
import org.apache.uima.resource.Resource;
import org.apache.uima.resource.ResourceSpecifier;
import org.apache.uima.resource.impl.CustomResourceSpecifier_impl;
import org.dkpro.tc.api.features.FeatureExtractorResource_ImplBase;
import org.dkpro.tc.api.features.TcFeature;
import org.dkpro.tc.api.features.TcFeatureFactory;
import org.dkpro.tc.api.features.meta.MetaDependent;
import org.dkpro.tc.core.Constants;

public class FeatureResourceLoader implements Constants, PrivilegedAction<Void> {

	private File tcModelLocation;
	URLClassLoader urlClassLoader = null;
	URL url = null;

	public FeatureResourceLoader(File tcModelLocation) throws MalformedURLException {
		this.tcModelLocation = tcModelLocation;

		File classFile = new File(tcModelLocation + "/" + Constants.MODEL_FEATURE_CLASS_FOLDER);
		url = classFile.toURI().toURL();

		AccessController.doPrivileged(this);
	}

	public List<ExternalResourceDescription> loadExternalResourceDescriptionOfFeatures() throws Exception {
		List<ExternalResourceDescription> erd = new ArrayList<>();

		File file = new File(tcModelLocation, MODEL_FEATURE_EXTRACTOR_CONFIGURATION);
		for (String l : FileUtils.readLines(file, "utf-8")) {
			String[] split = l.split("\t");
			String name = split[0];
			Object[] parameters = getParameters(split);

			Class<? extends Resource> feClass = urlClassLoader.loadClass(name).asSubclass(Resource.class);

			List<Object> idRemovedParameters = filterId(parameters);
			String id = getId(parameters);

			idRemovedParameters = addModelPathAsPrefixIfParameterIsExistingFile(idRemovedParameters,
					tcModelLocation.getAbsolutePath());

			TcFeature feature = TcFeatureFactory.create(id, feClass, idRemovedParameters.toArray());
			ExternalResourceDescription exRes = feature.getActualValue();

			// Skip feature extractors that are not dependent on meta collectors
			if (!MetaDependent.class.isAssignableFrom(feClass)) {
				erd.add(exRes);
				continue;
			}

			Map<String, String> overrides = loadOverrides(tcModelLocation, META_COLLECTOR_OVERRIDE);
			configureOverrides(tcModelLocation, exRes, overrides);
			overrides = loadOverrides(tcModelLocation, META_EXTRACTOR_OVERRIDE);
			configureOverrides(tcModelLocation, exRes, overrides);

			erd.add(exRes);
		}

		urlClassLoader.close();

		return erd;
	}

	private String getId(Object[] parameters) {
		for (int i = 0; i < parameters.length; i++) {
			if (parameters[i].toString().equals(FeatureExtractorResource_ImplBase.PARAM_UNIQUE_EXTRACTOR_NAME)) {
				return parameters[i + 1].toString();
			}
		}
		return null;
	}

	private List<Object> addModelPathAsPrefixIfParameterIsExistingFile(List<Object> idRemovedParameters,
			String modelPath) {
		List<Object> out = new ArrayList<>();

		for (int i = 0; i < idRemovedParameters.size(); i++) {
			if (i % 2 == 0) { // those are keys, keys are no surely no file
								// paths
				out.add(idRemovedParameters.get(i));
				continue;
			}
			if (valueExistAsFileOrFolderInTheFileSystem(modelPath + "/" + idRemovedParameters.get(i))) {
				out.add(modelPath + "/" + idRemovedParameters.get(i));
			} else {
				out.add(idRemovedParameters.get(i));
			}
		}

		return out;
	}

	private boolean valueExistAsFileOrFolderInTheFileSystem(String aValue) {
		return new File(aValue).exists();
	}

	private void configureOverrides(File tcModelLocation, ExternalResourceDescription exRes,
			Map<String, String> overrides) throws IOException {
		// We assume for the moment that we only have primitive analysis engines
		// for meta
		// collection, not aggregates. If there were aggregates, we'd have to do
		// this
		// recursively
		ResourceSpecifier aDesc = exRes.getResourceSpecifier();
		if (aDesc instanceof AnalysisEngineDescription) {
			// Analysis engines are ok
			if (!((AnalysisEngineDescription) aDesc).isPrimitive()) {
				throw new IllegalArgumentException("Only primitive meta collectors currently supported.");
			}
		} else if (aDesc instanceof CustomResourceSpecifier_impl) {
			// Feature extractors are ok
		} else {
			throw new IllegalArgumentException("Descriptors of type " + aDesc.getClass() + " not supported.");
		}

		for (Entry<String, String> e : overrides.entrySet()) {
			// We generate a storage location from the feature extractor
			// discriminator value
			// and the preferred value specified by the meta collector
			String parameterName = e.getKey();
			ConfigurationParameterFactory.setParameter(aDesc, parameterName,
					new File(tcModelLocation, e.getValue()).getAbsolutePath());

		}
	}

	private Map<String, String> loadOverrides(File tcModelLocation, String overrideFile) throws IOException {
		List<String> lines = FileUtils.readLines(new File(tcModelLocation, overrideFile), "utf-8");
		Map<String, String> overrides = new HashMap<>();

		for (String s : lines) {
			String[] split = s.split("=");
			overrides.put(split[0], split[1]);
		}

		return overrides;
	}

	private List<Object> filterId(Object[] parameters) {
		List<Object> out = new ArrayList<>();
		for (int i = 0; i < parameters.length; i++) {
			if (parameters[i].toString().equals(FeatureExtractorResource_ImplBase.PARAM_UNIQUE_EXTRACTOR_NAME)) {
				i++;
				continue;
			}
			out.add(parameters[i]);
		}

		return out;
	}

	private Object[] getParameters(String[] split) {
		List<Object> p = new ArrayList<>();
		for (int i = 1; i < split.length; i++) {
			String string = split[i];
			int indexOf = string.indexOf("=");
			String paramName = string.substring(0, indexOf);
			String paramVal = string.substring(indexOf + 1);
			p.add(paramName);
			p.add(paramVal);
		}

		return p.toArray();
	}

	/*
	 * Depending on security settings; class loading instantiation might require
	 * special privileges
	 */
	@Override
	public Void run() {

		urlClassLoader = new URLClassLoader(new URL[] { url }, Thread.currentThread().getContextClassLoader());

		return null;
	}
}
