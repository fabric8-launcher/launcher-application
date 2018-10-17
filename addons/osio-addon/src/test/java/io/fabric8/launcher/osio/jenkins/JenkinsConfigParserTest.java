package io.fabric8.launcher.osio.jenkins;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;

import org.apache.commons.io.IOUtils;
import org.junit.Test;

import static org.junit.Assert.assertTrue;

public class JenkinsConfigParserTest {

    private static final String REPOSITORY_NAME = "testRepo";

    @Test
    public void setGithubOwner() throws IOException {
        //given
        String gitOwner = "testOwnerName";
        String xml = parseTestXml("example-jenkins-config.xml");
        JenkinsConfigParser parser = new JenkinsConfigParser(xml);

        //when
        parser.setGithubOwner(gitOwner);
        String resultXml = parser.toXml();

        //then
        assertTrue("resultXml should be updated", resultXml.contains(gitOwner));
    }

    @Test
    public void setRepository() throws IOException {
        //when
        String resultXml = transformXml("example-jenkins-config.xml");

        //then
        assertTrue("resultXml should be updated", resultXml.contains(REPOSITORY_NAME));
    }

    @Test
    public void setRepositoryMissingTraits() throws IOException {
        //when
        String resultXml = transformXml("missing-traits-jenkins-config.xml");

        //then
        assertTrue("resultXml should be updated", resultXml.contains("oldName|" + REPOSITORY_NAME));
    }

    @Test
    public void setRepositoryNoOldRepoName() throws IOException {
        //when
        String resultXml = transformXml("no-oldname-jenkins-config.xml");

        //then
        assertTrue("resultXml should be updated", resultXml.contains(">" + REPOSITORY_NAME));
    }

    @Test
    public void setEmptyXml() {
        //given
        String gitOwner = "testOwnerName";
        JenkinsConfigParser parser = new JenkinsConfigParser("");

        //when
        parser.setGithubOwner(gitOwner);
        String resultXml = parser.toXml();

        //then
        assertTrue("resultXml should be updated", resultXml.contains(gitOwner));
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldThrowExceptionIllegalXml() {
        //given
        JenkinsConfigParser parser = new JenkinsConfigParser("notXml");

        //then
        parser.setRepository("dummy");
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldThrowExceptionIllegalMissingTags() {
        //given
        JenkinsConfigParser parser = new JenkinsConfigParser("<app><org.jenkinsci.plugins.github__branch__source.GitHubSCMNavigator/></app>");

        //then
        parser.setRepository("dummy");
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldThrowExceptionIllegalMissingTag() {
        //given
        JenkinsConfigParser parser = new JenkinsConfigParser("<app/>");

        //then
        parser.setRepository("dummy");
    }

    private String transformXml(String templateName) throws IOException {
        String xml = parseTestXml(templateName);
        JenkinsConfigParser parser = new JenkinsConfigParser(xml);

        parser.setRepository(REPOSITORY_NAME);
        return parser.toXml();
    }

    private String parseTestXml(String exampleXmlFilename) throws IOException {
        try (InputStream resource = getClass().getResourceAsStream("/" + exampleXmlFilename)) {
            return IOUtils.toString(resource, Charset.defaultCharset());
        }
    }
}