package eu.europa.fisheries.uvms.mobileterminal.service.arquillian;

import java.util.List;

import javax.ejb.EJB;

import eu.europa.ec.fisheries.uvms.mobileterminal.dao.bean.OceanRegionDaoBean;
import org.jboss.arquillian.container.test.api.OperateOnDeployment;
import org.jboss.arquillian.junit.Arquillian;
import org.junit.Test;
import org.junit.runner.RunWith;

import eu.europa.ec.fisheries.uvms.mobileterminal.dao.exception.ConfigDaoException;
import eu.europa.ec.fisheries.uvms.mobileterminal.entity.OceanRegion;

import static org.junit.Assert.*;

@RunWith(Arquillian.class)
public class OceanRegionDaoBeanIntTest extends TransactionalTests {

    @EJB
    private OceanRegionDaoBean oceanRegionDao;

    @Test
    @OperateOnDeployment("normal")
    public void testGetOceanRegionList() throws ConfigDaoException {
        // Since we have at least 4 regions inserted by LIQUIBASE this should always work
        List<OceanRegion> oceanRegions = oceanRegionDao.getOceanRegionList();
        assertNotNull(oceanRegions);
        assertTrue(oceanRegions.size() > 3);
    }
}