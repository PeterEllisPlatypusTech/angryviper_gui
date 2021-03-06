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

package av.proj.ide.plugin.views;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.part.ViewPart;

import av.proj.ide.avps.internal.AngryViperAssetService;
import av.proj.ide.swt.AddViewControls;
import av.proj.ide.swt.ColorSchemeBlue;
import av.proj.ide.swt.ProjectImages;
import av.proj.ide.swt.ProjectViewSwtDisplay;


public class AVProjectView extends ViewPart {

	/**
	 * Created from the SampleView plugin extension this is the 
	 * ID of the view as specified by the extension (in plugin.xml).
	 */
	public static final String ID = "av.proj.ide.plugin.views.AVProjectView";
	private ProjectViewSwtDisplay projectDisplay;

	/**
	 * The constructor.
	 */
	public AVProjectView() {
		AngryViperAssetService.getInstance();
	}

	/**
	 * This is a callback that will allow us
	 * to create the viewer and initialize it.
	 */
	public void createPartControl(Composite parent) {
		projectDisplay = new ProjectViewSwtDisplay(parent, SWT.NONE);
		ColorSchemeBlue colorScheme = new ColorSchemeBlue();
		projectDisplay.setPanelColorScheme(colorScheme);
	    ClassLoader cl = this.getClass().getClassLoader();
		ProjectImages pi = new ProjectImages(cl, parent.getDisplay());
		projectDisplay.loadModelData(AngryViperAssetService.getInstance(), pi);
		AddViewControls.addProjectPanelControls(projectDisplay);
	}
	
	
	/**
	 * Passing the focus request to the viewer's control.
	 */
	public void setFocus() {
	}

}