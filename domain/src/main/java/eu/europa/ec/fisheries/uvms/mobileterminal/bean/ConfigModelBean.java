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
package eu.europa.ec.fisheries.uvms.mobileterminal.bean;

import eu.europa.ec.fisheries.schema.mobileterminal.config.v1.CapabilityConfiguration;
import eu.europa.ec.fisheries.schema.mobileterminal.config.v1.ConfigList;
import eu.europa.ec.fisheries.schema.mobileterminal.config.v1.TerminalSystemConfiguration;
import eu.europa.ec.fisheries.schema.mobileterminal.config.v1.TerminalSystemType;
import eu.europa.ec.fisheries.schema.mobileterminal.types.v1.Plugin;
import eu.europa.ec.fisheries.schema.mobileterminal.types.v1.PluginService;
import eu.europa.ec.fisheries.uvms.commons.date.DateUtils;
import eu.europa.ec.fisheries.uvms.mobileterminal.constant.MobileTerminalConfigType;
import eu.europa.ec.fisheries.uvms.mobileterminal.constant.MobileTerminalConstants;
import eu.europa.ec.fisheries.uvms.mobileterminal.dao.ChannelDao;
import eu.europa.ec.fisheries.uvms.mobileterminal.dao.DNIDListDao;
import eu.europa.ec.fisheries.uvms.mobileterminal.dao.MobileTerminalPluginDao;
import eu.europa.ec.fisheries.uvms.mobileterminal.dao.OceanRegionDao;
import eu.europa.ec.fisheries.uvms.mobileterminal.dao.exception.ConfigDaoException;
import eu.europa.ec.fisheries.uvms.mobileterminal.dao.exception.InputArgumentException;
import eu.europa.ec.fisheries.uvms.mobileterminal.dao.exception.NoEntityFoundException;
import eu.europa.ec.fisheries.uvms.mobileterminal.dao.exception.TerminalDaoException;
import eu.europa.ec.fisheries.uvms.mobileterminal.entity.DNIDList;
import eu.europa.ec.fisheries.uvms.mobileterminal.entity.MobileTerminalPlugin;
import eu.europa.ec.fisheries.uvms.mobileterminal.entity.MobileTerminalPluginCapability;
import eu.europa.ec.fisheries.uvms.mobileterminal.entity.OceanRegion;
import eu.europa.ec.fisheries.uvms.mobileterminal.entity.types.MobileTerminalTypeEnum;
import eu.europa.ec.fisheries.uvms.mobileterminal.entity.types.PollTypeEnum;
import eu.europa.ec.fisheries.uvms.mobileterminal.mapper.PluginMapper;
import eu.europa.ec.fisheries.uvms.mobileterminal.model.exception.MobileTerminalModelException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ejb.EJB;
import javax.ejb.LocalBean;
import javax.ejb.Stateless;
import java.util.*;

@Stateless
@LocalBean
public class ConfigModelBean  {

    private final static Logger LOG = LoggerFactory.getLogger(ConfigModelBean.class);
    
    @EJB
    private OceanRegionDao oceanRegionDao;

    @EJB
    private MobileTerminalPluginDao mobileTerminalPluginDao;
    
    @EJB
    private DNIDListDao dnidListDao;
    
    @EJB
	private ChannelDao channelDao;
    
    public List<TerminalSystemType> getAllTerminalSystems() throws MobileTerminalModelException {
        Map<MobileTerminalTypeEnum, List<MobileTerminalPlugin>> pluginsByType = getPlugins();
		List<TerminalSystemType> terminalSystemList = new ArrayList<>();

		for (MobileTerminalTypeEnum type : pluginsByType.keySet()) {

			TerminalSystemConfiguration terminalFieldConfiguration = PluginMapper.mapTerminalFieldConfiguration(type);
			TerminalSystemConfiguration comchannelFieldConfiguration = PluginMapper.mapComchannelFieldConfiguration(type);
			List<OceanRegion> oceanRegionList = oceanRegionDao.getOceanRegionList();
			CapabilityConfiguration capabilityConfiguration = PluginMapper.mapCapabilityConfiguration(type, pluginsByType.get(type), oceanRegionList);

			TerminalSystemType systemType = new TerminalSystemType();

			systemType.setType(type.name());
			systemType.setTerminalConfiguration(terminalFieldConfiguration);
			systemType.setComchannelConfiguration(comchannelFieldConfiguration);
			systemType.setCapabilityConfiguration(capabilityConfiguration);

			terminalSystemList.add(systemType);
		}
        return terminalSystemList;
    }

	public List<ConfigList> getConfigValues() {
		List<ConfigList> configValues = new ArrayList<>();
		for(MobileTerminalConfigType config : MobileTerminalConfigType.values()) {
			ConfigList list = new ConfigList();
			list.setName(config.name());
			switch(config) {
			case POLL_TYPE:
				list.getValue().addAll(getPollTypes());
				break;
			case TRANSPONDERS:
				list.getValue().addAll(getTransponders());
				break;
			case POLL_TIME_SPAN:
				list.getValue().addAll(getPollTimeSpan());
				break;
			}
			configValues.add(list);
		}
		return configValues;
	}
	
	MobileTerminalPlugin updatePlugin(PluginService plugin) throws TerminalDaoException {
		try {
			MobileTerminalPlugin entity = mobileTerminalPluginDao.getPluginByServiceName(plugin.getServiceName());
			if(PluginMapper.equals(entity, plugin)) {
				return entity;
			} else {
				for(MobileTerminalPluginCapability capability : entity.getCapabilities()) {
					capability.setPlugin(null);
				}
				entity.getCapabilities().clear();
				entity = PluginMapper.mapModelToEntity(entity, plugin);
				mobileTerminalPluginDao.updateMobileTerminalPlugin(entity);
				return entity;
			}
		} catch (NoEntityFoundException e) {
			return null;
		}
	}
	
	public List<Plugin> upsertPlugins(List<PluginService> pluginList) throws MobileTerminalModelException {
		if(pluginList == null) {
			throw new InputArgumentException("No pluginList to upsert");
		}
		
		Map<String, PluginService> map = new HashMap<>();
		List<Plugin> responseList = new ArrayList<>();
		for(PluginService plugin : pluginList) {
			if(plugin.getLabelName() == null || plugin.getLabelName().isEmpty()) {
				throw new InputArgumentException("No plugin name");
			}
			if(plugin.getServiceName() == null || plugin.getServiceName().isEmpty()) {
				throw new InputArgumentException("No service name");
			}
			if(plugin.getSatelliteType() == null || plugin.getSatelliteType().isEmpty()) {
				throw new InputArgumentException("No satellite type");
			}
			
			try {
				MobileTerminalPlugin entity = updatePlugin(plugin);
				if(entity == null) {
					entity = PluginMapper.mapModelToEntity(plugin);
					entity = mobileTerminalPluginDao.createMobileTerminalPlugin(entity);
				}
				map.put(plugin.getServiceName(), plugin);
				responseList.add(PluginMapper.mapEntityToModel(entity));
			} catch (TerminalDaoException e) {
				throw new MobileTerminalModelException("Couldn't persist plugin " + e.getMessage());
			}
		}
		
		responseList.addAll(inactivatePlugins(map));
		
		return responseList;
	}
	
	List<Plugin> inactivatePlugins(Map<String, PluginService> map) throws ConfigDaoException {
		List<Plugin> responseList = new ArrayList<>();
		List<MobileTerminalPlugin> availablePlugins = mobileTerminalPluginDao.getPluginList();
		for(MobileTerminalPlugin plugin : availablePlugins) {
			PluginService pluginService = map.get(plugin.getPluginServiceName());
			if(pluginService == null && !plugin.getPluginInactive()) {
				LOG.debug("inactivate no longer available plugin");
				plugin.setPluginInactive(true);
				responseList.add(PluginMapper.mapEntityToModel(plugin));
			}
		}
		return responseList;
	}

	public List<String> updatedDNIDList(String pluginName) throws MobileTerminalModelException {
		List<String> dnids = new ArrayList<>();
		List<DNIDList> dnidList = dnidListDao.getDNIDList(pluginName);
		for(DNIDList entity : dnidList) {
			dnids.add(entity.getDNID());
		}
		return dnids;
	}

	public boolean checkDNIDListChange(String pluginName) {
		//TODO fix sql query:

		List<String> activeDnidList = channelDao.getActiveDNID(pluginName);
		try {
			List<DNIDList> dnidList = dnidListDao.getDNIDList(pluginName);
			if(changed(activeDnidList, dnidList)) {
				dnidListDao.removeByPluginName(pluginName);
				for(String terminalDnid : activeDnidList) {
					DNIDList dnid = new DNIDList();
					dnid.setDNID(terminalDnid);
					dnid.setPluginName(pluginName);
					dnid.setUpdateTime(DateUtils.getNowDateUTC());
					dnid.setUpdateUser(MobileTerminalConstants.UPDATE_USER);
					dnidListDao.create(dnid);
				}
				return true;
			}
		} catch (ConfigDaoException e) {
			LOG.error("Couldn't use DNID List {} {}",pluginName,e);
		}
		return false;
	}

	/**
	 * Creates a map containing the available plugins for each mobile terminal type.
	 * 
	 * Mobile terminal types will only be included if at least one plugin exists
	 * for it. Plugins with no type (satellite type) are not included.
	 * 
	 * @return a map from mobile terminal type, to list of plugins
	 * @throws ConfigDaoException if unable to fetch original plugin list
	 */
	private Map<MobileTerminalTypeEnum, List<MobileTerminalPlugin>> getPlugins() throws ConfigDaoException {
        Map<MobileTerminalTypeEnum, List<MobileTerminalPlugin>> plugins = new HashMap<>();
        for (MobileTerminalPlugin plugin : mobileTerminalPluginDao.getPluginList()) {
            MobileTerminalTypeEnum mobileTerminalType = MobileTerminalTypeEnum.getType(plugin.getPluginSatelliteType());
            if (mobileTerminalType == null) {
                continue;
            }

            List<MobileTerminalPlugin> typePlugins = plugins.get(mobileTerminalType);
            if (typePlugins == null) {
                typePlugins = new ArrayList<>();
                plugins.put(mobileTerminalType, typePlugins);
            }

            typePlugins.add(plugin);
        }

        return plugins;
	}

	private boolean changed(List<String> activeDnidList, List<DNIDList> existingDNIDList) {
		if(activeDnidList.isEmpty() && existingDNIDList.isEmpty()) {
			return false;
		}
		Set<String> activeDnidSet = new HashSet<>(activeDnidList);
		Set<String> entityDnidSet = new HashSet<>();
		for(DNIDList entity : existingDNIDList) {
			entityDnidSet.add(entity.getDNID());
		}
		if(activeDnidSet.size() != entityDnidSet.size()) return true;

		for(String activeDnid : activeDnidSet) {
			if(!entityDnidSet.contains(activeDnid)) {
				return true;
			}
		}

		for(String entityDnid : entityDnidSet) {
			if(!activeDnidSet.contains(entityDnid)) {
				return true;
			}
		}
		return false;
	}

	private List<String> getPollTimeSpan() {
		List<String> list = new ArrayList<>();
		list.add("Today");
		return list;
	}

	private List<String> getTransponders() {
		List<String> list = new ArrayList<>();
		for(MobileTerminalTypeEnum transponder : MobileTerminalTypeEnum.values()) {
			list.add(transponder.name());
		}
		return list;
	}

	private List<String> getPollTypes() {
		List<String> list = new ArrayList<>();
		for(PollTypeEnum type : PollTypeEnum.values()) {
			list.add(type.name());
		}
		return list;
	}
}
