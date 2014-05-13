/**
 * TAC AgentWare
 * http://www.sics.se/tac        tac-dev@sics.se
 *
 * Copyright (c) 2001-2005 SICS AB. All rights reserved.
 *
 * SICS grants you the right to use, modify, and redistribute this
 * software for noncommercial purposes, on the conditions that you:
 * (1) retain the original headers, including the copyright notice and
 * this text, (2) clearly document the difference between any derived
 * software and the original, and (3) acknowledge your use of this
 * software in pertaining publications and reports.  SICS provides
 * this software "as is", without any warranty of any kind.  IN NO
 * EVENT SHALL SICS BE LIABLE FOR ANY DIRECT, SPECIAL OR INDIRECT,
 * PUNITIVE, INCIDENTAL OR CONSEQUENTIAL LOSSES OR DAMAGES ARISING OUT
 * OF THE USE OF THE SOFTWARE.
 *
 * -----------------------------------------------------------------
 *
 * Author  : Joakim Eriksson, Niclas Finne, Sverker Janson
 * Created : 23 April, 2002
 * Updated : $Date: 2005/06/07 19:06:16 $
 *	     $Revision: 1.1 $
 * ---------------------------------------------------------
 * DummyAgent is a simplest possible agent for TAC. It uses
 * the TACAgent agent ware to interact with the TAC server.
 *
 * Important methods in TACAgent:
 *
 * Retrieving information about the current Game
 * ---------------------------------------------
 * int getGameID()
 *  - returns the id of current game or -1 if no game is currently plaing
 *
 * getServerTime()
 *  - returns the current server time in milliseconds
 *
 * getGameTime()
 *  - returns the time from start of game in milliseconds
 *
 * getGameTimeLeft()
 *  - returns the time left in the game in milliseconds
 *
 * getGameLength()
 *  - returns the game length in milliseconds
 *
 * int getAuctionNo()
 *  - returns the number of auctions in TAC
 *
 * int getClientPreference(int client, int type)
 *  - returns the clients preference for the specified type
 *   (types are TACAgent.{ARRIVAL, DEPARTURE, HOTEL_VALUE, E1, E2, E3}
 *
 * int getAuctionFor(int category, int type, int day)
 *  - returns the auction-id for the requested resource
 *   (categories are TACAgent.{CAT_FLIGHT, CAT_HOTEL, CAT_ENTERTAINMENT
 *    and types are TACAgent.TYPE_INFLIGHT, TACAgent.TYPE_OUTFLIGHT, etc)
 *
 * int getAuctionCategory(int auction)
 *  - returns the category for this auction (CAT_FLIGHT, CAT_HOTEL,
 *    CAT_ENTERTAINMENT)
 *
 * int getAuctionDay(int auction)
 *  - returns the day for this auction.
 *
 * int getAuctionType(int auction)
 *  - returns the type for this auction (TYPE_INFLIGHT, TYPE_OUTFLIGHT, etc).
 *
 * int getOwn(int auction)
 *  - returns the number of items that the agent own for this
 *    auction
 *
 * Submitting Bids
 * ---------------------------------------------
 * void submitBid(Bid)
 *  - submits a bid to the tac server
 *
 * void replaceBid(OldBid, Bid)
 *  - replaces the old bid (the current active bid) in the tac server
 *
 *   Bids have the following important methods:
 *    - create a bid with new Bid(AuctionID)
 *
 *   void addBidPoint(int quantity, float price)
 *    - adds a bid point in the bid
 *
 * Help methods for remembering what to buy for each auction:
 * ----------------------------------------------------------
 * int getAllocation(int auctionID)
 *   - returns the allocation set for this auction
 * void setAllocation(int auctionID, int quantity)
 *   - set the allocation for this auction
 *
 *
 * Callbacks from the TACAgent (caused via interaction with server)
 *
 * bidUpdated(Bid bid)
 *  - there are TACAgent have received an answer on a bid query/submission
 *   (new information about the bid is available)
 * bidRejected(Bid bid)
 *  - the bid has been rejected (reason is bid.getRejectReason())
 * bidError(Bid bid, int error)
 *  - the bid contained errors (error represent error status - commandStatus)
 *
 * quoteUpdated(Quote quote)
 *  - new information about the quotes on the auction (quote.getAuction())
 *    has arrived
 * quoteUpdated(int category)
 *  - new information about the quotes on all auctions for the auction
 *    category has arrived (quotes for a specific type of auctions are
 *    often requested at once).

 * auctionClosed(int auction)
 *  - the auction with id "auction" has closed
 *
 * transaction(Transaction transaction)
 *  - there has been a transaction
 *
 * gameStarted()
 *  - a TAC game has started, and all information about the
 *    game is available (preferences etc).
 *
 * gameStopped()
 *  - the current game has ended
 *
 */

package se.sics.tac.aw;
import se.sics.tac.util.ArgEnumerator;
import java.util.logging.*;

import java.util.Arrays;
import javax.swing.Timer;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.Iterator;

public class DummyAgent extends AgentImpl {

  private static final Logger log =
    Logger.getLogger(DummyAgent.class.getName());

  private static final boolean DEBUG = false;

  // Nadia
  // -----------------------------------------------
	// Initializing the price prediction arrays

	// In Flights
	ArrayList<Float> in_pastPrice1 = new ArrayList<Float>(); // day 1
	ArrayList<Float> in_pastPrice2 = new ArrayList<Float>(); // day 2
	ArrayList<Float> in_pastPrice3 = new ArrayList<Float>(); // day 3
	ArrayList<Float> in_pastPrice4 = new ArrayList<Float>(); // day 4

	private float[] in_curPrices = new float[4]; // current price for days 1-4
	private float[] in_futPrices = new float[4]; // future price for days 2-5

	// Out Flights
	ArrayList<Float> out_pastPrice2 = new ArrayList<Float>(); // day 2
	ArrayList<Float> out_pastPrice3 = new ArrayList<Float>(); // day 3
	ArrayList<Float> out_pastPrice4 = new ArrayList<Float>(); // day 4
	ArrayList<Float> out_pastPrice5 = new ArrayList<Float>(); // day 5

	private float[] out_curPrices = new float[4]; // current price for days 1-4
	private float[] out_futPrices = new float[4]; // future price for days 2-5

	// -----------------------------------------------



  //----------------------Konstantinos------------------------//

private float entUpperLowerBoundary[][];
//----------------------------------------------------------//


  private float[] prices;
  private Timer keepd;

  //float pricesInMinutes[] = new float[8];//time of game, meaning which minute and the type of auction
  //private int count[]= {1,1,1,1,1,1,1,1};//pricesInMinutes
  

  protected void init(ArgEnumerator args) {
    prices = new float[agent.getAuctionNo()];

//----------------------Konstantinos------------------------//

    entUpperLowerBoundary =new float[3][2];
    for(int i=0;i<3;i++){
      for(int j=0;j<2;j++){
        entUpperLowerBoundary[i][j]=0;
      }
    }
//-----------------------------------------------------------//
  }
  
  // Nadia
  // -----------------------------------------------
	public void updatePricePredictions() {

		// IN FLIGHTS
		// -----------------------------------------------
		// current in flights
		for (int i = 0; i <= 3; i++) {
			Quote tempQuote = agent.getQuote(agent.getAuctionFor(
					TACAgent.CAT_FLIGHT, TACAgent.TYPE_INFLIGHT, i + 1));
			in_curPrices[i] = tempQuote.getAskPrice();

			// if price set up
			if (tempQuote.getAskPrice() > 0) {
				// store the price for appropriate day
				switch (i) {
				case 0:
					in_pastPrice1.add(new Float(tempQuote.getAskPrice()));
					break;
				case 1:
					in_pastPrice2.add(new Float(tempQuote.getAskPrice()));
					break;
				case 2:
					in_pastPrice3.add(new Float(tempQuote.getAskPrice()));
					break;
				case 3:
					in_pastPrice4.add(new Float(tempQuote.getAskPrice()));
					break;
				}
			}
		}

		// future in flights
		for (int i = 0; i <= 3; i++) {
			float change = predictedFlightChange(TACAgent.TYPE_INFLIGHT, i + 1);
			in_futPrices[i] = in_curPrices[i] + change;
		}

		// OUT FLIGHTS
		// -----------------------------------------------
		// current out flights
		for (int i = 0; i <= 3; i++) {
			Quote tempQuote = agent.getQuote(agent.getAuctionFor(
					TACAgent.CAT_FLIGHT, TACAgent.TYPE_OUTFLIGHT, i + 2));
			out_curPrices[i] = tempQuote.getAskPrice();
			if (tempQuote.getAskPrice() > 0) {
				// store the price for appropriate day
				switch (i) {
				case 0:
					out_pastPrice2.add(new Float(tempQuote.getAskPrice()));
					break;
				case 1:
					out_pastPrice3.add(new Float(tempQuote.getAskPrice()));
					break;
				case 2:
					out_pastPrice4.add(new Float(tempQuote.getAskPrice()));
					break;
				case 3:
					out_pastPrice5.add(new Float(tempQuote.getAskPrice()));
					break;
				}
			}
		}

		// future out flights
		for (int i = 0; i <= 3; i++) {
			float change = predictedFlightChange(TACAgent.TYPE_OUTFLIGHT, i + 2);
			out_futPrices[i] = out_curPrices[i] + change;
		}

	}
	
	private float predictedFlightChange(int flightType, int day) {
		/*
		 * Predict the change of the future price by learning during some
		 * primary bids
		 */

		// Selecting past data array about flight with particular direction/day
		ArrayList<Float> storedFlightPricesData = new ArrayList<Float>();
		if (flightType == TACAgent.TYPE_INFLIGHT) {
			switch (day) {
			case 1:
				storedFlightPricesData = in_pastPrice1;
				break;
			case 2:
				storedFlightPricesData = in_pastPrice2;
				break;
			case 3:
				storedFlightPricesData = in_pastPrice3;
				break;
			case 4:
				storedFlightPricesData = in_pastPrice4;
				break;
			}
		}
		if (flightType == TACAgent.TYPE_OUTFLIGHT) {
			switch (day) {
			case 2:
				storedFlightPricesData = out_pastPrice2;
				break;
			case 3:
				storedFlightPricesData = out_pastPrice3;
				break;
			case 4:
				storedFlightPricesData = out_pastPrice4;
				break;
			case 5:
				storedFlightPricesData = out_pastPrice5;
				break;
			}
		}

		float lastPrice = 0;
		float curPrice = 0;

		int numOfPriceRise = 0;
		int numOfPriceDrop = 0;

		float sumOfRiseValue = 0;
		float sumOfDropValue = 0;

		int seqOfPriceRise = 0;
		int seqOfPriceDrop = 0;

		float seqOfPriceRiseValue = 0;
		float seqOfPriceDropValue = 0;

		float lastSeqOfPriceDropValue = 0;

		Iterator<Float> flightDataIterator = storedFlightPricesData.iterator();
		while (flightDataIterator.hasNext()) {
			lastPrice = curPrice;
			curPrice = (flightDataIterator.next()).floatValue();

			if (lastPrice != 0) {
				if (curPrice > lastPrice) {
					numOfPriceRise++;
					sumOfRiseValue += curPrice - lastPrice;

					seqOfPriceRise++;
					seqOfPriceRiseValue += curPrice - lastPrice;

					seqOfPriceDrop = 0;

					if (seqOfPriceDropValue != 0) {
						lastSeqOfPriceDropValue = seqOfPriceDropValue;
					}
					seqOfPriceDropValue = 0;
				} else {
					numOfPriceDrop++;
					sumOfDropValue += lastPrice - curPrice;

					seqOfPriceDrop++;
					seqOfPriceDropValue += lastPrice - curPrice;

					seqOfPriceRiseValue = 0;
					seqOfPriceRise = 0;
				}
			}
		}
		// continue rising
		if (sumOfRiseValue > 75) {
			return 5;
		}

		// continue rising
		if (seqOfPriceRise > 10) {
			return 5;
		}
		// drop in the price
		if (seqOfPriceDrop > 5) {
			return -10;
		}

		// rise in price
		if (seqOfPriceRiseValue > 50) {
			return 5;
		}

		// rise in price
		if ((seqOfPriceRiseValue > 5) && (lastSeqOfPriceDropValue > 25)) {
			return 5;
		}

		return 0;
	}
	
	private void updateBids(int auction, Quote quote) {
		int getAuctionType = agent.getAuctionType(auction);
		int alloc = agent.getAllocation(auction)-agent.getOwn(auction);
		updatePricePredictions();

		if (agent.getAllocation(auction) > agent.getProbablyOwn(auction)
				&& agent.getAllocation(auction) > agent.getOwn(auction)) {

			Bid bid = new Bid(auction);

			if (getAuctionType == TACAgent.TYPE_INFLIGHT) {
				bid.addBidPoint(alloc, in_futPrices[auction]);


			}
			else if (getAuctionType == TACAgent.TYPE_OUTFLIGHT) {
				bid.addBidPoint(alloc, out_futPrices[auction - 4]);

			}
			agent.submitBid(bid);

		} else {
			log.fine("*** No tickets needed ***");

		}
	}





//MANOS


  //QUOTE UPDATED--------------------------------------------------------

  public void quoteUpdated(Quote quote) {
    int auction = quote.getAuction();
    int auctionCategory = agent.getAuctionCategory(auction);
	// Nadia
    //FLIGHTS --------------------------------------------------------
    if (auctionCategory == TACAgent.CAT_FLIGHT) {
		updateBids(auction, quote);
	}
    //manos
    float askPrice;

  //HOTELS--------------------------------------------------------

    if (auctionCategory == TACAgent.CAT_HOTEL) {
      
      int alloc = agent.getAllocation(auction) - agent.getOwn(auction);
      askPrice=quote.getAskPrice();

      float newBid = increaseBidBy(auction, askPrice);
      if (alloc > 0 && quote.hasHQW(agent.getBid(auction)) && quote.getHQW() < alloc){
	   
	Bid bid = new Bid(auction);
	// Can not own anything in hotel auctions...
 /* System.out.println("MANOSold::::::OLD price for NOT middle day auctions : "+prices[auction]);

	prices[auction] = askPrice + (askPrice - prices[auction]) + 50;
  System.out.println("MANOSnot::::::new price for NOT middle day auctions : "+prices[auction]);
  if(auction==9 || auction==10  ||  auction==12 || auction==13  ){
      prices[auction] = askPrice + (askPrice - prices[auction]) + 50;
        System.out.println("MANOS::::::new price for  MIDDLE day auctions : "+prices[auction]);
*/
  

	 //margin for hotel auctions
   if(newBid>449){
   newBid=449;
    }

  bid.addBidPoint(alloc, newBid);
	
  if (DEBUG) {
	  log.finest("HOTELS--------submitting bid with alloc="
		     + agent.getAllocation(auction)
		     + " own=" + agent.getOwn(auction)+ " with new price "+newBid);

	}
 
   agent.submitBid(bid);
  
      }
 //HOTELS--------------------------------------------------------





     } else if (auctionCategory == TACAgent.CAT_ENTERTAINMENT) {
      int alloc = agent.getAllocation(auction) - agent.getOwn(auction); 
      if (alloc != 0) {
  Bid bid = new Bid(auction);
  if (alloc < 0){
  //----------------------Konstantinos------------------------//
    prices[auction] = calculateSellPrice(quote, auction);
    bid.addBidPoint(alloc, prices[auction]);
    agent.submitBid(bid);
  }
  else{

      if ( (agent.getOwn((auction % 4)+8)> 0) || (agent.getOwn((auction % 4)+12)> 0) ) {
            prices[auction] = calculateBuyPrice(quote,auction);
            bid.addBidPoint(alloc, prices[auction]);
            agent.submitBid(bid);
      } 
  }
  
  if (DEBUG) {
    log.finest("submitting bid with alloc="
         + agent.getAllocation(auction)
         + " own=" + agent.getOwn(auction));
  }
  
      }
    }
  }
//---------------- end of quoteUpdate--------------------------//

  //----------------------Konstantinos------------------------//
//----------------------------------------private void calculate boundaries

  public void entBoundaries(){
  for (int i = 0;i<8;i++){
      int e1 = agent.getClientPreference(i,TACAgent.E1);
      int e2 = agent.getClientPreference(i,TACAgent.E2);
      int e3 = agent.getClientPreference(i,TACAgent.E3);

      if (i==0){
        entUpperLowerBoundary[0][1] = e1;
        entUpperLowerBoundary[1][1] = e2;
        entUpperLowerBoundary[2][1] = e3;
      }
      

      //Get the Upper Boundaries for the entertainment tickets
      if ((e1 > e2) && (e1 > e3) ){
        if (e1 > entUpperLowerBoundary[0][0] ){
            entUpperLowerBoundary[0][0] = e1;
        }
      }
      if ((e2 > e1) && (e2 > e3) ){
        if (e2 > entUpperLowerBoundary[1][0] ){
            entUpperLowerBoundary[1][0] = e2;
        }
      }
      if ((e3 > e1) && (e3 > e2) ){
        if (e3 > entUpperLowerBoundary[2][0] ){
            entUpperLowerBoundary[2][0] = e3;
        }
      }

      //Get the Lower boundaries for the entertainment tickets
      if ((e1 < e2) && (e1 < e3) ){
        if (e1 < entUpperLowerBoundary[0][1] ){
            entUpperLowerBoundary[0][1] = e1;
        }
      }
      if ((e2 < e1) && (e2 < e3) ){
        if (e2 < entUpperLowerBoundary[1][1] ){
            entUpperLowerBoundary[1][1] = e2;
        }
      }
      if ((e3 < e1) && (e3 < e2) ){
        if (e3 < entUpperLowerBoundary[2][1] ){
            entUpperLowerBoundary[2][1] = e3;
        }
      } 
    }
  }



  //----------------------Konstantinos------------------------//
//-------------------------Calculate sell price----------------------------------------------------
private float calculateSellPrice(Quote quote, int auction){
  float quoteSellPrice = quote.getBidPrice();
  if (agent.getAuctionType(auction) == TACAgent.TYPE_ALLIGATOR_WRESTLING){
    if ((quoteSellPrice >= entUpperLowerBoundary[0][1]) && (quoteSellPrice <= entUpperLowerBoundary[0][0])){
      return quoteSellPrice;
    }
    else{
      return (entUpperLowerBoundary[0][0] - (((entUpperLowerBoundary[0][0])/100)*((agent.getGameTime()/6000)^2)));
    }

    
  }

  else if (agent.getAuctionType(auction) == TACAgent.TYPE_AMUSEMENT){
    if ((quoteSellPrice >= entUpperLowerBoundary[1][1]) && (quoteSellPrice <= entUpperLowerBoundary[1][0])){
      return quoteSellPrice;
    }
    else{
      return  (entUpperLowerBoundary[1][0] - (((entUpperLowerBoundary[1][0])/100)*((agent.getGameTime()/6000)^2)));
    }
  }


  else{
    if ((quoteSellPrice >= entUpperLowerBoundary[2][1]) && (quoteSellPrice <= entUpperLowerBoundary[2][0])){
      return quoteSellPrice;
    }
    else{
      return  (entUpperLowerBoundary[2][0] - (((entUpperLowerBoundary[2][0])/100)*((agent.getGameTime()/6000)^2)));
    }

  }
}

  //----------------------Konstantinos------------------------//
//-----------------------Calculate Buy price-------------------------------------------------------------
private float calculateBuyPrice(Quote quote, int auction){
  float quoteBuyPrice = quote.getAskPrice();
  if (agent.getAuctionType(auction) == TACAgent.TYPE_ALLIGATOR_WRESTLING){
    if ((quoteBuyPrice >= entUpperLowerBoundary[0][1]) && (quoteBuyPrice <= entUpperLowerBoundary[0][0])){
      return quoteBuyPrice;
    }
    else{
      return (entUpperLowerBoundary[0][1] + (((entUpperLowerBoundary[0][1])/100)*((agent.getGameTime()/6000)^2)));
    }   
  }

  else if (agent.getAuctionType(auction) == TACAgent.TYPE_AMUSEMENT){
    if ((quoteBuyPrice >= entUpperLowerBoundary[1][1]) && (quoteBuyPrice <= entUpperLowerBoundary[1][0])){
      return quoteBuyPrice;
    }
    else{
      return  (entUpperLowerBoundary[1][1] + (((entUpperLowerBoundary[1][1])/100)*((agent.getGameTime()/6000)^2)));
    }
  }


  else{
    if ((quoteBuyPrice >= entUpperLowerBoundary[2][1]) && (quoteBuyPrice <= entUpperLowerBoundary[2][0])){
      return quoteBuyPrice;
    }
    else{
      return  (entUpperLowerBoundary[2][1] + (((entUpperLowerBoundary[2][1])/100)*((agent.getGameTime()/6000)^2)));
    }
  }
}








//----------------------------------increased bid-------------------------

public float increaseBidBy(int auction, float askPrice){
  float newBidf;
  if(askPrice==0){
      prices[auction] = askPrice + 30;
  }

  else{
     prices[auction] = askPrice + (askPrice - prices[auction]) + 30;
      //if auction is for middle days then bid +29 higher
      if(auction==9 || auction==10  ||  auction==12 || auction==13  ){
        prices[auction] = prices[auction] + 29;
      }
  }
  newBidf = prices[auction];
  return newBidf;
}

//------------end----------------------increased bid-------------------------




  public void quoteUpdated(int auctionCategory) {
    log.fine("All quotes for "
	     + agent.auctionCategoryToString(auctionCategory)
	     + " has been updated");
  }

  public void bidUpdated(Bid bid) {
    log.fine("Bid Updated: id=" + bid.getID() + " auction="
	     + bid.getAuction() + " state="
	     + bid.getProcessingStateAsString());
    log.fine("       Hash: " + bid.getBidHash());
  }

  public void bidRejected(Bid bid) {
    log.warning("Bid Rejected: " + bid.getID());
    log.warning("      Reason: " + bid.getRejectReason()
		+ " (" + bid.getRejectReasonAsString() + ')');
  }

  public void bidError(Bid bid, int status) {
    log.warning("Bid Error in auction " + bid.getAuction() + ": " + status
		+ " (" + agent.commandStatusToString(status) + ')');
  }




//-----------------------------GAME STARTED-----------------------------------
  public void gameStarted() {
    log.fine("Game " + agent.getGameID() + " started!");
    entBoundaries();
    calculateAllocation();
    sendBids();




    ActionListener hotelReallocation = new ActionListener() {    
      public void actionPerformed(ActionEvent evt) {
        log.fine("---------------------Timer is on---------------------");
        Quote quote;
        for (int i = 8; i < 16; i++) {
          quote = agent.getQuote(i);
          //quoteUpdated(quote);
          interimHotelAllocation(quote);
        }
      } 
    }; 


    keepd = new Timer(1*60*1000, hotelReallocation);
    keepd.setInitialDelay(1*71*1000);
    keepd.start();


  }
//-----------------------------end GAME STARTED-----------------------------------
/* if we could try to keep some data about previous games. it would make sense only if we played against the same agents
  public void keepdata(){
      for (int i = 8; i < 16; i++) {
        Quote quote1 = agent.getQuote(i);
        float closPrice = quote1.getAskPrice();
        if (pricesInMinutes[i-8]==0){
         pricesInMinutes[i-8] = closPrice;
         count[i-8]++;
       }
        else{
          pricesInMinutes[i-8] = (pricesInMinutes[i-8] + closPrice) / count[i-8];
          System.out.println("------------DIVISION: "+pricesInMinutes[i-8]+" /TIME: "+count[i-8]);
        }
        System.out.println("-----KEEPDATA: "+pricesInMinutes[i-8]+" ,gameID:"+agent.getGameID());
      }
  }
*/



public void interimHotelAllocation(Quote quote){//this quote is for auction from 8 to 15 for hotels
    int auction = quote.getAuction();
    int alloc = agent.getAllocation(auction);
    int own = agent.getOwn(auction);
    int diff = alloc-own; 

    int inFlight;
    int outFlight;
    int day = agent.getAuctionDay(auction);

   /* if (quote.isAuctionClosed() && alloc > own) {
      
       for (int j = 0; j < diff; j++) {
      for(int i=0; i<8; i++){
          inFlight = agent.getClientPreference(i, TACAgent.ARRIVAL);
          outFlight = agent.getClientPreference(i, TACAgent.DEPARTURE);
          int duration = outFlight - inFlight;
            System.out.println("inside customers");
		//remove all flight tickets from auction's allocation, so not to buy if the agent is not able of making a feasible package
            if ((duration) == 1){
System.out.println("inside duration==1");

               if ( inFlight==day ){//if it is outflight day we are ok, because we do not want any hotel in the outflight day
               
					//change hotel
                  if (agent.getAuctionType(auction) == TACAgent.TYPE_CHEAP_HOTEL) {
                    //if ( !( agent.auctionClosed(auction+4))) {//we change hotel type to our client
                      //agent.setAllocation(auction, agent.getAllocation(auction) - 1);//useless because the auction is closed
                      auction = agent.getAuctionFor(TACAgent.CAT_HOTEL,TACAgent.TYPE_GOOD_HOTEL, day);
                      agent.setAllocation(auction, agent.getAllocation(auction) + 1);
                    //}
                    //else{//we change airplane tickets to our client, which is not profitable

System.out.println("cheap hotel, duration 1");

                    //}

                  } else {  //EXPENSIVE HOTEL
                     // if ( !( agent.auctionClosed(auction-4))) {//we change hotel type to our client
                        //agent.setAllocation(auction, agent.getAllocation(auction) - 1);//useless because the auction is closed
                        auction = agent.getAuctionFor(TACAgent.CAT_HOTEL,TACAgent.TYPE_CHEAP_HOTEL, day);
                        agent.setAllocation(auction, agent.getAllocation(auction) + 1);   
                     // }//end if
                     // else{//we change airplane tickets to our client, which is not profitable

System.out.println("expensive hotel, duration 1");

                      //}//end else
                  } //end expensive hotel               
               }//end if inflight=day   
              
             }//end if duration == 1
            /*else if ( (duration) != 1 ){

                switch (duration) { 
                
                case 2:
                System.out.println("case2");

                    if (day==outFlight-1){//change outflight to this day. cheap hotel minus 4, exp hotel -7 to go to outflights.
                      if (agent.getAuctionType(auction) == TACAgent.TYPE_CHEAP_HOTEL){
                      agent.setAllocation((auction-4), agent.getAllocation(auction-4) + 1);//for outflight
                      //agent.setAllocation((auction+1), agent.getAllocation(auction+1) - 1  );
                      }
                      else if (agent.getAuctionType(auction) == TACAgent.TYPE_GOOD_HOTEL){
                      agent.setAllocation(auction-7, agent.getAllocation(auction-7) + 1);//for outflight
                      //agent.setAllocation(auction+1, agent.getAllocation(auction+1) - 1);
                      }
                    }
                break;



                case 3:
                System.out.println("case3");

                      if(day == outFlight-1){
                      	                System.out.println("case3- ins prolast day");

                        if (agent.getAuctionType(auction) == TACAgent.TYPE_CHEAP_HOTEL){
                            agent.setAllocation((auction-3), agent.getAllocation(auction-3) - 1);//remove the initial request for flight allocation                            
                            agent.setAllocation((auction-4), agent.getAllocation(auction-4) + 1);//add a request for an outflight the day we lost the hotel auction
                        }
                        else if(agent.getAuctionType(auction) == TACAgent.TYPE_CHEAP_HOTEL){
                            agent.setAllocation((auction-6), agent.getAllocation(auction-6) - 1);//remove the initial request for flight allocation                            
                            agent.setAllocation((auction-7), agent.getAllocation(auction-7) + 1);//add a request for an outflight the day we lost the hotel auction                      
                        }
                      }

                      else if (day == outFlight-2){
                      		                System.out.println("case3- in pro prolast day");

                         if(agent.getAuctionType(auction) == TACAgent.TYPE_CHEAP_HOTEL){
                            agent.setAllocation((auction- 5), agent.getAllocation(auction - 3) - 1);
                            agent.setAllocation((auction- 8), agent.getAllocation(auction - 8) +1);  
                          }
                          else if(agent.getAuctionType(auction) == TACAgent.TYPE_CHEAP_HOTEL){
                            agent.setAllocation((auction-8), agent.getAllocation(auction-8) - 1);//remove the initial request for flight allocation                            
                            agent.setAllocation((auction-11), agent.getAllocation(auction-11) + 1);//add a request for an outflight the day we lost the hotel auction                      
                        }
                      }



                break;

                case 4://drop the inflight and outflight tickets
                      //ASK THE TEAM FOR WHAT TO DO!!
                      //for it is worthless to change the whole client preferences
                        
                        agent.setAllocation(0, agent.getAllocation(0) - 1);
                        agent.setAllocation(7, agent.getAllocation(7) - 1);
                break;

                
                }//end case
            }//end else if


      }//end for: clients 

     

}//end for : difference
}//end if
*/
}//end interimhotelallocation



  public void gameStopped() {
    log.fine("Game Stopped!");
  }

  public void auctionClosed(int auction) {
    log.fine("*** Auction " + auction + " closed!");
  }



//-----------------------------SEND BIDS-----------------------------------

  private void sendBids() {
    for (int i = 0, n = agent.getAuctionNo(); i < n; i++) {
      int alloc = agent.getAllocation(i) - agent.getOwn(i);
     // float price = 320f;
      //manos

      float price=-1f;
      switch (agent.getAuctionCategory(i)) {
//      case TACAgent.CAT_FLIGHT:
//	
//	break;

  //------------------------------------------------HOTELS send bids------------------------------------------------
      case TACAgent.CAT_HOTEL:
	if (alloc > 0) {
    System.out.println("Wanted hotel rooms!");
	  
    if(i>=8 && i<12){//cheap hotel auction
      if(i==8 || i==11){//not middle days
          price = 80;//initial bid
          prices[i] = 80f;
      }//END OF IF NOT MIDDLE DAYS

      else if( i==9 || i==10){//middle days
          
          price = 95;//initial bid for middle days
          prices[i] = 95f;
        }

      }//END OF CHEAP HOTEL
    

    else{//initial bidding for expensive hotel

      if(i>=12 && i<16){//EXPENSIVE hotel auction
       if( i==15){//last day
           price = 145;
           prices[i] = 145f;

       }//END OF IF NOT MIDDLE DAYS

       else if(i==12 || i==13 || i==14){//3 first days

           price = 160;
           prices[i] = 160f;
         }
         }
       }
    }
  
//initial bidding for unwanted hotels, for improving flexibility
  else if (agent.getAllocation(i)<=0){
        System.out.println("Unwanted hotel rooms, just for flexibility.");
        alloc=2;
    price=18;
    prices[i]=18f;
  }
  break;


  //------------------------------------------------END HOTELS send bids------------------------------------------------
          case TACAgent.CAT_ENTERTAINMENT:
  if (alloc < 0) {
  //----------------------Konstantinos------------------------//
    price = 0;
    prices[i] = 0f;
  } else if (alloc > 0) {
    price = 0;
    prices[i] = 0f;
  }
  break;
      default:
	break;
      }
      if (price > 0) {
	Bid bid = new Bid(i);
  //manos margin to hotel auction
  if((agent.getAuctionCategory(i)==TACAgent.CAT_HOTEL) && price>399){
    price=403;
  }

	   bid.addBidPoint(alloc, price);
	if (DEBUG) {
	  log.finest("submitting bid with alloc=" + agent.getAllocation(i)
		     + " own=" + agent.getOwn(i));


  
	}

    if(agent.getAuctionCategory(i)==TACAgent.CAT_HOTEL){
    System.out.println("---------submitting bid with alloc=" + agent.getAllocation(i)
         + " own=" + agent.getOwn(i));
  }
	agent.submitBid(bid);
      }
    }
  }

//-----------------------------SEND BIDS-----------------------------------







//------------------------------------------------CALC ALLOCATION------------------------------------------------
  private void calculateAllocation() {
    for (int i = 0; i < 8; i++) {
      int inFlight = agent.getClientPreference(i, TACAgent.ARRIVAL);
      int outFlight = agent.getClientPreference(i, TACAgent.DEPARTURE);
      int hotel = agent.getClientPreference(i, TACAgent.HOTEL_VALUE);
      int type;

      // Get the flight preferences auction and remember that we are
      // going to buy tickets for these days. (inflight=1, outflight=0)
      int auction = agent.getAuctionFor(TACAgent.CAT_FLIGHT,
					TACAgent.TYPE_INFLIGHT, inFlight);
      agent.setAllocation(auction, agent.getAllocation(auction) + 1);
      auction = agent.getAuctionFor(TACAgent.CAT_FLIGHT,
				    TACAgent.TYPE_OUTFLIGHT, outFlight);
      agent.setAllocation(auction, agent.getAllocation(auction) + 1);


//------------------------------------------------HOTELS    CALC ALLOCATION-------------
      // if the hotel value is greater than 70 we will select the
      // expensive hotel (type = 1)
      if ((hotel >85) || ((outFlight - inFlight)<3 )) {
	type = TACAgent.TYPE_GOOD_HOTEL;
      } else {
	type = TACAgent.TYPE_CHEAP_HOTEL;
      }
      // allocate a hotel night for each day that the agent stays
      for (int d = inFlight; d < outFlight; d++) {
	auction = agent.getAuctionFor(TACAgent.CAT_HOTEL, type, d);
	log.finer("--------------------Adding hotel for day: " + d + " on " + auction);
	System.out.println("--------------------Adding hotel for day: " + d + " on " + auction);
  agent.setAllocation(auction,  agent.getAllocation(auction) + 1);
      }
//------------------------------------------------end  HOTELS   CALC ALLOCATION-------------



      int eType = -1;
      while((eType = nextEntType(i, eType)) > 0) {
	auction = bestEntDay(inFlight, outFlight, eType);
	log.finer("Adding entertainment " + eType + " on " + auction);
	agent.setAllocation(auction, agent.getAllocation(auction) + 1);
      }
    }
  }

  private int bestEntDay(int inFlight, int outFlight, int type) {
    for (int i = inFlight; i < outFlight; i++) {
      int auction = agent.getAuctionFor(TACAgent.CAT_ENTERTAINMENT,
					type, i);
      if (agent.getAllocation(auction) < agent.getOwn(auction)) {
	return auction;
      }
    }
    // If no left, just take the first...
    return agent.getAuctionFor(TACAgent.CAT_ENTERTAINMENT,
			       type, inFlight);
  }

  private int nextEntType(int client, int lastType) {
    int e1 = agent.getClientPreference(client, TACAgent.E1);
    int e2 = agent.getClientPreference(client, TACAgent.E2);
    int e3 = agent.getClientPreference(client, TACAgent.E3);

    // At least buy what each agent wants the most!!!
    if ((e1 > e2) && (e1 > e3) && lastType == -1)
      return TACAgent.TYPE_ALLIGATOR_WRESTLING;
    if ((e2 > e1) && (e2 > e3) && lastType == -1)
      return TACAgent.TYPE_AMUSEMENT;
    if ((e3 > e1) && (e3 > e2) && lastType == -1)
      return TACAgent.TYPE_MUSEUM;
    return -1;
  }



  // -------------------------------------------------------------------
  // Only for backward compability
  // -------------------------------------------------------------------

  public static void main (String[] args) {
    TACAgent.main(args);
  }

} // DummyAgent
