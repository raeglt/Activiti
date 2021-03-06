package org.activiti.spring.test.autodeployment;

import java.util.List;

import org.activiti.engine.ActivitiException;
import org.activiti.spring.autodeployment.FailOnNoProcessAutoDeploymentStrategy;
import org.activiti.spring.impl.test.SpringActivitiTestCase;
import org.junit.Test;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.test.context.ContextConfiguration;

@ContextConfiguration("classpath:org/activiti/spring/test/autodeployment/errorHandling/spring-context.xml")
public class FailOnNoProcessAutoDeploymentStrategyTest extends SpringActivitiTestCase {

    private final String nameHint = "FailOnNoProcessAutoDeploymentStrategyTest";

    private final String validName1 = "org/activiti/spring/test/autodeployment/errorHandling/valid.bpmn20.xml";
    private final String invalidName1 = "org/activiti/spring/test/autodeployment/errorHandling/parsing-error.bpmn20.xml";
    private final String invalidName2 = "org/activiti/spring/test/autodeployment/errorHandling/validation-error.bpmn20.xml";

    private void cleanUp() {
        List<org.activiti.engine.repository.Deployment> deployments = repositoryService.createDeploymentQuery().list();
        for (org.activiti.engine.repository.Deployment deployment : deployments) {
            repositoryService.deleteDeployment(deployment.getId(), true);
        }
    }

    @Override
    public void setUp(){
        cleanUp();
    }

    @Override
    public void tearDown(){
        cleanUp();
    }

    @Test
    public void testValidResources() {
        final Resource[] resources = new Resource[]{new ClassPathResource(validName1)};
        FailOnNoProcessAutoDeploymentStrategy deploymentStrategy = new FailOnNoProcessAutoDeploymentStrategy();
        deploymentStrategy.deployResources(nameHint, resources, repositoryService);
        assertEquals(1, repositoryService.createDeploymentQuery().count());
    }

    @Test
    public void testInvalidResources() {
        final Resource[] resources = new Resource[]{new ClassPathResource(validName1), new ClassPathResource(invalidName1), new ClassPathResource(invalidName2)};
        FailOnNoProcessAutoDeploymentStrategy deploymentStrategy = new FailOnNoProcessAutoDeploymentStrategy();
        deploymentStrategy.deployResources(nameHint, resources, repositoryService);
        assertEquals(1, repositoryService.createDeploymentQuery().count());
    }

    @Test
    public void testWithParsingErrorResources() {
        final Resource[] resources = new Resource[]{new ClassPathResource(validName1), new ClassPathResource(invalidName1)};
        FailOnNoProcessAutoDeploymentStrategy deploymentStrategy = new FailOnNoProcessAutoDeploymentStrategy();
        deploymentStrategy.deployResources(nameHint, resources, repositoryService);
        assertEquals(1, repositoryService.createDeploymentQuery().count());
    }

    @Test
    public void testWithValidationErrorResources() {
        final Resource[] resources = new Resource[]{new ClassPathResource(validName1), new ClassPathResource(invalidName2)};
        FailOnNoProcessAutoDeploymentStrategy deploymentStrategy = new FailOnNoProcessAutoDeploymentStrategy();
        deploymentStrategy.deployResources(nameHint, resources, repositoryService);
        assertEquals(1, repositoryService.createDeploymentQuery().count());
    }

    @Test
    public void testOnlyInvalidResources() {
        final Resource[] resources = new Resource[]{new ClassPathResource(invalidName1)};
        FailOnNoProcessAutoDeploymentStrategy deploymentStrategy = new FailOnNoProcessAutoDeploymentStrategy();
        try {
            deploymentStrategy.deployResources(nameHint, resources, repositoryService);
        } catch (ActivitiException e) {
            assertEquals("No process definition was deployed.", e.getMessage());
            assertEquals(0, repositoryService.createDeploymentQuery().count());
            return;
        }
        fail();
    }
}