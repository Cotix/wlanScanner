package Location;

import java.util.HashMap;
import Utils.*;

/**
 * Simple Location finder that returns the first known APs location from the list of received MAC addresses
 * @author Bernd
 *
 */
public class SimpleLocationFinder implements LocationFinder{
	
	private HashMap<String, Position> knownLocations; //Contains the known locations of APs. The long is a MAC address.
	
	public SimpleLocationFinder(){
		knownLocations = Utils.getKnownLocations(); //Put the known locations in our hashMap
	}

	@Override
	public Position locate(MacRssiPair[] data) {
		printMacs(data); //print all the received data
		return getBestKnownFromList(data); //return the first known APs location
	}

	private Position getBestKnownFromList(MacRssiPair[] data){
		Position ret = new Position(0,0);
        MacRssiPair bestRssi = null;
        int best = -1000;
        int dst = 0;
		for(int i=0; i<data.length; i++){
			if(knownLocations.containsKey(data[i].getMacAsString()) && data[i].getRssi() > best){
				ret = knownLocations.get(data[i].getMacAsString());
                bestRssi = data[i];
                best = data[i].getRssi();
			}
            if(data[i].getMacAsString().equals("00:26:CB:42:8B:20")) {
                dst = data[i].getRssi();
            }
		}
        System.out.println("Best one: "  + bestRssi);
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
			System.out.println(pair);
		}
	}

}
