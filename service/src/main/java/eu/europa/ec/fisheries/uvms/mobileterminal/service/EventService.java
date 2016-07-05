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
package eu.europa.ec.fisheries.uvms.mobileterminal.service;

import eu.europa.ec.fisheries.uvms.mobileterminal.message.event.*;
import eu.europa.ec.fisheries.uvms.mobileterminal.message.event.carrier.EventMessage;
import javax.ejb.Local;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.enterprise.event.Observes;

@Local
public interface EventService {

    @TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
    void get(@Observes @GetReceivedEvent EventMessage message);

    @TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
    void ping(@Observes @PingReceivedEvent EventMessage message);

    @TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
    void list(@Observes @ListReceivedEvent EventMessage message);

//    public void getData(@Observes @MessageReceivedEvent EventMessage message);

    public void returnError(@Observes @ErrorEvent EventMessage message);
    
}