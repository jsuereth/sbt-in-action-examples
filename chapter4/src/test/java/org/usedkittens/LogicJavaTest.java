package org.usedkittens;

import org.junit.Assert.*;
import org.junit.*;
import scala.collection.immutable.*;

public class LogicJavaTest {
	@Test
	public void testKitten() {
        Kitten kitten = new Kitten("id", new HashSet());
		Assert.assertEquals(1, kitten.attributes().size());
	}
}