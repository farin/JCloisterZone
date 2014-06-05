package com.jcloisterzone.ui.grid.layer;

import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.awt.Graphics2D;
import java.awt.Image;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.jcloisterzone.action.TilePlacementAction;
import com.jcloisterzone.board.Position;
import com.jcloisterzone.board.Rotation;
import com.jcloisterzone.board.Tile;
import com.jcloisterzone.board.TileSymmetry;
import com.jcloisterzone.ui.grid.GridPanel;
import java.util.TreeSet;

public class TilePlacementLayerTest {
	@Mock
	GridPanel mGridPanel;
	@Mock
	TilePlacementAction mAction;
	@Mock
	Graphics2D mG2d;
	@Mock
	Image mIcon;
	@Mock
	Tile mTile;
	@Mock
	Position previewPosition;

	@Before
	public void initMocks() {
		MockitoAnnotations.initMocks(this);
	}
	
	public void testRotations(Rotation tileRotation, Set<Rotation> allowedRotations, 
			Rotation expectedRotation, TileSymmetry tileSymmetry) {
		// Initialise rotation test data
		Map<Position, Set<Rotation>> actionPlacements = new HashMap<>();
		actionPlacements.put(previewPosition, allowedRotations);

		when(mAction.getTileRotation()).thenReturn(tileRotation);
		when(mAction.getAvailablePlacements()).thenReturn(actionPlacements);
		when(mAction.getTile()).thenReturn(mTile);
		when(mTile.getSymmetry()).thenReturn(tileSymmetry);

		// Spy the layer test object in order to call the drawPrevewIcon and verify
		// that the correct rotation is set by preparePreviewRotation and used 
		// when later calling the getAffineTransform method
		TilePlacementLayer layer = spy(new TilePlacementLayer(
				mock(GridPanel.class), mAction));
		layer.drawPreviewIcon(mG2d, mIcon, previewPosition);
		verify(layer).getAffineTransform(anyInt(), eq(previewPosition),
				eq(expectedRotation));
	}

	@Test
	public void testDrawPreviewIcon_currentRotationIsAllowed() {
		Set<Rotation> allowedRotations = new TreeSet<>(Arrays.asList(new Rotation[]{Rotation.R0}));
		testRotations(Rotation.R0, allowedRotations, Rotation.R0, TileSymmetry.NONE);
	}
	
	@Test
	public void testDrawPreviewIcon_singleAllowedRotation() {
		Set<Rotation> allowedRotations = new TreeSet<>(Arrays.asList(new Rotation[]{Rotation.R90}));
		testRotations(Rotation.R0, allowedRotations, Rotation.R90, TileSymmetry.NONE);
	}
	
	@Test
	public void testDrawPreviewIcon_symmetricTile() {
		Set<Rotation> allowedRotations = new TreeSet<>(Arrays.asList(new Rotation[]{Rotation.R90}));
		testRotations(Rotation.R0, allowedRotations, Rotation.R90, TileSymmetry.S2);
	}
	
	@Test
	public void testDrawPreviewIcon_multipleAllowedRotations() {
		Set<Rotation> allowedRotations = new TreeSet<>(Arrays.asList(new Rotation[]{Rotation.R90, Rotation.R180}));
		testRotations(Rotation.R0, allowedRotations, Rotation.R90, TileSymmetry.NONE);
	}

	/**
	 * This is not a valid scenario in the application, since it is verified that the tile
	 * has allowed rotations before calling the drawPreviewIcon method, however we still
	 * test that if that were the case the preparePreviewRotation will return the initial rotation
	 * and not throw an exception.
	 */
	@Test
	public void testDrawPreviewIcon_noAllowedRotations() {
		Set<Rotation> allowedRotations = new TreeSet<>();
		testRotations(Rotation.R0, allowedRotations, Rotation.R0, TileSymmetry.NONE);
	}
}
