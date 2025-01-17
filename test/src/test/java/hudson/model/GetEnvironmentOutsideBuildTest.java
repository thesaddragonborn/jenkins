package hudson.model;

import java.io.IOException;

import hudson.EnvVars;
import hudson.matrix.MatrixBuild;
import hudson.matrix.MatrixProject;
import hudson.util.StreamTaskListener;

import jenkins.model.Jenkins;

import org.jvnet.hudson.test.Issue;
import org.jvnet.hudson.test.HudsonTestCase;

/**
 * Tests that getEnvironment() calls outside of builds are safe.
 *
 * @author kutzi
 */
@Issue("JENKINS-11592")
public class GetEnvironmentOutsideBuildTest extends HudsonTestCase {

    private int oldExecNum;

    @Override
    protected void runTest() throws Throwable {
        // Disable tests

        // It's unfortunately not working, yet, as whenJenkinsMasterHasNoExecutors is not working as expected
    }

    @Override
    public void setUp() throws Exception {
        super.setUp();

        this.oldExecNum = Jenkins.get().getNumExecutors();
    }

    @Override
    public void tearDown() throws Exception {
        restoreOldNumExecutors();
        super.tearDown();
    }

    private void restoreOldNumExecutors() throws IOException {
        Jenkins.get().setNumExecutors(this.oldExecNum);
        assertNotNull(Jenkins.get().toComputer());
    }

    private void whenJenkinsMasterHasNoExecutors() throws IOException {
        Jenkins.get().setNumExecutors(0);
        assertNull(Jenkins.get().toComputer());
    }

    public void testFreestyle() throws Exception {
        FreeStyleProject project = createFreeStyleProject();

        final FreeStyleBuild build = buildAndAssertSuccess(project);

        assertGetEnvironmentWorks(build);
    }

    public void testMatrix() throws Exception {
        MatrixProject createMatrixProject = jenkins.createProject(MatrixProject.class, "mp");

        final MatrixBuild build = buildAndAssertSuccess(createMatrixProject);

        assertGetEnvironmentWorks(build);
    }

    @SuppressWarnings("rawtypes")
    private void assertGetEnvironmentWorks(Run build) throws IOException, InterruptedException {
        whenJenkinsMasterHasNoExecutors();
        // and getEnvironment is called outside of build
        EnvVars envVars =  build.getEnvironment(StreamTaskListener.fromStdout());
        // then it should still succeed - i.e. no NPE o.s.l.t.
        assertNotNull(envVars);
    }
}
