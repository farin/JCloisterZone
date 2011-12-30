package com.jcloisterzone.ui.legacy;

import java.awt.geom.Area;
import java.util.Map;

import com.google.common.collect.ImmutableMap;
import com.jcloisterzone.board.Location;
import com.jcloisterzone.board.Rotation;
import com.jcloisterzone.board.Tile;
import com.jcloisterzone.feature.City;
import com.jcloisterzone.feature.Cloister;
import com.jcloisterzone.feature.Farm;
import com.jcloisterzone.feature.Feature;
import com.jcloisterzone.feature.Road;
import com.jcloisterzone.feature.Tower;
import com.jcloisterzone.ui.ImmutablePoint;
import com.jcloisterzone.ui.theme.TileTheme;


/**
 * Placement coordinates for figures placed on tiles.
 * TODO should be defined as xml file for each theme
 *
 * @author Roman Krejcik
 */
@Deprecated
public class FigurePositionProvider {

	private final TileTheme theme;

	/** farm mapping for directions */
	private ImmutablePoint map[] = new ImmutablePoint[256];

	private static final int CENTER = 50;
	private static final int NEAR_CENTER = 35;
	private static final int QUARTER = 25;
	private static final int BORDER = 18;
	private static final int TINY_BORDER = 15;

	private static final int ROAD_DIST = 20;
	private static final int MAX = 100;

	private static final int FIG_ROAD_PLACE = 20;
	private static final int FIG_QBLIQUE_ROAD_PLACE = 28;

	private final Map<String,ImmutablePoint> specialPlacements;

	public static final ImmutablePoint P_CENTER = new ImmutablePoint(CENTER,CENTER);
	public static final ImmutablePoint P_QUARTER = new ImmutablePoint(QUARTER,QUARTER);

	public static final ImmutablePoint P_CITY_N = new ImmutablePoint(CENTER, BORDER);
	public static final ImmutablePoint P_CITY_NW = new ImmutablePoint(37, 20);
	public static final ImmutablePoint P_CITY_NOT_N = new ImmutablePoint(CENTER, MAX - 20);

	public static final ImmutablePoint P_OBLIQUE_NW = new ImmutablePoint(FIG_QBLIQUE_ROAD_PLACE, FIG_QBLIQUE_ROAD_PLACE);
	public static final ImmutablePoint P_OBLIQUE_NE = new ImmutablePoint(100-FIG_QBLIQUE_ROAD_PLACE, FIG_QBLIQUE_ROAD_PLACE);
	public static final ImmutablePoint P_OBLIQUE_SW = new ImmutablePoint(FIG_QBLIQUE_ROAD_PLACE, 100-FIG_QBLIQUE_ROAD_PLACE);
	public static final ImmutablePoint P_OBLIQUE_SE = new ImmutablePoint(100-FIG_QBLIQUE_ROAD_PLACE, 100-FIG_QBLIQUE_ROAD_PLACE);

	public FigurePositionProvider(TileTheme theme) {
		this.theme = theme;

		map[0] = P_CENTER;

		generateRotations(1,new ImmutablePoint(CENTER-ROAD_DIST,TINY_BORDER));
		generateRotations(2,map[1].mirrorX());
		generateRotations(3,new ImmutablePoint(CENTER, BORDER));
		generateRotations(6,new ImmutablePoint(MAX-BORDER, BORDER));
		generateRotations(7,new ImmutablePoint(MAX-QUARTER, QUARTER));
		generateRotations(14,new ImmutablePoint(MAX-QUARTER, CENTER));
		generateRotations(15,new ImmutablePoint(MAX-QUARTER, QUARTER));
		generateRotations(30,new ImmutablePoint(MAX-QUARTER, CENTER));
		generateRotations(31,new ImmutablePoint(MAX-QUARTER, CENTER));
		generateRotations(62,new ImmutablePoint(MAX-QUARTER, CENTER));
		generateRotations(63,new ImmutablePoint(MAX-QUARTER, CENTER));
		generateRotations(126,new ImmutablePoint(MAX-QUARTER, MAX-QUARTER));
		generateRotations(127,new ImmutablePoint(MAX-QUARTER, CENTER));
		map[102] = new ImmutablePoint(MAX-BORDER, BORDER);
		map[153] = map[102];

		generateRotations(38,new ImmutablePoint(MAX-BORDER, BORDER));
		generateRotations(145,map[38].mirrorX());
		generateRotations(72,new ImmutablePoint(MAX-BORDER, BORDER));
		generateRotations(137,map[72].mirrorX());

		generateRotations(132,new ImmutablePoint(MAX-BORDER, CENTER-ROAD_DIST));
		generateRotations(72,map[132].mirrorY());

		generateRotations(156,new ImmutablePoint(MAX-BORDER, MAX-QUARTER));
		generateRotations(228,map[156].mirrorX());

		generateRotations(36,new ImmutablePoint(MAX-BORDER,CENTER-ROAD_DIST));
		generateRotations(9,map[36].mirrorY());

		generateRotations(51, P_CENTER);
		map[106] = P_CENTER;
		map[153] = map[106];

		map[255] = null;

		specialPlacements = new ImmutableMap.Builder<String,ImmutablePoint>()
		//river
			.put("R1.LIRI CLOISTER CLOISTER", new ImmutablePoint(CENTER,30))
			.put("R1.LIRI FARM WR+N+EL", new ImmutablePoint(MAX-BORDER,BORDER))
			.put("R1.LIRI ROAD S", new ImmutablePoint(30,MAX-BORDER))
			.put("R1.IFI.1 FARM WR+N+EL", new ImmutablePoint(BORDER,BORDER))
			.put("R1.RIrI ROAD NS", new ImmutablePoint(CENTER,QUARTER))
		//river 2
			.put("R2.LIFI CLOISTER CLOISTER", new ImmutablePoint(CENTER,30))
			.put("R2.LIFI FARM WR+N+EL", new ImmutablePoint(MAX-BORDER,BORDER))
			.put("R2.IFC FARM ER+NR", new ImmutablePoint(BORDER, BORDER))
			.put("R2.RIrI ROAD WE", new ImmutablePoint(MAX-QUARTER,CENTER))
			.put("R2.RIrI FARM NR+EL", new ImmutablePoint(MAX-NEAR_CENTER,TINY_BORDER))
			.put("R2.CIcI CITY WE", new ImmutablePoint(BORDER, CENTER))
		//king and scount
			.put("KS.LC CLOISTER CLOISTER", new ImmutablePoint(CENTER,65))
			.put("KS.LC FARM _N", new ImmutablePoint(MAX-BORDER,MAX-BORDER))
			.put("KS.CcRR! ROAD E", new ImmutablePoint(MAX-TINY_BORDER,CENTER-10))
			.put("KS.CCcc CITY WE", new ImmutablePoint(BORDER, CENTER))
			.put("KS.CCcc CITY NS", new ImmutablePoint(CENTER, MAX-BORDER))
		//traders and builders
			.put("TB.CCc.w FARM CENTER", new ImmutablePoint(MAX-QUARTER, MAX-QUARTER))
			.put("TB.CCc.c FARM CENTER", new ImmutablePoint(MAX-QUARTER, MAX-QUARTER))
			.put("TB.LRRR FARM WR+N+EL", new ImmutablePoint(MAX-QUARTER, QUARTER))
			.put("TB.CcRR!.c CITY NW", new ImmutablePoint(NEAR_CENTER, NEAR_CENTER))
			.put("TB.CcR!.w CITY NW", new ImmutablePoint(NEAR_CENTER, NEAR_CENTER))
			.put("TB.RRrr ROAD WE", new ImmutablePoint(QUARTER, CENTER))
			.put("TB.RRrr ROAD NS", new ImmutablePoint(CENTER, QUARTER))
			.put("TB.RRC ROAD W", new ImmutablePoint(20, CENTER))
			.put("TB.RRC ROAD S", new ImmutablePoint(CENTER, MAX-20))
		//carhars
			.put("SI.RCr ROAD WE", new ImmutablePoint(50,75))
			.put("SI.RCr FARM WL+S+ER", new ImmutablePoint(BORDER,MAX-BORDER))
			.put("SI.CcRr ROAD SE", new ImmutablePoint(70,70))
		//dragon tiles
			.put("DG.CRcr.d ROAD NS", new ImmutablePoint(CENTER,BORDER))
			.put("DG.CRcr.d FARM N", new ImmutablePoint(QUARTER,BORDER))
			.put("DG.CRcr.d FARM S", new ImmutablePoint(QUARTER,MAX-BORDER))
			.put("DG.LCcc.d CLOISTER CLOISTER", new ImmutablePoint(18,33))
			.put("DG.CCc+.p FARM W", new ImmutablePoint(8,30))
			.put("DG.CCc+.p CITY N", new ImmutablePoint(30,20))
			.put("DG.LRRR.d FARM WR+N+EL", new ImmutablePoint(20, 20))
			.put("DG.LCcc.d CITY _S", new ImmutablePoint(MAX-QUARTER, QUARTER))
		//tower tiles
			.put("TO.L CLOISTER CLOISTER", new ImmutablePoint(30,70))
			.put("TO.L FARM NWSE", P_QUARTER)
			.put("TO.CcR! CITY NW", new ImmutablePoint(CENTER+5, CENTER+5))
			.put("TO.RrRr.2 FARM D102", new ImmutablePoint(BORDER, MAX-BORDER))
			.put("TO.C FARM _N", new ImmutablePoint(QUARTER,CENTER))
			.put("TO.Cc FARM SE", new ImmutablePoint(MAX-QUARTER,CENTER-5))
			.put("TO.CccC+ FARM CENTER", new ImmutablePoint(MAX-QUARTER, MAX-QUARTER))
			.put("TO.CC.2 FARM SE", new ImmutablePoint(NEAR_CENTER, MAX-QUARTER))
			.put("TO.CFR FARM D224", new ImmutablePoint(QUARTER,MAX-BORDER))
			.put("TO.CRcr ROAD NS", new ImmutablePoint(CENTER, MAX-QUARTER))
			.put("TO.CRcr CITY WE", new ImmutablePoint(MAX-BORDER, CENTER))
			.put("TO.RCr ROAD WE", new ImmutablePoint(MAX-QUARTER, CENTER))
			.put("TO.RCr FARM WL+S+ER", new ImmutablePoint(MAX-BORDER, MAX-QUARTER))
			.put("TO.CccR CITY _S", new ImmutablePoint(QUARTER, QUARTER))
		//abbey and mayor
			.put("AM.RrC ROAD SE", new ImmutablePoint(MAX-QUARTER, CENTER))
			.put("AM.CCcc+ CITY WE", new ImmutablePoint(MAX-QUARTER, CENTER))
			.put("AM.CCcc+ CITY NS", new ImmutablePoint(CENTER, QUARTER))
			.put("AM.CCc+ CITY WE", new ImmutablePoint(MAX-QUARTER, CENTER))
			.put("AM.CCc+ FARM S", new ImmutablePoint(MAX-NEAR_CENTER, MAX-BORDER))
			.put("AM.CRr ROAD SE", new ImmutablePoint(NEAR_CENTER, MAX-QUARTER))
			.put("AM.CRcr+ ROAD NS", new ImmutablePoint(CENTER, MAX-BORDER))
			.put("AM.CRcr+ FARM N", new ImmutablePoint(QUARTER, BORDER))
			.put("AM.CRcr+ CITY WE", new ImmutablePoint(QUARTER, CENTER))
		//GQ
			.put("GQ.CcRR ROAD E", new ImmutablePoint(MAX-BORDER, CENTER-10))
			.put("GQ.CcRR FARM EL", new ImmutablePoint(MAX-TINY_BORDER, QUARTER-3))
			.put("GQ.RFI ROAD W", new ImmutablePoint(BORDER, CENTER))
		//Cult
			.put("CU.SC FARM _N", new ImmutablePoint(BORDER, MAX-BORDER))
			.put("CU.SCFR FARM _N", new ImmutablePoint(BORDER, MAX-BORDER))
			.put("CU.SR FARM NWSE", new ImmutablePoint(BORDER, BORDER))
			.put("CU.SRCR FARM WL+S+ER", new ImmutablePoint(MAX-BORDER, MAX-BORDER))
			.put("CU.SRFR FARM SR+W+NL", new ImmutablePoint(MAX-BORDER, MAX-BORDER))
			.put("CU.SRFR FARM NR+E+SL", new ImmutablePoint(BORDER, BORDER))
		//Tunnel
			.put("TU.RCR ROAD E", new ImmutablePoint(MAX-ROAD_DIST, CENTER+10))
			.put("TU.RRRR FARM WR+N+EL", new ImmutablePoint(BORDER, BORDER))
		//Catapult
			.put("CA.L CLOISTER CLOISTER", new ImmutablePoint(34, 40))

			.put("BA.CcRr ROAD SE", P_OBLIQUE_SE)
			.put("BA.CcRr+ ROAD SE", P_OBLIQUE_SE)
			.put("CA.RrR.1 ROAD SE", P_OBLIQUE_SE)
			.put("GQ.CRRr ROAD SW", P_OBLIQUE_SW)
			.put("IC.CcRr+.i ROAD SE", P_OBLIQUE_SE)
			.put("IC.RrRr ROAD SW", P_OBLIQUE_SW)
			.put("KS.CRrR ROAD SE", P_OBLIQUE_SE)
			.put("DG.RrRr.g ROAD SE", P_OBLIQUE_SE)
			.put("DG.CcRr.p ROAD SE", P_OBLIQUE_SE)
			.put("R1.RrII ROAD NE", P_OBLIQUE_NE)
			.put("R2.RrII ROAD NE", P_OBLIQUE_NE)
			.put("TO.RrRr.2 ROAD NW", P_OBLIQUE_NW)
			.put("TO.RrRr.2 ROAD SE", P_OBLIQUE_SE)

			.build();
	}

	/*TODO cely revise, momentalne je refactoring a presun sem ze Square */

	public ImmutablePoint getFigurePlacement(Tile tile, Class<? extends Feature> piece, Location d) {
		Rotation iconRotation = tile.getRotation();

		StringBuilder key = new StringBuilder();
		key.append(tile.getId()).append(" ");
		key.append(piece.getSimpleName().replace("Piece", "").toUpperCase()).append(" ");
		key.append(d.rotateCCW(iconRotation).toString());

		//System.out.println(key.toString());

		ImmutablePoint specialPlacement = specialPlacements.get(key.toString());
		if (specialPlacement != null) {
			return specialPlacement.rotate(iconRotation);
		}

		if (piece.equals(Cloister.class)) {
			return new ImmutablePoint(50, 50);
		}
		if (piece.equals(Road.class)) {
			return getRoadPlacement(tile,d);
		}
		if (piece.equals(City.class)) {
			return getCityPlacement(tile,d);
		}
		if (piece.equals(Farm.class)) {
			return getFarmPlacement(d, iconRotation);
		}
		if (piece.equals(Tower.class)) {
			Area a = theme.getAreaProvider().getArea(tile, Tower.class, Location.TOWER);
			int x = a.getBounds().x + a.getBounds().width / 2;
			int y = a.getBounds().y + a.getBounds().height / 2;
			return new ImmutablePoint(x / 10, y / 10); //normalize to old value 100px
		}
		return null;
	}

	private ImmutablePoint getRoadPlacement(Tile tile, Location d) {
		Rotation rot;
		rot = d.getRotationOf(Location.N);
		if (rot != null) return new ImmutablePoint(CENTER, FIG_ROAD_PLACE).rotate(rot);
		rot = d.getRotationOf(Location.NW);
		if (rot != null) return new ImmutablePoint(40, 40).rotate(rot);
		if (d == Location.NWSE) {
			return new ImmutablePoint(CENTER, QUARTER);
		}
		return P_CENTER;
	}

	private ImmutablePoint getCityPlacement(Tile tile, Location d) {
		Rotation rot;
		rot = d.getRotationOf(Location.N);
		if (rot != null) return P_CITY_N.rotate(rot);
		rot = d.getRotationOf(Location.NW);
		if (rot != null) return P_CITY_NW.rotate(rot);
		rot = d.getRotationOf(Location._N);
		if (rot != null) return P_CITY_NOT_N.rotate(rot);
		return P_CENTER;
	}

	/* -- zbytek bude PRIVATE -- */

	/**
	 * Returns coordinates of figure placed on specified farm.
	 * Icon rotation used only for cloister with road.
	 * @param farmShape farm placement on tile
	 * @param tileRotation rotation of tile
	 * @return coordinates of figure
	 */
	private ImmutablePoint getFarmPlacement(Location farmShape, Rotation tileRotation) {
		int mask = farmShape.getMask();
		mask = (mask & 255) | ((mask & 65280) >> 8);
		if (map[mask] != null) return map[mask];
		if (mask == 255) {
			/*
			 * whole farm, it must be cloister with road
			 * so placing up to cloister
			 */
			return map[Location.N.rotateCW(tileRotation).getMask() >> 8];
		}
		return new ImmutablePoint(0,0);
	}

	private void generateRotations(int mask, ImmutablePoint p) {
		Location d = Location.create(mask);
		for(int i = 0; i < 4; i++) {
			int m = d.getMask();
			m = (m & 255) | ((m & 65280) >> 8);
			map[m] = p;
			d = d.next();
			p = p.rotate();
		}
	}
}
