/*******************************************************************************
 * Copyright 2016
 * Ubiquitous Knowledge Processing (UKP) Lab
 * Technische Universität Darmstadt
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
package org.dkpro.tc.core.io;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.uima.collection.CollectionReaderDescription;
import org.apache.uima.resource.metadata.ConfigurationParameterSettings;
import org.apache.uima.resource.metadata.NameValuePair;
import org.apache.uima.resource.metadata.ResourceMetaData;
import org.dkpro.lab.task.Discriminable;

/**
 * This class is called from within the @see ord.dkpro.tc.core.task.InitTask class to let the
 * CollectionReaderDescription implement the Discriminable interface by using a dynamic proxy.
 *
 */
public class ReaderInvocationHandler
    implements InvocationHandler, Discriminable
{

    private CollectionReaderDescription crd;

    public ReaderInvocationHandler(Object crd)
    {
        this.crd = (CollectionReaderDescription) crd;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args)
        throws Throwable
    {

        if (method.getName().equals("getDiscriminatorValue")) {
            return getDiscriminatorValue();
        }
        if (method.getName().equals("getActualValue")) {
            return getActualValue();
        }

        return method.invoke(crd, args);
    }

    @Override
    public Object getDiscriminatorValue()
    {
        ResourceMetaData metaData = crd.getMetaData();
        ConfigurationParameterSettings configurationParameterSettings = metaData
                .getConfigurationParameterSettings();
        NameValuePair[] parameterSettings = configurationParameterSettings.getParameterSettings();

        String implementationName = crd.getImplementationName();

        List<String> entries = new ArrayList<>();
        entries.add(implementationName);
        entries = addParameters(entries, parameterSettings);

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < entries.size(); i++) {
            sb.append(entries.get(i));
            if (i + 1 < entries.size()) {
                sb.append(", ");
            }
        }

        String description = "MD5: " + DigestUtils.md5Hex(sb.toString()) + ", " + sb.toString();
        return description;
    }

    private List<String> addParameters(List<String> entries, NameValuePair[] parameterSettings)
    {
        for (int i = 0; i < parameterSettings.length; i++) {
            NameValuePair nvp = parameterSettings[i];
            Object value = nvp.getValue();
            StringBuilder sb = new StringBuilder();
            if (value instanceof Object[]) {
                Object[] x = (Object[]) value;
                for (int k = 0; k < x.length; k++) {
                    Object object = x[k];
                    sb.append(object);
                    if (k + 1 < x.length) {
                        sb.append(", ");
                    }
                }
                entries.add(nvp.getName() + "=" + sb.toString());
            }
            else {
                entries.add(nvp.getName() + "=" + nvp.getValue());
            }
        }

        return entries;
    }

    @Override
    public Object getActualValue()
    {
        return getDiscriminatorValue();
    }

}
