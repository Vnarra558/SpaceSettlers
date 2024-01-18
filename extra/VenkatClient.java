package narr0004;

import java.util.Vector;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.UUID;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Stack;
import java.awt.Color;

import spacesettlers.actions.AbstractAction;
import spacesettlers.actions.DoNothingAction;
import spacesettlers.actions.MoveAction;
import spacesettlers.actions.PurchaseCosts;
import spacesettlers.actions.PurchaseTypes;
import spacesettlers.game.AbstractGameAgent;
import spacesettlers.graphics.CircleGraphics;
import spacesettlers.graphics.LineGraphics;
import spacesettlers.graphics.SpacewarGraphics;
import spacesettlers.graphics.TargetGraphics;
import spacesettlers.objects.AbstractActionableObject;
import spacesettlers.objects.AbstractObject;
import spacesettlers.objects.Ship;
import spacesettlers.objects.Asteroid;
import spacesettlers.objects.Beacon;
import spacesettlers.objects.powerups.SpaceSettlersPowerupEnum;
import spacesettlers.objects.resources.ResourcePile;
import spacesettlers.simulator.Toroidal2DPhysics;
import spacesettlers.utilities.Position;
import spacesettlers.utilities.Vector2D;
import spacesettlers.clients.TeamClient;

import java.util.*;
/**
 * A team of random agents
 *
 * The agents pick a random location in space and aim for it.  They shoot somewhat randomly also.
 * @author amy
 *
 */

class GraphEdge {
    public GraphNode source; // sourceNode to the edge
    public GraphNode destination; // destinationNode to the edge
    public Vector2D edgeVector; // edge in the vector form

    Color edgeColor; // color of the edge

    public GraphEdge(Toroidal2DPhysics space, GraphNode s, GraphNode d) { // initialize edge with source, desitation & distance vector
        source = s;
        destination = d;
        edgeVector = space.findShortestDistanceVector(s.getPosition(), d.getPosition());
    }

    public void setColor(int r, int g, int b) { // set the edge color
        edgeColor = new Color(r, g, b);
    }

    public double getDistance() { // get the distance value
        return edgeVector.getMagnitude();
    }

    public SpacewarGraphics getGraphic() { // use LineGraphics for edges
        LineGraphics graphic = new LineGraphics(source.getPosition(), destination.getPosition(), edgeVector);
        graphic.setLineColor(edgeColor);
        return graphic;
    }
}

class GraphNode {
    public int id; // Every node has a unique ID
    public Vector<GraphEdge> edges; // All edges connected to a node

    Position position; // node Position in space
    Color nodeColor; // node color

    public GraphNode(Position p, int nodeId) { // initialize the node with position, id & empty list of edges
        position = p;
        id = nodeId;
        edges = new Vector<GraphEdge>();
    }

    public void setColor(int r, int g, int b) { // set the node color
        nodeColor = new Color(r, g, b);
    }

    public Color getColor() { // get the node color
        return nodeColor;
    }

    public void addEdge(GraphEdge edge) { // add a new edge
        if(edge.source.id == this.id)
            edges.add(edge);
    }

    public boolean removeEdge(GraphEdge edge) { // remove an edge
        return edges.remove(edge);
    }

    public Position getPosition() { // get the position of current node
        return position;
    }

    public Vector<GraphNode> getConnectedNodes() { // Get the list of all connected nodes to this node
        Vector<GraphNode> connectedNodes = new Vector<GraphNode>();
        for (GraphEdge edge: edges) {
            connectedNodes.add(edge.destination);
        }
        return connectedNodes;
    }

    public SpacewarGraphics getGraphic() { // use circle graphic with radius 5 for Node
        SpacewarGraphics graphic = new CircleGraphics(5, nodeColor, position);
        return graphic;
    }
}

class GraphSearchNode {
    public GraphNode node; // destination node object to search
    public Vector<GraphEdge> path; // search PATH to destination node
    public double totalDistance; // total distance

    GraphSearchNode(GraphNode n, Vector<GraphEdge> p, double distance) {
        node = n;
        path = p;
        totalDistance = distance;
    }

    public boolean isEdgeinPath(GraphEdge edge) { // function returs true if a edge is present in the searchPath
        for(GraphEdge e: path) {
            if((e.source.id == edge.source.id) && (e.destination.id == edge.destination.id))
                return true;
        }
        return false;
    }
}

class DFSGraph {
    GraphNode[] nodes; // Nodes object list
    GraphSearchNode searchNode; // SearchNode object

    GraphNode resetNode; // random startnode - used while replanning
    public GraphNode start; // startNode
    public GraphNode target; // goalNode
    Vector<GraphEdge> startEdges; // all edges connected to startNode
    Vector<GraphEdge> targetEdges; // all edges connected to goalNode

    public DFSGraph(Toroidal2DPhysics space, Random random, int nodesCount) {

        Position[] nodePositions = new Position[nodesCount]; // nodePositions
        for(int i=0; i<nodesCount; i++) {
            nodePositions[i] = space.getRandomFreeLocation(random, 20); // randomly assign positions to all the nodes
        }

        resetNode = new GraphNode(space.getRandomFreeLocation(random, 20), Integer.MIN_VALUE); // new random node for startNode
        nodes = new GraphNode[nodesCount];
        for(int i=0; i<nodePositions.length; i++) {
            nodes[i] = new GraphNode(nodePositions[i], i); // create Node objects
            nodes[i].setColor(128, 128, 128); // set gray color to all node objects
        }

        Set<Asteroid> asteroids = space.getAsteroids(); // get the set of Astroids
        Set<AbstractObject> obstructions = new HashSet<AbstractObject>();
        for(Asteroid asteroid: asteroids) {
            if(!asteroid.isMineable())
                obstructions.add(asteroid); // if the asteroid is not minanble then add it to obstructions list
        }

        for(int i=0; i<nodesCount; i++) {
            for(int j=0; j<nodesCount; j++) {
                if(i != j && space.isPathClearOfObstructions(nodes[i].getPosition(), nodes[j].getPosition(), obstructions, 10)) { // check if the path is clear of objstructions
                    double distance = space.findShortestDistance(nodePositions[i], nodePositions[j]); // find the distance between nodes
                    if((distance < 10.0)|| (distance > 50.0 && distance < 120.0)) { // add a constraint to numnber of edges - constraint is used to limit the number of edges, if there is no contraint then the graph will be too messy
                        GraphEdge newEdge = new GraphEdge(space, nodes[i], nodes[j]); // create an edge object with source & destination nodes
                        newEdge.setColor(128, 128, 128); // set gray color to each edge
                        nodes[i].addEdge(newEdge); // add the edge to source node
                    }
                }
            }
        }
    }

    public boolean isTargetReached(Toroidal2DPhysics space, Position p) {
        double distance = space.findShortestDistance(p, target.getPosition()); // check if target is reached
        if(distance < 5) return true; // since the SHIP has a radius, if the target reaches < 5 then defined as Target
        else return false;
    }

    public GraphNode resetTarget(Toroidal2DPhysics space) {
        if(startEdges != null) {
            for(GraphEdge edge: startEdges) {
                edge.source.removeEdge(edge); // remove all the startNode edges
            }
        }
        if(targetEdges != null) {
            for(GraphEdge edge: targetEdges) {
                edge.source.removeEdge(edge); // remove all targetNode edges
            }
        }

        if (start == null) {
            start = resetNode; start.id = -1; // if the startNode is not assinged then assign it to resetNode
            start.setColor(0, 255, 0); // set green color to startNode

        } else {
            start = target; start.id = -1;
            start.setColor(0, 255, 0);
        }

        startEdges = new Vector<GraphEdge>();
        for(int i=0; i<nodes.length; i++) {
            double distance = space.findShortestDistance(start.getPosition(), nodes[i].getPosition()); // add edges to startNode
            if((distance < 10.0)|| (distance > 50.0 && distance < 120.0)) { // constraint to add edges to Node
                GraphEdge newEdge = new GraphEdge(space, start, nodes[i]);
                newEdge.setColor(128, 128, 128); // set edge color to gray
                start.addEdge(newEdge); // add the edges to startNode
                startEdges.add(newEdge); // create a separate list of startNode edges - used to clean it in the next replanning phase
            }
        }

        Set<Beacon> beacons = space.getBeacons(); // get the Beacons set
        Beacon closestBeacon = null;
        double bestDistance = Double.POSITIVE_INFINITY;
        for (Beacon beacon : beacons) {
            double dist = space.findShortestDistance(start.getPosition(), beacon.getPosition());
            if (dist < bestDistance) {
                bestDistance = dist;
                closestBeacon = beacon;
            }
        }

        target = new GraphNode(closestBeacon.getPosition(), -2); // create a new goalNode
        target.setColor(255, 0, 0); // set the color to Red
        targetEdges = new Vector<GraphEdge>(); // edges of goalNode
        for(int i=0; i<nodes.length; i++) {
            double distance = space.findShortestDistance(target.getPosition(), nodes[i].getPosition());
            if((distance < 10.0)|| (distance > 50.0 && distance < 120.0)) { // assign the edges to goalNode based on constraint
                GraphEdge newEdge = new GraphEdge(space, nodes[i], target); // create edges for goalNode
                newEdge.setColor(128, 128, 128); // set edge color to gray
                nodes[i].addEdge(newEdge); // add the edges to source of goalNode
                targetEdges.add(newEdge); // create a separate list of goalNode edges - used to clean it in the next replanning phase
            }
        }

        return start;
    }

    public GraphSearchNode searchTarget(Toroidal2DPhysics space) {
        Stack<GraphSearchNode> frontier = new Stack<GraphSearchNode>(); // frontier Stack (LIFO Queue) used for DFS
        HashMap<Integer, GraphSearchNode> explored = new HashMap<Integer, GraphSearchNode>(); // explored HashSet to keep track of explored nodes

        Vector<GraphEdge> initPath = new Vector<GraphEdge>();
        searchNode = new GraphSearchNode(target, initPath, Integer.MAX_VALUE); // initialize the searchNode - this node stores the DFS solution
        GraphSearchNode initSNode = new GraphSearchNode(start, initPath, 0.0); // create an init searchNode to start DFS search
        frontier.push(initSNode); // add the initNode to frontier Queue

        while(!frontier.isEmpty()) { // Keep searching until the Frontier Queue is empty
            GraphSearchNode sn = frontier.pop(); // poll the frontier queue

            if(explored.get(sn.node.id) != null) { // If the node is already explored then check the newDistance & update the explored node
                if(sn.totalDistance < explored.get(sn.node.id).totalDistance)
                    explored.put(sn.node.id, sn);
            } else {
                explored.put(sn.node.id, sn); // Move the node from Frontier to Explored
            }

            if(sn.node.id == target.id) { // check if the search reached targetNode
                if(sn.totalDistance < searchNode.totalDistance) { // if the targetNode totalDistance is better than current distance then update the searchNode and return the solution
                    searchNode.path = sn.path;
                    searchNode.totalDistance = sn.totalDistance;
                    System.out.printf("Target Found!"); // HURRAY: we found the target!
                    return searchNode;
                }
            }

            for(GraphEdge e: sn.node.edges) {
                if(e.source.id != sn.node.id) // If the edge does not belong to current node then something is broken!
                    System.out.printf("Something is broken! (%d %d)", e.source.id, sn.node.id);

                GraphNode newNode = e.destination; // get the child node
                Vector<GraphEdge> newPath = new Vector<GraphEdge>(sn.path); newPath.add(e); // newPath to desitnation
                double newDistance = sn.totalDistance + e.getDistance(); // totalDistance to destination so far

                GraphSearchNode new_sn = new GraphSearchNode(newNode, newPath, newDistance); // create a new searchNode

                if(explored.get(new_sn.node.id) != null) { // If the child is already explored then dont add it to Frontier
                    if(newDistance < explored.get(new_sn.node.id).totalDistance)
                        explored.put(new_sn.node.id, new_sn);
                } else {
                    frontier.add(new_sn); // add the child node to Frontier Queue
                }
            }
        }

        return searchNode; // return the searchNode
    }

    public Set<SpacewarGraphics> getGraphics() {
        HashSet<SpacewarGraphics> newGraphics = new HashSet<SpacewarGraphics>();
        SpacewarGraphics graphic;

        for(GraphNode node: nodes) { // Graphics for Nodes
            graphic = node.getGraphic();
            newGraphics.add(graphic);

            for(GraphEdge edge: node.edges) { // Graphics for edges of each Node
                if(!searchNode.isEdgeinPath(edge)) {
                    graphic = edge.getGraphic();
                    newGraphics.add(graphic);
                }
            }
        }

        for(GraphEdge edge: startEdges) { // Graphics for startNode edges
            if(!searchNode.isEdgeinPath(edge)) {
                graphic = edge.getGraphic();
                newGraphics.add(graphic);
            }
        }

        for(GraphEdge edge: targetEdges) { // Graphics for targetNode edges
            if(!searchNode.isEdgeinPath(edge)) {
                graphic = edge.getGraphic();
                newGraphics.add(graphic);
            }
        }

        for(GraphEdge edge: searchNode.path) { // set the searchPath color to RED
            LineGraphics lGraphic = new LineGraphics(edge.source.getPosition(), edge.destination.getPosition(), edge.edgeVector);
            Color edgeColor = new Color(255, 0, 0);
            lGraphic.setLineColor(edgeColor);
            lGraphic.setStrokeWidth(5);
            newGraphics.add(lGraphic);
        }

        graphic = new TargetGraphics(20, start.getColor(), start.getPosition()); // Graphics for startNode
        newGraphics.add(graphic);

        graphic = new TargetGraphics(20, target.getColor(), target.getPosition()); // Graphics for goalNode
        newGraphics.add(graphic);

        return newGraphics; // return the Graphics
    }

}




public class VenkatClient extends TeamClient {
    HashSet<SpacewarGraphics> graphics;
    boolean fired = false;
    Position currentTarget;

    public static int RANDOM_MOVE_RADIUS = 200;
    public static double SHOOT_PROBABILITY = 0.1;

    @Override
    public void initialize(Toroidal2DPhysics space) {
        graphics = new HashSet<SpacewarGraphics>();
        currentTarget = null;
    }

    @Override
    public void shutDown(Toroidal2DPhysics space) {
        // TODO Auto-generated method stub

    }


    @Override
    public Map<UUID, AbstractAction> getMovementStart(Toroidal2DPhysics space,
                                                      Set<AbstractActionableObject> actionableObjects) {
        HashMap<UUID, AbstractAction> randomActions = new HashMap<UUID, AbstractAction>();


        for (AbstractObject actionable :  actionableObjects) {
            if (actionable instanceof Ship) {
                Ship ship = (Ship) actionable;
                AbstractAction current = ship.getCurrentAction();

                // if we finished, make a new spot in space to aim for
                if (current == null || current.isMovementFinished(space)) {
//                    Position currentPosition = ship.getPosition();
//                    Position newGoal = space.getRandomFreeLocationInRegion(random, Ship.SHIP_RADIUS, (int) currentPosition.getX(),
//                            (int) currentPosition.getY(), RANDOM_MOVE_RADIUS);
//                    currentTarget = newGoal;
                    MoveAction newAction = null;
                    newAction = (MoveAction) getToStar(space,ship);
                    //System.out.println("Ship is at " + currentPosition + " and goal is " + newGoal);
                    randomActions.put(ship.getId(), newAction);
                } else {
                    randomActions.put(ship.getId(), ship.getCurrentAction());
                }

            } else {
                // it is a base and random doesn't do anything to bases
                randomActions.put(actionable.getId(), new DoNothingAction());
            }


        }

        return randomActions;

    }

    private AbstractAction getToStar(Toroidal2DPhysics space, Ship ship){
        Position currPosition = ship.getPosition();
        AbstractAction newAction = null;
        Star star = pickNearestStar(space,ship);

        if(star !=null){
            newAction = new MoveAction(space,currPosition, star.getPosition());
        }
        else {
            newAction = new DoNothingAction();
        }
        return newAction;
    }

    private Star pickNearestStar(Toroidal2DPhysics space, Ship ship){
        Set<Star> stars = space.getStars();
        Star closetStar = null;
        double bestDistance = Double.POSITIVE_INFINITY;
        for(Star star: stars){
            double dist = space.findShortestDistance(ship.getPosition(),star.getPosition());
            if(dist < bestDistance) {
                bestDistance = dist;
                closetStar = star;
            }
        }
        return closetStar;
    }

    @Override
    public void getMovementEnd(Toroidal2DPhysics space, Set<AbstractActionableObject> actionableObjects) {
    }

    @Override
    public Set<SpacewarGraphics> getGraphics() {
        HashSet<SpacewarGraphics> newGraphics = new HashSet<SpacewarGraphics>();
        if (currentTarget != null) {
            SpacewarGraphics graphic = new TargetGraphics(20, getTeamColor(), this.currentTarget);
            newGraphics.add(graphic);
        }
        return newGraphics;
    }


    @Override
    /**
     * Random never purchases
     */
    public Map<UUID, PurchaseTypes> getTeamPurchases(Toroidal2DPhysics space,
                                                     Set<AbstractActionableObject> actionableObjects,
                                                     ResourcePile resourcesAvailable,
                                                     PurchaseCosts purchaseCosts) {
        return new HashMap<UUID,PurchaseTypes>();

    }

    /**
     * This is the new way to shoot (and use any other power up once they exist)
     */
    public Map<UUID, SpaceSettlersPowerupEnum> getPowerups(Toroidal2DPhysics space,
                                                           Set<AbstractActionableObject> actionableObjects) {

        HashMap<UUID, SpaceSettlersPowerupEnum> powerupMap = new HashMap<UUID, SpaceSettlersPowerupEnum>();

        for (AbstractObject actionable :  actionableObjects) {
            if (actionable instanceof Ship) {
                Ship ship = (Ship) actionable;
                if (random.nextDouble() < SHOOT_PROBABILITY) {
                    AbstractWeapon newBullet = ship.getNewWeapon(SpaceSettlersPowerupEnum.FIRE_MISSILE);
                    if (newBullet == null) {
                        powerupMap.put(ship.getId(), SpaceSettlersPowerupEnum.FIRE_MISSILE);
                        //System.out.println("Firing!");
                    }
                }
            }
        }
        return powerupMap;
    }

    @Override
    public Map<UUID, AbstractGameAgent> getGameSearch(Toroidal2DPhysics space,
                                                      Set<AbstractActionableObject> actionableObjects) {
        // TODO Auto-generated method stub
        return null;
    }


}