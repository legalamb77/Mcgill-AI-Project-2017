package student_player.mytools;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;

import boardgame.Board;
import boardgame.Move;
import bohnenspiel.BohnenspielBoardState;
import bohnenspiel.BohnenspielMove;

public class MyTools {
	static long timeCutoffNano = 1000000*695;
    
    //Counts the number of empty pits that a given player has on their side.
    public static int countEmpties (int id, BohnenspielBoardState boardstate) {
    	int[][] pits=boardstate.getPits();
    	int empties=0;
    	for (int i=0;i<6;i++)
    	{
    		if(pits[id][i]==0)
    		{
        		empties++;
    		}
    	}
    	return empties;
    }
    
    //Counts the number of beans on the board for a given player
    public static int countBeans (int id, BohnenspielBoardState boardstate) {
    	int[][] pits=boardstate.getPits();
    	int beans=0;
    	for (int i=0;i<6;i++)
    	{
    		beans+=pits[id][i];
    	}
    	return beans;
    }
    
    //A function for testing with one of the simplest evaluation functions, for benchmarking.
    public static double simpleEvaluate(BohnenspielBoardState boardstate, int pid, int eid) {
    	return (boardstate.getScore(pid));//-boardstate.getScore(eid));
    }
    
    //A more complex evaluation function, which takes into account the empty spaces on the board
    public static double complexEvaluate1(BohnenspielBoardState boardstate, int pid, int eid) {
    	int eEmpt=countEmpties(eid,boardstate);
    	double score = 2*boardstate.getScore(pid)+eEmpt;
    	double undesirablePits = 2*boardstate.getScore(eid)+countEmpties(pid,boardstate);
    	if(eEmpt==6)
    	{
    		score+=100; //It is extremely desirable if we can empty all of the opponent's pits, because then we capture everything on our side
    	}
    	double eval = score-undesirablePits;
    	return eval;
    }
    
    //A different complex evaluation function, which takes into account the beans each player has
    public static double complexEvaluate2(BohnenspielBoardState boardstate, int pid, int eid) {
    	double score = 2*boardstate.getScore(pid)+countBeans(pid,boardstate);
    	double undesirablePits = 2*boardstate.getScore(eid)+countBeans(eid,boardstate);
    	double eval = score-undesirablePits;
    	return eval;
    }
    
    //A third function that counts empties and beans
    public static double complexEvaluate3(BohnenspielBoardState boardstate, int pid, int eid) {
    	int eEmpt=countEmpties(eid,boardstate);
    	double score = 2*boardstate.getScore(pid)+eEmpt+countBeans(pid,boardstate);
    	double undesirablePits = 2*boardstate.getScore(eid)+countEmpties(pid,boardstate)+countBeans(eid,boardstate);
    	if(eEmpt==6)
    	{
    		score+=100; //It is extremely desirable if we can empty all of the opponent's pits, because then we capture everything on our side
    	}
    	double eval = score-undesirablePits;
    	return eval;
    }
    
    //This is the evaluation function for the board states. It calls upon auxillary functions for various evaluation methods, to make them easier to test.
    public static double evaluate(BohnenspielBoardState boardstate, int pid, int eid) {
    	return complexEvaluate3(boardstate,pid,eid);
    }

    //Checks if we have exceeded our depth
    public static boolean cutoff (int depthLimit, int depthNow) {
    	boolean check=depthLimit<=depthNow;
    	return check;
    }
    
    public static BohnenspielBoardState moveToState (BohnenspielBoardState stateNow, BohnenspielMove m)
    {
    	BohnenspielBoardState newState=(BohnenspielBoardState) stateNow.clone();
    	newState.move(m);
    	return newState;
    }
    
    //Takes the current state, the moves under consideration, and the pid and eid. Returns the ordered list of states caused by
    //applying the moves, with respect to the evaluation function.
    public static BohnenspielBoardState[] orderStates (BohnenspielBoardState state, BohnenspielMove[] moves,int pid, int eid)
    {
    	BohnenspielBoardState[] states = new BohnenspielBoardState[moves.length];
    	for (int i=0;i<moves.length;i++)
    	{
    		states[i]=(BohnenspielBoardState) state.clone();
    		states[i].move(moves[i]);
    	}
    	Collections.sort(Arrays.asList(moves), (a,b) -> Double.compare(evaluate((BohnenspielBoardState) moveToState(state,a),pid,eid),evaluate((BohnenspielBoardState) moveToState(state,b),pid,eid)));
    	Collections.sort(Arrays.asList(states), (a,b) -> Double.compare(evaluate((BohnenspielBoardState) a,pid,eid),evaluate((BohnenspielBoardState) b,pid,eid)));
    	return states;
    }
    
    //Takes the current state, the moves under consideration, and the pid and eid. Returns the ordered list of moves caused by
    //applying the moves to state, with respect to the evaluation function.
    public static BohnenspielMove[] orderMoves (BohnenspielBoardState state, BohnenspielMove[] moves,int pid, int eid)
    {
    	Collections.sort(Arrays.asList(moves), (a,b) -> Double.compare(evaluate((BohnenspielBoardState) moveToState(state,a),pid,eid),evaluate((BohnenspielBoardState) moveToState(state,b),pid,eid)));
    	return moves;
    }
    
    //Checks if a given elapsed time is over the cutoff. Returns true if it is over the cutoff, false if time still remains.
    public static boolean timeOut(long time) {
    	return time>=timeCutoffNano;
    	//if (time<timeCutoffNano)
    	//{
    		//return false;
    	//}
    	//else
    	//{
    		//return true;
    	//}
    }
    
    
    
    //First AB attempt, from book, trying slide code instead
    /*public static double maxValue (BohnenspielBoardState state, double alpha, double beta, int pid, int eid, int depthLimit, int depthNow, long startT) {
    	if (cutoff(depthLimit, depthNow) || state.gameOver() || timeOut(System.nanoTime()-startT)) 
    	{
    		return evaluate(state, pid, eid);
    	}
    	else
    	{
    		double val=Double.NEGATIVE_INFINITY;
    		ArrayList<BohnenspielMove> moves = state.getLegalMoves();
    		for(BohnenspielMove m : moves)
    		{
    			BohnenspielBoardState testState = (BohnenspielBoardState) state.clone();
    			testState.move(m);
    			val = Math.max(val, minValue(testState,alpha,beta,pid,eid,depthLimit,depthNow+1,startT));
    			if (val>=beta) 
    			{
    				return val;
    			}
    			alpha=Math.max(alpha, val);
    		}
    		return val;
    	}
    }*/
    
    public static double maxValue (BohnenspielBoardState state, double alpha, double beta, int pid, int eid, int depthLimit, int depthNow, long startT) {
    	if (cutoff(depthLimit, depthNow) || state.gameOver() || timeOut(System.nanoTime()-startT)) 
    	{
    		return evaluate(state, pid, eid);
    	}
    	else
    	{
    		ArrayList<BohnenspielMove> moves = state.getLegalMoves();
    		if (moves.size()==0)
    		{
    			return evaluate(state,pid,eid);
    		}
    		for(BohnenspielMove m : moves)
    		{
    			BohnenspielBoardState testState = (BohnenspielBoardState) state.clone();
    			testState.move(m);
    			alpha = Math.max(alpha, minValue(testState,alpha,beta,pid,eid,depthLimit,depthNow+1,startT));
    			if (alpha>=beta)
    			{
    				return beta;
    			}
    		}
    		return alpha;
    	}
    }
    
    //Same reasons as for maxval, optimization
    /*public static double minValue (BohnenspielBoardState state, double alpha, double beta, int pid, int eid, int depthLimit, int depthNow, long startT) {
    	if (cutoff(depthLimit,depthNow) || state.gameOver() || timeOut(System.nanoTime()-startT)) 
    	{
    		return evaluate(state, pid, eid);
    	}
    	else
    	{
    		double val=Double.POSITIVE_INFINITY;
    		ArrayList<BohnenspielMove> moves = state.getLegalMoves();
    		for(BohnenspielMove m : moves)
    		{
    			BohnenspielBoardState testState = (BohnenspielBoardState) state.clone();
    			testState.move(m);
    			val = Math.min(val, maxValue(testState,alpha,beta,pid,eid,depthLimit,depthNow+1,startT));
    			if (val<=alpha)
    			{
    				return val;
    			}
    			beta=Math.min(beta, val);
    		}
    		return val;
    	}
    }*/
    
    public static double minValue (BohnenspielBoardState state, double alpha, double beta, int pid, int eid, int depthLimit, int depthNow, long startT) {
    	if (cutoff(depthLimit,depthNow) || state.gameOver() || timeOut(System.nanoTime()-startT)) 
    	{
    		return evaluate(state, pid, eid);
    	}
    	else
    	{
    		ArrayList<BohnenspielMove> moves = state.getLegalMoves();
    		for(BohnenspielMove m : moves)
    		{
    			BohnenspielBoardState testState = (BohnenspielBoardState) state.clone();
    			testState.move(m);
    			beta = Math.min(beta, maxValue(testState,alpha,beta,pid,eid,depthLimit,depthNow+1,startT));
    			if (beta<=alpha)
    			{
    				return alpha;
    			}
    		}
    		return beta;
    	}
    }
    
    public static BohnenspielMove abSearch (BohnenspielBoardState boardstate, int pid, int eid, int depthL, long startT) {
    	ArrayList<BohnenspielMove> unsortedMoves = boardstate.getLegalMoves();
    	int si = unsortedMoves.size();
    	BohnenspielMove[] moveList = new BohnenspielMove[si];
    	moveList=unsortedMoves.toArray(moveList);
    	BohnenspielMove[] orderedMoveList = orderMoves(boardstate,moveList,pid,eid);
    	double[] valuesOfEachState = new double[si]; //Value in space i of array corresponds to state in space i of orderedMoveList
    	//BohnenspielBoardState[] statesInOrder =  orderStates(boardstate, moveList, pid, eid);
    	for (int i=0; i<si;i++)
    	{
    		BohnenspielBoardState testState = (BohnenspielBoardState) boardstate.clone();
    		testState.move(orderedMoveList[i]);
    		valuesOfEachState[i] = maxValue(testState,Double.NEGATIVE_INFINITY,Double.POSITIVE_INFINITY, pid, eid, depthL, 1, startT);
    	}
    	//Now, we just find the index of the maximum value inside of the values array, and return the corresponding move.
    	int maxIndex=0;
    	for (int i=0;i<valuesOfEachState.length;i++)
    	{
    		//if(valuesOfEachState[i]==Double.POSITIVE_INFINITY || valuesOfEachState[i]==Double.NEGATIVE_INFINITY)
    		//{
    			//System.out.println("Found bad infinity");
    		//}
    		if(valuesOfEachState[i]>valuesOfEachState[maxIndex])
    		{
    			maxIndex=i;
    		}
    	}
    	//System.out.println(Arrays.toString(valuesOfEachState)+" , "+maxIndex);
    	return orderedMoveList[maxIndex];
    	//throw new java.lang.Error("this is very bad, the moves do not match the value..."); //implement try catch later
    }
    
    /*public static BohnenspielMove abMinimaxDecision(BohnenspielBoardState boardstate, int pid, int eid) {
    	ArrayList<BohnenspielMove> moves = boardstate.getLegalMoves();
    	BohnenspielMove bestMove = moves.get(rand.nextInt(moves.size())); //later I need to change this from random -- Because the ordering will affect the abpruning
    	BohnenspielBoardState bestMoveState = boardstate;
    	bestMoveState.move(bestMove);
    	int bestMoveValue = evaluate(bestMoveState, pid, eid); //don't change to ab value -- This is just a bit of a placeholder, as a one depth check on it, and then I'll check the same move with the ab decision in the loop
    	for (BohnenspielMove m : moves)
    	{
    		BohnenspielBoardState testState = boardstate;
    		testState.move(m);
    		int testStateValue = evaluate(testState, pid, eid); //need to change to ab value
    		if (bestMoveValue == -1000)
    		{
    			bestMove=m;
    			bestMoveValue=testStateValue;
    		}
    		if (testStateValue > bestMoveValue)
    		{
    			bestMove=m;
    			bestMoveValue=testStateValue;
    		}
    		return bestMove;
    	}
    	return bestMove;
    }*/
}
