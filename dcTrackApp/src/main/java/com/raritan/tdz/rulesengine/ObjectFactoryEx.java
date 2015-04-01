/**
 * 
 */
package com.raritan.tdz.rulesengine;

import javax.xml.bind.annotation.XmlRegistry;

import com.raritan.dctrack.xsd.ObjectFactory;
import com.raritan.dctrack.xsd.UiComponent;
import com.raritan.tdz.dto.UiComponentDTO;

/**
 * @author prasanna
 *
 */
@XmlRegistry
public class ObjectFactoryEx extends ObjectFactory{
	@Override
    public UiComponent createUiComponent() {
        return new UiComponentDTO();
    }
}
