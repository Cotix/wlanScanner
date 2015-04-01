package Location;

import Utils.MacRssiPair;
import Utils.Position;
import Utils.Utils;

import java.util.HashMap;

/**
 * Simple Location finder that returns the first known APs location from the list of received MAC addresses
 * @author Bernd
 *
 */
public class LocationFinderDistance implements LocationFinder{
	private HashMap<String, Position> knownLocations; //Contains the known locations of APs. The long is a MAC address.
    private short[][] scoreMap;
    private Position bestPos;

    public LocationFinderDistance(){
        scoreMap = new short[1000][1000];
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
        return Math.sqrt(c);
    }

    private Position processData(MacRssiPair pair) {
        for (int dx = 0; dx != 20; ++dx) {
            for (int dy = 0; dy != 20; ++dy) {
                Position pos = knownLocations.get(pair.getMacAsString());
                int x = (int)Math.round(pos.getX() + dx);
                int y = (int)Math.round(pos.getY() + dy);
                scoreMap[x][y] += (200 + pair.getRssi())  - (Math.abs(Math.sqrt(dx*dx + dy+dy) - calculateDistance(pair.getRssi())*2));
                if (bestPos == null || scoreMap[x][y] > scoreMap[(int)bestPos.getX()][(int)bestPos.getY()]) {
                    bestPos = new Position(x, y);
                }
            }
        }
        return bestPos;
    }

	private Position getBestKnownFromList(MacRssiPair[] data){
		Position ret = new Position(0, 0);
        double dst = 0;
		for(int i=0; i<data.length; i++){
			if(knownLocations.containsKey(data[i].getMacAsString())){
				ret = processData(data[i]);
			}
            if(data[i].getMacAsString().equals("00:26:CB:42:8B:20")) {
                dst = calculateDistance(data[i].getRssi());
            }
		}
        System.out.println("Distance to 00:26:CB:42:8B:20: " + dst);
		return ret;
	}
	
	/**
	 * Outputs all the received MAC RSSI pairs to the standard out
	 * This method is provided so you can see the data you are getting
	 * @param data
	 */
	private void printMacs(MacRssiPair[] data) {
		for (MacRssiPair pair : data) {
			if (knownLocations.containsKey(pair.getMacAsString())) {
                System.out.println(pair + " loc: " + knownLocations.get(pair.getMacAsString()));
            }
		}
	}

}
