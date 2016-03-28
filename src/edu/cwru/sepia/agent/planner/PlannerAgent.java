package edu.cwru.sepia.agent.planner;

import edu.cwru.sepia.action.Action;
import edu.cwru.sepia.agent.Agent;
import edu.cwru.sepia.agent.planner.actions.StripsAction;
import edu.cwru.sepia.environment.model.history.History;
import edu.cwru.sepia.environment.model.state.State;

import java.io.*;
import java.util.*;

/**
 * Created by Devin on 3/15/15.
 */
public class PlannerAgent extends Agent {

    final int requiredWood;
    final int requiredGold;
    final boolean buildPeasants;

    // Your PEAgent implementation. This prevents you from having to parse the text file representation of your plan.
    PEAgent peAgent;

    public PlannerAgent(int playernum, String[] params) {
        super(playernum);

        if(params.length < 3) {
            System.err.println("You must specify the required wood and gold amounts and whether peasants should be built");
        }

        requiredWood = Integer.parseInt(params[0]);
        requiredGold = Integer.parseInt(params[1]);
        buildPeasants = Boolean.parseBoolean(params[2]);


        System.out.println("required wood: " + requiredWood + " required gold: " + requiredGold + " build Peasants: " + buildPeasants);
    }

    @Override
    public Map<Integer, Action> initialStep(State.StateView stateView, History.HistoryView historyView) {

        Stack<StripsAction> plan = AstarSearch(new GameState(stateView, playernum, requiredGold, requiredWood, buildPeasants));

        if(plan == null) {
            System.err.println("No plan was found");
            System.exit(1);
            return null;
        }

        // write the plan to a text file
        savePlan(plan);


        // Instantiates the PEAgent with the specified plan.
        peAgent = new PEAgent(playernum, plan);

        return peAgent.initialStep(stateView, historyView);
    }

    @Override
    public Map<Integer, Action> middleStep(State.StateView stateView, History.HistoryView historyView) {
        if(peAgent == null) {
            System.err.println("Planning failed. No PEAgent initialized.");
            return null;
        }

        return peAgent.middleStep(stateView, historyView);
    }

    @Override
    public void terminalStep(State.StateView stateView, History.HistoryView historyView) {

    }

    @Override
    public void savePlayerData(OutputStream outputStream) {

    }

    @Override
    public void loadPlayerData(InputStream inputStream) {

    }

    /**
     * Perform an A* search of the game graph. This should return your plan as a stack of actions. This is essentially
     * the same as your first assignment. The implementations should be very similar. The difference being that your
     * nodes are now GameState objects not MapLocation objects.
     *
     * @param startState The state which is being planned from
     * @return The plan or null if no plan is found.
     */
    private Stack<StripsAction> AstarSearch(GameState startState) {
    	
    	// Declare open and closed lists
    	PriorityQueue<GameState> openList = new PriorityQueue<GameState>();
    	HashSet<GameState> closedList = new HashSet<GameState>();
    	    	
    	// Add the starting location to the open list and empty the closed list
    	openList.add(startState);
    	    	
    	// While the openList is not empty
    	while (!openList.isEmpty()) {
    		
    		// Goal test
    		GameState curr = openList.peek();
    		if (curr.isGoal()) {
    			return buildStripsPlan(curr);
    		}
    		
    		// Move this node from open list to closed list
    		openList.remove(curr);
    		closedList.add(curr);
    		
    		// Look at every child of the step
    		List<GameState> children = curr.generateChildren();
    		for (GameState child: children) {
    			
    			// If the closed list contains this child, skip it
    			if (closedList.contains(child)) {
    				continue;
    			}
    			
    			// Calculate cost to the next state
    			// TODO: Make sure this is correct
    			double checkCost = curr.getCost() + child.heuristic();
    			
    			// If child node is not in open list
    			if (!openList.contains(child)) {
    				openList.add(child);
    			}
    			// Else if the estimated cost is greater than or equal to this child's cost
    			else if (checkCost >= child.getCost()) {
    				continue;
    			}
    			
    			// This path is the best yet - link it
    			child.setParent(curr);
    		}    		
    	}
    	
        return new Stack<StripsAction>();
    }
    
    /**
     * A helper method that will prepare the stack of actions based
     * on a list of GameState nodes
     * 
     * @param goal
     * @return
     */
    private Stack<StripsAction> buildStripsPlan(GameState goal) {
    	
    	// The stack to be returned with the plan
    	Stack<StripsAction> plan = new Stack<StripsAction>();
    	
    	GameState nodeptr = goal;
    	
    	while (nodeptr.getParent() != null) {
    		// TODO: push the action associated with this state onto the stack
    		// This action will be based on the previous state - it may be easiest to
    		// just generate this as we build the tree
    		nodeptr = nodeptr.getParent();
    	}
    	
    	return plan;
    }

    /**
     * This has been provided for you. Each strips action is converted to a string with the toString method. This means
     * each class implementing the StripsAction interface should override toString. Your strips actions should have a
     * form matching your included Strips definition writeup. That is <action name>(<param1>, ...). So for instance the
     * move action might have the form of Move(peasantID, X, Y) and when grounded and written to the file
     * Move(1, 10, 15).
     *
     * @param plan Stack of Strips Actions that are written to the text file.
     */
    private void savePlan(Stack<StripsAction> plan) {
        if (plan == null) {
            System.err.println("Cannot save null plan");
            return;
        }

        File outputDir = new File("saves");
        outputDir.mkdirs();

        File outputFile = new File(outputDir, "plan.txt");

        PrintWriter outputWriter = null;
        try {
            outputFile.createNewFile();

            outputWriter = new PrintWriter(outputFile.getAbsolutePath());

            Stack<StripsAction> tempPlan = (Stack<StripsAction>) plan.clone();
            while(!tempPlan.isEmpty()) {
                outputWriter.println(tempPlan.pop().toString());
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (outputWriter != null)
                outputWriter.close();
        }
    }
}
