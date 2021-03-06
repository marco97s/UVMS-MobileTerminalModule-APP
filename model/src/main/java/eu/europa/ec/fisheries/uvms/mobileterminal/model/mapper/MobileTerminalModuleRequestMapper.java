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
package eu.europa.ec.fisheries.uvms.mobileterminal.model.mapper;

import eu.europa.ec.fisheries.schema.mobileterminal.module.v1.*;
import eu.europa.ec.fisheries.schema.mobileterminal.types.v1.MobileTerminalId;
import eu.europa.ec.fisheries.schema.mobileterminal.types.v1.MobileTerminalListQuery;
import eu.europa.ec.fisheries.schema.mobileterminal.types.v1.MobileTerminalType;
import eu.europa.ec.fisheries.uvms.mobileterminal.model.exception.MobileTerminalModelMapperException;

import java.util.List;

public class MobileTerminalModuleRequestMapper {

    public static String createMobileTerminalResponse(MobileTerminalType mobTerm) throws MobileTerminalModelMapperException {
        MobileTerminalResponse response = new MobileTerminalResponse();
        response.setMobilTerminal(mobTerm);
        return JAXBMarshaller.marshallJaxBObjectToString(response);
    }

    public static String createGetMobileTerminalRequest(MobileTerminalId mobileTerminalId) throws MobileTerminalModelMapperException {
        GetMobileTerminalRequest request = new GetMobileTerminalRequest();
        request.setMethod(MobileTerminalModuleMethod.GET_MOBILE_TERMINAL);
        request.setId(mobileTerminalId);
        return JAXBMarshaller.marshallJaxBObjectToString(request);
    }

    public static String mapGetMobileTerminalList(List<MobileTerminalType> mobileTerminalTypes) throws MobileTerminalModelMapperException {
        MobileTerminalListResponse response = new MobileTerminalListResponse();
        response.getMobileTerminal().addAll(mobileTerminalTypes);
        return JAXBMarshaller.marshallJaxBObjectToString(response);
    }

    public static String mapToMobileTerminalListBatchResponse(List<MobileTerminalBatchListElement> mobileTerminalTypes) throws MobileTerminalModelMapperException {
        MobileTerminalBatchListResponse response = new MobileTerminalBatchListResponse();
        response.getResponseList().addAll(mobileTerminalTypes);
        return JAXBMarshaller.marshallJaxBObjectToString(response);
    }

    public static String createMobileTerminalListRequest(MobileTerminalListQuery query) throws MobileTerminalModelMapperException {
        MobileTerminalListRequest request = new MobileTerminalListRequest();
        request.setMethod(MobileTerminalModuleMethod.LIST_MOBILE_TERMINALS);
        request.setQuery(query);
        return JAXBMarshaller.marshallJaxBObjectToString(request);
    }

    public static String createMobileTerminalBatchListRequest(List<MobileTerminalListQuery> queries) throws MobileTerminalModelMapperException {
        MobileTerminalBatchListRequest request = new MobileTerminalBatchListRequest();
        request.setMethod(MobileTerminalModuleMethod.BATCH_LIST_MOBILE_TERMINALS);
        request.getQueryList().addAll(queries);
        return JAXBMarshaller.marshallJaxBObjectToString(request);
    }

}