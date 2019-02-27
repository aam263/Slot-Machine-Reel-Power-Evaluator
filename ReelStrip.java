import java.lang.Math;
import java.util.*;
import java.util.Iterator;

class node {

	int symbol;
	node next;
	node prev;

	public node(){}

	public node(int symbol){
		this.symbol = symbol;
	}

}


//changes made to getSYmbols_RP to avoid wilds double counting - check if it works now
class ReelStripInitializer {

	int alpha; //numbits required to store symbols
	int selector; //alphs bits of 1 for performance reasons
	int screenSize;

	Set<Integer> symbols;
	Map<Integer,Set<Integer>> wildSubs; //a map of every wild and what it substitutes for
	//wilds substitute themselves trivially

	ReelStripInitializer(int numSymbols, int screenSize, Set<Integer> symbols, Map<Integer,Set<Integer>> wildSubs){
		this.alpha = (int) (Math.floor(Math.log(numSymbols)/Math.log(2))) + 1;
		this.selector = (int) Math.pow(2,alpha) - 1;
		this.screenSize = screenSize;
		this.symbols = symbols;
		this.wildSubs = wildSubs;
	}
}

class ReelStrip implements Iterable<Integer>{

	ReelStripInitializer initializer;

	node sentinel; //used to help with easy iteration
	node endScreen; //used to find the last node on the screen
	int screen; //binary representation of current Screen
	int size; //size of reel (add to it every time the add function is called)

	public ReelStrip(ReelStripInitializer initializer){
		this.initializer = initializer;
		this.sentinel = new node();
		this.endScreen = this.sentinel;
		this.screen = 0;
		this.size = 0;
	}

	public ReelStrip(ReelStripInitializer initializer, Integer[] reelSymbols){
		this.initializer = initializer;
		this.sentinel = new node();
		this.endScreen = this.sentinel;
		this.screen = 0;
		this.size = 0;
		for (int symbol : reelSymbols){
			this.add(symbol);
		}
	}

	public void add(int symbol){
		//TEST TEST TEST// - seems to work
		//add to reelstrip
		//if reelstrip is smaller than screen, add it to the screen rep (binary stuff)
		//HAVE TO MAKE IT SO THAT SENTINEL ISNT POINTED TO AFTER IT LOOPS BACK AROUND
		node addend = new node(symbol);
		if (this.size < (this.initializer.screenSize-1)){
			this.screen = (this.screen << this.initializer.alpha) + symbol;
			this.endScreen = addend;
		}

		if (this.size == 0){
			addend.prev = addend;
			addend.next = addend;
			this.sentinel.next = addend;
			this.size++;
		}
		else{
			addend.prev = this.sentinel.next.prev;
			this.sentinel.next.prev.next = addend;
			this.sentinel.next.prev = addend;
			addend.next = this.sentinel.next;
			this.size++;
		}

	}

	public void printReel(){
		//TEST TEST TEST// - seems to work
		int i = 0;
		node curr = this.sentinel;
		if (size == 0){
			System.out.println("reelStrip Object is empty");
		}
		else{
			while (i < this.size){
				curr = curr.next;
				System.out.print(curr.symbol);
				System.out.print(" ");
				i++;
			}
			System.out.println("");
		}
	}

	@Override
	public Iterator<Integer> iterator(){
		//TEST TEST TEST// - seems to work now
		//assert that size >= screenSize
		Iterator<Integer> itr = new Iterator<Integer>(){

				//cant use the this keyword because this will refer to the iterator and no the enclosing reelStrip class
				int currIdx = 0; 
				node currNode = endScreen; //should be replacd by node corresponding to last symbol on screen (use screenSize to get)
			
				@Override
				public boolean hasNext(){
					return (currIdx < size) && (size >= initializer.screenSize);
				}

				@Override
				public Integer next(){
					//TEST TEST TEST// 
					currNode = currNode.next;
					int remove = (screen >> ((initializer.screenSize-1)*initializer.alpha)) << ((initializer.screenSize-1)*initializer.alpha); //symbol to ve removed from screenRep
					screen = ((screen-remove) << initializer.alpha) + currNode.symbol;
					currIdx++; 
					return screen;
				}

		};

		return itr;

	}
/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	//have to subtract screens with wilds from other symbols too
	public Map<Integer,Integer> XsymbolCounts_RP(){
		//TEST TEST TEST// - works on reels without wilds; seems to work on wild reels too
		Map<Integer,Integer> XsymbolCounts = new HashMap<>();
		for (int symbol : this.initializer.symbols){
			XsymbolCounts.put(symbol,this.size);
		}
		for (Iterator<Integer> reelItr = this.iterator(); reelItr.hasNext();){
			this.getXSymbols_RP(reelItr.next(),XsymbolCounts);
		}
		return XsymbolCounts;
	}

	public void getXSymbols_RP(int screenRep, Map<Integer,Integer> XsymbolCounts){
		//TEST TEST TEST//
		//create a set of symbols
		int screenPos = this.initializer.screenSize-1;
		Set<Integer> wilds = new HashSet<>();
		Set<Integer> symbolsTraversed = new HashSet<>();
		while (screenPos >= 0){
			int symbol = (screenRep >> (screenPos*this.initializer.alpha)) & this.initializer.selector;
			//assured that all symbols will be in XsymbolCounts
			if (!symbolsTraversed.contains(symbol)){
				XsymbolCounts.put(symbol, XsymbolCounts.get(symbol)-1);
				symbolsTraversed.add(symbol);
			}
			if (this.initializer.wildSubs.containsKey(symbol)){
				wilds.add(symbol);
			}
			screenPos--;
		}
		//to deal with wild substituting things
		if (!wilds.isEmpty()){
			//if wilds exist, then loop over wilds
			for (int wild : wilds){
				for (int symbol : XsymbolCounts.keySet()){
					//it is assured that wildSubs contains all wilds seen in wilds
					if (!symbolsTraversed.contains(symbol) && this.initializer.wildSubs.get(wild).contains(symbol)){
						XsymbolCounts.put(symbol, XsymbolCounts.get(symbol)-1);
					}
				}
			}
		}
	}

//////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	public Map<Integer,Integer> symbolCounts_RP(){
		//TEST TEST TEST// - seems to work on wild reels; works on non wild reels
		//only gives meaningful results when no wilds on reel 1 (left to right) or no wilds on reel 5 (right to left)
		Map<Integer,Integer> symbolCounts = new HashMap<>();
		for (Iterator<Integer> reelItr = this.iterator(); reelItr.hasNext();){
			//int val = reelItr.next();
			//System.out.println(Integer.toBinaryString(val));
			this.getSymbols_RP(reelItr.next(), symbolCounts);
			//System.out.println("");
		}
		return symbolCounts;
	}

	public void getSymbols_RP(int screenRep, Map<Integer,Integer> symbolCounts){
		//TEST TEST TEST// 
		//count number of wilds as you go and then increase all values in map by that (except same wilds)
		//modifies symbolCounts for each screenRep symbol assortment

		//get counts and num wilds
		int screenPos = this.initializer.screenSize-1;
		Map<Integer,Integer> numWilds = new HashMap<>(); //each wild has a different number of counts
		while (screenPos >= 0){
			int symbol = (screenRep >> (screenPos*this.initializer.alpha)) & this.initializer.selector;
			symbolCounts.put(symbol, symbolCounts.containsKey(symbol) ? symbolCounts.get(symbol) + 1 : 1);
			screenPos--;
			if (this.initializer.wildSubs.containsKey(symbol)){
				numWilds.put(symbol, numWilds.containsKey(symbol) ? numWilds.get(symbol)+1 : 1);
			}
		}

		//account for wild subs
		//TEST TEST TEST// above accounts for each symbols own counts (and stores number of wilds for different wild types)
		//below loops through wilds seen, and adds the counts to every symbol, since they substitute veery symbol
		if (!numWilds.isEmpty()){
			//if wilds exist, then loop over wilds
			for (int wild : numWilds.keySet()){
				for (int symbol : this.initializer.symbols){
					//it is assured that wildSubs contains all wilds seen in numWilds.keySet()
					//don't double count
					//below might cause issus
					if ((wild != symbol) && this.initializer.wildSubs.get(wild).contains(symbol)){
						//since symbol counts is being created serially, may be possible certain symbols havent appeared yet in the iteration, so have to check if symbol counts contains the symbol yet
						symbolCounts.put(symbol, symbolCounts.containsKey(symbol) ? symbolCounts.get(symbol)+numWilds.get(wild) : numWilds.get(wild));
					}
				}
			}
		}

	}

	public static void main(String[] args){
	//do something here
	}

}


