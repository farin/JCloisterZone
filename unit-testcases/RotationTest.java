package com.jcloisterzone.board;

import static org.junit.Assert.*;

import java.awt.geom.AffineTransform;

import org.junit.Test;

public class RotationTest {

	@Test
	public void testgetAffineTransform() {
		
		 Rotation r = Rotation.R0;
		 System.out.println(r);
		// AffineTransform afObj = new AffineTransform([[1.0, 0.0, 0.0], [0.0, 1.0, 0.0]]);
		 //float arr[] = {[1.0, 0.0, 0.0],[0.0, 1.0, 0.0]]};
		 String expected = "AffineTransform[[1.0, 0.0, 0.0], [0.0, 1.0, 0.0]]";
		 assertEquals(expected,r.getAffineTransform(5).toString());
		// Rotation exp = new AffineTransform();
		 
		 //assertEquals(new AffineTransform(0,-5),r.getAffineTransform(5));
		}
	
	@Test
	public void testNext() {
		Rotation r = Rotation.R0;
		assertEquals(Rotation.R90, r.next());
	}

	@Test
	public void testPrev() {
		Rotation r = Rotation.R0;
		assertEquals(Rotation.R270, r.prev());
	}
	
	@Test
	public void testGetTheta() {
		Rotation r = Rotation.R90;
		assertEquals(1, (int) r.getTheta());
	}
	
	@Test
	public void testAdd() {
		Rotation r = Rotation.R90;
		assertEquals(Rotation.R0, r.add(Rotation.R270));
	}
	
	@Test
	public void testInverse() {
		Rotation r = Rotation.R90;
		assertEquals(Rotation.R270, r.inverse());
		r = Rotation.R0;
		assertEquals(Rotation.R0, r.inverse());
		r = Rotation.R180;
		assertEquals(Rotation.R180, r.inverse());
		
	}
		
}
