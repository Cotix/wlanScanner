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
    private short[][] scoreMap;
    private Position bestPos;

    public class PairComparator implements Comparator<MacRssiPair> {

        @Override
        public int compare(MacRssiPair t1, MacRssiPair t2) {
            return t2.getRssi() - t1.getRssi();
        }
    }

    public LocationFinderDistance(){
        scoreMap = new short[200][200];
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
        c /= 2.5 * 2.5;
        return Math.sqrt(c);
    }

    private Position processData(MacRssiPair pair) {
        int dst = (int)Math.ceil(calculateDistance(pair.getRssi()));
        for (int dx = -dst; dx != dst; ++dx) {
            for (int dy = -dst; dy != dst; ++dy) {
                Position pos = knownLocations.get(pair.getMacAsString());
                int x = (int)Math.round(pos.getX() + dx);
                int y = (int)Math.round(pos.getY() + dy);
                if (x < 0 || y < 0 || x >= 1000 || y >= 1000) {
                    continue;
                }
                if (dst - Math.sqrt(dx*dx + dy*dy) > 0) {
                    scoreMap[x][y]++;
                }
                if (bestPos == null || scoreMap[x][y] > scoreMap[(int)bestPos.getX()][(int)bestPos.getY()]) {
                    bestPos = new Position(x, y);
                }
            }
        }
        System.out.println(scoreMap[(int)bestPos.getX()][(int)bestPos.getY()]);
        return bestPos;
    }

    private void drawWifiSpots(MacRssiPair[] data, int count) {
        HashSet<Integer[]> dataSet = new HashSet<Integer[]>();
        for(int i=0; i<data.length; i++){
            Integer[] array = new Integer[6];
            Position pos = knownLocations.get(data[i].getMacAsString());
            array[0] = (int)pos.getX();
            array[1] = (int)pos.getY();
            array[2] = (int)Math.ceil(calculateDistance(data[i].getRssi()));
            array[3] = count <= 0 ? 255 : 0;
            array[4] = count > 0 ? 255 : 0;
            array[5] = 0;
            dataSet.add(array);
        }
        //main.WlanScanner.GUI
    }

	private Position getBestKnownFromList(MacRssiPair[] data){
        scoreMap = new short[1000][1000];
        Arrays.sort(data, new PairComparator());
		Position ret = new Position(0, 0);
        double dst = 0;
        int count = 30;
		for(int i=0; i<data.length; i++){
			if(count > 0 && knownLocations.containsKey(data[i].getMacAsString())){
				ret = processData(data[i]);
                count--;
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
