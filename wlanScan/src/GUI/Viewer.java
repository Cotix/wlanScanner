package GUI;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Image;
import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.LinkedBlockingQueue;

import javax.imageio.ImageIO;
import javax.swing.*;

import Utils.Position;
import Utils.Utils;

/**
 * Shows the map of Spiegel with the current location drawn on it
 * @author Bernd
 *
 */
public class Viewer implements Runnable {

	private Set<int[]> points;

	private LinkedBlockingQueue<Position> pos;

	public Viewer(LinkedBlockingQueue<Position> pos){
		this.pos = pos;
	}

	@Override
	public void run() {
		JFrame frame = new JFrame();
		frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
		frame.setSize(1150, 950);
		Image image;
		Image backup;
		Image point;
		try {
			image = ImageIO.read(new File("horstoverlay.png")); //ImageIO.read(new File("spiegel_beter.png"));
			backup = ImageIO.read(new File("horstoverlay.png")); //ImageIO.read(new File("spiegel_beter.png"));
			point = ImageIO.read(new File("point.png"));
			JLabel label = new JLabel(new ImageIcon(image));
			JPanel panel = new JPanel(new BorderLayout());
			panel.add(label);
			frame.add(panel);
			frame.setVisible(true);

			Graphics draw = image.getGraphics();
			draw.setColor(Color.RED);

			Position currentPos;
			while(true){
				currentPos = pos.take();
				currentPos = getMapPosition(currentPos);
				draw.drawImage(backup, 0, 0, null);
				draw.drawImage(point, (int)currentPos.getX()-5, (int)currentPos.getY()-5, null);

				for (int[] p : points) {
					Position ppos = getMapPosition(new Position(p[0], p[1]));
					int radius = (p[2] * 10);
					draw.drawOval((int) (ppos.getX()-radius/2)-5, (int) (ppos.getY()-radius/2)-5, radius, radius);
				}

				label.repaint();
			}

		} catch (IOException | InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void updatePoints(final HashSet<int[]> update) {
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				points = update;
			}
		});
	}

	/**
	 * Translates from map grid coordinates to pixel coordinates used for drawing
	 * @param pos
	 * @return
	 */
	public static Position getMapPosition(Position pos){
		double x = pos.getX();
		x = x*10;
		x = x + 45;

		double y = pos.getY();
		y = y*10;
		y = y + 44;

		return new Position(x,y);
	}
}
