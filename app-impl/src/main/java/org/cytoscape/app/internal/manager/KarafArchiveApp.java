package org.cytoscape.app.internal.manager;

/*
 * #%L
 * Cytoscape App Impl (app-impl)
 * $Id:$
 * $HeadURL:$
 * %%
 * Copyright (C) 2008 - 2013 The Cytoscape Consortium
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as 
 * published by the Free Software Foundation, either version 2.1 of the 
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Lesser Public License for more details.
 * 
 * You should have received a copy of the GNU General Lesser Public 
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/lgpl-2.1.html>.
 * #L%
 */

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.io.FileExistsException;
import org.apache.commons.io.FileUtils;
import org.cytoscape.app.internal.exception.AppDisableException;
import org.cytoscape.app.internal.exception.AppInstallException;
import org.cytoscape.app.internal.exception.AppInstanceException;
import org.cytoscape.app.internal.exception.AppUninstallException;
import org.cytoscape.app.swing.CySwingAppAdapter;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleException;

public class KarafArchiveApp extends App {	
	
	/**
	 * Karaf feature information used to install/uninstall a bundle app, 
	 * which consists of Karaf features.
	 */
	
	public static class KarafFeature {
		public String featureName;
		public String featureVersion;
	}
	
	private Map<String, KarafFeature> featuresSet;
	
	public KarafArchiveApp() {
		super();
		
		this.featuresSet = new HashMap<String, KarafFeature>();
	}
	
	
	
	@Override
	public Object createAppInstance(CySwingAppAdapter appAdapter)
			throws AppInstanceException {
		
		BundleContext bundleContext = null;
		Bundle bundle = null;
		try {
			bundle = bundleContext.installBundle(this.getAppTemporaryInstallFile().toURI().toURL().toString());
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (BundleException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return bundle;
	}

	
	@Override
	public void install(AppManager appManager) throws AppInstallException {
		/*
		
		// Check if we already have an app object representing this app registered to the app manager
		for (App app : appManager.getApps()) {
			if (this.heuristicEquals(app) && this != app) {
				
				app.setAppFile(this.getAppFile());
				app.install(appManager);
				
				return;
			}
		}
		
		File appFile = this.getAppFile();
		
		try {
			if (appFile != null 
					&& !appFile.getParentFile().getCanonicalPath().equals(appManager.getInstalledAppsPath())) {
				
				FileUtils.copyFileToDirectory(appFile, new File(appManager.getInstalledAppsPath()));
				
				if (appFile.getParentFile().getCanonicalPath().equalsIgnoreCase(
						appManager.getUninstalledAppsPath())) {
					appFile.delete();
				}
				
				this.setAppFile(new File(appManager.getInstalledAppsPath() + File.separator + appFile.getName()));
			}
		} catch (IOException e) {
			throw new AppInstallException("Failed to copy bundle app to local storage directory for installed apps");
		}
		
		// Prepare to copy to Karaf deploy directory
		String karafDeployDirectory = appManager.getKarafDeployDirectory();
		
		try {
			
			// Copy if not already in Karaf deploy directory 
			File karafTargetFile = new File(karafDeployDirectory + File.separator + this.getAppFile().getName());
			if (!karafTargetFile.exists()) {
				FileUtils.copyFile(this.getAppFile(), karafTargetFile);
			}
			
			this.setAppTemporaryInstallFile(karafTargetFile);
			
		} catch (IOException e) {
			throw new AppInstallException("Failed to copy bundle app to Karaf deploy directory");
		}
		
		// Check if the features were installed
		FeaturesService featuresService = appManager.getFeaturesService();
		List<Feature> installedFeatures = getCorrespondingFeatures(featuresService); 
		
		//System.out.println("features from app: " + featuresSet.size());
		//System.out.println("available features: " + featuresService.listFeatures().length);
		
		if (!appManager.getApps().contains(this)) {
			appManager.getApps().add(this);
		}
		
		this.setStatus(AppStatus.INSTALLED);
		
		*/
	}

// Ticket 1554: FeaturesService is not API so we shouldn't use it.  Disabled for now.
//	private List<Feature> getCorrespondingFeatures(FeaturesService featuresService) {
//		List<Feature> correspondingFeatures = new LinkedList<Feature>();
//		
//		Feature[] availableFeatures = new Feature[]{};
//		try {
//			availableFeatures = featuresService.listFeatures();
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
//		
//		for (int i = 0; i < availableFeatures.length; i++) {
//			Feature availableFeature = availableFeatures[i];
//			
//			KarafArchiveApp.KarafFeature appFeature = featuresSet.get(availableFeature.getName());
//			
//			// System.out.println("available feature: " + availableFeature.getName() + ", " + availableFeature.getVersion());
//			
//			if (appFeature != null
//					&& appFeature.featureVersion.equalsIgnoreCase(availableFeature.getVersion())) {
//				correspondingFeatures.add(availableFeature);
//				
//				// System.out.println("feature match: " + availableFeature.getName() + " vs " + appFeature.featureName);
//			}
//		}
//		
//		return correspondingFeatures;
//	}
	
	@Override
	public void uninstall(AppManager appManager) throws AppUninstallException {
		
		// Use the default uninstallation procedure consisting of moving the app file
		// to the uninstalled apps directory
		// defaultUninstall(appManager);
		
		try {
			File uninstallDirectoryTargetFile = new File(appManager.getUninstalledAppsPath() + File.separator + getAppFile().getName());
			
			if (uninstallDirectoryTargetFile.exists()) {
				uninstallDirectoryTargetFile.delete();
			}
			
			try {
				FileUtils.moveFile(getAppFile(), uninstallDirectoryTargetFile);
			} catch (FileExistsException e) {
			}
			
			this.setAppFile(uninstallDirectoryTargetFile);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			
			throw new AppUninstallException("Unable to move app file to uninstalled apps directory: " + e.getMessage());
		}
		
		this.getAppTemporaryInstallFile().delete();
		
		this.setStatus(AppStatus.UNINSTALLED);
	}

	@Override
	public void disable(AppManager appManager) throws AppDisableException {
		// TODO Auto-generated method stub
		
	}
	
	/**
	 * Return the list of karaf features.
	 * @return The list of karaf features
	 */
	public Map<String, KarafFeature> getFeaturesList() {
		return featuresSet;
	}
	
	public void setFeaturesList(Map<String, KarafFeature> featuresSet) {
		this.featuresSet = featuresSet;
	}

}
