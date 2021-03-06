/*
﻿Developed with the contribution of the European Commission - Directorate General for Maritime Affairs and Fisheries
© European Union, 2015-2016.

This file is part of the Integrated Fisheries Data Management (IFDM) Suite. The IFDM Suite is free software: you can
redistribute it and/or modify it under the terms of the GNU General Public License as published by the
Free Software Foundation, either version 3 of the License, or any later version. The IFDM Suite is distributed in
the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more details. You should have received a
copy of the GNU General Public License along with the IFDM Suite. If not, see <http://www.gnu.org/licenses/>.
 */
package eu.europa.ec.fisheries.uvms.mobileterminal.model.validator;

import eu.europa.ec.fisheries.schema.mobileterminal.types.v1.ComChannelAttribute;
import eu.europa.ec.fisheries.schema.mobileterminal.types.v1.ComChannelType;
import eu.europa.ec.fisheries.schema.mobileterminal.types.v1.MobileTerminalAttribute;
import eu.europa.ec.fisheries.schema.mobileterminal.types.v1.MobileTerminalId;
import eu.europa.ec.fisheries.schema.mobileterminal.types.v1.MobileTerminalType;
import eu.europa.ec.fisheries.uvms.mobileterminal.model.exception.MobileTerminalModelValidationException;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 **/
public class MobileTerminalDataSourceRequestValidator {

    private static Logger LOG = LoggerFactory.getLogger(MobileTerminalDataSourceRequestValidator.class);
    private static String IRIDIUM = "IRIDIUM";

    public static void validateCreateMobileTerminalType(MobileTerminalType mobTermType) throws MobileTerminalModelValidationException {
        if(mobTermType.isInactive()){
            throw new MobileTerminalModelValidationException("Cannot create a Mobile Terminal with status set to inactive");
        }
        validateMobileTerminalAttributes(mobTermType.getAttributes());
        if(!IRIDIUM.equalsIgnoreCase(mobTermType.getType())) {
            validateComChannels(mobTermType);
        }
    }
	
    public static void validateMobileTerminalType(MobileTerminalType mobTermType) throws MobileTerminalModelValidationException {
        validateMobileTerminalId(mobTermType.getMobileTerminalId());
        validateMobileTerminalAttributes(mobTermType.getAttributes());
        if(!IRIDIUM.equalsIgnoreCase(mobTermType.getType())) {
            validateComChannels(mobTermType);
        }
    }

    public static void validateMobileTerminalId(MobileTerminalId id) throws MobileTerminalModelValidationException {
    	if(id == null || id.getGuid() == null || id.getGuid().isEmpty()) {
    		throw new MobileTerminalModelValidationException("Non valid mobile terminal id");
    	}
    }

    public static void validateMobileTerminalAttributes(List<MobileTerminalAttribute> attributes) throws MobileTerminalModelValidationException {
        Set<String> uniqueFields = new HashSet<String>();
        for (MobileTerminalAttribute attr : attributes) {
        	if(!"MULTIPLE_OCEAN".equalsIgnoreCase(attr.getType())) {
        		if (!uniqueFields.add(attr.getType())) {
                    throw new MobileTerminalModelValidationException("Non unique attribute field " + attr.getType());
                }
        	}
        }
    }

    public static void validateComChannels(MobileTerminalType type) throws MobileTerminalModelValidationException {
    	//TODO terminaltype -> comchannelvaluetype instead of channeltype when validate
        for (ComChannelType channel : type.getChannels()) {
        	if("VMS".equalsIgnoreCase(channel.getName())) {
        		validateVMS(channel);
        	}
        	else {
        	    LOG.debug("Channel name is not VMS. Will not validate further, and will probably fail validation in the future.");
        	}
        	//	throw new MobileTerminalModelValidationException("ComChannel with SystemType " + type.getType() + " validation not impemented");
        }
    }

    private static void validateVMS(ComChannelType channel) throws MobileTerminalModelValidationException {
        Set<String> fields = new HashSet<>();

        for (ComChannelAttribute attribute : channel.getAttributes()) {
            fields.add(attribute.getType());
        }

        boolean dnid = fields.contains("DNID");
        boolean memberId = fields.contains("MEMBER_NUMBER");

        if (!dnid || !memberId) {
            throw new MobileTerminalModelValidationException("A Com channel with channelType " + channel.getName() + " must contain DNID and Member Number");
        }

        if (fields.size() != channel.getAttributes().size()) {
            throw new MobileTerminalModelValidationException("ChannelType ids can only occur once!");
        }

    }

}