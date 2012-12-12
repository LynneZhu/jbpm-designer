package org.jbpm.designer.web.server;

import org.jbpm.designer.helper.TestHttpServletRequest;
import org.jbpm.designer.helper.TestHttpServletResponse;
import org.jbpm.designer.helper.TestServletConfig;
import org.jbpm.designer.helper.TestServletContext;
import org.jbpm.designer.repository.Asset;
import org.jbpm.designer.repository.AssetBuilderFactory;
import org.jbpm.designer.repository.Repository;
import org.jbpm.designer.repository.filters.FilterByExtension;
import org.jbpm.designer.repository.impl.AssetBuilder;
import org.jbpm.designer.repository.vfs.VFSRepository;
import org.jbpm.designer.web.profile.impl.JbpmProfileImpl;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * Note this test relies on external service repository: http://people.redhat.com/tsurdilo/repository/
 * so it will fail when service repository is not accessible
 */
public class JbpmServiceRepositoryServletTest {
    private static final String REPOSITORY_ROOT = System.getProperty("java.io.tmpdir")+"designer-repo";
    private static final String VFS_REPOSITORY_ROOT = "default://" + REPOSITORY_ROOT;
    private JbpmProfileImpl profile;

    @Before
    public void setup() {
        new File(REPOSITORY_ROOT).mkdir();
        profile = new JbpmProfileImpl();
        profile.setRepositoryId("vfs");
        profile.setRepositoryRoot(VFS_REPOSITORY_ROOT);
        profile.setREpositoryGlobalDir("/global");
    }

    private void deleteFiles(File directory) {
        for (File file : directory.listFiles()) {
            if (file.isDirectory()) {
                deleteFiles(file);
            }
            file.delete();
        }
    }

    @After
    public void teardown() {
        File repo = new File(REPOSITORY_ROOT);
        if(repo.exists()) {
            deleteFiles(repo);
        }
        repo.delete();
    }

    @Test
    public void testJbpmServiceRepositoryServlet() throws Exception {

        Repository repository = new VFSRepository(profile);

        // setup parameters
        Map<String, String> params = new HashMap<String, String>();
        params.put("repourl", "http://people.redhat.com/tsurdilo/repository/");
        params.put("asset", "Rewardsystem");
        params.put("profile", "jbpm");
        params.put("category", "Notifications");
        params.put("action", "install");

        JbpmServiceRepositoryServlet jbpmServiceRepositoryServlet = new JbpmServiceRepositoryServlet();
        jbpmServiceRepositoryServlet.setProfile(profile);

        jbpmServiceRepositoryServlet.init(new TestServletConfig(new TestServletContext(repository)));

        jbpmServiceRepositoryServlet.doPost(new TestHttpServletRequest(params), new TestHttpServletResponse());

        Collection<Asset> serviceAssets = repository.listAssets("/global");
        assertNotNull(serviceAssets);
        assertEquals(2, serviceAssets.size());

        Iterator<Asset> it = serviceAssets.iterator();

        assertEquals("reward.png", it.next().getFullName());
        assertEquals("Rewardsystem.wid", it.next().getFullName());

        Asset<String> form = repository.loadAsset(serviceAssets.iterator().next().getUniqueId());
        assertNotNull(form.getAssetContent());
    }
}
