package com.jcloisterzone.debug;

import java.awt.Graphics2D;


public class ElementDataDebugDecorator /*implements SquareDecoration*/ {

	//TODO use Fonts IF
//	private static Font font = new Font(null, Font.BOLD, 11);

	//@Override
	public int getZIndex() {
		return 1000;
	}

//	private static final Map<Direction, ImmutablePoint> textPosition =
//		new ImmutableMap.Builder<Direction, ImmutablePoint>()
//			.put(Direction.N, new ImmutablePoint(10,15))
//			.put(Direction.W, new ImmutablePoint(0,35))
//			.put(Direction.E, new ImmutablePoint(40,55))
//			.put(Direction.S, new ImmutablePoint(30,85))
//		.build();

	//@Override
	public void paint(Graphics2D g) {
//		if (sq.getTile() != null) {
//			/*Tile tile = Server.instance.getBoard().get(sq.x, sq.y);
//			g.setFont(font);
//
//			for (Direction d : Direction.sides()) {
//				MultiTileFeature mte = tile.getSideElement(d);
//				ImmutablePoint tp = textPosition.get(d);
//				if (mte == null) {
//					g.setColor(Color.RED);
//					g.drawString("NULL", tp.getX(), tp.getY());
//				} else {
//					g.setColor(Color.BLACK);
//					g.drawString(mte.toString(), tp.getX(), tp.getY());
//				}
//			}
//			g.drawString(tile.getRotation().toString(), 50, 50);*/
//		}

	}

}
