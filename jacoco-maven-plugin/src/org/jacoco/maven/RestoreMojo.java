/*******************************************************************************
 * Copyright (c) 2009, 2014 Mountainminds GmbH & Co. KG and Contributors
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Evgeny Mandrikov - initial API and implementation
 *
 *******************************************************************************/
package org.jacoco.maven;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.codehaus.plexus.util.FileUtils;
import org.jacoco.maven.util.FileUtil;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.Set;

/**
 * Restores original classes as they were before offline instrumentation.
 * 
 * @phase prepare-package
 * @goal restore-instrumented-classes
 * @requiresProject true
 * @threadSafe
 * @since 0.6.2
 */
public class RestoreMojo extends AbstractJacocoMojo {

	@Override
	protected void executeMojo() throws MojoExecutionException,
			MojoFailureException {
		final File originalClassesDir = new File(getProject().getBuild()
				.getDirectory(), "generated-classes/jacoco");
		final File classesDir = new File(getProject().getBuild()
				.getOutputDirectory());
		try {
			FileUtils.copyDirectoryStructure(originalClassesDir, classesDir);
		} catch (final IOException e) {
			throw new MojoFailureException("Unable to restore classes.", e);
		}

        /**
         * Delete dependency classes if project is multi-module and package patter is not null
         */
        if(isMultiModuleProject() && (getPackagePattern() != null))
        {
            Set artifacts = getProject().getDependencyArtifacts();
            for (Iterator artifactIterator = artifacts.iterator(); artifactIterator.hasNext();) {
                Artifact artifact = (Artifact) artifactIterator.next();
                if(artifact.getGroupId().contains(getPackagePattern()))
                {
                    getLog().info("Found dependency: "+artifact.getArtifactId());
                    String dependencyJarPath = getDependencyJarPath(artifact);
                    if(dependencyJarPath != null) {
                        FileUtil.deleteDependencyFile(getProject(), dependencyJarPath);
                    }
                }
            }
        }
	}

}
