package Location;
import Utils.MacRssiPair;
import Utils.Position;
import Utils.Utils;

import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;

/**
 * Simple Location finder that returns the first known APs location from the list of received MAC addresses
 * @author Bernd
 *
 */
public class LocationFinderDistance implements LocationFinder{
	private HashMap<String, Position> knownLocations; //Contains the known locations of APs. The long is a MAC address.
    private HashMap<String, Integer> routerMap;
    private Position myDeltaPos;
    private Position dDeltaPos;
    private String atRouter;
    private GUI.Viewer view;
    public class PairComparator implements Comparator<MacRssiPair> {

        @Override
        public int compare(MacRssiPair t1, MacRssiPair t2) {
            return t2.getRssi() - t1.getRssi();
        }
    }

    public LocationFinderDistance(GUI.Viewer p){
        routerMap = new HashMap<>();
        atRouter = "";
        myDeltaPos = new Position(0,0);
        view = p;
		knownLocations = Utils.getKnownLocations(); //Put the known locations in our hashMap
	}

	@Override
	public Position locate(MacRssiPair[] data) {
		printMacs(data); //print all the received data
		return getBestKnownFromList(data); //return the first known APs location
	}

    private double calculateDistance(int signalLevel) {
        double exp = (27.55 - (20 * Math.log10(2400)) + Math.abs(signalLevel)) / 20.0;
        double c = Math.pow(10.0, exp);
        c *= c;
        c /= 3 * 3;
        return Math.sqrt(c)*1.5;
    }

    private void processData(MacRssiPair pair) {
        String router = pair.getMacAsString();
        if (knownLocations.get(router) != null) {
            Position pos = knownLocations.get(router);
            if (routerMap.containsKey(router)) {
                int diff = Math.abs(routerMap.get(router) - pair.getRssi());
                System.out.println("diff: " + diff);
                if (diff <= 5 && pair.getRssi() >= -50) {
                    double diffDistance = Math.pow(10.0, diff/10.0);
                    System.out.println("diffDistance: " + diffDistance);
                    Position myPos = knownLocations.get(atRouter);
                    myPos = new Position(myPos.getX() + myDeltaPos.getX(), myPos.getY() + myDeltaPos.getY());
                    double x = (myPos.getX() - pos.getX());
                    double y = (myPos.getY() - pos.getY());
                    System.out.println("x,y: " + x + " " + y);
                    double oldX = x;
                    double oldY = y;
                    if (routerMap.get(router) < pair.getRssi()) {
                        x /= diffDistance;
                        y /= diffDistance;
                    } else {
                        x *= diffDistance;
                        y *= diffDistance;
                    }
                    System.out.println("x-oldx y-oldy: " + (x-oldX) + " " + (y-oldY));
                    dDeltaPos = new Position(dDeltaPos.getX() + (x-oldX), dDeltaPos.getY() + (y-oldY) );
                }
            }
            routerMap.put(router, pair.getRssi());
        }
    }

    private void drawWifiSpots(MacRssiPair[] data, int count) {
        HashSet<int[]> dataSet = new HashSet<int[]>();
        for(int i=0; i<data.length; i++){
            if (knownLocations.get(data[i].getMacAsString()) == null) {
                continue;
            }

            int[] array = new int[6];
            Position pos = knownLocations.get(data[i].getMacAsString());
            array[0] = (int)pos.getX();
            array[1] = (int)pos.getY();
            array[2] = (int)Math.ceil(calculateDistance(data[i].getRssi()));
            array[3] = count <= 0 ? 255 : 0;
            array[4] = count > 0 ? 255 : 0;
            array[5] = 0;
            count--;
            dataSet.add(array);
        }
        view.updatePoints(dataSet);
    }

	private Position getBestKnownFromList(MacRssiPair[] data){
        Arrays.sort(data, new PairComparator());
        drawWifiSpots(data, 1);
        dDeltaPos = new Position(0, 0);
        double dst = 0;
		for(int i=0; i<data.length; i++){
            if (knownLocations.get(data[i].getMacAsString()) == null) {
                continue;
            }
            if (atRouter.equals("")) {
                System.out.println("Setting first atRouter");
                atRouter = data[i].getMacAsString();
                routerMap.put(atRouter, data[i].getRssi());
            }
            if (data[i].getRssi() > routerMap.get(atRouter)) {
                Position myPos = knownLocations.get(atRouter);
                myPos = new Position(myPos.getX() + myDeltaPos.getX(), myPos.getY() + myDeltaPos.getY());
                Position newPos = knownLocations.get(data[i].getMacAsString());
                myDeltaPos = new Position(myPos.getX() - newPos.getX(), myPos.getY() - newPos.getY());
                atRouter = data[i].getMacAsString();
            }
            processData(data[i]);
		}
        myDeltaPos = new Position(myDeltaPos.getX() + dDeltaPos.getX(), myDeltaPos.getY() + dDeltaPos.getY());
        Position myPos = knownLocations.get(atRouter);
        myPos = new Position(myPos.getX() + myDeltaPos.getX(), myPos.getY() + myDeltaPos.getY());
		return myPos;
	}
	
	/**
	 * Outputs all the received MAC RSSI pairs to the standard out
	 * This method is provided so you can see the data you are getting
	 * @param data
	 */
	private void printMacs(MacRssiPair[] data) {
		for (MacRssiPair pair : data) {
			if (knownLocations.containsKey(pair.getMacAsString())) {
                //System.out.println(pair + " loc: " + knownLocations.get(pair.getMacAsString()) + " dst: " + calculateDistance(pair.getRssi()));
            }
		}
	}

}
