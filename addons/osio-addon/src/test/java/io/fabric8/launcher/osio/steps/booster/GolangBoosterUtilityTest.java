package io.fabric8.launcher.osio.steps.booster;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.Test;

import com.google.common.io.Resources;

public class GolangBoosterUtilityTest {

	@Test
	public void testReplaceContent() {
		List<String> diddleDiddle = new ArrayList<>();
		diddleDiddle.addAll(Arrays.asList("Hey, diddle, diddle", "The cat and the fiddle",
				"The cow jumped over the moon", "The little dog laughed", "To see such sport",
				"And the dish ran away with the spoon."));
		assertTrue(
				GolangBoosterUtility.replaceContent("the cow jumped over the moon", diddleDiddle, 3, "cow", "monkey"));
	}

	@Test
	public void testFileExtension() {
		assertEquals("Result", ".go",
				GolangBoosterUtility.getFileExtension("test.go"));

		assertEquals("Result", "",
				GolangBoosterUtility.getFileExtension("test"));

		assertEquals("Result", "",
				GolangBoosterUtility.getFileExtension(""));
	}

	@Test
	public void testFileContents() {
		List<String> contents =
				GolangBoosterUtility.getFileContents(new File(Resources.getResource("io/fabric8/launcher/osio/steps/booster/example.go").getPath()));

		assertEquals("Result", "package main", contents.get(0));
		assertEquals("Result", "", contents.get(1));
		assertEquals("Result", "import (", contents.get(2));
	}
}
