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
package eu.europa.ec.fisheries.uvms.mobileterminal.message.consumer.bean;

import javax.annotation.PostConstruct;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.JMSException;
import javax.jms.Queue;
import javax.jms.QueueConnectionFactory;
import javax.jms.Session;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.europa.ec.fisheries.uvms.config.exception.ConfigMessageException;
import eu.europa.ec.fisheries.uvms.config.message.ConfigMessageConsumer;
import eu.europa.ec.fisheries.uvms.message.JMSUtils;
import eu.europa.ec.fisheries.uvms.mobileterminal.message.constants.MessageConstants;
import eu.europa.ec.fisheries.uvms.mobileterminal.message.consumer.MessageConsumer;
import eu.europa.ec.fisheries.uvms.mobileterminal.message.exception.MobileTerminalMessageException;

@Stateless
public class MobileTerminalMessageConsumer implements MessageConsumer, ConfigMessageConsumer {

    final static Logger LOG = LoggerFactory.getLogger(MobileTerminalMessageConsumer.class);

    private final static long TIMEOUT = 30000; //TODO timeout

    private ConnectionFactory connectionFactory;

    private Queue responseMobileTerminalQueue;

    private Connection connection = null;
    private Session session = null;

    @PostConstruct
    private void init() {
        LOG.debug("Open connection to JMS broker");
        InitialContext ctx;
        try {
            ctx = new InitialContext();
        } catch (Exception e) {
            LOG.error("Failed to get InitialContext",e);
            throw new RuntimeException(e);
        }
        try {
            connectionFactory = (QueueConnectionFactory) ctx.lookup(MessageConstants.CONNECTION_FACTORY);
        } catch (NamingException ne) {
            //if we did not find the connection factory we might need to add java:/ at the start
            LOG.debug("Connection Factory lookup failed for " + MessageConstants.CONNECTION_FACTORY);
            String wfName = "java:/" + MessageConstants.CONNECTION_FACTORY;
            try {
                LOG.debug("trying "+wfName);
                connectionFactory = (QueueConnectionFactory) ctx.lookup(wfName);
            } catch (Exception e) {
                LOG.error("Connection Factory lookup failed for both "+MessageConstants.CONNECTION_FACTORY + " and " + wfName);
                throw new RuntimeException(e);
            }
        }
        responseMobileTerminalQueue = JMSUtils.lookupQueue(ctx, MessageConstants.COMPONENT_RESPONSE_QUEUE);
    }

    @Override
    @TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
    public <T> T getMessage(String correlationId, Class type) throws MobileTerminalMessageException {
        try {

            if (correlationId == null || correlationId.isEmpty()) {
                throw new MobileTerminalMessageException("No CorrelationID provided!");
            }

            connectToQueue();

            T response = (T) session.createConsumer(responseMobileTerminalQueue, "JMSCorrelationID='" + correlationId + "'").receive(TIMEOUT);

            if (response == null) {
                throw new MobileTerminalMessageException("[ Timeout reached or message null in MobileTerminalMessageConsumer. ]");
            }

            return response;
        } catch (Exception e) {
            LOG.error("[ Error when consuming message. ] {}", e.getMessage());
            throw new MobileTerminalMessageException("Error when retrieving message: " + e.getMessage());
        } finally {
            disconnectQueue();
        }
    }

    private void connectToQueue() throws JMSException {
        connection = connectionFactory.createConnection();
        session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
        connection.start();
    }

    @Override
    @TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
    public <T> T getConfigMessage(String correlationId, Class type) throws ConfigMessageException {
        try {
            return getMessage(correlationId, type);
        }
        catch (MobileTerminalMessageException e) {
            LOG.error("[ Error when getting config message. ] {}", e.getMessage());
            throw new ConfigMessageException(e.getMessage());
        }
    }

    private void disconnectQueue() {
        try {
            if (connection != null) {
                connection.close();
            }
        } catch (JMSException e) {
            LOG.error("[ Error when closing JMS connection ] {}", e.getMessage());
        }
    }

}