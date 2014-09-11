package de.tudarmstadt.ukp.dkpro.tc.crfsuite;
/*******************************************************************************
 * Copyright 2014
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
import java.io.File;
import java.net.URL;

import org.apache.commons.logging.LogFactory;
import org.apache.uima.resource.ResourceInitializationException;

import de.tudarmstadt.ukp.dkpro.core.api.resources.PlatformDetector;
import de.tudarmstadt.ukp.dkpro.core.api.resources.ResourceUtils;

public class BinaryLoader
{
    public File loadCRFBinary()
        throws Exception
    {
        PlatformDetector pd = new PlatformDetector();
        try {
            String platform = pd.getPlatformId();
            LogFactory.getLog(getClass()).warn("Your platform is " + platform);
            if (isLinux32Bit(platform)) {
                return loadBinary(platform, pd.getExecutableSuffix());
            }
            else if (isLinux64Bit(platform)) {
                return loadBinary(platform, pd.getExecutableSuffix());
            }
            else if (isOSX64Bit(platform)) {
                return loadBinary(platform, pd.getExecutableSuffix());
            }
            else if (isWindows64Bit(platform)) {
                return loadBinary(platform, pd.getExecutableSuffix());
            }
            // Windows 64bit should be abel to handle 32bit
            else if (isWindows32Bit(platform) || isWindows64Bit(platform)) {
                return loadBinary(platform, pd.getExecutableSuffix());
            }

            else {
                throw new ResourceInitializationException(new Throwable("CRFSuite native code for "
                        + platform + " is not supported"));
            }
        }
        catch (UnsatisfiedLinkError e) {
            LogFactory.getLog(getClass())
                    .warn("Cannot load the CRFSuit binaries for your platform");
            throw new ResourceInitializationException(e);

        }
    }

    private boolean isWindows32Bit(String aPlatform)
    {
        return isPlatform(aPlatform, PlatformDetector.OS_WINDOWS, PlatformDetector.ARCH_X86_32);
    }

    private boolean isWindows64Bit(String aPlatform)
    {
        return isPlatform(aPlatform, PlatformDetector.OS_WINDOWS, PlatformDetector.ARCH_X86_64);
    }

    private boolean isOSX64Bit(String aPlatform)
    {
        return isPlatform(aPlatform, PlatformDetector.OS_OSX, PlatformDetector.ARCH_X86_64);
    }

    private boolean isLinux64Bit(String aPlatform)
    {
        return isPlatform(aPlatform, PlatformDetector.OS_LINUX, PlatformDetector.ARCH_X86_64);
    }

    private boolean isLinux32Bit(String aPlatform)
    {
        return isPlatform(aPlatform, PlatformDetector.OS_LINUX, PlatformDetector.ARCH_X86_32);
    }

    private boolean isPlatform(String aPlatform, String operatingSystem, String architecture)
    {
        return aPlatform.equalsIgnoreCase(operatingSystem + "-" + architecture);
    }

    private File loadBinary(String aPlatform, String execSuffix)
        throws Exception
    {
        String fileName = "crfsuite";
        String prefix = "/" + aPlatform + "/" + "bin" + "/";
        String packagePrefix = getClass().getPackage().getName().replaceAll("\\.", "/");

        String loc = packagePrefix + prefix + fileName + appendSuffix(execSuffix);
        URL resource = getClass().getResource("/" + loc);
        File executable = ResourceUtils.getUrlAsExecutable(resource, true);
        
        Process chmod = Runtime.getRuntime().exec("chmod +x " + executable.getAbsolutePath());
        chmod.waitFor();

        return executable;
    }

    private String appendSuffix(String aExecSuffix)
    {
        return (aExecSuffix.isEmpty() ? "" : "." + aExecSuffix);
    }

}
