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
package eu.europa.ec.fisheries.uvms.mobileterminal.search.poll;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import eu.europa.ec.fisheries.schema.mobileterminal.polltypes.v1.ListCriteria;
import eu.europa.ec.fisheries.schema.mobileterminal.polltypes.v1.SearchKey;
import eu.europa.ec.fisheries.uvms.mobileterminal.entity.Channel;
import eu.europa.ec.fisheries.uvms.mobileterminal.mapper.exception.SearchMapperException;
import eu.europa.ec.fisheries.uvms.mobileterminal.search.PollSearchField;
import eu.europa.ec.fisheries.uvms.mobileterminal.search.PollSearchKeyValue;
import eu.europa.ec.fisheries.uvms.mobileterminal.search.SearchTable;

public class PollSearchMapper {

	public static List<PollSearchKeyValue> createSearchFields(List<ListCriteria> criterias) throws SearchMapperException {
		Map<PollSearchField, PollSearchKeyValue> searchKeyValues = new HashMap<>();
        for (ListCriteria criteria : criterias) {
        	PollSearchKeyValue keyValue = mapSearchKey(criteria, searchKeyValues);
            searchKeyValues.put(keyValue.getSearchField(), keyValue);
        }
        return new ArrayList<PollSearchKeyValue>(searchKeyValues.values());
	}
	
    private static PollSearchKeyValue mapSearchKey(ListCriteria criteria, Map<PollSearchField, PollSearchKeyValue> searchKeys) throws SearchMapperException {
        if (criteria == null || criteria.getKey() == null || criteria.getValue() == null) {
            throw new SearchMapperException("Non valid search criteria");
        }

        PollSearchKeyValue searchKeyValue = getSearchKeyValue(getSearchField(criteria.getKey()), searchKeys);
        searchKeyValue.getValues().add(criteria.getValue());
        return searchKeyValue;
    }
	
    private static PollSearchKeyValue getSearchKeyValue(PollSearchField field, Map<PollSearchField, PollSearchKeyValue> searchKeys) {
    	PollSearchKeyValue searchKeyValue = searchKeys.get(field);
        if (searchKeyValue == null) {
            searchKeyValue = new PollSearchKeyValue();
        }
        searchKeyValue.setSearchField(field);
        return searchKeyValue;
    }
    
	private static PollSearchField getSearchField(SearchKey key) throws SearchMapperException {
		switch(key) {
		case CONNECT_ID:
			return PollSearchField.CONNECT_ID;
		case POLL_ID:
			return PollSearchField.POLL_ID;
		case POLL_TYPE:
			return PollSearchField.POLL_TYPE;
		case TERMINAL_TYPE:
			return PollSearchField.TERMINAL_TYPE;
		case USER:
			return PollSearchField.USER;
		default:
			throw new SearchMapperException("No searchKey " + key.name());
		}
	}

	private static String createSearchSql(List<PollSearchKeyValue> searchKeys, boolean isDynamic) {
		StringBuilder builder = new StringBuilder();
		String OPERATOR = " OR ";
		if(isDynamic) {
			OPERATOR = " AND ";
		}
		
		builder.append(" INNER JOIN p.pollBase pb ");
		builder.append(" INNER JOIN pb.mobileterminal mt ");
		
		if(!searchKeys.isEmpty()) {
			builder.append(" WHERE ");
			boolean first = true;
			for(PollSearchKeyValue keyValue : searchKeys) {
				if(first) {
					first = false;
				} else {
					builder.append(OPERATOR);
				}
				builder.append(keyValue.getSearchField().getTable().getTableAlias()).append(".").append(keyValue.getSearchField().getSqlColumnName());
				builder.append(" IN (:").append(keyValue.getSearchField().getSqlReplaceToken()).append(") ");
			}
		}
		
		return builder.toString();
	}
	
	public static String createCountSearchSql(List<PollSearchKeyValue> searchKeys, boolean isDynamic) {
		StringBuilder builder = new StringBuilder();
		builder.append("SELECT COUNT (DISTINCT p) FROM Poll p ");
		builder.append(createSearchSql(searchKeys, isDynamic));
		return builder.toString();
	}

	public static String createSelectSearchSql(List<PollSearchKeyValue> searchKeys, boolean isDynamic) {
		StringBuilder builder = new StringBuilder();
		builder.append("SELECT DISTINCT p FROM Poll p ");
		builder.append(createSearchSql(searchKeys, isDynamic));
		return builder.toString();
	}
	
	public static String createPollableSearchSql(List<String> idList) {
		StringBuilder builder = new StringBuilder();
		builder.append("SELECT DISTINCT c FROM Channel c");
		builder.append(" INNER JOIN FETCH c.mobileTerminal mt");
		builder.append(" INNER JOIN FETCH mt.mobileTerminalEvents me");
		builder.append(" INNER JOIN FETCH me.pollChannel pc");
		builder.append(" INNER JOIN FETCH mt.plugin p ");
		builder.append(" INNER JOIN FETCH p.capabilities cap ");
		builder.append(" WHERE ");
		builder.append(" c.guid = pc.guid ");
		builder.append(" AND me.active = true ");
		builder.append(" AND mt.archived = '0' AND mt.inactivated = '0' AND p.pluginInactive = '0' ");
		builder.append(" AND (cap.name = 'POLLABLE' AND UPPER(cap.value) = 'TRUE' ) ");
		builder.append(" AND (me.connectId is not null) ");
		if( idList != null && !idList.isEmpty()) {
			builder.append(" AND me.connectId IN :idList");
		}
		builder.append(" ORDER BY c.guid DESC ");
		return builder.toString();
	}
}