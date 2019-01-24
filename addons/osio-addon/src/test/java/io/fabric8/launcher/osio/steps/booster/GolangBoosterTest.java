package io.fabric8.launcher.osio.steps.booster;

import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Test;

import com.google.common.io.Resources;

import io.fabric8.launcher.booster.catalog.rhoar.Mission;
import io.fabric8.launcher.booster.catalog.rhoar.RhoarBooster;
import io.fabric8.launcher.booster.catalog.rhoar.Runtime;

public class GolangBoosterTest {

	@Test
	public void testGolangBooster() {
        // Mock a RhoarBooster
        RhoarBooster booster = mock(RhoarBooster.class);
        when(booster.getMission()).thenReturn(new Mission("http"));
        when(booster.getRuntime()).thenReturn(new Runtime("golang"));

        File file = new File(Resources.getResource("io/fabric8/launcher/osio/steps/booster").getPath());

		GolangBooster goBooster = new GolangBooster(file.toPath(), setupBoosterTestData(), "testorgreplacement", "testprojectnamereplacement");
		Map<File, String> response = goBooster.customize();

		String content = response.get(new File(Resources.getResource("io/fabric8/launcher/osio/steps/booster/example.go").getPath()));
		List<String> fileContents  = GolangBoosterUtility.getFileContents(new File(Resources.getResource("io/fabric8/launcher/osio/steps/booster/example.go").getPath()));
		String line = fileContents.get(4);
		line = line.replace("golang-starters", "testorgreplacement");
		line = line.replaceAll("golang-rest-http", "testprojectnamereplacement");
		assertTrue(content.contains(line));
		// Ensure that only imports within the import block are converted.
		line = fileContents.get(8);
		assertTrue(content.contains(line));
	}

	private static Map<String, Object> setupBoosterTestData() {
		Map<String, String> url = new HashMap<>();
		Map<String, Object> git = new HashMap<>();
		Map<String, Object> source = new HashMap<>();
		url.put("url", "https://github.com/golang-starters/golang-rest-http");
		git.put("git", url);
		source.put("source", git);

		return source;
	}
}
