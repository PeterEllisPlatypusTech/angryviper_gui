/*
 * This file is protected by Copyright. Please refer to the COPYRIGHT file
 * distributed with this source distribution.
 *
 * This file is part of OpenCPI <http://www.opencpi.org>
 *
 * OpenCPI is free software: you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or (at your option) any
 * later version.
 *
 * OpenCPI is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package av.proj.ide.avps.internal;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import org.json.simple.JSONObject;

public class SearchForSpecs {
	private Map<String, String> specs;
	private Set<String> protocols;
	private Set<String> hdlAdapters;
	private HashSet<String> invalidSpecPath;
	
	
	public SearchForSpecs() {
		this.specs = new TreeMap<String,String>();
		this.protocols = new TreeSet<String>();
		
		invalidSpecPath = new HashSet<String>();
		invalidSpecPath.add("applications");
		invalidSpecPath.add("doc");
		invalidSpecPath.add("exports");
		invalidSpecPath.add("imports");
		invalidSpecPath.add("scripts");
		invalidSpecPath.add("gen");
		invalidSpecPath.add("lib");
		invalidSpecPath.add("assemblies");
		invalidSpecPath.add("primitives");
		searchForSpecs();
	}
	
	protected String [] getProjectsCmd = {"ocpidev", "show", "projects", "--json"};

	public class Project {
		public String name;
//		public Boolean registered;
//		public Boolean exists;
		public String fullPath;
		
		protected Project (String name, JSONObject prjData) {
			this.name = name;
			fullPath = (String) prjData.get("real_path");
/*(**			
 Not needed at this time.			
			Object obj = prjData.get("registered");
			if(obj instanceof Boolean) {
				registered = (Boolean) obj;
			}
			
			obj = prjData.get("exists");
			if(obj instanceof Boolean) {
				exists = (Boolean) obj;
			}
***/
		}
	}
	
	protected List <Project> getOcpiProjects() {

		JSONObject jsonObject = EnvBuildTargets.getEnvInfo(getProjectsCmd);
        ArrayList<Project> envProjects = new ArrayList<Project>();
		if(jsonObject != null) {
        	JSONObject projectsObj = (JSONObject) jsonObject.get("projects");

        	if(projectsObj != null) {
    	        @SuppressWarnings("unchecked")
    			Set<String> projects = projectsObj.keySet();
    	        for(String key : projects) {
    	        	Project project = new Project(key, (JSONObject) projectsObj.get(key));
	   	        	envProjects.add(project);
    	        }
   		    }
		}
		return envProjects;
	}
	
	public void searchForSpecs() {
		List<Project> projects = getOcpiProjects();
		getProjectEnvSpecs(projects);
	}
	
	public void searchForAdapters() {
		List<Project> projects = getOcpiProjects();
		getProjectEnvHdlAdapters(projects);
	}
	private void getProjectEnvHdlAdapters(List<Project> projects) {
		hdlAdapters = new HashSet<String>();
		for (Project project : projects) {
			if("ocpi.cdk".equals(project.name))
				continue;
			
			File projectFolder = new File(project.fullPath);
			if (projectFolder != null && projectFolder.exists()) {
				File adaptersDir = new File(projectFolder, "hdl/adapters");
				if(adaptersDir.exists() && adaptersDir.isDirectory() ){
					String[] children = adaptersDir.list();
					for(String child : children) {
						if(child.endsWith(".hdl")) {
							hdlAdapters.add(child.substring(0, child.length() -4));
						}
					}
				}
			}
		}
	}

	public Map<String, String> getSpecs() {
		return specs;
	}
	public Set<String> getProtocols() {
		return protocols;
	}
	public Set<String> getHdlAdapters() {
		return hdlAdapters;
	}
	
	private void getProjectEnvSpecs(List<Project> projects) {
		for (Project project : projects) {
			if("ocpi.cdk".equals(project.name))
				continue;
			
			File projectFolder = new File(project.fullPath);
			if (projectFolder != null && projectFolder.exists()) {
				File topSpecsDir = new File(projectFolder, "specs");
				if(topSpecsDir.exists() && topSpecsDir.isDirectory() ){
					String packageName = getPackageId(topSpecsDir, topSpecsDir.list());
					getSpecs(topSpecsDir, packageName);
				}
				
				findSpecs(projectFolder,"components");
				
				File hdlFolder = new File(projectFolder, "hdl");
				findSpecs(hdlFolder, "adapters");
				findSpecs(hdlFolder, "cards");
				findSpecs(hdlFolder, "devices");
				findSpecs(hdlFolder, "platforms");
			}
		}
	}
	
	protected boolean itsNotOneLike(String dirName) {
		boolean itsNotLike = true;
		if( dirName.endsWith(".rcc") 
			|| dirName.endsWith(".hdl") || dirName.endsWith(".hdl")
			|| dirName.endsWith(".test")) {
			itsNotLike = false;
		}
		return itsNotLike;
	}
	
	protected void findSpecs (File parentDir, String directory) {
		File dir = new File(parentDir, directory);
		if(dir.exists() && dir.isDirectory()) {
			String[] children = dir.list();
			for(String child : children) {
				if("specs".equals(child)) {
					File specsDir = new File(dir, child);
					String[] specDirs = specsDir.list();
					
					if(specDirs.length == 0)
						break;
					String packageName = getPackage(dir, children);
					getSpecs(specsDir, packageName);
				}
				else if( ! invalidSpecPath.contains(child) && itsNotOneLike(child)) {
					findSpecs(dir, child);
				}
			}
			
		}
	}
	
	protected void getSpecs(File specsDir, String packageName) {
		if( ! specsDir.exists() || ! specsDir.isDirectory())
			return;
		
		String[] children = specsDir.list();
		for(String child : children) {
			File specFile = new File(specsDir, child);
			if(specFile.isFile()){
				String name = specFile.getName();
				String specName;
				String frameworkComponentName;
				if (name.endsWith("-spec.xml")) {
					specName = packageName + name.substring(0, name.length() - 4);
					frameworkComponentName = packageName + name.replace("-spec.xml", "");
					specs.put(specName, frameworkComponentName);
				} else if (name.endsWith("_spec.xml")) {
					specName = packageName + name.substring(0, name.length() - 4);
					frameworkComponentName = packageName + name.replace("_spec.xml", "");
					specs.put(specName, frameworkComponentName);
				}
				else if (name.endsWith("-prot.xml") ||
						name.endsWith("_prot.xml") ||
						name.endsWith("-protocol.xml") ||
						name.endsWith("_protocol.xml")) {
					protocols.add(name.replace(".xml", ""));
				}
				
			}
		}
		
	}
	
	protected String getPackageId(File pkgIdFolder, String[] children) {
		String packageName = "";
		for(String file : children) {
			if("package-id".equals(file)) {
				File packageFile = new File(pkgIdFolder, file);
				String line = null;
				FileReader fileReader = null;
				BufferedReader bufferedReader = null;
				try {
					fileReader = new FileReader(packageFile);
					bufferedReader = new BufferedReader(fileReader);
					line = bufferedReader.readLine();
					if(line != null) {
						packageName = line +".";
						break;
					}
				} catch (FileNotFoundException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				} finally {
					try {
						if (fileReader != null) {
							fileReader.close();
						}
						if (bufferedReader != null) {
							bufferedReader.close();
						}
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
				
			}
		}
		return packageName;
	}

	protected String getPackage(File dir, String[] children) {
		String packageName = "";
		for(String child : children) {
			if("lib".equals(child)) {
				File libDir = new File(dir, child);
				children = libDir.list();
				packageName = getPackageId(libDir, children);
			}
		}
		return packageName;
	}
	
}
