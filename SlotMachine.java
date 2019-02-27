import java.util.*;


class tuple{

	int payRep;
	int multiplicity;

	tuple(int payRep, int multiplicity){
		this.payRep = payRep;
		this.multiplicity = multiplicity;
	}

}


class SlotMachineInitializer {
	//in debug mode you only pass SlotMachineInitializer to slot machine class instead of both that and a ReelStripinitializer object
	//pass Initializer instance along with the reelstrips into slot machine class
	int alpha; //numbits required to store symbols
	int selector; //alphs bits of 1 for performance reasons
	int screenSize; //can ignore outside of debug mode (replace anywhere that uses screensize with the screens corresponnding to that in specified reel) ->need to chamge size of dp map (can do so in outer functions)
	int numReels;

	Map<Integer,Set<Integer>> wildSubs;// can ignore outside of debug mode
	Map<Integer,Integer> payTable;
	Map<Integer,Integer[]> reelStripsRaw;

	SlotMachineInitializer(int numSymbols,int numReels, int screenSize, Map<Integer,Integer> payTable, Map<Integer,Set<Integer>> wildSubs){
		this.alpha = (int) (Math.floor(Math.log(numSymbols)/Math.log(2))) + 1;
		this.selector = (int) Math.pow(2,alpha) - 1;
		this.numReels = numReels;
		this.screenSize = screenSize;
		this.payTable = payTable;
		this.wildSubs = wildSubs;//once you debug getPays you can remove this
	}

	SlotMachineInitializer(int numSymbols,int numReels, int screenSize, Map<Integer,Integer> payTable,Map<Integer,Set<Integer>> wildSubs, Map<Integer,Integer[]> reelStripsRaw){
		this.alpha = (int) (Math.floor(Math.log(numSymbols)/Math.log(2))) + 1;
		this.selector = (int) Math.pow(2,alpha) - 1;
		this.numReels = numReels;
		this.screenSize = screenSize;
		this.payTable = payTable;
		this.wildSubs = wildSubs;//once you debug getPays you can remove this
		this.reelStripsRaw = reelStripsRaw;
	}

}

class SlotMachine{
	
	// theres a set of if statements ti be changed if you use the nondebug constructor
	//theres a different version of gPH_helper that accounts for timberwolf style wilds but you have to swap out how multiplicity is calculated (both versions included just comment/uncomment appropriate ones)
	//use each individual reels screensize when iterating (generateCycle, generatePayHits, getPays_RP) - have to modify dp_arr in getPays too 

	//slot machine generateCycle using reelstrip iterator
	//  -  allow for fixing positions on any of the reelstrips
	//  -  reelpower (left - to - right // no wilds on reel 1)

	SlotMachineInitializer initializer;
	Map<Integer,ReelStrip> reelStrips; //assert : all reels inputted labeled [1, ... , N]

	SlotMachine(SlotMachineInitializer initializer_S){
		//use only when reelStripsRaw isn't provided with SlotMachineInitializer
		//set of if statements in getPays_RP that must be changd out if you start in debug mode
		//for debug purposes - can't use any methods that operate on reelStrips
		this.initializer = initializer_S; 
	};

	SlotMachine(SlotMachineInitializer initializer_S, ReelStripInitializer initializer_R){
		//constructor for all reels with the same reelStripInitializer
		this.reelStrips = new HashMap<Integer,ReelStrip>();
		this.initializer = initializer_S;
		for (int reelNo : initializer_S.reelStripsRaw.keySet()){
			ReelStrip newReel = new ReelStrip(initializer_R,initializer_S.reelStripsRaw.get(reelNo));
			this.reelStrips.put(reelNo, newReel);
		}
	}

	SlotMachine(SlotMachineInitializer initializer_S, ReelStripInitializer[] initializers_R){
		//TEST TEST TEST//
		//constructor with reels containing different reelStripInitializers
		//assert : initializers.size == reelStrips.keySet().size()
		//assert : reelStrips.keySet() properly indexes initializers array
		this.reelStrips = new HashMap<Integer,ReelStrip>();
		this.initializer = initializer_S;
		for (int reelNo : initializer_S.reelStripsRaw.keySet()){
			ReelStrip newReel = new ReelStrip(initializers_R[reelNo],initializer_S.reelStripsRaw.get(reelNo));
			this.reelStrips.put(reelNo, newReel);
		}
	}

	public void generateCycle(){
		//TEST TEST TEST// - seems to be working
		// prints out the binaryRep of all possible screens fo the slot machine
		//intrinsically uses each individual reels screenSize
		Map<Integer,Integer> screen = new HashMap<>();
		gCycle_helper(1,screen);
	}

	public void gCycle_helper(int reelNo, Map<Integer,Integer> currScreen){
		//TEST TEST TEST// - seems to be working
		// without reelStops it just prints out the cycle
		//intrinsically uses each individual reels screenSize
		if (reelNo <= this.initializer.numReels){			
			for (Iterator<Integer> reelItr = this.reelStrips.get(reelNo).iterator(); reelItr.hasNext();){
				int reelScreen = reelItr.next();
				currScreen.put(reelNo, reelScreen);
				gCycle_helper(reelNo+1,currScreen);
			}
		}
		else{
			//do something here
		}
		
	}

	public void generateCycle(Map<Integer,Set<Integer>> reelStops){
		//TEST TEST TEST// - seems to be working 
		// prints out the binaryRep of all possible screens of the slot machine at the specified reelstops
		// if a specified stop falls outside of index range then it will just be skipped
		// if a specific reel is not included in reelStops, it will iterate over the entire reel
		//intrinsically uses each individual reels screenSize
		//assert : 1 <= reelStop.get(reelNo) <= this.reelStrips.get(reelNo).size (0th stop corresponds to havng the sentinel node in the screen so we skip that)
		Map<Integer,Integer> screen = new HashMap<>();
		gCycle_helper(1,screen,reelStops);
	}

	public void gCycle_helper(int reelNo, Map<Integer,Integer> currScreen, Map<Integer,Set<Integer>> reelStops){
		//TEST TEST TEST// - seems to be working
		//in reelStops can pass reelNos and reelStops
		//intrinsically uses each individual reels screenSize
		//added the ability to specify reelstops to stay on during iteration
		if (reelNo <= this.initializer.numReels){
			if (!reelStops.containsKey(reelNo)){
				for (Iterator<Integer> reelItr = this.reelStrips.get(reelNo).iterator(); reelItr.hasNext();){
					int reelScreen = reelItr.next();
					currScreen.put(reelNo, reelScreen);
					gCycle_helper(reelNo+1,currScreen,reelStops);
				}
			}
			else{
				//since we asseert that reel.size >= screenSize, we know that reelItrs.hasNext will be nonzero (initialized as zero for compiler reasons)
				//assert : 1 <= reelStop.get(reelNo) <= this.reelStrips.get(reelNo).size (0th stop corresponds to havng the sentinel node in the screen so we skip that)
				//possible improvement -> make each rotation in O(1) time uing a lookup table for which node you want to iterate to (imporves speed of finding reel positions)
				int reelStop = 1;
				int reelScreen = 0;
				for(Iterator<Integer> reelItr = this.reelStrips.get(reelNo).iterator(); reelItr.hasNext(); ++reelStop){
					reelScreen = reelItr.next();
					if (reelStops.get(reelNo).contains(reelStop)){
						currScreen.put(reelNo,reelScreen);
						gCycle_helper(reelNo+1,currScreen,reelStops);
						//break statement messes up an iterator?? - look up later
					}
				}
			}
		}
		else{
			//do something
		}		
	}
/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	public Map<Integer,Long> generatePayHits(){
		//TEST TEST TEST// - seems to work 
		// same as generateCycle, but uses the O(n^2) generatePays algorithm to determine the screen pays
		//intrinsically uses each individual reels screenSize
		Map<Integer,Integer> screen = new HashMap<>();
		Map<Integer,Long> payHits = new HashMap<>();
		gPH_helper(1,screen,payHits);
		return payHits;
	}

	public void gPH_helper(int reelNo, Map<Integer,Integer> currScreen, Map<Integer,Long> payHits){
		//TEST TEST TEST// - seems to work
		//intrinsically uses each individual reels screenSize
		if (reelNo <= this.initializer.numReels){
			for (Iterator<Integer> reelItr = this.reelStrips.get(reelNo).iterator(); reelItr.hasNext();){
				int reelScreen = reelItr.next();
				currScreen.put(reelNo, reelScreen);
				gPH_helper(reelNo+1,currScreen,payHits);
			}
		}
		else{getPays_RP(currScreen,payHits);}		
	}

	public Map<Integer,Long> generatePayHits(Map<Integer,Set<Integer>> reelStops){
		//TEST TEST TEST// - seems to work
		//added the ability to specify reelstops to stay on during iteration
		//same as generateCycle, but uses the O(n^2) generatePays algorithm to determine the screen pays for allowable screens specified by reelstops
		//intrinsically uses each individual reels screenSize
		Map<Integer,Integer> screen = new HashMap<>();
		Map<Integer,Long> payHits = new HashMap<>();
		gPH_helper(1,screen,payHits,reelStops);
		return payHits;
	}

	public void gPH_helper(int reelNo, Map<Integer,Integer> currScreen, Map<Integer,Long> payHits, Map<Integer,Set<Integer>> reelStops){
		//TEST TEST TEST// - seems to work
		//multithread this to get faster stuff for 5 reels
		//in reelStops can pass reelNos and reelStops
		//intrinsically uses each individual reels screenSize
		if (reelNo <= this.initializer.numReels){
			if (!reelStops.containsKey(reelNo)){
				for (Iterator<Integer> reelItr = this.reelStrips.get(reelNo).iterator(); reelItr.hasNext();){
					int reelScreen = reelItr.next();
					currScreen.put(reelNo, reelScreen);
					//if reelNo = 1 then call this with a thread
					gPH_helper(reelNo+1,currScreen,payHits,reelStops);
				}
			}
			else{
				//since we asseert that reel.size >= screenSize, we know that reelItrs.hasNext will be nonzero (initialized as zero for compiler reasons)
				//assert : 1 <= reelStop.get(reelNo) <= this.reelStrips.get(reelNo).size (0th stop corresponds to havng the sentinel node in the screen so we skip that)
				//possible improvement -> make each rotation in O(1) time uing a lookup table for which node you want to iterate to (imporves speed of finding reel positions)
				int reelStop = 1;
				int reelScreen = 0;
				for(Iterator<Integer> reelItr = this.reelStrips.get(reelNo).iterator(); reelItr.hasNext(); ++reelStop){
					reelScreen = reelItr.next();
					if (reelStops.get(reelNo).contains(reelStop)){
						currScreen.put(reelNo,reelScreen);
						gPH_helper(reelNo+1,currScreen,payHits,reelStops);
						//break statement messes up an iterator??
					}
				}
			}
		}
		//try adding this to a list and then iterate over list
		else{getPays_RP(currScreen,payHits);}	
	}

////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	public void getPays_RP(Map<Integer,Integer> currScreen, Map<Integer,Long> payHits){
		//TEST TEST TEST// -  seems to work now
		//O(n^2) - time
		//no wilds on reel 1
		//modifies payHits with pays and hits of each screen
		//modify here to use each screens individual screensize (dp arr will be oversided and use the biggest reel size then)
		//modify here to use each reelStrips individual wildSubs (relace set of if statements)
		//assert : first reel is reelNo = 1
		//assumes a square screen but can modify later to allow for any form of screen(as long as you use individual reelstrip heights instead of an overall one)
		int reelNo = 1;
		tuple[][] dp_arr = new tuple[this.initializer.screenSize][this.initializer.numReels];
		for (int height = this.initializer.screenSize; height > 0; height--){
			int symbol = (currScreen.get(reelNo) >> ((height-1)*this.initializer.alpha)) & this.initializer.selector;
			tuple payInfo;
			payInfo = gP_RP_helper(symbol,reelNo+1,currScreen,dp_arr);
			if (this.initializer.payTable.containsKey(payInfo.payRep)){
				int pay = this.initializer.payTable.get(payInfo.payRep);
				payHits.put(pay, payHits.containsKey(pay) ? payHits.get(pay) + payInfo.multiplicity : payInfo.multiplicity);
			}			
		}

	}


	public tuple gP_RP_helper(int currSymbol, int reelNo, Map<Integer,Integer> currScreen, tuple[][] dp_arr){
		//TEST TEST TEST// 
		//modify here to use each screens individual screensize (dp arr will be oversided and use the biggest reel size then)
		//modify here to use each reelStrips individual wildSubs (relace set of if statements)
		int payRep = currSymbol;
		int multiplicity = 0;
		if (reelNo <= this.initializer.numReels){
			boolean flag = true; // need so we only have to augment payRep once
			boolean noneMatching = true;
			for (int height = this.initializer.screenSize; height > 0; height--){
				tuple payInfo;
				int symbol = (currScreen.get(reelNo) >> ((height-1)*this.initializer.alpha)) & this.initializer.selector; 
				if (currSymbol == symbol){
					noneMatching = false;
					if (dp_arr[this.initializer.screenSize-height][reelNo-1] != null){
						payInfo = dp_arr[this.initializer.screenSize-height][reelNo-1];
					}
					else{
						payInfo = (reelNo == this.initializer.numReels) ?  new tuple(symbol,1) : gP_RP_helper(symbol,reelNo+1,currScreen,dp_arr);
						dp_arr[this.initializer.screenSize-height][reelNo-1] = payInfo;
					}
					multiplicity += payInfo.multiplicity;
					if (flag){
						payRep = (payInfo.payRep << this.initializer.alpha) + payRep;
						flag = false;
					}
				}
				//use second set of if conditions for when you actually pass in the reelstrips (non-debug mode)
				//if (this.initializer.wildSubs.containsKey(symbol)){
				if (this.reelStrips.get(reelNo).initializer.wildSubs.containsKey(symbol)){
					//if (this.initializer.wildSubs.get(symbol).contains(currSymbol)){
					if (this.reelStrips.get(reelNo).initializer.wildSubs.get(symbol).contains(currSymbol)){
						noneMatching = false;
						//if there's a wild it isnt going to be placed in dp_arr
						payInfo = (reelNo == this.initializer.numReels) ?  new tuple(currSymbol,1) : gP_RP_helper(currSymbol,reelNo+1,currScreen,dp_arr);
						//replace multiplicty with commented version (top) for sticky wilds on reel 2 & 4 that only pay for ways that pass through them.
						//multiplicity += (reelNo == 2) ? 2*payInfo.multiplicity : (reelNo == 4) ? 4*payInfo.multiplicity : payInfo.multiplicity;
						multiplicity += payInfo.multiplicity;
						if (flag){
							payRep = (payInfo.payRep << this.initializer.alpha) + payRep;
							flag = false;
						}
					}
				}
			}
			if (noneMatching){return new tuple(payRep,1);}
		}
		return new tuple(payRep,multiplicity);
	}


	public static void main(String[] args){
		//create initializers and other stuff
			
		
	}

}
