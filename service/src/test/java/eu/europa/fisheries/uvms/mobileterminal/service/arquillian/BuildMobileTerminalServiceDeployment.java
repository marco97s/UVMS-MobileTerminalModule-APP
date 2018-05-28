package eu.europa.fisheries.uvms.mobileterminal.service.arquillian;

import eu.europa.ec.fisheries.schema.mobileterminal.polltypes.v1.PollRequestType;
import eu.europa.ec.fisheries.schema.mobileterminal.types.v1.PluginService;
import eu.europa.ec.fisheries.uvms.config.exception.ConfigServiceException;
import eu.europa.ec.fisheries.uvms.mobileterminal.dao.bean.MobileTerminalPluginDaoBean;
import eu.europa.ec.fisheries.uvms.mobileterminal.dao.bean.PollDaoBean;
import eu.europa.ec.fisheries.uvms.mobileterminal.dao.bean.PollProgramDaoBean;
import eu.europa.ec.fisheries.uvms.mobileterminal.dao.bean.TerminalDaoBean;
import eu.europa.ec.fisheries.uvms.mobileterminal.dto.CreatePollResultDto;
import eu.europa.ec.fisheries.uvms.mobileterminal.entity.MobileTerminal;
import eu.europa.ec.fisheries.uvms.mobileterminal.entity.poll.PollProgram;
import eu.europa.ec.fisheries.uvms.mobileterminal.entity.types.MobileTerminalTypeEnum;
import eu.europa.ec.fisheries.uvms.mobileterminal.mapper.AuditModuleRequestMapper;
import eu.europa.ec.fisheries.uvms.mobileterminal.mapper.PollMapper;
import eu.europa.ec.fisheries.uvms.mobileterminal.mapper.PollToCommandRequestMapper;
import eu.europa.ec.fisheries.uvms.mobileterminal.message.exception.MobileTerminalMessageException;
import eu.europa.ec.fisheries.uvms.mobileterminal.model.exception.MobileTerminalException;
import eu.europa.ec.fisheries.uvms.mobileterminal.model.exception.MobileTerminalModelException;
import eu.europa.ec.fisheries.uvms.mobileterminal.model.exception.MobileTerminalModelMapperException;
import eu.europa.ec.fisheries.uvms.mobileterminal.model.exception.MobileTerminalUnmarshallException;
import eu.europa.ec.fisheries.uvms.mobileterminal.search.PollSearchKeyValue;
import eu.europa.ec.fisheries.uvms.mobileterminal.search.poll.PollSearchMapper;
import eu.europa.ec.fisheries.uvms.mobileterminal.service.bean.*;
import eu.europa.ec.fisheries.uvms.mobileterminal.service.exception.MobileTerminalServiceException;
import eu.europa.fisheries.uvms.mobileterminal.service.arquillian.helper.TestPollHelper;
import org.eu.ingwar.tools.arquillian.extension.suite.annotations.ArquillianSuiteDeployment;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.shrinkwrap.resolver.api.maven.Maven;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

@ArquillianSuiteDeployment
public abstract class BuildMobileTerminalServiceDeployment {

    private final static Logger LOG = LoggerFactory.getLogger(BuildMobileTerminalServiceDeployment.class);

    @Deployment(name = "normal", order = 1)
    public static Archive<?> createDeployment() {

        // Import Maven runtime dependencies
        File[] files = Maven.configureResolver().loadPomFromFile("pom.xml")
                .importRuntimeAndTestDependencies().resolve().withTransitivity().asFile();

        // Embedding war package which contains the test class is needed
        // So that Arquillian can invoke test class through its servlet test runner
        WebArchive testWar = ShrinkWrap.create(WebArchive.class, "test.war");
        testWar.addPackages(true, "com.tocea.easycoverage.framework.api");
        testWar.addPackages(true, "eu.europa.fisheries.uvms.mobileterminal.service");
        testWar.addPackages(true,"eu.europa.ec.fisheries.uvms.mobileterminal.service");
        testWar.addPackages(true,"eu.europa.ec.fisheries.uvms.mobileterminal.dto");
        //testWar.addPackages(true,"eu.europa.ec.fisheries.uvms.mobileterminal.dao.exception");


        testWar.addClass(TransactionalTests.class);
        testWar.addClass(TestPollHelper.class);
        testWar.addClass(ConfigServiceBean.class);
        testWar.addClass(MobileTerminalMessageException.class);
        testWar.addClass(MobileTerminalConfigHelper.class);

        // för Mapped PollServiceBean
        testWar.addClass(MappedPollServiceBean.class);
        testWar.addClass(PollRequestType.class);
        testWar.addClass(CreatePollResultDto.class);
        testWar.addClass(MobileTerminalServiceException.class);
        testWar.addClass(PollServiceBean.class);
        testWar.addClass(MobileTerminalServiceBean.class);
        testWar.addClass(PollToCommandRequestMapper.class);
        testWar.addClass(PollMapper.class);
        testWar.addClass(AuditModuleRequestMapper.class);

        testWar.addClass(PluginService.class);
        testWar.addClass(MobileTerminalModelMapperException.class);
        testWar.addClass(MobileTerminalModelException.class);
        testWar.addClass(MobileTerminalException.class);
        testWar.addClass(MobileTerminalUnmarshallException.class);
        testWar.addClass(ConfigServiceException.class);

        // FROM DOMAIN MODULE
        testWar.addPackages(true, "eu.europa.ec.fisheries.uvms.mobileterminal.constant");
        testWar.addPackages(true, "eu.europa.ec.fisheries.uvms.mobileterminal.entity");
        testWar.addPackages(true, "eu.europa.ec.fisheries.uvms.mobileterminal.dao");
        testWar.addPackages(true, "eu.europa.ec.fisheries.uvms.mobileterminal.mapper");
        testWar.addPackages(true, "eu.europa.ec.fisheries.uvms.mobileterminal.exception");
        testWar.addPackages(true, "eu.europa.ec.fisheries.uvms.mobileterminal.model.exception");
        testWar.addPackages(true, "eu.europa.ec.fisheries.uvms.mobileterminal.util");
        testWar.addPackages(true, "eu.europa.ec.fisheries.uvms.mobileterminal.dto");
        testWar.addPackages(true, "eu.europa.ec.fisheries.uvms.mobileterminal.search");
        testWar.addPackages(true,"eu.europa.ec.fisheries.uvms.mobileterminal.model");
        testWar.addPackages(true,"eu.europa.ec.fisheries.uvms.mobileterminal.arquillian");
        testWar.addPackages(true,"eu.europa.ec.fisheries.uvms.mobileterminal.arquillian.bean");
        testWar.addPackages(true, "eu.europa.ec.fisheries.schema");

        testWar.addClass(TerminalDaoBean.class);
        testWar.addClass(MobileTerminal.class);
        testWar.addClass(MobileTerminalTypeEnum.class);
        testWar.addClass(MobileTerminalPluginDaoBean.class);
        testWar.addClass(PollDaoBean.class);
        testWar.addClass(PollSearchKeyValue.class);
        testWar.addClass(PollSearchMapper.class);
        testWar.addClass(PollProgramDaoBean.class);
        testWar.addClass(PollProgram.class);

        // Empty beans for EE6 CDI
        testWar.addAsWebInfResource(EmptyAsset.INSTANCE, "beans.xml");
        testWar.addAsResource("persistence-integration.xml", "META-INF/persistence.xml");

        testWar.addAsLibraries(files);
        return testWar;
    }

    private static void printFiles(File[] files) {

        List<File> filesSorted = new ArrayList<>();
        Collections.addAll(filesSorted, files);

        Collections.sort(filesSorted, new Comparator<File>() {
            @Override
            public int compare(File o1, File o2) {
                return o1.getName().compareTo(o2.getName());
            }
        });

        LOG.info("FROM POM - begin");
        for(File f : filesSorted){
            LOG.info("       --->>>   "   +   f.getName());
        }
        LOG.info("FROM POM - end");
    }
}
