package eu.europa.ec.fisheries.uvms.mobileterminal.arquillian;

import org.jboss.arquillian.container.test.api.OperateOnDeployment;
import org.jboss.arquillian.junit.Arquillian;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Created by thofan on 2017-04-27.
 */

@RunWith(Arquillian.class)
public class TestTestRemoveMe  extends BuildMobileTerminalDeployment {




    @Test
    @OperateOnDeployment("normal")
    public void test() {

        double longitude = 9.140626D;
        double latitude = 57.683805D;

    }



}