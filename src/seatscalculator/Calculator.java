package seatscalculator;

/**
 *
 * @author Redjan Shabani
 */
public class Calculator
{
	private final int seats;
	private final int[] votes;
	
	public Calculator(int seats, int[] votes)
	{
		this.seats = seats;
		this.votes = votes;
	}
	
	public int getSeats()
	{
		return seats;
	}
	
	public int[] getVotes()
	{
		return votes;
	}
	
	public int getTotVotes()
	{
		int totVotes = 0;
		for(int i=0; i<this.votes.length; i++)
			totVotes += this.votes[i];
		return totVotes;
	}
	
	public float[] getPercentages()
	{
		float[] pcs = new float[this.votes.length];
		
		for(int i=0; i<votes.length; i++)
			pcs[i] = 100.0f * this.votes[i] / this.getTotVotes();
		
		return pcs;
	}
	
	public float[] getIdealSeats()
	{
		float[] idealSeats = new float[this.votes.length];
		
		for(int i=0; i<votes.length; i++)
			idealSeats[i] = (float) this.seats * this.votes[i] / this.getTotVotes();
		
		return idealSeats;
	}
	
	public static final int HARE_NIEMEYER_METHOD = 1;
	public static final int DHONDT_METHOD = 2;
	public static final int SAINT_LAGUE_METHOD = 3;
	public int[] getSeats(int method)
	{
		//apply threshold
		
		//????????????????????????
		
		
		int[] seats = new int[this.votes.length];
		if(method == Calculator.HARE_NIEMEYER_METHOD)
			return hnm(this.seats, this.votes);
		else if(method == Calculator.DHONDT_METHOD)
			return dm(this.seats, votes);
		else if(method == Calculator.SAINT_LAGUE_METHOD)
			return slm(this.seats, votes);
		return seats;
	}
	
	private static int[] hnm(int totSeats, int[] votes)
	{
		int[] seats = new int[votes.length];
		
		return seats;
	}
	
	private static int[] dm(int totSeats, int[] votes)
	{
		int[] seats = new int[votes.length];
		
		if(totSeats < 1)
			return seats;
		
		if(votes.length==0)
			return seats;
		
		//computing the D'Hondt table
		int[][] quotTable = new int[totSeats][votes.length];
		for(int r=0; r<totSeats; r++)
		{
			for(int c=0; c<votes.length; c++)
			{
				quotTable[r][c] = Math.round( votes[c] / (r + 1.0f) );
			}
		}
		
		//counting the first (seats)-greatest quotiens
		for(int i = 0; i < totSeats; i++)
		{
			//find the max value indeces
			int max = Integer.MIN_VALUE;
			int R = 0, C = 0;
			for(int r=0; r<quotTable.length; r++)
			{
				for(int c=0; c<quotTable[0].length; c++)
				{
					if(max < quotTable[r][c])
					{
						max = quotTable[r][c];
						R = r;
						C = c;
					}
				}
			}
			quotTable[R][C] = -quotTable[R][C];
			seats[C]++;// +1 seat for the corresponding subject 
		}
		
		return seats;
	}
	
	private static int[] slm(int totSeats, int[] votes)
	{
		int[] seats = new int[votes.length];
		
		if(totSeats < 0)
			return seats;
		
		if(votes.length==0)
			return seats;
		
		
		//computing the D'Hondt table
		int[][] quotTable = new int[totSeats][votes.length];
		for(int r=0; r<totSeats; r++)
		{
			for(int c=0; c<votes.length; c++)
			{
				quotTable[r][c] = Math.round( votes[c] / (2*r + 1.0f) );
			}
		}
		
		//counting the first (seats)-greatest quotiens
		for(int i = 0; i < totSeats; i++)
		{
			//find the max value indeces
			int max = Integer.MIN_VALUE;
			int R = 0, C = 0;
			for(int r=0; r<quotTable.length; r++)
			{
				for(int c=0; c<quotTable[0].length; c++)
				{
					if(max < quotTable[r][c])
					{
						max = quotTable[r][c];
						R = r;
						C = c;
					}
				}
			}
			quotTable[R][C] = -quotTable[R][C];
			seats[C]++;// +1 seat for the corresponding subject 
		}
		
		return seats;
	}
}
