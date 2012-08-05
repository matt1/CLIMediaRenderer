package org.matt1.climediarenderer.managers;

import java.util.ArrayList;
import java.util.Collection;
import java.util.logging.Logger;

import org.fourthline.cling.model.meta.LocalService;
import org.fourthline.cling.model.meta.StateVariable;
import org.fourthline.cling.model.state.StateVariableValue;
import org.fourthline.cling.model.types.UnsignedIntegerFourBytes;
import org.fourthline.cling.support.lastchange.LastChange;
import org.fourthline.cling.support.lastchange.LastChangeAwareServiceManager;
import org.fourthline.cling.support.lastchange.LastChangeDelegator;
import org.fourthline.cling.support.lastchange.LastChangeParser;

/**
 * Slightly more lenient LastChangeAwareServiceManager that racefully handles null pointers
 * @author Matt
 *
 * @param <T>
 */
public class LenientChangeAwareServiceManager<T extends LastChangeDelegator> extends
		LastChangeAwareServiceManager<T> {

	private static Logger log = Logger.getLogger(LenientChangeAwareServiceManager.class.getName());
	
    public LenientChangeAwareServiceManager(LocalService<T> localService,
            LastChangeParser lastChangeParser) {
    	super(localService, null, lastChangeParser);
    }

    public LenientChangeAwareServiceManager(LocalService<T> localService,
            Class<T> serviceClass,
            LastChangeParser lastChangeParser) {
    	super(localService, serviceClass, lastChangeParser);
    }


    @SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
    protected Collection<StateVariableValue> readInitialEventedStateVariableValues() 
    		throws Exception {

        // We don't use the service's internal LastChange but a fresh new one just for
        // this initial event. Modifying the internal one would trigger event notification's
        // to other subscribers!
        LastChange lc = new LastChange(getLastChangeParser());

        // Get the current "logical" instances of the service
        UnsignedIntegerFourBytes[] ids = getImplementation().getCurrentInstanceIds();
        if (ids == null) {
        	log.warning("LenientChangeAwareService got a null ID from " 
        			+ getImplementation().getClass().getCanonicalName());
            getImplementation().appendCurrentState(lc, new UnsignedIntegerFourBytes(0));
        } else {
	        if (ids.length > 0) {
	            for (UnsignedIntegerFourBytes instanceId : ids) {
	                // Iterate through all "logical" instances and ask them what their state is
	                getImplementation().appendCurrentState(lc, instanceId);
	            }
	        } else {
	            // Use the default "logical" instance with ID 0
	            getImplementation().appendCurrentState(lc, new UnsignedIntegerFourBytes(0));
	        }
        }

        // Sum it all up and return it in the initial event to the GENA subscriber
        StateVariable variable = getService().getStateVariable("LastChange");
        Collection<StateVariableValue> values = new ArrayList();
        values.add(new StateVariableValue(variable, lc.toString()));
        return values;
    }
    
	
}
