import java.lang.*;


public class Point {
	public double x;
	public double y;

	public Point(double a, double b) {
        x = a;
        y = b;
    }


public static Point findTropInter(Point a, Point b)
{
	//test for downsloping intersection
	if ((a.x - b.x) * (a.y - b.y) < 0)
	{
		return(new Point(Math.max(a.x,b.x), Math.max(a.y,b.y)));
	}

	//test for non-transverse x intersection
	if(a.x == b.x)
	{
		return(new Point(a.x, Math.max(a.y, b.y)));
	}

	//test for non-transverse y intersection
	if(a.y == b.y)
	{
		return(new Point(Math.max(a.x, b.x), a.y));
	}

	//test for non-transverse diagonal intersection
	if (a.x - a.y == b.x - b.y) 
	{
		return(new Point(Math.min(a.x,b.x), Math.min(a.y,b.y)));
	}

	//test for a larger and x relatively larger, or smaller
	if (a.x > b.x)
	{
		if (a.x - a.y > b.x - b.y) 
		{
			return(new Point(a.x - (a.y-b.y), b.y));
		}

		if (a.x - a.y < b.x - b.y) 
		{
			return(new Point(b.x, a.y - (a.x - b.x)));
		}
	}

	//same for b larger
	if (b.x > a.x)
	{
		if (b.x - b.y > a.x - a.y) 
		{
			return(new Point(b.x - (b.y-a.y), a.y));
		}

		if (b.x - b.y < a.x - a.y) 
		{
			return(new Point(a.x, b.y - (b.x - a.x)));
		}
	}

	return a;
}


public static Point[] findAllInter(Point[] vertices)
{
	int resultLength  = vertices.length * (vertices.length + 1) / 2;
	int counter = 0;
	Point[] result = new Point[resultLength];
	//returns vertices followed by the new intersections
	for (int i = 0; i < vertices.length; i++) {
		
			result[counter] = findTropInter(vertices[i], vertices[i]);
			counter++;
			
	}
	for (int i = 0; i < vertices.length; i++) {
		for (int j = i+1; j < vertices.length; j++) {
			result[counter] = findTropInter(vertices[i], vertices[j]);
			counter++;
		}	
	}

	return result;
}

public static boolean hasNAbove(int n, Point p, Point[] vertices)
{
	int tally = 0;

	for (int i =0; i < vertices.length; i++) {
		boolean flag = false;
		if (p.x < vertices[i].x) {
			flag = true;
		}
		else if (p.y < vertices[i].y) {
			flag = true;
		}


		if (flag) {
			tally++;
		}
	}

	return tally == n;
}

//finds prices for n units given bids in vertices, with two goods
public static Point[] findPrices(int n, Point[] vertices)
{
	Point[] inters = findAllInter(vertices);

	//for two people, this needs to be thought about more
	Point[] prices = new Point[n+1];
	int counter = 0;

	for (int i = 0; i < inters.length; i++) {
		if (hasNAbove(n, inters[i], vertices)) {
			prices[counter] = inters[i];
			counter++;
		}
	}

	return prices;
}

public static boolean comparePrices(Point price1, Point price2)
{
	if(price1.x < price2.x || (price1.x == price2.x && price1.y > price2.y))
	{
		return true;
	}
	else
		return false;
}

public static boolean equalPoints(Point point1, Point point2)
{
	if (point1.x == point2.x && point1.y == point2.y) {
		return true;
	}
	else
		return false;
}

//sorts prices in order of number of Y goods allocated from 0 to n
public static Point[] sortPrices(Point[] prices)
{
	Point temp;

	for (int i = 1; i < prices.length; i++) {
		for (int j = i; j > 0; j--) {
			if (comparePrices(prices[j], prices[j-1]))
			{
				temp = prices[j];
				prices[j] = prices[j-1];
				prices[j-1] = temp;
			}
		}
	}

	return prices;
}


public static Point choosePrice(Point[] prices)
{
	int index = (int) Math.floor(Math.random() * prices.length);
	return(prices[index]);
}


public static double getUtility(Point[] myBids, Point[] myValues, Point[] prices, Point[] allBids)
{
	double utility = 0;
	int numbids = myBids.length;

	for (int i = 0; i < prices.length; i++) {
		for (int j = 0; j < numbids; j++) {

			if (myBids[j].x > prices[i].x && myBids[j].x - myBids[j].y > prices[i].x - prices[i].y) {
				utility += myValues[j].x - prices[i].x;
			}
			else if (myBids[j].y > prices[i].y && myBids[j].x - myBids[j].y < prices[i].x - prices[i].y) {
				utility += myValues[j].y - prices[i].y;
			}
			else if (myBids[j].x > prices[i].x && myBids[j].x - myBids[j].y == prices[i].x - prices[i].y) {
				int yCounter = 0;
						for (int k = 0; k < allBids.length; k++) {
							if (equalPoints(allBids[k], myBids[j]) == false && allBids[k].y > prices[i].y && allBids[k].x - allBids[k].y < prices[i].x - prices[i].y) {
								yCounter++;
							}
						}
						//note that the order of prices ensures that i should be number of y goods
						if (yCounter == i) {
							utility += myValues[j].x - prices[i].x;
						}
						else {
							utility += myValues[j].y - prices[i].y;
						}
					}
			}
		}

	return (utility / (double) prices.length);
}

//
public static Point[] bestResponse(Point[] vertices, int numBidsPerPerson)
{
	Point[] values = new Point[numBidsPerPerson];
	for (int i = 0; i < numBidsPerPerson; i++) {
		values[i] = vertices[i];
	}

	Point[] bids = new Point[numBidsPerPerson];

	Point[] prices = findPrices(numBidsPerPerson, vertices);

	for (int i = 0; i < numBidsPerPerson; i++) {
		boolean xflag = false;
		boolean yflag = false;

		for (int j = 0; j < prices.length; j++) {
			if (values[i].x == prices[j].x) {
				//test if you are paying the price you set
				for (int k = 0; k < numBidsPerPerson; k++) {
					if(values[i].x < values[k].x)
					{
						xflag = true;
					}
				}

			}
			if (values[i].y == prices[j].y) {
				for (int k = 0; k < numBidsPerPerson; k++) {
					if(values[i].y < values[k].y)
					{
						yflag = true;
					}
				}
			}
		}
		double bidx;
		double bidy;

		if (xflag) {
			bidx = 0;
		}
		else {
			bidx = values[i].x;
		}

		if (yflag)
		{
			bidy = 0;
		}
		else {
			bidy = values[i].y;
		}

		bids[i] = new Point(bidx, bidy);
	}
	return bids;
}

//MAIN METHOD TO PRINT RESULTS OF ONE TRIAL

// public static void main(String[] args)
// {
// 	int numPoints = 4;

// 	Point[] tester = new Point[numPoints];

// 	for (int i = 0; i < numPoints; i++) {
// 		tester[i] = new Point(Math.random(),Math.random());
// 	}

// 	Point[] result = findAllInter(tester);

// 	Point[] prices = findPrices(2, tester);

// 	Point[] bids = bestResponse(tester, 2);

// 	System.out.println("Intersections:\n");
// 	for (int i = 0; i < result.length; i++) {
// 		System.out.println("(" + result[i].x + ", " + result[i].y + ")\n");
// 	}

// 	System.out.println("Prices:\n");
// 	for (int i = 0; i < prices.length; i++) {
// 		System.out.println("(" + prices[i].x + ", " + prices[i].y + ")\n");
// 	}

// 	System.out.println("Best response for player 1\n");
// 	for (int i = 0; i < bids.length; i++) {
// 		System.out.println("(" + bids[i].x + ", " + bids[i].y + ")\n");
// 	}
// }


//MAIN METHOD TO CALCULATE UTILITY OF BIDDING VALUES
// Three players, three bids, three goods
// public static void main(String[] args) {
// 	Point[] bids = new Point[9];

// 	Point a = new Point(0, 0);
// 	Point b = new Point(.5, .51);
// 	Point c = new Point(.9, .915);

// 	bids[0] = a;
// 	bids[1] = a;
// 	bids[2] = c;
// 	for (int i = 3; i < 9; i++) {
// 		bids[i] = new Point(Math.random(), Math.random());
// 	}

// 	Point[] myBids = new Point[3];
// 	myBids[0] = a;
// 	myBids[1] = a;
// 	myBids[2] = c;

// 	Point[] myValues = new Point[3];
// 	myValues[0] = a;
// 	myValues[1] = b;
// 	myValues[2] = c;


// 	double utility = 0; 
// 	int n = 100000;

// 	for (int i = 0; i < n; i++) {
// 		for (int j = 3; j < 9; j++) {
// 		bids[j] = new Point(Math.random(), Math.random());
// 		}

// 		Point[] prices = sortPrices(findPrices(3, bids));

// 		utility += getUtility(myBids, myValues, prices, bids);
// 	}

// 	System.out.println(utility/ ((double) n));
// }


//Main method to calculate utility, three players two bids two goods

// public static void main(String[] args) {
// 	for (int k = 0; k < 30; k++) {
		
	
// 	Point[] bids = new Point[10];

// 	Point a = new Point(.1, .1);
// 	Point b = new Point(.5, .51);

// 	bids[0] = a;
// 	bids[1] = b;
// 	for (int i = 2; i < 10; i++) {
// 		bids[i] = new Point(Math.random(), Math.random());
// 	}

// 	Point[] myBids = new Point[2];
// 	myBids[0] = a;
// 	myBids[1] = b;

// 	Point[] myValues = new Point[2];
// 	myValues[0] = new Point(.4,.4);
// 	myValues[1] = b;


// 	double utility = 0; 
// 	int n = 10000000;

// 	for (int i = 0; i < n; i++) {
// 		for (int j = 2; j < 10; j++) {
// 			bids[j] = new Point(Math.random(), Math.random());
// 		}

// 		Point[] prices = sortPrices(findPrices(2, bids));

// 		utility += getUtility(myBids, myValues, prices, bids);
// 	}

// 	System.out.println(utility);
// }
// }

//Main method 2 and 2
public static void main(String[] args) {
		
	
	Point[] bids = new Point[4];

	Point a = new Point(0, 0);
	Point b = new Point(.9, .91);

	bids[0] = a;
	bids[1] = b;
	for (int i = 2; i < 4; i++) {
		bids[i] = new Point(Math.random(), Math.random());
	}

	Point[] myBids = new Point[2];
	myBids[0] = a;
	myBids[1] = b;

	Point[] myValues = new Point[2];
	myValues[0] = new Point(.8,.8);
	myValues[1] = b;


	double[] utility = new double[15]; 
	for (int k = 0; k < utility.length; k++) {
		utility[k] = 0;
	}

	int n = 1000000;

	for (int i = 0; i < n; i++) {
		
		for (int j = 2; j < 4; j++) {
			bids[j] = new Point(Math.random(), Math.random());
		}

		for (int k = 0; k < utility.length; k++) {
			bids[0] = new Point(.05 * k, .05 * k);
			myBids[0] = new Point(.05 * k, .05 * k);

			Point[] prices = sortPrices(findPrices(2, bids));

			utility[k] += getUtility(myBids, myValues, prices, bids);
		}
	}

	
	for (int i =0; i < utility.length; i++) {
		System.out.println(utility[i]);
	}

}


//MAIN METHOD TO CALCULATE DOWN SHIFT USING BEST RESPONSE METHOD
// public static void main(String[] args) {
// 	Point[] bids = new Point[4];

//  	Point a = new Point(.2, .2);
//  	Point b = new Point(.9, .95);

//  	bids[0] = a;
//  	bids[1] = b;
//  	bids[2] = new Point(Math.random(), Math.random());
//  	bids[3] = new Point(Math.random(), Math.random());

//  	Point[] myValues = new Point[2];
//  	myValues[0] = a;
//  	myValues[1] = b;

//  	Point[] myBids = new Point[2];


//  	double myShiftedBidsSummed[] = {0,0,0,0.0};

//  	int n = 10000;

//  	for (int i = 0; i < n; i++) {
//  		bids[2] = new Point(Math.random(), Math.random());
//  		bids[3] = new Point(Math.random(), Math.random());
//  		Point[] trialBids = bestResponse(bids, 2);
//  		for (int j = 0; j < 2; j++) {
//  			myShiftedBidsSummed[2 * j] += trialBids[j].x;
//  			myShiftedBidsSummed[2 * j +1] += trialBids[j].y;
//  		}
//  	}

//  	for (int k = 0; k < 2; k++) {
//  			System.out.println("(" + myShiftedBidsSummed[2* k] / ((double) n) + ", " + myShiftedBidsSummed[2*k + 1] / ((double) n) + " )\n");
//  		}
// }




//MAIN METHOD TO CALCULATE DOWNSHIFT WITH RANDOMNESS
// public static void main(String[] args) {
// 	Point[] bids = new Point[4];

//  	Point a = new Point(.2, .2);
//  	Point b = new Point(.9, .95);

//  	bids[0] = a;
//  	bids[1] = b;
//  	bids[2] = new Point(Math.random(), Math.random());
//  	bids[3] = new Point(Math.random(), Math.random());

//  	Point[] myValues = new Point[2];
//  	myValues[0] = a;
//  	myValues[1] = b;

//  	Point[] trialBids = new Point[2];

//  	Point[] bestBid = new Point[2];
//  	bestBid[0] = new Point(0,0);
//  	bestBid[1] = b;
//  	double maxUtility = 0;

//  	double trialUtility;
// 	int n = 100000;
	

// 	for (int i = 0; i < n; i++) {
	 		
// 	 		bids[2] = new Point(Math.random(), Math.random());
// 	 		bids[3] = new Point(Math.random(), Math.random());

// 	 		Point[] prices = sortPrices(findPrices(2, bids));
//  			maxUtility += getUtility(bestBid, myValues, prices, bids);
// 	}
 	

//  	for (int k = 0; k < n; k++) {

// 	 	trialBids[0] = new Point(Math.random() * a.x, Math.random() * a.y);
// 	 	trialBids[1] = b;

// 	 	trialUtility = 0;

// 		bids[0] = trialBids[0];
// 	 	bids[1] = trialBids[1];


// 	 	for (int i = 0; i < n; i++) {
	 		
// 	 		bids[2] = new Point(Math.random(), Math.random());
// 	 		bids[3] = new Point(Math.random(), Math.random());

// 	 		Point[] prices = sortPrices(findPrices(2, bids));
//  			trialUtility += getUtility(trialBids, myValues, prices, bids);
// 	 	}

// 	 	if(trialUtility > maxUtility)
// 	 	{
// 	 		maxUtility = trialUtility;
// 	 		bestBid[0] = new Point(trialBids[0].x, trialBids[0].y);
// 	 	}
//  	}

//  	System.out.println("(" + bestBid[0].x + ", " + bestBid[0].y + ")\n");
//  	System.out.println(maxUtility / ((double) n));
// }



}