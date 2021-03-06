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

package av.proj.ide.hplat;

import org.eclipse.sapphire.Element;
import org.eclipse.sapphire.ElementList;
import org.eclipse.sapphire.ElementType;
import org.eclipse.sapphire.ListProperty;
import org.eclipse.sapphire.Type;
import org.eclipse.sapphire.Value;
import org.eclipse.sapphire.ValueProperty;
import org.eclipse.sapphire.modeling.annotations.DefaultValue;
import org.eclipse.sapphire.modeling.annotations.Label;
import org.eclipse.sapphire.modeling.annotations.MustExist;
import org.eclipse.sapphire.modeling.annotations.Required;
import org.eclipse.sapphire.modeling.annotations.Service;
import org.eclipse.sapphire.modeling.xml.annotations.CustomXmlListBinding;
import org.eclipse.sapphire.modeling.xml.annotations.XmlBinding;
//import org.eclipse.sapphire.modeling.xml.annotations.XmlListBinding;

import av.proj.ide.custom.bindings.list.SlotSignalXmlListBinding;
import av.proj.ide.services.NameValidationService;

public interface Slot extends Element
{
	ElementType TYPE = new ElementType(Slot.class);
	
	// *** name attribute***
	@XmlBinding(path = "@name")
	@Label(standard = "name")
	@Required
	@Service(impl=NameValidationService.class)
	ValueProperty PROP_NAME = new ValueProperty(TYPE, "Name");

	// *** name attribute***
	@XmlBinding(path = "@type")
	@Label(standard = "type")
	@Required
	ValueProperty PROP_TYPE = new ValueProperty(TYPE, "Type");

	// Create the slot signal model.  Note that platform, device, and slot signals
	// have different attributes.
	public interface Signal extends Element
	{
		ElementType TYPE = new ElementType(Signal.class);
		
		// *** name attribute***
		@XmlBinding(path = "@slot")
		@Label(standard = "slot")
		@Required
		ValueProperty PROP_SLOT = new ValueProperty(TYPE, "Slot");
		
		Value<String> getSlot();
		void setSlot(String value);
		
		// *** name attribute***
		@XmlBinding(path = "@platform")
		@Label(standard = "platform")
		@DefaultValue(text="")
		@MustExist
		ValueProperty PROP_PLATFORM = new ValueProperty(TYPE, "Platform");
		
		Value<String> getPlatform();
		void setPlatform(String value);
	}

	// *** Slot signal elements ***
	@Type( base = Signal.class )
	@CustomXmlListBinding(impl = SlotSignalXmlListBinding.class)
	@Label( standard = "signal" )
	//ListProperty  PROP_SIGNALS = new ListProperty(TYPE, "Signals");
	//Signal getSignals();  // Not sure how this was right?

	ListProperty  PROP_SIGNAL = new ListProperty(TYPE, "Signal");

	ElementList<Signal> getSignal();
}
