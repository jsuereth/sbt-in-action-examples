package org.preownedkittens;

import org.junit.*;
import scala.collection.immutable.*;

public class LogicJavaTest {
	@Test
	public void testKitten() {
        Kitten kitten = new Kitten(1, new HashSet());
        // in chapter 5 we have Assert.assertEquals(1, kitten.attributes().size());
        // but as part of the chapter, we correct it - this test should pass
		Assert.assertEquals(0, kitten.attributes().size());
	}
}